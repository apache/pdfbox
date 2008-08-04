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

import java.io.OutputStream;
import java.io.IOException;

/**
 * This is an n-bit output stream.  This means that you write data in n-bit chunks.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class NBitOutputStream
{
    private int bitsInChunk;
    private OutputStream out;

    private int currentByte;
    private int positionInCurrentByte;

    /**
     * Constructor.
     *
     * @param os The output stream to write to.
     */
    public NBitOutputStream( OutputStream os )
    {
        out = os;
        currentByte = 0;
        positionInCurrentByte = 7;
    }

    /**
     * This will write the next n-bits to the stream.
     *
     * @param chunk The next chunk of data to write.
     *
     * @throws IOException If there is an error writing the chunk.
     */
    public void write( long chunk ) throws IOException
    {
        long bitToWrite;
        for( int i=(bitsInChunk-1); i>=0; i-- )
        {
            bitToWrite = (chunk >> i) & 0x1;
            bitToWrite <<= positionInCurrentByte;
            currentByte |= bitToWrite;
            positionInCurrentByte--;
            if( positionInCurrentByte < 0 )
            {
                out.write( currentByte );
                currentByte = 0;
                positionInCurrentByte = 7;
            }
        }
    }

    /**
     * This will close the stream.
     *
     * @throws IOException if there is an error closing the stream.
     */
    public void close() throws IOException
    {
        if( positionInCurrentByte < 7 )
        {
            out.write( currentByte );
        }
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