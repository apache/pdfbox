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
import org.apache.pdfbox.pdmodel.font.PDFontLike;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;

public abstract class SimpleFontValidator<T extends FontContainer> extends FontValidator<T>
{
    protected PDFontLike font;
    protected COSDictionary fontDictionary;

    public SimpleFontValidator(PreflightContext context, PDFontLike font, COSDictionary fontDictionary, T fContainer)
    {
        super(context, fontDictionary, fContainer);
        this.fontDictionary = fontDictionary;
        this.font = font;
    }

    /**
     * Call this method to validate the font wrapped by this object. If the
     * validation failed, the error is updated in the FontContainer with the
     * right error code.
     *
     * Errors that are saved in the container will be added on the
     * PreflightContext if the font is used later.
     */
    @Override
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
        String missingFields = "";
        boolean areFieldsPresent = fontDictionary.containsKey(COSName.TYPE);
        if (!areFieldsPresent)
        {
            missingFields = "type, ";
        }
        boolean subType = fontDictionary.containsKey(COSName.SUBTYPE);
        areFieldsPresent &= subType;
        if (!subType)
        {
            missingFields += "subType, ";
        }
        boolean baseFont = fontDictionary.containsKey(COSName.BASE_FONT);
        areFieldsPresent &= baseFont;
        if (!baseFont)
        {
            missingFields += "baseFont, ";
        }
        boolean firstChar = fontDictionary.containsKey(COSName.FIRST_CHAR);
        areFieldsPresent &= firstChar;
        if (!firstChar)
        {
            missingFields += "firstChar, ";
        }
        boolean lastChar = fontDictionary.containsKey(COSName.LAST_CHAR);
        areFieldsPresent &= lastChar;
        if (!lastChar)
        {
            missingFields += "lastChar, ";
        }
        boolean widths = fontDictionary.containsKey(COSName.WIDTHS);
        areFieldsPresent &= widths;
        if (!widths)
        {
            missingFields += "widths, ";
        }

        if (!areFieldsPresent)
        {
            if (missingFields.endsWith(", "))
            {
                missingFields = missingFields.substring(0, missingFields.length() - 2);
            }
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    this.font.getName()
                    + ": some required fields are missing from the Font dictionary: " + missingFields + "."));
        }
    }

    protected abstract void createFontDescriptorHelper();

    protected void processFontDescriptorValidation()
    {
        this.descriptorHelper.validate();
    }
}
