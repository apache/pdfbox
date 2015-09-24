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
    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of character codes and their corresponding names.
     */
    private static final Object[][] CFF_ISO_ADOBE_CHARSET_TABLE = {
            {0, ".notdef"},
            {1, "space"},
            {2, "exclam"},
            {3, "quotedbl"},
            {4, "numbersign"},
            {5, "dollar"},
            {6, "percent"},
            {7, "ampersand"},
            {8, "quoteright"},
            {9, "parenleft"},
            {10, "parenright"},
            {11, "asterisk"},
            {12, "plus"},
            {13, "comma"},
            {14, "hyphen"},
            {15, "period"},
            {16, "slash"},
            {17, "zero"},
            {18, "one"},
            {19, "two"},
            {20, "three"},
            {21, "four"},
            {22, "five"},
            {23, "six"},
            {24, "seven"},
            {25, "eight"},
            {26, "nine"},
            {27, "colon"},
            {28, "semicolon"},
            {29, "less"},
            {30, "equal"},
            {31, "greater"},
            {32, "question"},
            {33, "at"},
            {34, "A"},
            {35, "B"},
            {36, "C"},
            {37, "D"},
            {38, "E"},
            {39, "F"},
            {40, "G"},
            {41, "H"},
            {42, "I"},
            {43, "J"},
            {44, "K"},
            {45, "L"},
            {46, "M"},
            {47, "N"},
            {48, "O"},
            {49, "P"},
            {50, "Q"},
            {51, "R"},
            {52, "S"},
            {53, "T"},
            {54, "U"},
            {55, "V"},
            {56, "W"},
            {57, "X"},
            {58, "Y"},
            {59, "Z"},
            {60, "bracketleft"},
            {61, "backslash"},
            {62, "bracketright"},
            {63, "asciicircum"},
            {64, "underscore"},
            {65, "quoteleft"},
            {66, "a"},
            {67, "b"},
            {68, "c"},
            {69, "d"},
            {70, "e"},
            {71, "f"},
            {72, "g"},
            {73, "h"},
            {74, "i"},
            {75, "j"},
            {76, "k"},
            {77, "l"},
            {78, "m"},
            {79, "n"},
            {80, "o"},
            {81, "p"},
            {82, "q"},
            {83, "r"},
            {84, "s"},
            {85, "t"},
            {86, "u"},
            {87, "v"},
            {88, "w"},
            {89, "x"},
            {90, "y"},
            {91, "z"},
            {92, "braceleft"},
            {93, "bar"},
            {94, "braceright"},
            {95, "asciitilde"},
            {96, "exclamdown"},
            {97, "cent"},
            {98, "sterling"},
            {99, "fraction"},
            {100, "yen"},
            {101, "florin"},
            {102, "section"},
            {103, "currency"},
            {104, "quotesingle"},
            {105, "quotedblleft"},
            {106, "guillemotleft"},
            {107, "guilsinglleft"},
            {108, "guilsinglright"},
            {109, "fi"},
            {110, "fl"},
            {111, "endash"},
            {112, "dagger"},
            {113, "daggerdbl"},
            {114, "periodcentered"},
            {115, "paragraph"},
            {116, "bullet"},
            {117, "quotesinglbase"},
            {118, "quotedblbase"},
            {119, "quotedblright"},
            {120, "guillemotright"},
            {121, "ellipsis"},
            {122, "perthousand"},
            {123, "questiondown"},
            {124, "grave"},
            {125, "acute"},
            {126, "circumflex"},
            {127, "tilde"},
            {128, "macron"},
            {129, "breve"},
            {130, "dotaccent"},
            {131, "dieresis"},
            {132, "ring"},
            {133, "cedilla"},
            {134, "hungarumlaut"},
            {135, "ogonek"},
            {136, "caron"},
            {137, "emdash"},
            {138, "AE"},
            {139, "ordfeminine"},
            {140, "Lslash"},
            {141, "Oslash"},
            {142, "OE"},
            {143, "ordmasculine"},
            {144, "ae"},
            {145, "dotlessi"},
            {146, "lslash"},
            {147, "oslash"},
            {148, "oe"},
            {149, "germandbls"},
            {150, "onesuperior"},
            {151, "logicalnot"},
            {152, "mu"},
            {153, "trademark"},
            {154, "Eth"},
            {155, "onehalf"},
            {156, "plusminus"},
            {157, "Thorn"},
            {158, "onequarter"},
            {159, "divide"},
            {160, "brokenbar"},
            {161, "degree"},
            {162, "thorn"},
            {163, "threequarters"},
            {164, "twosuperior"},
            {165, "registered"},
            {166, "minus"},
            {167, "eth"},
            {168, "multiply"},
            {169, "threesuperior"},
            {170, "copyright"},
            {171, "Aacute"},
            {172, "Acircumflex"},
            {173, "Adieresis"},
            {174, "Agrave"},
            {175, "Aring"},
            {176, "Atilde"},
            {177, "Ccedilla"},
            {178, "Eacute"},
            {179, "Ecircumflex"},
            {180, "Edieresis"},
            {181, "Egrave"},
            {182, "Iacute"},
            {183, "Icircumflex"},
            {184, "Idieresis"},
            {185, "Igrave"},
            {186, "Ntilde"},
            {187, "Oacute"},
            {188, "Ocircumflex"},
            {189, "Odieresis"},
            {190, "Ograve"},
            {191, "Otilde"},
            {192, "Scaron"},
            {193, "Uacute"},
            {194, "Ucircumflex"},
            {195, "Udieresis"},
            {196, "Ugrave"},
            {197, "Yacute"},
            {198, "Ydieresis"},
            {199, "Zcaron"},
            {200, "aacute"},
            {201, "acircumflex"},
            {202, "adieresis"},
            {203, "agrave"},
            {204, "aring"},
            {205, "atilde"},
            {206, "ccedilla"},
            {207, "eacute"},
            {208, "ecircumflex"},
            {209, "edieresis"},
            {210, "egrave"},
            {211, "iacute"},
            {212, "icircumflex"},
            {213, "idieresis"},
            {214, "igrave"},
            {215, "ntilde"},
            {216, "oacute"},
            {217, "ocircumflex"},
            {218, "odieresis"},
            {219, "ograve"},
            {220, "otilde"},
            {221, "scaron"},
            {222, "uacute"},
            {223, "ucircumflex"},
            {224, "udieresis"},
            {225, "ugrave"},
            {226, "yacute"},
            {227, "ydieresis"},
            {228, "zcaron"}
    };
    
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
        for (Object[] charsetEntry : CFF_ISO_ADOBE_CHARSET_TABLE)
        {
            INSTANCE.addSID(gid++, (Integer) charsetEntry[CHAR_CODE], charsetEntry[CHAR_NAME].toString());
        }
    }
}