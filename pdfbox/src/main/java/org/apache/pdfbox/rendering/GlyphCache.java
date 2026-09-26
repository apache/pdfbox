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
package org.apache.pdfbox.rendering;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.pdmodel.font.PDFontLike;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;

/**
 * A simple glyph outline cache.
 *
 * @author John Hewson
 */
final class GlyphCache
{
    private static final Logger LOG = LogManager.getLogger(GlyphCache.class);
    
    private final PDVectorFont font;
    private final Map<Integer, GeneralPath> cache = new HashMap<>();
    private final Map<Long, GeneralPath> hintedCache = new HashMap<>();

    GlyphCache(PDVectorFont font)
    {
        this.font = font;
    }

    /**
     * Returns the grid-fitted (hinted) glyph path for the given character code at the given ppem,
     * falling back to the unhinted path when the font does not hint that glyph/ppem. Results are
     * cached per {@code (code, ppem)}.
     *
     * @param code character code in a PDF
     * @param ppem the pixels-per-em the glyph will be rendered at
     * @return the hinted path if available, otherwise the unhinted path
     */
    public GeneralPath getPathForCharacterCode(int code, int ppem)
    {
        long key = ((long) ppem << 32) | (code & 0xFFFFFFFFL);
        GeneralPath cached = hintedCache.get(key);
        if (cached != null)
        {
            return cached;
        }
        GeneralPath path = null;
        try
        {
            path = font.getHintedNormalizedPath(code, ppem);
        }
        catch (IOException e)
        {
            String fontName = ((PDFontLike) font).getName();
            LOG.warn(() -> "Hinting failed for code " + code + " in font " + fontName, e);
        }
        // fall back to the unhinted path (itself cached by code); cache the decision per (code, ppem)
        GeneralPath result = path != null ? path : getPathForCharacterCode(code);
        hintedCache.put(key, result);
        return result;
    }
    
    public GeneralPath getPathForCharacterCode(int code)
    {
        GeneralPath path = cache.get(code);
        if (path != null)
        {
            return path;
        }

        try
        {
            if (!font.hasGlyph(code))
            {
                String fontName = ((PDFontLike) font).getName();
                if (font instanceof PDType0Font)
                {
                    int cid = ((PDType0Font) font).codeToCID(code);
                    String cidHex = String.format("%04x", cid);
                    LOG.warn("No glyph for code {} (CID {}) in font {}", code, cidHex, fontName);
                }
                else if (font instanceof PDSimpleFont)
                {
                    PDSimpleFont simpleFont = (PDSimpleFont) font;
                    LOG.warn("No glyph for code {} in {} {} (embedded or system font used: {})",
                            code, font.getClass().getSimpleName(), fontName,
                            simpleFont.getFontBoxFont().getName());
                    if (code == 10 && simpleFont.isStandard14())
                    {
                        // PDFBOX-4001 return empty path for line feed on std14
                        path = new GeneralPath();
                        cache.put(code, path);
                        return path;
                    }
                }
                else
                {
                    LOG.warn("No glyph for code {} in font {}", code, fontName);
                }
            }

            path = font.getNormalizedPath(code);
            cache.put(code, path);
            return path;
        }
        catch (IOException e)
        {
            // todo: escalate this error?
            String fontName = ((PDFontLike) font).getName();
            LOG.error(() -> "Glyph rendering failed for code " + code + " in font " + fontName, e);
            return new GeneralPath();
        }
    }
}
