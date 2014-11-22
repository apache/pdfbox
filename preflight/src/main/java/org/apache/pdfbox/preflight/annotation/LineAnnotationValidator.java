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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_FORBIDDEN_COLOR;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

/**
 * Validation class for the LineAnnotation
 */
public class LineAnnotationValidator extends AnnotationValidator
{
    /**
     * PDFBox object which wraps the annotation dictionary
     */
    protected PDAnnotationLine pdLine = null;

    public LineAnnotationValidator(PreflightContext ctx, COSDictionary annotDictionary)
    {
        super(ctx, annotDictionary);
        this.pdLine = new PDAnnotationLine(annotDictionary);
        this.pdAnnot = this.pdLine;
    }

    /**
     * In addition of the AnnotationValidator.validate() method, this method executes the the checkIColors method.
     * 
     * @see AnnotationValidator#validate()
     */
    @Override
    public boolean validate() throws ValidationException
    {
        boolean isValid = super.validate();
        isValid = checkIColors() && isValid;
        return isValid;
    }

    /**
     * Return true if the IC field is present in the Annotation dictionary and if the RGB profile is used in the
     * DestOutputProfile of the OutputIntent dictionary.
     * 
     * @return the state of the IC field validation.
     */
    protected boolean checkIColors() throws ValidationException
    {
        if (this.pdLine.getInteriorColour() != null)
        {
            if (!searchRGBProfile())
            {
                ctx.addValidationError(new ValidationError(ERROR_ANNOT_FORBIDDEN_COLOR,
                        "Annotation uses a Color profile which isn't the same than the profile contained by the OutputIntent"));
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see AnnotationValidator#checkMandatoryFields()
     */
    @Override
    protected boolean checkSpecificMandatoryFields()
    {
        return this.annotDictionary.containsKey(COSName.L);
    }
}
