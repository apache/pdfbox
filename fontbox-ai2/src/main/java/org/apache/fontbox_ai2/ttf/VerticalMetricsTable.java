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
package org.apache.fontbox_ai2.ttf;

import java.io.IOException;

/**
 * A vertical metrics 'vmtx' table in a TrueType or OpenType font.
 * 
 * This table is required by the OpenType CJK Font Guidelines for "all
 * OpenType fonts that are used for vertical writing".
 * 
 * This table is specified in both the TrueType and OpenType specifications.
 * 
 * @author Glenn Adams
 * 
 */
public class VerticalMetricsTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "vmtx";
    
    private int[] advanceHeight;
    private short[] topSideBearing;
    private short[] nonVerticalTopSideBearing;
    private int numVMetrics;

    VerticalMetricsTable(TrueTypeFont font)
    {
        super(font);
    }

    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        VerticalHeaderTable vHeader = ttf.getVerticalHeader();
        numVMetrics = vHeader.getNumberOfVMetrics();
        int numGlyphs = ttf.getNumberOfGlyphs();

        int bytesRead = 0;
        advanceHeight = new int[ numVMetrics ];
        topSideBearing = new short[ numVMetrics ];
        for( int i=0; i<numVMetrics; i++ )
        {
            advanceHeight[i] = data.readUnsignedShort();
            topSideBearing[i] = data.readSignedShort();
            bytesRead += 4;
        }

        if (bytesRead < getLength())
        {
            int numberNonVertical = numGlyphs - numVMetrics;

            // handle bad fonts with too many vmetrics
            if (numberNonVertical < 0)
            {
                numberNonVertical = numGlyphs;
            }

            nonVerticalTopSideBearing = new short[ numberNonVertical ];
            for( int i=0; i<numberNonVertical; i++ )
            {
                if (bytesRead < getLength())
                {
                    nonVerticalTopSideBearing[i] = data.readSignedShort();
                    bytesRead += 2;
                }
            }
        }

        initialized = true;
    }

    /**
     * Returns the advance height for the given GID.
     *
     * @param gid GID
     */
    public int getAdvanceHeight(int gid)
    {
        if (gid < numVMetrics)
        {
            return advanceHeight[gid];
        }
        else
        {
            // monospaced fonts may not have a height for every glyph
            // the last one is for subsequent glyphs
            return advanceHeight[advanceHeight.length -1];
        }
    }
}
