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
package org.apache.pdfbox.pdmodel.font.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Tests the glyph name to Unicode mapping of the Adobe Glyph List.
 */
class GlyphListTest
{
    private final GlyphList glyphList = GlyphList.getAdobeGlyphList();

    /**
     * Ligature names in the AGL underscore form, see section 2 of the AGL specification:
     * https://github.com/adobe-type-tools/agl-specification
     */
    @Test
    void testUnderscoreLigatures()
    {
        assertEquals("ff", glyphList.toUnicode("f_f"));
        assertEquals("fi", glyphList.toUnicode("f_i"));
        assertEquals("fl", glyphList.toUnicode("f_l"));
        assertEquals("ffi", glyphList.toUnicode("f_f_i"));
        assertEquals("ffl", glyphList.toUnicode("f_f_l"));
        assertEquals("Th", glyphList.toUnicode("T_h"));
    }

    @Test
    void testUnderscoreLigaturesWithSuffixAndUniComponents()
    {
        assertEquals("ffi", glyphList.toUnicode("f_f_i.alt"));
        assertEquals("fi", glyphList.toUnicode("uni0066_uni0069"));
        assertEquals("fi", glyphList.toUnicode("f_uni0069"));
    }

    @Test
    void testUnresolvableUnderscoreNames()
    {
        assertNull(glyphList.toUnicode("f__i"));
        assertNull(glyphList.toUnicode("_"));
        assertNull(glyphList.toUnicode("f_"));
        assertNull(glyphList.toUnicode("_f"));
        assertNull(glyphList.toUnicode("f_bogus"));
    }

    @Test
    void testExistingNamesUnchanged()
    {
        assertEquals("\uFB01", glyphList.toUnicode("fi"));
        assertEquals("\uFB03", glyphList.toUnicode("ffi"));
        assertEquals("A", glyphList.toUnicode("A"));
        assertEquals("_", glyphList.toUnicode("underscore"));
        assertEquals("5", glyphList.toUnicode("five.lin"));
        assertEquals("\u20AC", glyphList.toUnicode("uni20AC"));
    }
}
