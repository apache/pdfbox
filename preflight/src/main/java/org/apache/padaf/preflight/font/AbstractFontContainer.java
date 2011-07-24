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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.pdmodel.font.PDFont;

public abstract class AbstractFontContainer {
  /**
   * PDFBox object which contains the Font Dictionary
   */
  protected PDFont font = null;

  protected Map<Integer, GlyphDetail> cidKnownByFont = new HashMap<Integer, GlyphDetail>();

  /**
   * Boolean used to know if the Font Program is embedded.
   */
  protected boolean isFontProgramEmbedded = true;

  /**
   * Errors which occurs during the Font Validation
   */
  protected List<ValidationError> errors = new ArrayList<ValidationError>(0);

  /**
   * The FontContainer Constructor. The type attribute is initialized according
   * to the given PDFont object.
   * 
   * @param fd
   *          Font object of the PDFBox API. (Mandatory)
   * @throws NullPointerException
   *           If the fd is null.
   */
  public AbstractFontContainer(PDFont fd) {
    this.font = fd;
  }

  /**
   * Return the PDFont object
   * 
   * @return
   */
  public PDFont getFont() {
    return font;
  }

  public void addCID(Integer cid, GlyphDetail details) {
    this.cidKnownByFont.put(cid, details);
  }

  /**
   * @return the isFontProgramEmbedded
   */
  public boolean isFontProgramEmbedded() {
    return isFontProgramEmbedded;
  }

  /**
   * @param isFontProgramEmbedded
   *          the isFontProgramEmbedded to set
   */
  public void setFontProgramEmbedded(boolean isFontProgramEmbedded) {
    this.isFontProgramEmbedded = isFontProgramEmbedded;
  }

  /**
   * Addition of a validation error.
   * 
   * @param error
   */
  public void addError(ValidationError error) {
    this.errors.add(error);
  }

  /**
   * This method returns the validation state of the font.
   * 
   * If the list of errors is empty, the validation is successful (State :
   * VALID). If the size of the list is 1 and if the error is about EmbeddedFont,
   * the state is "MAYBE" because the font can be valid if
   * it isn't used (for Width error) or if the rendering mode is 3 (For not
   * embedded font). Otherwise, the validation failed (State : INVALID)
   * 
   * @return
   */
  public State isValid() {
    if (this.errors.isEmpty()) {
      return State.VALID;
    }

    if ((this.errors.size() == 1)
        && !this.isFontProgramEmbedded) {
      return State.MAYBE;
    }

    // else more than one error, the validation failed
    return State.INVALID;
  }

  /**
   * @return the errors
   */
  public List<ValidationError> getErrors() {
    return errors;
  }

  public static enum State {
    VALID, MAYBE, INVALID;
  }

  
  /**
   * Check if the cid is present and consistent in the contained font.
   * @param cid the cid
   * @return true if cid is present and consistent, false otherwise
   */
  public abstract void checkCID (int cid) throws GlyphException;

  
  void addKnownCidElement(GlyphDetail glyphDetail) {
    this.cidKnownByFont.put(glyphDetail.getCID(), glyphDetail);
  }
  
  protected boolean isAlreadyComputedCid(int cid) throws GlyphException {
  	boolean already = false;
	  GlyphDetail gdetail = this.cidKnownByFont.get(cid);
	  if (gdetail != null) {
		  gdetail.throwExceptionIfNotValid();
		  already = true;
	  }
	  return already;
  }

  protected void checkWidthsConsistency(int cid, float widthProvidedByPdfDictionary, float widthInFontProgram) throws GlyphException {
  	if(!(Math.floor(widthInFontProgram) == widthProvidedByPdfDictionary || Math.round(widthInFontProgram) == widthProvidedByPdfDictionary)) {
  	  GlyphException e = new GlyphException(ValidationConstants.ERROR_FONTS_METRICS, cid, 
				  				"Width of the character \"" + cid 
				  				+ "\" in the font program \""
				  				+ this.font.getBaseFont() 
				  				+ "\"is inconsistent with the width in the PDF dictionary.");
		  addKnownCidElement(new GlyphDetail(cid, e));
		  throw e;
	  }
  }
}
