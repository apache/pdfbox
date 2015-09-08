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
 * A table in a true type font.
 * 
 * @author Ben Litchfield
 */
public class GlyphTable extends TTFTable
{
    /**
     * Tag to identify this table.
     */
    public static final String TAG = "glyf";

    private GlyphData[] glyphs;

    // lazy table reading
    private TTFDataStream data;
    private IndexToLocationTable loca;
    private int numGlyphs;

    GlyphTable(TrueTypeFont font)
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
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        loca = ttf.getIndexToLocation();
        numGlyphs = ttf.getNumberOfGlyphs();

        // we don't actually read the table yet because it can contain tens of thousands of glyphs
        this.data = data;
        initialized = true;
    }

    /**
     * Reads all glyphs from the font. Can be very slow.
     */
    private void readAll() throws IOException
    {
        // the glyph offsets
        long[] offsets = loca.getOffsets();

        // the end of the glyph table
        // should not be 0, but sometimes is, see PDFBOX-2044
        // structure of this table: see
        // https://developer.apple.com/fonts/TTRefMan/RM06/Chap6loca.html
        long endOfGlyphs = offsets[numGlyphs];
        long offset = getOffset();
        glyphs = new GlyphData[numGlyphs];
        for (int i = 0; i < numGlyphs; i++)
        {
            // end of glyphs reached?
            if (endOfGlyphs != 0 &&
                    endOfGlyphs == offsets[i])
            {
                break;
            }
            // the current glyph isn't defined
            // if the next offset is equal or smaller to the current offset
            if (offsets[i + 1] <= offsets[i])
            {
                continue;
            }
            glyphs[i] = new GlyphData();
            data.seek(offset + offsets[i]);
            
            HorizontalMetricsTable hmt = font.getHorizontalMetrics();
            int leftSideBearing = hmt == null ? 0 : hmt.getLeftSideBearing(i);
            
            glyphs[i].initData(this, data, leftSideBearing);
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
        initialized = true;
    }

    /**
     * Returns all glyphs. This method can be very slow.
     */
    public GlyphData[] getGlyphs() throws IOException
    {
        if (glyphs == null)
        {
            synchronized (font)
            {
                readAll();
            }
        }
        return glyphs;
    }

    /**
     * @param glyphsValue The glyphs to set.
     */
    public void setGlyphs(GlyphData[] glyphsValue)
    {
        glyphs = glyphsValue;
    }

    /**
     * Returns the data for the glyph with the given GID.
     *
     * @param gid GID
     * @throws IOException if the font cannot be read
     */
    public GlyphData getGlyph(int gid) throws IOException
    {
        if (gid < 0 || gid >= numGlyphs)
        {
            return null;
        }

        synchronized (font)
        {
            // save
            long currentPosition = data.getCurrentPosition();

            // read a single glyph
            long[] offsets = loca.getOffsets();

            GlyphData glyph;
            if (offsets[gid] == offsets[gid + 1])
            {
                // no outline
                glyph = null;
            }
            else
            {
                data.seek(getOffset() + offsets[gid]);
                glyph = new GlyphData();
                
                HorizontalMetricsTable hmt = font.getHorizontalMetrics();
                int leftSideBearing = hmt == null ? 0 : hmt.getLeftSideBearing(gid);

                glyph.initData(this, data, leftSideBearing);

                // resolve composite glyph
                if (glyph.getDescription().isComposite())
                {
                    glyph.getDescription().resolve();
                }
            }

            // restore
            data.seek(currentPosition);
            return glyph;
        }
    }
}
