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
 * This class provides a view of a part of a random access read. It clips the section starting at the given start
 * position with the given length into a new random access read.
 * 
 */
public class RandomAccessReadView implements RandomAccessRead
{
    // the underlying random access read
    private RandomAccessRead randomAccessRead;
    // the start position within the underlying source
    private final long startPosition;
    // stream length
    private final long streamLength;
    // current position within the view
    private long currentPosition = 0;

    /**
     * Constructor.
     * 
     * @param randomAccessRead the underlying random access read
     * @param startPosition start position within the underlying random access read
     * @param streamLength stream length
     */
    public RandomAccessReadView(RandomAccessRead randomAccessRead, long startPosition,
            long streamLength)
    {
        this.randomAccessRead = randomAccessRead;
        this.startPosition = startPosition;
        this.streamLength = streamLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() throws IOException
    {
        checkClosed();
        return currentPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(final long newOffset) throws IOException
    {
        checkClosed();
        if (newOffset < streamLength)
        {
            randomAccessRead.seek(startPosition + newOffset);
            currentPosition = newOffset;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();
        if (currentPosition >= streamLength)
        {
            return -1;
        }
        restorePosition();
        int readValue = randomAccessRead.read();
        if (readValue > -1)
        {
            currentPosition++;
        }
        return readValue;
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
        if (currentPosition >= streamLength)
        {
            return 0;
        }
        restorePosition();
        int readBytes = randomAccessRead.read(b, off, Math.min(len, available()));
        currentPosition += readBytes;
        return readBytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException
    {
        checkClosed();
        return (int) (streamLength - currentPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long length() throws IOException
    {
        checkClosed();
        return streamLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        checkClosed();
        randomAccessRead = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed()
    {
        return randomAccessRead == null || randomAccessRead.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int peek() throws IOException
    {
        checkClosed();
        restorePosition();
        return randomAccessRead.peek();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rewind(int bytes) throws IOException
    {
        checkClosed();
        restorePosition();
        randomAccessRead.rewind(bytes);
        currentPosition -= bytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEOF() throws IOException
    {
        checkClosed();
        return currentPosition >= streamLength;
    }

    /**
     * Restore the current position within the underlying random access read.
     * 
     * @throws IOException
     */
    private void restorePosition() throws IOException
    {
        randomAccessRead.seek(startPosition + currentPosition);
    }

    /**
     * Ensure that that the view isn't closed.
     * 
     * @throws IOException
     */
    private void checkClosed() throws IOException
    {
        if (isClosed())
        {
            // consider that the rab is closed if there is no current buffer
            throw new IOException("RandomAccessReadView already closed");
        }
    }

}
