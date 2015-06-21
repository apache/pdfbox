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
import java.io.RandomAccessFile;

/**
 * A {@link RandomAccess} implemented as a doubly linked list over multiple pages in a {@link java.io.RandomAccessFile}.
 * <p>
 * Each page is {@link #PAGE_SIZE} bytes, with the first 8 bytes being a pointer to page index (
 * {@code pageOffset / PAGE_SIZE}) of the previous page in the buffer, and the last 8 bytes being a pointer to the page
 * index of the next page in the buffer.
 * 
 * @author Jesse Long
 */
class ScratchFileBuffer implements RandomAccess
{
    /**
     * The size of each page.
     */
    private static final int PAGE_SIZE = 4096;
    /**
     * The underlying scratch file.
     */
    private ScratchFile scratchFile;
    /**
     * The random access file of the scratch file.
     */
    private RandomAccessFile raFile;
    /**
     * The first page in this buffer.
     */
    private final long firstPage;
    /**
     * The number of bytes of content in this buffer.
     */
    private long length = 0;
    /**
     * The index of the page in which the current position of this buffer is in.
     */
    private long currentPage;
    /**
     * The current position of the buffer as an offset in the current page.
     */
    private int positionInPage;
    /**
     * The current position in the space of the whole buffer.
     */
    private long positionInBuffer;

    /**
     * Creates a new buffer in the provided {@link ScratchFile}.
     * 
     * @param scratchFile The {@link ScratchFile} in which to create the new buffer.
     * @throws IOException If there was an error writing to the file.
     */
    ScratchFileBuffer(ScratchFile scratchFile) throws IOException
    {
        scratchFile.checkClosed();

        this.scratchFile = scratchFile;

        raFile = scratchFile.getRandomAccessFile();

        /*
         * We must allocate a new first page for each new buffer, in case multiple buffers are created at the same time,
         * and use the same space.
         */
        firstPage = createNewPage();

        /*
         * Mark the first page back pointer to -1 to indicate start of buffer.
         */
        raFile.seek(firstPage * PAGE_SIZE);
        raFile.writeLong(-1L);

        /*
         * Reset variables to beginning of empty buffer.
         */
        clear();
    }

    /**
     * Checks if this buffer, or the underlying {@link ScratchFile} have been closed, throwing {@link IOException} if
     * so.
     * 
     * @throws IOException If either this buffer, or the underlying {@link ScratchFile} have been closed.
     */
    private void checkClosed() throws IOException
    {
        if (scratchFile == null)
        {
            throw new IOException("Scratch file buffer already closed");
        }
        scratchFile.checkClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() throws IOException
    {
        checkClosed();
        return length;
    }

    /**
     * Allocates a new page, and links the current and the new page.
     * 
     * @throws IOException If there was an error writing to the file.
     */
    private void growToNewPage() throws IOException
    {
        long newPage = createNewPage();

        /*
         * We should only grow to a new page when previous pages are full. If not, links won't work.
         */
        if (positionInPage != PAGE_SIZE - 8)
        {
            throw new IOException("Corruption detected in scratch file");
        }
        seekToCurrentPositionInFile();
        raFile.writeLong(newPage);
        
        long previousPage = currentPage;
        currentPage = newPage;
        positionInPage = 0;
        /*
         * write back link to previous page.
         */
        seekToCurrentPositionInFile();
        raFile.writeLong(previousPage);
        positionInPage = 8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException
    {
        checkClosed();
        seekToCurrentPositionInFile();
        if (positionInPage == PAGE_SIZE - 8)
        {
            growToNewPage();
        }

        raFile.write(b);

        positionInPage++;
        positionInBuffer++;
        if (positionInBuffer > length)
        {
            length = positionInBuffer;
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

        seekToCurrentPositionInFile();

        while (len > 0)
        {
            if (positionInPage == PAGE_SIZE - 8)
            {
                growToNewPage();
            }

            int availableSpaceInCurrentPage = (PAGE_SIZE - 8) - positionInPage;

            int bytesToWrite = Math.min(len, availableSpaceInCurrentPage);

            raFile.write(b, off, bytesToWrite);

            off += bytesToWrite;
            len -= bytesToWrite;
            positionInPage += bytesToWrite;
            positionInBuffer += bytesToWrite;
            if (positionInBuffer > length)
            {
                length = positionInBuffer;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clear() throws IOException
    {
        checkClosed();
        length = 0;
        currentPage = firstPage;
        positionInBuffer = 0;
        positionInPage = 8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return positionInBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long seekToPosition) throws IOException
    {
        checkClosed();

        /*
         * Can't seek past end of file. If you want to change implementation, seek to end of file, write zero bytes for
         * remaining seek distance.
         */
        if (seekToPosition > length)
        {
            throw new EOFException();
        }

        if (seekToPosition < positionInBuffer)
        {
            if (currentPage != firstPage && seekToPosition < (positionInBuffer / 2))
            {
                /*
                 * If we are seeking backwards, and the seek to position is closer to the beginning of the buffer than
                 * our current position, just go to the start of the buffer and seek forward from there. Recurse exactly
                 * once.
                 */
                currentPage = firstPage;
                positionInPage = 8;
                positionInBuffer = 0;
                seek(seekToPosition);
            }
            else
            {
                while (positionInBuffer - seekToPosition > positionInPage - 8)
                {
                    raFile.seek(currentPage * PAGE_SIZE);
                    long previousPage = raFile.readLong();
                    currentPage = previousPage;
                    positionInBuffer -= (positionInPage - 8);
                    positionInPage = PAGE_SIZE - 8;
                }

                positionInPage -= (positionInBuffer - seekToPosition);
                positionInBuffer = seekToPosition;
            }
        }
        else
        {
            while (seekToPosition - positionInBuffer > (PAGE_SIZE - 8) - positionInPage)
            {
                // seek to 8 bytes from end of current page, to read next page pointer.
                raFile.seek(((currentPage + 1) * PAGE_SIZE) - 8);
                long nextPage = raFile.readLong();
                positionInBuffer += (PAGE_SIZE - 8) - positionInPage;
                currentPage = nextPage;
                positionInPage = 8;
            }

            positionInPage += seekToPosition - positionInBuffer;
            positionInBuffer = seekToPosition;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed()
    {
        return scratchFile == null;
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
        seek(positionInBuffer - bytes);
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
        return positionInBuffer >= length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException
    {
        checkClosed();
        return (int) Math.min(length - positionInBuffer, Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();

        if (positionInBuffer >= length)
        {
            return -1;
        }

        seekToCurrentPositionInFile();

        if (positionInPage == PAGE_SIZE - 8)
        {
            currentPage = raFile.readLong();
            positionInPage = 8;
            seekToCurrentPositionInFile();
        }

        int retv = raFile.read();

        if (retv >= 0)
        {
            positionInPage++;
            positionInBuffer++;
        }

        return retv;
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

        if (positionInBuffer >= length)
        {
            return -1;
        }

        seekToCurrentPositionInFile();

        if (positionInPage == PAGE_SIZE - 8)
        {
            currentPage = raFile.readLong();
            positionInPage = 8;
            seekToCurrentPositionInFile();
        }

        len = (int) Math.min(len, length - positionInBuffer);

        int totalBytesRead = 0;

        while (len > 0)
        {
            int availableInThisPage = (PAGE_SIZE - 8) - positionInPage;

            int rdbytes = raFile.read(b, off, Math.min(len, availableInThisPage));

            if (rdbytes < 0)
            {
                throw new IOException("EOF reached before end of scratch file stream");
            }

            if (rdbytes == availableInThisPage)
            {
                currentPage = raFile.readLong();
                positionInPage = 8;
                seekToCurrentPositionInFile();
            }
            else
            {
                positionInPage += rdbytes;
            }

            totalBytesRead += rdbytes;
            positionInBuffer += rdbytes;
            off += rdbytes;
            len -= rdbytes;
        }

        return totalBytesRead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        scratchFile = null;
        raFile = null;
    }

    /**
     * Positions the underlying {@link java.io.RandomAccessFile} to the correct position for use by this buffer.
     * 
     * @throws IOException If there was a problem seeking in the {@link java.io.RandomAccessFile}.
     */
    private void seekToCurrentPositionInFile() throws IOException
    {
        long positionInFile = (currentPage * PAGE_SIZE) + positionInPage;
        if (raFile.getFilePointer() != positionInFile)
        {
            raFile.seek(positionInFile);
        }
    }

    /**
     * Allocates a new page in the temporary file by growing the file, returning the page index of the new page.
     * 
     * @return The index of the new page.
     * @throws IOException If there was an error growing the file.
     */
    private long createNewPage() throws IOException
    {
        long fileLen = raFile.length();

        fileLen += PAGE_SIZE;

        if (fileLen % PAGE_SIZE > 0)
        {
            fileLen += PAGE_SIZE - (fileLen % PAGE_SIZE);
        }

        raFile.setLength(fileLen);

        return (fileLen / PAGE_SIZE) - 1;
    }
}
