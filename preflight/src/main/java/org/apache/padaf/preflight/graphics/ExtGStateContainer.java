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

package org.apache.padaf.preflight.graphics;

import static org.apache.padaf.preflight.ValidationConstants.ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_TRANSPARENCY_EXT_GS_CA;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK;
import static org.apache.padaf.preflight.ValidationConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE;
import static org.apache.padaf.preflight.ValidationConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE_ENTRY_REGEX;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_KEY_BLEND_MODE;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_KEY_LOWER_CA;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_KEY_SOFT_MASK;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_KEY_UPPER_CA;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_VALUE_BM_COMPATIBLE;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_VALUE_BM_NORMAL;
import static org.apache.padaf.preflight.ValidationConstants.TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE;

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.padaf.preflight.utils.RenderingIntents;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;

/**
 * This class wraps a list of COSDictionary which represent an extended graphics
 * state dictionary.
 */
public class ExtGStateContainer {
  /**
   * The COSDocument which contains the extended graphic states.
   */
  private COSDocument cDoc = null;
  /**
   * A list of Extended Graphic States.
   */
  private List<COSDictionary> listOfExtGState = new ArrayList<COSDictionary>(0);

  /**
   * Create an instance of ExtGStateContainer using a Resource dictionary and a
   * COSDocument. This constructor initializes the listOfExtGState attribute
   * using the given Resource dictionary and the COSDocument.
   * 
   * @param resources
   *          a resource COSDictionary
   * @param cDoc
   *          the COSDocument which contains the Resource dictionary
   * @throws ValidationException
   *           thrown if a the Extended Graphic State isn't valid
   */
  public ExtGStateContainer(COSDictionary resources, COSDocument cDoc)
      throws ValidationException {
    this.cDoc = cDoc;
    COSBase egsEntry = resources.getItem(COSName
        .getPDFName(TRANPARENCY_DICTIONARY_KEY_EXTGSTATE));
    COSDictionary extGStates = COSUtils.getAsDictionary(egsEntry, cDoc);

    if (extGStates != null) {
      for (Object object : extGStates.keySet()) {
        COSName key = (COSName) object;
        if (key.getName().matches(
            TRANPARENCY_DICTIONARY_KEY_EXTGSTATE_ENTRY_REGEX)) {
          COSBase gsBase = extGStates.getItem(key);
          COSDictionary gsDict = COSUtils.getAsDictionary(gsBase, cDoc);
          if (gsDict == null) {
            throw new ValidationException(
                "The Extended Graphics State dictionary is invalid");
          }
          this.listOfExtGState.add(gsDict);
        }
      }
    } 
    //else  if there are no ExtGState, the list will be empty.
    
  }

  /**
   * Validate all ExtGState dictionaries of this container
   * 
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if all Graphic States are valid, false otherwise.
   */
  public boolean validateTransparencyRules(List<ValidationError> error) {
    boolean res = true;

    for (COSDictionary egs : listOfExtGState) {
      res = res && checkSoftMask(egs, error);
      res = res && checkCA(egs, error);
      res = res && checkBlendMode(egs, error);
      res = res && checkTRKey(egs, error);
      res = res && checkTR2Key(egs, error);
    }

    return res;
  }

  /**
   * This method checks the SMask value of the ExtGState dictionary. The Soft
   * Mask is optional but must be "None" if it is present.
   * 
   * @param egs
   *          the Graphic state to check
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if SMask is missing or equals to None
   */
  private boolean checkSoftMask(COSDictionary egs, List<ValidationError> error) {
    COSBase smVal = egs.getItem(COSName
        .getPDFName(TRANSPARENCY_DICTIONARY_KEY_SOFT_MASK));
    if (smVal != null) {
      // ---- Soft Mask is valid only if it is a COSName equals to None
      if (!(smVal instanceof COSName && TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE
          .equals(((COSName) smVal).getName()))) {
        error.add(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK, "SoftMask must be null or None"));
        return false;
      }
    }
    return true;
  }

  /**
   * This method checks the BM value of the ExtGState dictionary. The Blend Mode
   * is optional but must be "Normal" or "Compatible" if it is present.
   * 
   * @param egs
   *          the graphic state to check
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if BM is missing or equals to "Normal" or "Compatible"
   */
  private boolean checkBlendMode(COSDictionary egs, List<ValidationError> error) {
    COSBase bmVal = egs.getItem(COSName
        .getPDFName(TRANSPARENCY_DICTIONARY_KEY_BLEND_MODE));
    if (bmVal != null) {
      // ---- Blend Mode is valid only if it is equals to Normal or Compatible
      if (!(bmVal instanceof COSName && (TRANSPARENCY_DICTIONARY_VALUE_BM_NORMAL
          .equals(((COSName) bmVal).getName()) || TRANSPARENCY_DICTIONARY_VALUE_BM_COMPATIBLE
          .equals(((COSName) bmVal).getName())))) {
        error.add(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE,"BlendMode value isn't valid (only Normal and Compatible are authorized)"));
        return false;
      }
    }
    return true;
  }

  /**
   * This method checks the "CA" and "ca" values of the ExtGState dictionary.
   * They are optional but must be 1.0 if they are present.
   * 
   * @param egs
   *          the graphic state to check
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if CA/ca is missing or equals to 1.0
   */
  private boolean checkCA(COSDictionary egs, List<ValidationError> error) {
    COSBase uCA = egs.getItem(COSName
        .getPDFName(TRANSPARENCY_DICTIONARY_KEY_UPPER_CA));
    COSBase lCA = egs.getItem(COSName
        .getPDFName(TRANSPARENCY_DICTIONARY_KEY_LOWER_CA));
    if (uCA != null) {
      // ---- If CA is present only the value 1.0 is authorized
      Float fca = COSUtils.getAsFloat(uCA, cDoc);
      Integer ica = COSUtils.getAsInteger(uCA, cDoc);
      if (!(fca != null && fca == 1.0f) && !(ica != null && ica == 1)) {
        error.add(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_CA,"CA entry in a ExtGState is invalid"));
        return false;
      }
    }

    if (lCA != null) {
      // ---- If ca is present only the value 1.0 is authorized
      Float fca = COSUtils.getAsFloat(lCA, cDoc);
      Integer ica = COSUtils.getAsInteger(lCA, cDoc);
      if (!(fca != null && fca == 1.0f) && !(ica != null && ica == 1)) {
        error.add(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_CA,"ca entry in a ExtGState  is invalid."));
        return false;
      }
    }
    return true;
  }

  /**
   * Check the TR entry. A valid ExtGState hasn't TR entry.
   * 
   * @param egs
   *          the graphic state to check
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if TR entry is missing, false otherwise.
   */
  protected boolean checkTRKey(COSDictionary egs, List<ValidationError> error) {
    if (egs.getItem(COSName.getPDFName("TR")) != null) {
      error.add(new ValidationError(
          ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_KEY,
          "No TR key expected in Extended graphics state"));
      return false;
    }
    return true;
  }

  /**
   * Check the TR2 entry. A valid ExtGState hasn't TR2 entry or a TR2 entry
   * equals to "default".
   * 
   * @param egs
   *          the graphic state to check
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if TR2 entry is missing or equals to "default", false
   *         otherwise.
   */
  protected boolean checkTR2Key(COSDictionary egs, List<ValidationError> error) {
    if (egs.getItem(COSName.getPDFName("TR2")) != null) {
      String s = egs.getNameAsString(COSName.getPDFName("TR2"));
      if (!"default".equals(s)) {
        error.add(new ValidationError(
            ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
            "TR2 key only expect 'default' value, not '" + s + "'"));
        return false;
      }
    }
    return true;
  }

  /**
   * Check the RI entry of the Graphic State. If the rendering intent entry is
   * present, the value must be one of the four values defined in the PDF
   * reference. (@see net.awl.edoc.pdfa.validation.utils.RenderingIntents)
   * 
   * @param egs
   *          the graphic state to check
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if RI entry is valid, false otherwise.
   */
  protected boolean checkRIKey(COSDictionary egs, List<ValidationError> error) {
    String rendenringIntent = egs.getNameAsString(COSName.getPDFName("RI"));
    if (rendenringIntent != null && !"".equals(rendenringIntent)
        && !RenderingIntents.contains(rendenringIntent)) {
      error.add(new ValidationError(
          ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
          "Invalid rendering intent value in Extended graphics state"));
      return false;
    }
    return true;
  }
}
