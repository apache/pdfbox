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

package org.apache.fontbox.ttf;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.apache.fontbox.ttf.advanced.GlyphVectorAdvanced;
import org.apache.fontbox.ttf.advanced.GlyphVectorSimple;
import org.apache.fontbox.ttf.advanced.util.GlyphSequence;

/**
 * An OpenType (OTF/TTF) font.
 */
public class OpenTypeFont extends TrueTypeFont
{
    private boolean isPostScript;
    
    /**
     * Constructor. Clients should use the OTFParser to create a new OpenTypeFont object.
     *
     * @param fontData The font data.
     */
    OpenTypeFont(TTFDataStream fontData)
    {
        this(fontData, false);
    }

    /**
     * Constructor. Clients should use the OTFParser to create a new OpenTypeFont object.
     *
     * @param fontData The font data.
     * @param useAlternateATT true if using alternate ATT (advanced typograph tables) implementation.
     */
    OpenTypeFont(TTFDataStream fontData, boolean useAlternateATT)
    {
        super(fontData, useAlternateATT);
    }

    @Override
    void setVersion(float versionValue)
    {
        isPostScript = Float.floatToIntBits(versionValue) == 0x469EA8A9; // OTTO
        super.setVersion(versionValue);
    }
    
    /**
     * Get the "CFF" table for this OTF.
     *
     * @return The "CFF" table.
     */
    public CFFTable getCFF() throws IOException
    {
        if (!isPostScript)
        {
            throw new UnsupportedOperationException("TTF fonts do not have a CFF table");
        }
        return (CFFTable) getTable(CFFTable.TAG);
    }

    @Override
    public GlyphTable getGlyph() throws IOException
    {
        if (isPostScript)
        {
            throw new UnsupportedOperationException("OTF fonts do not have a glyf table");
        }
        return super.getGlyph();
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        int gid = nameToGID(name);
        return getCFF().getFont().getType2CharString(gid).getPath();
    }

    /**
     * Returns true if this font is a PostScript outline font.
     */
    public boolean isPostScript()
    {
        return tables.containsKey(CFFTable.TAG);
    }

    /**
     * Returns true if this font uses OpenType Layout (Advanced Typographic) tables.
     */
    public boolean hasLayoutTables()
    {
        return tables.containsKey("BASE") ||
               tables.containsKey("GDEF") ||
               tables.containsKey("GPOS") ||
               tables.containsKey("GSUB") ||
               tables.containsKey("JSTF");
    }

    /**
     * Get the "GDEF" table for this OTF.
     *
     * @return The "GDEF" table.
     */
    public org.apache.fontbox.ttf.advanced.GlyphDefinitionTable getGDEF() throws IOException
    {
        if (useAlternateATT)
            return (org.apache.fontbox.ttf.advanced.GlyphDefinitionTable) getTable(org.apache.fontbox.ttf.advanced.GlyphDefinitionTable.TAG);
        else
            return null;
    }

    /**
     * Get the "GSUB" table for this OTF.
     *
     * @return The "GSUB" table.
     */
    public org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable getGSUB() throws IOException
    {
        if (useAlternateATT)
            return (org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable) getTable(org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable.TAG);
        else
            return null;
    }

    /**
     * Get the "GPOS" table for this OTF.
     *
     * @return The "GPOS" table.
     */
    public org.apache.fontbox.ttf.advanced.GlyphPositioningTable getGPOS() throws IOException
    {
        if (useAlternateATT)
            return (org.apache.fontbox.ttf.advanced.GlyphPositioningTable) getTable(org.apache.fontbox.ttf.advanced.GlyphPositioningTable.TAG);
        else
            return null;
    }

    /**
     * TODO
     */
    private float getNormalizedWidth(float width) throws IOException {
        float unitsPerEM = getUnitsPerEm();
        if (Float.compare(unitsPerEM, 1000) != 0)
        {
            width *= 1000f / unitsPerEM;
        }
        return width;
    }

    /** TODO */
    public GlyphVector createGlyphVector(String text) throws IOException 
    {
        if (text.isEmpty()) {
            return new GlyphVectorSimple(null);
        } else if (!useAlternateATT) {
            return new GlyphVectorSimple(null); // TODO
        }

        int[] codePoints = text.codePoints().toArray();
        int[] originalGlyphs = new int[codePoints.length];

        CmapLookup cmapLookup = getUnicodeCmapLookup();

        for (int i = 0; i < codePoints.length; i++) {
            originalGlyphs[i] = cmapLookup.getGlyphId(codePoints[i]);
        }

        IntBuffer characters = IntBuffer.wrap(codePoints);
        IntBuffer glyphs = IntBuffer.wrap(originalGlyphs);

        GlyphSequence sequence = new GlyphSequence(characters, glyphs, null);

        org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable substitutionTable =
          (org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable) getGSUB();

        org.apache.fontbox.ttf.advanced.GlyphPositioningTable positioningTable =
          (org.apache.fontbox.ttf.advanced.GlyphPositioningTable) getGPOS();

        // TODO: Correct script and language
        GlyphSequence substituted = substitutionTable != null ? 
             substitutionTable.substitute(sequence, "latn", "en", null) : sequence;

        int[][] adjustments = new int[substituted.getGlyphCount()][4];
        int[] widths = new int[substituted.getGlyphCount()];
        int[] workingGlyphs = substituted.getGlyphArray(false);

        // for (int i = 0; i < widths.length; i++) {
        //     widths[i] = (int) (getAdvanceWidth(workingGlyphs[i]) * 20);
        // }

        // TODO: Correct script, language and font size
        // TODO: widths?
        boolean positioned = positioningTable.position(
            substituted, "latn", "en", null, 20, widths, adjustments);
//System.out.println(Arrays.toString(widths));
        int[] outGlyphs = substituted.getGlyphArray(true);

        float width = 0f;
        for (int i = 0; i < outGlyphs.length; i++) {
            int[] glyphAdjust = adjustments[i];

            int placementX = glyphAdjust[0];
            int placementY = glyphAdjust[1];
            int advanceX = glyphAdjust[2];
            int advanceY = glyphAdjust[3];

            int xAdjust = 0;
            if (placementX != 0 || advanceX != 0) {
                xAdjust = advanceX + (placementX);
            }

            // System.out.println("xAdjust = " + Arrays.toString(glyphAdjust));
            // System.out.println("advance = " + getAdvanceWidth(outGlyphs[i]));

            // TODO: Something with yAdjust
            // TODO: Debug width

            width += getNormalizedWidth(getAdvanceWidth(outGlyphs[i]) + (xAdjust));
        }

        //System.out.println("w = " + width);

        return new GlyphVectorAdvanced(outGlyphs, width, positioned ? adjustments : null);
    }

}
