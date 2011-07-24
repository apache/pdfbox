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

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

import static org.apache.padaf.preflight.ValidationConstants.*;

/**
 * This class processes commons validations of XObjects.
 */
public abstract class AbstractXObjValidator implements XObjectValidator {
  /**
   * The XObject to validate as a COSStream.
   */
  protected COSStream xobject = null;
  /**
   * The document handler which contains useful information to process
   * validation.
   */
  protected DocumentHandler handler = null;
  /**
   * The PDF document as COSDocument.
   */
  protected COSDocument cosDocument = null;

  public AbstractXObjValidator(DocumentHandler handler, COSStream xobj) {
    this.xobject = xobj;
    this.handler = handler;
    this.cosDocument = handler.getDocument().getDocument();
  }

  /**
   * This method checks the SMask entry in the XObject dictionary. According to
   * the PDF Reference, a SMask in a XObject is a Stream. So if it is not null,
   * it should be an error but a SMask with the name None is authorized in the
   * PDF/A Specification 6.4. If the validation fails (SMask not null and
   * different from None), the error list is updated with the error code
   * ERROR_GRAPHIC_TRANSPARENCY_SMASK (2.2.2).
   * 
   * @param error
   *          the list of error to update if the validation fails
   * @return true if the SMask is valid, false otherwise.
   */
  protected boolean checkSMask(List<ValidationError> error) {
    COSBase smask = xobject.getItem(COSName
        .getPDFName(TRANSPARENCY_DICTIONARY_KEY_SOFT_MASK));
    // 
    if (smask != null
        && !(COSUtils.isString(smask, cosDocument) && TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE
            .equals(COSUtils.getAsString(smask, cosDocument)))) {
      error.add(new ValidationError(ERROR_GRAPHIC_TRANSPARENCY_SMASK, "Soft Mask must be null or None"));
      return false;
    }

    return true;
  }

  /**
   * According the ISO 190005:1-2005 specification, a XObject can't have an OPI
   * entry in its dictionary. If the XObject has a OPI entry, the error list is
   * updated with the error code ERROR_GRAPHIC_UNEXPECTED_KEY (2.3).
   * 
   * @param errors
   *          the list of error to update if the validation fails.
   * @return true if the OPI is missing, false otherwise.
   */
  protected boolean checkOPI(List<ValidationError> errors) {
    // 6.2.4 and 6.2.5 no OPI
    if (this.xobject.getItem(COSName.getPDFName("OPI")) != null) {
      errors.add(new ValidationError(
          ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_KEY,
          "Unexpected 'OPI' Key"));
      return false;
    }
    return true;
  }

  /**
   * According the ISO 190005:1-2005 specification, a XObject can't have an Ref
   * entry in its dictionary. If the XObject has a Ref entry, the error list is
   * updated with the error code ERROR_GRAPHIC_UNEXPECTED_KEY (2.3).
   * 
   * @param errors
   *          the list of error to update if the validation fails.
   * @return true if the Ref is missing, false otherwise.
   */
  protected boolean checkReferenceXObject(List<ValidationError> errors) {
    // 6.2.6 No reference xobject
    if (this.xobject.getItem(COSName.getPDFName("Ref")) != null) {
      errors.add(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_KEY,
          "No reference Xobject allowed in PDF/A"));
      return false;
    }
    return true;
  }

  /**
   * According the ISO 190005:1-2005 specification, PostSCript XObject are
   * forbidden. If the XObject is a PostScript XObject, the error list is
   * updated with the error code ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY (2.3.2).
   * 
   * To know the if the object a Postscript XObject, "Subtype" and "Subtype2"
   * entries are checked.
   * 
   * @param errors
   *          the list of error to update if the validation fails.
   * @return true if the XObject isn't a Postscript XObject, false otherwise.
   */
  protected boolean checkPostscriptXObject(List<ValidationError> errors) {
    // 6.2.7 No PostScript XObjects
    if (this.xobject.getItem(COSName.SUBTYPE) != null
        && XOBJECT_DICTIONARY_VALUE_SUBTYPE_POSTSCRIPT.equals(this.xobject
            .getNameAsString(COSName.SUBTYPE))) {
      errors.add(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
          "No Postscript Xobject allowed in PDF/A"));
      return false;
    }
    if (this.xobject.getItem(COSName.getPDFName("Subtype2")) != null) {
      errors.add(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
          "No Postscript Xobject allowed in PDF/A (Subtype2)"));
      return false;
    }
    return true;
  }

  /**
   * This method checks if required fields are present.
   * 
   * @param result
   *          the list of error to update if the validation fails.
   * @return true if all fields are present, false otherwise.
   */
  protected abstract boolean checkMandatoryFields(List<ValidationError> result);

  /*
   * (non-Javadoc)
   * 
   * @see net.awl.edoc.pdfa.validation.graphics.XObjectValidator#validate()
   */
  public List<ValidationError> validate() throws ValidationException {
    List<ValidationError> result = new ArrayList<ValidationError>(0);
    if (checkMandatoryFields(result)) {
      checkOPI(result);
      checkSMask(result);
      checkReferenceXObject(result);
      checkPostscriptXObject(result);
    }
    return result;
  }
}
