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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.ContextHelper;


import static org.apache.pdfbox.preflight.PreflightConfiguration.EXTGSTATE_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.FONT_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.GRAPHIC_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.SHADING_PATTERN_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.TILING_PATTERN_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_MAIN;

public class ResourcesValidationProcess extends AbstractProcess
{

    @Override
    public void validate(final PreflightContext ctx) throws ValidationException
    {
        final PreflightPath vPath = ctx.getValidationPath();
        if (vPath.isEmpty())
        {
        }
        else if (!vPath.isExpectedType(PDResources.class))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_PDF_PROCESSING_MISSING, "Resources validation process needs at least one PDResources object"));
        } 
        else
        {
            final PDResources resources = (PDResources) vPath.peek();
            
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
    protected void validateFonts(final PreflightContext context, final PDResources resources) throws ValidationException
    {
        final Map<String, PDFont> mapOfFonts = getFonts(resources.getCOSObject(), context);
        for (final Entry<String, PDFont> entry : mapOfFonts.entrySet())
        {
            ContextHelper.validateElement(context, entry.getValue(), FONT_PROCESS);
        }
    }

    /**
     * This will get the map of fonts. This will never return null.
     *
     * @return The map of fonts.
     */
    private Map<String, PDFont> getFonts(final COSDictionary resources, final PreflightContext context)
    {
        final Map<String, PDFont> fonts = new HashMap<>();
        COSDictionary fontsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.FONT);
        if (fontsDictionary == null)
        {
            fontsDictionary = new COSDictionary();
            resources.setItem(COSName.FONT, fontsDictionary);
        }
        for (final COSName fontName : fontsDictionary.keySet())
        {
            final COSBase font = fontsDictionary.getDictionaryObject(fontName);
            // data-000174.pdf contains a font that is a COSArray, looks to be an error in the
            // PDF, we will just ignore entries that are not dictionaries.
            if (font instanceof COSDictionary)
            {
                PDFont newFont = null;
                try
                {
                    newFont = PDFontFactory.createFont((COSDictionary) font);
                }
                catch (final IOException e)
                {
                    addFontError((COSDictionary) font, context, e);
                }
                if (newFont != null)
                {
                    fonts.put(fontName.getName(), newFont);
                }
            }
        }
        return fonts;
    }

    /**
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateExtGStates(final PreflightContext context, final PDResources resources) throws ValidationException
    {
        final COSDictionary extGState = resources.getCOSObject().getCOSDictionary(COSName.EXT_G_STATE);
        if (extGState != null)
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
    protected void validateShadingPattern(final PreflightContext context, final PDResources resources) throws ValidationException
    {
        try
        {
            for (final COSName name : resources.getShadingNames())
            {
                final PDShading shading = resources.getShading(name);
                ContextHelper.validateElement(context, shading, SHADING_PATTERN_PROCESS);
            }
        }
        catch (final IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION, e.getMessage(), e));
        }
    }

    /**
     * This method check the Shading entry of the resource dictionary if exists.
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateTilingPattern(final PreflightContext context, final PDResources resources) throws ValidationException
    {
        try
        {
            for (final COSName name : resources.getPatternNames())
            {
                final PDAbstractPattern pattern = resources.getPattern(name);
                if (pattern instanceof PDTilingPattern)
                {
                    ContextHelper.validateElement(context, pattern, TILING_PATTERN_PROCESS);
                }
            }
        }
        catch (final IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION, e.getMessage(), e));
        }
    }

    protected void validateXObjects(final PreflightContext context, final PDResources resources) throws ValidationException
    {
        final COSDictionary mapOfXObj = resources.getCOSObject().getCOSDictionary(COSName.XOBJECT);
        if (mapOfXObj == null)
        {
            return;
        }
        for (final Entry<COSName, COSBase> entry : mapOfXObj.entrySet())
        {
            COSBase xobj = entry.getValue();
            if (xobj instanceof COSObject)
            {
                xobj = ((COSObject) xobj).getObject();
            }
            if (xobj instanceof COSStream)
            {
                try
                {
                    final PDXObject pdXObject = PDXObject.createXObject(xobj, resources);
                    ContextHelper.validateElement(context, pdXObject, GRAPHIC_PROCESS);
                }
                catch (final IOException e)
                {
                    context.addValidationError(new ValidationError(ERROR_GRAPHIC_MAIN,
                            e.getMessage() + " for entry '" + entry.getKey().getName() + "'", e));
                }
            }
        }
    }
}
