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

import static org.apache.pdfbox.preflight.PreflightConfiguration.RESOURCES_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPatternResources;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.content.ContentStreamWrapper;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class TilingPatternValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()) {
            return;
        }
        else if (!vPath.isExpectedType(PDTilingPatternResources.class))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_OBJECT, "Tiling pattern validation required at least a PDPage"));
        }
        else
        {
            PDTilingPatternResources tilingPattern = (PDTilingPatternResources) vPath.peek();
            PDPage page = vPath.getClosestPathElement(PDPage.class);

            checkMandatoryFields(context, page, tilingPattern);
            parseResources(context, page, tilingPattern);
            parsePatternContent(context, page, tilingPattern);
        }
    }

    protected void parseResources(PreflightContext context, PDPage page, PDTilingPatternResources pattern)
            throws ValidationException
            {
        PDResources resources = pattern.getResources();
        if (resources != null)
        {
            ContextHelper.validateElement(context, resources, RESOURCES_PROCESS);
        }
            }

    /**
     * Validate the Pattern content like Color and Show Text Operators using an instance of ContentStreamWrapper.
     */
    protected void parsePatternContent(PreflightContext context, PDPage page, PDTilingPatternResources pattern)
            throws ValidationException
            {
        ContentStreamWrapper csWrapper = new ContentStreamWrapper(context, page);
        csWrapper.validPatternContentStream((COSStream) pattern.getCOSObject());
            }

    /**
     * This method checks if required fields are present.
     */
    protected void checkMandatoryFields(PreflightContext context, PDPage page, PDTilingPatternResources pattern)
    {
        COSDictionary dictionary = pattern.getCOSDictionary();
        boolean res = dictionary.getItem(COSName.RESOURCES) != null;
        res = res && dictionary.getItem(COSName.BBOX) != null;
        res = res && dictionary.getItem(COSName.PAINT_TYPE) != null;
        res = res && dictionary.getItem(COSName.TILING_TYPE) != null;
        res = res && dictionary.getItem(COSName.X_STEP) != null;
        res = res && dictionary.getItem(COSName.Y_STEP) != null;
        if (!res)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION));
        }
    }
}
