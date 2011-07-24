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

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This helper validates FileSpec dictionaries
 */
public class FileSpecificationValidationHelper extends AbstractValidationHelper {

  public FileSpecificationValidationHelper(ValidatorConfig cfg)
  throws ValidationException {
	super(cfg);
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
    PDDocument pdfDoc = handler.getDocument();
    COSDocument cDoc = pdfDoc.getDocument();

    List<?> lCOSObj = cDoc.getObjects();
    for (Object o : lCOSObj) {
      COSObject cObj = (COSObject) o;

      // ---- If this object represents a Stream
      // The Dictionary must contain the Length key
      COSBase cBase = cObj.getObject();
      if (cBase instanceof COSDictionary) {
        COSDictionary dic = (COSDictionary) cBase;
        String type = dic.getNameAsString(COSName
            .getPDFName(DICTIONARY_KEY_TYPE));
        if (FILE_SPECIFICATION_VALUE_TYPE.equals(type)) {
          // ---- It is a file specification
          result.addAll(validateFileSpecification(handler, cObj));
        }
      }
    }
    return result;
  }

  /**
   * Validate a FileSpec dictionary, a FileSpec dictionary mustn't have the EF
   * (EmbeddedFile) entry.
   * 
   * @param handler
   *          The document handler
   * @param cObj
   *          the FileSpec Dictionary
   * @return
   */
  public List<ValidationError> validateFileSpecification(
      DocumentHandler handler, COSObject cObj) {
    List<ValidationError> result = new ArrayList<ValidationError>(0);
    COSDictionary fileSpec = (COSDictionary) cObj.getObject();

    // ---- Check dictionary entries
    // ---- Only the EF entry is forbidden
    if (fileSpec.getItem(COSName
        .getPDFName(FILE_SPECIFICATION_KEY_EMBEDDED_FILE)) != null) {
      result.add(new ValidationError(ERROR_SYNTAX_EMBEDDED_FILES,"EmbeddedFile entry is present in a FileSpecification dictionary"));
    }

    return result;
  }
}
