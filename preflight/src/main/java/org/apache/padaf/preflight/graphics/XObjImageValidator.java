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

import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_MISSING_FIELD;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_KEY;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY;
import static org.apache.padaf.preflight.ValidationConstants.XOBJECT_DICTIONARY_KEY_COLOR_SPACE;

import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelper;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelperFactory;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.padaf.preflight.utils.RenderingIntents;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

/**
 * This class validates XObject with the Image subtype.
 */
public class XObjImageValidator extends AbstractXObjValidator {

  public XObjImageValidator(DocumentHandler handler, COSStream xobj) {
    super(handler, xobj);
  }

  /*
   * (non-Javadoc)
   * 
   * @seenet.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator#
   * checkMandatoryFields(java.util.List)
   */
  @Override
  protected boolean checkMandatoryFields(List<ValidationError> result) {
    boolean res = this.xobject.getItem(COSName.getPDFName("Width")) != null;
    res = res && this.xobject.getItem(COSName.getPDFName("Height")) != null;
    // type and subtype checked before to create the Validator.
    if (!res) {
      result.add(new ValidationError(ERROR_GRAPHIC_MISSING_FIELD));
    }
    return res;
  }

  /*
   * 6.2.4 no Alternates
   */
  protected void checkAlternates(List<ValidationError> result) throws ValidationException {
    if (this.xobject.getItem(COSName.getPDFName("Alternates")) != null) {
      result.add(new ValidationError(
          ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_KEY,
      "Unexpected 'Alternates' Key"));
    }
  }
  
  /*
   * 6.2.4 if interpolates, value = false
   */
  protected void checkInterpolate(List<ValidationError> result) throws ValidationException {
    if (this.xobject.getItem(COSName.getPDFName("Interpolate")) != null) {
      if (this.xobject.getBoolean(COSName.getPDFName("Interpolate"), true)) {
        result.add(new ValidationError(
            ValidationConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
        "Unexpected 'true' value for 'Interpolate' Key"));
      }
    }
  }

  /*
   * 6.2.4 Intent has specific values
   */
  protected void checkIntent(List<ValidationError> result) throws ValidationException {
    if (this.xobject.getItem(COSName.getPDFName("Intent")) != null) {
      String s = this.xobject.getNameAsString("Intent");
      if (!RenderingIntents.contains(s)) {
        result.add(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
            "Unexpected value '" + s + "' for Intent key in image"));
      }
    }
  }

  /*
   * According to the PDF Reference file, there are some specific rules on following fields
   * ColorSpace, Mask, ImageMask and BitsPerComponent.
   * If ImageMask is set to true, ColorSpace and Mask entries are forbidden. 
   */
  protected void checkColorSpaceAndImageMask(List<ValidationError> result) throws ValidationException {

    COSBase csImg = this.xobject.getItem(COSName.getPDFName(XOBJECT_DICTIONARY_KEY_COLOR_SPACE));
    COSBase bitsPerComp = this.xobject.getItem(COSName.getPDFName("BitsPerComponent"));
    COSBase mask = this.xobject.getItem(COSName.getPDFName("Mask"));

    if (isImageMaskTrue()) {
    	if ( csImg != null || mask != null) {
    		result.add(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_KEY, "ImageMask entry is true, ColorSpace and Mask are forbidden."));
    	}

    	Integer bitsPerCompValue = COSUtils.getAsInteger(bitsPerComp, cosDocument);
    	if (bitsPerCompValue != 1 ) {
    		result.add(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY, "ImageMask entry is true, BitsPerComponent must be 1."));
    	}

  	} else {
  		ColorSpaceHelper csh = ColorSpaceHelperFactory.getColorSpaceHelper(csImg, handler, ColorSpaceRestriction.NO_PATTERN);
  		csh.validate(result);
  	}
  }

  private boolean isImageMaskTrue() {
  	COSBase imgMask = this.xobject.getItem(COSName.getPDFName("ImageMask"));
  	if (imgMask != null && imgMask instanceof COSBoolean) {
  		return ((COSBoolean) imgMask).getValue();
  	} else {
  		return false;
  	}
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator#validate()
   */
  @Override
  public List<ValidationError> validate() throws ValidationException {
    List<ValidationError> result = super.validate();

    checkAlternates(result);
    checkInterpolate(result);
    checkIntent(result);

    checkColorSpaceAndImageMask(result);

    return result;
  }
}
