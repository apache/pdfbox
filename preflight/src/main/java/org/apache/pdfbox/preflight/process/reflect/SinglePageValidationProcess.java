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

import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.ANNOTATIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.GRAPHIC_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.RESOURCES_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_TRANSPARENCY_GROUP;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_UNKNOWN_ERROR;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.content.PreflightContentStream;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelper;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class SinglePageValidationProcess extends AbstractProcess
{
    private static final Log LOGGER = LogFactory.getLog(SinglePageValidationProcess.class);

    @Override
    public void validate(final PreflightContext context) throws ValidationException
    {
        final PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty())
        {
            return;
        }
        if (!vPath.isExpectedType(PDPage.class)) 
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_PDF_PROCESSING_MISSING, "Page validation required at least a PDPage"));
        } 
        else 
        {
            final PDPage page = (PDPage) vPath.peek();
            validateActions(context, page);
            validateAnnotation(context, page);
            validateColorSpaces(context, page);
            validateResources(context, page);
            validateGraphicObjects(context, page);
            validateGroupTransparency(context, page);
            // TODO
            // add MetaData validation ?
            validateContent(context, page);
        }
    }

    /**
     * This method checks additional actions contained in the given Page object.
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @throws ValidationException
     */
    protected void validateActions(final PreflightContext context, final PDPage page) throws ValidationException
    {
        ContextHelper.validateElement(context, page.getCOSObject(), ACTIONS_PROCESS);
    }

    /**
     * Check that all ColorSpace present in the Resource dictionary are conforming to the ISO 19005:2005-1
     * specification.
     * 
     * @param context the preflight context.
     * @param page the page to check.
     */
    protected void validateColorSpaces(final PreflightContext context, final PDPage page)
    {
        final PDResources resources = page.getResources();
        if (resources != null)
        {
            final PreflightConfiguration config = context.getConfig();
            final ColorSpaceHelperFactory colorSpaceFactory = config.getColorSpaceHelperFact();
            for (final COSName name : resources.getColorSpaceNames())
            {
                try
                {
                    final PDColorSpace pdCS = resources.getColorSpace(name);
                    final ColorSpaceHelper csHelper = colorSpaceFactory.getColorSpaceHelper(context, pdCS,
                            ColorSpaceRestriction.NO_RESTRICTION);
                    csHelper.validate();
                }
                catch (IOException e)
                {
                    // fixme: this code was previously in PDResources
                    LOGGER.debug("Unable to create colorspace", e);
                }
            }
        }
    }

    /**
     * Check that all XObject references in the PDResource of the page and in the Thumb entry are confirming to the
     * PDF/A specification.
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @throws ValidationException
     */
    protected void validateGraphicObjects(final PreflightContext context, final PDPage page) throws ValidationException
    {
        COSBase thumbBase = page.getCOSObject().getItem(COSName.THUMB);
        if (thumbBase != null)
        {
            try
            {
                if (thumbBase instanceof COSObject)
                {
                    thumbBase = ((COSObject) thumbBase).getObject();
                }
                if (!(thumbBase instanceof COSStream))
                {
                    context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID,
                            "Thumb image must be a stream"));
                    return;
                }
                final PDXObject thumbImg = PDImageXObject.createThumbnail((COSStream)thumbBase);
                ContextHelper.validateElement(context, thumbImg, GRAPHIC_PROCESS);
            }
            catch (IOException e)
            {
                LOGGER.debug("Unable to read Thumb image", e);
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID, "Unable to read Thumb image : "
                        + e.getMessage()));
            }
        }
    }

    protected void validateResources(final PreflightContext context, final PDPage page) throws ValidationException
    {
        ContextHelper.validateElement(context, page.getResources(), RESOURCES_PROCESS);
    }

    /**
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @throws ValidationException
     */
    protected void validateContent(final PreflightContext context, final PDPage page) throws ValidationException
    {
        // TODO add this wrapper in the config object ?
        try
        {
            final PreflightContentStream csWrapper = new PreflightContentStream(context, page);
            csWrapper.validatePageContentStream();
        }
        catch (IOException e)
        {
            LOGGER.debug("Unable to read page content stream", e);
            context.addValidationError(new ValidationError(ERROR_UNKNOWN_ERROR, e.getMessage(), e));
        }
    }

    /**
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @throws ValidationException
     */
    protected void validateAnnotation(final PreflightContext context, final PDPage page) throws ValidationException
    {
        try
        {
            final List<?> lAnnots = page.getAnnotations();
            for (final Object object : lAnnots)
            {
                if (object instanceof PDAnnotation)
                {
                    final COSDictionary cosAnnot = ((PDAnnotation) object).getCOSObject();
                    ContextHelper.validateElement(context, cosAnnot, ANNOTATIONS_PROCESS);
                }
            }
        }
        catch (ValidationException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            // TODO IOException probably due to Encrypt
            throw new ValidationException("Unable to access Annotation", e);
        }
    }

    /**
     * Check that the group dictionary doesn't have a Transparency attribute
     * 
     * @param context the preflight context.
     * @param page the page to check.
     * @throws ValidationException
     */
    protected void validateGroupTransparency(final PreflightContext context, final PDPage page) throws ValidationException
    {
        final COSDictionary groupDictionary = page.getCOSObject().getCOSDictionary(COSName.GROUP);
        if (groupDictionary != null)
        {
            final COSName sVal = groupDictionary.getCOSName(COSName.S);
            if (COSName.TRANSPARENCY.equals(sVal))
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_TRANSPARENCY_GROUP,
                        "Group has a transparency S entry or the S entry is null"));
            }
        }
    }

}
