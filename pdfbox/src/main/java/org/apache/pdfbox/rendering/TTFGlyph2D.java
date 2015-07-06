/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.pdfbox.rendering;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;

/**
 * This class provides a glyph to GeneralPath conversion for TrueType and OpenType fonts.
 */
final class TTFGlyph2D implements Glyph2D
{
    private static final Log LOG = LogFactory.getLog(TTFGlyph2D.class);

    private final PDFont font;
    private final TrueTypeFont ttf;
    private PDVectorFont vectorFont;
    private float scale = 1.0f;
    private boolean hasScaling;
    private final Map<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private final boolean isCIDFont;

    /**
     * Constructor.
     *
     * @param ttfFont TrueType font
     */
    TTFGlyph2D(PDTrueTypeFont ttfFont) throws IOException
    {
        this(ttfFont.getTrueTypeFont(), ttfFont, false);
        vectorFont = ttfFont;
    }

    /**
     * Constructor.
     *
     * @param type0Font Type0 font, with CIDFontType2 descendant
     */
    TTFGlyph2D(PDType0Font type0Font) throws IOException
    {
        this(((PDCIDFontType2)type0Font.getDescendantFont()).getTrueTypeFont(), type0Font, true);
        vectorFont = type0Font;
    }

    private TTFGlyph2D(TrueTypeFont ttf, PDFont font, boolean isCIDFont) throws IOException
    {
        this.font = font;
        this.ttf = ttf;
        this.isCIDFont = isCIDFont;
        // get units per em, which is used as scaling factor
        HeaderTable header = this.ttf.getHeader();
        if (header != null && header.getUnitsPerEm() != 1000)
        {
            // in most case the scaling factor is set to 1.0f
            // due to the fact that units per em is set to 1000
            scale = 1000f / header.getUnitsPerEm();
            hasScaling = true;
        }
    }

    @Override
    public GeneralPath getPathForCharacterCode(int code) throws IOException
    {
        int gid = getGIDForCharacterCode(code);
        return getPathForGID(gid, code);
    }

    // Try to map the given code to the corresponding glyph-ID
    private int getGIDForCharacterCode(int code) throws IOException
    {
        if (isCIDFont)
        {
            return ((PDType0Font)font).codeToGID(code);
        }
        else
        {
            return ((PDTrueTypeFont)font).codeToGID(code);
        }
    }

    /**
     * Returns the path describing the glyph for the given glyphId.
     *
     * @param gid the GID
     * @param code the character code
     *
     * @return the GeneralPath for the given glyphId
     */
    public GeneralPath getPathForGID(int gid, int code) throws IOException
    {
        GeneralPath glyphPath;
        if (glyphs.containsKey(gid))
        {
            glyphPath = glyphs.get(gid);
        }
        else
        {
            if (gid == 0 || gid >= ttf.getMaximumProfile().getNumGlyphs())
            {
                if (isCIDFont)
                {
                    int cid = ((PDType0Font) font).codeToCID(code);
                    String cidHex = String.format("%04x", cid);
                    LOG.warn("No glyph for " + code + " (CID " + cidHex + ") in font " +
                            font.getName());
                }
                else
                {
                    LOG.warn("No glyph for " + code + " in font " + font.getName());
                }
            }
            
            GeneralPath glyph = vectorFont.getPath(code);

            // Acrobat only draws GID 0 for embedded or "Standard 14" fonts, see PDFBOX-2372
            if (gid == 0 && !font.isEmbedded() && !font.isStandard14())
            {
                glyph = null;
            }

            if (glyph == null)
            {
                // empty glyph (e.g. space, newline)
                glyphPath = new GeneralPath();
                glyphs.put(gid, glyphPath);
            }
            else
            {
                glyphPath = glyph;
                if (hasScaling)
                {
                    AffineTransform atScale = AffineTransform.getScaleInstance(scale, scale);
                    glyphPath.transform(atScale);
                }
                glyphs.put(gid, glyphPath);
            }
        }
        return glyphPath != null ? (GeneralPath) glyphPath.clone() : null; // todo: expensive
    }

    @Override
    public void dispose()
    {
        glyphs.clear();
    }
}
