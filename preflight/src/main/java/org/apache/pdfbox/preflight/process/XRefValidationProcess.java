/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.process;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.preflight.PreflightConstants;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_INDIRECT_OBJ_RANGE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

public class XRefValidationProcess extends AbstractProcess
{

    @Override
    public void validate(final PreflightContext ctx) throws ValidationException
    {
        final COSDocument document = ctx.getDocument().getDocument();
        if (document.isXRefStream())
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER, "/XRef cross reference streams are not allowed"));
        }
        if (document.getXrefTable().size() > PreflightConstants.MAX_INDIRECT_OBJ)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_INDIRECT_OBJ_RANGE, "Too many indirect objects"));
        }
    }

}
