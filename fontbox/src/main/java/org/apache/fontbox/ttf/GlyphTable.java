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
    
    private int cached = 0;
    
    /**
     * Don't even bother to cache huge fonts.
     */
    private static final int MAX_CACHE_SIZE = 5000;
    
    /**
     * Don't cache more glyphs than this.
     */
    private static final int MAX_CACHED_GLYPHS = 100;

    GlyphTable(final TrueTypeFont font)
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
    void read(final TrueTypeFont ttf, final TTFDataStream data) throws IOException
    {
        loca = ttf.getIndexToLocation();
        numGlyphs = ttf.getNumberOfGlyphs();

        if (numGlyphs < MAX_CACHE_SIZE)
        {
            // don't cache the huge fonts to save memory
            glyphs = new GlyphData[numGlyphs];
        }

        // we don't actually read the complete table here because it can contain tens of thousands of glyphs
        this.data = data;
        initialized = true;
    }

    /**
     * Returns all glyphs. This method can be very slow.
     *
     * @throws IOException If there is an error reading the data.
     */
    public GlyphData[] getGlyphs() throws IOException
    {
        // PDFBOX-4219: synchronize on data because it is accessed by several threads
        // when PDFBox is accessing a standard 14 font for the first time
        synchronized (data)
        {
            // the glyph offsets
            final long[] offsets = loca.getOffsets();

            // the end of the glyph table
            // should not be 0, but sometimes is, see PDFBOX-2044
            // structure of this table: see
            // https://developer.apple.com/fonts/TTRefMan/RM06/Chap6loca.html
            final long endOfGlyphs = offsets[numGlyphs];
            final long offset = getOffset();
            if (glyphs == null)
            {
                glyphs = new GlyphData[numGlyphs];
            }
         
            for (int gid = 0; gid < numGlyphs; gid++)
            {
                // end of glyphs reached?
                if (endOfGlyphs != 0 && endOfGlyphs == offsets[gid])
                {
                    break;
                }
                // the current glyph isn't defined
                // if the next offset is equal or smaller to the current offset
                if (offsets[gid + 1] <= offsets[gid])
                {
                    continue;
                }
                if (glyphs[gid] != null)
                {
                    // already cached
                    continue;
                }

                data.seek(offset + offsets[gid]);

                if (glyphs[gid] == null)
                {
                    ++cached;
                }
                glyphs[gid] = getGlyphData(gid);
            }
            initialized = true;
            return glyphs;
        }
    }

    /**
     * @param glyphsValue The glyphs to set.
     */
    public void setGlyphs(final GlyphData[] glyphsValue)
    {
        glyphs = glyphsValue;
    }

    /**
     * Returns the data for the glyph with the given GID.
     *
     * @param gid GID
     * @throws IOException if the font cannot be read
     */
    public GlyphData getGlyph(final int gid) throws IOException
    {
        if (gid < 0 || gid >= numGlyphs)
        {
            return null;
        }
        
        if (glyphs != null && glyphs[gid] != null)
        {
            return glyphs[gid];
        }

        // PDFBOX-4219: synchronize on data because it is accessed by several threads
        // when PDFBox is accessing a standard 14 font for the first time
        synchronized (data)
        {
            // read a single glyph
            final long[] offsets = loca.getOffsets();

            if (offsets[gid] == offsets[gid + 1])
            {
                // no outline
                return null;
            }
            
            // save
            final long currentPosition = data.getCurrentPosition();

            data.seek(getOffset() + offsets[gid]);

            final GlyphData glyph = getGlyphData(gid);

            // restore
            data.seek(currentPosition);

            if (glyphs != null && glyphs[gid] == null && cached < MAX_CACHED_GLYPHS)
            {
                glyphs[gid] = glyph;
                ++cached;
            }

            return glyph;
        }
    }

    private GlyphData getGlyphData(final int gid) throws IOException
    {
        final GlyphData glyph = new GlyphData();
        final HorizontalMetricsTable hmt = font.getHorizontalMetrics();
        final int leftSideBearing = hmt == null ? 0 : hmt.getLeftSideBearing(gid);
        glyph.initData(this, data, leftSideBearing);
        // resolve composite glyph
        if (glyph.getDescription().isComposite())
        {
            glyph.getDescription().resolve();
        }
        return glyph;
    }
}
