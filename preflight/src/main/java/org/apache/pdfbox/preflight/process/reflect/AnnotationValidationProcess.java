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

package org.apache.pdfbox.preflight.process.reflect;

import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.annotation.AnnotationValidator;
import org.apache.pdfbox.preflight.annotation.AnnotationValidatorFactory;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;

public class AnnotationValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()) {
            return;
        }
        else if (!vPath.isExpectedType(COSDictionary.class))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_ANNOT_INVALID_ELEMENT, "Annotation validation process needs at least one COSDictionary object"));
        }
        else
        {
            COSDictionary annotDict = (COSDictionary) vPath.peek();

            PreflightConfiguration config = context.getConfig();
            AnnotationValidatorFactory factory = config.getAnnotFact();
            AnnotationValidator annotValidator = factory.getAnnotationValidator(context, annotDict);
            if (annotValidator != null)
            {
                annotValidator.validate();
            }
        }
    }

}
