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

/**
 * Information about a font on the system.
 *
 * @author John Hewson
 */
public abstract class FontInfo
{
    /**
     * Returns the PostScript name of the font.
     */
    public abstract String getPostScriptName();

    /**
     * Returns the font's format.
     */
    public abstract FontFormat getFormat();

    /**
     * Returns the CIDSystemInfo associated with the font, if any.
     */
    public abstract PDCIDSystemInfo getCIDSystemInfo();

    /**
     * Returns a new FontBox font instance for the font. Implementors of this method must not
     * cache the return value of this method unless doing so via the current {@link FontCache}.
     */
    public abstract FontBoxFont getFont();
    
    @Override
    public String toString()
    {
        return getPostScriptName() + " (" + getFormat() + ")";
    }
}
