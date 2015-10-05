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

import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSStream;

/**
 * Implementation of {@link RandomAccess} as sequence of multiple fixed size pages handled
 * by {@link ScratchFile}.
 */
class ScratchFileBuffer implements RandomAccess
{
    private final int pageSize;
    /**
     * The underlying page handler.
     */
    private ScratchFile pageHandler;
    /**
     * The number of bytes of content in this buffer.
     */
    private long size = 0;
    /**
     * Index of current page in {@link #pageIndexes} (the nth page within this buffer).
     */
    private int currentPagePositionInPageIndexes;
    /**
     * The offset of the current page within this buffer.
     */
    private long currentPageOffset;
    /**
     * The current page data.
     */
    private byte[] currentPage;
    /**
     * The current position (for next read/write) of the buffer as an offset in the current page.
     */
    private int positionInPage;
    /** 
     * <code>true</code> if current page was changed by a write method
     */
    private boolean currentPageContentChanged = false;

    /** contains ordered list of pages with the index the page is known by page handler ({@link ScratchFile}) */
    private int[] pageIndexes = new int[16];
    /** number of pages held by this buffer */
    private int pageCount = 0;
    
    private static final Log LOG = LogFactory.getLog(ScratchFileBuffer.class);
    
    /**
     * Creates a new buffer using pages handled by provided {@link ScratchFile}.
     * 
     * @param pageHandler The {@link ScratchFile} managing the pages to be used by this buffer.
     * 
     * @throws IOException If getting first page failed.
     */
    ScratchFileBuffer(ScratchFile pageHandler) throws IOException
    {
        pageHandler.checkClosed();

        this.pageHandler = pageHandler;
        
        pageSize = this.pageHandler.getPageSize();
        
        addPage();
    }

    /**
     * Checks if this buffer, or the underlying {@link ScratchFile} have been closed,
     * throwing {@link IOException} if so.
     * 
     * @throws IOException If either this buffer, or the underlying {@link ScratchFile} have been closed.
     */
    private void checkClosed() throws IOException
    {
        if (pageHandler == null)
        {
            throw new IOException("Buffer already closed");
        }
        pageHandler.checkClosed();
    }

    /**
     * Adds a new page and positions all pointers to start of new page.
     * 
     * @throws IOException if requesting a new page fails
     */
    private void addPage() throws IOException
    {
        if (pageCount+1 >= pageIndexes.length)
        {
            int newSize = pageIndexes.length*2;
            // check overflow
            if (newSize<pageIndexes.length)
            {
                if (pageIndexes.length == Integer.MAX_VALUE)
                {
                    throw new IOException("Maximum buffer size reached.");
                }
                newSize = Integer.MAX_VALUE;
            }
            int[] newPageIndexes = new int[newSize];
            System.arraycopy(pageIndexes, 0, newPageIndexes, 0, pageCount);
            pageIndexes = newPageIndexes;
        }
        
        int newPageIdx = pageHandler.getNewPage();
        
        pageIndexes[pageCount] = newPageIdx;
        currentPagePositionInPageIndexes = pageCount;
        currentPageOffset = ((long)pageCount) * pageSize; 
        pageCount++;
        currentPage = new byte[pageSize];
        positionInPage = 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public long length() throws IOException
    {
        return size;
    }

    /**
     * Ensures the current page has at least one byte left
     * ({@link #positionInPage} in &lt; {@link #pageSize}).
     * 
     * <p>If this is not the case we go to next page (writing
     * current one if changed). If current buffer has no more
     * pages we add a new one.</p>
     * 
     * @param addNewPageIfNeeded if <code>true</code> it is allowed to add a new page in case
     *                           we are currently at end of last buffer page
     * 
     * @return <code>true</code> if we were successful positioning pointer before end of page;
     *         we might return <code>false</code> if it is not allowed to add another page
     *         and current pointer points at end of last page
     * 
     * @throws IOException
     */
    private boolean ensureAvailableBytesInPage(boolean addNewPageIfNeeded) throws IOException
    {
        if (positionInPage >= pageSize)
        {
            // page full
            if (currentPageContentChanged)
            {
                // write page
                pageHandler.writePage(pageIndexes[currentPagePositionInPageIndexes], currentPage);
                currentPageContentChanged = false;
            }
            // get new page
            if (currentPagePositionInPageIndexes+1 < pageCount)
            {
                // we already have more pages assigned (there was a backward seek before)
                currentPage = pageHandler.readPage(pageIndexes[++currentPagePositionInPageIndexes]);
                currentPageOffset = ((long)currentPagePositionInPageIndexes) * pageSize;
                positionInPage = 0;
            }
            else if (addNewPageIfNeeded)
            {
                // need new page
                addPage();
            }
            else
            {
                // we are at last page and are not allowed to add new page
                return false;
            }
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException
    {
        checkClosed();
        
        ensureAvailableBytesInPage(true);
        
        currentPage[positionInPage++] = (byte) b;
        currentPageContentChanged = true;
        
        if(currentPageOffset + positionInPage > size)
        {
            size = currentPageOffset + positionInPage;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        checkClosed();

        int remain = len;
        int bOff   = off;
        
        while (remain > 0)
        {
            ensureAvailableBytesInPage(true);

            int bytesToWrite = Math.min(remain, pageSize-positionInPage);
            
            System.arraycopy(b, bOff, currentPage, positionInPage, bytesToWrite);
            
            positionInPage += bytesToWrite;
            currentPageContentChanged = true;
            
            bOff   += bytesToWrite;
            remain -= bytesToWrite;
        }
        
        if(currentPageOffset + positionInPage > size)
        {
            size = currentPageOffset + positionInPage;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clear() throws IOException
    {
        checkClosed();
        
        // keep only the first page, discard all other pages
        pageHandler.markPagesAsFree(pageIndexes, 1, pageCount - 1);
        pageCount = 1;
        
        // change to first page if we are not already there
        if (currentPagePositionInPageIndexes > 0)
        {
            currentPage = pageHandler.readPage(pageIndexes[0]);
            currentPagePositionInPageIndexes = 0;
            currentPageOffset = 0;
        }
        positionInPage = 0;
        size = 0;
        currentPageContentChanged = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return currentPageOffset + positionInPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long seekToPosition) throws IOException
    {
        checkClosed();

        /*
         * for now we won't allow to seek past end of buffer; this can be changed by adding new pages as needed
         */
        if (seekToPosition > size)
        {
            throw new EOFException();
        }
        
        if (seekToPosition < 0)
        {
            throw new IOException("Negative seek offset: " + seekToPosition);
        }
        
        if ((seekToPosition >= currentPageOffset) && (seekToPosition <= currentPageOffset + pageSize))
        {
            // within same page
            positionInPage = (int) (seekToPosition - currentPageOffset);
        }
        else
        {
            // have to go to another page
            
            // check if current page needs to be written to file
            if (currentPageContentChanged)
            {
                pageHandler.writePage(pageIndexes[currentPagePositionInPageIndexes], currentPage);
                currentPageContentChanged = false;
            }
            
            int newPagePosition = (int) (seekToPosition / pageSize);
            
            currentPage = pageHandler.readPage(pageIndexes[newPagePosition]);
            currentPagePositionInPageIndexes = newPagePosition;
            currentPageOffset = ((long)currentPagePositionInPageIndexes) * pageSize;
            positionInPage = (int) (seekToPosition - currentPageOffset);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed()
    {
        return pageHandler == null;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void rewind(int bytes) throws IOException
    {
        seek(currentPageOffset + positionInPage - bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readFully(int len) throws IOException
    {
        byte[] b = new byte[len];

        int n = 0;
        do
        {
            int count = read(b, n, len - n);
            if (count < 0)
            {
                throw new EOFException();
            }
            n += count;
        } while (n < len);

        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEOF() throws IOException
    {
        checkClosed();
        return currentPageOffset + positionInPage >= size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException
    {
        checkClosed();
        return (int) Math.min(size - (currentPageOffset + positionInPage), Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();

        if (currentPageOffset + positionInPage >= size)
        {
            return -1;
        }

        if (! ensureAvailableBytesInPage(false))
        {
            // should not happen, we checked it before
            throw new IOException("Unexpectedly no bytes available for read in buffer.");
        }
        
        return currentPage[positionInPage++] & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        checkClosed();

        if (currentPageOffset + positionInPage >= size)
        {
            return -1;
        }

        int remain = (int) Math.min(len, size - (currentPageOffset + positionInPage));

        int totalBytesRead = 0;
        int bOff           = off;

        while (remain > 0)
        {
            if (! ensureAvailableBytesInPage(false))
            {
                // should not happen, we checked it before
                throw new IOException("Unexpectedly no bytes available for read in buffer.");
            }
            
            int readBytes = Math.min(remain, pageSize - positionInPage);

            System.arraycopy(currentPage, positionInPage, b, bOff, readBytes);

            positionInPage += readBytes;
            totalBytesRead += readBytes;
            bOff += readBytes;
            remain -= readBytes;
        }

        return totalBytesRead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        if (pageHandler != null) {

            pageHandler.markPagesAsFree(pageIndexes, 0, pageCount);
            pageHandler = null;
            
            pageIndexes = null;
            currentPage = null;
            currentPageOffset = 0;
            currentPagePositionInPageIndexes = -1;
            positionInPage = 0;
            size = 0;
        }
    }
    
    /**
     * While calling finalize is normally discouraged we will have to
     * use it here as long as closing a scratch file buffer is not 
     * done in every case. Currently {@link COSStream} creates new
     * buffers without closing the old one - which might still be
     * used.
     * 
     * <p>Enabling debugging one will see if there are still cases
     * where the buffer is not closed.</p>
     */
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            if ((pageHandler != null) && LOG.isDebugEnabled())
            {
                LOG.debug("ScratchFileBuffer not closed!");
            }
            close();
        }
        finally
        {
            super.finalize();
        }
    }
}
