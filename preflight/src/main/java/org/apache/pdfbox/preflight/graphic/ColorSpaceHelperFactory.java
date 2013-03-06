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

package org.apache.pdfbox.preflight.graphic;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.preflight.PreflightContext;

/**
 * This factory create the right Helper according to the owner of the ColorSpace entry.
 */
public class ColorSpaceHelperFactory
{

    /**
     * Return an instance of ColorSpaceHelper according to the ColorSpaceRestiction value.
     * <UL>
     * <li>ColorSpaceRestiction.NO_PATTERN : returns NoPatternColorSpaceHelper
     * <li>ColorSpaceRestiction.ONLY_DEVICE : returns DeviceColorSpaceHelper
     * <li>default : returns StandardColorSpaceHelper
     * </UL>
     * 
     * @param context
     *            the PreflightContext to access useful data
     * @param cs
     *            the High level PDFBox object which represents the ColorSpace
     * @param csr
     *            the color space restriction
     * @return
     */
    public ColorSpaceHelper getColorSpaceHelper(PreflightContext context, PDColorSpace cs, ColorSpaceRestriction csr)
    {
        switch (csr)
        {
        case NO_PATTERN:
            return new NoPatternColorSpaceHelper(context, cs);
        case ONLY_DEVICE:
            return new DeviceColorSpaceHelper(context, cs);
        default:
            return new StandardColorSpaceHelper(context, cs);
        }
    }

    /**
     * Enum used as argument of methods of this factory to return the right Helper.
     */
    public enum ColorSpaceRestriction
    {
        NO_RESTRICTION, NO_PATTERN, ONLY_DEVICE;
    }
}
