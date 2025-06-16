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

/**
 * An OpenType (OTF/TTF) font.
 */
public class OpenTypeFont extends TrueTypeFont
{
    // indicates whether the version info identifies this font as PostScriptFont
    private boolean hasPostScriptTag;
    
    /**
     * Constructor. Clients should use the OTFParser to create a new OpenTypeFont object.
     *
     * @param fontData The font data.
     */
    OpenTypeFont(TTFDataStream fontData)
    {
        super(fontData);
    }

    @Override
    void setVersion(float versionValue)
    {
        hasPostScriptTag = Float.floatToIntBits(versionValue) == 0x469EA8A9; // OTTO
        super.setVersion(versionValue);
    }
    
    /**
     * Get the "CFF" table for this OTF.
     *
     * @return The "CFF" table.
     * 
     * @throws IOException if the font data could not be read
     * @throws UnsupportedOperationException if the current font isn't a CFF font
     */
    public CFFTable getCFF() throws IOException
    {
        if (!hasPostScriptTag)
        {
            throw new UnsupportedOperationException("TTF fonts do not have a CFF table");
        }
        return (CFFTable) getTable(CFFTable.TAG);
    }

    @Override
    public GlyphTable getGlyph() throws IOException
    {
        if (hasPostScriptTag)
        {
            throw new UnsupportedOperationException("OTF fonts do not have a glyf table");
        }
        return super.getGlyph();
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        if (hasPostScriptTag && isSupportedOTF())
        {
            int gid = nameToGID(name);
            return getCFF().getFont().getType2CharString(gid).getPath();
        }
        else
        {
            return super.getPath(name);
        }
    }

    /**
     * Returns true if this font is a PostScript outline font.
     * 
     * @return true if the font is a PostScript outline font, otherwise false
     */
    public boolean isPostScript()
    {
        return hasPostScriptTag || tables.containsKey(CFFTable.TAG) || tables.containsKey("CFF2");
    }

    /**
     * Returns true if this font is supported.
     * 
     * There are 3 kind of OpenType fonts, fonts using TrueType outlines, fonts using CFF outlines (version 1 and 2)
     * 
     * Fonts using CFF outlines version 2 aren't supported yet.
     * 
     * @return true if the font is supported
     */
    public boolean isSupportedOTF()
    {
        // OTF using CFF2 based outlines aren't yet supported
        return !(hasPostScriptTag //
                && !tables.containsKey(CFFTable.TAG) //
                && tables.containsKey("CFF2") //
        );
    }

    /**
     * Returns true if this font uses OpenType Layout (Advanced Typographic) tables.
     * 
     * @return true if the font has any layout table, otherwise false
     */
    public boolean hasLayoutTables()
    {
        return tables.containsKey("BASE") //
                || tables.containsKey("GDEF") //
                || tables.containsKey("GPOS") //
                || tables.containsKey(GlyphSubstitutionTable.TAG) //
                || tables.containsKey(OTLTable.TAG);
    }
}
