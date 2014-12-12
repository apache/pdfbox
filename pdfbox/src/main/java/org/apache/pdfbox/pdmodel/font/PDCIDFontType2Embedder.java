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

import java.io.InputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Embedded PDCIDFontType2 builder. Helper class to populate a PDCIDFontType2 and its parent
 * PDType0Font from a TTF.
 *
 * @author Keiji Suzuki
 * @author John Hewson
 */
final class PDCIDFontType2Embedder extends TrueTypeEmbedder
{
    private final PDType0Font parent;
    private final COSDictionary cidFont;

    /**
     * Creates a new TrueType font embedder for the given TTF as a PDCIDFontType2.
     *
     * @param document parent document
     * @param dict font dictionary
     * @param ttfStream TTF stream
     * @param parent parent Type 0 font
     * @throws IOException if the TTF could not be read
     */
    PDCIDFontType2Embedder(PDDocument document, COSDictionary dict, InputStream ttfStream,
                           PDType0Font parent) throws IOException
    {
        super(document, dict, ttfStream);
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
        int[] subwidths = ttf.getHorizontalMetrics().getAdvanceWidth();
        int[] gidwidths = new int[subwidths.length*2];
        for (int i = 0; i < subwidths.length; i++)
        {
            gidwidths[i * 2] = i;
            gidwidths[i * 2 + 1] = subwidths[i];
        }
        cidFont.setItem(COSName.W, getWidths(gidwidths));

        // CIDToGIDMap - todo: optional (can be used for easy sub-setting)
        cidFont.setItem(COSName.CID_TO_GID_MAP, COSName.IDENTITY);

        return cidFont;
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

        final int FIRST = 0, BRACKET = 1, SERIAL = 2;
        int state = FIRST;

        for (int i = 2; i < widths.length; i += 2)
        {
            long cid   = widths[i];
            long value = Math.round(widths[i + 1] * scaling);

            switch (state)
            {
                case FIRST:
                    if (cid == lastCid + 1 && value == lastValue)
                    {
                        state = SERIAL;
                    }
                    else if (cid == lastCid + 1)
                    {
                        state = BRACKET;
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
                        state = SERIAL;
                        outer.add(inner);
                        outer.add(COSInteger.get(lastCid));
                    }
                    else if (cid == lastCid + 1)
                    {
                        inner.add(COSInteger.get(lastValue));
                    }
                    else
                    {
                        state = FIRST;
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
                        state = FIRST;
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
        return PDFontFactory.createDescendantFont(cidFont, parent);
    }
}
