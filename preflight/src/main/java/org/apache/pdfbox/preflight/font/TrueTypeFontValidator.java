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

package org.apache.pdfbox.preflight.font;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_ENCODING;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.MacRomanEncoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.TrueTypeContainer;
import org.apache.pdfbox.preflight.font.descriptor.TrueTypeDescriptorHelper;

public class TrueTypeFontValidator extends SimpleFontValidator<TrueTypeContainer>
{

    public TrueTypeFontValidator(PreflightContext context, PDFont font)
    {
        super(context, font, new TrueTypeContainer(font));
    }

    protected void createFontDescriptorHelper()
    {
        this.descriptorHelper = new TrueTypeDescriptorHelper(context, font, fontContainer);
    }

    protected void checkEncoding()
    {
        PDFontDescriptor fd = this.font.getFontDescriptor();
        if (fd != null)
        {
            /*
             * only MacRomanEncoding or WinAnsiEncoding are allowed for a non symbolic font.
             */
            if (fd.isNonSymbolic())
            {
                Encoding encodingValue = this.font.getFontEncoding();
                if (encodingValue == null
                        || !(encodingValue instanceof MacRomanEncoding || encodingValue instanceof WinAnsiEncoding))
                {
                    this.fontContainer.push(new ValidationError(ERROR_FONTS_ENCODING,
                            "The Encoding is invalid for the NonSymbolic TTF"));
                }
            }

            /*
             * For symbolic font, no encoding entry is allowed and only one encoding entry is expected into the FontFile
             * CMap (Check latter when the FontFile stream will be checked)
             */
            if (fd.isSymbolic() && ((COSDictionary) this.font.getCOSObject()).getItem(COSName.ENCODING) != null)
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_ENCODING,
                        "The Encoding should be missing for the Symbolic TTF"));
            }
        }
    }
}
