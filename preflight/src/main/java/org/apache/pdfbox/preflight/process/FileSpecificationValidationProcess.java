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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

/**
 * 
 * This validation process check that FileSpec dictionaries are confirming with the PDF/A-1b specification.
 */
public class FileSpecificationValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        COSDocument cDoc = ctx.getDocument().getDocument();

        cDoc.getObjectsByType(COSName.FILESPEC, COSName.F)
                .forEach(o -> validateFileSpecification(ctx, (COSDictionary) o.getObject()));
    }

    /**
     * Validate a FileSpec dictionary, a FileSpec dictionary mustn't have the EF (EmbeddedFile) entry.
     * 
     * @param ctx the preflight context.
     * @param fileSpec the FileSpec Dictionary.
     */
    public void validateFileSpecification(PreflightContext ctx, COSDictionary fileSpec)
    {
        // ---- Check dictionary entries
        // ---- Only the EF entry is forbidden
        if (fileSpec.getItem(COSName.EF) != null)
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_EMBEDDED_FILES,
                    "EmbeddedFile entry is present in a FileSpecification dictionary"));
        }
    }
}
