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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fontbox.ttf.HorizontalMetricsTable;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;

/**
 * Embedded PDTrueTypeFont builder. Helper class to populate a PDTrueTypeFont from a TTF.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
final class PDTrueTypeFontEmbedder extends TrueTypeEmbedder
{
    private final Encoding fontEncoding;

    /**
     * Creates a new TrueType font embedder for the given TTF as a PDTrueTypeFont.
     *
     * @param document parent document
     * @param dict font dictionary
     * @param ttfStream TTF stream
     * @throws IOException if the TTF could not be read
     */
    PDTrueTypeFontEmbedder(PDDocument document, COSDictionary dict, InputStream ttfStream)
            throws IOException
    {
        super(document, dict, ttfStream);
        dict.setItem(COSName.SUBTYPE, COSName.TRUE_TYPE);

        // only support WinAnsiEncoding encoding right now
        Encoding encoding = new WinAnsiEncoding(); // fixme: read encoding from TTF
        this.fontEncoding = encoding;
        dict.setItem(COSName.ENCODING, encoding.getCOSObject());

        // add the font descriptor
        dict.setItem(COSName.FONT_DESC, fontDescriptor);

        // set the glyph widths
        setWidths(dict);
    }

    /**
     * Sets the glyph widths in the font dictionary.
     */
    private void setWidths(COSDictionary font) throws IOException
    {
        float scaling = 1000f / ttf.getHeader().getUnitsPerEm();

        Map<Integer, String> codeToName = this.getFontEncoding().getCodeToNameMap();

        int firstChar = Collections.min(codeToName.keySet());
        int lastChar = Collections.max(codeToName.keySet());

        HorizontalMetricsTable hMet = ttf.getHorizontalMetrics();
        int[] widthValues = hMet.getAdvanceWidth();

        // some monospaced fonts provide only one value for the width
        // instead of an array containing the same value for every glyph id
        boolean isMonospaced = ttf.getHorizontalHeader().getNumberOfHMetrics() == 1;

        int numWidths = lastChar - firstChar + 1;
        List<Integer> widths = new ArrayList<Integer>(numWidths);

        // use the first width as default
        // proportional fonts -> width of the .notdef character
        // monospaced-fonts -> the first width
        int defaultWidth = Math.round(widthValues[0] * scaling);
        for (int i = 0; i < numWidths; i++)
        {
            widths.add(defaultWidth);
        }

        // a character code is mapped to a glyph name via the provided font encoding
        // afterwards, the glyph name is translated to a glyph ID.
        for (Map.Entry<Integer, String> e : codeToName.entrySet())
        {
            String name = e.getValue();
            // pdf code to unicode by glyph list.
            if (!name.equals(".notdef"))
            {
                // todo: we're supposed to use the 'provided font encoding'
                String c = GlyphList.getAdobeGlyphList().toUnicode(name);
                int charCode = c.codePointAt(0);
                int gid = cmap.getGlyphId(charCode);
                if (gid != 0)
                {
                    if (isMonospaced)
                    {
                        widths.set(e.getKey() - firstChar, defaultWidth);
                    }
                    else
                    {
                        widths.set(e.getKey() - firstChar,
                                Math.round(widthValues[gid] * scaling));
                    }
                }
            }
        }

        font.setInt(COSName.FIRST_CHAR, firstChar);
        font.setInt(COSName.LAST_CHAR, lastChar);
        font.setItem(COSName.WIDTHS, COSArrayList.converterToCOSArray(widths));
    }

    /**
     * Returns the font's encoding.
     */
    public Encoding getFontEncoding()
    {
        return fontEncoding;
    }
}
