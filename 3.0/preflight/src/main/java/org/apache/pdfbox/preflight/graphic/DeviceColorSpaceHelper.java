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
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN;

/**
 * This class defines restrictions on Color Spaces. It checks the consistency of the Color space with the
 * DestOutputIntent, if the color space isn't a Device Color space or a Indexed color space using Device the validation
 * will fail.
 */
public class DeviceColorSpaceHelper extends StandardColorSpaceHelper
{

    public DeviceColorSpaceHelper(PreflightContext _context, PDColorSpace _cs)
    {
        super(_context, _cs);
    }

    /**
     * This method updates the given list with a ValidationError (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN)
     * and returns false.
     */
    @Override
    protected void processPatternColorSpace(PDColorSpace colorSpace)
    {
        context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN,
                "Pattern ColorSpace is forbidden"));
    }

    /**
     * This method updates the given list with a ValidationError (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN)
     * and returns false.
     */
    @Override
    protected void processDeviceNColorSpace(PDColorSpace colorSpace)
    {
        context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN,
                "DeviceN ColorSpace is forbidden"));
    }

    /**
     * Indexed color space is authorized only if the BaseColorSpace is a DeviceXXX color space. In all other cases the
     * given list is updated with a ValidationError (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN) and returns
     * false.
     */
    @Override
    protected void processIndexedColorSpace(PDColorSpace colorSpace)
    {
        PDIndexed indexed = (PDIndexed) colorSpace;
        PDColorSpace baseColorSpace = indexed.getBaseColorSpace();
        ColorSpaces colorSpaces = ColorSpaces.valueOf(baseColorSpace.getName());
        switch (colorSpaces)
        {
        case Indexed:
        case I:
        case Pattern:
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN, colorSpaces
                    .getLabel() + " ColorSpace is forbidden"));
            break;
        default:
            processAllColorSpace(baseColorSpace);
        }
    }
}
