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

/**
 * This class contains some functionality to read a byte buffer.
 * 
 * @author Villu Ruusmann
 */
public class DataInput
{

    private final byte[] inputBuffer;
    private int bufferPosition = 0;

    /**
     * Constructor.
     * @param buffer the buffer to be read
     */
    public DataInput(byte[] buffer)
    {
        inputBuffer = buffer;
    }

    /**
     * Determines if there are any bytes left to read or not. 
     * @return true if there are any bytes left to read
     */
    public boolean hasRemaining()
    {
        return bufferPosition < inputBuffer.length;
    }

    /**
     * Returns the current position.
     * @return current position
     */
    public int getPosition()
    {
        return bufferPosition;
    }

    /**
     * Sets the current position to the given value.
     * 
     * @param position the given position
     * @throws IOException if the new position ist out of range
     */
    public void setPosition(int position) throws IOException
    {
        if (position < 0)
        {
            throw new IOException("position is negative");
        }
        if (position >= inputBuffer.length)
        {
            throw new IOException(
                    "New position is out of range " + position + " >= " + inputBuffer.length);
        }
        bufferPosition = position;
    }

    /**
     * Read one single byte from the buffer.
     * @return the byte
     * @throws IOException if an error occurs during reading
     */
    public byte readByte() throws IOException
    {
        if (!hasRemaining())
        {
            throw new IOException("End off buffer reached");
        }
        return inputBuffer[bufferPosition++];
    }

    /**
     * Read one single unsigned byte from the buffer.
     * @return the unsigned byte as int
     * @throws IOException if an error occurs during reading
     */
    public int readUnsignedByte() throws IOException
    {
        if (!hasRemaining())
        {
            throw new IOException("End off buffer reached");
        }
        return inputBuffer[bufferPosition++] & 0xff;
    }

    /**
     * Peeks one single unsigned byte from the buffer.
     * 
     * @param offset offset to the byte to be peeked
     * @return the unsigned byte as int
     * @throws IOException if an error occurs during reading
     */
    public int peekUnsignedByte(int offset) throws IOException
    {
        if (offset < 0)
        {
            throw new IOException("offset is negative");
        }
        if (bufferPosition + offset >= inputBuffer.length)
        {
            throw new IOException("Offset position is out of range " + (bufferPosition + offset)
                    + " >= " + inputBuffer.length);
        }
        return inputBuffer[bufferPosition + offset] & 0xff;
    }

    /**
     * Read one single short value from the buffer.
     * @return the short value
     * @throws IOException if an error occurs during reading
     */
    public short readShort() throws IOException
    {
        return (short) readUnsignedShort();
    }

    /**
     * Read one single unsigned short (2 bytes) value from the buffer.
     * @return the unsigned short value as int
     * @throws IOException if an error occurs during reading
     */
    public int readUnsignedShort() throws IOException
    {
        int b1 = readUnsignedByte();
        int b2 = readUnsignedByte();
        return b1 << 8 | b2;
    }

    /**
     * Read one single int (4 bytes) from the buffer.
     * @return the int value
     * @throws IOException if an error occurs during reading
     */
    public int readInt() throws IOException
    {
        int b1 = readUnsignedByte();
        int b2 = readUnsignedByte();
        int b3 = readUnsignedByte();
        int b4 = readUnsignedByte();
        return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }

    /**
     * Read a number of single byte values from the buffer.
     * @param length the number of bytes to be read
     * @return an array with containing the bytes from the buffer 
     * @throws IOException if an error occurs during reading
     */
    public byte[] readBytes(int length) throws IOException
    {
        if (length < 0)
        {
            throw new IOException("length is negative"); 
        }
        if (inputBuffer.length - bufferPosition < length)
        {
            throw new IOException("Premature end of buffer reached");
        }
        byte[] bytes = new byte[length];
        System.arraycopy(inputBuffer, bufferPosition, bytes, 0, length);
        bufferPosition += length;
        return bytes;
    }

    public int length()
    {
        return inputBuffer.length;
    }
}