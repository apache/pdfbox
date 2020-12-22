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

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class contains some functionality to read a byte buffer.
 * 
 * @author Villu Ruusmann
 */
public class DataInput
{

    private byte[] inputBuffer = null;
    private int bufferPosition = 0;

    private static final Log LOG = LogFactory.getLog(DataInput.class);

    /**
     * Constructor.
     * @param buffer the buffer to be read
     */
    public DataInput(final byte[] buffer)
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
     * @param position the given position
     */
    public void setPosition(final int position)
    {
        bufferPosition = position;
    }

    /** 
     * Returns the buffer as an ISO-8859-1 string.
     * @return the buffer as string
     * @throws IOException if an error occurs during reading
     */
    public String getString() throws IOException
    {
        return new String(inputBuffer, StandardCharsets.ISO_8859_1);
    }

    /**
     * Read one single byte from the buffer.
     * @return the byte
     * @throws IOException if an error occurs during reading
     */
    public byte readByte() throws IOException
    {
        try
        {
            final byte value = inputBuffer[bufferPosition];
            bufferPosition++;
            return value;
        } 
        catch (RuntimeException re)
        {
            LOG.debug("An error occurred reading a byte - returning -1", re);
            return -1;
        }
    }

    /**
     * Read one single unsigned byte from the buffer.
     * @return the unsigned byte as int
     * @throws IOException if an error occurs during reading
     */
    public int readUnsignedByte() throws IOException
    {
        final int b = read();
        if (b < 0)
        {
            throw new EOFException();
        }
        return b;
    }

    /**
     * Peeks one single unsigned byte from the buffer.
     * @return the unsigned byte as int
     * @throws IOException if an error occurs during reading
     */
    public int peekUnsignedByte(final int offset) throws IOException
    {
        final int b = peek(offset);
        if (b < 0)
        {
            throw new EOFException();
        }
        return b;
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
        final int b1 = read();
        final int b2 = read();
        if ((b1 | b2) < 0)
        {
            throw new EOFException();
        }
        return b1 << 8 | b2;
    }

    /**
     * Read one single int (4 bytes) from the buffer.
     * @return the int value
     * @throws IOException if an error occurs during reading
     */
    public int readInt() throws IOException
    {
        final int b1 = read();
        final int b2 = read();
        final int b3 = read();
        final int b4 = read();
        if ((b1 | b2 | b3 | b4) < 0)
        {
            throw new EOFException();
        }
        return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }

    /**
     * Read a number of single byte values from the buffer.
     * @param length the number of bytes to be read
     * @return an array with containing the bytes from the buffer 
     * @throws IOException if an error occurs during reading
     */
    public byte[] readBytes(final int length) throws IOException
    {
        if (length < 0)
        {
            throw new IOException("length is negative"); 
        }
        if (inputBuffer.length - bufferPosition < length)
        {
            throw new EOFException(); 
        }
        final byte[] bytes = new byte[length];
        System.arraycopy(inputBuffer, bufferPosition, bytes, 0, length);
        bufferPosition += length;
        return bytes;
    }

    private int read()
    {
        try
        {
            final int value = inputBuffer[bufferPosition] & 0xff;
            bufferPosition++;
            return value;
        } 
        catch (RuntimeException re)
        {
            LOG.debug("An error occurred reading an int - returning -1", re);
            return -1;
        }
    }

    private int peek(final int offset)
    {
        try
        {
            return inputBuffer[bufferPosition + offset] & 0xff;
        }
        catch (RuntimeException re)
        {
            LOG.debug("An error occurred peeking at offset " + offset + " - returning -1", re);
            return -1;
        }
    }
    
    public int length()
    {
        return inputBuffer.length;
    }
}