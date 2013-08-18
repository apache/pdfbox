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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_NOT_RECOMMENDED_FLAG;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * Validation class for Text Annotation
 */
public class TextAnnotationValidator extends AnnotationValidator
{
    /**
     * PDFBox object which wraps the annotation dictionary
     */
    protected PDAnnotationText pdText = null;

    public TextAnnotationValidator(PreflightContext ctx, COSDictionary annotDictionary)
    {
        super(ctx, annotDictionary);
        this.pdText = new PDAnnotationText(annotDictionary);
        this.pdAnnot = this.pdText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.annotation.AnnotationValidator#checkFlags( java.util.List)
     */
    protected boolean checkFlags()
    {
        // call common flags settings
        boolean result = super.checkFlags();

        /*
         * For Text Annotation, this two flags should be set to avoid potential ambiguity between the annotation
         * dictionary and the reader behavior.
         */
        result = result && this.pdAnnot.isNoRotate();
        result = result && this.pdAnnot.isNoZoom();
        if (!result)
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_NOT_RECOMMENDED_FLAG));
        }
        return result;
    }
}
