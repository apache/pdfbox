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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.Type1Container;


import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_TYPE1_DAMAGED;

public class Type1DescriptorHelper extends FontDescriptorHelper<Type1Container>
{
    public Type1DescriptorHelper(final PreflightContext context, final PDSimpleFont font, final Type1Container fontContainer)
    {
        super(context, font, fontContainer);
    }

    @Override
    protected boolean checkMandatoryFields(final COSDictionary fDescriptor)
    {
        boolean result = super.checkMandatoryFields(fDescriptor);
        /*
         * if this font is a subset, the CharSet entry must be present in the FontDescriptor
         */
        if (isSubSet(fontDescriptor.getFontName()))
        {
            final String charsetStr = fontDescriptor.getCharSet();
            if (charsetStr == null || charsetStr.isEmpty())
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET,
                        fontDescriptor.getFontName()
                        + ": The Charset entry is missing for the Type1 Subset"));
                result = false;
            }
        }
        return result;
    }

    @Override
    public PDStream extractFontFile(final PDFontDescriptor fontDescriptor)
    {
        final PDStream ff1 = fontDescriptor.getFontFile();
        final PDStream ff3 = fontDescriptor.getFontFile3();

        if (ff1 != null)
        {
            final COSStream stream = ff1.getCOSObject();
            if (stream == null)
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, 
                        fontDescriptor.getFontName() + ": The FontFile is missing"));
                this.fContainer.notEmbedded();
                return null;
            }

            final boolean hasLength1 = stream.getInt(COSName.LENGTH1) > 0;
            final boolean hasLength2 = stream.getInt(COSName.LENGTH2) > 0;
            final boolean hasLength3 = stream.getInt(COSName.LENGTH3) >= 0;
            if (!(hasLength1 && hasLength2 && hasLength3))
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, 
                        fontDescriptor.getFontName() + ": The FontFile is invalid"));
                return null;
            }

            return ff1;
        }
        else
        {
            return ff3;
        }
    }

    @Override
    protected void processFontFile(final PDFontDescriptor fontDescriptor, final PDStream fontFile)
    {
        if (font.isDamaged())
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_TYPE1_DAMAGED,
                    this.font.getName() + ": The FontFile can't be read"));
        }
    }
}
