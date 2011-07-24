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
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_L;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_RECT;
import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_SUBTYPE;

import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;

/**
 * Validation class for the LineAnnotation
 */
public class LineAnnotationValidator extends AnnotationValidator {
  /**
   * PDFBox object which wraps the annotation dictionary
   */
  protected PDAnnotationLine pdLine = null;

  public LineAnnotationValidator(DocumentHandler handler,
      COSDictionary annotDictionary) {
    super(handler, annotDictionary);
    this.pdLine = new PDAnnotationLine(annotDictionary);
    this.pdAnnot = this.pdLine;
  }

  /**
   * In addition of the AnnotationValidator.validate() method, this method
   * executes the the checkIColors method.
   * 
   * @see org.apache.padaf.preflight.annotation.AnnotationValidator#validate(java.util.List)
   */
  @Override
  public boolean validate(List<ValidationError> errors)
      throws ValidationException {
    boolean isValide = super.validate(errors);
    isValide = isValide && checkIColors(errors);
    return isValide;
  }

  /**
   * Return true if the IC field is present in the Annotation dictionary and if
   * the RGB profile is used in the DestOutputProfile of the OutputIntent
   * dictionary.
   * 
   * @param errors
   *          list of errors with is updated if no RGB profile is found when the
   *          IC element is present
   * @return
   */
  protected boolean checkIColors(List<ValidationError> errors) {
    if (this.pdLine.getInteriorColour() != null) {
      if (!searchRGBProfile()) {
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_ANNOT_FORBIDDEN_COLOR,"Annotation uses a Color profile which isn't the same than the profile contained by the OutputIntent"));
        return false;
      }
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
    boolean l = false;
    boolean f = false;
    boolean rect = false;

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
      if (cosName.equals(ANNOT_DICTIONARY_KEY_L)) {
        l = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_RECT)) {
        rect = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_F)) {
        f = true;
      }
    }
    /*
     * ---- After PDF 1.4, all additional entries in this annotation are
     * optional and they seem to be compatible with the PDF/A specification.
     */
    boolean result = (subtype && rect && f && l);
    if (!result) {
      errors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_ANNOT_MISSING_FIELDS));
    }
    return result;
  }
}
