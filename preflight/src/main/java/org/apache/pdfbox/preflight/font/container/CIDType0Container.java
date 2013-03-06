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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFFont.Mapping;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class CIDType0Container extends FontContainer
{
    protected List<CFFFont> lCFonts = new ArrayList<CFFFont>();

    public CIDType0Container(PDFont font)
    {
        super(font);
    }

    @Override
    protected float getFontProgramWidth(int cid)
    {
        // build the font container and keep it in the Handler.
        boolean cidFound = false;
        for (CFFFont font : this.lCFonts)
        {
            Collection<Mapping> cMapping = font.getMappings();
            for (Mapping mapping : cMapping)
            {
                /*
                 * REMARK : May be this code must be changed like the Type1FontContainer to Map the SID with the
                 * character name? Not enough PDF with this kind of Font to test the current implementation
                 */
                if (mapping.getSID() == cid)
                {
                    cidFound = true;
                    break;
                }
            }
            if (cidFound)
            {
                break;
            }
        }

        float widthInFontProgram = 0;
        if (cidFound || cid == 0)
        {

            float defaultGlyphWidth = 0;
            if (this.font.getFontDescriptor() != null)
            {
                defaultGlyphWidth = this.font.getFontDescriptor().getMissingWidth();
            }

            try
            {
                // Search the CID in all CFFFont in the FontProgram
                for (CFFFont cff : this.lCFonts)
                {
                    widthInFontProgram = cff.getWidth(cid);
                    if (widthInFontProgram != defaultGlyphWidth)
                    {
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                widthInFontProgram = -1;
            }
        }
        else
        {
            /*
             * Cid 0 is commonly used as the NotDef Glyph. this glyph can be used as Space. IN PDF/A-1 the Notdef glyph
             * can be used as space. Not in PDF/A-2
             */
            widthInFontProgram = -1;
        }
        return widthInFontProgram;
    }

    public void setlCFonts(List<CFFFont> lCFonts)
    {
        this.lCFonts = lCFonts;
    }

}
