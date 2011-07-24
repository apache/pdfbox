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

package org.apache.padaf.preflight.annotation;

import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_CA;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_D;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_N;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_R;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_SYNTAX_DICT_INVALID;

import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.actions.AbstractActionManager;
import org.apache.padaf.preflight.actions.ActionManagerFactory;
import org.apache.padaf.preflight.graphics.ICCProfileWrapper;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;

public abstract class AnnotationValidator {

  protected AnnotationValidatorFactory annotFact = null;
  protected ActionManagerFactory actionFact = null;
  protected COSDocument cosDoc = null;
	
  protected DocumentHandler handler = null;
  /**
   * COSDictionary of the annotation
   */
  protected COSDictionary annotDictionary = null;
  /**
   * Instance of PDAnnotation built using the annotDictionary
   */
  protected PDAnnotation pdAnnot = null;

  public AnnotationValidator(DocumentHandler handler,
      COSDictionary annotDictionary) {
    super();
    this.handler = handler;
    this.cosDoc = handler.getDocument().getDocument();
    this.annotDictionary = annotDictionary;
  }

  /**
   * Checks if flags of the annotation are authorized.
   * <UL>
   * <li>Print flag must be 1
   * <li>NoView flag must be 0
   * <li>Hidden flag must be 0
   * <li>Invisible flag must be 0
   * </UL>
   * If one of these flags is invalid, the errors list is updated with the
   * ERROR_ANNOT_FORBIDDEN_FLAG ValidationError code.
   * 
   * @param errors
   *          list of errors which is updated if validation fails
   * @return false if a flag is invalid, true otherwise.
   */
  protected boolean checkFlags(List<ValidationError> errors) {
    boolean result = this.pdAnnot.isPrinted();
    result = result && !this.pdAnnot.isHidden();
    result = result && !this.pdAnnot.isInvisible();
    result = result && !this.pdAnnot.isNoView();
    if (!result) {
      errors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_ANNOT_FORBIDDEN_FLAG, "Flags of " +  pdAnnot.getSubtype() + " annotation are invalid"));
    }

    return result;
  }

  /**
   * Check if the CA value is 1.0. Return true if the CA element is missing or
   * if the value is 1.0. Return false otherwise and update the given list of
   * errors.
   * 
   * @param errors
   *          list of errors which is updated if validation fails
   * @return
   */
  protected boolean checkCA(List<ValidationError> errors) {
    COSBase ca = this.pdAnnot.getDictionary().getItem(
        COSName.getPDFName(ANNOT_DICTIONARY_KEY_CA));
    if (ca != null) {
      float caf = this.pdAnnot.getDictionary().getFloat(
          COSName.getPDFName(ANNOT_DICTIONARY_KEY_CA));
      if (caf != 1.0f) { // ---- Only 1.0 is authorized as value
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_ANNOT_INVALID_CA, "CA entry is invalid. Expected 1.0 / Read " + caf));
        return false;
      }
    } //else  optional field,  ok
    return true;
  }

  /**
   * Return true if the C field is present in the Annotation dictionary and if
   * the RGB profile is used in the DestOutputProfile of the OutputIntent
   * dictionary.
   * 
   * @param errors
   *          list of errors which is updated if no RGB profile is found when
   *          the C element is present
   * @return
   */
  protected boolean checkColors(List<ValidationError> errors) {
    if (this.pdAnnot.getColour() != null) {
      if (!searchRGBProfile()) {
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_ANNOT_FORBIDDEN_COLOR,"Annotation uses a Color profile which isn't the same than the profile contained by the OutputIntent"));
        return false;
      }
    }
    return true;
  }

  /**
   * Search the RGB Profile in OutputIntents dictionaries
   * 
   * @return true if a rgb profile is found, false otherwise.
   */
  protected boolean searchRGBProfile() {
    ICCProfileWrapper iccpw = this.handler.getIccProfileWrapper();
    if (iccpw != null) {
      return iccpw.isRGBColorSpace();
    }
    return false;
  }

  /**
   * This method checks the AP entry of the Annotation Dictionary. If the AP key
   * is missing, this method returns true. If the AP key exists, only the N
   * entry is authorized and must be a Stream which define the appearance of the
   * annotation. (Currently, only the type of the N entry is checked because of
   * the Appearance stream is a Form XObject, so it will be checked by the
   * Graphics Helper)
   * 
   * If the AP content isn't valid, this method return false and updates the
   * errors list.
   * 
   * @param errors
   *          list of errors which is updated if validation fails
   * @return
   */
  protected boolean checkAP(List<ValidationError> errors) {
    PDAppearanceDictionary ap = this.pdAnnot.getAppearance();
    if (ap != null) {
      COSDictionary apDict = ap.getDictionary(); 
      // ---- Only N entry is authorized
      if (apDict.getItem(COSName.getPDFName(ANNOT_DICTIONARY_KEY_D)) != null
          || apDict.getItem(
              COSName.getPDFName(ANNOT_DICTIONARY_KEY_R)) != null) {
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_ANNOT_INVALID_AP_CONTENT,
            "Only the N Appearance is authorized"));
        return false;
      } else if (apDict.getItem(
          COSName.getPDFName(ANNOT_DICTIONARY_KEY_N)) == null) {
        // ---- N entry is required
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_ANNOT_MISSING_AP_N_CONTENT,
            "The N Appearance must be present"));
        return false;
      } else {
        // ---- the N entry must be a Stream (Dictionaries are forbidden)
        COSBase apn = apDict.getItem(
            COSName.getPDFName(ANNOT_DICTIONARY_KEY_N));
        if (!COSUtils.isStream(apn, this.cosDoc)) {
          errors.add(new ValidationResult.ValidationError(
              ValidationConstants.ERROR_ANNOT_INVALID_AP_CONTENT,
              "The N Appearance must be a Stream"));
          return false;
        }
      }
    } //else  ok, nothing to check,this field is optional
    return true;
  }

  /**
   * Extract a list of ActionManager from the Annotation dictionary and valid
   * them. If an action is invalid, the errors list is updated and the method
   * returns false. Otherwise, the method returns true and the errors list
   * doesn't change.
   * 
   * @param errors
   *          list of errors which is updated if validation fails
   * @return
   * @throws ValidationException
   */
  protected boolean checkActions(List<ValidationError> errors)
      throws ValidationException {

	if ( actionFact == null) {
		return false;
	}

    List<AbstractActionManager> la = actionFact.getActions(annotDictionary, cosDoc);
    for (AbstractActionManager aMng : la) {
      if (!aMng.valid(errors)) {
        return false;
      }
    }
    return true;
  }

  /**
   * This method validates the Popup entry. This entry shall contain an other
   * Annotation. This annotation is validated with the right
   * AnnotationValidator.
   * 
   * @param errors
   * @return
   * @throws ValidationException
   */
  protected boolean checkPopup(List<ValidationError> errors)
      throws ValidationException {
    COSBase cosPopup = this.annotDictionary.getItem(COSName
        .getPDFName(ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP));
    if (cosPopup != null) {
      COSDictionary popupDict = COSUtils.getAsDictionary(cosPopup, this.cosDoc);
      if (popupDict == null) {
        errors
            .add(new ValidationError(
                ERROR_SYNTAX_DICT_INVALID,
                "An Annotation has a Popup entry, but the value is missing or isn't a dictionary"));
        return false;
      }
      AnnotationValidator popupVal = this.annotFact.getAnnotationValidator(popupDict, handler,
          errors);
      return popupVal.validate(errors);
    }
    return true;
  }

  /**
   * Execute validation of the Annotation dictionary.
   * 
   * @param errors
   *          list of errors which is updated if validation fails
   * @return true if validation succeed, false otherwise.
   * @throws ValidationException
   */
  public boolean validate(List<ValidationError> errors)
      throws ValidationException {
    boolean isValide = checkMandatoryFields(errors);
    isValide = isValide && checkFlags(errors);
    isValide = isValide && checkColors(errors);
    isValide = isValide && checkAP(errors);
    isValide = isValide && checkCA(errors);
    isValide = isValide && checkActions(errors);
    isValide = isValide && checkPopup(errors);
    return isValide;
  }

  /**
   * Checks if all mandatory fields of an annotation are present. If some fields
   * are missing, the method returns false and the errors list is updated.
   * 
   * @param errors
   *          list of errors which is updated if validation fails
   * @return true if validation succeed, false otherwise.
   */
  protected abstract boolean checkMandatoryFields(List<ValidationError> errors);

  /**
   * Initialize the annotFact attribute of this object.
   * This method must be called by the Factory at the creation of this object.
   * Only the Factory should call this method.
   *  
   * @param fact
   */
  public final void setFactory (AnnotationValidatorFactory fact) {
	  this.annotFact = fact;
  }

//  /**
//   * Return an instance of AnnotationValidator if the annotation subtype is
//   * authorized for a PDF/A. Otherwise, returns null and the given list is
//   * updated with the right error code.
//   * 
//   * If the subtype isn't mentioned in the PDF/A specification and if it doesn't
//   * exist in the PDF Reference 1.4, it will be considered as an invalid
//   * annotation. Here is the list of Annotations which appear after the PDF 1.4
//   * :
//   * <UL>
//   * <li>Polygon (1.5)
//   * <li>Polyline (1.5)
//   * <li>Caret (1.5)
//   * <li>Screen (1.5)
//   * <li>Watermark (1.6)
//   * <li>3D (1.6)
//   * </UL>
//   * 
//   * @param annotDic
//   * @param handler
//   * @param errors
//   * @return
//   */
//  public static AnnotationValidator getAnnotationValidator(
//      COSDictionary annotDic, DocumentHandler handler,
//      List<ValidationError> errors) {
//    AnnotationValidator av = null;
//
//    String subtype = annotDic.getNameAsString(COSName
//        .getPDFName(DICTIONARY_KEY_SUBTYPE));
//    if (subtype == null || "".equals(subtype)) {
//      errors.add(new ValidationError(ERROR_ANNOT_MISSING_SUBTYPE));
//    } else {
//      if (ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT.equals(subtype)) {
//        av = new TextAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK.equals(subtype)) {
//        av = new LinkAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT.equals(subtype)) {
//        av = new FreeTextAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE.equals(subtype)) {
//        av = new LineAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE.equals(subtype)
//          || ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE.equals(subtype)) {
//        av = new SquareCircleAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT.equals(subtype)
//          || ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE.equals(subtype)
//          || ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT.equals(subtype)
//          || ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUILGGLY.equals(subtype)) {
//        av = new MarkupAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP.equals(subtype)) {
//        av = new RubberStampAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_INK.equals(subtype)) {
//        av = new InkAnnotationValdiator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP.equals(subtype)) {
//        av = new PopupAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET.equals(subtype)) {
//        av = new WidgetAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK.equals(subtype)) {
//        av = new PrintMarkAnnotationValidator(handler, annotDic);
//      } else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET.equals(subtype)) {
//        av = new TrapNetAnnotationValidator(handler, annotDic);
//      } else {
//        errors.add(new ValidationError(ERROR_ANNOT_FORBIDDEN_SUBTYPE,
//            "The subtype isn't authorized : " + subtype));
//      }
//    }
//    return av;
//  }
}
