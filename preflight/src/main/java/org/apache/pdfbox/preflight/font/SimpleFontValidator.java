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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_DICTIONARY_INVALID;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;

public abstract class SimpleFontValidator<T extends FontContainer> extends FontValidator<T>
{

    public SimpleFontValidator(PreflightContext context, PDFont font, T fContainer)
    {
        super(context, font, fContainer);
    }

    /**
     * Call this method to validate the font wrapped by this object. If the validation failed, the error is updated in
     * the FontContainer with the right error code.
     * 
     * Errors that are saved in the container will be added on the PreflightContext if the font is used later.
     * 
     * @return
     */
    public void validate() throws ValidationException
    {
        checkMandatoryField();

        createFontDescriptorHelper();
        processFontDescriptorValidation();

        checkEncoding();
        checkToUnicode();
    }

    protected void checkMandatoryField()
    {
        COSDictionary fontDictionary = (COSDictionary) font.getCOSObject();
        boolean areFieldsPResent = fontDictionary.containsKey(COSName.TYPE);
        areFieldsPResent &= fontDictionary.containsKey(COSName.SUBTYPE);
        areFieldsPResent &= fontDictionary.containsKey(COSName.BASE_FONT);
        areFieldsPResent &= fontDictionary.containsKey(COSName.FIRST_CHAR);
        areFieldsPResent &= fontDictionary.containsKey(COSName.LAST_CHAR);
        areFieldsPResent &= fontDictionary.containsKey(COSName.WIDTHS);

        if (!areFieldsPResent)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "Some required fields are missing from the Font dictionary."));
        }
    }

    protected abstract void createFontDescriptorHelper();

    protected void processFontDescriptorValidation()
    {
        this.descriptorHelper.validate();
    }
}
