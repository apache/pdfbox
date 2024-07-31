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

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An implementation of the RandomAccessRead interface using an InputStream as source.
 * 
 * It is optimized for a minimal memory footprint by using a small buffer to read from the input stream instead of
 * copying the whole input stream to memory. This reduces the random access read abilities, so that peek/rewind
 * operations are limited to the data within the buffer.
 * 
 * This class is meant to be used by consumers which process the data more or less in a serial manner and therefore
 * don't need full random access.
 * 
 */
public class NonSeekableRandomAccessReadInputStream implements RandomAccessRead
{
    private static final Logger LOG = LogManager
            .getLogger(NonSeekableRandomAccessReadInputStream.class);

    // current position within the stream
    protected long position = 0;
    // current pointer for the current chunk
    protected int currentBufferPointer = 0;
    // current size of the stream
    protected long size = 0;

    // the source input stream
    private final InputStream is;

    // buffer size
    private static final int BUFFER_SIZE = 4096;
    // we are using 3 different buffers for navigation
    private static final int CURRENT = 0;
    private static final int LAST = 1;
    private static final int NEXT = 2;

    // array holding all buffers
    private final byte[][] buffers = new byte[][] { new byte[BUFFER_SIZE], new byte[BUFFER_SIZE],
            new byte[BUFFER_SIZE] };
    // array holding the number of bytes of all buffers
    private final int[] bufferBytes = new int[] { -1, -1, -1 };

    private boolean isClosed = false;
    private boolean isEOF = false;

    /**
     * Default constructor.
     */
    public NonSeekableRandomAccessReadInputStream(InputStream inputStream)
    {
        is = inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        is.close();
        isClosed = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long position) throws IOException
    {
        throw new IOException(getClass().getName() + ".seek isn't supported.");
    }

    @Override
    public void skip(int length) throws IOException
    {
        for (int i=0; i< length;i++)
        {
            read();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return position;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();
        if (isEOF())
        {
            return -1;
        }
        if (currentBufferPointer >= bufferBytes[CURRENT] && !fetch())
        {
            isEOF = true;
            return -1;
        }
        position++;
        return buffers[CURRENT][currentBufferPointer++] & 0xFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        checkClosed();
        if (isEOF())
        {
            return -1;
        }
        int numberOfBytesRead = 0;
        while (numberOfBytesRead < length)
        {
            int available = bufferBytes[CURRENT] - currentBufferPointer;
            if (available > 0)
            {
                int bytes2Copy = Math.min(length - numberOfBytesRead, available);
                System.arraycopy(buffers[CURRENT], currentBufferPointer, b,
                        numberOfBytesRead + offset,
                        bytes2Copy);
                currentBufferPointer += bytes2Copy;
                position += bytes2Copy;
                numberOfBytesRead += bytes2Copy;
            }
            else if (!fetch())
            {
                isEOF = true;
                break;
            }
        }
        return numberOfBytesRead;
    }

    private void switchBuffers(int firstBuffer, int secondBuffer)
    {
        byte[] tmpBuffer = buffers[firstBuffer];
        buffers[firstBuffer] = buffers[secondBuffer];
        buffers[secondBuffer] = tmpBuffer;
        int tmpBufferBytes = bufferBytes[firstBuffer];
        bufferBytes[firstBuffer] = bufferBytes[secondBuffer];
        bufferBytes[secondBuffer] = tmpBufferBytes;
    }

    private boolean fetch() throws IOException
    {
        checkClosed();
        currentBufferPointer = 0;
        if (bufferBytes[NEXT] > -1)
        {
            // there is a next buffer from a former rewind operation
            // switch to the next buffer and don't read any new data
            switchBuffers(CURRENT, LAST);
            switchBuffers(CURRENT, NEXT);
            // reset next buffer
            bufferBytes[NEXT] = -1;
            return true;
        }
        try
        {
            // move the current data to last to support rewind operations
            // right after refilling the current buffer
            switchBuffers(CURRENT, LAST);
            bufferBytes[CURRENT] = is.read(buffers[CURRENT]);
            if (bufferBytes[CURRENT] <= 0)
            {
                bufferBytes[CURRENT] = -1;
                return false;
            }
            size += bufferBytes[CURRENT];
        }
        catch (IOException exception)
        {
            // some data could be read -> don't throw an exception
            LOG.warn("FlateFilter: premature end of stream due to a DataFormatException");
            isEOF = true;
            throw exception;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long length() throws IOException
    {
        checkClosed();
        return size;
    }

    @Override
    public void rewind(int bytes) throws IOException
    {
        // check if the rewind operation is limited to the current buffer
        if (currentBufferPointer >= bytes)
        {
            currentBufferPointer -= bytes;
            position -= bytes;
        }
        else if (bufferBytes[LAST] > 0)
        {
            // there is a former buffer
            int remainingBytesToRewind = bytes - currentBufferPointer;
            // save the current as next buffer
            switchBuffers(CURRENT, NEXT);
            // make the former buffer the current one
            switchBuffers(CURRENT, LAST);
            // reset last buffer
            bufferBytes[LAST] = -1;
            currentBufferPointer = bufferBytes[CURRENT] - remainingBytesToRewind;
            position -= bytes;
        }
        else
        {
            // there aren't enough bytes left in the buffers to perform the rewind operation
            throw new IOException("not enough bytes available to perfomr the rewind operation");
        }
    }

    /**
     * Ensure that the RandomAccessBuffer is not closed
     * @throws IOException If RandomAccessBuffer already closed
     */
    protected void checkClosed() throws IOException
    {
        if (isClosed)
        {
            throw new IOException(getClass().getSimpleName() + " already closed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEOF() throws IOException
    {
        checkClosed();
        return isEOF;
    }

    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength) throws IOException
    {
        throw new IOException(getClass().getName() + ".createView isn't supported.");
    }

}
