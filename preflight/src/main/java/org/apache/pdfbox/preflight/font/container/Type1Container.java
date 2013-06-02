/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.font.container;

import java.io.IOException;
import java.util.List;

import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFFont.Mapping;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.font.util.GlyphException;
import org.apache.pdfbox.preflight.font.util.Type1;

public class Type1Container extends FontContainer
{
    /**
     * Represent the missingWidth value of the FontDescriptor dictionary. According to the PDF Reference, if this value
     * is missing, the default one is 0.
     */
    private float defaultGlyphWidth = 0;

    /**
     * true if information come from the FontFile1 Stream, false if they come from the FontFile3
     */
    protected boolean isFontFile1 = true;

    protected Type1 type1Font;
    protected List<CFFFont> lCFonts;

    public Type1Container(PDFont font)
    {
        super(font);
    }

    @Override
    protected float getFontProgramWidth(int cid) throws GlyphException
    {
        float widthResult = -1;
        try
        {
            if (isFontFile1)
            {
                if (type1Font != null)
                {
                    widthResult = this.type1Font.getWidthOfCID(cid);
                }
            }
            else
            {
                /*
                 * Retrieves the SID with the Character Name in the encoding map Need
                 * more PDF with a Type1C subfont to valid this implementation
                 */
                String name = null;
                if (this.font.getFontEncoding() != null) {
                    name = this.font.getFontEncoding().getName(cid);
                }

                int SID = -1;
                
                /* For each CFF, try to found the SID that correspond to the CID. 
                 * Look up by name if the encoding entry is present in the PDFont object 
                 * otherwise use the internal encoding map of the font.
                 */
                for (CFFFont cff : lCFonts)
                {
                    if (name == null) {
                        SID = cff.getEncoding().getSID(cid);
                    } else {
                        SID = getSIDByCharacterName(name, cff);
                    }

                    if (SID > 0) {
                        widthResult = cff.getWidth(SID);
                        if (widthResult != defaultGlyphWidth)
                        {
                            break;
                        }
                    }
                }

                if (SID < 0) 
                {
                    throw new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH_MISSING, cid, "Unknown character CID(" + cid+")");
                }
            }
        }
        catch (IOException e)
        {
            throw new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH, cid, "Unexpected error during the width validtion for the character CID(" + cid+") : " + e.getMessage());
        }

        return widthResult;
    }

    /**
     * Return the SID of the given character name.
     * 
     * @param name the character name looked up
     * @param cff Compact Font Format that represents a sub set of the Type1C Font.
     * @return -1 if the name is missing from the Font encoding map, the SID of the character if it is present in the CFF.
     */
    private int getSIDByCharacterName(String name, CFFFont cff)
    {
        int SID = -1;
        for (Mapping m : cff.getMappings())
        {
            if (m.getName().equals(name))
            {
                SID = m.getSID();
                break;
            }
        }
        return SID;
    }

    public void setType1Font(Type1 type1Font)
    {
        this.type1Font = type1Font;
    }

    public void setFontFile1(boolean isFontFile1)
    {
        this.isFontFile1 = isFontFile1;
    }

    public void setCFFFontObjects(List<CFFFont> lCFonts)
    {
        this.lCFonts = lCFonts;
    }
}
