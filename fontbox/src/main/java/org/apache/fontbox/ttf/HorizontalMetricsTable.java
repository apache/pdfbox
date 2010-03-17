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
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class HorizontalMetricsTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "hmtx";
    
    private int[] advanceWidth;
    private short[] leftSideBearing;
    private short[] nonHorizontalLeftSideBearing;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        HorizontalHeaderTable hHeader = ttf.getHorizontalHeader();
        MaximumProfileTable maxp = ttf.getMaximumProfile();
        int numHMetrics = hHeader.getNumberOfHMetrics();
        int numGlyphs = maxp.getNumGlyphs();
        
        advanceWidth = new int[ numHMetrics ];
        leftSideBearing = new short[ numHMetrics ];
        for( int i=0; i<numHMetrics; i++ )
        {
            advanceWidth[i] = data.readUnsignedShort();
            leftSideBearing[i] = data.readSignedShort();
        }
        
        int numberNonHorizontal = numGlyphs - numHMetrics;
        nonHorizontalLeftSideBearing = new short[ numberNonHorizontal ];
        for( int i=0; i<numberNonHorizontal; i++ )
        {
            nonHorizontalLeftSideBearing[i] = data.readSignedShort();
        }
    }
    /**
     * @return Returns the advanceWidth.
     */
    public int[] getAdvanceWidth()
    {
        return advanceWidth;
    }
    /**
     * @param advanceWidthValue The advanceWidth to set.
     */
    public void setAdvanceWidth(int[] advanceWidthValue)
    {
        this.advanceWidth = advanceWidthValue;
    }
}
