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

import org.apache.pdfbox.io.RandomAccessReadBuffer;

/**
 * This 'glyf'-table is a required table in a TrueType font.
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

    private HorizontalMetricsTable hmt = null;
    private MaximumProfileTable maxp = null;

    /**
     * Don't even bother to cache huge fonts.
     */
    private static final int MAX_CACHE_SIZE = 5000;

    /**
     * Don't cache more glyphs than this.
     */
    private static final int MAX_CACHED_GLYPHS = 100;

    GlyphTable()
    {
    }

    /**
     * This will read the required data from the stream.
     *
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        loca = ttf.getIndexToLocation();
        numGlyphs = ttf.getNumberOfGlyphs();

        if (numGlyphs < MAX_CACHE_SIZE)
        {
            // don't cache the huge fonts to save memory
            glyphs = new GlyphData[numGlyphs];
        }

        // we don't actually read the complete table here because it can contain tens of thousands of glyphs
        // cache the relevant part of the font data so that the data stream can be closed if it is no longer needed
        byte[] dataBytes = data.read((int) getLength());
        try (RandomAccessReadBuffer read = new RandomAccessReadBuffer(dataBytes))
        {
            this.data = new RandomAccessReadDataStream(read);
        }

        // PDFBOX-5460: read hmtx table early to avoid deadlock if getGlyph() locks "data"
        // and then locks TrueTypeFont to read this table, while another thread
        // locks TrueTypeFont and then tries to lock "data"
        hmt = ttf.getHorizontalMetrics();

        maxp = ttf.getMaximumProfile();

        initialized = true;
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
     *
     * @return data of the glyph with the given GID or null
     *
     * @throws IOException if the font cannot be read
     */
    public GlyphData getGlyph(int gid) throws IOException
    {
        return getGlyph(gid, 0);
    }

    GlyphData getGlyph(int gid, int level) throws IOException
    {
        if (gid < 0 || gid >= numGlyphs)
        {
            return null;
        }

        if (glyphs != null && glyphs[gid] != null)
        {
            return glyphs[gid];
        }

        GlyphData glyph;

        // PDFBOX-4219: synchronize on data because it is accessed by several threads
        // when PDFBox is accessing a standard 14 font for the first time
        synchronized (data)
        {
            // read a single glyph
            long[] offsets = loca.getOffsets();

            if (offsets[gid] == offsets[gid + 1] || offsets[gid] == data.getOriginalDataSize())
            {
                // no outline
                // PDFBOX-5135: can't return null, must return an empty glyph because
                // sometimes this is used in a composite glyph.
                // PDFBOX-5917: offset points to end of the stream
                glyph = new GlyphData();
                glyph.initEmptyData();
            }
            else
            {
                // save
                long currentPosition = data.getCurrentPosition();

                data.seek(offsets[gid]);

                glyph = getGlyphData(gid, level);

                // restore
                data.seek(currentPosition);
            }

            if (glyphs != null && glyphs[gid] == null && cached < MAX_CACHED_GLYPHS)
            {
                glyphs[gid] = glyph;
                ++cached;
            }

            return glyph;
        }
    }

    private GlyphData getGlyphData(int gid, int level) throws IOException
    {
        if (level > maxp.getMaxComponentDepth())
        {
            throw new IOException("composite glyph maximum level reached");
        }
        GlyphData glyph = new GlyphData();
        int leftSideBearing = hmt == null ? 0 : hmt.getLeftSideBearing(gid);
        glyph.initData(this, data, leftSideBearing, level);
        // resolve composite glyph
        if (glyph.getDescription().isComposite())
        {
            glyph.getDescription().resolve();
        }
        return glyph;
    }
}
