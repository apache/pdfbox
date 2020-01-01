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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.Type1Container;
import org.apache.pdfbox.preflight.font.descriptor.Type1DescriptorHelper;

public class Type1FontValidator extends SimpleFontValidator<Type1Container>
{
    public Type1FontValidator(PreflightContext context, PDSimpleFont font)
    {
        super(context, font, font.getCOSObject(), new Type1Container(font));
    }

    @Override
    protected void createFontDescriptorHelper()
    {
        descriptorHelper = new Type1DescriptorHelper(context, (PDSimpleFont) font, fontContainer);
    }

    @Override
    protected void checkEncoding()
    {
        COSBase encoding = fontDictionary.getDictionaryObject(COSName.ENCODING);
        if (encoding != null)
        {
            if (encoding instanceof COSName)
            {
                COSName encodingName = (COSName) encoding;
                if (!(encodingName.equals(COSName.MAC_ROMAN_ENCODING)
                        || encodingName.equals(COSName.MAC_EXPERT_ENCODING)
                        || encodingName.equals(COSName.WIN_ANSI_ENCODING)
                        || encodingName.equals(COSName.PDF_DOC_ENCODING)
                        || encodingName.equals(COSName.STANDARD_ENCODING)))
                {
                    fontContainer
                            .push(new ValidationError(PreflightConstants.ERROR_FONTS_ENCODING));
                }
            }
            else if (!(encoding instanceof COSDictionary))
            {
                fontContainer
                        .push(new ValidationError(PreflightConstants.ERROR_FONTS_ENCODING));
            }
        }
    }

}
