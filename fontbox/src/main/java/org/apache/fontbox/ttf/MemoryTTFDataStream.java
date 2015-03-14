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
package org.apache.fontbox.ttf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An interface into a data stream.
 * 
 * @author Ben Litchfield
 * 
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
        if (currentPosition >= data.length)
        {
            return -1;
        }
        int retval = data[currentPosition];
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
     * @return The number of bytes read, or -1 at the end of the stream
     * 
     * @throws IOException If there is an error reading from the stream.
     */
    public int read(byte[] b,
            int off,
            int len)
     throws IOException
     {
        if (currentPosition < data.length)
        {
            int amountRead = Math.min( len, data.length-currentPosition );
            System.arraycopy(data,currentPosition,b, off, amountRead );
            currentPosition+=amountRead;
            return amountRead;
        }
        else
        {
            return -1;
        }
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