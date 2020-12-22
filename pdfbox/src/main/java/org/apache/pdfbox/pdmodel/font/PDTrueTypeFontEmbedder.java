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
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;

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
     * @param document The parent document
     * @param dict Font dictionary
     * @param ttf TTF stream
     * @param encoding The PostScript encoding vector to be used for embedding.
     * @throws IOException if the TTF could not be read
     */
    PDTrueTypeFontEmbedder(final PDDocument document, final COSDictionary dict, final TrueTypeFont ttf,
                           final Encoding encoding) throws IOException
    {
        super(document, dict, ttf, false);
        dict.setItem(COSName.SUBTYPE, COSName.TRUE_TYPE);
        
        final GlyphList glyphList = GlyphList.getAdobeGlyphList();
        this.fontEncoding = encoding;
        dict.setItem(COSName.ENCODING, encoding.getCOSObject());
        fontDescriptor.setSymbolic(false);
        fontDescriptor.setNonSymbolic(true);
        
        // add the font descriptor
        dict.setItem(COSName.FONT_DESC, fontDescriptor);

        // set the glyph widths
        setWidths(dict, glyphList);
    }

    /**
     * Sets the glyph widths in the font dictionary.
     */
    private void setWidths(final COSDictionary font, final GlyphList glyphList) throws IOException
    {
        final float scaling = 1000f / ttf.getHeader().getUnitsPerEm();
        final HorizontalMetricsTable hmtx = ttf.getHorizontalMetrics();

        final Map<Integer, String> codeToName = getFontEncoding().getCodeToNameMap();

        final int firstChar = Collections.min(codeToName.keySet());
        final int lastChar = Collections.max(codeToName.keySet());

        final List<Integer> widths = new ArrayList<>(lastChar - firstChar + 1);
        for (int i = 0; i < lastChar - firstChar + 1; i++)
        {
            widths.add(0);
        }

        // a character code is mapped to a glyph name via the provided font encoding
        // afterwards, the glyph name is translated to a glyph ID.
        for (final Map.Entry<Integer, String> entry : codeToName.entrySet())
        {
            final int code = entry.getKey();
            final String name = entry.getValue();

            if (code >= firstChar && code <= lastChar)
            {
                final String uni = glyphList.toUnicode(name);
                final int charCode = uni.codePointAt(0);
                final int gid = cmapLookup.getGlyphId(charCode);
                widths.set(entry.getKey() - firstChar,
                           Math.round(hmtx.getAdvanceWidth(gid) * scaling));
            }
        }

        font.setInt(COSName.FIRST_CHAR, firstChar);
        font.setInt(COSName.LAST_CHAR, lastChar);
        font.setItem(COSName.WIDTHS, COSArray.ofCOSIntegers(widths));
    }

    /**
     * Returns the font's encoding.
     */
    public Encoding getFontEncoding()
    {
        return fontEncoding;
    }

    @Override
    protected void buildSubset(final InputStream ttfSubset, final String tag,
                               final Map<Integer, Integer> gidToCid) throws IOException
    {
        // use PDType0Font instead
        throw new UnsupportedOperationException();
    }
}
