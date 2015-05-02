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

import java.io.IOException;

import static org.apache.pdfbox.preflight.PreflightConfiguration.EXTGSTATE_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelper;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class ShadingPatternValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty())
        {
            return;
        }
        if (!vPath.isExpectedType(PDShading.class))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_OBJECT,
                    "ShadingPattern validation required at least a PDResources"));
        } 
        else 
        {
            PDShading shadingResource = (PDShading) vPath.peek();
            PDPage page = vPath.getClosestPathElement(PDPage.class);
            checkColorSpace(context, page, shadingResource);
            checkGraphicState(context, page, shadingResource);
        }
    }

    /**
     * Checks if the ColorSapce entry is consistent which rules of the PDF Reference and the ISO 190005-1:2005
     * Specification.
     * 
     * This method is called by the validate method.
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @param shadingRes the Shading pattern to check.
     * @throws ValidationException
     */
    protected void checkColorSpace(PreflightContext context, PDPage page, PDShading shadingRes)
            throws ValidationException
    {
        try
        {
            PDColorSpace pColorSpace = shadingRes.getColorSpace();
            PreflightConfiguration config = context.getConfig();
            ColorSpaceHelperFactory csFact = config.getColorSpaceHelperFact();
            ColorSpaceHelper csh = csFact.getColorSpaceHelper(context, pColorSpace, ColorSpaceRestriction.NO_PATTERN);
            csh.validate();
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE, e.getMessage()));
        }
    }

    /**
     * Check the Extended Graphic State contains in the ShadingPattern dictionary if it is present. To check this
     * ExtGState, this method uses the net.awl.edoc.pdfa.validation.graphics.ExtGStateContainer object.
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @param shadingRes the Shading pattern to check.
     * @throws ValidationException
     */
    protected void checkGraphicState(PreflightContext context, PDPage page, PDShading shadingRes)
            throws ValidationException
    {
        COSDictionary resources = (COSDictionary) shadingRes.getCOSObject().getDictionaryObject(
                TRANPARENCY_DICTIONARY_KEY_EXTGSTATE);
        if (resources != null)
        {
            ContextHelper.validateElement(context, resources, EXTGSTATE_PROCESS);
        }
    }
}
