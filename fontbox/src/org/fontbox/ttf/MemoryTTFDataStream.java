/**
 * Copyright (c) 2005, www.fontbox.org
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
 * 3. Neither the name of fontbox; nor the names of its
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
 * http://www.fontbox.org
 *
 */
package org.fontbox.ttf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An interface into a data stream.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.2 $
 */
public class MemoryTTFDataStream extends TTFDataStream 
{
    private byte[] data = null;
    private int currentPosition = 0;
    
    /**
     * Constructor from a stream. 
     * @param is The stream of read from.
     * @throws IOException If an error occurs while reading from the stream.
     */
    public MemoryTTFDataStream( InputStream is ) throws IOException
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream( is.available() );
            byte[] buffer = new byte[1024];
            int amountRead = 0;
            while( (amountRead = is.read( buffer ) ) != -1 )
            {
                output.write( buffer, 0, amountRead );
            }
            data = output.toByteArray();
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
        }
    }
    

    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    public long readLong() throws IOException
    {
        return ((long)(readSignedInt()) << 32) + (readSignedInt() & 0xFFFFFFFFL);
    }
    
    /**
     * Read a signed integer.
     * 
     * @return A signed integer.
     * @throws IOException If there is a problem reading the file.
     */
    public int readSignedInt() throws IOException
    {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if( (ch1 | ch2 | ch3 | ch4) < 0)
        {
            throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    public int read() throws IOException
    {
        int retval = -1;
        if( currentPosition < data.length )
        {
            retval = data[currentPosition];
        }
        currentPosition++;
        return (retval+256)%256;
    }
    
    /**
     * Read an unsigned short.
     * 
     * @return An unsigned short.
     * @throws IOException If there is an error reading the data.
     */
    public int readUnsignedShort() throws IOException
    {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (ch1 << 8) + (ch2 << 0);
    }
    
    /**
     * Read an signed short.
     * 
     * @return An signed short.
     * @throws IOException If there is an error reading the data.
     */
    public short readSignedShort() throws IOException
    {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (short)((ch1 << 8) + (ch2 << 0));
    }
    
    /**
     * Close the underlying resources.
     * 
     * @throws IOException If there is an error closing the resources.
     */
    public void close() throws IOException
    {
        data = null;
    }
    
    /**
     * Seek into the datasource.
     * 
     * @param pos The position to seek to.
     * @throws IOException If there is an error seeking to that position.
     */
    public void seek(long pos) throws IOException
    {
        currentPosition = (int)pos;
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
    public int read(byte[] b,
            int off,
            int len)
     throws IOException
     {
        int amountRead = Math.min( len, data.length-currentPosition );
        System.arraycopy(data,currentPosition,b, off, amountRead );
        currentPosition+=amountRead;
        
        return amountRead;
     }
    
    /**
     * Get the current position in the stream.
     * @return The current position in the stream.
     * @throws IOException If an error occurs while reading the stream.
     */
    public long getCurrentPosition() throws IOException
    {
        return currentPosition;
    }
    
    /**
     * {@inheritDoc}
     */
    public InputStream getOriginalData() throws IOException
    {
        return new ByteArrayInputStream( data );
    }
}