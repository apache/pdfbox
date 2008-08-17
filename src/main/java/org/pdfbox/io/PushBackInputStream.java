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
