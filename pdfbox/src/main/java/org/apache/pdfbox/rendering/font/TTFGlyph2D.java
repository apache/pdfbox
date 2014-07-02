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
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2Font;
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
    private PDCIDFontType2Font descendantFont;
    private String name;
    private float scale = 1.0f;
    private boolean hasScaling = false;
    private Map<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private CMap fontCMap = null;
    private boolean isCIDFont = false;
    private boolean hasIdentityCIDMapping = false;
    private boolean hasCID2GIDMapping = false;
    private boolean hasTwoByteMappings = false;

    /**
     * Constructor.
     *
     * @param ttfFont TrueType font
     */
    public TTFGlyph2D(PDTrueTypeFont ttfFont) throws IOException
    {
        this(ttfFont.getTTFFont(), ttfFont, null);
    }

    /**
     * Constructor.
     *
     * @param type0Font Type0 font, with CIDFontType2 descendant
     */
    public TTFGlyph2D(PDType0Font type0Font) throws IOException
    {
        this(((PDCIDFontType2Font)type0Font.getDescendantFont()).getTTFFont(), type0Font,
                (PDCIDFontType2Font)type0Font.getDescendantFont());
    }

    public TTFGlyph2D(TrueTypeFont ttf, PDFont pdFont, PDCIDFontType2Font descFont)
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
        extractFontSpecifics(pdFont, descFont);
    }

    /**
     * Extract all font specific information.
     * 
     * @param pdFont the given PDFont
     */
    private void extractFontSpecifics(PDFont pdFont, PDCIDFontType2Font descFont)
    {
        name = pdFont.getBaseFont();
        if (descFont != null)
        {
            isCIDFont = true;
            descendantFont = descFont;
            hasIdentityCIDMapping = descendantFont.hasIdentityCIDToGIDMap();
            hasCID2GIDMapping = descendantFont.hasCIDToGIDMap();
            fontCMap = pdFont.getCMap();
            if (fontCMap != null)
            {
                hasTwoByteMappings = fontCMap.hasTwoByteMappings();
            }
        }
    }

    /**
     * Get the GID for the given CIDFont.
     * 
     * @param code the given CID
     * @return the mapped GID
     */
    private int getGID(int code)
    {
        if (hasIdentityCIDMapping)
        {
            // identity mapping
            return code;
        }
        if (hasCID2GIDMapping)
        {
            // use the provided CID2GID mapping
            return descendantFont.mapCIDToGID(code);
        }
        if (fontCMap != null)
        {
            String string = fontCMap.lookup(code, hasTwoByteMappings ? 2 : 1);
            if (string != null)
            {
                return string.codePointAt(0);
            }
        }
        return code;
    }

    @Override
    public GeneralPath getPathForCharacterCode(int code)
    {
        int glyphId = getGIDForCharacterCode(code);

        if (glyphId > 0)
        {
            return getPathForGlyphId(glyphId);
        }
        glyphId = code;
        // there isn't any mapping, but probably an optional CMap
        if (fontCMap != null)
        {
            String string = fontCMap.lookup(code, hasTwoByteMappings ? 2 : 1);
            if (string != null)
            {
                glyphId = string.codePointAt(0);
            }
        }
        return getPathForGlyphId(glyphId);
    }

    // Try to map the given code to the corresponding glyph-ID
    private int getGIDForCharacterCode(int code)
    {
        if (isCIDFont)
        {
            return getGID(code);
        }
        else
        {
            return ((PDTrueTypeFont)pdFont).getGIDForCharacterCode(code);
        }
    }

    /**
     * Returns the path describing the glyph for the given glyphId.
     *
     * @param glyphId the glyphId
     *
     * @return the GeneralPath for the given glyphId
     */
    public GeneralPath getPathForGlyphId(int glyphId)
    {
        GeneralPath glyphPath = null;
        if (glyphs.containsKey(glyphId))
        {
            glyphPath = glyphs.get(glyphId);
        }
        else
        {
            GlyphData[] glyphData = ttf.getGlyph().getGlyphs();
            if (glyphId < glyphData.length && glyphData[glyphId] != null)
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
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug(name + ": Glyph not found:" + glyphId);
                }
            }
        }
        return glyphPath != null ? (GeneralPath) glyphPath.clone() : null;
    }

    @Override
    public void dispose()
    {
        ttf = null;
        descendantFont = null;
        fontCMap = null;
        if (glyphs != null)
        {
            glyphs.clear();
        }
    }
}
