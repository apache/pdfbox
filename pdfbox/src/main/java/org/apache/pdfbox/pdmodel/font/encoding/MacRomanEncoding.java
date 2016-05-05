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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

/**
 * This is an interface to a text encoder.
 *
 * @author Ben Litchfield
 */
public class MacRomanEncoding extends Encoding
{

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names.
     */
    private static final Object[][] MAC_ROMAN_ENCODING_TABLE = {
            {0101, "A"},
            {0256, "AE"},
            {0347, "Aacute"},
            {0345, "Acircumflex"},
            {0200, "Adieresis"},
            {0313, "Agrave"},
            {0201, "Aring"},
            {0314, "Atilde"},
            {0102, "B"},
            {0103, "C"},
            {0202, "Ccedilla"},
            {0104, "D"},
            {0105, "E"},
            {0203, "Eacute"},
            {0346, "Ecircumflex"},
            {0350, "Edieresis"},
            {0351, "Egrave"},
            {0106, "F"},
            {0107, "G"},
            {0110, "H"},
            {0111, "I"},
            {0352, "Iacute"},
            {0353, "Icircumflex"},
            {0354, "Idieresis"},
            {0355, "Igrave"},
            {0112, "J"},
            {0113, "K"},
            {0114, "L"},
            {0115, "M"},
            {0116, "N"},
            {0204, "Ntilde"},
            {0117, "O"},
            {0316, "OE"},
            {0356, "Oacute"},
            {0357, "Ocircumflex"},
            {0205, "Odieresis"},
            {0361, "Ograve"},
            {0257, "Oslash"},
            {0315, "Otilde"},
            {0120, "P"},
            {0121, "Q"},
            {0122, "R"},
            {0123, "S"},
            {0124, "T"},
            {0125, "U"},
            {0362, "Uacute"},
            {0363, "Ucircumflex"},
            {0206, "Udieresis"},
            {0364, "Ugrave"},
            {0126, "V"},
            {0127, "W"},
            {0130, "X"},
            {0131, "Y"},
            {0331, "Ydieresis"},
            {0132, "Z"},
            {0141, "a"},
            {0207, "aacute"},
            {0211, "acircumflex"},
            {0253, "acute"},
            {0212, "adieresis"},
            {0276, "ae"},
            {0210, "agrave"},
            {046, "ampersand"},
            {0214, "aring"},
            {0136, "asciicircum"},
            {0176, "asciitilde"},
            {052, "asterisk"},
            {0100, "at"},
            {0213, "atilde"},
            {0142, "b"},
            {0134, "backslash"},
            {0174, "bar"},
            {0173, "braceleft"},
            {0175, "braceright"},
            {0133, "bracketleft"},
            {0135, "bracketright"},
            {0371, "breve"},
            {0245, "bullet"},
            {0143, "c"},
            {0377, "caron"},
            {0215, "ccedilla"},
            {0374, "cedilla"},
            {0242, "cent"},
            {0366, "circumflex"},
            {072, "colon"},
            {054, "comma"},
            {0251, "copyright"},
            {0333, "currency"},
            {0144, "d"},
            {0240, "dagger"},
            {0340, "daggerdbl"},
            {0241, "degree"},
            {0254, "dieresis"},
            {0326, "divide"},
            {044, "dollar"},
            {0372, "dotaccent"},
            {0365, "dotlessi"},
            {0145, "e"},
            {0216, "eacute"},
            {0220, "ecircumflex"},
            {0221, "edieresis"},
            {0217, "egrave"},
            {070, "eight"},
            {0311, "ellipsis"},
            {0321, "emdash"},
            {0320, "endash"},
            {075, "equal"},
            {041, "exclam"},
            {0301, "exclamdown"},
            {0146, "f"},
            {0336, "fi"},
            {065, "five"},
            {0337, "fl"},
            {0304, "florin"},
            {064, "four"},
            {0332, "fraction"},
            {0147, "g"},
            {0247, "germandbls"},
            {0140, "grave"},
            {076, "greater"},
            {0307, "guillemotleft"},
            {0310, "guillemotright"},
            {0334, "guilsinglleft"},
            {0335, "guilsinglright"},
            {0150, "h"},
            {0375, "hungarumlaut"},
            {055, "hyphen"},
            {0151, "i"},
            {0222, "iacute"},
            {0224, "icircumflex"},
            {0225, "idieresis"},
            {0223, "igrave"},
            {0152, "j"},
            {0153, "k"},
            {0154, "l"},
            {074, "less"},
            {0302, "logicalnot"},
            {0155, "m"},
            {0370, "macron"},
            {0265, "mu"},
            {0156, "n"},
            {071, "nine"},
            {0226, "ntilde"},
            {043, "numbersign"},
            {0157, "o"},
            {0227, "oacute"},
            {0231, "ocircumflex"},
            {0232, "odieresis"},
            {0317, "oe"},
            {0376, "ogonek"},
            {0230, "ograve"},
            {061, "one"},
            {0273, "ordfeminine"},
            {0274, "ordmasculine"},
            {0277, "oslash"},
            {0233, "otilde"},
            {0160, "p"},
            {0246, "paragraph"},
            {050, "parenleft"},
            {051, "parenright"},
            {045, "percent"},
            {056, "period"},
            {0341, "periodcentered"},
            {0344, "perthousand"},
            {053, "plus"},
            {0261, "plusminus"},
            {0161, "q"},
            {077, "question"},
            {0300, "questiondown"},
            {042, "quotedbl"},
            {0343, "quotedblbase"},
            {0322, "quotedblleft"},
            {0323, "quotedblright"},
            {0324, "quoteleft"},
            {0325, "quoteright"},
            {0342, "quotesinglbase"},
            {047, "quotesingle"},
            {0162, "r"},
            {0250, "registered"},
            {0373, "ring"},
            {0163, "s"},
            {0244, "section"},
            {073, "semicolon"},
            {067, "seven"},
            {066, "six"},
            {057, "slash"},
            {040, "space"},
            {0243, "sterling"},
            {0164, "t"},
            {063, "three"},
            {0367, "tilde"},
            {0252, "trademark"},
            {062, "two"},
            {0165, "u"},
            {0234, "uacute"},
            {0236, "ucircumflex"},
            {0237, "udieresis"},
            {0235, "ugrave"},
            {0137, "underscore"},
            {0166, "v"},
            {0167, "w"},
            {0170, "x"},
            {0171, "y"},
            {0330, "ydieresis"},
            {0264, "yen"},
            {0172, "z"},
            {060, "zero"},
            // adding an additional mapping as defined in Appendix D of the pdf spec
            {0312, "space"}
    };
    
    /**
     * Singleton instance of this class.
     *
     * @since Apache PDFBox 1.3.0
     */
    public static final MacRomanEncoding INSTANCE = new MacRomanEncoding();

    /**
     * Constructor.
     */
    public MacRomanEncoding()
    {
        for (Object[] encodingEntry : MAC_ROMAN_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }
    }
    
    @Override
    public COSBase getCOSObject()
    {
        return COSName.MAC_ROMAN_ENCODING;
    }

    @Override
    public String getEncodingName()
    {
        return "MacRomanEncoding";
    }
}
