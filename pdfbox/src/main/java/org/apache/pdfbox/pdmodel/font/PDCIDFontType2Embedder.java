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

import org.apache.fontbox.ttf.TrueTypeFont;
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
    private final PDDocument document;
    private final PDType0Font parent;
    private final COSDictionary dict;
    private final COSDictionary cidFont;

    /**
     * Creates a new TrueType font embedder for the given TTF as a PDCIDFontType2.
     *
     * @param document parent document
     * @param dict font dictionary
     * @param ttf True Type Font
     * @param parent parent Type 0 font
     * @throws IOException if the TTF could not be read
     */
    PDCIDFontType2Embedder(PDDocument document, COSDictionary dict, TrueTypeFont ttf,
                           boolean embedSubset, PDType0Font parent) throws IOException
    {
        super(document, dict, ttf, embedSubset);
        this.document = document;
        this.dict = dict;
        this.parent = parent;

        // parent Type 0 font
        dict.setItem(COSName.SUBTYPE, COSName.TYPE0);
        dict.setName(COSName.BASE_FONT, fontDescriptor.getFontName());
        dict.setItem(COSName.ENCODING, COSName.IDENTITY_H); // CID = GID

        // descendant CIDFont
        cidFont = createCIDFont();
        COSArray descendantFonts = new COSArray();
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
    protected void buildSubset(InputStream ttfSubset, String tag, Map<Integer, Integer> gidToCid)
            throws IOException
    {
        // build CID2GIDMap, because the content stream has been written with the old GIDs
        Map<Integer, Integer> cidToGid = new HashMap<Integer, Integer>(gidToCid.size());
        for (Map.Entry<Integer, Integer> entry : gidToCid.entrySet())
        {
            int newGID = entry.getKey();
            int oldGID = entry.getValue();
            cidToGid.put(oldGID, newGID);
        }
        // build unicode mapping before subsetting as the subsetted font won't have a cmap
        buildToUnicodeCMap(gidToCid);
        // rebuild the relevant part of the font
        buildFontFile2(ttfSubset);
        addNameTag(tag);
        buildWidths(cidToGid);
        buildCIDToGIDMap(cidToGid);
        buildCIDSet(cidToGid);
    }

    private void buildToUnicodeCMap(Map<Integer, Integer> newGIDToOldCID) throws IOException
    {
        ToUnicodeWriter toUniWriter = new ToUnicodeWriter();
        boolean hasSurrogates = false;
        for (int gid = 1, max = ttf.getMaximumProfile().getNumGlyphs(); gid <= max; gid++)
        {
            // optional CID2GIDMap for subsetting
            int cid;
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
            List<Integer> codes = cmap.getCharCodes(cid); // old GID -> Unicode
            if (codes != null)
            {
                // use the first entry even for ambiguous mappings
                int codePoint = codes.get(0);
                if (codePoint > 0xFFFF)
                {
                    hasSurrogates = true;
                }
                toUniWriter.add(cid, new String(new int[]{ codePoint }, 0, 1));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toUniWriter.writeTo(out);
        InputStream cMapStream = new ByteArrayInputStream(out.toByteArray());

        PDStream stream = new PDStream(document, cMapStream, COSName.FLATE_DECODE);

        // surrogate code points, requires PDF 1.5
        if (hasSurrogates)
        {
            float version = document.getVersion();
            if (version < 1.5)
            {
                document.setVersion(1.5f);
            }
        }

        dict.setItem(COSName.TO_UNICODE, stream);
    }

    private COSDictionary toCIDSystemInfo(String registry, String ordering, int supplement)
    {
        COSDictionary info = new COSDictionary();
        info.setString(COSName.REGISTRY, registry);
        info.setString(COSName.ORDERING, ordering);
        info.setInt(COSName.SUPPLEMENT, supplement);
        return info;
    }

    private COSDictionary createCIDFont() throws IOException
    {
        COSDictionary cidFont = new COSDictionary();

        // Type, Subtype
        cidFont.setItem(COSName.TYPE, COSName.FONT);
        cidFont.setItem(COSName.SUBTYPE, COSName.CID_FONT_TYPE2);

        // BaseFont
        cidFont.setName(COSName.BASE_FONT, fontDescriptor.getFontName());

        // CIDSystemInfo
        COSDictionary info = toCIDSystemInfo("Adobe", "Identity", 0);
        cidFont.setItem(COSName.CIDSYSTEMINFO, info);

        // FontDescriptor
        cidFont.setItem(COSName.FONT_DESC, fontDescriptor.getCOSObject());

        // W - widths
        buildWidths(cidFont);

        // CIDToGIDMap
        cidFont.setItem(COSName.CID_TO_GID_MAP, COSName.IDENTITY);

        return cidFont;
    }

    private void addNameTag(String tag) throws IOException
    {
        String name = fontDescriptor.getFontName();
        String newName = tag + name;

        dict.setName(COSName.BASE_FONT, newName);
        fontDescriptor.setFontName(newName);
        cidFont.setName(COSName.BASE_FONT, newName);
    }

    private void buildCIDToGIDMap(Map<Integer, Integer> cidToGid) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int cidMax = Collections.max(cidToGid.keySet());
        for (int i = 0; i <= cidMax; i++)
        {
            int gid;
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

        InputStream input = new ByteArrayInputStream(out.toByteArray());
        PDStream stream = new PDStream(document, input, COSName.FLATE_DECODE);
        stream.getCOSObject().setInt(COSName.LENGTH1, stream.toByteArray().length);

        cidFont.setItem(COSName.CID_TO_GID_MAP, stream);
    }

    /**
     * Builds the CIDSet entry, required by PDF/A. This lists all CIDs in the font.
     */
    private void buildCIDSet(Map<Integer, Integer> cidToGid) throws IOException
    {
        byte[] bytes = new byte[Collections.max(cidToGid.keySet()) / 8 + 1];
        for (int cid : cidToGid.keySet())
        {
            int mask = 1 << 7 - cid % 8;
            bytes[cid / 8] |= mask;
        }

        InputStream input = new ByteArrayInputStream(bytes);
        PDStream stream = new PDStream(document, input, COSName.FLATE_DECODE);

        fontDescriptor.setCIDSet(stream);
    }

    /**
     * Builds widths with a custom CIDToGIDMap (for embedding font subset).
     */
    private void buildWidths(Map<Integer, Integer> cidToGid) throws IOException
    {
        float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        COSArray widths = new COSArray();
        COSArray ws = new COSArray();
        int prev = Integer.MIN_VALUE;
        // Use a sorted list to get an optimal width array  
        Set<Integer> keys = new TreeSet<Integer>(cidToGid.keySet());
        for (int cid : keys)
        {
            int gid = cidToGid.get(cid);
            long width = Math.round(ttf.getHorizontalMetrics().getAdvanceWidth(gid) * scaling);
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

    /**
     * Build widths with Identity CIDToGIDMap (for embedding full font).
     */
    private void buildWidths(COSDictionary cidFont) throws IOException
    {
        int cidMax = ttf.getNumberOfGlyphs();
        int[] gidwidths = new int[cidMax * 2];
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

    private COSArray getWidths(int[] widths) throws IOException
    {
        if (widths.length == 0)
        {
            throw new IllegalArgumentException("length of widths must be > 0");
        }

        float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        long lastCid = widths[0];
        long lastValue = Math.round(widths[1] * scaling);

        COSArray inner = null;
        COSArray outer = new COSArray();
        outer.add(COSInteger.get(lastCid));

        State state = State.FIRST;

        for (int i = 2; i < widths.length; i += 2)
        {
            long cid   = widths[i];
            long value = Math.round(widths[i + 1] * scaling);

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
     * Returns the descendant CIDFont.
     */
    public PDCIDFont getCIDFont() throws IOException
    {
        return new PDCIDFontType2(cidFont, parent, ttf);
    }
}
