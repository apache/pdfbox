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
package org.apache.pdfbox_ai2.pdmodel.font.encoding;

import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSName;

/**
 * This the win ansi encoding.
 * 
 * @author Ben Litchfield
 */
public class WinAnsiEncoding extends Encoding
{

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
        add(0101, "A");
        add(0306, "AE");
        add(0301, "Aacute");
        add(0302, "Acircumflex");
        add(0304, "Adieresis");
        add(0300, "Agrave");
        add(0305, "Aring");
        add(0303, "Atilde");
        add(0102, "B");
        add(0103, "C");
        add(0307, "Ccedilla");
        add(0104, "D");
        add(0105, "E");
        add(0311, "Eacute");
        add(0312, "Ecircumflex");
        add(0313, "Edieresis");
        add(0310, "Egrave");
        add(0320, "Eth");
        add(0200, "Euro");
        add(0106, "F");
        add(0107, "G");
        add(0110, "H");
        add(0111, "I");
        add(0315, "Iacute");
        add(0316, "Icircumflex");
        add(0317, "Idieresis");
        add(0314, "Igrave");
        add(0112, "J");
        add(0113, "K");
        add(0114, "L");
        add(0115, "M");
        add(0116, "N");
        add(0321, "Ntilde");
        add(0117, "O");
        add(0214, "OE");
        add(0323, "Oacute");
        add(0324, "Ocircumflex");
        add(0326, "Odieresis");
        add(0322, "Ograve");
        add(0330, "Oslash");
        add(0325, "Otilde");
        add(0120, "P");
        add(0121, "Q");
        add(0122, "R");
        add(0123, "S");
        add(0212, "Scaron");
        add(0124, "T");
        add(0336, "Thorn");
        add(0125, "U");
        add(0332, "Uacute");
        add(0333, "Ucircumflex");
        add(0334, "Udieresis");
        add(0331, "Ugrave");
        add(0126, "V");
        add(0127, "W");
        add(0130, "X");
        add(0131, "Y");
        add(0335, "Yacute");
        add(0237, "Ydieresis");
        add(0132, "Z");
        add(0216, "Zcaron");
        add(0141, "a");
        add(0341, "aacute");
        add(0342, "acircumflex");
        add(0264, "acute");
        add(0344, "adieresis");
        add(0346, "ae");
        add(0340, "agrave");
        add(046, "ampersand");
        add(0345, "aring");
        add(0136, "asciicircum");
        add(0176, "asciitilde");
        add(052, "asterisk");
        add(0100, "at");
        add(0343, "atilde");
        add(0142, "b");
        add(0134, "backslash");
        add(0174, "bar");
        add(0173, "braceleft");
        add(0175, "braceright");
        add(0133, "bracketleft");
        add(0135, "bracketright");
        add(0246, "brokenbar");
        add(0225, "bullet");
        add(0143, "c");
        add(0347, "ccedilla");
        add(0270, "cedilla");
        add(0242, "cent");
        add(0210, "circumflex");
        add(072, "colon");
        add(054, "comma");
        add(0251, "copyright");
        add(0244, "currency");
        add(0144, "d");
        add(0206, "dagger");
        add(0207, "daggerdbl");
        add(0260, "degree");
        add(0250, "dieresis");
        add(0367, "divide");
        add(044, "dollar");
        add(0145, "e");
        add(0351, "eacute");
        add(0352, "ecircumflex");
        add(0353, "edieresis");
        add(0350, "egrave");
        add(070, "eight");
        add(0205, "ellipsis");
        add(0227, "emdash");
        add(0226, "endash");
        add(075, "equal");
        add(0360, "eth");
        add(041, "exclam");
        add(0241, "exclamdown");
        add(0146, "f");
        add(065, "five");
        add(0203, "florin");
        add(064, "four");
        add(0147, "g");
        add(0337, "germandbls");
        add(0140, "grave");
        add(076, "greater");
        add(0253, "guillemotleft");
        add(0273, "guillemotright");
        add(0213, "guilsinglleft");
        add(0233, "guilsinglright");
        add(0150, "h");
        add(055, "hyphen");
        add(0151, "i");
        add(0355, "iacute");
        add(0356, "icircumflex");
        add(0357, "idieresis");
        add(0354, "igrave");
        add(0152, "j");
        add(0153, "k");
        add(0154, "l");
        add(074, "less");
        add(0254, "logicalnot");
        add(0155, "m");
        add(0257, "macron");
        add(0265, "mu");
        add(0327, "multiply");
        add(0156, "n");
        add(071, "nine");
        add(0361, "ntilde");
        add(043, "numbersign");
        add(0157, "o");
        add(0363, "oacute");
        add(0364, "ocircumflex");
        add(0366, "odieresis");
        add(0234, "oe");
        add(0362, "ograve");
        add(061, "one");
        add(0275, "onehalf");
        add(0274, "onequarter");
        add(0271, "onesuperior");
        add(0252, "ordfeminine");
        add(0272, "ordmasculine");
        add(0370, "oslash");
        add(0365, "otilde");
        add(0160, "p");
        add(0266, "paragraph");
        add(050, "parenleft");
        add(051, "parenright");
        add(045, "percent");
        add(056, "period");
        add(0267, "periodcentered");
        add(0211, "perthousand");
        add(053, "plus");
        add(0261, "plusminus");
        add(0161, "q");
        add(077, "question");
        add(0277, "questiondown");
        add(042, "quotedbl");
        add(0204, "quotedblbase");
        add(0223, "quotedblleft");
        add(0224, "quotedblright");
        add(0221, "quoteleft");
        add(0222, "quoteright");
        add(0202, "quotesinglbase");
        add(047, "quotesingle");
        add(0162, "r");
        add(0256, "registered");
        add(0163, "s");
        add(0232, "scaron");
        add(0247, "section");
        add(073, "semicolon");
        add(067, "seven");
        add(066, "six");
        add(057, "slash");
        add(040, "space");
        add(0243, "sterling");
        add(0164, "t");
        add(0376, "thorn");
        add(063, "three");
        add(0276, "threequarters");
        add(0263, "threesuperior");
        add(0230, "tilde");
        add(0231, "trademark");
        add(062, "two");
        add(0262, "twosuperior");
        add(0165, "u");
        add(0372, "uacute");
        add(0373, "ucircumflex");
        add(0374, "udieresis");
        add(0371, "ugrave");
        add(0137, "underscore");
        add(0166, "v");
        add(0167, "w");
        add(0170, "x");
        add(0171, "y");
        add(0375, "yacute");
        add(0377, "ydieresis");
        add(0245, "yen");
        add(0172, "z");
        add(0236, "zcaron");
        add(060, "zero");
        // adding some additional mappings as defined in Appendix D of the pdf spec
        add(0240, "space");
        add(0255, "hyphen");
        for (int i = 041; i <= 255; i++)
        {
            if (!codeToName.containsKey(i))
            {
                add(i, "bullet");
            }
        }
    }

    /**
     * Convert this standard java object to a COS object.
     * 
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return COSName.WIN_ANSI_ENCODING;
    }
}
