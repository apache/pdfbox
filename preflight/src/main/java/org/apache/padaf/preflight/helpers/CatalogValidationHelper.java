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

package org.apache.padaf.preflight.helpers;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.actions.AbstractActionManager;
import org.apache.padaf.preflight.graphics.ICCProfileWrapper;
import org.apache.padaf.preflight.utils.COSUtils;
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

/**
 * This helper validates the PDF file catalog
 */
public class CatalogValidationHelper extends AbstractValidationHelper {
	protected List<String> listICC = new ArrayList<String>();

	public CatalogValidationHelper(ValidatorConfig cfg)
	throws ValidationException {
		super(cfg);
		listICC.add(ICC_Characterization_Data_Registry_FOGRA43);
		listICC.add(ICC_Characterization_Data_Registry_CGATS_TR_006);
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

	protected boolean isStandardICCCharacterization(String name) {
		for (String iccStandard : listICC) {
			if (iccStandard.contains(name)) { // TODO check with an equal instead of contains?
				return true;
	}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.awl.edoc.pdfa.validation.helpers.AbstractValidationHelper#innerValidate
	 * (net.awl.edoc.pdfa.validation.DocumentHandler)
	 */
  @Override
  public List<ValidationError> innerValidate(DocumentHandler handler)
      throws ValidationException {
    List<ValidationError> result = new ArrayList<ValidationError>(0);
    PDDocument pdfbox = handler.getDocument();
    PDDocumentCatalog catalog = pdfbox.getDocumentCatalog();
    if (catalog != null) {
      validateActions(handler, catalog, result);
      validateLang(handler, catalog, result);
      validateNames(handler, catalog, result);
      validateOCProperties(handler, catalog, result);
    } else {
      throw new ValidationException(
          "There are no Catalog entry in the Document.");
    }

    // ---- Check OutputIntent to know the ICC Profile
    result.addAll(validateOutputIntent(handler));

    return result;
  }

  /**
   * This method validates if OpenAction entry contains forbidden action type.
   * It checks too if an Additional Action is present.
   * 
   * @param handler
   * @param catalog
   * @param result
   * @throws ValidationException
   */
  protected void validateActions(DocumentHandler handler,
      PDDocumentCatalog catalog, List<ValidationError> result)
      throws ValidationException {
    // ---- get OpenAction and Additional Action if these entries are present
    List<AbstractActionManager> lActions = this.actionFact.getActions(catalog
        .getCOSDictionary(), handler.getDocument().getDocument());
    for (AbstractActionManager action : lActions) {
      if (!action.valid(result)) {
        return;
      }
    }
  }

  /**
   * The Lang element is optional but it is recommended. This method check the
   * Syntax of the Lang if this entry is present.
   * 
   * @param handler
   * @param catalog
   * @param result
   * @throws ValidationException
   */
  protected void validateLang(DocumentHandler handler,
      PDDocumentCatalog catalog, List<ValidationError> result)
      throws ValidationException {
    String lang = catalog.getLanguage();
    if (lang != null && !lang.matches("[A-Za-z]{1,8}(-[A-Za-z]{1,8})*")) {
      result.add(new ValidationError(ERROR_SYNTAX_LANG_NOT_RFC1766));
    }
  }

  /**
   * A Catalog shall not contain the EmbeddedFiles entry.
   * 
   * @param handler
   * @param catalog
   * @param result
   * @throws ValidationException
   */
  protected void validateNames(DocumentHandler handler,
      PDDocumentCatalog catalog, List<ValidationError> result)
      throws ValidationException {
    PDDocumentNameDictionary names = catalog.getNames();
    if (names != null) {
      PDEmbeddedFilesNameTreeNode efs = names.getEmbeddedFiles();
      if (efs != null) {
        result.add(new ValidationError(
            ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES,"EmbeddedFile entry is present in the Names dictionary"));
      }
    }
  }

  /**
   * A Catalog shall not contain the OCPProperties (Optional Content Properties)
   * entry.
   * 
   * @param handler
   * @param catalog
   * @param result
   * @throws ValidationException
   */
  protected void validateOCProperties(DocumentHandler handler,
      PDDocumentCatalog catalog, List<ValidationError> result)
      throws ValidationException {
    COSBase ocp = catalog.getCOSDictionary().getItem(
        COSName.getPDFName(DOCUMENT_DICTIONARY_KEY_OPTIONAL_CONTENTS));
    if (ocp != null) {
      result
          .add(new ValidationError(ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES, "A Catalog shall not contain the OCPProperties entry."));
    }
  }

  /**
   * This method checks the content of each OutputIntent. The S entry must
   * contain GTS_PDFA1. The DestOuputProfile must contain a valid ICC Profile
   * Stream.
   * 
   * If there are more than one OutputIntent, they have to use the same ICC
   * Profile.
   * 
   * This method returns a list of ValidationError. It is empty if no errors
   * have been found.
   * 
   * @param handler
   * @return
   * @throws ValidationException
   */
  public List<ValidationError> validateOutputIntent(DocumentHandler handler)
      throws ValidationException {
    List<ValidationError> result = new ArrayList<ValidationError>(0);
    PDDocument pdDocument = handler.getDocument();
    PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
    COSDocument cDoc = pdDocument.getDocument();

    COSBase cBase = catalog.getCOSDictionary().getItem(COSName.getPDFName(DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS));
    COSArray outputIntents = COSUtils.getAsArray(cBase, cDoc);

    Map<COSObjectKey, Boolean> tmpDestOutputProfile = new HashMap<COSObjectKey, Boolean>();

    for (int i = 0; outputIntents != null && i < outputIntents.size(); ++i) {
		COSDictionary dictionary = COSUtils.getAsDictionary(outputIntents.get(i), cDoc);

      if (dictionary == null) {

        result.add(new ValidationError(
            ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
            "OutputIntent object is null or isn't a dictionary"));

      } else {
        // ---- S entry is mandatory and must be equals to GTS_PDFA1
			String sValue = dictionary.getNameAsString(COSName.getPDFName(OUTPUT_INTENT_DICTIONARY_KEY_S));
        if (!OUTPUT_INTENT_DICTIONARY_VALUE_GTS_PDFA1.equals(sValue)) {
				result.add(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID,
				"The S entry of the OutputIntent isn't GTS_PDFA1"));
          continue;
        }

        // ---- OutputConditionIdentifier is a mandatory field
        String outputConditionIdentifier = dictionary
			.getString(COSName.getPDFName(OUTPUT_INTENT_DICTIONARY_KEY_OUTPUT_CONDITION_IDENTIFIER));
			if (outputConditionIdentifier == null) {// empty string is autorized (it may be an application specific value)
				result.add(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
              "The OutputIntentCondition is missing"));
          continue;
        }

			// ---- If OutputConditionIdentifier is "Custom" or a non Standard ICC Characterization :
        // ---- DestOutputProfile and Info are mandatory
        // ---- DestOutputProfile must be a ICC Profile

			// ---- Because of PDF/A conforming file needs to specify the color characteristics, the DestOutputProfile
			// ---- is checked even if the OutputConditionIdentifier isn't "Custom"
			COSBase dop = dictionary.getItem(COSName.getPDFName(OUTPUT_INTENT_DICTIONARY_KEY_DEST_OUTPUT_PROFILE));
			ValidationError valer = validateICCProfile(dop, cDoc, tmpDestOutputProfile, handler);
        if (valer != null) {
          result.add(valer);
          continue;
        }

			if (!isStandardICCCharacterization(outputConditionIdentifier)) {
				String info = dictionary.getString(COSName.getPDFName(OUTPUT_INTENT_DICTIONARY_KEY_INFO));
          if (info == null || "".equals(info)) {
					result.add(new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
                "The Info entry of a OutputIntent dictionary is missing"));
            continue;
          }
        }
      }
    }
    return result;
  }

  /**
   * This method checks the destOutputProfile which must be a valid ICCProfile.
   * 
   * If an other ICCProfile exists in the mapDestOutputProfile, a
   * ValdiationError (ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE) is
   * returned because of only one profile is authorized. If the ICCProfile
   * already exist in the mapDestOutputProfile, the method returns null. If the
   * destOutputProfile contains an invalid ICCProfile, a ValidationError
   * (ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID) is returned If the
   * destOutputProfile is an empty stream, a
   * ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY) is returned.
   * 
   * If the destOutputFile is valid, mapDestOutputProfile is updated, the
   * ICCProfile is added to the document handler and null is returned.
   * 
   * @param destOutputProfile
   * @param cDoc
   * @param tmpDestOutputProfile
   * @param handler
   * @return
   * @throws ValidationException
   */
  protected ValidationError validateICCProfile(COSBase destOutputProfile,
      COSDocument cDoc, Map<COSObjectKey, Boolean> mapDestOutputProfile,
      DocumentHandler handler) throws ValidationException {
    try {
      if (destOutputProfile == null) {
        return new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
            "OutputIntent object uses a NULL Object");
      }

      // ---- destOutputProfile should be an instance of COSObject because of
      // this is a object reference
      if (destOutputProfile instanceof COSObject) {
        if (mapDestOutputProfile.containsKey(new COSObjectKey(
            (COSObject) destOutputProfile))) {
          // ---- the profile is already checked. continue
          return null;
        } else if (!mapDestOutputProfile.isEmpty()) {
          // ---- A DestOutputProfile exits but it isn't the same, error
          return new ValidationError(
              ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE, "More than one ICCProfile is defined");
        } 
        // else  the profile will be kept in the tmpDestOutputProfile if it is valid
      }

      PDStream stream = PDStream.createFromCOS(COSUtils.getAsStream(
          destOutputProfile, cDoc));
      if (stream == null) {
        return new ValidationError(ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY,
            "OutputIntent object uses a NULL Object");
      }

      ICC_Profile iccp = ICC_Profile.getInstance(stream.getByteArray());
      // check the ICC Profile version (6.2.2)
      if (iccp.getMajorVersion() == 2) {
        if (iccp.getMinorVersion() > 0x20) {
          // in PDF 1.4, max version is 02h.20h (meaning V 3.5)
          return new ValidationError(
              ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT, "Invalid version of the ICCProfile");
        } // else OK
      } else if (iccp.getMajorVersion() > 2) {
        // in PDF 1.4, max version is 02h.20h (meaning V 3.5)
        return new ValidationError(
            ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT, "Invalid version of the ICCProfile");
      } // else seems less than 2, so correct

      if (handler.getIccProfileWrapper() == null) {
        handler.setIccProfileWrapper(new ICCProfileWrapper(iccp));
      }

      // ---- keep reference to avoid multiple profile definition
      mapDestOutputProfile.put(new COSObjectKey((COSObject) destOutputProfile),
          true);

    } catch (IllegalArgumentException e) {
      // ---- this is not a ICC_Profile
      return new ValidationError(
          ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID, "DestOutputProfile isn't a ICCProfile");
    } catch (IOException e) {
      throw new ValidationException("Unable to parse the ICC Profile", e);
    }

    return null;
  }
}
