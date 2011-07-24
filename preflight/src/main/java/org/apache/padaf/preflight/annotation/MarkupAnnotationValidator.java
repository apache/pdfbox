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

import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_CONTENTS;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_F;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_QUADPOINTS;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_KEY_RECT;
import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_SUBTYPE;

import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;

/**
 * Validation class for the MarkupAnnotation
 */
public class MarkupAnnotationValidator extends AnnotationValidator {
  /**
   * PDFBox object which wraps the annotation dictionary
   */
  protected PDAnnotationTextMarkup pdMarkup = null;

  public MarkupAnnotationValidator(DocumentHandler handler,
      COSDictionary annotDictionary) {
    super(handler, annotDictionary);
    this.pdMarkup = new PDAnnotationTextMarkup(annotDictionary);
    this.pdAnnot = this.pdMarkup;
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
    boolean contents = false;
    boolean qp = false;

    for (Object key : this.annotDictionary.keySet()) {
      if (!(key instanceof COSName)) {
        errors.add(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
            "Invalid key in The Annotation dictionary"));
        return false;
      }

      String cosName = ((COSName) key).getName();
      if (cosName.equals(ANNOT_DICTIONARY_KEY_RECT)) {
        rect = true;
      }
      if (cosName.equals(DICTIONARY_KEY_SUBTYPE)) {
        subtype = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_F)) {
        f = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_CONTENTS)) {
        contents = true;
      }
      if (cosName.equals(ANNOT_DICTIONARY_KEY_QUADPOINTS)) {
        qp = true;
      }
    }

    boolean result = (subtype && rect && f && contents && qp);
    if (!result) {
      errors.add(new ValidationResult.ValidationError(
          ValidationConstants.ERROR_ANNOT_MISSING_FIELDS));
    }
    return result;
  }
}
