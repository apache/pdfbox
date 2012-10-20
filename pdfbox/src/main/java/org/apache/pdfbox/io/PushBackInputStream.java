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

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * A simple subclass that adds a few convience methods.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PushBackInputStream extends java.io.PushbackInputStream
{
    /*
     * The current position in the file. 
     */
    private long offset = 0;
    
    /** In case provided input stream implements {@link RandomAccessRead} we hold
     *  a typed reference to it in order to support seek operations. */
    private final RandomAccessRead raInput;
    
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
        
        raInput = ( input instanceof RandomAccessRead ) ?
										(RandomAccessRead) input : null;
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
     * Returns the current byte offset in the file.
     * @return the int byte offset
     */
    public long getOffset()
    {
        return offset;
    }
    
    /**
     * {@inheritDoc} 
     */
    public int read() throws IOException
    {
        int retval = super.read();
        if (retval != -1)
        {
            offset++;
        }
        return retval;
    }
    
    /**
     * {@inheritDoc} 
     */
    public int read(byte[] b) throws IOException
    {
        return this.read(b, 0, b.length);
    }
    /**
     * {@inheritDoc} 
     */
    public int read(byte[] b, int off, int len) throws IOException
    {
        int retval = super.read(b, off, len);
        if (retval != -1)
        {
            offset += retval;
        }
        return retval;
    }
    
    /**
     * {@inheritDoc} 
     */
    public void unread(int b) throws IOException
    {
        offset--;
        super.unread(b);
    }
    
    /**
     * {@inheritDoc} 
     */
    public void unread(byte[] b) throws IOException
    {
        this.unread(b, 0, b.length);
    }
    
    /**
     * {@inheritDoc} 
     */
    public void unread(byte[] b, int off, int len) throws IOException
    {
        if (len > 0)
        {
            offset -= len;
            super.unread(b, off, len);
        }
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
    
    /**
     * Reads a given number of bytes from the underlying stream.
     * @param length the number of bytes to be read
     * @return a byte array containing the bytes just read
     * @throws IOException if an I/O error occurs while reading data
     */
    public byte[] readFully(int length) throws IOException
    {
        byte[] data = new byte[length];
        int pos = 0;
        while (pos < length)
        {
            int amountRead = read( data, pos, length - pos );
            if (amountRead < 0) 
            {
                throw new EOFException("Premature end of file");
            }
            pos += amountRead;
        }
        return data;
    }

    /** Allows to seek to another position within stream in case the underlying
     *  stream implements {@link RandomAccessRead}. Otherwise an {@link IOException}
     *  is thrown.
     *  
     *  Pushback buffer is cleared before seek operation by skipping over all bytes
     *  of buffer.
     *  
     *  @param newOffset  new position within stream from which to read next
     *  
     *  @throws IOException if underlying stream does not implement {@link RandomAccessRead}
     *                      or seek operation on underlying stream was not successful
     */
    public void seek( long newOffset ) throws IOException
    {
    	if ( raInput == null )
    			throw new IOException( "Provided stream of type " + in.getClass().getSimpleName() +
    													 	 " is not seekable." );
    	
    	// clear unread buffer by skipping over all bytes of buffer
    	int unreadLength = buf.length - pos;
    	if ( unreadLength > 0 )
    	{
    			skip( unreadLength );
    	}
    	
    	raInput.seek( newOffset );
    	offset = newOffset;
    }

}
