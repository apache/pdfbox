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
import org.apache.padaf.preflight.graphics.ShadingPattern;
import org.apache.padaf.preflight.graphics.TilingPattern;
import org.apache.padaf.preflight.graphics.XObjFormValidator;
import org.apache.padaf.preflight.graphics.XObjImageValidator;
import org.apache.padaf.preflight.graphics.XObjPostscriptValidator;
import org.apache.padaf.preflight.graphics.XObjectValidator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @author eric
 * 
 */
public class GraphicsValidationHelper extends AbstractValidationHelper {

  public GraphicsValidationHelper(ValidatorConfig cfg)
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

    // ---- Checks all XObjects
    COSDocument cDoc = pdfDoc.getDocument();
    List<?> lCOSObj = cDoc.getObjects();
    for (Object o : lCOSObj) {
      COSObject cObj = (COSObject) o;
      COSBase cBase = cObj.getObject();
      if (cBase instanceof COSDictionary) {
        COSDictionary dic = (COSDictionary) cBase;
        String type = dic.getNameAsString(COSName
            .getPDFName(DICTIONARY_KEY_TYPE));
        if (type != null && DICTIONARY_KEY_XOBJECT.equals(type)) {
          result.addAll(validateXObject(handler, cObj));
        } else if (type != null && DICTIONARY_KEY_PATTERN.equals(type)) {
          result.addAll(validatePattern(handler, cObj));
        }
      }
    }
    return result;
  }

  public List<ValidationError> validatePattern(DocumentHandler handler,
      COSObject cObj) throws ValidationException {
    COSDictionary cosPattern = (COSDictionary) cObj.getObject();
    int ptype = cosPattern.getInt(DICTIONARY_KEY_PATTERN_TYPE);

    XObjectValidator validator = null;

    switch (ptype) {
    case DICTIONARY_PATTERN_TILING:
      validator = new TilingPattern(handler, (COSStream) cosPattern);
      break;
    case DICTIONARY_PATTERN_SHADING:
      validator = new ShadingPattern(handler, cosPattern);
      break;
    default:
      throw new ValidationException("Unkown pattern type : " + ptype);
    }

    return validator.validate();
  }

  public List<ValidationError> validateXObject(DocumentHandler handler,
      COSObject cObj) throws ValidationException {
    XObjectValidator xObjVal = null;

    // ---- According to the XObject subtype, the validation isn't processed by
    // the same Validator
    COSStream dic = (COSStream) cObj.getObject();
    String subtype = dic.getNameAsString(COSName
        .getPDFName(DICTIONARY_KEY_SUBTYPE));

    if (XOBJECT_DICTIONARY_VALUE_SUBTYPE_IMG.equals(subtype)) {
      xObjVal = new XObjImageValidator(handler, dic);
    } else if (XOBJECT_DICTIONARY_VALUE_SUBTYPE_FORM.equals(subtype)) {
      xObjVal = new XObjFormValidator(handler, dic);
    } else if (XOBJECT_DICTIONARY_VALUE_SUBTYPE_POSTSCRIPT.equals(subtype)) {
      xObjVal = new XObjPostscriptValidator(handler, dic);
    } else {
      throw new ValidationException("Invalid XObject subtype");
    }

    return xObjVal.validate();
  }

}
