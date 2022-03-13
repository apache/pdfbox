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
    // close input
    private final boolean closeInput;
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
        this(randomAccessRead, startPosition, streamLength, false);
    }

    /**
     * Constructor.
     * 
     * @param randomAccessRead the underlying random access read
     * @param startPosition start position within the underlying random access read
     * @param streamLength stream length
     * @param closeInput close the underlying random access read when closing the view if set to true
     */
    public RandomAccessReadView(RandomAccessRead randomAccessRead, long startPosition,
            long streamLength, boolean closeInput)
    {
        this.randomAccessRead = randomAccessRead;
        this.startPosition = startPosition;
        this.streamLength = streamLength;
        this.closeInput = closeInput;
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
        if (newOffset < 0)
        {
            throw new IOException("Invalid position " + newOffset);
        }
        randomAccessRead.seek(startPosition + Math.min(newOffset, streamLength));
        currentPosition = newOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        if (isEOF())
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
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (isEOF())
        {
            return -1;
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
        if (closeInput && randomAccessRead != null)
        {
            randomAccessRead.close();
        }
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
     * @throws IOException If RandomAccessReadView already closed
     */
    private void checkClosed() throws IOException
    {
        if (isClosed())
        {
            // consider that the rab is closed if there is no current buffer
            throw new IOException("RandomAccessReadView already closed");
        }
    }

    @Override
    public RandomAccessReadView createView(long startPosition, long streamLength) throws IOException
    {
        throw new IOException(getClass().getName() + ".createView isn't supported.");
    }

}
