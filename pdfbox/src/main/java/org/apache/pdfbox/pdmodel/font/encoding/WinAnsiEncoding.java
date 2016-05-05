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
 * This the win ansi encoding.
 * 
 * @author Ben Litchfield
 */
public class WinAnsiEncoding extends Encoding
{

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names.
     */
    private static final Object[][] WIN_ANSI_ENCODING_TABLE = {
            {0101, "A"},
            {0306, "AE"},
            {0301, "Aacute"},
            {0302, "Acircumflex"},
            {0304, "Adieresis"},
            {0300, "Agrave"},
            {0305, "Aring"},
            {0303, "Atilde"},
            {0102, "B"},
            {0103, "C"},
            {0307, "Ccedilla"},
            {0104, "D"},
            {0105, "E"},
            {0311, "Eacute"},
            {0312, "Ecircumflex"},
            {0313, "Edieresis"},
            {0310, "Egrave"},
            {0320, "Eth"},
            {0200, "Euro"},
            {0106, "F"},
            {0107, "G"},
            {0110, "H"},
            {0111, "I"},
            {0315, "Iacute"},
            {0316, "Icircumflex"},
            {0317, "Idieresis"},
            {0314, "Igrave"},
            {0112, "J"},
            {0113, "K"},
            {0114, "L"},
            {0115, "M"},
            {0116, "N"},
            {0321, "Ntilde"},
            {0117, "O"},
            {0214, "OE"},
            {0323, "Oacute"},
            {0324, "Ocircumflex"},
            {0326, "Odieresis"},
            {0322, "Ograve"},
            {0330, "Oslash"},
            {0325, "Otilde"},
            {0120, "P"},
            {0121, "Q"},
            {0122, "R"},
            {0123, "S"},
            {0212, "Scaron"},
            {0124, "T"},
            {0336, "Thorn"},
            {0125, "U"},
            {0332, "Uacute"},
            {0333, "Ucircumflex"},
            {0334, "Udieresis"},
            {0331, "Ugrave"},
            {0126, "V"},
            {0127, "W"},
            {0130, "X"},
            {0131, "Y"},
            {0335, "Yacute"},
            {0237, "Ydieresis"},
            {0132, "Z"},
            {0216, "Zcaron"},
            {0141, "a"},
            {0341, "aacute"},
            {0342, "acircumflex"},
            {0264, "acute"},
            {0344, "adieresis"},
            {0346, "ae"},
            {0340, "agrave"},
            {046, "ampersand"},
            {0345, "aring"},
            {0136, "asciicircum"},
            {0176, "asciitilde"},
            {052, "asterisk"},
            {0100, "at"},
            {0343, "atilde"},
            {0142, "b"},
            {0134, "backslash"},
            {0174, "bar"},
            {0173, "braceleft"},
            {0175, "braceright"},
            {0133, "bracketleft"},
            {0135, "bracketright"},
            {0246, "brokenbar"},
            {0225, "bullet"},
            {0143, "c"},
            {0347, "ccedilla"},
            {0270, "cedilla"},
            {0242, "cent"},
            {0210, "circumflex"},
            {072, "colon"},
            {054, "comma"},
            {0251, "copyright"},
            {0244, "currency"},
            {0144, "d"},
            {0206, "dagger"},
            {0207, "daggerdbl"},
            {0260, "degree"},
            {0250, "dieresis"},
            {0367, "divide"},
            {044, "dollar"},
            {0145, "e"},
            {0351, "eacute"},
            {0352, "ecircumflex"},
            {0353, "edieresis"},
            {0350, "egrave"},
            {070, "eight"},
            {0205, "ellipsis"},
            {0227, "emdash"},
            {0226, "endash"},
            {075, "equal"},
            {0360, "eth"},
            {041, "exclam"},
            {0241, "exclamdown"},
            {0146, "f"},
            {065, "five"},
            {0203, "florin"},
            {064, "four"},
            {0147, "g"},
            {0337, "germandbls"},
            {0140, "grave"},
            {076, "greater"},
            {0253, "guillemotleft"},
            {0273, "guillemotright"},
            {0213, "guilsinglleft"},
            {0233, "guilsinglright"},
            {0150, "h"},
            {055, "hyphen"},
            {0151, "i"},
            {0355, "iacute"},
            {0356, "icircumflex"},
            {0357, "idieresis"},
            {0354, "igrave"},
            {0152, "j"},
            {0153, "k"},
            {0154, "l"},
            {074, "less"},
            {0254, "logicalnot"},
            {0155, "m"},
            {0257, "macron"},
            {0265, "mu"},
            {0327, "multiply"},
            {0156, "n"},
            {071, "nine"},
            {0361, "ntilde"},
            {043, "numbersign"},
            {0157, "o"},
            {0363, "oacute"},
            {0364, "ocircumflex"},
            {0366, "odieresis"},
            {0234, "oe"},
            {0362, "ograve"},
            {061, "one"},
            {0275, "onehalf"},
            {0274, "onequarter"},
            {0271, "onesuperior"},
            {0252, "ordfeminine"},
            {0272, "ordmasculine"},
            {0370, "oslash"},
            {0365, "otilde"},
            {0160, "p"},
            {0266, "paragraph"},
            {050, "parenleft"},
            {051, "parenright"},
            {045, "percent"},
            {056, "period"},
            {0267, "periodcentered"},
            {0211, "perthousand"},
            {053, "plus"},
            {0261, "plusminus"},
            {0161, "q"},
            {077, "question"},
            {0277, "questiondown"},
            {042, "quotedbl"},
            {0204, "quotedblbase"},
            {0223, "quotedblleft"},
            {0224, "quotedblright"},
            {0221, "quoteleft"},
            {0222, "quoteright"},
            {0202, "quotesinglbase"},
            {047, "quotesingle"},
            {0162, "r"},
            {0256, "registered"},
            {0163, "s"},
            {0232, "scaron"},
            {0247, "section"},
            {073, "semicolon"},
            {067, "seven"},
            {066, "six"},
            {057, "slash"},
            {040, "space"},
            {0243, "sterling"},
            {0164, "t"},
            {0376, "thorn"},
            {063, "three"},
            {0276, "threequarters"},
            {0263, "threesuperior"},
            {0230, "tilde"},
            {0231, "trademark"},
            {062, "two"},
            {0262, "twosuperior"},
            {0165, "u"},
            {0372, "uacute"},
            {0373, "ucircumflex"},
            {0374, "udieresis"},
            {0371, "ugrave"},
            {0137, "underscore"},
            {0166, "v"},
            {0167, "w"},
            {0170, "x"},
            {0171, "y"},
            {0375, "yacute"},
            {0377, "ydieresis"},
            {0245, "yen"},
            {0172, "z"},
            {0236, "zcaron"},
            {060, "zero"},
            // adding some additional mappings as defined in Appendix D of the pdf spec
            {0240, "space"},
            {0255, "hyphen"}
    };

    /**
     * Singleton instance of this class.
     * 
     * @since Apache PDFBox 1.3.0
     */
    public static final WinAnsiEncoding INSTANCE = new WinAnsiEncoding();

    /**
     * Constructor.
     */
    public WinAnsiEncoding()
    {
        for (Object[] encodingEntry : WIN_ANSI_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }

        // From the PDF specification:
        // In WinAnsiEncoding, all unused codes greater than 40 map to the bullet character.
        for (int i = 041; i <= 255; i++)
        {
            if (!codeToName.containsKey(i))
            {
                add(i, "bullet");
            }
        }
    }

    @Override
    public COSBase getCOSObject()
    {
        return COSName.WIN_ANSI_ENCODING;
    }

    @Override
    public String getEncodingName()
    {
        return "WinAnsiEncoding";
    }
}
