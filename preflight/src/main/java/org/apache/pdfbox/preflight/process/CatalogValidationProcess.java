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

import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_ADDITIONAL_ACTION;
import static org.apache.pdfbox.preflight.PreflightConstants.DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_LANG_NOT_RFC1766;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NOCATALOG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_Adobe;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR001;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR002;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR003;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR005;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR006;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR_001;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR_002;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR_003;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR_005;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_CGATS_TR_006;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_ERIMM;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_EUROSB104;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_EUROSB204;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA27;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA28;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA29;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA30;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA31;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA32;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA33;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA34;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA35;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA36;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA37;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA38;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA39;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA40;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA41;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA42;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA43;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA44;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA45;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA46;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_FOGRA47;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_IFRA26;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_JC200103;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_JC200104;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_JCN2002;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_JCW2003;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_RIMM;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_ROMM;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_bg_sRGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_eciRGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_opRGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_sRGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_sRGB_IEC;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_sYCC;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_scRGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_scRGB_nl;
import static org.apache.pdfbox.preflight.PreflightConstants.ICC_Characterization_Data_Registry_scYCC_nl;
import static org.apache.pdfbox.preflight.PreflightConstants.OUTPUT_INTENT_DICTIONARY_KEY_DEST_OUTPUT_PROFILE;
import static org.apache.pdfbox.preflight.PreflightConstants.OUTPUT_INTENT_DICTIONARY_KEY_INFO;
import static org.apache.pdfbox.preflight.PreflightConstants.OUTPUT_INTENT_DICTIONARY_KEY_OUTPUT_CONDITION_IDENTIFIER;
import static org.apache.pdfbox.preflight.PreflightConstants.OUTPUT_INTENT_DICTIONARY_KEY_S;
import static org.apache.pdfbox.preflight.PreflightConstants.OUTPUT_INTENT_DICTIONARY_VALUE_GTS_PDFA1;

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
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.persistence.util.COSObjectKey;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;

/**
 * This ValidationProcess check if the Catalog entries are confirming with the PDF/A-1b specification.
 */
public class CatalogValidationProcess extends AbstractProcess
{

    protected PDDocumentCatalog catalog;

    protected List<String> listICC = new ArrayList<String>();

    public CatalogValidationProcess()
    {
        listICC.add(ICC_Characterization_Data_Registry_FOGRA43);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR_006);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR006);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA39);
        listICC.add(ICC_Characterization_Data_Registry_JC200103);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA27);
        listICC.add(ICC_Characterization_Data_Registry_EUROSB104);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA45);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA46);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA41);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR_001);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR_003);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR_005);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR001);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR003);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR005);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA28);
        listICC.add(ICC_Characterization_Data_Registry_JCW2003);
        listICC.add(ICC_Characterization_Data_Registry_EUROSB204);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA47);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA44);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA29);
        listICC.add(ICC_Characterization_Data_Registry_JC200104);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA40);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA30);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA42);
        listICC.add(ICC_Characterization_Data_Registry_IFRA26);
        listICC.add(ICC_Characterization_Data_Registry_JCN2002);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR_002);
        listICC.add(ICC_Characterization_Data_Registry_CGATS_TR002);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA33);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA37);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA31);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA35);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA32);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA34);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA36);
        listICC.add(ICC_Characterization_Data_Registry_FOGRA38);
        listICC.add(ICC_Characterization_Data_Registry_sRGB);
        listICC.add(ICC_Characterization_Data_Registry_sRGB_IEC);
        listICC.add(ICC_Characterization_Data_Registry_Adobe);
        listICC.add(ICC_Characterization_Data_Registry_bg_sRGB);
        listICC.add(ICC_Characterization_Data_Registry_sYCC);
        listICC.add(ICC_Characterization_Data_Registry_scRGB);
        listICC.add(ICC_Characterization_Data_Registry_scRGB_nl);
        listICC.add(ICC_Characterization_Data_Registry_scYCC_nl);
        listICC.add(ICC_Characterization_Data_Registry_ROMM);
        listICC.add(ICC_Characterization_Data_Registry_RIMM);
        listICC.add(ICC_Characterization_Data_Registry_ERIMM);
        listICC.add(ICC_Characterization_Data_Registry_eciRGB);
        listICC.add(ICC_Characterization_Data_Registry_opRGB);
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

    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfbox = ctx.getDocument();
        this.catalog = pdfbox.getDocumentCatalog();

        if (this.catalog == null)
        {
            ctx.addValidationError(new ValidationError(ERROR_SYNTAX_NOCATALOG, "There are no Catalog entry in the Document."));
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
        ContextHelper.validateElement(ctx, catalog.getCOSDictionary(), ACTIONS_PROCESS);
        // AA entry if forbidden in PDF/A-1
        COSBase aa = catalog.getCOSDictionary().getItem(DICTIONARY_KEY_ADDITIONAL_ACTION);
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
                    "A Catalog shall not contain the OCPProperties entry."));
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
        COSBase cBase = catalog.getCOSDictionary().getItem(COSName.getPDFName(DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS));
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
                {// empty string is authorized (it may be an application specific value)
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
     * @param tmpDestOutputProfile
     * @param ctx
     * @return
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
            PDStream stream = PDStream.createFromCOS(COSUtils.getAsStream(destOutputProfile, cosDocument));
            if (stream == null)
            {
                addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                        "OutputIntent object uses a NULL Object"));
                return;
            }

            ICC_Profile iccp = ICC_Profile.getInstance(stream.getByteArray());
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
                    return;
                } // else OK
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
                return;
            } // else seems less than 2, so correct

            if (ctx.getIccProfileWrapper() == null)
            {
                ctx.setIccProfileWrapper(new ICCProfileWrapper(iccp));
            }

        }
        catch (IllegalArgumentException e)
        {
            // this is not a ICC_Profile
            addValidationError(ctx, new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID,
                    "DestOutputProfile isn't a valid ICCProfile. Caused by : " + e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to parse the ICC Profile.", e);
        }
            }
}
