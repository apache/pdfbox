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

package org.apache.pdfbox.preflight.process;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightConstants;

import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

public abstract class AbstractProcess implements ValidationProcess
{

    protected void addValidationError(PreflightContext ctx, ValidationError error)
    {
        ctx.addValidationError(error);
    }

    protected void addValidationErrors(PreflightContext ctx, List<ValidationError> errors)
    {
        for (ValidationError error : errors)
        {
            addValidationError(ctx, error);
        }
    }

    /**
     * PDFont loads embedded fonts in its constructor so we have to handle IOExceptions
     * from PDFont and translate them into validation errors.
     */
    protected void addFontError(COSDictionary dictionary, PreflightContext context, IOException e)
    {
        COSName type = dictionary.getCOSName(COSName.TYPE, COSName.FONT);
        if (!COSName.FONT.equals(type))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_TYPE, "Expected 'Font' dictionary but found '" + type.getName() + "'"));
        }
        String fontName = "Unknown";
        if (dictionary.containsKey(COSName.BASE_FONT))
        {
            fontName = dictionary.getNameAsString(COSName.BASE_FONT);
        }
        COSName subType = dictionary.getCOSName(COSName.SUBTYPE);
        if (COSName.TYPE1.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_TYPE1_DAMAGED, "The FontFile can't be read for " + fontName + ": " + e.getMessage()));
        }
        else if (COSName.MM_TYPE1.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_TYPE1_DAMAGED, "The FontFile can't be read for " + fontName + ": " + e.getMessage()));
        }
        else if (COSName.TRUE_TYPE.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_TRUETYPE_DAMAGED, "The FontFile can't be read for " + fontName + ": " + e.getMessage()));
        }
        else if (COSName.TYPE3.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_TYPE3_DAMAGED, "The FontFile can't be read for " + fontName + ": " + e.getMessage()));
        }
        else if (COSName.TYPE0.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read for " + fontName + ": " + e.getMessage()));
        }
        else if (COSName.CID_FONT_TYPE0.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_TYPE, "Unexpected CIDFontType0 descendant font for " + fontName + ": " + e.getMessage()));
        }
        else if (COSName.CID_FONT_TYPE2.equals(subType))
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_TYPE, "Unexpected CIDFontType2 descendant font for " + fontName + ": " + e.getMessage()));
        }
        else
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_TYPE, "Unknown font type for " + fontName));
        }
    }
}
