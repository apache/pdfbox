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

/**
 * A read only wrapper for a RandomAccessRead instance.
 *
 */
public class RandomAccessReadWrapper implements RandomAccessRead
{
    private boolean isClosed;

    private RandomAccessRead randomAccessInput;
    private final long startPosition;
    private final long inputLength;

    /**
     * Create a read only wrapper for a RandomAccessRead instance.
     *
     * @param input the RandomAccessRead instance to be read.
     * @param offset offset to start reading at.
     * @param length the total length of data to be read.
     * @throws IOException if something went wrong while accessing the given RandomAccessRead.
     */
    public RandomAccessReadWrapper(RandomAccessRead input, long offset, long length) throws IOException
    {
        randomAccessInput = input;
        startPosition = offset;
        inputLength = length;
        seek(0);
    }

    /**
     * Ensure that the RandomAccessFile is not closed.
     * 
     * @throws IOException
     */
    private void checkClosed() throws IOException
    {
        if (isClosed)
        {
            throw new IOException("RandomAccessFile already closed");
        }
    }

    /** Returns offset in file at which next byte would be read. */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return randomAccessInput.getPosition() + startPosition;
    }

    /**
     * Seeks to new position. If new position is outside of current page the new page is either taken from cache or read
     * from file and added to cache.
     *
     * @param newOffset the position to seek to.
     * @throws java.io.IOException if something went wrong.
     */
    @Override
    public void seek(final long newOffset) throws IOException
    {
        checkClosed();
        randomAccessInput.seek(newOffset + startPosition);
    }

    @Override
    public int read() throws IOException
    {
        checkClosed();
        return randomAccessInput.read();
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
        return randomAccessInput.read(b, off, len);
    }

    @Override
    public int available() throws IOException
    {
        return (int) Math.min(inputLength - getPosition(), Integer.MAX_VALUE);
    }

    @Override
    public long length() throws IOException
    {
        return inputLength;
    }

    @Override
    public void close() throws IOException
    {
        // don't close the underlying random access
        isClosed = true;
        randomAccessInput = null;
    }

    @Override
    public boolean isClosed()
    {
        if (isClosed || randomAccessInput == null)
        {
            return true;
        }
        return randomAccessInput.isClosed();
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
        int peek = peek();
        return peek == -1;
    }
}
