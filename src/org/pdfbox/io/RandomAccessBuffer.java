/**
 * Copyright (c) 2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
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
