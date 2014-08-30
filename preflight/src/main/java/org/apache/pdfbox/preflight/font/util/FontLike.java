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

package org.apache.pdfbox.preflight.font.util;

import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

/**
 * Wrapper over font-like objects.
 */
public class FontLike
{
    private PDFont font;
    private PDCIDFont cidFont;

    /**
     * Constructor.
     *
     * @param font A font.
     */
    public FontLike(PDFont font)
    {
        this.font = font;
    }

    /**
     * Constructor.
     *
     * @param cidFont A CIDFont, this is not actually a font.
     */
    public FontLike(PDCIDFont cidFont)
    {
        this.cidFont = cidFont;
    }

    /**
     * The PostScript name of the font.
     */
    public String getBaseFont()
    {
        if (font != null)
        {
            return font.getBaseFont();
        }
        else
        {
            return cidFont.getBaseFont();
        }
    }

    /**
     * Returns the width of the given character.
     *
     * @param code character code
     */
    public float getWidth(int code) throws IOException
    {
        if (font != null)
        {
            return font.getWidth(code);
        }
        else
        {
            return cidFont.getWidth(code);
        }
    }
}
