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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;


import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.*;

/**
 * This ValidationProcess check if the Catalog entries are confirming with the PDF/A-1b specification.
 */
public class CatalogValidationProcess extends AbstractProcess
{

    protected PDDocumentCatalog catalog;

    protected List<String> listICC = new ArrayList<String>();

    public CatalogValidationProcess()
    {
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA43);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR_006);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR006);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA39);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_JC200103);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA27);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_EUROSB104);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA45);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA46);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA41);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR_001);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR_003);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR_005);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR001);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR003);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR005);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA28);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_JCW2003);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_EUROSB204);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA47);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA44);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA29);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_JC200104);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA40);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA30);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA42);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_IFRA26);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_JCN2002);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR_002);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_CGATS_TR002);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA33);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA37);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA31);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA35);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA32);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA34);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA36);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_FOGRA38);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_sRGB);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_sRGB_IEC);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_Adobe);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_bg_sRGB);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_sYCC);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_scRGB);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_scRGB_nl);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_scYCC_nl);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_ROMM);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_RIMM);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_ERIMM);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_eciRGB);
        listICC.add(ICC_CHARACTERIZATION_DATA_REGISTRY_opRGB);
    }

    protected boolean isStandardICCCharacterization(String name)
    {
        for (String iccStandard : listICC)
        {
            if (iccStandard.contains(name))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfbox = ctx.getDocument();
        this.catalog = pdfbox.getDocumentCatalog();

        if (this.catalog == null)
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
     * @throws ValidationException
     *             Propagate the ActionManager exception
     */
    protected void validateActions(PreflightContext ctx) throws ValidationException
    {
        ContextHelper.validateElement(ctx, catalog.getCOSObject(), ACTIONS_PROCESS);
        // AA entry if forbidden in PDF/A-1
        COSBase aa = catalog.getCOSObject().getItem(DICTIONARY_KEY_ADDITIONAL_ACTION);
        if (aa != null)
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
     * @throws ValidationException
     */
    protected void validateLang(PreflightContext ctx) throws ValidationException
    {
        String lang = catalog.getLanguage();
        if (lang != null && !"".equals(lang) && !lang.matches("[A-Za-z]{1,8}(-[A-Za-z]{1,8})*"))
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_LANG_NOT_RFC1766));
        }
    }

    /**
     * A Catalog shall not contain the EmbeddedFiles entry.
     * 
     * @param ctx
     * @throws ValidationException
     */
    protected void validateNames(PreflightContext ctx) throws ValidationException
    {
        PDDocumentNameDictionary names = catalog.getNames();
        if (names != null)
        {
            PDEmbeddedFilesNameTreeNode efs = names.getEmbeddedFiles();
            if (efs != null)
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
     * @throws ValidationException
     */
    protected void validateOCProperties(PreflightContext ctx) throws ValidationException
    {
        if (catalog.getOCProperties() != null)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES,
                    "A Catalog shall not contain the OCPProperties entry"));
        }
    }

    /**
     * This method checks the content of each OutputIntent. The S entry must contain GTS_PDFA1. The DestOuputProfile
     * must contain a valid ICC Profile Stream.
     * 
     * If there are more than one OutputIntent, they have to use the same ICC Profile.
     * 
     * This method returns a list of ValidationError. It is empty if no errors have been found.
     * 
     * @param ctx
     * @throws ValidationException
     */
    public void validateOutputIntent(PreflightContext ctx) throws ValidationException
    {
        COSDocument cosDocument = ctx.getDocument().getDocument();
        COSBase cBase = catalog.getCOSObject().getItem(COSName.getPDFName(DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS));
        COSArray outputIntents = COSUtils.getAsArray(cBase, cosDocument);

        Map<COSObjectKey, Boolean> tmpDestOutputProfile = new HashMap<COSObjectKey, Boolean>();
        for (int i = 0; outputIntents != null && i < outputIntents.size(); ++i)
        {
            COSDictionary outputIntentDict = COSUtils.getAsDictionary(outputIntents.get(i), cosDocument);

            if (outputIntentDict == null)
            {
                addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                        "OutputIntent object is null or isn't a dictionary"));
            }
            else
            {
                // S entry is mandatory and must be equals to GTS_PDFA1
                String sValue = outputIntentDict.getNameAsString(OUTPUT_INTENT_DICTIONARY_KEY_S);
                if (!OUTPUT_INTENT_DICTIONARY_VALUE_GTS_PDFA1.equals(sValue))
                {
                    addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID,
                            "The S entry of the OutputIntent isn't GTS_PDFA1"));
                    continue;
                }

                // OutputConditionIdentifier is a mandatory field
                String outputConditionIdentifier = outputIntentDict
                        .getString(OUTPUT_INTENT_DICTIONARY_KEY_OUTPUT_CONDITION_IDENTIFIER);
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
                COSBase destOutputProfile = outputIntentDict.getItem(OUTPUT_INTENT_DICTIONARY_KEY_DEST_OUTPUT_PROFILE);
                validateICCProfile(destOutputProfile, tmpDestOutputProfile, ctx);

                PreflightConfiguration config = ctx.getConfig();
                if (config.isLazyValidation() && !isStandardICCCharacterization(outputConditionIdentifier))
                {
                    String info = outputIntentDict.getString(COSName.getPDFName(OUTPUT_INTENT_DICTIONARY_KEY_INFO));
                    if (info == null || "".equals(info))
                    {
                        ValidationError error = new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                                "The Info entry of a OutputIntent dictionary is missing");
                        error.setWarning(true);
                        addValidationError(ctx, error);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * This method checks the destOutputProfile which must be a valid ICCProfile.
     * 
     * If an other ICCProfile exists in the mapDestOutputProfile, a ValdiationError
     * (ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE) is returned because of only one profile is authorized. If the
     * ICCProfile already exist in the mapDestOutputProfile, the method returns null. If the destOutputProfile contains
     * an invalid ICCProfile, a ValidationError (ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID) is returned If the
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
    protected void validateICCProfile(COSBase destOutputProfile, Map<COSObjectKey, Boolean> mapDestOutputProfile,
            PreflightContext ctx) throws ValidationException
            {
        try
        {
            if (destOutputProfile == null)
            {
                return;
            }

            // destOutputProfile should be an instance of COSObject because of this is a object reference
            if (destOutputProfile instanceof COSObject)
            {
                if (mapDestOutputProfile.containsKey(new COSObjectKey((COSObject) destOutputProfile)))
                {
                    // the profile is already checked. continue
                    return;
                }
                else if (!mapDestOutputProfile.isEmpty())
                {
                    // A DestOutputProfile exits but it isn't the same, error
                    addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE,
                            "More than one ICCProfile is defined"));
                    return;
                }
                // else the profile will be kept in the tmpDestOutputProfile if it is valid
            }

            // keep reference to avoid multiple profile definition
            mapDestOutputProfile.put(new COSObjectKey((COSObject) destOutputProfile), true);
            COSDocument cosDocument = ctx.getDocument().getDocument();
            COSStream stream = COSUtils.getAsStream(destOutputProfile, cosDocument);
            if (stream == null)
            {
                addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                        "OutputIntent object uses a NULL Object"));
                return;
            }

            ICC_Profile iccp = ICC_Profile.getInstance(stream.createInputStream());
            
            if (!validateICCProfileNEntry(stream, ctx, iccp))
            {
                return;
            }
            if (!validateICCProfileVersion(iccp, ctx))
            {
                return;
            }
            if (ctx.getIccProfileWrapper() == null)
            {
                ctx.setIccProfileWrapper(new ICCProfileWrapper(iccp));
            }
        }
        catch (IllegalArgumentException e)
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

    private boolean validateICCProfileVersion(ICC_Profile iccp, PreflightContext ctx)
    {
        PreflightConfiguration config = ctx.getConfig();

        // check the ICC Profile version (6.2.2)
        if (iccp.getMajorVersion() == 2)
        {
            if (iccp.getMinorVersion() > 0x40)
            {
                // in PDF 1.4, max version is 02h.40h (meaning V 3.5)
                // see the ICCProfile specification (ICC.1:1998-09)page 13 - §6.1.3 :
                // The current profile version number is "2.4.0" (encoded as 02400000h")
                ValidationError error = new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT,
                        "Invalid version of the ICCProfile");
                error.setWarning(config.isLazyValidation());
                addValidationError(ctx, error);
                return false;
            }
            // else OK
        }
        else if (iccp.getMajorVersion() > 2)
        {
            // in PDF 1.4, max version is 02h.40h (meaning V 3.5)
            // see the ICCProfile specification (ICC.1:1998-09)page 13 - §6.1.3 :
            // The current profile version number is "2.4.0" (encoded as 02400000h"
            ValidationError error = new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT,
                    "Invalid version of the ICCProfile");
            error.setWarning(config.isLazyValidation());
            addValidationError(ctx, error);
            return false;
        }
        // else seems less than 2, so correct
        return true;
    }

    private boolean validateICCProfileNEntry(COSStream stream, PreflightContext ctx, ICC_Profile iccp)
    {
        COSDictionary streamDict = (COSDictionary) stream.getCOSObject();
        if (!streamDict.containsKey(COSName.N))
        {
            addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile is mandatory"));
            return false;
        }
        COSBase nValue = streamDict.getItem(COSName.N);
        if (!(nValue instanceof COSNumber))
        {
            addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile must be a number, but is " + nValue));
            return false;
        }
        int nNumberValue = ((COSNumber) nValue).intValue();
        if (nNumberValue != 1 && nNumberValue != 3 && nNumberValue != 4)
        {
            addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile must be 1, 3 or 4, but is " + nNumberValue));
            return false;
        }
        if (iccp.getNumComponents() != nNumberValue)
        {
            addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                    "/N entry of ICC profile is " + nNumberValue + " but the ICC profile has " + iccp.getNumComponents() + " components"));
            return false;
        }
        return true;
    }
}
