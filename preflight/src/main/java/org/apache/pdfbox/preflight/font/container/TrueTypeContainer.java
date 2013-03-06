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
import java.util.List;

import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class TrueTypeContainer extends FontContainer
{

    protected TrueTypeFont ttFont;

    private CMAPEncodingEntry[] cmapEncodingEntries = null;

    public TrueTypeContainer(PDFont font)
    {
        super(font);
    }

    public void setTrueTypeFont(TrueTypeFont ttFont)
    {
        this.ttFont = ttFont;
        initCMapEncodingEntries(); // TODO appel sur le checkWidth
    }

    /**
     * Initialize the {@linkplain #cmapEncodingEntries} with CMaps that belong to the TrueType Font Program.
     * 
     * Here the selection rules :
     * <UL>
     * <li>For a Symbolic TrueType, the Font Program has only one CMap (Checked in the checkFontFileElement method)
     * <li>For a Non-Symbolic TrueType, the list of CMap is reordered to provide WinAnsi CMap first (plateformId : 3 /
     * encodingId : 1) followed by MacRoman CMap (plateformId : 1 / encodingId : 0). This CMap returns the CMap which
     * corresponds to the Encoding value of the FontDescriptor dictionary.
     * </UL>
     */
    protected void initCMapEncodingEntries()
    {
        if (this.cmapEncodingEntries != null)
            return;

        CMAPTable cmap = this.ttFont.getCMAP();
        if (this.font.getFontDescriptor().isSymbolic())
        {
            this.cmapEncodingEntries = cmap.getCmaps();
        }
        else
        {
            this.cmapEncodingEntries = orderCMapEntries(cmap);
        }
    }

    private CMAPEncodingEntry[] orderCMapEntries(CMAPTable cmap)
    {
        List<CMAPEncodingEntry> res = new ArrayList<CMAPEncodingEntry>();
        boolean firstIs31 = false;
        for (CMAPEncodingEntry cmapEntry : cmap.getCmaps())
        {
            // WinAnsi
            if ((cmapEntry.getPlatformId() == 3) && (cmapEntry.getPlatformEncodingId() == 1))
            {
                res.add(0, cmapEntry);
                firstIs31 = true;
            }
            else if ((cmapEntry.getPlatformId() == 1) && (cmapEntry.getPlatformEncodingId() == 0))
            {
                // MacRoman
                if (firstIs31)
                {
                    // WinAnsi is present, MacRoman is set in second position
                    res.add(1, cmapEntry);
                }
                else
                {
                    // WinAnsi is missing, MacRoman has the priority
                    res.add(0, cmapEntry);
                }
            }
            else
            {
                res.add(cmapEntry);
            }
        }
        return res.toArray(new CMAPEncodingEntry[res.size()]);
    }

    @Override
    protected float getFontProgramWidth(int cid)
    {
        float result = -1f;
        if (cmapEncodingEntries != null)
        {
            for (CMAPEncodingEntry entry : cmapEncodingEntries)
            {
                int glyphID = extractGlyphID(cid, entry);
                if (glyphID > 0)
                {
                    result = extractGlyphWidth(glyphID);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * TrueType has internal CMap that map the CID used in the PDF file with an internal character identifier. This
     * method converts the given CID in the internal font program identifier. (0 if no match found)
     * 
     * @param cid
     * @param cmap
     * @return
     */
    private int extractGlyphID(int cid, CMAPEncodingEntry cmap)
    {
        int notFoundGlyphID = 0;

        int innerFontCid = cid;
        if (cmap.getPlatformEncodingId() == 1 && cmap.getPlatformId() == 3)
        {
            try
            {
                Encoding fontEncoding = this.font.getFontEncoding();
                String character = fontEncoding.getCharacter(cid);
                if (character == null)
                {
                    return notFoundGlyphID;
                }

                char[] characterArray = character.toCharArray();
                if (characterArray.length == 1)
                {
                    innerFontCid = (int) characterArray[0];
                }
                else
                {
                    // TODO OD-PDFA-87 A faire?
                    innerFontCid = (int) characterArray[0];
                    for (int i = 1; i < characterArray.length; ++i)
                    {
                        if (cmap.getGlyphId((int) characterArray[i]) == 0)
                        {
                            return notFoundGlyphID; // TODO what we have to do here ???
                        }
                    }
                }
            }
            catch (IOException ioe)
            {
                // should never happen
                return notFoundGlyphID;
            }
        }

        // search glyph
        return cmap.getGlyphId(innerFontCid);
    }

    private float extractGlyphWidth(int glyphID)
    {
        int unitsPerEm = this.ttFont.getHeader().getUnitsPerEm();
        int[] glyphWidths = this.ttFont.getHorizontalMetrics().getAdvanceWidth();
        /*
         * In a Mono space font program, the length of the AdvanceWidth array must be one. According to the TrueType
         * font specification, the Last Value of the AdvanceWidth array is apply to the subsequent glyphs. So if the
         * GlyphId is greater than the length of the array the last entry is used.
         */
        int numberOfLongHorMetrics = this.ttFont.getHorizontalHeader().getNumberOfHMetrics();
        float glypdWidth = glyphWidths[numberOfLongHorMetrics - 1];
        if (glyphID < numberOfLongHorMetrics)
        {
            glypdWidth = glyphWidths[glyphID];
        }
        return ((glypdWidth * 1000) / unitsPerEm);
    }
}
