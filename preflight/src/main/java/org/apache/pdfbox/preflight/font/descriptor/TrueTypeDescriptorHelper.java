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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.TrueTypeContainer;

public class TrueTypeDescriptorHelper extends FontDescriptorHelper<TrueTypeContainer>
{

    public TrueTypeDescriptorHelper(PreflightContext context, PDFont font, TrueTypeContainer fontContainer)
    {
        super(context, font, fontContainer);
    }

    public PDStream extractFontFile(PDFontDescriptorDictionary fontDescriptor)
    {
        PDStream fontFile = fontDescriptor.getFontFile2();
        COSStream stream = (fontFile == null ? null : fontFile.getStream());
        if (stream == null)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile2 is missing for "
                    + fontDescriptor.getFontName()));
            this.fContainer.notEmbedded();
            return null;
        }

        if (stream.getInt(COSName.LENGTH1) <= 0)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
                    "The FontFile entry /Length1 is invalid for " + fontDescriptor.getFontName()));
            return null;
        }

        return fontFile;
    }

    protected void processFontFile(PDFontDescriptorDictionary fontDescriptor, PDStream fontFile)
    {
        /*
         * Try to load the font using the TTFParser object. If the font is invalid, an exception will be thrown. Because
         * of it is a Embedded Font Program, some tables are required and other are optional see PDF Reference (§5.8)
         */
        ByteArrayInputStream bis = null;
        try
        {

            bis = new ByteArrayInputStream(fontFile.getByteArray());
            TrueTypeFont ttf = new TTFParser(true).parseTTF(bis);

            if (fontDescriptor.isSymbolic() && ttf.getCMAP().getCmaps().length != 1)
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_ENCODING,
                        "The Encoding should be missing for the Symbolic TTF"));
            }
            else
            {
                ((TrueTypeContainer) this.fContainer).setTrueTypeFont(ttf);
                // TODO check the WIdth consistency too
            }
        }
        catch (IOException e)
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_TRUETYPE_DAMAGED, "The FontFile can't be read for "
                    + this.font.getBaseFont()));
        }
        finally
        {
            IOUtils.closeQuietly(bis);
        }
    }
}
