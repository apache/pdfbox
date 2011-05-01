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
 * An interface to allow PDF files to be stored completely in memory.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class RandomAccessBuffer implements RandomAccess
{

    private byte[] buffer;
    private long pointer;
    private long size;

    /**
     * Default constructor.
     */
    public RandomAccessBuffer()
    {
        // starting with a 16kb buffer
        buffer = new byte[16384];
        pointer = 0;
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
        buffer = null;
        pointer = 0;
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void seek(long position) throws IOException
    {
        this.pointer = position;
    }

    /**
     * {@inheritDoc}
     */
    public int read() throws IOException
    {
        if (pointer >= this.size)
        {
            return -1;
        }
        return buffer[(int)pointer++] & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] b, int offset, int length) throws IOException
    {
        if (pointer >= this.size)
        {
            return 0;
        }
        int maxLength = (int) Math.min(length, this.size-pointer);
        System.arraycopy(buffer, (int) pointer, b, offset, maxLength);
        pointer += maxLength;
        return maxLength;
    }

    /**
     * {@inheritDoc}
     */
    public long length() throws IOException
    {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public void write(int b) throws IOException
    {
        if (pointer >= buffer.length)
        {
            if (pointer >= Integer.MAX_VALUE) 
            {
                throw new IOException("RandomAccessBuffer overflow");
            }
            buffer = expandBuffer(buffer, (int)Math.min(2L * buffer.length, Integer.MAX_VALUE));
        }
        buffer[(int)pointer++] = (byte)b;
        if (pointer > this.size)
        {
            this.size = pointer;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b, int offset, int length) throws IOException
    {
        long newSize = pointer+length;
        if (newSize >= buffer.length)
        {
            if (newSize > Integer.MAX_VALUE) 
            {
                throw new IOException("RandomAccessBuffer overflow");
            }
            newSize = Math.min(Math.max(2L * buffer.length, newSize), Integer.MAX_VALUE);
            buffer = expandBuffer(buffer, (int)newSize);
        }
        System.arraycopy(b, offset, buffer, (int)pointer, length);
        pointer += length;
        if (pointer > this.size)
        {
            this.size = pointer;
        }
    }

    /**
     * expand the given buffer to the new size.
     * 
     * @param buffer the given buffer
     * @param newSize the new size
     * @return the expanded buffer
     * 
     */
    private byte[] expandBuffer(byte[] buffer, int newSize) 
    {
        byte[] expandedBuffer = new byte[newSize];
        System.arraycopy(buffer, 0, expandedBuffer, 0, buffer.length);
        return expandedBuffer;
    }
}
