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

import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_TYPE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION;
import static org.apache.padaf.preflight.ValidationConstants.PATTERN_KEY_SHADING;
import static org.apache.padaf.preflight.ValidationConstants.PATTERN_KEY_SHADING_TYPE;
import static org.apache.padaf.preflight.ValidationConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE;
import static org.apache.padaf.preflight.ValidationConstants.XOBJECT_DICTIONARY_KEY_COLOR_SPACE;

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelper;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelperFactory;
import org.apache.padaf.preflight.graphics.color.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;

/**
 * This class process the validation of a ShadingPattern object.
 */
public class ShadingPattern implements XObjectValidator {
  /**
   * COSDictionary which represent the shading pattern.
   */
  private COSDictionary pattern = null;
  /**
   * The COSDocument which contains the shading pattern to check.
   */
  private COSDocument cosDoc = null;
  /**
   * The document handler which contains useful information to process the
   * validation.
   */
  private DocumentHandler documentHandler = null;

  /**
   * @param _handler
   *          DocumentHandler used to initialize "documentHandler" and "cosDoc"
   *          attributes.
   * @param _pattern
   *          COSDictionary used to initialize the shading pattern to validate.
   */
  public ShadingPattern(DocumentHandler _handler, COSDictionary _pattern) {
    this.documentHandler = _handler;
    this.pattern = _pattern;
    this.cosDoc = this.documentHandler.getDocument().getDocument();
  }

  /**
   * Because of a Shading Pattern can be used as an Indirect Object or Directly
   * define in an other dictionary, there are two ways to obtain the Shading
   * Pattern dictionary according to the pattern attribute. This method returns
   * the Shading pattern dictionary represented by the pattern attribute or
   * contained in it.
   * 
   * This is the first method called by the validate method.
   * 
   * @param errors
   *          the list of error to update if there are no Shading pattern in the
   *          pattern COSDictionary.
   * @return the ShadingPattern dictionary
   */
  protected COSDictionary getShadingDictionary(List<ValidationError> errors) {
    if (!"Shading".equals(pattern.getNameAsString(COSName
        .getPDFName(DICTIONARY_KEY_TYPE)))) {
      COSBase shading = pattern
          .getItem(COSName.getPDFName(PATTERN_KEY_SHADING));
      if (shading == null) {
        errors
            .add(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION));
        return null;
      }
      return COSUtils.getAsDictionary(shading, cosDoc);
    } else {
      return pattern;
    }
  }

  /**
   * Checks if the given shading pattern contains the ShadingType entry and if
   * the ColorSapce entry is consistent which rules of the PDF Reference and the
   * ISO 190005-1:2005 Specification.
   * 
   * This method is called by the validate method.
   * 
   * @param shadingDict
   *          the Shading pattern dictionary to check
   * @param errors
   *          the list of error to update if the validation fails
   * @return true if the Shading pattern is valid, false otherwise.
   * @throws ValidationException
   */
  protected boolean checkShadingDictionary(COSDictionary shadingDict,
      List<ValidationError> errors) throws ValidationException {
    if (shadingDict.getItem(COSName.getPDFName(PATTERN_KEY_SHADING_TYPE)) == null) {
      errors.add(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION));
      return false;
    }

    COSBase csImg = shadingDict.getItem(COSName
        .getPDFName(XOBJECT_DICTIONARY_KEY_COLOR_SPACE));
    ColorSpaceHelper csh = ColorSpaceHelperFactory.getColorSpaceHelper(csImg,
        documentHandler, ColorSpaceRestriction.NO_PATTERN);
    return csh.validate(errors);
  }

  /**
   * Check the Extended Graphic State contains in the ShadingPattern dictionary
   * if it is present. To check this ExtGState, this method uses the
   * net.awl.edoc.pdfa.validation.graphics.ExtGStateContainer object.
   * 
   * @param errors
   *          the list of error to update if the validation fails
   * @return true is the ExtGState is missing or valid, false otherwise.
   * @throws ValidationException
   */
  protected boolean checkGraphicState(List<ValidationError> errors)
      throws ValidationException {
    COSDictionary resources = (COSDictionary) this.pattern
        .getDictionaryObject(TRANPARENCY_DICTIONARY_KEY_EXTGSTATE);
    if (resources != null) {
      ExtGStateContainer extContainer = new ExtGStateContainer(resources,
          cosDoc);
      return extContainer.validateTransparencyRules(errors);
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.awl.edoc.pdfa.validation.graphics.XObjectValidator#validate()
   */
  public List<ValidationError> validate() throws ValidationException {
    List<ValidationError> result = new ArrayList<ValidationError>();
    COSDictionary shading = getShadingDictionary(result);
    if (shading != null) {
      boolean isValid = checkShadingDictionary(shading, result);
      isValid = isValid && checkGraphicState(result);
    }
    return result;
  }
}
