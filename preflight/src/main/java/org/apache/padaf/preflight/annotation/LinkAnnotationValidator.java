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

import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_F;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_RECT;
import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_SUBTYPE;

import java.io.IOException;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;

/**
 * Validation class for the LinkAnnotation
 */
public class LinkAnnotationValidator extends AnnotationValidator {
  /**
   * PDFBox object which wraps the annotation dictionary
   */
  protected PDAnnotationLink pdLink = null;

  public LinkAnnotationValidator(DocumentHandler handler,
      COSDictionary annotDictionary) {
    super(handler, annotDictionary);
    this.pdLink = new PDAnnotationLink(annotDictionary);
    this.pdAnnot = this.pdLink;
  }

  /**
   * In addition of the AnnotationValidator.validate() method, this method
   * executes the the checkDest method.
   * 
   * @see org.apache.padaf.preflight.annotation.AnnotationValidator#validate(java.util.List)
   */
  @Override
  public boolean validate(List<ValidationError> errors)
      throws ValidationException {
    boolean isValide = super.validate(errors);
    isValide = isValide && checkDest(errors);
    return isValide;
  }

  /**
   * Check if the Dest element is authorized according to the A entry
   * 
   * @param errors
   * @return
   */
  protected boolean checkDest(List<ValidationError> errors) {
    try {
      PDDestination dest = this.pdLink.getDestination();
      if (dest != null) {
        // ---- check the if an A entry is present.
        if (this.pdLink.getAction() != null) {
          errors.add(new ValidationResult.ValidationError(
              ValidationConstants.ERROR_ANNOT_FORBIDDEN_DEST,
              "Dest can't be used due to A element"));
          return false;
        }
      }
    } catch (IOException e) {
      errors
          .add(new ValidationResult.ValidationError(
              ValidationConstants.ERROR_ANNOT_INVALID_DEST,
              "Dest can't be checked"));
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @seenet.awl.edoc.pdfa.validation.annotation.AnnotationValidator#
   * checkMandatoryFields(java.util.List)
   */
  protected boolean checkMandatoryFields(List<ValidationError> errors) {
    boolean subtype = false;
    boolean rect = false;
    boolean f = false;

    for (Object key : this.annotDictionary.keySet()) {
      if (!(key instanceof COSName)) {
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
            "Invalid key in The Annotation dictionary"));
        return false;
      }

      String cosName = ((COSName) key).getName();
      if (cosName.equals(DICTIONARY_KEY_SUBTYPE)) {
        subtype = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_RECT)) {
        rect = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_F)) {
        f = true;
      }
    }

    boolean result = (subtype && rect && f);
    if (!result) {
      errors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_ANNOT_MISSING_FIELDS));
    }
    return result;
  }
}
