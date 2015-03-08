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
package org.apache.fontbox.cff;


/**
 * This is specialized CFFCharset. It's used if the CharsetId of a font is set to 0.
 * 
 * @author Villu Ruusmann
 */
public final class CFFISOAdobeCharset extends CFFCharset
{

    private CFFISOAdobeCharset()
    {
        super(false);
    }

    /**
     * Returns an instance of the CFFExpertSubsetCharset class.
     * @return an instance of CFFExpertSubsetCharset
     */
    public static CFFISOAdobeCharset getInstance()
    {
        return CFFISOAdobeCharset.INSTANCE;
    }

    private static final CFFISOAdobeCharset INSTANCE = new CFFISOAdobeCharset();

    static
    {
        int gid = 0;
        INSTANCE.addSID(gid++, 0, ".notdef");
        INSTANCE.addSID(gid++, 1, "space");
        INSTANCE.addSID(gid++, 2, "exclam");
        INSTANCE.addSID(gid++, 3, "quotedbl");
        INSTANCE.addSID(gid++, 4, "numbersign");
        INSTANCE.addSID(gid++, 5, "dollar");
        INSTANCE.addSID(gid++, 6, "percent");
        INSTANCE.addSID(gid++, 7, "ampersand");
        INSTANCE.addSID(gid++, 8, "quoteright");
        INSTANCE.addSID(gid++, 9, "parenleft");
        INSTANCE.addSID(gid++, 10, "parenright");
        INSTANCE.addSID(gid++, 11, "asterisk");
        INSTANCE.addSID(gid++, 12, "plus");
        INSTANCE.addSID(gid++, 13, "comma");
        INSTANCE.addSID(gid++, 14, "hyphen");
        INSTANCE.addSID(gid++, 15, "period");
        INSTANCE.addSID(gid++, 16, "slash");
        INSTANCE.addSID(gid++, 17, "zero");
        INSTANCE.addSID(gid++, 18, "one");
        INSTANCE.addSID(gid++, 19, "two");
        INSTANCE.addSID(gid++, 20, "three");
        INSTANCE.addSID(gid++, 21, "four");
        INSTANCE.addSID(gid++, 22, "five");
        INSTANCE.addSID(gid++, 23, "six");
        INSTANCE.addSID(gid++, 24, "seven");
        INSTANCE.addSID(gid++, 25, "eight");
        INSTANCE.addSID(gid++, 26, "nine");
        INSTANCE.addSID(gid++, 27, "colon");
        INSTANCE.addSID(gid++, 28, "semicolon");
        INSTANCE.addSID(gid++, 29, "less");
        INSTANCE.addSID(gid++, 30, "equal");
        INSTANCE.addSID(gid++, 31, "greater");
        INSTANCE.addSID(gid++, 32, "question");
        INSTANCE.addSID(gid++, 33, "at");
        INSTANCE.addSID(gid++, 34, "A");
        INSTANCE.addSID(gid++, 35, "B");
        INSTANCE.addSID(gid++, 36, "C");
        INSTANCE.addSID(gid++, 37, "D");
        INSTANCE.addSID(gid++, 38, "E");
        INSTANCE.addSID(gid++, 39, "F");
        INSTANCE.addSID(gid++, 40, "G");
        INSTANCE.addSID(gid++, 41, "H");
        INSTANCE.addSID(gid++, 42, "I");
        INSTANCE.addSID(gid++, 43, "J");
        INSTANCE.addSID(gid++, 44, "K");
        INSTANCE.addSID(gid++, 45, "L");
        INSTANCE.addSID(gid++, 46, "M");
        INSTANCE.addSID(gid++, 47, "N");
        INSTANCE.addSID(gid++, 48, "O");
        INSTANCE.addSID(gid++, 49, "P");
        INSTANCE.addSID(gid++, 50, "Q");
        INSTANCE.addSID(gid++, 51, "R");
        INSTANCE.addSID(gid++, 52, "S");
        INSTANCE.addSID(gid++, 53, "T");
        INSTANCE.addSID(gid++, 54, "U");
        INSTANCE.addSID(gid++, 55, "V");
        INSTANCE.addSID(gid++, 56, "W");
        INSTANCE.addSID(gid++, 57, "X");
        INSTANCE.addSID(gid++, 58, "Y");
        INSTANCE.addSID(gid++, 59, "Z");
        INSTANCE.addSID(gid++, 60, "bracketleft");
        INSTANCE.addSID(gid++, 61, "backslash");
        INSTANCE.addSID(gid++, 62, "bracketright");
        INSTANCE.addSID(gid++, 63, "asciicircum");
        INSTANCE.addSID(gid++, 64, "underscore");
        INSTANCE.addSID(gid++, 65, "quoteleft");
        INSTANCE.addSID(gid++, 66, "a");
        INSTANCE.addSID(gid++, 67, "b");
        INSTANCE.addSID(gid++, 68, "c");
        INSTANCE.addSID(gid++, 69, "d");
        INSTANCE.addSID(gid++, 70, "e");
        INSTANCE.addSID(gid++, 71, "f");
        INSTANCE.addSID(gid++, 72, "g");
        INSTANCE.addSID(gid++, 73, "h");
        INSTANCE.addSID(gid++, 74, "i");
        INSTANCE.addSID(gid++, 75, "j");
        INSTANCE.addSID(gid++, 76, "k");
        INSTANCE.addSID(gid++, 77, "l");
        INSTANCE.addSID(gid++, 78, "m");
        INSTANCE.addSID(gid++, 79, "n");
        INSTANCE.addSID(gid++, 80, "o");
        INSTANCE.addSID(gid++, 81, "p");
        INSTANCE.addSID(gid++, 82, "q");
        INSTANCE.addSID(gid++, 83, "r");
        INSTANCE.addSID(gid++, 84, "s");
        INSTANCE.addSID(gid++, 85, "t");
        INSTANCE.addSID(gid++, 86, "u");
        INSTANCE.addSID(gid++, 87, "v");
        INSTANCE.addSID(gid++, 88, "w");
        INSTANCE.addSID(gid++, 89, "x");
        INSTANCE.addSID(gid++, 90, "y");
        INSTANCE.addSID(gid++, 91, "z");
        INSTANCE.addSID(gid++, 92, "braceleft");
        INSTANCE.addSID(gid++, 93, "bar");
        INSTANCE.addSID(gid++, 94, "braceright");
        INSTANCE.addSID(gid++, 95, "asciitilde");
        INSTANCE.addSID(gid++, 96, "exclamdown");
        INSTANCE.addSID(gid++, 97, "cent");
        INSTANCE.addSID(gid++, 98, "sterling");
        INSTANCE.addSID(gid++, 99, "fraction");
        INSTANCE.addSID(gid++, 100, "yen");
        INSTANCE.addSID(gid++, 101, "florin");
        INSTANCE.addSID(gid++, 102, "section");
        INSTANCE.addSID(gid++, 103, "currency");
        INSTANCE.addSID(gid++, 104, "quotesingle");
        INSTANCE.addSID(gid++, 105, "quotedblleft");
        INSTANCE.addSID(gid++, 106, "guillemotleft");
        INSTANCE.addSID(gid++, 107, "guilsinglleft");
        INSTANCE.addSID(gid++, 108, "guilsinglright");
        INSTANCE.addSID(gid++, 109, "fi");
        INSTANCE.addSID(gid++, 110, "fl");
        INSTANCE.addSID(gid++, 111, "endash");
        INSTANCE.addSID(gid++, 112, "dagger");
        INSTANCE.addSID(gid++, 113, "daggerdbl");
        INSTANCE.addSID(gid++, 114, "periodcentered");
        INSTANCE.addSID(gid++, 115, "paragraph");
        INSTANCE.addSID(gid++, 116, "bullet");
        INSTANCE.addSID(gid++, 117, "quotesinglbase");
        INSTANCE.addSID(gid++, 118, "quotedblbase");
        INSTANCE.addSID(gid++, 119, "quotedblright");
        INSTANCE.addSID(gid++, 120, "guillemotright");
        INSTANCE.addSID(gid++, 121, "ellipsis");
        INSTANCE.addSID(gid++, 122, "perthousand");
        INSTANCE.addSID(gid++, 123, "questiondown");
        INSTANCE.addSID(gid++, 124, "grave");
        INSTANCE.addSID(gid++, 125, "acute");
        INSTANCE.addSID(gid++, 126, "circumflex");
        INSTANCE.addSID(gid++, 127, "tilde");
        INSTANCE.addSID(gid++, 128, "macron");
        INSTANCE.addSID(gid++, 129, "breve");
        INSTANCE.addSID(gid++, 130, "dotaccent");
        INSTANCE.addSID(gid++, 131, "dieresis");
        INSTANCE.addSID(gid++, 132, "ring");
        INSTANCE.addSID(gid++, 133, "cedilla");
        INSTANCE.addSID(gid++, 134, "hungarumlaut");
        INSTANCE.addSID(gid++, 135, "ogonek");
        INSTANCE.addSID(gid++, 136, "caron");
        INSTANCE.addSID(gid++, 137, "emdash");
        INSTANCE.addSID(gid++, 138, "AE");
        INSTANCE.addSID(gid++, 139, "ordfeminine");
        INSTANCE.addSID(gid++, 140, "Lslash");
        INSTANCE.addSID(gid++, 141, "Oslash");
        INSTANCE.addSID(gid++, 142, "OE");
        INSTANCE.addSID(gid++, 143, "ordmasculine");
        INSTANCE.addSID(gid++, 144, "ae");
        INSTANCE.addSID(gid++, 145, "dotlessi");
        INSTANCE.addSID(gid++, 146, "lslash");
        INSTANCE.addSID(gid++, 147, "oslash");
        INSTANCE.addSID(gid++, 148, "oe");
        INSTANCE.addSID(gid++, 149, "germandbls");
        INSTANCE.addSID(gid++, 150, "onesuperior");
        INSTANCE.addSID(gid++, 151, "logicalnot");
        INSTANCE.addSID(gid++, 152, "mu");
        INSTANCE.addSID(gid++, 153, "trademark");
        INSTANCE.addSID(gid++, 154, "Eth");
        INSTANCE.addSID(gid++, 155, "onehalf");
        INSTANCE.addSID(gid++, 156, "plusminus");
        INSTANCE.addSID(gid++, 157, "Thorn");
        INSTANCE.addSID(gid++, 158, "onequarter");
        INSTANCE.addSID(gid++, 159, "divide");
        INSTANCE.addSID(gid++, 160, "brokenbar");
        INSTANCE.addSID(gid++, 161, "degree");
        INSTANCE.addSID(gid++, 162, "thorn");
        INSTANCE.addSID(gid++, 163, "threequarters");
        INSTANCE.addSID(gid++, 164, "twosuperior");
        INSTANCE.addSID(gid++, 165, "registered");
        INSTANCE.addSID(gid++, 166, "minus");
        INSTANCE.addSID(gid++, 167, "eth");
        INSTANCE.addSID(gid++, 168, "multiply");
        INSTANCE.addSID(gid++, 169, "threesuperior");
        INSTANCE.addSID(gid++, 170, "copyright");
        INSTANCE.addSID(gid++, 171, "Aacute");
        INSTANCE.addSID(gid++, 172, "Acircumflex");
        INSTANCE.addSID(gid++, 173, "Adieresis");
        INSTANCE.addSID(gid++, 174, "Agrave");
        INSTANCE.addSID(gid++, 175, "Aring");
        INSTANCE.addSID(gid++, 176, "Atilde");
        INSTANCE.addSID(gid++, 177, "Ccedilla");
        INSTANCE.addSID(gid++, 178, "Eacute");
        INSTANCE.addSID(gid++, 179, "Ecircumflex");
        INSTANCE.addSID(gid++, 180, "Edieresis");
        INSTANCE.addSID(gid++, 181, "Egrave");
        INSTANCE.addSID(gid++, 182, "Iacute");
        INSTANCE.addSID(gid++, 183, "Icircumflex");
        INSTANCE.addSID(gid++, 184, "Idieresis");
        INSTANCE.addSID(gid++, 185, "Igrave");
        INSTANCE.addSID(gid++, 186, "Ntilde");
        INSTANCE.addSID(gid++, 187, "Oacute");
        INSTANCE.addSID(gid++, 188, "Ocircumflex");
        INSTANCE.addSID(gid++, 189, "Odieresis");
        INSTANCE.addSID(gid++, 190, "Ograve");
        INSTANCE.addSID(gid++, 191, "Otilde");
        INSTANCE.addSID(gid++, 192, "Scaron");
        INSTANCE.addSID(gid++, 193, "Uacute");
        INSTANCE.addSID(gid++, 194, "Ucircumflex");
        INSTANCE.addSID(gid++, 195, "Udieresis");
        INSTANCE.addSID(gid++, 196, "Ugrave");
        INSTANCE.addSID(gid++, 197, "Yacute");
        INSTANCE.addSID(gid++, 198, "Ydieresis");
        INSTANCE.addSID(gid++, 199, "Zcaron");
        INSTANCE.addSID(gid++, 200, "aacute");
        INSTANCE.addSID(gid++, 201, "acircumflex");
        INSTANCE.addSID(gid++, 202, "adieresis");
        INSTANCE.addSID(gid++, 203, "agrave");
        INSTANCE.addSID(gid++, 204, "aring");
        INSTANCE.addSID(gid++, 205, "atilde");
        INSTANCE.addSID(gid++, 206, "ccedilla");
        INSTANCE.addSID(gid++, 207, "eacute");
        INSTANCE.addSID(gid++, 208, "ecircumflex");
        INSTANCE.addSID(gid++, 209, "edieresis");
        INSTANCE.addSID(gid++, 210, "egrave");
        INSTANCE.addSID(gid++, 211, "iacute");
        INSTANCE.addSID(gid++, 212, "icircumflex");
        INSTANCE.addSID(gid++, 213, "idieresis");
        INSTANCE.addSID(gid++, 214, "igrave");
        INSTANCE.addSID(gid++, 215, "ntilde");
        INSTANCE.addSID(gid++, 216, "oacute");
        INSTANCE.addSID(gid++, 217, "ocircumflex");
        INSTANCE.addSID(gid++, 218, "odieresis");
        INSTANCE.addSID(gid++, 219, "ograve");
        INSTANCE.addSID(gid++, 220, "otilde");
        INSTANCE.addSID(gid++, 221, "scaron");
        INSTANCE.addSID(gid++, 222, "uacute");
        INSTANCE.addSID(gid++, 223, "ucircumflex");
        INSTANCE.addSID(gid++, 224, "udieresis");
        INSTANCE.addSID(gid++, 225, "ugrave");
        INSTANCE.addSID(gid++, 226, "yacute");
        INSTANCE.addSID(gid++, 227, "ydieresis");
        INSTANCE.addSID(gid++, 228, "zcaron");
    }
}