/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel.font;

import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.ttf.OpenTypeFont;

/**
 * A CIDFontMapping is a kind of FontMapping which allows for an additional TrueTypeFont substitute
 * to be provided if a CID font is not available.
 *
 * @author John Hewson
 */
public final class CIDFontMapping extends FontMapping<OpenTypeFont>
{
    private final FontBoxFont ttf;

    public CIDFontMapping(OpenTypeFont font, FontBoxFont fontBoxFont, boolean isFallback)
    {
        super(font, isFallback);
        this.ttf = fontBoxFont;
    }

    /**
     * Returns a TrueType font when isCIDFont() is true, otherwise null.
     */
    public FontBoxFont getTrueTypeFont()
    {
        return ttf;
    }

    /**
     * Returns true if this is a CID font.
     */
    public boolean isCIDFont()
    {
        return getFont() != null;
    }
}
