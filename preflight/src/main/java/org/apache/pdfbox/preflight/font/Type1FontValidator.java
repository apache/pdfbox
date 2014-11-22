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
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_MAC;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_PDFDOC;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_STD;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_WIN;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType1Equivalent;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.Type1Container;
import org.apache.pdfbox.preflight.font.descriptor.Type1DescriptorHelper;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class Type1FontValidator extends SimpleFontValidator<Type1Container>
{
    public Type1FontValidator(PreflightContext context, PDSimpleFont font)
    {
        super(context, font, font.getCOSObject(), new Type1Container((PDType1Equivalent)font));
    }

    @Override
    protected void createFontDescriptorHelper()
    {
        this.descriptorHelper = new Type1DescriptorHelper(context, (PDType1Equivalent)font, fontContainer);
    }

    @Override
    protected void checkEncoding()
    {
        COSBase encoding = ((COSDictionary) fontDictionary).getItem(COSName.ENCODING);
        if (encoding != null)
        {
            COSDocument cosDocument = context.getDocument().getDocument();
            if (COSUtils.isString(encoding, cosDocument))
            {
                String encodingName = COSUtils.getAsString(encoding, cosDocument);
                if (!(encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_MAC)
                        || encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP)
                        || encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_WIN)
                        || encodingName.equals(FONT_DICTIONARY_VALUE_ENCODING_PDFDOC) || encodingName
                            .equals(FONT_DICTIONARY_VALUE_ENCODING_STD)))
                {
                    this.fontContainer.push(new ValidationError(ERROR_FONTS_ENCODING));
                }
            }
            else if (!COSUtils.isDictionary(encoding, cosDocument))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_ENCODING));
            }
        }
    }

}
