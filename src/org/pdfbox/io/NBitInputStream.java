/**
  * Copyright (c) 2003, www.pdfbox.org
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

import java.io.InputStream;
import java.io.IOException;

/**
 * This is an n-bit input stream.  This means that you can read chunks of data
 * as any number of bits, not just 8 bits like the regular input stream.  Just set the
 * number of bits that you would like to read on each call.  The default is 8.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class NBitInputStream
{
    private int bitsInChunk;
    private InputStream in;

    private int currentByte;
    private int bitsLeftInCurrentByte;

    /**
     * Constructor.
     *
     * @param is The input stream to read from.
     */
    public NBitInputStream( InputStream is )
    {
        in = is;
        bitsLeftInCurrentByte = 0;
        bitsInChunk = 8;
    }

    /**
     * This will unread some data.
     *
     * @param data The data to put back into the stream.
     */
    public void unread( long data )
    {
        data <<= bitsLeftInCurrentByte;
        currentByte |= data;
        bitsLeftInCurrentByte += bitsInChunk;
    }

    /**
     * This will read the next n bits from the stream and return the unsigned
     * value of  those bits.
     *
     * @return The next n bits from the stream.
     *
     * @throws IOException If there is an error reading from the underlying stream.
     */
    public long read() throws IOException
    {
        long retval = 0;
        for( int i=0; i<bitsInChunk && retval != -1; i++ )
        {
            if( bitsLeftInCurrentByte == 0 )
            {
                currentByte = in.read();
                bitsLeftInCurrentByte = 8;
            }
            if( currentByte == -1 )
            {
                retval = -1;
            }
            else
            {
                retval <<= 1;
                retval |= ((currentByte >> (bitsLeftInCurrentByte-1))&0x1);
                bitsLeftInCurrentByte--;
            }
        }
        return retval;
    }

    /** Getter for property bitsToRead.
     * @return Value of property bitsToRead.
     */
    public int getBitsInChunk()
    {
        return bitsInChunk;
    }

    /** Setter for property bitsToRead.
     * @param bitsInChunkValue New value of property bitsToRead.
     */
    public void setBitsInChunk(int bitsInChunkValue)
    {
        bitsInChunk = bitsInChunkValue;
    }

}