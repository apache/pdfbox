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
     * @param ttfStream TTF stream
     * @param encoding The PostScript encoding vector to be used for embedding.
     * @throws IOException if the TTF could not be read
     */
    PDTrueTypeFontEmbedder(PDDocument document, COSDictionary dict, InputStream ttfStream,
                           Encoding encoding) throws IOException
    {
        super(document, dict, ttfStream, false);
        dict.setItem(COSName.SUBTYPE, COSName.TRUE_TYPE);
        
        GlyphList glyphList = GlyphList.getAdobeGlyphList();
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
    private void setWidths(COSDictionary font, GlyphList glyphList) throws IOException
    {
        float scaling = 1000f / ttf.getHeader().getUnitsPerEm();
        HorizontalMetricsTable hmtx = ttf.getHorizontalMetrics();

        Map<Integer, String> codeToName = getFontEncoding().getCodeToNameMap();

        int firstChar = Collections.min(codeToName.keySet());
        int lastChar = Collections.max(codeToName.keySet());

        List<Integer> widths = new ArrayList<Integer>(lastChar - firstChar + 1);
        for (int i = 0; i < lastChar - firstChar + 1; i++)
        {
            widths.add(0);
        }

        // a character code is mapped to a glyph name via the provided font encoding
        // afterwards, the glyph name is translated to a glyph ID.
        for (Map.Entry<Integer, String> entry : codeToName.entrySet())
        {
            int code = entry.getKey();
            String name = entry.getValue();

            if (code >= firstChar && code <= lastChar)
            {
                String uni = glyphList.toUnicode(name);
                int charCode = uni.codePointAt(0);
                int gid = cmap.getGlyphId(charCode);
                widths.set(entry.getKey() - firstChar,
                           Math.round(hmtx.getAdvanceWidth(gid) * scaling));
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

    @Override
    protected void buildSubset(InputStream ttfSubset, String tag,
                            Map<Integer, Integer> gidToCid) throws IOException
    {
        // use PDType0Font instead
        throw new UnsupportedOperationException();
    }
}
