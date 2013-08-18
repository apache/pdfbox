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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_FORBIDDEN_AA;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

/**
 * Validation class for the Widget Annotation
 */
public class WidgetAnnotationValidator extends AnnotationValidator
{
    /**
     * PDFBox object which wraps the annotation dictionary
     */
    protected PDAnnotationWidget pdWidget = null;

    public WidgetAnnotationValidator(PreflightContext ctx, COSDictionary annotDictionary)
    {
        super(ctx, annotDictionary);
        this.pdWidget = new PDAnnotationWidget(annotDictionary);
        this.pdAnnot = this.pdWidget;
    }

    /**
     * In addition of the AnnotationValidator.validate() method, this method executes the the checkAAField method.
     * 
     * @see org.apache.padaf.preflight.annotation.AnnotationValidator#validate(java.util.List)
     */
    @Override
    public boolean validate() throws ValidationException
    {
        boolean isValide = super.validate();
        isValide = isValide && checkAAField();
        return isValide;
    }

    /**
     * The AA field is forbidden for the Widget annotation when the PDF is a PDF/A. This method return false and update
     * the errors list if this key is present. returns true otherwise
     * 
     * @return
     */
    protected boolean checkAAField()
    {
        if (this.pdWidget.getActions() != null)
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_FORBIDDEN_AA));
            return false;
        }
        return true;
    }
}
