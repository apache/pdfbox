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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides {@link InputStream} access to portions of a file combined with
 * buffered reading of content. Start of next bytes to read can be set via seek
 * method.
 * 
 * File is accessed via {@link RandomAccessFile} and is read in byte chunks
 * which are cached.
 * 
 * @author Timo Boehme
 */
public class RandomAccessBufferedFileInputStream
extends InputStream implements RandomAccessRead
{
    /**
     * The prefix for the temp file being used. 
     */
    private static final String TMP_FILE_PREFIX = "tmpPDFBox";

    private int pageSizeShift = 12;
    private int pageSize = 1 << pageSizeShift;
    private long pageOffsetMask = -1L << pageSizeShift;
    private int maxCachedPages = 1000;
    private File tempFile;

    private byte[] lastRemovedCachePage = null;

    /** Create a LRU page cache. */
    private final Map<Long, byte[]> pageCache =
        new LinkedHashMap<Long, byte[]>( maxCachedPages, 0.75f, true )
    {
        private static final long serialVersionUID = -6302488539257741101L;

        @Override
        protected boolean removeEldestEntry( Map.Entry<Long, byte[]> eldest )
        {
            final boolean doRemove = size() > maxCachedPages;
            if (doRemove)
            {
                lastRemovedCachePage = eldest.getValue();
            }
            return doRemove;
        }
    };

    private long curPageOffset = -1;
    private byte[] curPage = new byte[pageSize];
    private int offsetWithinPage = 0;

    private final RandomAccessFile raFile;
    private final long fileLength;
    private long fileOffset = 0;
    private boolean isClosed;
    
    /**
     * Create a random access input stream instance for the file with the given name.
     *
     * @param filename the filename of the file to be read.
     * @throws IOException if something went wrong while accessing the given file.
     */
    public RandomAccessBufferedFileInputStream( String filename ) throws IOException 
    {
        this(new File(filename));
    }

    /** 
     * Create a random access input stream instance for the given file.
     *
     * @param file the file to be read.
     * @throws IOException if something went wrong while accessing the given file.
     */
    public RandomAccessBufferedFileInputStream( File file ) throws IOException 
    {
        raFile = new RandomAccessFile(file, "r");
        fileLength = file.length();
        seek(0);
    }

    /**
     * Create a random access input stream for the given input stream by copying the data to a
     * temporary file.
     *
     * @param input the input stream to be read. It will be closed by this method.
     * @throws IOException if something went wrong while creating the temporary file.
     */
    public RandomAccessBufferedFileInputStream( InputStream input ) throws IOException 
    {
        tempFile = createTmpFile(input);
        fileLength = tempFile.length();
        raFile = new RandomAccessFile(tempFile, "r");
        seek(0);
    }

    private File createTmpFile(InputStream input) throws IOException
    {
        File tmpFile = File.createTempFile(TMP_FILE_PREFIX, ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tmpFile))
        {
            IOUtils.copy(input, fos);
            return tmpFile;
        }
        finally
        {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Remove the temporary file. A temporary file is created if this class is instantiated with an InputStream
     */
    private void deleteTempFile()
    {
        if (tempFile != null)
        {
            tempFile.delete();
        }
    }

    /** Returns offset in file at which next byte would be read. */
    @Override
    public long getPosition()
    {
        return fileOffset;
    }
    
    /**
     * Seeks to new position. If new position is outside of current page the new page is either
     * taken from cache or read from file and added to cache.
     *
     * @param newOffset the position to seek to.
     * @throws java.io.IOException if something went wrong.
     */
    @Override
    public void seek( final long newOffset ) throws IOException
    {
        final long newPageOffset = newOffset & pageOffsetMask;
        if ( newPageOffset != curPageOffset )
        {
            byte[] newPage = pageCache.get( newPageOffset );
            if ( newPage == null )
            {
                raFile.seek( newPageOffset );
                newPage = readPage();
                pageCache.put( newPageOffset, newPage );
            }
            curPageOffset = newPageOffset;
            curPage = newPage;
        }

        offsetWithinPage = (int) ( newOffset - curPageOffset );
        fileOffset = newOffset;
    }
    
    /**
     * Reads a page with data from current file position. If we have a
     * previously removed page from cache the buffer of this page is reused.
     * Otherwise a new byte buffer is created.
     */
    private byte[] readPage() throws IOException
    {
        byte[] page;

        if ( lastRemovedCachePage != null )
        {
            page = lastRemovedCachePage;
            lastRemovedCachePage = null;
        }
        else
        {
            page = new byte[pageSize];
        }

        int readBytes = 0;
        while ( readBytes < pageSize )
        {
            int curBytesRead = raFile.read( page, readBytes, pageSize - readBytes);
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
        if ( fileOffset >= fileLength )
        {
            return -1;
        }

        if ( offsetWithinPage == pageSize )
        {
            seek( fileOffset );
        }

        fileOffset++;
        return curPage[offsetWithinPage++] & 0xff;
    }
    
    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }
    
    @Override
    public int read( byte[] b, int off, int len ) throws IOException
    {
        if ( fileOffset >= fileLength )
        {
            return -1;
        }

        if ( offsetWithinPage == pageSize )
        {
            seek( fileOffset );
        }

        int commonLen = Math.min( pageSize - offsetWithinPage, len );
        if ( ( fileLength - fileOffset ) < pageSize )
        {
            commonLen = Math.min( commonLen, (int) ( fileLength - fileOffset ) );
        }

        System.arraycopy( curPage, offsetWithinPage, b, off, commonLen );

        offsetWithinPage += commonLen;
        fileOffset += commonLen;

        return commonLen;
    }
    
    @Override
    public int available() throws IOException
    {
        return (int) Math.min( fileLength - fileOffset, Integer.MAX_VALUE );
    }
    
    @Override
    public long skip( long n ) throws IOException
    {
        // test if we have to reduce skip count because of EOF
        long toSkip = n;

        if ( fileLength - fileOffset < toSkip )
        {
            toSkip = fileLength - fileOffset;
        }

        if ( ( toSkip < pageSize ) && ( ( offsetWithinPage + toSkip ) <= pageSize ) )
        {
            // we can skip within current page
            offsetWithinPage += toSkip;
            fileOffset += toSkip;
        }
        else
        {
            // seek to the page we will get after skipping
            seek( fileOffset + toSkip );
        }

        return toSkip;
    }
    
    @Override
    public long length() throws IOException
    {
        return fileLength;
    }
    
    @Override
    public void close() throws IOException
    {
        raFile.close();
        deleteTempFile();
        pageCache.clear();
        isClosed = true;
    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    @Override
    public int peek() throws IOException
    {
        int result = read();
        if (result != -1)
        {
            rewind(1);
        }
        return result;
    }

    @Override
    public void rewind(int bytes) throws IOException
    {
        seek(getPosition() - bytes);
    }

    @Override
    public byte[] readFully(int length) throws IOException
    {
        byte[] b = new byte[length];
        int bytesRead = read(b);
        while(bytesRead < length)
        {
            bytesRead += read(b, bytesRead, length-bytesRead);
        }
        return b;
    }

    @Override
    public boolean isEOF() throws IOException
    {
        int peek = peek();
        return peek == -1;
    }
}
