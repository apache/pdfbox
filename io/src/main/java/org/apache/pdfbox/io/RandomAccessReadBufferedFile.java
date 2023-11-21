/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides random access to portions of a file combined with buffered reading of content. Start of next bytes to read
 * can be set via seek method.
 * 
 * File is accessed via {@link FileChannel} and is read in ByteBuffer chunks which are cached.
 * 
 * @author Timo Boehme
 */
public class RandomAccessReadBufferedFile implements RandomAccessRead
{
    private static final int PAGE_SIZE_SHIFT = 12;
    private static final int PAGE_SIZE = 1 << PAGE_SIZE_SHIFT;
    private static final long PAGE_OFFSET_MASK = -1L << PAGE_SIZE_SHIFT;
    private static final int MAX_CACHED_PAGES = 1000;

    // map holding all copies of the current buffered file
    private final ConcurrentMap<Long, RandomAccessReadBufferedFile> rafCopies = new ConcurrentHashMap<>();

    private ByteBuffer lastRemovedCachePage = null;

    /** Create a LRU page cache. */
    private final Map<Long, ByteBuffer> pageCache = new LinkedHashMap<>(MAX_CACHED_PAGES, 0.75f,
            true)
    {
        private static final long serialVersionUID = -6302488539257741101L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, ByteBuffer> eldest)
        {
            final boolean doRemove = size() > MAX_CACHED_PAGES;
            if (doRemove)
            {
                lastRemovedCachePage = eldest.getValue();
                lastRemovedCachePage.clear();
            }
            return doRemove;
        }
    };

    private long curPageOffset = -1;
    private ByteBuffer curPage;
    private int offsetWithinPage = 0;

    private final FileChannel fileChannel;
    private final Path path;
    private final long fileLength;
    private long fileOffset = 0;
    private boolean isClosed;
    
    /**
     * Create a random access buffered file instance for the file with the given name.
     *
     * @param filename the filename of the file to be read.
     * @throws IOException if something went wrong while accessing the given file.
     */
    public RandomAccessReadBufferedFile( String filename ) throws IOException 
    {
        this(new File(filename));
    }

    /**
     * Create a random access buffered file instance for the given file.
     *
     * @param file the file to be read.
     * @throws IOException if something went wrong while accessing the given file.
     */
    public RandomAccessReadBufferedFile( File file ) throws IOException 
    {
        this(file.toPath());
    }

    /**
     * Create a random access buffered file instance using the given path.
     *
     * @param path path of the file to be read.
     * @throws IOException if something went wrong while accessing the given file.
     */
    public RandomAccessReadBufferedFile(Path path) throws IOException
    {
        this.path = path;
        fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        fileLength = fileChannel.size();
        seek(0);
    }

    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return fileOffset;
    }
    
    /**
     * Seeks to new position. If new position is outside of current page the new page is either
     * taken from cache or read from file and added to cache.
     *
     * @param position the position to seek to.
     * @throws java.io.IOException if something went wrong.
     */
    @Override
    public void seek( final long position ) throws IOException
    {
        checkClosed();
        if (position < 0)
        {
            throw new IOException("Invalid position " + position);
        }
        final long newPageOffset = position & PAGE_OFFSET_MASK;
        if ( newPageOffset != curPageOffset )
        {
            ByteBuffer newPage = pageCache.get(newPageOffset);
            if ( newPage == null )
            {
                fileChannel.position(newPageOffset);
                newPage = readPage();
                pageCache.put( newPageOffset, newPage );
            }
            curPageOffset = newPageOffset;
            curPage = newPage;
        }

        fileOffset = Math.min(position, fileLength);
        offsetWithinPage = (int) (fileOffset - curPageOffset);
    }
    
    /**
     * Reads a page with data from current file position. If we have a
     * previously removed page from cache the buffer of this page is reused.
     * Otherwise a new byte buffer is created.
     */
    private ByteBuffer readPage() throws IOException
    {
        ByteBuffer page;

        if ( lastRemovedCachePage != null )
        {
            page = lastRemovedCachePage;
            lastRemovedCachePage = null;
        }
        else
        {
            page = ByteBuffer.allocate(PAGE_SIZE);
        }

        int readBytes = 0;
        while (readBytes < PAGE_SIZE)
        {
            int curBytesRead = fileChannel.read(page);
            if (curBytesRead < 0)
            {
                // EOF
                break;
            }
            readBytes += curBytesRead;
        }

        return page;
    }
    
    @Override
    public int read() throws IOException
    {
        checkClosed();
        if ( fileOffset >= fileLength )
        {
            return -1;
        }

        if (offsetWithinPage == PAGE_SIZE)
        {
            seek( fileOffset );
        }

        fileOffset++;
        return curPage.get(offsetWithinPage++) & 0xff;
    }
    
    @Override
    public int read( byte[] b, int off, int len ) throws IOException
    {
        checkClosed();
        if ( fileOffset >= fileLength )
        {
            return -1;
        }

        if (offsetWithinPage == PAGE_SIZE)
        {
            seek( fileOffset );
        }

        int commonLen = Math.min(PAGE_SIZE - offsetWithinPage, len);
        if ((fileLength - fileOffset) < PAGE_SIZE)
        {
            commonLen = Math.min( commonLen, (int) ( fileLength - fileOffset ) );
        }

        curPage.position(offsetWithinPage);
        curPage.get(b, off, commonLen);

        offsetWithinPage += commonLen;
        fileOffset += commonLen;

        return commonLen;
    }
    
    @Override
    public long length() throws IOException
    {
        return fileLength;
    }
    
    @Override
    public void close() throws IOException
    {
        rafCopies.values().forEach(IOUtils::closeQuietly);
        rafCopies.clear();
        fileChannel.close();
        pageCache.clear();
        isClosed = true;
    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    /**
     * Ensure that the RandomAccessBuffer is not closed
     * @throws IOException If RandomAccessBuffer already closed
     */
    private void checkClosed() throws IOException
    {
        if (isClosed)
        {
            throw new IOException(getClass().getName() + " already closed");
        }
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return peek() == -1;
    }

    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength) throws IOException
    {
        checkClosed();
        Long currentThreadID = Thread.currentThread().getId();
        RandomAccessReadBufferedFile randomAccessReadBufferedFile = rafCopies.get(currentThreadID);
        if (randomAccessReadBufferedFile == null || randomAccessReadBufferedFile.isClosed())
        {
            randomAccessReadBufferedFile = new RandomAccessReadBufferedFile(path);
            rafCopies.put(currentThreadID, randomAccessReadBufferedFile);
        }
        return new RandomAccessReadView(randomAccessReadBufferedFile, startPosition, streamLength);
    }

}
