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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * This class defines restrictions on Pattern ColorSpace. It checks the consistency of the Color space with the
 * DestOutputIntent, if the color space is a Pattern the validation will fail.
 */
public class NoPatternColorSpaceHelper extends StandardColorSpaceHelper
{

    public NoPatternColorSpaceHelper(PreflightContext _context, PDColorSpace _cs)
    {
        super(_context, _cs);
    }

    /**
     * This method updates the given list with a ValidationError (ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN)
     * and returns false.
     */
    protected void processPatternColorSpace(PDColorSpace pdcs)
    {
        context.addValidationError(new ValidationError(ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN,
                "Pattern color space is forbidden"));
    }
}
