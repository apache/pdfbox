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
package org.apache.fontbox.cff;

import java.io.IOException;
import org.apache.pdfbox.io.RandomAccessRead;

/**
 * This class implements the DataInput interface using a RandomAccessRead as source.
 * <br>
 * Note: things can get hairy when the underlying buffer is larger than {@link Integer#MAX_VALUE}.
 * Straight forward reading may work, but {@link #getPosition()} and {@link #setPosition(int)}
 * may have problems.
 */
public class DataInputRandomAccessRead implements DataInput
{

    private final RandomAccessRead randomAccessRead;

    /**
     * Constructor.
     *
     * @param randomAccessRead the source to be read from
     */
    public DataInputRandomAccessRead(RandomAccessRead randomAccessRead)
    {
        this.randomAccessRead = randomAccessRead;
    }

    /**
     * Determines if there are any bytes left to read or not.
     *
     * @return true if there are any bytes left to read.
     * @throws IOException when the underlying buffer has already been closed.
     */
    @Override
    public boolean hasRemaining() throws IOException
    {
        return randomAccessRead.available() > 0;
    }

    /**
     * Returns the current position.
     *
     * @return current position.
     * @throws IOException when the underlying buffer has already been closed.
     */
    @Override
    public int getPosition() throws IOException
    {
        return (int) randomAccessRead.getPosition();
    }

    /**
     * Sets the current <i>absolute</i> position to the given value. You <i>cannot</i> use
     * <code>setPosition(-20)</code> to move 20 bytes back!
     *
     * @param position the given position, must be 0 &le; position &lt; length.
     * @throws IOException if the new position is out of rangeor when the underlying buffer has
     * already been closed.
     */
    @Override
    public void setPosition(int position) throws IOException
    {
        if (position < 0)
        {
            throw new IOException("position is negative");
        }
        if (position >= randomAccessRead.length())
        {
            throw new IOException("New position is out of range " + position + " >= "
                    + randomAccessRead.length());
        }
        randomAccessRead.seek(position);
    }

    /**
     * Read one single byte from the buffer.
     *
     * @return the byte.
     * @throws IOException when there are no bytes to reador when the underlying buffer has already
     * been closed.
     */
    @Override
    public byte readByte() throws IOException
    {
        if (!hasRemaining())
        {
            throw new IOException("End of buffer reached!");
        }
        return (byte) randomAccessRead.read();
    }

    /**
     * Read one single unsigned byte from the buffer.
     *
     * @return the unsigned byte as int.
     * @throws IOException when there are no bytes to read or when the underlying buffer has already
     * been closed.
     */
    @Override
    public int readUnsignedByte() throws IOException
    {
        if (!hasRemaining())
        {
            throw new IOException("End of buffer reached!");
        }
        return randomAccessRead.read();
    }

    /**
     * Peeks one single unsigned byte from the buffer.
     *
     * @param offset offset to the byte to be peeked, must be 0 &le; offset.
     * @return the unsigned byte as int.
     * @throws IOException when the offset is negative or beyond end_of_buffer or when the
     * underlying buffer has been closed already.
     */
    @Override
    public int peekUnsignedByte(int offset) throws IOException
    {
        if (offset < 0)
        {
            throw new IOException("offset is negative");
        }
        if (offset == 0)
        {
            return randomAccessRead.peek();
        }
        long currentPosition = randomAccessRead.getPosition();
        if (currentPosition + offset >= randomAccessRead.length())
        {
            throw new IOException("Offset position is out of range " + (currentPosition + offset)
                    + " >= " + randomAccessRead.length());
        }
        randomAccessRead.seek(currentPosition + offset);
        int peekValue = randomAccessRead.read();
        randomAccessRead.seek(currentPosition);
        return peekValue;
    }

    /**
     * Read a number of single byte values from the buffer.<br>
     * Note: when <code>readBytes(5)</code> is called, but there are only 3 bytes available, the
     * caller gets an IOException, not the 3 bytes!
     *
     * @param length the number of bytes to be read, must be 0 &le; length.
     * @return an array with containing the bytes from the buffer.
     * @throws IOException when there are less than <code>length</code> bytes available or when the
     * underlying buffer has already been closed.
     */
    @Override
    public byte[] readBytes(int length) throws IOException
    {
        if (length < 0)
        {
            throw new IOException("length is negative");
        }
        if (randomAccessRead.length() - randomAccessRead.getPosition() < length)
        {
            throw new IOException("Premature end of buffer reached");
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
        {
            bytes[i] = readByte();
        }
        return bytes;
    }

    @Override
    public int length() throws IOException
    {
        return (int) randomAccessRead.length();
    }

}