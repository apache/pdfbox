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

package org.apache.padaf.preflight.font;

import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_SUBTYPE;
import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_TYPE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_FONTS_DICTIONARY_INVALID;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_COMPOSITE;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_MMTYPE;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TRUETYPE;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TYPE0;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TYPE0C;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TYPE1;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TYPE1C;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TYPE2;
import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_TYPE3;

import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;

/**
 * This class returns a FontValidator object according to the Font dictionary.
 */
public class FontValidatorFactory {

  public FontValidator getFontValidator(COSObject cObj,
      DocumentHandler handler) throws ValidationException {
    COSDictionary dic = (COSDictionary) cObj.getObject();
    String type = dic.getNameAsString(COSName.getPDFName(DICTIONARY_KEY_TYPE));
    String subtype = dic.getNameAsString(COSName
        .getPDFName(DICTIONARY_KEY_SUBTYPE));

    if ((type == null || "".equals(type))
        || (subtype == null || "".equals(subtype))) {
      throw new ValidationException("Type and/or Subtype keys are missing : "
          + ERROR_FONTS_DICTIONARY_INVALID);
    } else {
      if (FONT_DICTIONARY_VALUE_TRUETYPE.equals(subtype)) {
        return new TrueTypeFontValidator(handler, cObj);
      } else if (FONT_DICTIONARY_VALUE_MMTYPE.equals(subtype)
          || FONT_DICTIONARY_VALUE_TYPE1.equals(subtype)) {
        return new Type1FontValidator(handler, cObj);
      } else if (FONT_DICTIONARY_VALUE_TYPE3.equals(subtype)) {
        return new Type3FontValidator(handler, cObj);
      } else if (FONT_DICTIONARY_VALUE_COMPOSITE.equals(subtype)) {
       return new CompositeFontValidator(handler, cObj);
      } else if (FONT_DICTIONARY_VALUE_TYPE2.equals(subtype)
          || FONT_DICTIONARY_VALUE_TYPE1C.equals(subtype)
          || FONT_DICTIONARY_VALUE_TYPE0C.equals(subtype)
          || FONT_DICTIONARY_VALUE_TYPE0.equals(subtype)) {
        // ---- Font managed by a Composite font.
        // this dictionary will be checked by a CompositeFontValidator
        return null;
      } else {
        throw new ValidationException("Unknown font type : " + subtype);
      }
    }
  }
}
