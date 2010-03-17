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
