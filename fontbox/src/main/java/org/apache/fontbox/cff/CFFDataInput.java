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
package org.apache.fontbox.cff;

import java.io.IOException;

/**
 * This is specialized DataInput. It's used to parse a CFFFont.
 * 
 * @author Villu Ruusmann
 */
public class CFFDataInput extends DataInput
{

    /**
     * Constructor.
     * @param buffer the buffer to be read 
     */
    public CFFDataInput(final byte[] buffer)
    {
        super(buffer);
    }

    /**
     * Read one single Card8 value from the buffer. 
     * @return the card8 value
     * @throws IOException if an error occurs during reading
     */
    public int readCard8() throws IOException
    {
        return readUnsignedByte();
    }

    /**
     * Read one single Card16 value from the buffer.
     * @return the card16 value
     * @throws IOException if an error occurs during reading
     */
    public int readCard16() throws IOException
    {
        return readUnsignedShort();
    }

    /**
     * Read the offset from the buffer.
     * @param offSize the given offsize
     * @return the offset
     * @throws IOException if an error occurs during reading
     */
    public int readOffset(final int offSize) throws IOException
    {
        int value = 0;
        for (int i = 0; i < offSize; i++)
        {
            value = value << 8 | readUnsignedByte();
        }
        return value;
    }

    /**
     * Read offSize from the buffer. This is a 1 byte value between 1 and 4.
     *
     * @return the offSize.
     * @throws IOException if an error occurs during reading or if the value is illegal.
     */
    public int readOffSize() throws IOException
    {
        final int offSize = readUnsignedByte();
        if (offSize < 1 || offSize > 4)
        {
            throw new IOException("Illegal (< 1 or > 4) offSize value " + offSize + " in CFF font at position " + (getPosition() - 1));
        }
        return offSize;        
    }

    /**
     * Read a SID from the buffer.
     * @return the SID
     * @throws IOException if an error occurs during reading
     */
    public int readSID() throws IOException
    {
        return readUnsignedShort();
    }
}