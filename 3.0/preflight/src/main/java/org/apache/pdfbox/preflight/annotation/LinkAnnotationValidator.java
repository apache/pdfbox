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

package org.apache.pdfbox.preflight.annotation;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_FORBIDDEN_DEST;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_INVALID_DEST;

import java.io.IOException;
import org.apache.pdfbox.cos.COSBase;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import static org.apache.pdfbox.preflight.PreflightConfiguration.DESTINATION_PROCESS;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.ContextHelper;

/**
 * Validation class for the LinkAnnotation
 */
public class LinkAnnotationValidator extends AnnotationValidator
{
    /**
     * PDFBox object which wraps the annotation dictionary
     */
    protected PDAnnotationLink pdLink = null;

    public LinkAnnotationValidator(PreflightContext ctx, COSDictionary annotDictionary)
    {
        super(ctx, annotDictionary);
        this.pdLink = new PDAnnotationLink(annotDictionary);
        this.pdAnnot = this.pdLink;
    }

    /**
     * In addition of the AnnotationValidator.validate() method, this method executes the checkDest method.
     * 
     * @see AnnotationValidator#validate()
     */
    @Override
    public boolean validate() throws ValidationException
    {
        boolean isValid = super.validate();
        isValid = checkDest() && isValid;
        return isValid;
    }

    /**
     * Check if the Dest element is authorized according to the A entry
     * 
     * @return if the Dest element is authorized according to the A entry.
     */
    protected boolean checkDest()
    {
        try
        {
            if (this.pdLink.getDestination() != null && this.pdLink.getAction() != null)
            {
                ctx.addValidationError(new ValidationError(ERROR_ANNOT_FORBIDDEN_DEST,
                        "Dest can't be used due to A element"));
                return false;
            }
            COSDictionary dict = pdLink.getCOSObject();
            COSBase dest = dict.getDictionaryObject(COSName.DEST);
            if (dest != null)
            {
                ContextHelper.validateElement(ctx, dest, DESTINATION_PROCESS);    
            }
        }
        catch (IOException e)
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_DEST, e.getMessage(), e));
            return false;
        }
        return true;
    }
}
