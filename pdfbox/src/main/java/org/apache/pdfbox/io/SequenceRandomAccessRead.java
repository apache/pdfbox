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
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>SequenceRandomAccessRead</code> represents the logical concatenation of a couple of RandomAccessRead
 * instances.
 *
 */
public class SequenceRandomAccessRead implements RandomAccessRead
{
    private boolean isClosed;

    private final List<? extends RandomAccessRead> source;
    private final List<Long> sourceLength;
    private final long bufferLength;
    private RandomAccessRead currentBuffer;
    private long currentPosition;
    private long currentBufferPosition;
    private long currentBufferLength;
    private int currentIndex;
    private int maxIndex;

    /**
     * Create a read only wrapper for a RandomAccessRead instance.
     *
     * @param list a list containing all instances of RandomAccessRead to be read.
     * 
     * @throws IOException if something went wrong while accessing the given list of RandomAccessRead.
     */
    public SequenceRandomAccessRead(List<? extends RandomAccessRead> list) throws IOException
    {
        source = list;
        maxIndex = list.size();
        sourceLength = new ArrayList<Long>(maxIndex);
        long sumLength = 0;
        for (RandomAccessRead input : list)
        {
            long inputLength = input.length();
            input.seek(0);
            sourceLength.add(inputLength);
            sumLength += inputLength;
        }
        bufferLength = sumLength;
        currentBuffer = source.get(0);
        currentBufferLength = sourceLength.get(0);
        currentBufferPosition = 0;
        currentPosition = 0;
        currentIndex = 0;
    }

    /**
     * Ensure that the RandomAccessFile is not closed.
     * 
     * @throws IOException if the buffer is already closed
     */
    private void checkClosed() throws IOException
    {
        if (isClosed)
        {
            throw new IOException("RandomAccessFile already closed");
        }
    }

    /**
     * Switch to the next buffer.
     * 
     * @return true if another buffer is available
     * 
     * @throws IOException if something went wrong when switching to the next buffer
     */
    private boolean nextBuffer() throws IOException
    {
        if (currentIndex < maxIndex)
        {
            currentIndex++;
            switchBuffer(currentIndex, false);
            return true;
        }
        return false;
    }

    /**
     * Switch to buffer with the given index.
     * 
     * @param index the index of the buffer to be switched to
     * @param calculatePosition calculate the new position if set to true
     * 
     * @throws IOException if the given index exceeds the available number of buffers
     */
    private void switchBuffer(int index, boolean calculatePosition) throws IOException
    {
        currentBuffer = source.get(index);
        currentBufferLength = sourceLength.get(index);
        currentBufferPosition = 0;
        if (calculatePosition)
        {
            currentPosition = 0;
            for (int i = 0; i < index; i++)
            {
                currentPosition += sourceLength.get(i);
            }
        }
    }

    /** Returns offset in file at which next byte would be read. */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return currentPosition;
    }

    /**
     * Seeks to new position. If new position is outside of current page the new page is either taken from cache or read
     * from file and added to cache.
     *
     * @param newPosition the position to seek to.
     * @throws java.io.IOException if something went wrong.
     */
    @Override
    public void seek(final long newPosition) throws IOException
    {
        checkClosed();
        // new position beyond EOF
        if (newPosition >= bufferLength)
        {
            currentIndex = maxIndex - 1;
            switchBuffer(currentIndex, false);
            currentBufferPosition = sourceLength.get(currentIndex);
            currentPosition = bufferLength;
        }
        else
        {
            int index = 0;
            long position = sourceLength.get(index);
            while (newPosition > position)
            {
                position += sourceLength.get(index++);
            }
            switchBuffer(index, true);
            currentBufferPosition = newPosition - currentPosition;
            currentPosition = newPosition;
            currentBuffer.seek(currentBufferPosition);
        }
    }

    @Override
    public int read() throws IOException
    {
        checkClosed();
        int returnValue = -1;
        if (currentPosition < bufferLength)
        {
            if (currentBufferPosition < currentBufferLength)
            {
                returnValue = currentBuffer.read();
                currentPosition++;
                currentBufferPosition++;
            }
            else
            {
                if (nextBuffer())
                {
                    returnValue = currentBuffer.read();
                    currentPosition++;
                    currentBufferPosition++;
                }
            }
        }
        return returnValue;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        checkClosed();
        int bytesReadTotal = readBytes(b, off, len);
        int bytesRead = bytesReadTotal;
        while (bytesReadTotal < len && bytesRead > 0)
        {
            bytesRead = read(b, bytesRead, len - bytesRead);
            bytesReadTotal += bytesRead;
        }
        return bytesReadTotal;
    }

    private int readBytes(byte[] b, int off, int len) throws IOException
    {
        // end of current buffer reached?
        if (currentBufferLength - currentBufferPosition == 0)
        {
            nextBuffer();
        }
        int bytesRead = currentBuffer.read(b, off, len);
        currentBufferPosition += bytesRead;
        currentPosition += bytesRead;
        return bytesRead;
    }

    @Override
    public int available() throws IOException
    {
        return (int) Math.min(bufferLength - getPosition(), Integer.MAX_VALUE);
    }

    @Override
    public long length() throws IOException
    {
        return bufferLength;
    }

    @Override
    public void close() throws IOException
    {
        // don't close the underlying random access
        isClosed = true;
        currentBuffer = null;
        source.clear();
    }

    @Override
    public boolean isClosed()
    {
        return isClosed || source == null;
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
        while (bytesRead < length)
        {
            bytesRead += read(b, bytesRead, length - bytesRead);
        }
        return b;
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return peek() == -1;
    }
}
