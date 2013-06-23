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

import static org.apache.pdfbox.preflight.PreflightConfiguration.EXTGSTATE_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.FONT_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.GRAPHIC_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.SHADDING_PATTERN_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.TILING_PATTERN_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPatternResources;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class ResourcesValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext ctx) throws ValidationException
    {
        PreflightPath vPath = ctx.getValidationPath();
        if (vPath.isEmpty()) {
            return;
        }
        else if (!vPath.isExpectedType(PDResources.class))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_PDF_PROCESSING_MISSING, "Resources validation process needs at least one PDResources object"));
        } 
        else
        {

            PDResources resources = (PDResources) vPath.peek();

            validateFonts(ctx, resources);
            validateExtGStates(ctx, resources);
            validateShadingPattern(ctx, resources);
            validateTilingPattern(ctx, resources);
            validateXObjects(ctx, resources);
        }
    }

    /**
     * Check that fonts present in the Resources dictionary match with PDF/A-1 rules
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateFonts(PreflightContext context, PDResources resources) throws ValidationException
    {
        Map<String, PDFont> mapOfFonts = resources.getFonts();
        if (mapOfFonts != null)
        {
            for (Entry<String, PDFont> entry : mapOfFonts.entrySet())
            {
                ContextHelper.validateElement(context, entry.getValue(), FONT_PROCESS);
            }
        }
    }

    /**
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateExtGStates(PreflightContext context, PDResources resources) throws ValidationException
    {
        COSBase egsEntry = resources.getCOSDictionary().getItem(TRANPARENCY_DICTIONARY_KEY_EXTGSTATE);
        COSDocument cosDocument = context.getDocument().getDocument();
        COSDictionary extGState = COSUtils.getAsDictionary(egsEntry, cosDocument);
        if (egsEntry != null)
        {
            ContextHelper.validateElement(context, extGState, EXTGSTATE_PROCESS);
        }
    }

    /**
     * This method check the Shading entry of the resource dictionary if exists.
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateShadingPattern(PreflightContext context, PDResources resources) throws ValidationException
    {
        try
        {
            Map<String, PDShadingResources> shadingResources = resources.getShadings();
            if (shadingResources != null)
            {
                for (Entry<String, PDShadingResources> entry : shadingResources.entrySet())
                {
                    ContextHelper.validateElement(context, entry.getValue(), SHADDING_PATTERN_PROCESS);
                }
            }
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION, e.getMessage()));
        }
    }

    /**
     * This method check the Shading entry of the resource dictionary if exists.
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateTilingPattern(PreflightContext context, PDResources resources) throws ValidationException
    {
        try
        {
            Map<String, PDPatternResources> patternResources = resources.getPatterns();
            if (patternResources != null)
            {
                for (Entry<String, PDPatternResources> entry : patternResources.entrySet())
                {
                    if (entry.getValue() instanceof PDTilingPatternResources)
                    {
                        ContextHelper.validateElement(context, entry.getValue(), TILING_PATTERN_PROCESS);
                    }
                }
            }
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION, e.getMessage()));
        }
    }

    protected void validateXObjects(PreflightContext context, PDResources resources) throws ValidationException
    {
        COSDocument cosDocument = context.getDocument().getDocument();
        COSDictionary mapOfXObj = COSUtils.getAsDictionary(resources.getCOSDictionary().getItem(COSName.XOBJECT),
                cosDocument);
        if (mapOfXObj != null)
        {
            for (Entry<COSName, COSBase> entry : mapOfXObj.entrySet())
            {
                COSBase xobj = entry.getValue();
                if (xobj != null && COSUtils.isStream(xobj, cosDocument))
                {
                    try
                    {
                        COSStream stream = COSUtils.getAsStream(xobj, cosDocument);
                        PDXObject pdXObject = PDXObject.createXObject(stream);
                        if (pdXObject != null)
                        {
                            ContextHelper.validateElement(context, pdXObject, GRAPHIC_PROCESS);
                        }
                        else
                        {
                            ContextHelper.validateElement(context, stream, GRAPHIC_PROCESS);
                        }
                    }
                    catch (IOException e)
                    {
                        throw new ValidationException(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
