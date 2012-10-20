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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

/**
 * This is an interface to a text encoder.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class PdfDocEncoding extends Encoding
{

    /**
     * Singleton instance of this class.
     *
     * @since Apache PDFBox 1.3.0
     */
    public static final PdfDocEncoding INSTANCE = new PdfDocEncoding();

    /**
     * Constructor.
     */
    public PdfDocEncoding()
    {
        addCharacterEncoding( 0101, "A" );
        addCharacterEncoding( 0306, "AE" );
        addCharacterEncoding( 0301, "Aacute" );
        addCharacterEncoding( 0302, "Acircumflex" );
        addCharacterEncoding( 0304, "Adieresis" );
        addCharacterEncoding( 0300, "Agrave" );
        addCharacterEncoding( 0305, "Aring" );
        addCharacterEncoding( 0303, "Atilde" );
        addCharacterEncoding( 0102, "B" );
        addCharacterEncoding( 0103, "C" );
        addCharacterEncoding( 0307, "Ccedilla" );
        addCharacterEncoding( 0104, "D" );
        addCharacterEncoding( 0105, "E" );
        addCharacterEncoding( 0311, "Eacute" );
        addCharacterEncoding( 0312, "Ecircumflex" );
        addCharacterEncoding( 0313, "Edieresis" );
        addCharacterEncoding( 0310, "Egrave" );
        addCharacterEncoding( 0320, "Eth" );
        addCharacterEncoding( 0240, "Euro" );
        addCharacterEncoding( 0106, "F" );
        addCharacterEncoding( 0107, "G" );
        addCharacterEncoding( 0110, "H" );
        addCharacterEncoding( 0111, "I" );
        addCharacterEncoding( 0315, "Iacute" );
        addCharacterEncoding( 0316, "Icircumflex" );
        addCharacterEncoding( 0317, "Idieresis" );
        addCharacterEncoding( 0314, "Igrave" );
        addCharacterEncoding( 0112, "J" );
        addCharacterEncoding( 0113, "K" );
        addCharacterEncoding( 0114, "L" );
        addCharacterEncoding( 0225, "Lslash" );
        addCharacterEncoding( 0115, "M" );
        addCharacterEncoding( 0116, "N" );
        addCharacterEncoding( 0321, "Ntilde" );
        addCharacterEncoding( 0117, "O" );
        addCharacterEncoding( 0226, "OE" );
        addCharacterEncoding( 0323, "Oacute" );
        addCharacterEncoding( 0324, "Ocircumflex" );
        addCharacterEncoding( 0326, "Odieresis" );
        addCharacterEncoding( 0322, "Ograve" );
        addCharacterEncoding( 0330, "Oslash" );
        addCharacterEncoding( 0325, "Otilde" );
        addCharacterEncoding( 0120, "P" );
        addCharacterEncoding( 0121, "Q" );
        addCharacterEncoding( 0122, "R" );
        addCharacterEncoding( 0123, "S" );
        addCharacterEncoding( 0227, "Scaron" );
        addCharacterEncoding( 0124, "T" );
        addCharacterEncoding( 0336, "Thorn" );
        addCharacterEncoding( 0125, "U" );
        addCharacterEncoding( 0332, "Uacute" );
        addCharacterEncoding( 0333, "Ucircumflex" );
        addCharacterEncoding( 0334, "Udieresis" );
        addCharacterEncoding( 0331, "Ugrave" );
        addCharacterEncoding( 0126, "V" );
        addCharacterEncoding( 0127, "W" );
        addCharacterEncoding( 0130, "X" );
        addCharacterEncoding( 0131, "Y" );
        addCharacterEncoding( 0335, "Yacute" );
        addCharacterEncoding( 0230, "Ydieresis" );
        addCharacterEncoding( 0132, "Z" );
        addCharacterEncoding( 0231, "Zcaron" );
        addCharacterEncoding( 0141, "a" );
        addCharacterEncoding( 0341, "aacute" );
        addCharacterEncoding( 0342, "acircumflex" );
        addCharacterEncoding( 0264, "acute" );
        addCharacterEncoding( 0344, "adieresis" );
        addCharacterEncoding( 0346, "ae" );
        addCharacterEncoding( 0340, "agrave" );
        addCharacterEncoding( 046, "ampersand" );
        addCharacterEncoding( 0345, "aring" );
        addCharacterEncoding( 0136, "asciicircum" );
        addCharacterEncoding( 0176, "asciitilde" );
        addCharacterEncoding( 052, "asterisk" );
        addCharacterEncoding( 0100, "at" );
        addCharacterEncoding( 0343, "atilde" );
        addCharacterEncoding( 0142, "b" );
        addCharacterEncoding( 0134, "backslash" );
        addCharacterEncoding( 0174, "bar" );
        addCharacterEncoding( 0173, "braceleft" );
        addCharacterEncoding( 0175, "braceright" );
        addCharacterEncoding( 0133, "bracketleft" );
        addCharacterEncoding( 0135, "bracketright" );
        addCharacterEncoding( 030, "breve" );
        addCharacterEncoding( 0246, "brokenbar" );
        addCharacterEncoding( 0200, "bullet" );
        addCharacterEncoding( 0143, "c" );
        addCharacterEncoding( 031, "caron" );
        addCharacterEncoding( 0347, "ccedilla" );
        addCharacterEncoding( 0270, "cedilla" );
        addCharacterEncoding( 0242, "cent" );
        addCharacterEncoding( 032, "circumflex" );
        addCharacterEncoding( 072, "colon" );
        addCharacterEncoding( 054, "comma" );
        addCharacterEncoding( 0251, "copyright" );
        addCharacterEncoding( 0244, "currency" );
        addCharacterEncoding( 0144, "d" );
        addCharacterEncoding( 0201, "dagger" );
        addCharacterEncoding( 0202, "daggerdbl" );
        addCharacterEncoding( 0260, "degree" );
        addCharacterEncoding( 0250, "dieresis" );
        addCharacterEncoding( 0367, "divide" );
        addCharacterEncoding( 044, "dollar" );
        addCharacterEncoding( 033, "dotaccent" );
        addCharacterEncoding( 0232, "dotlessi" );
        addCharacterEncoding( 0145, "e" );
        addCharacterEncoding( 0351, "eacute" );
        addCharacterEncoding( 0352, "ecircumflex" );
        addCharacterEncoding( 0353, "edieresis" );
        addCharacterEncoding( 0350, "egrave" );
        addCharacterEncoding( 070, "eight" );
        addCharacterEncoding( 0203, "ellipsis" );
        addCharacterEncoding( 0204, "emdash" );
        addCharacterEncoding( 0205, "endash" );
        addCharacterEncoding( 075, "equal" );
        addCharacterEncoding( 0360, "eth" );
        addCharacterEncoding( 041, "exclam" );
        addCharacterEncoding( 0241, "exclamdown" );
        addCharacterEncoding( 0146, "f" );
        addCharacterEncoding( 0223, "fi" );
        addCharacterEncoding( 065, "five" );
        addCharacterEncoding( 0224, "fl" );
        addCharacterEncoding( 0206, "florin" );
        addCharacterEncoding( 064, "four" );
        addCharacterEncoding( 0207, "fraction" );
        addCharacterEncoding( 0147, "g" );
        addCharacterEncoding( 0337, "germandbls" );
        addCharacterEncoding( 0140, "grave" );
        addCharacterEncoding( 076, "greater" );
        addCharacterEncoding( 0253, "guillemotleft" );
        addCharacterEncoding( 0273, "guillemotright" );
        addCharacterEncoding( 0210, "guilsinglleft" );
        addCharacterEncoding( 0211, "guilsinglright" );
        addCharacterEncoding( 0150, "h" );
        addCharacterEncoding( 034, "hungarumlaut" );
        addCharacterEncoding( 055, "hyphen" );
        addCharacterEncoding( 0151, "i" );
        addCharacterEncoding( 0355, "iacute" );
        addCharacterEncoding( 0356, "icircumflex" );
        addCharacterEncoding( 0357, "idieresis" );
        addCharacterEncoding( 0354, "igrave" );
        addCharacterEncoding( 0152, "j" );
        addCharacterEncoding( 0153, "k" );
        addCharacterEncoding( 0154, "l" );
        addCharacterEncoding( 074, "less" );
        addCharacterEncoding( 0254, "logicalnot" );
        addCharacterEncoding( 0233, "lslash" );
        addCharacterEncoding( 0155, "m" );
        addCharacterEncoding( 0257, "macron" );
        addCharacterEncoding( 0212, "minus" );
        addCharacterEncoding( 0265, "mu" );
        addCharacterEncoding( 0327, "multiply" );
        addCharacterEncoding( 0156, "n" );
        addCharacterEncoding( 071, "nine" );
        addCharacterEncoding( 0361, "ntilde" );
        addCharacterEncoding( 043, "numbersign" );
        addCharacterEncoding( 0157, "o" );
        addCharacterEncoding( 0363, "oacute" );
        addCharacterEncoding( 0364, "ocircumflex" );
        addCharacterEncoding( 0366, "odieresis" );
        addCharacterEncoding( 0234, "oe" );
        addCharacterEncoding( 035, "ogonek" );
        addCharacterEncoding( 0362, "ograve" );
        addCharacterEncoding( 061, "one" );
        addCharacterEncoding( 0275, "onehalf" );
        addCharacterEncoding( 0274, "onequarter" );
        addCharacterEncoding( 0271, "onesuperior" );
        addCharacterEncoding( 0252, "ordfeminine" );
        addCharacterEncoding( 0272, "ordmasculine" );
        addCharacterEncoding( 0370, "oslash" );
        addCharacterEncoding( 0365, "otilde" );
        addCharacterEncoding( 0160, "p" );
        addCharacterEncoding( 0266, "paragraph" );
        addCharacterEncoding( 050, "parenleft" );
        addCharacterEncoding( 051, "parenright" );
        addCharacterEncoding( 045, "percent" );
        addCharacterEncoding( 056, "period" );
        addCharacterEncoding( 0267, "periodcentered" );
        addCharacterEncoding( 0213, "perthousand" );
        addCharacterEncoding( 053, "plus" );
        addCharacterEncoding( 0261, "plusminus" );
        addCharacterEncoding( 0161, "q" );
        addCharacterEncoding( 077, "question" );
        addCharacterEncoding( 0277, "questiondown" );
        addCharacterEncoding( 042, "quotedbl" );
        addCharacterEncoding( 0214, "quotedblbase" );
        addCharacterEncoding( 0215, "quotedblleft" );
        addCharacterEncoding( 0216, "quotedblright" );
        addCharacterEncoding( 0217, "quoteleft" );
        addCharacterEncoding( 0220, "quoteright" );
        addCharacterEncoding( 0221, "quotesinglbase" );
        addCharacterEncoding( 047, "quotesingle" );
        addCharacterEncoding( 0162, "r" );
        addCharacterEncoding( 0256, "registered" );
        addCharacterEncoding( 036, "ring" );
        addCharacterEncoding( 0163, "s" );
        addCharacterEncoding( 0235, "scaron" );
        addCharacterEncoding( 0247, "section" );
        addCharacterEncoding( 073, "semicolon" );
        addCharacterEncoding( 067, "seven" );
        addCharacterEncoding( 066, "six" );
        addCharacterEncoding( 057, "slash" );
        addCharacterEncoding( 040, "space" );
        addCharacterEncoding( 0243, "sterling" );
        addCharacterEncoding( 0164, "t" );
        addCharacterEncoding( 0376, "thorn" );
        addCharacterEncoding( 063, "three" );
        addCharacterEncoding( 0276, "threequarters" );
        addCharacterEncoding( 0263, "threesuperior" );
        addCharacterEncoding( 037, "tilde" );
        addCharacterEncoding( 0222, "trademark" );
        addCharacterEncoding( 062, "two" );
        addCharacterEncoding( 0262, "twosuperior" );
        addCharacterEncoding( 0165, "u" );
        addCharacterEncoding( 0372, "uacute" );
        addCharacterEncoding( 0373, "ucircumflex" );
        addCharacterEncoding( 0374, "udieresis" );
        addCharacterEncoding( 0371, "ugrave" );
        addCharacterEncoding( 0137, "underscore" );
        addCharacterEncoding( 0166, "v" );
        addCharacterEncoding( 0167, "w" );
        addCharacterEncoding( 0170, "x" );
        addCharacterEncoding( 0171, "y" );
        addCharacterEncoding( 0375, "yacute" );
        addCharacterEncoding( 0377, "ydieresis" );
        addCharacterEncoding( 0245, "yen" );
        addCharacterEncoding( 0172, "z" );
        addCharacterEncoding( 0236, "zcaron" );
        addCharacterEncoding( 060, "zero" );
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return COSName.PDF_DOC_ENCODING;
    }
}
