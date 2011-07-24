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

package org.apache.padaf.preflight.graphics.color;

import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING;

import java.io.IOException;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;

/**
 * This class defines restrictions on Color Spaces. It checks the consistency of
 * the Color space with the DestOutputIntent, if the color space isn't a Device
 * Color space or a Indexed color space using Device the validation will fail.
 */
public class DeviceColorSpaceHelper extends StandardColorSpaceHelper {

  DeviceColorSpaceHelper(COSBase object, DocumentHandler _handler) {
    super(object, _handler);
  }

  DeviceColorSpaceHelper(PDColorSpace object, DocumentHandler _handler) {
    super(object, _handler);
  }

  /**
   * This method updates the given list with a ValidationError
   * (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and returns false.
   */
  protected boolean processPatternColorSpace(List<ValidationError> result) {
    result
        .add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, "Pattern ColorSpace is forbidden"));
    return false;
  }

  /**
   * This method updates the given list with a ValidationError
   * (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and returns false.
   */
  protected boolean processCalibratedColorSpace(List<ValidationError> result) {
    result
        .add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, "Calibrated ColorSpace is forbidden"));
    return false;
  }

  /**
   * This method updates the given list with a ValidationError
   * (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and returns false.
   */
  protected boolean processICCBasedColorSpace(PDColorSpace pdcs,
      List<ValidationError> result) {
    result
        .add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, "ICCBased ColorSpace is forbidden"));
    return false;
  }

  /**
   * This method updates the given list with a ValidationError
   * (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and returns false.
   */
  protected boolean processDeviceNColorSpace(PDColorSpace pdcs,
      List<ValidationError> result) {
    result
        .add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, "DeviceN ColorSpace is forbidden"));
    return false;
  }

  /**
   * Indexed color space is authorized only if the BaseColorSpace is a DeviceXXX
   * color space. In all other cases the given list is updated with a
   * ValidationError (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and
   * returns false.
   */
  protected boolean processIndexedColorSpace(PDColorSpace pdcs,
      List<ValidationError> result) {
    PDIndexed indexed = (PDIndexed) pdcs;
    try {
      if (iccpw == null) {
        result.add(new ValidationError(
            ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING, "DestOutputProfile is missing"));
        return false;
      }

      PDColorSpace based = indexed.getBaseColorSpace();
      ColorSpaces cs = ColorSpaces.valueOf(based.getName());
      switch (cs) {
      case DeviceCMYK:
      case DeviceCMYK_SHORT:
      case DeviceRGB:
      case DeviceRGB_SHORT:
      case DeviceGray:
      case DeviceGray_SHORT:
        return processAllColorSpace(based, result);
      default:
        result.add(new ValidationError(
            ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, cs.getLabel() + " ColorSpace is forbidden"));
        return false;
      }

    } catch (IOException e) {
      result.add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE, "Unable to read Indexed Color Space : " + e.getMessage()));
      return false;
    }
  }

  /**
   * This method updates the given list with a ValidationError
   * (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and returns false.
   */
  protected boolean processSeparationColorSpace(PDColorSpace pdcs,
      List<ValidationError> result) {
    result
        .add(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, "Separation ColorSpace is forbidden"));
    return false;
  }
}
