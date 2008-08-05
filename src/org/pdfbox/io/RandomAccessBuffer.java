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
package org.pdfbox.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * An interface to allow PDF files to be stored completely in memory.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class RandomAccessBuffer implements RandomAccess
{

    private static final int EXTRA_SPACE = 16384; // 16kb
    private byte[] buffer;
    private long pointer;
    private long size;

    /**
     * Default constructor.
     */
    public RandomAccessBuffer()
    {
        buffer = new byte[EXTRA_SPACE];
        pointer = 0;
        size = EXTRA_SPACE;
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
        int result = buffer[(int)pointer];
        pointer++;
        return result;
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
        write(new byte[] {(byte) b}, 0, 1);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b, int offset, int length) throws IOException
    {
        if (pointer+length >= buffer.length)
        {
            // expand buffer
            byte[] temp = new byte[buffer.length+length+EXTRA_SPACE];
            Arrays.fill(temp, (byte)0);
            System.arraycopy(buffer, 0, temp, 0, (int) this.size);
            buffer = temp;
        }
        System.arraycopy(b, offset, buffer, (int)pointer, length);
        pointer += length;
        if (pointer > this.size)
        {
            this.size = pointer;
        }
    }
}
