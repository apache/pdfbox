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

import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_TYPE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_BBOX;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_MISSING_FIELD;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_TRANSPARENCY_GROUP;
import static org.apache.padaf.preflight.ValidationConstants.PATTERN_KEY_SHADING;
import static org.apache.padaf.preflight.ValidationConstants.XOBJECT_DICTIONARY_KEY_BBOX;
import static org.apache.padaf.preflight.ValidationConstants.XOBJECT_DICTIONARY_KEY_GROUP;
import static org.apache.padaf.preflight.ValidationConstants.XOBJECT_DICTIONARY_KEY_GROUP_S;
import static org.apache.padaf.preflight.ValidationConstants.XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY;

import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.contentstream.ContentStreamWrapper;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;

/**
 * This class validates XObject with the Form subtype.
 */
public class XObjFormValidator extends AbstractXObjValidator {
  /**
   * High level object which represents the XObjectForm
   */
  PDXObjectForm pdXObj = null;

  public XObjFormValidator(DocumentHandler handler, COSStream xobj) {
    super(handler, xobj);
    this.pdXObj = new PDXObjectForm(xobj);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator#validate()
   */
  @Override
  public List<ValidationError> validate() throws ValidationException {
    List<ValidationError> result = super.validate();
    checkExtGraphicState(result);
    checkGroup(result);
    checkSubtype2Value(result);
    validateXObjectContent(result);
    validateShadingPattern(result);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @seenet.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator#
   * checkMandatoryFields(java.util.List)
   */
  @Override
  protected boolean checkMandatoryFields(List<ValidationError> result) {
    boolean lastMod = this.xobject.getItem(COSName.getPDFName("LastModified")) != null;
    boolean pieceInfo = this.xobject.getItem(COSName.getPDFName("PieceInfo")) != null;
    // type and subtype checked before to create the Validator.
    if (lastMod ^ pieceInfo) {
      result.add(new ValidationError(ERROR_GRAPHIC_MISSING_FIELD));
      return false;
    }

    COSBase bbBase = this.xobject.getItem(COSName
        .getPDFName(XOBJECT_DICTIONARY_KEY_BBOX));
    // ---- BBox is an Array (Rectangle)
    if (bbBase == null || !COSUtils.isArray(bbBase, cosDocument)) {
      result.add(new ValidationError(ERROR_GRAPHIC_INVALID_BBOX));
      return false;
    }
    return true;
  }

  /**
   * An Form XObject is a ContentStream. This method method uses an instance of
   * ContentStreamWrapper to check the Stream of this Form XObject.
   * 
   * @param result
   *          the list of error to update if the validation fails.
   * @return true if the validation succeed, false otherwise.
   * @throws ValidationException
   */
  protected boolean validateXObjectContent(List<ValidationError> result)
      throws ValidationException {

    ContentStreamWrapper csWrapper = new ContentStreamWrapper(handler);
    List<ValidationError> csParseErrors = csWrapper
        .validXObjContentStream(pdXObj);
    if (csParseErrors == null
        || (csParseErrors != null && csParseErrors.isEmpty())) {
      return true;
    }

    result.addAll(csParseErrors);
    return false;
  }

  /**
   * A Form XObject may contain a Group object (Key =" Group"). If a Group
   * object is present, this method checks if the S entry is present and if its
   * value is different from "Transparency".
   * 
   * @param error
   *          the list of error to update if the validation fails.
   * @return true if the validation succeed, false otherwise
   * @throws ValidationException
   */
  protected boolean checkGroup(List<ValidationError> error)
      throws ValidationException {
    COSBase baseGroup = this.xobject.getItem(COSName
        .getPDFName(XOBJECT_DICTIONARY_KEY_GROUP));

    COSDictionary groupDictionary = COSUtils.getAsDictionary(baseGroup,
        cosDocument);
    if (groupDictionary != null) {
      if (!XOBJECT_DICTIONARY_KEY_GROUP.equals(groupDictionary
          .getNameAsString(DICTIONARY_KEY_TYPE))) {
        throw new ValidationException(
            "The Group dictionary hasn't Group as Type value");
      }

      String sVal = groupDictionary
          .getNameAsString(XOBJECT_DICTIONARY_KEY_GROUP_S);
      if (sVal == null || XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY.equals(sVal)) {
        error.add(new ValidationError(ERROR_GRAPHIC_TRANSPARENCY_GROUP , "Group has a transparency S entry or the S entry is null."));
        return false;
      }
    }
    return true;
  }

  /**
   * Check the Extended Graphic State contains in the Form XObject if it is
   * present. To check this ExtGState, this method uses the
   * net.awl.edoc.pdfa.validation.graphics.ExtGStateContainer object.
   * 
   * @param errors
   *          the list of error to update if the validation fails
   * @return true is the ExtGState is missing or valid, false otherwise.
   * @throws ValidationException
   */
  protected boolean checkExtGraphicState(List<ValidationError> error)
      throws ValidationException {
    PDResources resources = this.pdXObj.getResources();
    if (resources != null) {
      ExtGStateContainer extContainer = new ExtGStateContainer(resources
          .getCOSDictionary(), this.cosDocument);
      return extContainer.validateTransparencyRules(error);
    }
    return true;
  }

  /**
   * This method check the Shading entry of the resource dictionary if exists.
   * To process this validation, an instance of ShadinPattern is used.
   * 
   * @param result
   *          the list of error to update if the validation fails
   * @return true if the validation succeed, false otherwise.
   * @throws ValidationException
   */
  protected boolean validateShadingPattern(List<ValidationError> result)
      throws ValidationException {
    PDResources resources = this.pdXObj.getResources();
    boolean res = true;
    if (resources != null) {
      COSDictionary shadings = (COSDictionary) resources.getCOSDictionary()
          .getDictionaryObject(PATTERN_KEY_SHADING);
      if (shadings != null) {
        for (Object key : shadings.keySet()) {
          COSDictionary aShading = (COSDictionary) shadings
              .getDictionaryObject((COSName) key);
          ShadingPattern sp = new ShadingPattern(handler, aShading);
          List<ValidationError> lErrors = sp.validate();
          if (lErrors != null && !lErrors.isEmpty()) {
            result.addAll(lErrors);
            res = false;
          }
        }
      }
    }
    return res;
  }

  /**
   * Check if there are no PS entry in the Form XObject dictionary
   * 
   * @param errors
   *          the list of error to update if the validation fails.
   * @return true if PS entry is missing, false otherwise
   */
  protected boolean checkPS(List<ValidationError> errors) {
    // 6.2.4 and 6.2.5 no PS
    if (this.xobject.getItem(COSName.getPDFName("PS")) != null) {
      errors.add(new ValidationError(
          ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_KEY,
          "Unexpected 'PS' Key"));
      return false;
    }
    return true;
  }

  /**
   * Check the SUbtype2 entry according to the §6.2.5 of the ISO 190005-1:2005
   * specification.
   * 
   * @param errors
   *          the list of error to update if the validation fails.
   * @return true if Subtype2 is missing or different from PS, false otherwise
   */
  protected boolean checkSubtype2Value(List<ValidationError> errors) {
    // 6.2.5 if Subtype2, value not PS
    if (this.xobject.getItem(COSName.getPDFName("Subtype2")) != null) {
      if ("PS".equals(this.xobject.getNameAsString(COSName
          .getPDFName("Subtype2")))) {
        errors.add(new ValidationError(
            ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
            "Unexpected 'PS' value for 'Subtype2' Key"));
        return false;
      }
    }
    return true;
  }
}
