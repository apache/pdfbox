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
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_UNKOWN_ERROR;
import static org.apache.pdfbox.preflight.PreflightConstants.PAGE_DICTIONARY_VALUE_THUMB;
import static org.apache.pdfbox.preflight.PreflightConstants.XOBJECT_DICTIONARY_KEY_GROUP;
import static org.apache.pdfbox.preflight.PreflightConstants.XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.content.ContentStreamWrapper;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelper;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class SinglePageValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()){
            return;
        }
        else if (!vPath.isExpectedType(PDPage.class)) 
        {
            addValidationError(context, new ValidationError(PreflightConstants.ERROR_PDF_PROCESSING_MISSING, "Page validation required at least a PDPage"));
        } 
        else 
        {
            PDPage page = (PDPage) vPath.peek();
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
     * @param context
     * @param page
     * @return
     * @throws ValidationException
     */
    protected void validateActions(PreflightContext context, PDPage page) throws ValidationException
    {
        ContextHelper.validateElement(context, page.getCOSDictionary(), ACTIONS_PROCESS);
    }

    /**
     * Check that all ColorSpace present in the Resource dictionary are conforming to the ISO 19005:2005-1
     * specification.
     * 
     * @param context
     * @param page
     */
    protected void validateColorSpaces(PreflightContext context, PDPage page) throws ValidationException
    {
        PDResources resources = page.getResources();
        if (resources != null)
        {
            Map<String, PDColorSpace> colorSpaces = resources.getColorSpaces();
            if (colorSpaces != null)
            {
                PreflightConfiguration config = context.getConfig();
                ColorSpaceHelperFactory colorSpaceFactory = config.getColorSpaceHelperFact();
                for (PDColorSpace pdCS : colorSpaces.values())
                {
                    ColorSpaceHelper csHelper = colorSpaceFactory.getColorSpaceHelper(context, pdCS,
                            ColorSpaceRestriction.NO_RESTRICTION);
                    csHelper.validate();
                }
            }
        }
    }

    /**
     * Check that all XObject references in the PDResource of the page and in the Thumb entry are confirming to the
     * PDF/A specification.
     * 
     * @param context
     * @param page
     * @throws ValidationException
     */
    protected void validateGraphicObjects(PreflightContext context, PDPage page) throws ValidationException
    {
        COSBase thumbBase = page.getCOSDictionary().getItem(PAGE_DICTIONARY_VALUE_THUMB);
        if (thumbBase != null)
        {
            try
            {
                if (thumbBase instanceof COSObject)
                {
                    thumbBase = ((COSObject) thumbBase).getObject();
                }
                PDXObject thumbImg = PDXObjectImage.createThumbnailXObject(thumbBase);
                ContextHelper.validateElement(context, thumbImg, GRAPHIC_PROCESS);
            }
            catch (IOException e)
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID, "Unable to read Thumb image : "
                        + e.getMessage()));
            }
        }
    }

    protected void validateResources(PreflightContext context, PDPage page) throws ValidationException
    {
        ContextHelper.validateElement(context, page.getResources(), RESOURCES_PROCESS);
    }

    /**
     * 
     * @param page
     * @param context
     * @return
     * @throws ValidationException
     */
    protected void validateContent(PreflightContext context, PDPage page) throws ValidationException
    {
        // TODO add this wrapper in the config object ?
        try
        {
            ContentStreamWrapper csWrapper = new ContentStreamWrapper(context, page);
            csWrapper.validPageContentStream();
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationError(ERROR_UNKOWN_ERROR, e.getMessage()));
        }
    }

    /**
     * 
     * @param page
     * @return
     * @throws ValidationException
     */
    protected void validateAnnotation(PreflightContext context, PDPage page) throws ValidationException
    {
        try
        {
            List<?> lAnnots = page.getAnnotations();
            for (Object object : lAnnots)
            {
                if (object instanceof PDAnnotation)
                {
                    COSDictionary cosAnnot = ((PDAnnotation) object).getDictionary();
                    ContextHelper.validateElement(context, cosAnnot, ANNOTATIONS_PROCESS);
                }
            }
        }
        catch (IOException e)
        {
            if (e instanceof ValidationException)
            {
                throw (ValidationException) e;
            }
            // TODO IOException probably due to Encrypt
            throw new ValidationException("Unable to access Annotation", e);
        }
    }

    /**
     * Check that the group dictionary doesn't have a Transparency attribute
     * 
     * @param context
     * @param page
     * @throws ValidationException
     */
    protected void validateGroupTransparency(PreflightContext context, PDPage page) throws ValidationException
    {
        COSBase baseGroup = page.getCOSDictionary().getItem(XOBJECT_DICTIONARY_KEY_GROUP);
        COSDictionary groupDictionary = COSUtils.getAsDictionary(baseGroup, context.getDocument().getDocument());
        if (groupDictionary != null)
        {
            String sVal = groupDictionary.getNameAsString(COSName.S);
            if (XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY.equals(sVal))
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_TRANSPARENCY_GROUP,
                        "Group has a transparency S entry or the S entry is null."));
                return;
            }
        }
    }

}
