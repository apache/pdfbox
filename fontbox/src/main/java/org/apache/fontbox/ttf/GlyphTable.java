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
 * 
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
    public void initData(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        MaximumProfileTable maxp = ttf.getMaximumProfile();
        IndexToLocationTable loc = ttf.getIndexToLocation();
        // the glyph offsets
        long[] offsets = loc.getOffsets();
        // number of glyphs
        int numGlyphs = maxp.getNumGlyphs();
        // the end of the glyph table
        long endOfGlyphs = offsets[numGlyphs];
        long offset = getOffset();
        glyphs = new GlyphData[numGlyphs];
        for (int i = 0; i < numGlyphs; i++)
        {
            // end of glyphs reached?
            if (endOfGlyphs == offsets[i])
            {
                break;
            }
            // the current glyph isn't defined
            // if the next offset equals the current index
            if (offsets[i] == offsets[i + 1])
            {
                continue;
            }
            glyphs[i] = new GlyphData();
            data.seek(offset + offsets[i]);
            glyphs[i].initData(ttf, data);
        }
        for (int i = 0; i < numGlyphs; i++)
        {
            GlyphData glyph = glyphs[i];
            // resolve composite glyphs
            if (glyph != null && glyph.getDescription().isComposite())
            {
                glyph.getDescription().resolve();
            }
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
        glyphs = glyphsValue;
    }
}
