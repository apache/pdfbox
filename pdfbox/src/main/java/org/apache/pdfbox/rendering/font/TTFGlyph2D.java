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
import java.util.HashMap;
import java.util.Map;

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

    private PDFont pdFont;
    private TrueTypeFont ttf;
    private String name;
    private float scale = 1.0f;
    private boolean hasScaling = false;
    private Map<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private boolean isCIDFont = false;

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

    public TTFGlyph2D(TrueTypeFont ttf, PDFont pdFont, boolean isCIDFont)
            throws IOException
    {
        this.pdFont = pdFont;
        this.ttf = ttf;
        // get units per em, which is used as scaling factor
        HeaderTable header = this.ttf.getHeader();
        if (header != null && header.getUnitsPerEm() != 1000)
        {
            // in most case the scaling factor is set to 1.0f
            // due to the fact that units per em is set to 1000
            scale = 1000f / header.getUnitsPerEm();
            hasScaling = true;
        }
        extractFontSpecifics(pdFont, isCIDFont);
    }

    /**
     * Extract all font specific information.
     * 
     * @param pdFont the given PDFont
     */
    private void extractFontSpecifics(PDFont pdFont, boolean isCIDFont)
    {
        name = pdFont.getBaseFont();
        this.isCIDFont = isCIDFont;
    }

    @Override
    public GeneralPath getPathForCharacterCode(int code) throws IOException
    {
        int gid = getGIDForCharacterCode(code);
        return getPathForGlyphId(gid);
    }

    // Try to map the given code to the corresponding glyph-ID
    private int getGIDForCharacterCode(int code) throws IOException
    {
        if (isCIDFont)
        {
            return ((PDType0Font)pdFont).codeToGID(code);
        }
        else
        {
            return ((PDTrueTypeFont)pdFont).codeToGID(code);
        }
    }

    /**
     * Returns the path describing the glyph for the given glyphId.
     *
     * @param glyphId the glyphId
     *
     * @return the GeneralPath for the given glyphId
     */
    public GeneralPath getPathForGlyphId(int glyphId) throws IOException
    {
        GeneralPath glyphPath;
        if (glyphs.containsKey(glyphId))
        {
            glyphPath = glyphs.get(glyphId);
        }
        else
        {
            // fixme: TrueTypeFont is buggy so we have to catch RuntimeException for debugging
            GlyphData[] glyphData;
            try
            {
                glyphData = ttf.getGlyph().getGlyphs();
            }
            catch (RuntimeException e)
            {
                LOG.error("Error in TTF: " + pdFont.getBaseFont() + " -> " +
                        ttf.getNaming().getPostScriptName());
                throw e;
            }

            if (glyphId >= glyphData.length)
            {
                LOG.warn(name + ": Glyph not found: " + glyphId);
                glyphPath = new GeneralPath();
                glyphs.put(glyphId, glyphPath);
            }
            else if (glyphData[glyphId] == null)
            {
                // empty glyph (e.g. space, newline)
                glyphPath = new GeneralPath();
                glyphs.put(glyphId, glyphPath);
            }
            else
            {
                GlyphData glyph = glyphData[glyphId];
                glyphPath = glyph.getPath();
                if (hasScaling)
                {
                    AffineTransform atScale = AffineTransform.getScaleInstance(scale, scale);
                    glyphPath.transform(atScale);
                }
                glyphs.put(glyphId, glyphPath);
            }
        }
        return glyphPath != null ? (GeneralPath) glyphPath.clone() : null; // todo: expensive
    }

    @Override
    public void dispose()
    {
        ttf = null;
        if (glyphs != null)
        {
            glyphs.clear();
        }
    }
}
