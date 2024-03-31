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

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_LANG_NOT_RFC1766;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NOCATALOG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelper;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.ContextHelper;

/**
 * This ValidationProcess check if the Catalog entries are confirming with the PDF/A-1b specification.
 */
public class CatalogValidationProcess extends AbstractProcess
{

    private static final List<String> listICC = Arrays.asList(//
            "FOGRA43", "CGATS TR 006", "CGATS TR006", "FOGRA39", "JC200103", "FOGRA27", "EUROSB104",
            "FOGRA45", "FOGRA46", "FOGRA41", "CGATS TR 001", "CGATS TR001", "CGATS TR 003",
            "CGATS TR003", "CGATS TR 005", "CGATS TR005", "FOGRA28", "JCW2003", "EUROSB204",
            "FOGRA47", "FOGRA44", "FOGRA29", "JC200104", "FOGRA40", "FOGRA30", "FOGRA42", "IFRA26",
            "JCN2002", "CGATS TR 002", "CGATS TR002", "FOGRA33", "FOGRA37", "FOGRA31", "FOGRA35",
            "FOGRA32", "FOGRA34", "FOGRA36", "FOGRA38", "sRGB", "sRGB IEC61966-2.1",
            "Adobe RGB (1998)", "bg-sRGB", "sYCC", "scRGB", "scRGB-nl", "scYCC-nl", "ROMM RGB",
            "RIMM RGB", "ERIMM RGB", "eciRGB", "opRGB");

    private PDDocumentCatalog catalog;

    public CatalogValidationProcess()
    {
    }

    private boolean isStandardICCCharacterization(String name)
    {
        return listICC.stream().anyMatch(i -> i.contains(name));
    }

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfbox = ctx.getDocument();
        catalog = pdfbox.getDocumentCatalog();

        if (catalog == null)
        {
            ctx.addValidationError(new ValidationError(ERROR_SYNTAX_NOCATALOG, "There are no Catalog entry in the Document"));
        } 
        else 
        {
            validateActions(ctx);
            validateLang(ctx);
            validateNames(ctx);
            validateOCProperties(ctx);
            validateOutputIntent(ctx);
        }
    }

    /**
     * This method validates if OpenAction entry contains forbidden action type. It checks too if an Additional Action
     * is present.
     * 
     * @param ctx
     * @throws ValidationException Propagate the ActionManager exception
     */
    private void validateActions(PreflightContext ctx) throws ValidationException
    {
        ContextHelper.validateElement(ctx, catalog.getCOSObject(), ACTIONS_PROCESS);
        // AA entry if forbidden in PDF/A-1
        if (catalog.getCOSObject().containsKey(COSName.AA))
        {
            addValidationError(ctx, new ValidationError(ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION,
                    "The AA field is forbidden for the Catalog  when the PDF is a PDF/A"));
        }
    }

    /**
     * The Lang element is optional but it is recommended. This method check the Syntax of the Lang if this entry is
     * present.
     * 
     * @param ctx
     */
    private void validateLang(PreflightContext ctx)
    {
        String lang = catalog.getLanguage();
        if (lang != null && !lang.isEmpty() && !lang.matches("[A-Za-z]{1,8}(-[A-Za-z]{1,8})*"))
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_LANG_NOT_RFC1766));
        }
    }

    /**
     * A Catalog shall not contain the EmbeddedFiles entry.
     * 
     * @param ctx
     */
    private void validateNames(PreflightContext ctx)
    {
        PDDocumentNameDictionary names = catalog.getNames();
        if (names != null)
        {
            if (names.getEmbeddedFiles() != null)
            {
                addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES,
                        "EmbeddedFile entry is present in the Names dictionary"));
            }
            if (names.getJavaScript() != null)
            {
                addValidationError(ctx, new ValidationError(ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED,
                        "Javascript entry is present in the Names dictionary"));
            }
        }
    }

    /**
     * A Catalog shall not contain the OCPProperties (Optional Content Properties) entry.
     * 
     * @param ctx
     */
    private void validateOCProperties(PreflightContext ctx)
    {
        if (catalog.getOCProperties() != null)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES,
                    "A Catalog shall not contain the OCPProperties entry"));
        }
    }

    /**
     * This method checks the content of each OutputIntent. The S entry must contain GTS_PDFA1. The DestOutputProfile
     * must contain a valid ICC Profile Stream.
     * 
     * If there are more than one OutputIntent, they have to use the same ICC Profile.
     * 
     * This method returns a list of ValidationError. It is empty if no errors have been found.
     * 
     * @param ctx
     * @throws ValidationException
     */
    private void validateOutputIntent(PreflightContext ctx) throws ValidationException
    {
        COSArray outputIntents = catalog.getCOSObject().getCOSArray(COSName.OUTPUT_INTENTS);
        Map<COSObjectKey, Boolean> tmpDestOutputProfile = new HashMap<>();
        if (outputIntents == null)
        {
            return;
        }
        for (int i = 0; i < outputIntents.size(); ++i)
        {
            COSDictionary outputIntentDict = (COSDictionary) outputIntents.getObject(i);

            if (outputIntentDict == null)
            {
                addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                        "OutputIntent object is null or isn't a dictionary"));
            }
            else
            {
                // S entry is mandatory and must be equals to GTS_PDFA1
                COSName sValue = outputIntentDict.getCOSName(COSName.S);
                if (!COSName.GTS_PDFA1.equals(sValue))
                {
                    addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID,
                            "The S entry of the OutputIntent isn't GTS_PDFA1"));
                    continue;
                }

                // OutputConditionIdentifier is a mandatory field
                String outputConditionIdentifier = outputIntentDict
                        .getString(COSName.OUTPUT_CONDITION_IDENTIFIER);
                if (outputConditionIdentifier == null)
                {
                    // empty string is authorized (it may be an application specific value)
                    addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                            "The OutputIntentCondition is missing"));
                    continue;
                }

                /*
                 * If OutputConditionIdentifier is "Custom" or a non Standard ICC Characterization : DestOutputProfile
                 * and Info are mandatory DestOutputProfile must be a ICC Profile
                 * 
                 * Because of PDF/A conforming file needs to specify the color characteristics, the DestOutputProfile is
                 * checked even if the OutputConditionIdentifier isn't "Custom"
                 */
                COSBase destOutputProfile = outputIntentDict.getItem(COSName.DEST_OUTPUT_PROFILE);
                validateICCProfile(destOutputProfile, tmpDestOutputProfile, ctx);

                PreflightConfiguration config = ctx.getConfig();
                if (config.isLazyValidation() && !isStandardICCCharacterization(outputConditionIdentifier))
                {
                    String info = outputIntentDict.getString(COSName.INFO);
                    if (info == null || info.isEmpty())
                    {
                        ValidationError error = new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                                "The Info entry of a OutputIntent dictionary is missing");
                        error.setWarning(true);
                        addValidationError(ctx, error);
                    }
                }
            }
        }
    }

    /**
     * This method checks the destOutputProfile which must be a valid ICCProfile.
     * 
     * If another ICCProfile exists in the mapDestOutputProfile, a ValidationError
     * (ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE) is returned because only one profile is authorized. If the
     * ICCProfile already exists in the mapDestOutputProfile, the method returns null. If the destOutputProfile contains
     * an invalid ICCProfile, a ValidationError (ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID) is returned. If the
     * destOutputProfile is an empty stream, a ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY) is returned.
     * 
     * If the destOutputFile is valid, mapDestOutputProfile is updated, the ICCProfile is added to the document ctx and
     * null is returned.
     * 
     * @param destOutputProfile
     * @param mapDestOutputProfile
     * @param ctx the preflight context.
     * @throws ValidationException
     */
    private void validateICCProfile(COSBase destOutputProfile,
            Map<COSObjectKey, Boolean> mapDestOutputProfile, PreflightContext ctx)
            throws ValidationException
    {
        try
        {
            // destOutputProfile should be an instance of COSObject because of this is a object reference
            if (!(destOutputProfile instanceof COSObject))
            {
                addValidationError(ctx,
                        new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                                "OutputIntent object should be a reference: " + destOutputProfile));
                return;
            }

            COSObject cosObj = (COSObject) destOutputProfile;
            COSObjectKey key = cosObj.getKey();
            if (mapDestOutputProfile.containsKey(key))
            {
                // the profile is already checked. continue
                return;
            }
            else if (!mapDestOutputProfile.isEmpty())
            {
                // A DestOutputProfile exits but it isn't the same, error
                addValidationError(ctx,
                        new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE,
                                "More than one ICCProfile is defined: " + destOutputProfile));
                return;
            }

            // keep reference to avoid multiple profile definition
            mapDestOutputProfile.put(key, true);

            COSBase localDestOutputProfile = cosObj.getObject();
            if (!(localDestOutputProfile instanceof COSStream))
            {
                addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                        "OutputIntent object must be a stream"));
                return;
            }
            COSStream stream = (COSStream) localDestOutputProfile;

            COSArray array = new COSArray();
            array.add(COSName.ICCBASED);
            array.add(stream);
            PDICCBased iccBased = PDICCBased.create(array, null);
            PreflightConfiguration cfg = ctx.getConfig();
            ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
            ColorSpaceHelper csHelper =
                    csFact.getColorSpaceHelper(ctx, iccBased, ColorSpaceRestriction.NO_RESTRICTION);
            csHelper.validate();

            if (ctx.getIccProfileWrapper() == null)
            {
                try (InputStream is = stream.createInputStream())
                {
                    ctx.setIccProfileWrapper(new ICCProfileWrapper(ICC_Profile.getInstance(is)));
                }
            }
        }
        catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e)
        {
            // this is not a ICC_Profile
            addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID,
                    "DestOutputProfile isn't a valid ICCProfile: " + e.getMessage(), e));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to parse the ICC Profile.", e);
        }
    }
}
