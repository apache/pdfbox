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


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;

public abstract class SimpleFontValidator extends AbstractFontValidator {
  protected String basefont;
  protected COSBase firstChar;
  protected COSBase lastChar;
  protected COSBase widths;
  protected COSBase encoding;
  protected COSBase toUnicode;
  /**
   * The PdfBox font descriptor dictionary wrapper.
   */
  protected PDFontDescriptorDictionary pFontDesc = null;
  /**
   * The font descriptor dictionary linked with the font dictionary
   */
  protected COSDictionary fDescriptor = null;

  public SimpleFontValidator(DocumentHandler handler, COSObject obj)
      throws ValidationException {
    super(handler, obj);
    COSBase tmpFontDesc = fDictionary.getItem(COSName
        .getPDFName(FONT_DICTIONARY_KEY_FONT_DESC));
    this.fDescriptor = COSUtils.getAsDictionary(tmpFontDesc, handler
        .getDocument().getDocument());
    if (this.fDescriptor != null) {
      this.pFontDesc = new PDFontDescriptorDictionary(this.fDescriptor);
    }
  }

  /**
   * Extract element from the COSObject to avoid useless access to this object.
   */
  private void extractElementsToCheck() {
    // ---- Here is required elements
    this.basefont = fDictionary.getNameAsString(COSName
        .getPDFName(FONT_DICTIONARY_KEY_BASEFONT));
    this.firstChar = fDictionary.getItem(COSName
        .getPDFName(FONT_DICTIONARY_KEY_FIRSTCHAR));
    this.lastChar = fDictionary.getItem(COSName
        .getPDFName(FONT_DICTIONARY_KEY_LASTCHAR));
    this.widths = fDictionary.getItem(COSName
        .getPDFName(FONT_DICTIONARY_KEY_WIDTHS));
    // ---- Here is optional elements
    this.encoding = fDictionary.getItem(COSName
        .getPDFName(FONT_DICTIONARY_KEY_ENCODING));
    this.toUnicode = fDictionary.getItem(COSName
        .getPDFName(FONT_DICTIONARY_KEY_TOUNICODE));
  }

  /**
   * Check if All required fields of a Font Dictionary are present. If there are
   * some missing fields, this method returns false and the FontContainer is
   * updated.
   * 
   * @return
   */
  protected boolean checkMandatoryFields() {
    String type = fDictionary.getNameAsString(COSName
        .getPDFName(DICTIONARY_KEY_TYPE));
    String subtype = fDictionary.getNameAsString(COSName
        .getPDFName(DICTIONARY_KEY_SUBTYPE));

    if (this.fDescriptor == null) {
      this.fontContainer
          .addError(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID,
              "The FontDescriptor is missing, so the Font Program isn't embedded."));
      this.fontContainer.setFontProgramEmbedded(false);
      return false;
    }

    if ((type == null || "".equals(type))
        || (subtype == null || "".equals(subtype))) {
      this.fontContainer.addError(new ValidationError(
          ERROR_FONTS_DICTIONARY_INVALID,
          "Type and/or Subtype keys are missing"));
      return false;
    } else {
      extractElementsToCheck();
      // ---- according to the subtype, the validation process isn't the same.
      return checkSpecificMandatoryFields();
    }
  }

  /**
   * This method checks the presence of some fields according to the Font type
   * 
   * @return
   */
  protected abstract boolean checkSpecificMandatoryFields();

  /**
   * Check if the widths array contains integer and if its length is valid. If
   * the validation fails, the FontContainer is updated.
   * 
   * @param cDoc
   */
  protected boolean checkWidthsArray(COSDocument cDoc) {
    // ---- the Widths value can be a reference to an object
    // ---- Access the object using the COSkey
    COSArray wArr = COSUtils.getAsArray(this.widths, cDoc);

    if (wArr == null) {
      this.fontContainer.addError(new ValidationError(
          ERROR_FONTS_DICTIONARY_INVALID, "The Witdhs array is unreachable"));
      return false;
    }

    // ---- firstChar and lastChar must be integer.
    int fc = ((COSInteger) this.firstChar).intValue();
    int lc = ((COSInteger) this.lastChar).intValue();

    // ---- wArr length = (lc - fc) +1 and it is an array of int.
    // ---- If FirstChar is greater than LastChar, the validation will fail
    // because of
    // ---- the array will have an expected size <= 0.
    int expectedLength = (lc - fc) + 1;
    if (wArr.size() != expectedLength) {
      this.fontContainer.addError(new ValidationError(
          ERROR_FONTS_DICTIONARY_INVALID,
          "The length of Witdhs array is invalid. Expected : \""
              + expectedLength + "\" Current : \"" + wArr.size() + "\""));
      return false;
    }

    for (Object arrContent : wArr.toList()) {
      boolean isInt = false;
      if (arrContent instanceof COSBase) {
        isInt = COSUtils.isInteger((COSBase) arrContent, cDoc);
      }

      if (!isInt) {
        this.fontContainer.addError(new ValidationError(
            ERROR_FONTS_DICTIONARY_INVALID,
            "The Witdhs array is invalid. (some element aren't integer)"));
        return false;
      }
    }

    return true;
  }

  protected boolean checkEncoding(COSDocument cDoc) {
    return true;
  }

  protected boolean checkToUnicode(COSDocument cDoc) {
    // Check the toUnicode -- Useless for PDF/A 1-b
    return true;
  }

  /**
   * This method checks the font descriptor dictionary and embedded font files.
   * If the FontDescriptor validation fails, the FontContainer is updated.
   * 
   * @return
   */
  protected abstract boolean checkFontDescriptor() throws ValidationException;

  /**
   * Check if all required fields are present in the PDF file to describe the
   * Font Descriptor. If validation fails, FontConatiner is updated and false is
   * returned.
   */
  protected boolean checkFontDescriptorMandatoryFields() {
    boolean fname = false, flags = false, itangle = false, cheight = false;
    boolean fbbox = false, asc = false, desc = false, stemv = false;

    for (Object key : this.fDescriptor.keySet()) {
      if (!(key instanceof COSName)) {
        this.fontContainer.addError(new ValidationResult.ValidationError(
            ValidationConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
            "Invalid key in The font descriptor"));
        return false;
      }

      String cosName = ((COSName) key).getName();
      if (cosName.equals(FONT_DICTIONARY_KEY_FONTNAME)) {
        fname = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_FLAGS)) {
        flags = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_ITALICANGLE)) {
        itangle = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_CAPHEIGHT)) {
        cheight = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_FONTBBOX)) {
        fbbox = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_ASCENT)) {
        asc = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_DESCENT)) {
        desc = true;
      }
      if (cosName.equals(FONT_DICTIONARY_KEY_STEMV)) {
        stemv = true;
      }
    }

    if (!(fname && flags && itangle && cheight && fbbox && asc && desc && stemv)) {
      this.fontContainer.addError(new ValidationError(
          ERROR_FONTS_DESCRIPTOR_INVALID, "Some mandatory fields are missing"));
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * net.awl.edoc.pdfa.validation.font.FontValidator#validate(java.util.List)
   */
  public boolean validate() throws ValidationException {
    COSDocument cDoc = handler.getDocument().getDocument();
    if (!checkMandatoryFields()) {
      return false;
    }

    boolean result = true;
    result = result && checkWidthsArray(cDoc);
    result = result && checkFontDescriptor();
    result = result && checkEncoding(cDoc);
    result = result && checkToUnicode(cDoc);
    return result;
  }
}
