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

package org.apache.pdfbox.pdmodel.fdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
/*
 * Test the XML 1.0 escaping performed by FDFUtils.escapeXML10().
 */
public class FDFUtilsTest
{
    @Test
    public void testPlainAsciiIsUnchanged()
    {
        String input = "Hello World 123";
        assertEquals(input, FDFUtils.escapeXML10(input));
    }

    @Test
    public void testEmptyStringReturnsEmptyString()
    {
        assertEquals("", FDFUtils.escapeXML10(""));
    }

    @Test
    public void testEscapesLessThanSign()
    {
        assertEquals("&lt;", FDFUtils.escapeXML10("<"));
    }

    @Test
    public void testEscapesGreaterThanSign()
    {
        assertEquals("&gt;", FDFUtils.escapeXML10(">"));
    }

    @Test
    public void testEscapesAmpersand()
    {
        assertEquals("&amp;", FDFUtils.escapeXML10("&"));
    }

    @Test
    public void testEscapesDoubleQuote()
    {
        assertEquals("&quot;", FDFUtils.escapeXML10("\""));
    }

    @Test
    public void testEscapesSingleQuote()
    {
        assertEquals("&apos;", FDFUtils.escapeXML10("'"));
    }

    @Test
    public void testEscapesAllSpecialCharactersInOneString()
    {
        String input = "<tag attr=\"value\" other='x'>&</tag>";
        String expected = "&lt;tag attr=&quot;value&quot; other=&apos;x&apos;&gt;&amp;&lt;/tag&gt;";
        assertEquals(expected, FDFUtils.escapeXML10(input));
    }

    @Test
    public void testLegalWhitespaceControlCharactersPassThroughUnescaped()
    {
        // Tab (0x9), line feed (0xA) and carriage return (0xD) are explicitly
        // legal XML 1.0 characters and are not part of the escaped set.
        String input = "line1\tline2\nline3\rline4";
        assertEquals(input, FDFUtils.escapeXML10(input));
    }

    @Test
    public void testNonAsciiBmpCharacterIsEscapedAsNumericReference()
    {
        // 'é' is U+00E9 (233 decimal)
        assertEquals("caf&#233;", FDFUtils.escapeXML10("caf\u00e9"));
    }

    @Test
    public void testMultipleNonAsciiCharactersAreEachEscaped()
    {
        // '\u00e9' = 233, '\u00e8' = 232
        assertEquals("&#233;&#232;", FDFUtils.escapeXML10("\u00e9\u00e8"));
    }

    @Test
    public void testIllegalControlCharacterIsNotPassedThroughRaw()
    {
        // 0x0B (vertical tab) is not a legal XML 1.0 character and must not
        // appear unescaped in the output.
        String result = FDFUtils.escapeXML10("a\u000bb");
        assertFalse("Illegal control character must not appear raw in escaped output",
                result.indexOf('\u000b') >= 0);
        assertEquals("a\ufffdb", result);
    }

    @Test
    public void testSupplementaryCharacterProducesSingleValidReference()
    {
        // U+1F600 (GRINNING FACE) is represented in Java as a surrogate pair.
        // It must be escaped as a single reference to its code point (128512),
        // not as two references to the individual (illegal) surrogate values.
        String input = new String(Character.toChars(0x1F600));
        assertEquals("&#128512;", FDFUtils.escapeXML10(input));
    }
}