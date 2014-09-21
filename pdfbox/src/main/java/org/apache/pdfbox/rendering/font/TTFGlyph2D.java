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
package org.apache.pdfbox.rendering.font;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * This class provides a glyph to GeneralPath conversion for TrueType fonts.
 */
public class TTFGlyph2D implements Glyph2D
{
    private static final Log LOG = LogFactory.getLog(TTFGlyph2D.class);

    private final PDFont font;
    private final TrueTypeFont ttf;
    private float scale = 1.0f;
    private boolean hasScaling;
    private final Map<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private final boolean isCIDFont;

    /**
     * Constructor.
     *
     * @param ttfFont TrueType font
     */
    public TTFGlyph2D(PDTrueTypeFont ttfFont) throws IOException
    {
        this(ttfFont.getTrueTypeFont(), ttfFont, false);
    }

    /**
     * Constructor.
     *
     * @param type0Font Type0 font, with CIDFontType2 descendant
     */
    public TTFGlyph2D(PDType0Font type0Font) throws IOException
    {
        this(((PDCIDFontType2)type0Font.getDescendantFont()).getTrueTypeFont(), type0Font, true);
    }

    public TTFGlyph2D(TrueTypeFont ttf, PDFont font, boolean isCIDFont)
            throws IOException
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

    // todo: HACK!
    private static Set<String> STANDARD_14 = new HashSet<String>();
    static
    {
        // standard 14 names
        STANDARD_14.addAll(Arrays.asList(
                "Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique", "Helvetica",
                "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique", "Times-Roman",
                "Times-Bold", "Times-Italic", "Times-BoldItalic", "Symbol", "ZapfDingbats"
        ));
        // alternative names from Adobe Supplement to the ISO 32000
        STANDARD_14.addAll(Arrays.asList(
                "CourierCourierNew", "CourierNew", "CourierNew,Italic", "CourierNew,Bold",
                "CourierNew,BoldItalic", "Arial", "Arial,Italic", "Arial,Bold", "Arial,BoldItalic",
                "TimesNewRoman", "TimesNewRoman,Italic", "TimesNewRoman,Bold", "TimesNewRoman,BoldItalic"
        ));
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

            // ------

            GlyphData glyph = ttf.getGlyph().getGlyph(gid);

            // todo: MEGA HACK! (for CIDFont "known") - sort of works (width issues?)
            if (gid == 0 && !font.isEmbedded() && STANDARD_14.contains(font.getName()))
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
                glyphPath = glyph.getPath();
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
