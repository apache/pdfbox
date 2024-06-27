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
package org.apache.fontbox.ttf;

import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadView;

/**
 * In contrast to {@link RandomAccessReadDataStream},
 * this class doesn't pre-load {@code RandomAccessRead} into a {@code byte[]},
 * it works with {@link RandomAccessRead} directly.
 * 
 * Performance: it is much faster if most of the buffer is skipped, and slower if whole buffer is read()
 */
class RandomAccessReadUnbufferedDataStream extends TTFDataStream
{
    private final long length;
    private final RandomAccessRead randomAccessRead;

    /**
     * @throws IOException If there is a problem reading the source length.
     */
    RandomAccessReadUnbufferedDataStream(RandomAccessRead randomAccessRead) throws IOException
    {
        this.length = randomAccessRead.length();
        this.randomAccessRead = randomAccessRead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentPosition() throws IOException
    {
        return randomAccessRead.getPosition();
    }

    /**
     * Close the underlying resources.
     *
     * @throws IOException If there is an error closing the resources.
     */
    @Override
    public void close() throws IOException
    {
        randomAccessRead.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        return randomAccessRead.read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long readLong() throws IOException
    {
        return ((long) readInt() << 32) | (readInt() & 0xFFFFFFFFL);
    }

    /**
     * {@inheritDoc}
     */
    private int readInt() throws IOException
    {
        int b1 = read();
        int b2 = read();
        int b3 = read();
        int b4 = read();
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long pos) throws IOException
    {
        randomAccessRead.seek(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return randomAccessRead.read(b, off, len);
    }

    /**
     * Lifetime of returned InputStream is bound by {@code this} lifetime, it won't close underlying {@code RandomAccessRead}.
     * 
     * {@inheritDoc}
     */
    @Override
    public InputStream getOriginalData() throws IOException
    {
        return new RandomAccessReadNonClosingInputStream(randomAccessRead.createView(0, length));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getOriginalDataSize()
    {
        return length;
    }

    @Override
    public RandomAccessRead createSubView(long length)
    {
        try
        {
            return randomAccessRead.createView(randomAccessRead.getPosition(), length);
        }
        catch (IOException ex)
        {
            assert false : "Please implement " + randomAccessRead.getClass() + ".createView()";
            return null;
        }
    }

    private static final class RandomAccessReadNonClosingInputStream extends InputStream
    {

        private final RandomAccessReadView randomAccessRead;

        public RandomAccessReadNonClosingInputStream(RandomAccessReadView randomAccessRead)
        {
            this.randomAccessRead = randomAccessRead;
        }

        @Override
        public int read() throws IOException
        {
            return randomAccessRead.read();
        }

        @Override
        public int read(byte[] b) throws IOException
        {
            return randomAccessRead.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            return randomAccessRead.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException
        {
            randomAccessRead.seek(randomAccessRead.getPosition() + n);
            return n;
        }

        @Override
        public void close() throws IOException
        {
            // WARNING: .close() will close RandomAccessReadMemoryMappedFile if this View was based on it
//            randomAccessRead.close();
        }
    }
}
