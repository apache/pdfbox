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
public class StandardEncoding extends Encoding
{

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
        add(0101, "A");
        add(0341, "AE");
        add(0102, "B");
        add(0103, "C");
        add(0104, "D");
        add(0105, "E");
        add(0106, "F");
        add(0107, "G");
        add(0110, "H");
        add(0111, "I");
        add(0112, "J");
        add(0113, "K");
        add(0114, "L");
        add(0350, "Lslash");
        add(0115, "M");
        add(0116, "N");
        add(0117, "O");
        add(0352, "OE");
        add(0351, "Oslash");
        add(0120, "P");
        add(0121, "Q");
        add(0122, "R");
        add(0123, "S");
        add(0124, "T");
        add(0125, "U");
        add(0126, "V");
        add(0127, "W");
        add(0130, "X");
        add(0131, "Y");
        add(0132, "Z");
        add(0141, "a");
        add(0302, "acute");
        add(0361, "ae");
        add(0046, "ampersand");
        add(0136, "asciicircum");
        add(0176, "asciitilde");
        add(0052, "asterisk");
        add(0100, "at");
        add(0142, "b");
        add(0134, "backslash");
        add(0174, "bar");
        add(0173, "braceleft");
        add(0175, "braceright");
        add(0133, "bracketleft");
        add(0135, "bracketright");
        add(0306, "breve");
        add(0267, "bullet");
        add(0143, "c");
        add(0317, "caron");
        add(0313, "cedilla");
        add(0242, "cent");
        add(0303, "circumflex");
        add(0072, "colon");
        add(0054, "comma");
        add(0250, "currency");
        add(0144, "d");
        add(0262, "dagger");
        add(0263, "daggerdbl");
        add(0310, "dieresis");
        add(0044, "dollar");
        add(0307, "dotaccent");
        add(0365, "dotlessi");
        add(0145, "e");
        add(0070, "eight");
        add(0274, "ellipsis");
        add(0320, "emdash");
        add(0261, "endash");
        add(0075, "equal");
        add(0041, "exclam");
        add(0241, "exclamdown");
        add(0146, "f");
        add(0256, "fi");
        add(0065, "five");
        add(0257, "fl");
        add(0246, "florin");
        add(0064, "four");
        add(0244, "fraction");
        add(0147, "g");
        add(0373, "germandbls");
        add(0301, "grave");
        add(0076, "greater");
        add(0253, "guillemotleft");
        add(0273, "guillemotright");
        add(0254, "guilsinglleft");
        add(0255, "guilsinglright");
        add(0150, "h");
        add(0315, "hungarumlaut");
        add(0055, "hyphen");
        add(0151, "i");
        add(0152, "j");
        add(0153, "k");
        add(0154, "l");
        add(0074, "less");
        add(0370, "lslash");
        add(0155, "m");
        add(0305, "macron");
        add(0156, "n");
        add(0071, "nine");
        add(0043, "numbersign");
        add(0157, "o");
        add(0372, "oe");
        add(0316, "ogonek");
        add(0061, "one");
        add(0343, "ordfeminine");
        add(0353, "ordmasculine");
        add(0371, "oslash");
        add(0160, "p");
        add(0266, "paragraph");
        add(0050, "parenleft");
        add(0051, "parenright");
        add(0045, "percent");
        add(0056, "period");
        add(0264, "periodcentered");
        add(0275, "perthousand");
        add(0053, "plus");
        add(0161, "q");
        add(0077, "question");
        add(0277, "questiondown");
        add(0042, "quotedbl");
        add(0271, "quotedblbase");
        add(0252, "quotedblleft");
        add(0272, "quotedblright");
        add(0140, "quoteleft");
        add(0047, "quoteright");
        add(0270, "quotesinglbase");
        add(0251, "quotesingle");
        add(0162, "r");
        add(0312, "ring");
        add(0163, "s");
        add(0247, "section");
        add(0073, "semicolon");
        add(0067, "seven");
        add(0066, "six");
        add(0057, "slash");
        add(0040, "space");
        add(0243, "sterling");
        add(0164, "t");
        add(0063, "three");
        add(0304, "tilde");
        add(0062, "two");
        add(0165, "u");
        add(0137, "underscore");
        add(0166, "v");
        add(0167, "w");
        add(0170, "x");
        add(0171, "y");
        add(0245, "yen");
        add(0172, "z");
        add(0060, "zero");
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return COSName.STANDARD_ENCODING;
    }
}
