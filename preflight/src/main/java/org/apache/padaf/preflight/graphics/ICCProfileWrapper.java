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

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/**
 * This class embeds an instance of java.awt.color.ICC_Profile which represent
 * the ICCProfile defined by the DestOutputItents key of the OutputIntents of
 * the PDF.
 */
public class ICCProfileWrapper {
  /**
   * The ICCProfile extracted from DestOutputItents
   */
  private ICC_Profile profile = null;

  /**
   * The ICC ColorSpace created using the ICCProfile
   */
  private ICC_ColorSpace colorSpace = null;

  public ICCProfileWrapper(ICC_Profile _profile) {
    this.profile = _profile;
    this.colorSpace = new ICC_ColorSpace(_profile);
  }

  /**
   * Call the ICC_ColorSpace.getType method and return the value.
   * 
   * @return
   */
  public int getColorSpaceType() {
    return colorSpace.getType();
  }

  /**
   * @return the profile
   */
  public ICC_Profile getProfile() {
    return profile;
  }

  /**
   * Return true if the ColourSpace is RGB
   * 
   * @return
   */
  public boolean isRGBColorSpace() {
    return ICC_ColorSpace.TYPE_RGB == colorSpace.getType();
  }

  /**
   * Return true if the ColourSpace is CMYK
   * 
   * @return
   */
  public boolean isCMYKColorSpace() {
    return ICC_ColorSpace.TYPE_CMYK == colorSpace.getType();
  }

  /**
   * Return true if the ColourSpace is Gray scale
   * 
   * @return
   */
  public boolean isGrayColorSpace() {
    return ICC_ColorSpace.TYPE_GRAY == colorSpace.getType();
  }
}
