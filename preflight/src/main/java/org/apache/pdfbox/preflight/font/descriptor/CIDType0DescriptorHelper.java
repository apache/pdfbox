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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CID_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_CIDSET;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_TYPE0C;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_TYPE1C;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontLike;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.FontValidator;
import org.apache.pdfbox.preflight.font.container.CIDType0Container;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class CIDType0DescriptorHelper extends FontDescriptorHelper<CIDType0Container>
{
    public CIDType0DescriptorHelper(PreflightContext context, PDFontLike font, CIDType0Container fontContainer)
    {
        super(context, font, fontContainer);
    }

    @Override
    public PDStream extractFontFile(PDFontDescriptor fontDescriptor)
    {
        PDStream ff3 = fontDescriptor.getFontFile3();
        if (ff3 != null)
        {
            // Stream validation should be done by the StreamValidateHelper. Process font specific check
            COSStream stream = ff3.getStream();
            if (stream == null)
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, 
                        fontDescriptor.getFontName() + ": The FontFile is missing"));
                this.fContainer.notEmbedded();
            }
            else
            {
                // Length1/2/3 aren't mandatory for this type of font
                // But the Subtype is a mandatory field with specific values
                String st = stream.getNameAsString(COSName.SUBTYPE);
                if (!(FONT_DICTIONARY_VALUE_TYPE0C.equals(st) || FONT_DICTIONARY_VALUE_TYPE1C.equals(st)))
                {
                    this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
                            fontDescriptor.getFontName()
                            + ": The FontFile3 stream doesn't have the right Subtype"));
                }

                checkCIDSet(fontDescriptor);
            }
        }
        return ff3;
    }

    /**
     * If the embedded font is a subset, the CIDSet entry is mandatory and must be a Stream. If the CIDSet entry doesn't
     * respects conditions, the FontContainer is updated.
     * 
     * @param pfDescriptor
     */
    protected void checkCIDSet(PDFontDescriptor pfDescriptor)
    {
        if (FontValidator.isSubSet(pfDescriptor.getFontName()))
        {
            COSDocument cosDocument = context.getDocument().getDocument();
            COSBase cidset = pfDescriptor.getCOSObject().getItem(COSName.getPDFName(FONT_DICTIONARY_KEY_CIDSET));
            if (cidset == null || !COSUtils.isStream(cidset, cosDocument))
            {
                this.fContainer.push(new ValidationResult.ValidationError(ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET,
                        pfDescriptor.getFontName() + ": The CIDSet entry is missing for the Composite Subset"));
            }
        }
    }

    @Override
    protected void processFontFile(PDFontDescriptor fontDescriptor, PDStream fontFile)
    {
        if (font.isDamaged())
        {
            this.fContainer.push(new ValidationError(ERROR_FONTS_CID_DAMAGED, 
                    font.getName() + ": The FontFile can't be read"));
            
        }
    }
}
