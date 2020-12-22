/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.preflight.process.reflect;

import java.io.IOException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.preflight.PreflightConstants;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_DICT_INVALID;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;

/**
 *
 * @author Tilman Hausherr
 */
public class DestinationValidationProcess extends AbstractProcess
{

    @Override
    public void validate(final PreflightContext ctx) throws ValidationException
    {
        final PreflightPath vPath = ctx.getValidationPath();
        if (vPath.isEmpty())
        {
            return;
        }
        if (!vPath.isExpectedType(COSBase.class))
        {
            ctx.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_ACTION_INVALID_TYPE,
                    "Destination validation process needs at least one COSBase object"));
            return;
        }
        final COSBase dest = (COSBase) vPath.peek();
        
        // "A destination may be specified either explicitly by 
        // an array of parameters defining its properties or indirectly by name."

        if (!(dest instanceof COSName || dest instanceof COSString || dest instanceof COSArray))
        {
            ctx.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID, 
                    "Destination type entry " + dest.getClass().getSimpleName() + " is invalid"));
            return;
        }

        if (dest instanceof COSArray)
        {
            final COSArray ar = (COSArray) dest;
            if (ar.size() < 2)
            {
                ctx.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID,
                        "Destination array must have at least 2 elements"));
                return;
            }
            if (!(ar.get(1) instanceof COSName))
            {
                ctx.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID,
                        "Second element of destination array must be a name"));
                return;
            }
                
            validateExplicitDestination(ctx, ar);
        }

        try
        {
            PDDestination.create(dest);
        }
        catch (IOException e)
        {
            ctx.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_SYNTAX_DICT_INVALID,
                    e.getMessage(), e));
        }

    }

    void validateExplicitDestination(final PreflightContext ctx, final COSArray ar)
    {
        // "In each case, page is an indirect reference to a page object."
        final COSBase cosObject = ar.getObject(0);
        if (cosObject instanceof COSDictionary)
        {
            final COSName type = ((COSDictionary) cosObject).getCOSName(COSName.TYPE);
            if (COSName.PAGE.equals(type))
            {
                try
                {
                    PDDestination.create(ar);
                }
                catch (IOException e)
                {
                    ctx.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID,
                            e.getMessage(), e));
                }
                return;
            }
        }
        ctx.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID,
                "First element in Destination array entry must be an indirect reference to a dictionary of /Type /Page, but is "
                + ar.getName(0)));
    }

}
