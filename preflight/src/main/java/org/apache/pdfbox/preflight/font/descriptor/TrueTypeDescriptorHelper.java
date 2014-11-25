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

package org.apache.pdfbox.preflight.font.descriptor;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_ENCODING;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_TRUETYPE_DAMAGED;

import java.io.IOException;

import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.TrueTypeContainer;

public class TrueTypeDescriptorHelper extends FontDescriptorHelper<TrueTypeContainer>
{
    private final PDTrueTypeFont pdTrueTypeFont;

    public TrueTypeDescriptorHelper(PreflightContext context, PDTrueTypeFont font, TrueTypeContainer fontContainer)
    {
        super(context, font, fontContainer);
        pdTrueTypeFont = font;
    }

    @Override
    public PDStream extractFontFile(PDFontDescriptor fontDescriptor)
    {
        PDStream fontFile = fontDescriptor.getFontFile2();
        COSStream stream = (fontFile == null ? null : fontFile.getStream());
        if (stream == null)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, 
                    fontDescriptor.getFontName() + ": The FontFile2 is missing"));
            this.fContainer.notEmbedded();
            return null;
        }

        if (stream.getInt(COSName.LENGTH1) <= 0)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
                    fontDescriptor.getFontName() + ": The FontFile entry /Length1 is invalid"));
            return null;
        }

        return fontFile;
    }

    @Override
    protected void processFontFile(PDFontDescriptor fontDescriptor, PDStream fontFile)
    {
        if (font.isDamaged())
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_TRUETYPE_DAMAGED,
                    this.font.getName() + ": The FontFile can't be read"));
        }
        else
        {
            // there must be exactly one encoding in the "cmap" table if the font is symbolic
            TrueTypeFont ttf = pdTrueTypeFont.getTrueTypeFont();
            try
            {
                if (pdTrueTypeFont.isSymbolic() && ttf.getCmap().getCmaps().length != 1)
                {
                    this.fContainer.push(new ValidationError(ERROR_FONTS_ENCODING,
                            this.font.getName() + ": Symbolic TrueType font has more than one 'cmap' entry"));
                }
            }
            catch (IOException e)
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_TRUETYPE_DAMAGED,
                         this.font.getName() + ": The TTF 'cmap' could not be read"));
            }
        }
    }
}
