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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;

/**
 * An implementation of the TTFDataStream using RandomAccessRead as source.
 * 
 */
class RandomAccessReadDataStream extends TTFDataStream
{
    private final long length;
    private final byte[] data;
    private int currentPosition = 0;

    /**
     * Constructor.
     * 
     * @param randomAccessRead source to be read from
     * 
     * @throws IOException If there is a problem reading the source data.
     */
    RandomAccessReadDataStream(RandomAccessRead randomAccessRead) throws IOException
    {
        length = randomAccessRead.length();
        data = new byte[(int) length];
        int remainingBytes = data.length;
        int amountRead;
        while ((amountRead = randomAccessRead.read(data, data.length - remainingBytes,
                remainingBytes)) > 0)
        {
            remainingBytes -= amountRead;
        }
    }
    
    /**
     * Constructor.
     * 
     * @param inputStream source to be read from
     * 
     * @throws IOException If there is a problem reading the source data.
     */
    RandomAccessReadDataStream(InputStream inputStream) throws IOException
    {
        data = IOUtils.toByteArray(inputStream);
        length = data.length;
    }

    /**
     * Get the current position in the stream.
     * @return The current position in the stream.
     * @throws IOException If an error occurs while reading the stream.
     */
    @Override
    public long getCurrentPosition() throws IOException
    {
        return currentPosition;
    }
    
    /**
     * Close the underlying resources.
     * 
     * @throws IOException If there is an error closing the resources.
     */
    @Override
    public void close() throws IOException
    {
        // nothing to do
    }
    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    public int read() throws IOException
    {
        if (currentPosition >= length)
        {
            return -1;
        }
        return data[currentPosition++] & 0xff;
    }
    
    /**
     * Read a signed 64-bit integer.
     * 
     * @return eight bytes interpreted as a long.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    public final long readLong() throws IOException
    {
        return ((long) readInt() << 32) + (readInt() & 0xFFFFFFFFL);
    }

    /**
     * Read a signed 32-bit integer.
     * 
     * @return 4 bytes interpreted as a int.
     * @throws IOException If there is an error reading the data.
     */
    private int readInt() throws IOException
    {
        int b1 = read();
        int b2 = read();
        int b3 = read();
        int b4 = read();
        return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
    }
    /**
     * Seek into the datasource.
     * 
     * @param pos The position to seek to.
     * @throws IOException If there is an error seeking to that position.
     */
    @Override
    public void seek(long pos) throws IOException
    {
        if (pos < 0)
        {
            throw new IOException("Invalid position " + pos);
        }
        currentPosition = pos < length ? (int) pos : (int) length;
    }
    
    /**
     * @see java.io.InputStream#read( byte[], int, int )
     * 
     * @param b The buffer to write to.
     * @param off The offset into the buffer.
     * @param len The length into the buffer.
     * 
     * @return The number of bytes read.
     * 
     * @throws IOException If there is an error reading from the stream.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (currentPosition >= length)
        {
            return -1;
        }
        int remainingBytes = (int) (length - currentPosition);
        int bytesToRead = Math.min(remainingBytes, len);
        System.arraycopy(data, currentPosition, b, off, bytesToRead);
        currentPosition += bytesToRead;
        return bytesToRead;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getOriginalData() throws IOException
    {
        return new ByteArrayInputStream(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getOriginalDataSize()
    {
        return length;
    }
}
