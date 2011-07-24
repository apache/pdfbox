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


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;

/**
 * This factory create the right Helper according to the owner of the ColorSpace
 * entry.
 */
public class ColorSpaceHelperFactory {

  /**
   * Return an instance of ColorSpaceHelper according to the
   * ColorSpaceRestiction value.
   * <UL>
   * <li>ColorSpaceRestiction.NO_PATTERN : returns NoPatternColorSpaceHelper
   * <li>ColorSpaceRestiction.ONLY_DEVICE : returns DeviceColorSpaceHelper
   * <li>default : returns StandardColorSpaceHelper
   * </UL>
   * 
   * @param csObj
   *          the COSBase which represents the ColorSpace (COSName or COSArray)
   * @param handler
   *          the DocumentHandler to access useful data
   * @param csr
   *          the color space restriction
   * @return
   */
  public static ColorSpaceHelper getColorSpaceHelper(COSBase csObj,
      DocumentHandler handler, ColorSpaceRestriction csr) {
    switch (csr) {
    case NO_PATTERN:
      return new NoPatternColorSpaceHelper(csObj, handler);
    case ONLY_DEVICE:
      return new DeviceColorSpaceHelper(csObj, handler);
    default:
      return new StandardColorSpaceHelper(csObj, handler);
    }
  }

  /**
   * Return an instance of ColorSpaceHelper according to the
   * ColorSpaceRestiction value.
   * <UL>
   * <li>ColorSpaceRestiction.NO_PATTERN : returns NoPatternColorSpaceHelper
   * <li>ColorSpaceRestiction.ONLY_DEVICE : returns DeviceColorSpaceHelper
   * <li>default : returns StandardColorSpaceHelper
   * </UL>
   * 
   * @param cs
   *          the High level PDFBox object which represents the ColorSpace
   * @param handler
   *          the DocumentHandler to access useful data
   * @param csr
   *          the color space restriction
   * @return
   */
  public static ColorSpaceHelper getColorSpaceHelper(PDColorSpace cs,
      DocumentHandler handler, ColorSpaceRestriction csr) {
    switch (csr) {
    case NO_PATTERN:
      return new NoPatternColorSpaceHelper(cs, handler);
    case ONLY_DEVICE:
      return new DeviceColorSpaceHelper(cs, handler);
    default:
      return new StandardColorSpaceHelper(cs, handler);
    }
  }

  /**
   * Enum used as argument of methods of this factory to return the right
   * Helper.
   */
  public enum ColorSpaceRestriction {
    NO_RESTRICTION, NO_PATTERN, ONLY_DEVICE;
  }
}
