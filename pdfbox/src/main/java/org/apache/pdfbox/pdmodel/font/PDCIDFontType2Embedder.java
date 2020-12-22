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

package org.apache.pdfbox.pdmodel.font;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.HorizontalMetricsTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.VerticalHeaderTable;
import org.apache.fontbox.ttf.VerticalMetricsTable;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Embedded PDCIDFontType2 builder. Helper class to populate a PDCIDFontType2 and its parent
 * PDType0Font from a TTF.
 *
 * @author Keiji Suzuki
 * @author John Hewson
 */
final class PDCIDFontType2Embedder extends TrueTypeEmbedder
{

    private static final Log LOG = LogFactory.getLog(PDCIDFontType2Embedder.class);

    private final PDDocument document;
    private final PDType0Font parent;
    private final COSDictionary dict;
    private final COSDictionary cidFont;
    private final boolean vertical;

    /**
     * Creates a new TrueType font embedder for the given TTF as a PDCIDFontType2.
     *
     * @param document parent document
     * @param dict font dictionary
     * @param ttf True Type Font
     * @param parent parent Type 0 font
     * @throws IOException if the TTF could not be read
     */
    PDCIDFontType2Embedder(final PDDocument document, final COSDictionary dict, final TrueTypeFont ttf,
                           final boolean embedSubset, final PDType0Font parent, final boolean vertical) throws IOException
    {
        super(document, dict, ttf, embedSubset);
        this.document = document;
        this.dict = dict;
        this.parent = parent;
        this.vertical = vertical;

        // parent Type 0 font
        dict.setItem(COSName.SUBTYPE, COSName.TYPE0);
        dict.setName(COSName.BASE_FONT, fontDescriptor.getFontName());
        dict.setItem(COSName.ENCODING, vertical ? COSName.IDENTITY_V : COSName.IDENTITY_H); // CID = GID

        // descendant CIDFont
        cidFont = createCIDFont();
        final COSArray descendantFonts = new COSArray();
        descendantFonts.add(cidFont);
        dict.setItem(COSName.DESCENDANT_FONTS, descendantFonts);

        if (!embedSubset)
        {
            // build GID -> Unicode map
            buildToUnicodeCMap(null);
        }
    }

    /**
     * Rebuild a font subset.
     */
    @Override
    protected void buildSubset(final InputStream ttfSubset, final String tag, final Map<Integer, Integer> gidToCid)
            throws IOException
    {
        // build CID2GIDMap, because the content stream has been written with the old GIDs
        final Map<Integer, Integer> cidToGid = new HashMap<>(gidToCid.size());
        gidToCid.forEach((newGID, oldGID) -> cidToGid.put(oldGID, newGID));
        
        // build unicode mapping before subsetting as the subsetted font won't have a cmap
        buildToUnicodeCMap(gidToCid);
        // build vertical metrics before subsetting as the subsetted font won't have vhea, vmtx
        if (vertical)
        {
            buildVerticalMetrics(cidToGid);
        }
        // rebuild the relevant part of the font
        buildFontFile2(ttfSubset);
        addNameTag(tag);
        buildWidths(cidToGid);
        buildCIDToGIDMap(cidToGid);
        buildCIDSet(cidToGid);
    }

    private void buildToUnicodeCMap(final Map<Integer, Integer> newGIDToOldCID) throws IOException
    {
        final ToUnicodeWriter toUniWriter = new ToUnicodeWriter();
        boolean hasSurrogates = false;
        for (int gid = 1, max = ttf.getMaximumProfile().getNumGlyphs(); gid <= max; gid++)
        {
            // optional CID2GIDMap for subsetting
            final int cid;
            if (newGIDToOldCID != null)
            {
                if (!newGIDToOldCID.containsKey(gid))
                {
                    continue;
                }
                else
                {
                    cid = newGIDToOldCID.get(gid);
                }
            }
            else
            {
                cid = gid;
            }

            // skip composite glyph components that have no code point
            final List<Integer> codes = cmapLookup.getCharCodes(cid); // old GID -> Unicode
            if (codes != null)
            {
                // use the first entry even for ambiguous mappings
                final int codePoint = codes.get(0);
                if (codePoint > 0xFFFF)
                {
                    hasSurrogates = true;
                }
                toUniWriter.add(cid, new String(new int[]{ codePoint }, 0, 1));
            }
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        toUniWriter.writeTo(out);
        final InputStream cMapStream = new ByteArrayInputStream(out.toByteArray());

        final PDStream stream = new PDStream(document, cMapStream, COSName.FLATE_DECODE);

        // surrogate code points, requires PDF 1.5
        if (hasSurrogates)
        {
            final float version = document.getVersion();
            if (version < 1.5)
            {
                document.setVersion(1.5f);
            }
        }

        dict.setItem(COSName.TO_UNICODE, stream);
    }

    private COSDictionary toCIDSystemInfo(final String registry, final String ordering, final int supplement)
    {
        final COSDictionary info = new COSDictionary();
        info.setString(COSName.REGISTRY, registry);
        info.setString(COSName.ORDERING, ordering);
        info.setInt(COSName.SUPPLEMENT, supplement);
        return info;
    }

    private COSDictionary createCIDFont() throws IOException
    {
        final COSDictionary cidFont = new COSDictionary();

        // Type, Subtype
        cidFont.setItem(COSName.TYPE, COSName.FONT);
        cidFont.setItem(COSName.SUBTYPE, COSName.CID_FONT_TYPE2);

        // BaseFont
        cidFont.setName(COSName.BASE_FONT, fontDescriptor.getFontName());

        // CIDSystemInfo
        final COSDictionary info = toCIDSystemInfo("Adobe", "Identity", 0);
        cidFont.setItem(COSName.CIDSYSTEMINFO, info);

        // FontDescriptor
        cidFont.setItem(COSName.FONT_DESC, fontDescriptor.getCOSObject());

        // W - widths
        buildWidths(cidFont);

        // Vertical metrics
        if (vertical)
        {
            buildVerticalMetrics(cidFont);
        }

        // CIDToGIDMap
        cidFont.setItem(COSName.CID_TO_GID_MAP, COSName.IDENTITY);

        return cidFont;
    }

    private void addNameTag(final String tag)
    {
        final String name = fontDescriptor.getFontName();
        final String newName = tag + name;

        dict.setName(COSName.BASE_FONT, newName);
        fontDescriptor.setFontName(newName);
        cidFont.setName(COSName.BASE_FONT, newName);
    }

    private void buildCIDToGIDMap(final Map<Integer, Integer> cidToGid) throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final int cidMax = Collections.max(cidToGid.keySet());
        for (int i = 0; i <= cidMax; i++)
        {
            final int gid;
            if (cidToGid.containsKey(i))
            {
                gid = cidToGid.get(i);
            }
            else
            {
                gid = 0;
            }
            out.write(new byte[] { (byte)(gid >> 8 & 0xff), (byte)(gid & 0xff) });
        }

        final InputStream input = new ByteArrayInputStream(out.toByteArray());
        final PDStream stream = new PDStream(document, input, COSName.FLATE_DECODE);

        cidFont.setItem(COSName.CID_TO_GID_MAP, stream);
    }

    /**
     * Builds the CIDSet entry, required by PDF/A. This lists all CIDs in the font, including those
     * that don't have a GID.
     */
    private void buildCIDSet(final Map<Integer, Integer> cidToGid) throws IOException
    {
        final int cidMax = Collections.max(cidToGid.keySet());
        final byte[] bytes = new byte[cidMax / 8 + 1];
        for (int cid = 0; cid <= cidMax; cid++)
        {
            final int mask = 1 << 7 - cid % 8;
            bytes[cid / 8] |= mask;
        }

        final InputStream input = new ByteArrayInputStream(bytes);
        final PDStream stream = new PDStream(document, input, COSName.FLATE_DECODE);

        fontDescriptor.setCIDSet(stream);
    }

    /**
     * Builds widths with a custom CIDToGIDMap (for embedding font subset).
     */
    private void buildWidths(final Map<Integer, Integer> cidToGid) throws IOException
    {
        final float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        final COSArray widths = new COSArray();
        COSArray ws = new COSArray();
        int prev = Integer.MIN_VALUE;
        // Use a sorted list to get an optimal width array  
        final Set<Integer> keys = new TreeSet<>(cidToGid.keySet());
        for (final int cid : keys)
        {
            final int gid = cidToGid.get(cid);
            final long width = Math.round(ttf.getHorizontalMetrics().getAdvanceWidth(gid) * scaling);
            if (width == 1000)
            {
                // skip default width
                continue;
            }
            // c [w1 w2 ... wn]
            if (prev != cid - 1)
            {
                ws = new COSArray();
                widths.add(COSInteger.get(cid)); // c
                widths.add(ws);
            }
            ws.add(COSInteger.get(width)); // wi
            prev = cid;
        }
        cidFont.setItem(COSName.W, widths);
    }

    private boolean buildVerticalHeader(final COSDictionary cidFont) throws IOException
    {
        final VerticalHeaderTable vhea = ttf.getVerticalHeader();
        if (vhea == null)
        {
            LOG.warn("Font to be subset is set to vertical, but has no 'vhea' table");
            return false;
        }

        final float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        final long v = Math.round(vhea.getAscender() * scaling);
        final long w1 = Math.round(-vhea.getAdvanceHeightMax() * scaling);
        if (v != 880 || w1 != -1000)
        {
            final COSArray cosDw2 = new COSArray();
            cosDw2.add(COSInteger.get(v));
            cosDw2.add(COSInteger.get(w1));
            cidFont.setItem(COSName.DW2, cosDw2);
        }
        return true;
    }

    /**
     * Builds vertical metrics with a custom CIDToGIDMap (for embedding font subset).
     */
    private void buildVerticalMetrics(final Map<Integer, Integer> cidToGid) throws IOException
    {
        // The "vhea" and "vmtx" tables that specify vertical metrics shall never be used by a conforming
        // reader. The only way to specify vertical metrics in PDF shall be by means of the DW2 and W2
        // entries in a CIDFont dictionary.

        if (!buildVerticalHeader(cidFont))
        {
            return;
        }

        final float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        final VerticalHeaderTable vhea = ttf.getVerticalHeader();
        final VerticalMetricsTable vmtx = ttf.getVerticalMetrics();
        final GlyphTable glyf = ttf.getGlyph();
        final HorizontalMetricsTable hmtx = ttf.getHorizontalMetrics();

        final long v_y = Math.round(vhea.getAscender() * scaling);
        final long w1 = Math.round(-vhea.getAdvanceHeightMax() * scaling);

        final COSArray heights = new COSArray();
        COSArray w2 = new COSArray();
        int prev = Integer.MIN_VALUE;
        // Use a sorted list to get an optimal width array
        final Set<Integer> keys = new TreeSet<>(cidToGid.keySet());
        for (final int cid : keys)
        {
            // Unlike buildWidths, we look up with cid (not gid) here because this is
            // the original TTF, not the rebuilt one.
            final GlyphData glyph = glyf.getGlyph(cid);
            if (glyph == null)
            {
                continue;
            }
            final long height = Math.round((glyph.getYMaximum() + vmtx.getTopSideBearing(cid)) * scaling);
            final long advance = Math.round(-vmtx.getAdvanceHeight(cid) * scaling);
            if (height == v_y && advance == w1)
            {
                // skip default metrics
                continue;
            }
            // c [w1_1y v_1x v_1y w1_2y v_2x v_2y ... w1_ny v_nx v_ny]
            if (prev != cid - 1)
            {
                w2 = new COSArray();
                heights.add(COSInteger.get(cid)); // c
                heights.add(w2);
            }
            w2.add(COSInteger.get(advance)); // w1_iy
            final long width = Math.round(hmtx.getAdvanceWidth(cid) * scaling);
            w2.add(COSInteger.get(width / 2)); // v_ix
            w2.add(COSInteger.get(height)); // v_iy
            prev = cid;
        }
        cidFont.setItem(COSName.W2, heights);
    }

    /**
     * Build widths with Identity CIDToGIDMap (for embedding full font).
     */
    private void buildWidths(final COSDictionary cidFont) throws IOException
    {
        final int cidMax = ttf.getNumberOfGlyphs();
        final int[] gidwidths = new int[cidMax * 2];
        for (int cid = 0; cid < cidMax; cid++)
        {
            gidwidths[cid * 2] = cid;
            gidwidths[cid * 2 + 1] = ttf.getHorizontalMetrics().getAdvanceWidth(cid);
        }

        cidFont.setItem(COSName.W, getWidths(gidwidths));
    }
    
    enum State
    {
        FIRST, BRACKET, SERIAL
    }

    private COSArray getWidths(final int[] widths) throws IOException
    {
        if (widths.length == 0)
        {
            throw new IllegalArgumentException("length of widths must be > 0");
        }

        final float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        long lastCid = widths[0];
        long lastValue = Math.round(widths[1] * scaling);

        COSArray inner = new COSArray();
        final COSArray outer = new COSArray();
        outer.add(COSInteger.get(lastCid));

        State state = State.FIRST;

        for (int i = 2; i < widths.length; i += 2)
        {
            final long cid   = widths[i];
            final long value = Math.round(widths[i + 1] * scaling);

            switch (state)
            {
                case FIRST:
                    if (cid == lastCid + 1 && value == lastValue)
                    {
                        state = State.SERIAL;
                    }
                    else if (cid == lastCid + 1)
                    {
                        state = State.BRACKET;
                        inner = new COSArray();
                        inner.add(COSInteger.get(lastValue));
                    }
                    else
                    {
                        inner = new COSArray();
                        inner.add(COSInteger.get(lastValue));
                        outer.add(inner);
                        outer.add(COSInteger.get(cid));
                    }
                    break;
                case BRACKET:
                    if (cid == lastCid + 1 && value == lastValue)
                    {
                        state = State.SERIAL;
                        outer.add(inner);
                        outer.add(COSInteger.get(lastCid));
                    }
                    else if (cid == lastCid + 1)
                    {
                        inner.add(COSInteger.get(lastValue));
                    }
                    else
                    {
                        state = State.FIRST;
                        inner.add(COSInteger.get(lastValue));
                        outer.add(inner);
                        outer.add(COSInteger.get(cid));
                    }
                    break;
                case SERIAL:
                    if (cid != lastCid + 1 || value != lastValue)
                    {
                        outer.add(COSInteger.get(lastCid));
                        outer.add(COSInteger.get(lastValue));
                        outer.add(COSInteger.get(cid));
                        state = State.FIRST;
                    }
                    break;
            }
            lastValue = value;
            lastCid = cid;
        }

        switch (state)
        {
            case FIRST:
                inner = new COSArray();
                inner.add(COSInteger.get(lastValue));
                outer.add(inner);
                break;
            case BRACKET:
                inner.add(COSInteger.get(lastValue));
                outer.add(inner);
                break;
            case SERIAL:
                outer.add(COSInteger.get(lastCid));
                outer.add(COSInteger.get(lastValue));
                break;
        }
        return outer;
    }

    /**
     * Build vertical metrics with Identity CIDToGIDMap (for embedding full font).
     */
    private void buildVerticalMetrics(final COSDictionary cidFont) throws IOException
    {
        if (!buildVerticalHeader(cidFont))
        {
            return;
        }

        final int cidMax = ttf.getNumberOfGlyphs();
        final int[] gidMetrics = new int[cidMax * 4];
        for (int cid = 0; cid < cidMax; cid++)
        {
            final GlyphData glyph = ttf.getGlyph().getGlyph(cid);
            if (glyph == null)
            {
                gidMetrics[cid * 4] = Integer.MIN_VALUE;
            }
            else
            {
                gidMetrics[cid * 4] = cid;
                gidMetrics[cid * 4 + 1] = ttf.getVerticalMetrics().getAdvanceHeight(cid);
                gidMetrics[cid * 4 + 2] = ttf.getHorizontalMetrics().getAdvanceWidth(cid);
                gidMetrics[cid * 4 + 3] = glyph.getYMaximum() + ttf.getVerticalMetrics().getTopSideBearing(cid);
            }
        }

        cidFont.setItem(COSName.W2, getVerticalMetrics(gidMetrics));
    }

    private COSArray getVerticalMetrics(final int[] values) throws IOException
    {
        if (values.length == 0)
        {
            throw new IllegalArgumentException("length of values must be > 0");
        }

        final float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        long lastCid = values[0];
        long lastW1Value = Math.round(-values[1] * scaling);
        long lastVxValue = Math.round(values[2] * scaling / 2f);
        long lastVyValue = Math.round(values[3] * scaling);

        COSArray inner = new COSArray();
        final COSArray outer = new COSArray();
        outer.add(COSInteger.get(lastCid));

        State state = State.FIRST;

        for (int i = 4; i < values.length; i += 4)
        {
            final long cid = values[i];
            if (cid == Integer.MIN_VALUE)
            {
                // no glyph for this cid
                continue;
            }
            final long w1Value = Math.round(-values[i + 1] * scaling);
            final long vxValue = Math.round(values[i + 2] * scaling / 2);
            final long vyValue = Math.round(values[i + 3] * scaling);

            switch (state)
            {
            case FIRST:
                if (cid == lastCid + 1 && w1Value == lastW1Value && vxValue == lastVxValue && vyValue == lastVyValue)
                {
                    state = State.SERIAL;
                }
                else if (cid == lastCid + 1)
                {
                    state = State.BRACKET;
                    inner = new COSArray();
                    inner.add(COSInteger.get(lastW1Value));
                    inner.add(COSInteger.get(lastVxValue));
                    inner.add(COSInteger.get(lastVyValue));
                }
                else
                {
                    inner = new COSArray();
                    inner.add(COSInteger.get(lastW1Value));
                    inner.add(COSInteger.get(lastVxValue));
                    inner.add(COSInteger.get(lastVyValue));
                    outer.add(inner);
                    outer.add(COSInteger.get(cid));
                }
                break;
            case BRACKET:
                if (cid == lastCid + 1 && w1Value == lastW1Value && vxValue == lastVxValue && vyValue == lastVyValue)
                {
                    state = State.SERIAL;
                    outer.add(inner);
                    outer.add(COSInteger.get(lastCid));
                }
                else if (cid == lastCid + 1)
                {
                    inner.add(COSInteger.get(lastW1Value));
                    inner.add(COSInteger.get(lastVxValue));
                    inner.add(COSInteger.get(lastVyValue));
                }
                else
                {
                    state = State.FIRST;
                    inner.add(COSInteger.get(lastW1Value));
                    inner.add(COSInteger.get(lastVxValue));
                    inner.add(COSInteger.get(lastVyValue));
                    outer.add(inner);
                    outer.add(COSInteger.get(cid));
                }
                break;
            case SERIAL:
                if (cid != lastCid + 1 || w1Value != lastW1Value || vxValue != lastVxValue || vyValue != lastVyValue)
                {
                    outer.add(COSInteger.get(lastCid));
                    outer.add(COSInteger.get(lastW1Value));
                    outer.add(COSInteger.get(lastVxValue));
                    outer.add(COSInteger.get(lastVyValue));
                    outer.add(COSInteger.get(cid));
                    state = State.FIRST;
                }
                break;
            }
            lastW1Value = w1Value;
            lastVxValue = vxValue;
            lastVyValue = vyValue;
            lastCid = cid;
        }

        switch (state)
        {
        case FIRST:
            inner = new COSArray();
            inner.add(COSInteger.get(lastW1Value));
            inner.add(COSInteger.get(lastVxValue));
            inner.add(COSInteger.get(lastVyValue));
            outer.add(inner);
            break;
        case BRACKET:
            inner.add(COSInteger.get(lastW1Value));
            inner.add(COSInteger.get(lastVxValue));
            inner.add(COSInteger.get(lastVyValue));
            outer.add(inner);
            break;
        case SERIAL:
            outer.add(COSInteger.get(lastCid));
            outer.add(COSInteger.get(lastW1Value));
            outer.add(COSInteger.get(lastVxValue));
            outer.add(COSInteger.get(lastVyValue));
            break;
        }
        return outer;
    }

    /**
     * Returns the descendant CIDFont.
     */
    public PDCIDFont getCIDFont() throws IOException
    {
        return new PDCIDFontType2(cidFont, parent, ttf);
    }
}
