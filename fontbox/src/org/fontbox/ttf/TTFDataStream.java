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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * An interface into a data stream.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public abstract class TTFDataStream 
{

    /**
     * Read a 16.16 fixed value, where the first 16 bits are the decimal and the last
     * 16 bits are the fraction.
     * @return A 32 bit value.
     * @throws IOException If there is an error reading the data.
     */
    public float read32Fixed() throws IOException
    {
        float retval = 0;
        retval = readSignedShort();
        retval += (readUnsignedShort()/65536);
        return retval;
    }
    
    /**
     * Read a fixed length ascii string.
     * @param length The length of the string to read.
     * @return A string of the desired length.
     * @throws IOException If there is an error reading the data.
     */
    public String readString( int length ) throws IOException
    {
        return readString( length, "ISO-8859-1" );
    }
    
    /**
     * Read a fixed length ascii string.
     * @param length The length of the string to read in bytes.
     * @param charset The expected character set of the string.
     * @return A string of the desired length.
     * @throws IOException If there is an error reading the data.
     */
    public String readString( int length, String charset ) throws IOException
    {
        byte[] buffer = read( length );
        return new String(buffer, charset);
    }
    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    public abstract int read() throws IOException;
    
    /**
     * Read an unsigned byte.
     * @return An unsigned byte.
     * @throws IOException If there is an error reading the data.
     */
    public abstract long readLong() throws IOException;
    
    
    /**
     * Read a signed byte.
     * @return A signed byte.
     * @throws IOException If there is an error reading the data.
     */
    public int readSignedByte() throws IOException
    {
        int signedByte = read();
        return signedByte < 127 ? signedByte : signedByte-256;
    }
    
    /**
     * Read an unsigned integer.
     * @return An unsiged integer.
     * @throws IOException If there is an error reading the data.
     */
    public long readUnsignedInt() throws IOException
    {
        long byte1 = read();
        long byte2 = read();
        long byte3 = read();
        long byte4 = read();
        if( byte4 < 0 )
        {
            throw new EOFException();
        }
        return (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + (byte4 << 0);
    }
    
    /**
     * Read an unsigned short.
     * 
     * @return An unsigned short.
     * @throws IOException If there is an error reading the data.
     */
    public abstract int readUnsignedShort() throws IOException;
    
    /**
     * Read an unsigned short array.
     * 
     * @param length The length of the array to read.
     * @return An unsigned short array.
     * @throws IOException If there is an error reading the data.
     */
    public int[] readUnsignedShortArray( int length ) throws IOException
    {
        int[] array = new int[ length ];
        for( int i=0; i<length; i++ )
        {
            array[i] = readUnsignedShort();
        }
        return array;
    }
    
    /**
     * Read an signed short.
     * 
     * @return An signed short.
     * @throws IOException If there is an error reading the data.
     */
    public abstract short readSignedShort() throws IOException;
    
    /**
     * Read an eight byte international date.
     * 
     * @return An signed short.
     * @throws IOException If there is an error reading the data.
     */
    public Calendar readInternationalDate() throws IOException
    {
        long secondsSince1904 = readLong();
        GregorianCalendar cal = new GregorianCalendar( 1904, 0, 1 );
        long millisFor1904 = cal.getTimeInMillis();
        millisFor1904 += (secondsSince1904*1000);
        cal.setTimeInMillis( millisFor1904 );
        return cal;
    }
    
    /**
     * Close the underlying resources.
     * 
     * @throws IOException If there is an error closing the resources.
     */
    public abstract void close() throws IOException;
    
    /**
     * Seek into the datasource.
     * 
     * @param pos The position to seek to.
     * @throws IOException If there is an error seeking to that position.
     */
    public abstract void seek(long pos) throws IOException;
    
    /**
     * Read a specific number of bytes from the stream.
     * @param numberOfBytes The number of bytes to read.
     * @return The byte buffer.
     * @throws IOException If there is an error while reading.
     */
    public byte[] read( int numberOfBytes ) throws IOException
    {
        byte[] data = new byte[ numberOfBytes ];
        int amountRead = 0;
        int totalAmountRead = 0;
        while( (amountRead = read( data, totalAmountRead, numberOfBytes-totalAmountRead ) ) != -1 && 
               totalAmountRead < numberOfBytes )
        {
            totalAmountRead += amountRead;
            //read at most numberOfBytes bytes from the stream.
        }
        return data;
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
    public abstract int read(byte[] b,
            int off,
            int len)
     throws IOException;
    
    /**
     * Get the current position in the stream.
     * @return The current position in the stream.
     * @throws IOException If an error occurs while reading the stream.
     */
    public abstract long getCurrentPosition() throws IOException;
    
    /**
     * This will get the original data file that was used for this
     * stream.
     * 
     * @return The data that was read from.
     * @throws IOException If there is an issue reading the data.
     */
    public abstract InputStream getOriginalData() throws IOException;

}