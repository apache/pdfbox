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
 * This is an interface to a text encoder.
 *
 * @author Ben Litchfield
 */
public class MacRomanEncoding extends Encoding
{

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
        add(0101, "A");
        add(0256, "AE");
        add(0347, "Aacute");
        add(0345, "Acircumflex");
        add(0200, "Adieresis");
        add(0313, "Agrave");
        add(0201, "Aring");
        add(0314, "Atilde");
        add(0102, "B");
        add(0103, "C");
        add(0202, "Ccedilla");
        add(0104, "D");
        add(0105, "E");
        add(0203, "Eacute");
        add(0346, "Ecircumflex");
        add(0350, "Edieresis");
        add(0351, "Egrave");
        add(0106, "F");
        add(0107, "G");
        add(0110, "H");
        add(0111, "I");
        add(0352, "Iacute");
        add(0353, "Icircumflex");
        add(0354, "Idieresis");
        add(0355, "Igrave");
        add(0112, "J");
        add(0113, "K");
        add(0114, "L");
        add(0115, "M");
        add(0116, "N");
        add(0204, "Ntilde");
        add(0117, "O");
        add(0316, "OE");
        add(0356, "Oacute");
        add(0357, "Ocircumflex");
        add(0205, "Odieresis");
        add(0361, "Ograve");
        add(0257, "Oslash");
        add(0315, "Otilde");
        add(0120, "P");
        add(0121, "Q");
        add(0122, "R");
        add(0123, "S");
        add(0124, "T");
        add(0125, "U");
        add(0362, "Uacute");
        add(0363, "Ucircumflex");
        add(0206, "Udieresis");
        add(0364, "Ugrave");
        add(0126, "V");
        add(0127, "W");
        add(0130, "X");
        add(0131, "Y");
        add(0331, "Ydieresis");
        add(0132, "Z");
        add(0141, "a");
        add(0207, "aacute");
        add(0211, "acircumflex");
        add(0253, "acute");
        add(0212, "adieresis");
        add(0276, "ae");
        add(0210, "agrave");
        add(046, "ampersand");
        add(0214, "aring");
        add(0136, "asciicircum");
        add(0176, "asciitilde");
        add(052, "asterisk");
        add(0100, "at");
        add(0213, "atilde");
        add(0142, "b");
        add(0134, "backslash");
        add(0174, "bar");
        add(0173, "braceleft");
        add(0175, "braceright");
        add(0133, "bracketleft");
        add(0135, "bracketright");
        add(0371, "breve");
        add(0245, "bullet");
        add(0143, "c");
        add(0377, "caron");
        add(0215, "ccedilla");
        add(0374, "cedilla");
        add(0242, "cent");
        add(0366, "circumflex");
        add(072, "colon");
        add(054, "comma");
        add(0251, "copyright");
        add(0333, "currency");
        add(0144, "d");
        add(0240, "dagger");
        add(0340, "daggerdbl");
        add(0241, "degree");
        add(0254, "dieresis");
        add(0326, "divide");
        add(044, "dollar");
        add(0372, "dotaccent");
        add(0365, "dotlessi");
        add(0145, "e");
        add(0216, "eacute");
        add(0220, "ecircumflex");
        add(0221, "edieresis");
        add(0217, "egrave");
        add(070, "eight");
        add(0311, "ellipsis");
        add(0321, "emdash");
        add(0320, "endash");
        add(075, "equal");
        add(041, "exclam");
        add(0301, "exclamdown");
        add(0146, "f");
        add(0336, "fi");
        add(065, "five");
        add(0337, "fl");
        add(0304, "florin");
        add(064, "four");
        add(0332, "fraction");
        add(0147, "g");
        add(0247, "germandbls");
        add(0140, "grave");
        add(076, "greater");
        add(0307, "guillemotleft");
        add(0310, "guillemotright");
        add(0334, "guilsinglleft");
        add(0335, "guilsinglright");
        add(0150, "h");
        add(0375, "hungarumlaut");
        add(055, "hyphen");
        add(0151, "i");
        add(0222, "iacute");
        add(0224, "icircumflex");
        add(0225, "idieresis");
        add(0223, "igrave");
        add(0152, "j");
        add(0153, "k");
        add(0154, "l");
        add(074, "less");
        add(0302, "logicalnot");
        add(0155, "m");
        add(0370, "macron");
        add(0265, "mu");
        add(0156, "n");
        add(071, "nine");
        add(0226, "ntilde");
        add(043, "numbersign");
        add(0157, "o");
        add(0227, "oacute");
        add(0231, "ocircumflex");
        add(0232, "odieresis");
        add(0317, "oe");
        add(0376, "ogonek");
        add(0230, "ograve");
        add(061, "one");
        add(0273, "ordfeminine");
        add(0274, "ordmasculine");
        add(0277, "oslash");
        add(0233, "otilde");
        add(0160, "p");
        add(0246, "paragraph");
        add(050, "parenleft");
        add(051, "parenright");
        add(045, "percent");
        add(056, "period");
        add(0341, "periodcentered");
        add(0344, "perthousand");
        add(053, "plus");
        add(0261, "plusminus");
        add(0161, "q");
        add(077, "question");
        add(0300, "questiondown");
        add(042, "quotedbl");
        add(0343, "quotedblbase");
        add(0322, "quotedblleft");
        add(0323, "quotedblright");
        add(0324, "quoteleft");
        add(0325, "quoteright");
        add(0342, "quotesinglbase");
        add(047, "quotesingle");
        add(0162, "r");
        add(0250, "registered");
        add(0373, "ring");
        add(0163, "s");
        add(0244, "section");
        add(073, "semicolon");
        add(067, "seven");
        add(066, "six");
        add(057, "slash");
        add(040, "space");
        add(0243, "sterling");
        add(0164, "t");
        add(063, "three");
        add(0367, "tilde");
        add(0252, "trademark");
        add(062, "two");
        add(0165, "u");
        add(0234, "uacute");
        add(0236, "ucircumflex");
        add(0237, "udieresis");
        add(0235, "ugrave");
        add(0137, "underscore");
        add(0166, "v");
        add(0167, "w");
        add(0170, "x");
        add(0171, "y");
        add(0330, "ydieresis");
        add(0264, "yen");
        add(0172, "z");
        add(060, "zero");
        // adding an additional mapping as defined in Appendix D of the pdf spec
        add(0312, "space");
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return COSName.MAC_ROMAN_ENCODING;
    }
}
