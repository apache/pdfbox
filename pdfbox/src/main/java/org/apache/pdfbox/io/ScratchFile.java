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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements a memory page handling mechanism as base for creating (multiple)
 * {@link RandomAccess} buffers each having its set of pages (implemented by
 * {@link ScratchFileBuffer}). A buffer is created calling {@link #createBuffer()}.
 * 
 * <p>Pages can be stored in main memory or in a temporary file. A mixed mode
 * is supported storing a certain amount of pages in memory and only the
 * additional ones in temporary file (defined by maximum main memory to
 * be used).</p>
 * 
 * <p>Pages can be marked as 'free' in order to re-use them. For in-memory pages
 * this will release the used memory while for pages in temporary file this
 * simply marks the area as free to re-use.</p>
 * 
 * <p>If a temporary file was created (done with the first page to be stored
 * in temporary file) it is deleted when {@link ScratchFile#close()} is called.</p>
 * 
 * <p>Using this class for {@link RandomAccess} buffers allows for a direct control
 * on the maximum memory usage and allows processing large files for which we
 * otherwise would get an {@link OutOfMemoryError} in case of using {@link RandomAccessBuffer}.</p>
 * 
 * <p>This base class for providing pages is thread safe (the buffer implementations are not).</p>
 */
public class ScratchFile implements Closeable
{
    private static final Log LOG = LogFactory.getLog(ScratchFile.class);

    /** number of pages by which we enlarge the scratch file (reduce I/O-operations) */
    private static final int ENLARGE_PAGE_COUNT = 16;
    /** in case of unrestricted main memory usage this is the initial number of pages
     *  {@link #inMemoryPages} is setup for */
    private static final int INIT_UNRESTRICTED_MAINMEM_PAGECOUNT = 100000;
    private static final int PAGE_SIZE = 4096;
    
    private final Object ioLock = new Object();
    private final File scratchFileDirectory;
    /** scratch file; only to be accessed under synchronization of {@link #ioLock} */
    private File file;
    /** random access to scratch file; only to be accessed under synchronization of {@link #ioLock} */
    private java.io.RandomAccessFile raf;
    private volatile int pageCount = 0;
    private final BitSet freePages = new BitSet();
    /** holds pointers to in-memory page content; will be initialized once in case of restricted
     *  main memory, otherwise it is enlarged as needed and first initialized to a size of
     *  {@link #INIT_UNRESTRICTED_MAINMEM_PAGECOUNT} */
    private volatile byte[][] inMemoryPages;
    private final int inMemoryMaxPageCount;
    private final int maxPageCount;
    private final boolean useScratchFile;
    private final boolean maxMainMemoryIsRestricted;

    private volatile boolean isClosed = false;
    
    /**
     * Initializes page handler. If a <code>scratchFileDirectory</code> is supplied,
     * then the scratch file will be created in that directory.
     * 
     * <p>All pages will be stored in the scratch file.</p>
     * 
     * @param scratchFileDirectory The directory in which to create the scratch file
     *                             or <code>null</code> to created it in the default temporary directory.
     * 
     * @throws IOException If scratch file directory was given but don't exist.
     */
    public ScratchFile(File scratchFileDirectory) throws IOException
    {
        this(MemoryUsageSetting.setupTempFileOnly().setTempDir(scratchFileDirectory));
    }
    
    /**
     * Initializes page handler. If a <code>scratchFileDirectory</code> is supplied,
     * then the scratch file will be created in that directory.
     * 
     * <p>Depending on the size of allowed memory usage a number of pages (memorySize/{@link #PAGE_SIZE})
     * will be stored in-memory and only additional pages will be written to/read from scratch file.</p>
     * 
     * @param memUsageSetting set how memory/temporary files are used for buffering streams etc. 
     * 
     * @throws IOException If scratch file directory was given but don't exist.
     */
    public ScratchFile(MemoryUsageSetting memUsageSetting) throws IOException
    {
        maxMainMemoryIsRestricted = (!memUsageSetting.useMainMemory()) || memUsageSetting.isMainMemoryRestricted();
        useScratchFile = maxMainMemoryIsRestricted ? memUsageSetting.useTempFile() : false;
        scratchFileDirectory = useScratchFile ? memUsageSetting.getTempDir() : null;

        if ((scratchFileDirectory != null) && (!scratchFileDirectory.isDirectory()))
        {
            throw new IOException("Scratch file directory does not exist: " + this.scratchFileDirectory);
        }
        
        maxPageCount = memUsageSetting.isStorageRestricted() ?
                           (int) Math.min(Integer.MAX_VALUE, memUsageSetting.getMaxStorageBytes() / PAGE_SIZE) :
                           Integer.MAX_VALUE;
                
        inMemoryMaxPageCount = memUsageSetting.useMainMemory() ? 
                                   (memUsageSetting.isMainMemoryRestricted() ?
                                       (int) Math.min(Integer.MAX_VALUE, memUsageSetting.getMaxMainMemoryBytes() / PAGE_SIZE) :
                                       Integer.MAX_VALUE) :
                                   0;
        inMemoryPages = new byte[maxMainMemoryIsRestricted ? inMemoryMaxPageCount : INIT_UNRESTRICTED_MAINMEM_PAGECOUNT][];
        
        freePages.set(0, inMemoryPages.length);
    }

    /**
     * Getter for an instance using only unrestricted main memory for buffering
     * (same as <code>new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly())</code>).
     * 
     * @return instance configured to only use main memory with no size restriction
     */
    public static ScratchFile getMainMemoryOnlyInstance()
    {
        try
        {
            return new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly());
        }
        catch (IOException ioe)
        {
            // cannot happen for main memory setup
            LOG.error("Unexpected exception occurred creating main memory scratch file instance: " + ioe.getMessage() );
            return null;
        }
    }
    
    /**
     * Returns a new free page, either from free page pool
     * or by enlarging scratch file (may be created).
     * 
     * @return index of new page
     */
    int getNewPage() throws IOException
    {
        synchronized (freePages)
        {
            int idx = freePages.nextSetBit( 0 );
            
            if (idx < 0)
            {
                enlarge();
                
                idx = freePages.nextSetBit( 0 );
                if (idx < 0)
                {
                    throw new IOException("Maximum allowed scratch file memory exceeded.");
                }
            }
            
            freePages.clear(idx);
            
            if (idx >= pageCount)
            {
                pageCount = idx + 1;
            }
            
            return idx;
        }
    }

    /**
     * This will provide new free pages by either enlarging the scratch file 
     * by a number of pages defined by {@link #ENLARGE_PAGE_COUNT} - in case
     * scratch file usage is allowed - or increase the {@link #inMemoryPages}
     * array in case main memory was not restricted. If neither of both is
     * allowed/the case than free pages count won't be changed. The same is true
     * if no new pages could be added because we reached the maximum of
     * {@link Integer#MAX_VALUE} pages.
     * 
     * <p>If scratch file uage is allowed and scratch file does not exist already
     * it will be created.</p>
     * 
     * <p>Only to be called under synchronization on {@link #freePages}.</p>
     */
    private void enlarge() throws IOException
    {
        synchronized (ioLock)
        {
            checkClosed();
     
            if (pageCount >= maxPageCount)
            {
                return;
            }

            if (useScratchFile)
            {
                // create scratch file is needed
                if ( raf == null )
                {
                    file = File.createTempFile("PDFBox", ".tmp", scratchFileDirectory);
                    try
                    {
                        raf = new java.io.RandomAccessFile(file, "rw");
                    }
                    catch (IOException e)
                    {
                        if (!file.delete())
                        {
                            LOG.warn("Error deleting scratch file: " + file.getAbsolutePath());
                        }
                        throw e;
                    }
                }
                
                long fileLen = raf.length();
                long expectedFileLen = ((long)pageCount - inMemoryMaxPageCount) * PAGE_SIZE;
                
                if (expectedFileLen != fileLen)
                {
                    throw new IOException("Expected scratch file size of " + expectedFileLen + " but found " + fileLen);
                }
                    
                // enlarge if we do not overflow
                if (pageCount + ENLARGE_PAGE_COUNT > pageCount)
                {
                    fileLen += ENLARGE_PAGE_COUNT * PAGE_SIZE;
        
                    raf.setLength(fileLen);
        
                    freePages.set(pageCount, pageCount + ENLARGE_PAGE_COUNT);
                }
            }
            else if (!maxMainMemoryIsRestricted)
            {
                // increase number of in-memory pages
                int oldSize = inMemoryPages.length;
                int newSize = (int) Math.min( ((long)oldSize) * 2, Integer.MAX_VALUE);  // this handles integer overflow
                if (newSize > oldSize)
                {
                    byte[][] newInMemoryPages = new byte[newSize][];
                    System.arraycopy(inMemoryPages, 0, newInMemoryPages, 0, oldSize);
                    inMemoryPages = newInMemoryPages;
                    
                    freePages.set(oldSize, newSize);
                }
            }
        }
    }
    
    /**
     * Returns byte size of a page.
     * 
     * @return byte size of a page
     */
    int getPageSize()
    {
        return PAGE_SIZE;
    }
    
    /**
     * Reads the page with specified index.
     * 
     * @param pageIdx index of page to read
     * 
     * @return byte array of size {@link #PAGE_SIZE} filled with page data read from file 
     * 
     * @throws IOException
     */
    byte[] readPage(int pageIdx) throws IOException
    {
        if ((pageIdx < 0) || (pageIdx >= pageCount))
        {
            checkClosed();
            throw new IOException("Page index out of range: " + pageIdx + ". Max value: " + (pageCount - 1) );
        }
        
        // check if we have the page in memory
        if (pageIdx < inMemoryMaxPageCount)
        {
            byte[] page = inMemoryPages[pageIdx];
            
            // handle case that we are closed
            if (page == null)
            {
                checkClosed();
                throw new IOException("Requested page with index " + pageIdx + " was not written before.");
            }
            
            return page;
        }
        
        synchronized (ioLock)
        {
            if (raf == null)
            {
                checkClosed();
                throw new IOException("Missing scratch file to read page with index " + pageIdx + " from.");
            }
            
            byte[] page = new byte[PAGE_SIZE];
            raf.seek(((long)pageIdx - inMemoryMaxPageCount) * PAGE_SIZE);
            raf.readFully(page);
            
            return page;
        }
    }
    
    /**
     * Writes updated page. Page is either kept in-memory if pageIdx &lt; {@link #inMemoryMaxPageCount}
     * or is written to scratch file.
     * 
     * <p>Provided page byte array must not be re-used for other pages since we
     * store it as is in case of in-memory handling.</p>
     * 
     * @param pageIdx index of page to write
     * @param page page to write (length has to be {@value #PAGE_SIZE})
     * 
     * @throws IOException in case page index is out of range or page has wrong length
     *                     or writing to file failed
     */
    void writePage(int pageIdx, byte[] page) throws IOException
    {
        if ((pageIdx<0) || (pageIdx>=pageCount))
        {
            checkClosed();
            throw new IOException("Page index out of range: " + pageIdx + ". Max value: " + (pageCount - 1) );
        }
        
        if (page.length != PAGE_SIZE)
        {
            throw new IOException("Wrong page size to write: " + page.length + ". Expected: " + PAGE_SIZE );
        }
        
        if (pageIdx < inMemoryMaxPageCount)
        {
            if (maxMainMemoryIsRestricted)
            {
                inMemoryPages[pageIdx] = page;
            }
            else
            {
                // need synchronization since inMemoryPages may change
                synchronized (ioLock)
                {
                    inMemoryPages[pageIdx] = page;
                }
            }
            
            // in case we were closed in between throw exception
            checkClosed();
        }
        else
        {
            synchronized (ioLock)
            {
                checkClosed();
                raf.seek(((long)pageIdx - inMemoryMaxPageCount) * PAGE_SIZE);
                raf.write(page);
            }
        }
    }
    
    /**
     * Checks if this page handler has already been closed. If so,
     * an {@link IOException} is thrown.
     * 
     * @throws IOException If {@link #close()} has already been called.
     */
    void checkClosed() throws IOException
    {
        if (isClosed)
        {
            throw new IOException("Scratch file already closed");
        }
    }

    /**
     * Creates a new buffer using this page handler.
     * 
     * @return A new buffer.
     * 
     * @throws IOException If an error occurred.
     */
    public RandomAccess createBuffer() throws IOException
    {
        return new ScratchFileBuffer(this);
    }

    /**
     * Creates a new buffer using this page handler and initializes it with the
     * data read from provided input stream (input stream is copied to buffer).
     * The buffer data pointer is reset to point to first byte.
     * 
     * @return A new buffer containing data read from input stream.
     * 
     * @throws IOException If an error occurred.
     */
    public RandomAccess createBuffer(InputStream input) throws IOException
    {
        ScratchFileBuffer buf = new ScratchFileBuffer(this);
        
        byte[] byteBuffer = new byte[8192];
        int bytesRead = 0;
        while ((bytesRead = input.read(byteBuffer)) > -1)
        {
            buf.write(byteBuffer, 0, bytesRead);
        }
        buf.seek(0);

        return buf;
    }

    /**
     * Allows a buffer which is cleared/closed to release its pages to be re-used.
     * 
     * @param pageIndexes pages indexes of pages to release
     * @param count number of page indexes contained in provided array 
     */
    void markPagesAsFree(int[] pageIndexes, int off, int count) {
        
        synchronized (freePages)
        {
            for (int aIdx = off; aIdx < count; aIdx++)
            {
                int pageIdx = pageIndexes[aIdx];
                if ((pageIdx>=0) && (pageIdx<pageCount) && (!freePages.get(pageIdx)))
                {
                    freePages.set(pageIdx);
                    if (pageIdx < inMemoryMaxPageCount)
                    {
                        inMemoryPages[pageIdx] = null;  // remark: not in ioLock synchronization since behavior won't
                                                        // change even in case of parallel called 'enlarge' method
                    }
                }
                    
            }
        }
    }
    
    /**
     * Closes and deletes the temporary file. No further interaction with
     * the scratch file or associated buffers can happen after this method is called.
     * It also releases in-memory pages.
     * 
     * @throws IOException If there was a problem closing or deleting the temporary file.
     */
    @Override
    public void close() throws IOException
    {
        IOException ioexc = null;
        
        synchronized (ioLock)
        {
            if (isClosed)
            {
                return;
            }
        
            isClosed = true;

            if (raf != null)
            {
                try
                {
                    raf.close();
                }
                catch (IOException ioe)
                {
                    ioexc = ioe;
                }
            }
            
            if (file != null)
            {
                if (!file.delete())
                {
                    if (file.exists() && (ioexc == null))
                    {
                        ioexc = new IOException("Error deleting scratch file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        
        synchronized (freePages)
        {
            freePages.clear();
            pageCount = 0;
        }
        
        if (ioexc != null)
        {
            throw ioexc;
        }
    }
}
