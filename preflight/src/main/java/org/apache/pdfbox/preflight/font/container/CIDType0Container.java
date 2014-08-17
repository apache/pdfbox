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

import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
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
        CFFCIDFont cffFont = (CFFCIDFont)lCFonts.get(0);
        try
        {
            // fixme: this does not take into account the PDF's CMap or the FontDescriptor's default width
            return cffFont.getType2CharString(cid).getWidth();
        }
        catch (IOException e)
        {
            return -1;
        }
    }

    public void setlCFonts(List<CFFFont> lCFonts)
    {
        this.lCFonts = lCFonts;
    }

}
