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

package org.apache.pdfbox.preflight.utils;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_PDF_PROCESSING;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_PDF_PROCESSING_MISSING;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.ValidationProcess;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

public final class ContextHelper
{
    private ContextHelper()
    {
    }
    
    /**
     * Check that the element parameter isn't null before calling the
     * {@link #callValidation(PreflightContext, Object, String)} method.
     * 
     * @param context
     * @param element
     * @param processName
     * @throws ValidationException
     */
    public static void validateElement(PreflightContext context, Object element, String processName) throws ValidationException
    {
        if (element == null)
        {
            context.addValidationError(new ValidationError(ERROR_PDF_PROCESSING_MISSING, "Unable to process an element if it is null."));
        } 
        else 
        {
            callValidation(context, element, processName);
        }
    }

    /**
     * Put the element to check on the top of the ValidationPath and call the validation method on the Process.
     * 
     * @param context
     *            (mandatory) the preflight context that contains all required information
     * @param element
     * @param processName
     *            the process to instantiate and to compute
     * @throws ValidationException
     */
    private static void callValidation(PreflightContext context, Object element, String processName)
    throws ValidationException
    {
        PreflightPath validationPath = context.getValidationPath();
        
        if (hasRecursion(context, element, validationPath))
        {
            return;
        }
        // avoid checking certain elements twice. This can't be generalized,
        // because some are checked by several processes.
        if (element instanceof COSObjectable && 
                // PDFBOX-4450 catch undetected recursions
                (PreflightConfiguration.GRAPHIC_PROCESS.equals(processName) || 
                 PreflightConfiguration.FONT_PROCESS.equals(processName))) // for speed
        {
            // don't check PDObjects, only their COSObject
            COSBase cos = ((COSObjectable) element).getCOSObject();
            if (context.isInProcessedSet(cos))
            {
                return;
            }
            context.addToProcessedSet(cos);
        }

        boolean needPop = validationPath.pushObject(element);
        PreflightConfiguration config = context.getConfig();
        ValidationProcess process = config.getInstanceOfProcess(processName);
        process.validate(context);
        if (needPop)
        {
            validationPath.pop();
        }
    }

    // detect recursion that would lead to stack overflow
    private static boolean hasRecursion(PreflightContext context, Object element, PreflightPath validationPath)
    {
        if (element instanceof PDResources || element instanceof PDFormXObject)
        {
            for (int i = 0; i < validationPath.size(); ++i)
            {
                Object obj = validationPath.getPathElement(i, Object.class);
                if (obj instanceof COSObjectable)
                {
                    COSObjectable cos = (COSObjectable) obj;
                    if (cos.getCOSObject() == ((COSObjectable) element).getCOSObject())
                    {
                        context.addValidationError(new ValidationError(ERROR_PDF_PROCESSING, 
                                element.getClass().getSimpleName() + " recursion"));
                        return true;
                    }
                }               
            }
        }
        return false;
    }

    /**
     * call directly the {@link #callValidation(PreflightContext, Object, String)}
     * 
     * @param context
     * @param processName
     * @throws ValidationException
     */
    public static void validateElement(PreflightContext context, String processName) throws ValidationException
    {
        callValidation(context, null, processName);
    }
}
