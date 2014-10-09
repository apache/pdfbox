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
package org.apache.pdfbox.encoding;

import java.nio.charset.Charset;

/**
 * {@link Charset} implementation for the "PDFDocEncoding" from the PDF specification.
 * @version $Revision$
 */
public class PDFDocEncodingCharset extends SingleByteCharset
{

    /** Canonical name for the PDFDocEncoding. */
    public static final String NAME = "PDFDocEncoding";

    /** Singleton instance. */
    public static final PDFDocEncodingCharset INSTANCE = new PDFDocEncodingCharset();

    /**
     * Creates a new "PDFDocEncoding" charset.
     */
    public PDFDocEncodingCharset()
    {
        super(NAME, null, createEncoding());
    }

    private static char[] createEncoding()
    {
        char[] encoding = new char[256];

        //Initialize with basically ISO-8859-1
        for (int i = 0; i < 256; i++)
        {
            encoding[i] = (char)i;
        }
        //...then do all deviations (based on the table in ISO 32000-1:2008)
        //block 1
        encoding[0x18] = '\u02D8'; //BREVE
        encoding[0x19] = '\u02C7'; //CARON
        encoding[0x1A] = '\u02C6'; //MODIFIER LETTER CIRCUMFLEX ACCENT
        encoding[0x1B] = '\u02D9'; //DOT ABOVE
        encoding[0x1C] = '\u02DD'; //DOUBLE ACUTE ACCENT
        encoding[0x1D] = '\u02DB'; //OGONEK
        encoding[0x1E] = '\u02DA'; //RING ABOVE
        encoding[0x1F] = '\u02DC'; //SMALL TILDE
        //block 2
        encoding[0x7F] = REPLACEMENT_CHARACTER; //undefined
        encoding[0x80] = '\u2022'; //BULLET
        encoding[0x81] = '\u2020'; //DAGGER
        encoding[0x82] = '\u2021'; //DOUBLE DAGGER
        encoding[0x83] = '\u2026'; //HORIZONTAL ELLIPSIS
        encoding[0x84] = '\u2014'; //EM DASH
        encoding[0x85] = '\u2013'; //EN DASH
        encoding[0x86] = '\u0192'; //LATIN SMALL LETTER SCRIPT F
        encoding[0x87] = '\u2044'; //FRACTION SLASH (solidus)
        encoding[0x88] = '\u2039'; //SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        encoding[0x89] = '\u203A'; //SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        encoding[0x8A] = '\u2212'; //MINUS SIGN
        encoding[0x8B] = '\u2030'; //PER MILLE SIGN
        encoding[0x8C] = '\u201E'; //DOUBLE LOW-9 QUOTATION MARK (quotedblbase)
        encoding[0x8D] = '\u201C'; //LEFT DOUBLE QUOTATION MARK (double quote left)
        encoding[0x8E] = '\u201D'; //RIGHT DOUBLE QUOTATION MARK (quotedblright)
        encoding[0x8F] = '\u2018'; //LEFT SINGLE QUOTATION MARK (quoteleft)
        encoding[0x90] = '\u2019'; //RIGHT SINGLE QUOTATION MARK (quoteright)
        encoding[0x91] = '\u201A'; //SINGLE LOW-9 QUOTATION MARK (quotesinglbase)
        encoding[0x92] = '\u2122'; //TRADE MARK SIGN
        encoding[0x93] = '\uFB01'; //LATIN SMALL LIGATURE FI
        encoding[0x94] = '\uFB02'; //LATIN SMALL LIGATURE FL
        encoding[0x95] = '\u0141'; //LATIN CAPITAL LETTER L WITH STROKE
        encoding[0x96] = '\u0152'; //LATIN CAPITAL LIGATURE OE
        encoding[0x97] = '\u0160'; //LATIN CAPITAL LETTER S WITH CARON
        encoding[0x98] = '\u0178'; //LATIN CAPITAL LETTER Y WITH DIAERESIS
        encoding[0x99] = '\u017D'; //LATIN CAPITAL LETTER Z WITH CARON
        encoding[0x9A] = '\u0131'; //LATIN SMALL LETTER DOTLESS I
        encoding[0x9B] = '\u0142'; //LATIN SMALL LETTER L WITH STROKE
        encoding[0x9C] = '\u0153'; //LATIN SMALL LIGATURE OE
        encoding[0x9D] = '\u0161'; //LATIN SMALL LETTER S WITH CARON
        encoding[0x9E] = '\u017E'; //LATIN SMALL LETTER Z WITH CARON
        encoding[0x9F] = REPLACEMENT_CHARACTER; //undefined
        encoding[0xA0] = '\u20AC'; //EURO SIGN
        //end of deviations
        return encoding;
    }

}
