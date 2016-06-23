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
import org.apache.fontbox.ttf.TrueTypeFont;

/**
 * Font mapper, locates non-embedded fonts. If you implement this then you're responsible for
 * caching the fonts. SoftReference&lt;FontBoxFont&gt; is recommended.
 *
 * @author John Hewson
 */
public interface FontMapper
{
    /**
     * Finds a TrueType font with the given PostScript name, or a suitable substitute, or null.
     *
     * @param fontDescriptor FontDescriptor
     */
    FontMapping<TrueTypeFont> getTrueTypeFont(String baseFont, PDFontDescriptor fontDescriptor);
    
    /**
     * Finds a font with the given PostScript name, or a suitable substitute, or null. This allows
     * any font to be substituted with a PFB, TTF or OTF.
     *
     * @param fontDescriptor the FontDescriptor of the font to find
     */
    FontMapping<FontBoxFont> getFontBoxFont(String baseFont, PDFontDescriptor fontDescriptor);
    
    /**
     * Finds a CFF CID-Keyed font with the given PostScript name, or a suitable substitute, or null.
     * This method can also map CJK fonts via their CIDSystemInfo (ROS).
     * 
     * @param fontDescriptor FontDescriptor
     * @param cidSystemInfo the CID system info, e.g. "Adobe-Japan1", if any.
     */
    CIDFontMapping getCIDFont(String baseFont, PDFontDescriptor fontDescriptor,
                              PDCIDSystemInfo cidSystemInfo);
}
