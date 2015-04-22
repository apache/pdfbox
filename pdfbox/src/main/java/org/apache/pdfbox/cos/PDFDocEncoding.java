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

package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The "PDFDocEncoding" encoding. Note that this is *not* a Type 1 font encoding, it is used only
 * within PDF "text strings".
 */
final class PDFDocEncoding
{
    private static final char REPLACEMENT_CHARACTER = '\uFFFD';

    private static final int[] CODE_TO_UNI;
    private static final Map<Character, Integer> UNI_TO_CODE;

    static
    {
        CODE_TO_UNI = new int[256];
        UNI_TO_CODE = new HashMap<Character, Integer>(256);

        // initialize with basically ISO-8859-1
        for (int i = 0; i < 256; i++)
        {
            set(i, (char)i);
        }

        // then do all deviations (based on the table in ISO 32000-1:2008)
        // block 1
        set(0x18, '\u02D8'); // BREVE
        set(0x19, '\u02C7'); // CARON
        set(0x1A, '\u02C6'); // MODIFIER LETTER CIRCUMFLEX ACCENT
        set(0x1B, '\u02D9'); // DOT ABOVE
        set(0x1C, '\u02DD'); // DOUBLE ACUTE ACCENT
        set(0x1D, '\u02DB'); // OGONEK
        set(0x1E, '\u02DA'); // RING ABOVE
        set(0x1F, '\u02DC'); // SMALL TILDE
        // block 2
        set(0x7F, REPLACEMENT_CHARACTER); // undefined
        set(0x80, '\u2022'); // BULLET
        set(0x81, '\u2020'); // DAGGER
        set(0x82, '\u2021'); // DOUBLE DAGGER
        set(0x83, '\u2026'); // HORIZONTAL ELLIPSIS
        set(0x84, '\u2014'); // EM DASH
        set(0x85, '\u2013'); // EN DASH
        set(0x86, '\u0192'); // LATIN SMALL LETTER SCRIPT F
        set(0x87, '\u2044'); // FRACTION SLASH (solidus)
        set(0x88, '\u2039'); // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        set(0x89, '\u203A'); // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        set(0x8A, '\u2212'); // MINUS SIGN
        set(0x8B, '\u2030'); // PER MILLE SIGN
        set(0x8C, '\u201E'); // DOUBLE LOW-9 QUOTATION MARK (quotedblbase)
        set(0x8D, '\u201C'); // LEFT DOUBLE QUOTATION MARK (quotedblleft)
        set(0x8E, '\u201D'); // RIGHT DOUBLE QUOTATION MARK (quotedblright)
        set(0x8F, '\u2018'); // LEFT SINGLE QUOTATION MARK (quoteleft)
        set(0x90, '\u2019'); // RIGHT SINGLE QUOTATION MARK (quoteright)
        set(0x91, '\u201A'); // SINGLE LOW-9 QUOTATION MARK (quotesinglbase)
        set(0x92, '\u2122'); // TRADE MARK SIGN
        set(0x93, '\uFB01'); // LATIN SMALL LIGATURE FI
        set(0x94, '\uFB02'); // LATIN SMALL LIGATURE FL
        set(0x95, '\u0141'); // LATIN CAPITAL LETTER L WITH STROKE
        set(0x96, '\u0152'); // LATIN CAPITAL LIGATURE OE
        set(0x97, '\u0160'); // LATIN CAPITAL LETTER S WITH CARON
        set(0x98, '\u0178'); // LATIN CAPITAL LETTER Y WITH DIAERESIS
        set(0x99, '\u017D'); // LATIN CAPITAL LETTER Z WITH CARON
        set(0x9A, '\u0131'); // LATIN SMALL LETTER DOTLESS I
        set(0x9B, '\u0142'); // LATIN SMALL LETTER L WITH STROKE
        set(0x9C, '\u0153'); // LATIN SMALL LIGATURE OE
        set(0x9D, '\u0161'); // LATIN SMALL LETTER S WITH CARON
        set(0x9E, '\u017E'); // LATIN SMALL LETTER Z WITH CARON
        set(0x9F, REPLACEMENT_CHARACTER); // undefined
        set(0xA0, '\u20AC'); // EURO SIGN
        // end of deviations
    }
    
    private PDFDocEncoding()
    {
    }

    private static void set(int code, char unicode)
    {
        CODE_TO_UNI[code] = unicode;
        UNI_TO_CODE.put(unicode, code);
    }

    /**
     * Returns the string representation of the given PDFDocEncoded bytes.
     */
    public static String toString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            if ((b & 0xff) >= CODE_TO_UNI.length)
            {
                sb.append('?');
            }
            else
            {
                sb.append((char)CODE_TO_UNI[b & 0xff]);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the given string encoded with PDFDocEncoding.
     */
    public static byte[] getBytes(String text)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (char c : text.toCharArray())
        {
            Integer code = UNI_TO_CODE.get(c);
            if (code == null)
            {
                out.write(0);
            }
            else
            {
                out.write(code);
            }
        }
        return out.toByteArray();
    }

    /**
     * Returns true if the given character is available in PDFDocEncoding.
     *
     * @param character UTF-16 character
     */
    public static boolean containsChar(char character)
    {
        return UNI_TO_CODE.containsKey(character);
    }
}
