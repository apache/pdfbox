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
