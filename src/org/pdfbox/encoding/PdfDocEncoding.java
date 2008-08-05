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
package org.pdfbox.encoding;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;

/**
 * This is an interface to a text encoder.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class PdfDocEncoding extends Encoding
{
    /**
     * Constructor.
     */
    public PdfDocEncoding()
    {
        addCharacterEncoding( 0101, COSName.getPDFName( "A" ) );
        addCharacterEncoding( 0306, COSName.getPDFName( "AE" ) );
        addCharacterEncoding( 0301, COSName.getPDFName( "Aacute" ) );
        addCharacterEncoding( 0302, COSName.getPDFName( "Acircumflex" ) );
        addCharacterEncoding( 0304, COSName.getPDFName( "Adieresis" ) );
        addCharacterEncoding( 0300, COSName.getPDFName( "Agrave" ) );
        addCharacterEncoding( 0305, COSName.getPDFName( "Aring" ) );
        addCharacterEncoding( 0303, COSName.getPDFName( "Atilde" ) );
        addCharacterEncoding( 0102, COSName.getPDFName( "B" ) );
        addCharacterEncoding( 0103, COSName.getPDFName( "C" ) );
        addCharacterEncoding( 0307, COSName.getPDFName( "Ccedilla" ) );
        addCharacterEncoding( 0104, COSName.getPDFName( "D" ) );
        addCharacterEncoding( 0105, COSName.getPDFName( "E" ) );
        addCharacterEncoding( 0311, COSName.getPDFName( "Eacute" ) );
        addCharacterEncoding( 0312, COSName.getPDFName( "Ecircumflex" ) );
        addCharacterEncoding( 0313, COSName.getPDFName( "Edieresis" ) );
        addCharacterEncoding( 0310, COSName.getPDFName( "Egrave" ) );
        addCharacterEncoding( 0320, COSName.getPDFName( "Eth" ) );
        addCharacterEncoding( 0240, COSName.getPDFName( "Euro" ) );
        addCharacterEncoding( 0106, COSName.getPDFName( "F" ) );
        addCharacterEncoding( 0107, COSName.getPDFName( "G" ) );
        addCharacterEncoding( 0110, COSName.getPDFName( "H" ) );
        addCharacterEncoding( 0111, COSName.getPDFName( "I" ) );
        addCharacterEncoding( 0315, COSName.getPDFName( "Iacute" ) );
        addCharacterEncoding( 0316, COSName.getPDFName( "Icircumflex" ) );
        addCharacterEncoding( 0317, COSName.getPDFName( "Idieresis" ) );
        addCharacterEncoding( 0314, COSName.getPDFName( "Igrave" ) );
        addCharacterEncoding( 0112, COSName.getPDFName( "J" ) );
        addCharacterEncoding( 0113, COSName.getPDFName( "K" ) );
        addCharacterEncoding( 0114, COSName.getPDFName( "L" ) );
        addCharacterEncoding( 0225, COSName.getPDFName( "Lslash" ) );
        addCharacterEncoding( 0115, COSName.getPDFName( "M" ) );
        addCharacterEncoding( 0116, COSName.getPDFName( "N" ) );
        addCharacterEncoding( 0321, COSName.getPDFName( "Ntilde" ) );
        addCharacterEncoding( 0117, COSName.getPDFName( "O" ) );
        addCharacterEncoding( 0226, COSName.getPDFName( "OE" ) );
        addCharacterEncoding( 0323, COSName.getPDFName( "Oacute" ) );
        addCharacterEncoding( 0324, COSName.getPDFName( "Ocircumflex" ) );
        addCharacterEncoding( 0326, COSName.getPDFName( "Odieresis" ) );
        addCharacterEncoding( 0322, COSName.getPDFName( "Ograve" ) );
        addCharacterEncoding( 0330, COSName.getPDFName( "Oslash" ) );
        addCharacterEncoding( 0325, COSName.getPDFName( "Otilde" ) );
        addCharacterEncoding( 0120, COSName.getPDFName( "P" ) );
        addCharacterEncoding( 0121, COSName.getPDFName( "Q" ) );
        addCharacterEncoding( 0122, COSName.getPDFName( "R" ) );
        addCharacterEncoding( 0123, COSName.getPDFName( "S" ) );
        addCharacterEncoding( 0227, COSName.getPDFName( "Scaron" ) );
        addCharacterEncoding( 0124, COSName.getPDFName( "T" ) );
        addCharacterEncoding( 0336, COSName.getPDFName( "Thorn" ) );
        addCharacterEncoding( 0125, COSName.getPDFName( "U" ) );
        addCharacterEncoding( 0332, COSName.getPDFName( "Uacute" ) );
        addCharacterEncoding( 0333, COSName.getPDFName( "Ucircumflex" ) );
        addCharacterEncoding( 0334, COSName.getPDFName( "Udieresis" ) );
        addCharacterEncoding( 0331, COSName.getPDFName( "Ugrave" ) );
        addCharacterEncoding( 0126, COSName.getPDFName( "V" ) );
        addCharacterEncoding( 0127, COSName.getPDFName( "W" ) );
        addCharacterEncoding( 0130, COSName.getPDFName( "X" ) );
        addCharacterEncoding( 0131, COSName.getPDFName( "Y" ) );
        addCharacterEncoding( 0335, COSName.getPDFName( "Yacute" ) );
        addCharacterEncoding( 0230, COSName.getPDFName( "Ydieresis" ) );
        addCharacterEncoding( 0132, COSName.getPDFName( "Z" ) );
        addCharacterEncoding( 0231, COSName.getPDFName( "Zcaron" ) );
        addCharacterEncoding( 0141, COSName.getPDFName( "a" ) );
        addCharacterEncoding( 0341, COSName.getPDFName( "aacute" ) );
        addCharacterEncoding( 0342, COSName.getPDFName( "acircumflex" ) );
        addCharacterEncoding( 0264, COSName.getPDFName( "acute" ) );
        addCharacterEncoding( 0344, COSName.getPDFName( "adieresis" ) );
        addCharacterEncoding( 0346, COSName.getPDFName( "ae" ) );
        addCharacterEncoding( 0340, COSName.getPDFName( "agrave" ) );
        addCharacterEncoding( 046, COSName.getPDFName( "ampersand" ) );
        addCharacterEncoding( 0345, COSName.getPDFName( "aring" ) );
        addCharacterEncoding( 0136, COSName.getPDFName( "asciicircum" ) );
        addCharacterEncoding( 0176, COSName.getPDFName( "asciitilde" ) );
        addCharacterEncoding( 052, COSName.getPDFName( "asterisk" ) );
        addCharacterEncoding( 0100, COSName.getPDFName( "at" ) );
        addCharacterEncoding( 0343, COSName.getPDFName( "atilde" ) );
        addCharacterEncoding( 0142, COSName.getPDFName( "b" ) );
        addCharacterEncoding( 0134, COSName.getPDFName( "backslash" ) );
        addCharacterEncoding( 0174, COSName.getPDFName( "bar" ) );
        addCharacterEncoding( 0173, COSName.getPDFName( "braceleft" ) );
        addCharacterEncoding( 0175, COSName.getPDFName( "braceright" ) );
        addCharacterEncoding( 0133, COSName.getPDFName( "bracketleft" ) );
        addCharacterEncoding( 0135, COSName.getPDFName( "bracketright" ) );
        addCharacterEncoding( 030, COSName.getPDFName( "breve" ) );
        addCharacterEncoding( 0246, COSName.getPDFName( "brokenbar" ) );
        addCharacterEncoding( 0200, COSName.getPDFName( "bullet" ) );
        addCharacterEncoding( 0143, COSName.getPDFName( "c" ) );
        addCharacterEncoding( 031, COSName.getPDFName( "caron" ) );
        addCharacterEncoding( 0347, COSName.getPDFName( "ccedilla" ) );
        addCharacterEncoding( 0270, COSName.getPDFName( "cedilla" ) );
        addCharacterEncoding( 0242, COSName.getPDFName( "cent" ) );
        addCharacterEncoding( 032, COSName.getPDFName( "circumflex" ) );
        addCharacterEncoding( 072, COSName.getPDFName( "colon" ) );
        addCharacterEncoding( 054, COSName.getPDFName( "comma" ) );
        addCharacterEncoding( 0251, COSName.getPDFName( "copyright" ) );
        addCharacterEncoding( 0244, COSName.getPDFName( "currency1" ) );
        addCharacterEncoding( 0144, COSName.getPDFName( "d" ) );
        addCharacterEncoding( 0201, COSName.getPDFName( "dagger" ) );
        addCharacterEncoding( 0202, COSName.getPDFName( "daggerdbl" ) );
        addCharacterEncoding( 0260, COSName.getPDFName( "degree" ) );
        addCharacterEncoding( 0250, COSName.getPDFName( "dieresis" ) );
        addCharacterEncoding( 0367, COSName.getPDFName( "divide" ) );
        addCharacterEncoding( 044, COSName.getPDFName( "dollar" ) );
        addCharacterEncoding( 033, COSName.getPDFName( "dotaccent" ) );
        addCharacterEncoding( 0232, COSName.getPDFName( "dotlessi" ) );
        addCharacterEncoding( 0145, COSName.getPDFName( "e" ) );
        addCharacterEncoding( 0351, COSName.getPDFName( "eacute" ) );
        addCharacterEncoding( 0352, COSName.getPDFName( "ecircumflex" ) );
        addCharacterEncoding( 0353, COSName.getPDFName( "edieresis" ) );
        addCharacterEncoding( 0350, COSName.getPDFName( "egrave" ) );
        addCharacterEncoding( 070, COSName.getPDFName( "eight" ) );
        addCharacterEncoding( 0203, COSName.getPDFName( "ellipsis" ) );
        addCharacterEncoding( 0204, COSName.getPDFName( "emdash" ) );
        addCharacterEncoding( 0205, COSName.getPDFName( "endash" ) );
        addCharacterEncoding( 075, COSName.getPDFName( "equal" ) );
        addCharacterEncoding( 0360, COSName.getPDFName( "eth" ) );
        addCharacterEncoding( 041, COSName.getPDFName( "exclam" ) );
        addCharacterEncoding( 0241, COSName.getPDFName( "exclamdown" ) );
        addCharacterEncoding( 0146, COSName.getPDFName( "f" ) );
        addCharacterEncoding( 0223, COSName.getPDFName( "fi" ) );
        addCharacterEncoding( 065, COSName.getPDFName( "five" ) );
        addCharacterEncoding( 0224, COSName.getPDFName( "fl" ) );
        addCharacterEncoding( 0206, COSName.getPDFName( "florin" ) );
        addCharacterEncoding( 064, COSName.getPDFName( "four" ) );
        addCharacterEncoding( 0207, COSName.getPDFName( "fraction" ) );
        addCharacterEncoding( 0147, COSName.getPDFName( "g" ) );
        addCharacterEncoding( 0337, COSName.getPDFName( "germandbls" ) );
        addCharacterEncoding( 0140, COSName.getPDFName( "grave" ) );
        addCharacterEncoding( 076, COSName.getPDFName( "greater" ) );
        addCharacterEncoding( 0253, COSName.getPDFName( "guillemotleft" ) );
        addCharacterEncoding( 0273, COSName.getPDFName( "guillemotright" ) );
        addCharacterEncoding( 0210, COSName.getPDFName( "guilsinglleft" ) );
        addCharacterEncoding( 0211, COSName.getPDFName( "guilsinglright" ) );
        addCharacterEncoding( 0150, COSName.getPDFName( "h" ) );
        addCharacterEncoding( 034, COSName.getPDFName( "hungarumlaut" ) );
        addCharacterEncoding( 055, COSName.getPDFName( "hyphen" ) );
        addCharacterEncoding( 0151, COSName.getPDFName( "i" ) );
        addCharacterEncoding( 0355, COSName.getPDFName( "iacute" ) );
        addCharacterEncoding( 0356, COSName.getPDFName( "icircumflex" ) );
        addCharacterEncoding( 0357, COSName.getPDFName( "idieresis" ) );
        addCharacterEncoding( 0354, COSName.getPDFName( "igrave" ) );
        addCharacterEncoding( 0152, COSName.getPDFName( "j" ) );
        addCharacterEncoding( 0153, COSName.getPDFName( "k" ) );
        addCharacterEncoding( 0154, COSName.getPDFName( "l" ) );
        addCharacterEncoding( 074, COSName.getPDFName( "less" ) );
        addCharacterEncoding( 0254, COSName.getPDFName( "logicalnot" ) );
        addCharacterEncoding( 0233, COSName.getPDFName( "lslash" ) );
        addCharacterEncoding( 0155, COSName.getPDFName( "m" ) );
        addCharacterEncoding( 0257, COSName.getPDFName( "macron" ) );
        addCharacterEncoding( 0212, COSName.getPDFName( "minus" ) );
        addCharacterEncoding( 0265, COSName.getPDFName( "mu" ) );
        addCharacterEncoding( 0327, COSName.getPDFName( "multiply" ) );
        addCharacterEncoding( 0156, COSName.getPDFName( "n" ) );
        addCharacterEncoding( 071, COSName.getPDFName( "nine" ) );
        addCharacterEncoding( 0361, COSName.getPDFName( "ntilde" ) );
        addCharacterEncoding( 043, COSName.getPDFName( "numbersign" ) );
        addCharacterEncoding( 0157, COSName.getPDFName( "o" ) );
        addCharacterEncoding( 0363, COSName.getPDFName( "oacute" ) );
        addCharacterEncoding( 0364, COSName.getPDFName( "ocircumflex" ) );
        addCharacterEncoding( 0366, COSName.getPDFName( "odieresis" ) );
        addCharacterEncoding( 0234, COSName.getPDFName( "oe" ) );
        addCharacterEncoding( 035, COSName.getPDFName( "ogonek" ) );
        addCharacterEncoding( 0362, COSName.getPDFName( "ograve" ) );
        addCharacterEncoding( 061, COSName.getPDFName( "one" ) );
        addCharacterEncoding( 0275, COSName.getPDFName( "onehalf" ) );
        addCharacterEncoding( 0274, COSName.getPDFName( "onequarter" ) );
        addCharacterEncoding( 0271, COSName.getPDFName( "onesuperior" ) );
        addCharacterEncoding( 0252, COSName.getPDFName( "ordfeminine" ) );
        addCharacterEncoding( 0272, COSName.getPDFName( "ordmasculine" ) );
        addCharacterEncoding( 0370, COSName.getPDFName( "oslash" ) );
        addCharacterEncoding( 0365, COSName.getPDFName( "otilde" ) );
        addCharacterEncoding( 0160, COSName.getPDFName( "p" ) );
        addCharacterEncoding( 0266, COSName.getPDFName( "paragraph" ) );
        addCharacterEncoding( 050, COSName.getPDFName( "parenleft" ) );
        addCharacterEncoding( 051, COSName.getPDFName( "parenright" ) );
        addCharacterEncoding( 045, COSName.getPDFName( "percent" ) );
        addCharacterEncoding( 056, COSName.getPDFName( "period" ) );
        addCharacterEncoding( 0267, COSName.getPDFName( "periodcentered" ) );
        addCharacterEncoding( 0213, COSName.getPDFName( "perthousand" ) );
        addCharacterEncoding( 053, COSName.getPDFName( "plus" ) );
        addCharacterEncoding( 0261, COSName.getPDFName( "plusminus" ) );
        addCharacterEncoding( 0161, COSName.getPDFName( "q" ) );
        addCharacterEncoding( 077, COSName.getPDFName( "question" ) );
        addCharacterEncoding( 0277, COSName.getPDFName( "questiondown" ) );
        addCharacterEncoding( 042, COSName.getPDFName( "quotedbl" ) );
        addCharacterEncoding( 0214, COSName.getPDFName( "quotedblbase" ) );
        addCharacterEncoding( 0215, COSName.getPDFName( "quotedblleft" ) );
        addCharacterEncoding( 0216, COSName.getPDFName( "quotedblright" ) );
        addCharacterEncoding( 0217, COSName.getPDFName( "quoteleft" ) );
        addCharacterEncoding( 0220, COSName.getPDFName( "quoteright" ) );
        addCharacterEncoding( 0221, COSName.getPDFName( "quotesinglbase" ) );
        addCharacterEncoding( 047, COSName.getPDFName( "quotesingle" ) );
        addCharacterEncoding( 0162, COSName.getPDFName( "r" ) );
        addCharacterEncoding( 0256, COSName.getPDFName( "registered" ) );
        addCharacterEncoding( 036, COSName.getPDFName( "ring" ) );
        addCharacterEncoding( 0163, COSName.getPDFName( "s" ) );
        addCharacterEncoding( 0235, COSName.getPDFName( "scaron" ) );
        addCharacterEncoding( 0247, COSName.getPDFName( "section" ) );
        addCharacterEncoding( 073, COSName.getPDFName( "semicolon" ) );
        addCharacterEncoding( 067, COSName.getPDFName( "seven" ) );
        addCharacterEncoding( 066, COSName.getPDFName( "six" ) );
        addCharacterEncoding( 057, COSName.getPDFName( "slash" ) );
        addCharacterEncoding( 040, COSName.getPDFName( "space" ) );
        addCharacterEncoding( 0243, COSName.getPDFName( "sterling" ) );
        addCharacterEncoding( 0164, COSName.getPDFName( "t" ) );
        addCharacterEncoding( 0376, COSName.getPDFName( "thorn" ) );
        addCharacterEncoding( 063, COSName.getPDFName( "three" ) );
        addCharacterEncoding( 0276, COSName.getPDFName( "threequarters" ) );
        addCharacterEncoding( 0263, COSName.getPDFName( "threesuperior" ) );
        addCharacterEncoding( 037, COSName.getPDFName( "tilde" ) );
        addCharacterEncoding( 0222, COSName.getPDFName( "trademark" ) );
        addCharacterEncoding( 062, COSName.getPDFName( "two" ) );
        addCharacterEncoding( 0262, COSName.getPDFName( "twosuperior" ) );
        addCharacterEncoding( 0165, COSName.getPDFName( "u" ) );
        addCharacterEncoding( 0372, COSName.getPDFName( "uacute" ) );
        addCharacterEncoding( 0373, COSName.getPDFName( "ucircumflex" ) );
        addCharacterEncoding( 0374, COSName.getPDFName( "udieresis" ) );
        addCharacterEncoding( 0371, COSName.getPDFName( "ugrave" ) );
        addCharacterEncoding( 0137, COSName.getPDFName( "underscore" ) );
        addCharacterEncoding( 0166, COSName.getPDFName( "v" ) );
        addCharacterEncoding( 0167, COSName.getPDFName( "w" ) );
        addCharacterEncoding( 0170, COSName.getPDFName( "x" ) );
        addCharacterEncoding( 0171, COSName.getPDFName( "y" ) );
        addCharacterEncoding( 0375, COSName.getPDFName( "yacute" ) );
        addCharacterEncoding( 0377, COSName.getPDFName( "ydieresis" ) );
        addCharacterEncoding( 0245, COSName.getPDFName( "yen" ) );
        addCharacterEncoding( 0172, COSName.getPDFName( "z" ) );
        addCharacterEncoding( 0236, COSName.getPDFName( "zcaron" ) );
        addCharacterEncoding( 060, COSName.getPDFName( "zero" ) );
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
