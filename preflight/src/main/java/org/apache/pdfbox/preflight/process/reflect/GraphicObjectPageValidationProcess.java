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

import static org.apache.pdfbox.preflight.PreflightConstants.XOBJECT_DICTIONARY_VALUE_SUBTYPE_POSTSCRIPT;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.xobject.XObjFormValidator;
import org.apache.pdfbox.preflight.xobject.XObjImageValidator;
import org.apache.pdfbox.preflight.xobject.XObjPostscriptValidator;
import org.apache.pdfbox.preflight.xobject.XObjectValidator;

import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

public class GraphicObjectPageValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();

        XObjectValidator validator = null;
        if (!vPath.isEmpty() && vPath.isExpectedType(PDXObjectImage.class))
        {
            validator = new XObjImageValidator(context, (PDXObjectImage) vPath.peek());
        }
        else if (!vPath.isEmpty() && vPath.isExpectedType(PDXObjectForm.class))
        {
            validator = new XObjFormValidator(context, (PDXObjectForm) vPath.peek());
        }
        else if (!vPath.isEmpty() && vPath.isExpectedType(COSStream.class))
        {
            COSStream stream = (COSStream) vPath.peek();
            String subType = stream.getNameAsString(COSName.SUBTYPE);
            if (XOBJECT_DICTIONARY_VALUE_SUBTYPE_POSTSCRIPT.equals(subType))
            {
                validator = new XObjPostscriptValidator(context, stream);
            }
            else
            {
                context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_XOBJECT_INVALID_TYPE, "Invalid XObject subtype"));
            }
        }
        else
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_OBJECT, "Graphic validation process needs at least one PDXObject"));
        }

        if (validator != null) {
            validator.validate();
        }
    }
}
