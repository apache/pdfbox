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

import java.io.IOException;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield
 */
public class IndexToLocationTable extends TTFTable
{
    private static final short SHORT_OFFSETS = 0;
    private static final short LONG_OFFSETS = 1;
    
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "loca";
    
    private long[] offsets;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        HeaderTable head = ttf.getHeader();
        int numGlyphs = ttf.getNumberOfGlyphs();
        offsets = new long[ numGlyphs +1];
        for( int i=0; i<numGlyphs+1; i++ )
        {
            if( head.getIndexToLocFormat() == SHORT_OFFSETS )
            {
                offsets[i] = data.readUnsignedShort() * 2;
            }
            else if(  head.getIndexToLocFormat() == LONG_OFFSETS )
            {
                offsets[i] = data.readUnsignedInt();
            }
            else
            {
                throw new IOException( "Error:TTF.loca unknown offset format.");
            }
        }
        initialized = true;
    }
    /**
     * @return Returns the offsets.
     */
    public long[] getOffsets()
    {
        return offsets;
    }
    /**
     * @param offsetsValue The offsets to set.
     */
    public void setOffsets(long[] offsetsValue)
    {
        offsets = offsetsValue;
    }
}
