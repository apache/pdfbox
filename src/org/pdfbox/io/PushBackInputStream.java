/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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
 * A simple subclass that adds a few convience methods.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PushBackInputStream extends java.io.PushbackInputStream
{

    /**
     * Constructor.
     *
     * @param input The input stream.
     * @param size The size of the push back buffer.
     * 
     * @throws IOException If there is an error with the stream. 
     */
    public PushBackInputStream( InputStream input, int size ) throws IOException
    {
        super( input, size );
        if( input == null )
        {
            throw new IOException( "Error: input was null" );
        }
    }

    /**
     * This will peek at the next byte.
     *
     * @return The next byte on the stream, leaving it as available to read.
     *
     * @throws IOException If there is an error reading the next byte.
     */
    public int peek() throws IOException
    {
        int result = read();
        if( result != -1 )
        {
            unread( result );
        }
        return result;
    }

    /**
     * A simple test to see if we are at the end of the stream.
     *
     * @return true if we are at the end of the stream.
     *
     * @throws IOException If there is an error reading the next byte.
     */
    public boolean isEOF() throws IOException
    {
        int peek = peek();
        return peek == -1;
    }
    
    /**
     * This is a method used to fix PDFBox issue 974661, the PDF parsing code needs
     * to know if there is at least x amount of data left in the stream, but the available()
     * method returns how much data will be available without blocking.  PDFBox is willing to
     * block to read the data, so we will first fill the internal buffer.
     * 
     * @throws IOException If there is an error filling the buffer.
     */
    public void fillBuffer() throws IOException
    {
        int bufferLength = buf.length;
        byte[] tmpBuffer = new byte[bufferLength];
        int amountRead = 0;
        int totalAmountRead = 0;
        while( amountRead != -1 && totalAmountRead < bufferLength )
        {
            amountRead = this.read( tmpBuffer, totalAmountRead, bufferLength - totalAmountRead );
            if( amountRead != -1 )
            {
                totalAmountRead += amountRead;
            }
        }
        this.unread( tmpBuffer, 0, totalAmountRead );
    }
}