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
public class StandardEncoding extends Encoding
{

    
    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names.
     */
    private static final Object[][] STANDARD_ENCODING_TABLE = {
            {0101, "A"},
            {0341, "AE"},
            {0102, "B"},
            {0103, "C"},
            {0104, "D"},
            {0105, "E"},
            {0106, "F"},
            {0107, "G"},
            {0110, "H"},
            {0111, "I"},
            {0112, "J"},
            {0113, "K"},
            {0114, "L"},
            {0350, "Lslash"},
            {0115, "M"},
            {0116, "N"},
            {0117, "O"},
            {0352, "OE"},
            {0351, "Oslash"},
            {0120, "P"},
            {0121, "Q"},
            {0122, "R"},
            {0123, "S"},
            {0124, "T"},
            {0125, "U"},
            {0126, "V"},
            {0127, "W"},
            {0130, "X"},
            {0131, "Y"},
            {0132, "Z"},
            {0141, "a"},
            {0302, "acute"},
            {0361, "ae"},
            {0046, "ampersand"},
            {0136, "asciicircum"},
            {0176, "asciitilde"},
            {0052, "asterisk"},
            {0100, "at"},
            {0142, "b"},
            {0134, "backslash"},
            {0174, "bar"},
            {0173, "braceleft"},
            {0175, "braceright"},
            {0133, "bracketleft"},
            {0135, "bracketright"},
            {0306, "breve"},
            {0267, "bullet"},
            {0143, "c"},
            {0317, "caron"},
            {0313, "cedilla"},
            {0242, "cent"},
            {0303, "circumflex"},
            {0072, "colon"},
            {0054, "comma"},
            {0250, "currency"},
            {0144, "d"},
            {0262, "dagger"},
            {0263, "daggerdbl"},
            {0310, "dieresis"},
            {0044, "dollar"},
            {0307, "dotaccent"},
            {0365, "dotlessi"},
            {0145, "e"},
            {0070, "eight"},
            {0274, "ellipsis"},
            {0320, "emdash"},
            {0261, "endash"},
            {0075, "equal"},
            {0041, "exclam"},
            {0241, "exclamdown"},
            {0146, "f"},
            {0256, "fi"},
            {0065, "five"},
            {0257, "fl"},
            {0246, "florin"},
            {0064, "four"},
            {0244, "fraction"},
            {0147, "g"},
            {0373, "germandbls"},
            {0301, "grave"},
            {0076, "greater"},
            {0253, "guillemotleft"},
            {0273, "guillemotright"},
            {0254, "guilsinglleft"},
            {0255, "guilsinglright"},
            {0150, "h"},
            {0315, "hungarumlaut"},
            {0055, "hyphen"},
            {0151, "i"},
            {0152, "j"},
            {0153, "k"},
            {0154, "l"},
            {0074, "less"},
            {0370, "lslash"},
            {0155, "m"},
            {0305, "macron"},
            {0156, "n"},
            {0071, "nine"},
            {0043, "numbersign"},
            {0157, "o"},
            {0372, "oe"},
            {0316, "ogonek"},
            {0061, "one"},
            {0343, "ordfeminine"},
            {0353, "ordmasculine"},
            {0371, "oslash"},
            {0160, "p"},
            {0266, "paragraph"},
            {0050, "parenleft"},
            {0051, "parenright"},
            {0045, "percent"},
            {0056, "period"},
            {0264, "periodcentered"},
            {0275, "perthousand"},
            {0053, "plus"},
            {0161, "q"},
            {0077, "question"},
            {0277, "questiondown"},
            {0042, "quotedbl"},
            {0271, "quotedblbase"},
            {0252, "quotedblleft"},
            {0272, "quotedblright"},
            {0140, "quoteleft"},
            {0047, "quoteright"},
            {0270, "quotesinglbase"},
            {0251, "quotesingle"},
            {0162, "r"},
            {0312, "ring"},
            {0163, "s"},
            {0247, "section"},
            {0073, "semicolon"},
            {0067, "seven"},
            {0066, "six"},
            {0057, "slash"},
            {0040, "space"},
            {0243, "sterling"},
            {0164, "t"},
            {0063, "three"},
            {0304, "tilde"},
            {0062, "two"},
            {0165, "u"},
            {0137, "underscore"},
            {0166, "v"},
            {0167, "w"},
            {0170, "x"},
            {0171, "y"},
            {0245, "yen"},
            {0172, "z"},
            {0060, "zero"}
    };

    /**
     * Singleton instance of this class.
     *
     * @since Apache PDFBox 1.3.0
     */
    public static final StandardEncoding INSTANCE = new StandardEncoding();

    /**
     * Constructor.
     */
    public StandardEncoding()
    {
        for (Object[] encodingEntry : STANDARD_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }
    }

    @Override
    public COSBase getCOSObject()
    {
        return COSName.STANDARD_ENCODING;
    }

    @Override
    public String getEncodingName()
    {
        return "StandardEncoding";
    }
}
