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
public class GlyphTable extends TTFTable
{
    /**
     * Tag to identify this table.
     */
    public static final String TAG = "glyf";
    
    private GlyphData[] glyphs;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        MaximumProfileTable maxp = ttf.getMaximumProfile();
        IndexToLocationTable loc = ttf.getIndexToLocation();
        long[] offsets = loc.getOffsets();
        int numGlyphs = maxp.getNumGlyphs();
        glyphs = new GlyphData[numGlyphs];
        for( int i=0; i<numGlyphs-1; i++ )
        {
            GlyphData glyph = new GlyphData();
            data.seek( getOffset() + offsets[i] );
            glyph.initData( ttf, data );
            glyphs[i] = glyph;
        }
    }
    /**
     * @return Returns the glyphs.
     */
    public GlyphData[] getGlyphs()
    {
        return glyphs;
    }
    /**
     * @param glyphsValue The glyphs to set.
     */
    public void setGlyphs(GlyphData[] glyphsValue)
    {
        this.glyphs = glyphsValue;
    }
}
