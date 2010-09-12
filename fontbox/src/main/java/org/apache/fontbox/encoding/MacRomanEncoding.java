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
package org.apache.fontbox.encoding;

/**
 * This is an interface to a text encoder.
 *
 * @author Ben Litchfield
 * @version $Revision: 1.1 $
 */
public class MacRomanEncoding extends Encoding
{
    /**
     * Constructor.
     */
    public MacRomanEncoding()
    {
        addCharacterEncoding( 0101, "A" );
        addCharacterEncoding( 0256, "AE" );
        addCharacterEncoding( 0347, "Aacute" );
        addCharacterEncoding( 0345, "Acircumflex" );
        addCharacterEncoding( 0200, "Adieresis" );
        addCharacterEncoding( 0313, "Agrave" );
        addCharacterEncoding( 0201, "Aring" );
        addCharacterEncoding( 0314, "Atilde" );
        addCharacterEncoding( 0102, "B" );
        addCharacterEncoding( 0103, "C" );
        addCharacterEncoding( 0202, "Ccedilla" );
        addCharacterEncoding( 0104, "D" );
        addCharacterEncoding( 0105, "E" );
        addCharacterEncoding( 0203, "Eacute" );
        addCharacterEncoding( 0346, "Ecircumflex" );
        addCharacterEncoding( 0350, "Edieresis" );
        addCharacterEncoding( 0351, "Egrave" );
        addCharacterEncoding( 0106, "F" );
        addCharacterEncoding( 0107, "G" );
        addCharacterEncoding( 0110, "H" );
        addCharacterEncoding( 0111, "I" );
        addCharacterEncoding( 0352, "Iacute" );
        addCharacterEncoding( 0353, "Icircumflex" );
        addCharacterEncoding( 0354, "Idieresis" );
        addCharacterEncoding( 0355, "Igrave" );
        addCharacterEncoding( 0112, "J" );
        addCharacterEncoding( 0113, "K" );
        addCharacterEncoding( 0114, "L" );
        addCharacterEncoding( 0115, "M" );
        addCharacterEncoding( 0116, "N" );
        addCharacterEncoding( 0204, "Ntilde" );
        addCharacterEncoding( 0117, "O" );
        addCharacterEncoding( 0316, "OE" );
        addCharacterEncoding( 0356, "Oacute" );
        addCharacterEncoding( 0357, "Ocircumflex" );
        addCharacterEncoding( 0205, "Odieresis" );
        addCharacterEncoding( 0361, "Ograve" );
        addCharacterEncoding( 0257, "Oslash" );
        addCharacterEncoding( 0315, "Otilde" );
        addCharacterEncoding( 0120, "P" );
        addCharacterEncoding( 0121, "Q" );
        addCharacterEncoding( 0122, "R" );
        addCharacterEncoding( 0123, "S" );
        addCharacterEncoding( 0124, "T" );
        addCharacterEncoding( 0125, "U" );
        addCharacterEncoding( 0362, "Uacute" );
        addCharacterEncoding( 0363, "Ucircumflex" );
        addCharacterEncoding( 0206, "Udieresis" );
        addCharacterEncoding( 0364, "Ugrave" );
        addCharacterEncoding( 0126, "V" );
        addCharacterEncoding( 0127, "W" );
        addCharacterEncoding( 0130, "X" );
        addCharacterEncoding( 0131, "Y" );
        addCharacterEncoding( 0331, "Ydieresis" );
        addCharacterEncoding( 0132, "Z" );
        addCharacterEncoding( 0141, "a" );
        addCharacterEncoding( 0207, "aacute" );
        addCharacterEncoding( 0211, "acircumflex" );
        addCharacterEncoding( 0253, "acute" );
        addCharacterEncoding( 0212, "adieresis" );
        addCharacterEncoding( 0276, "ae" );
        addCharacterEncoding( 0210, "agrave" );
        addCharacterEncoding( 046, "ampersand" );
        addCharacterEncoding( 0214, "aring" );
        addCharacterEncoding( 0136, "asciicircum" );
        addCharacterEncoding( 0176, "asciitilde" );
        addCharacterEncoding( 052, "asterisk" );
        addCharacterEncoding( 0100, "at" );
        addCharacterEncoding( 0213, "atilde" );
        addCharacterEncoding( 0142, "b" );
        addCharacterEncoding( 0134, "backslash" );
        addCharacterEncoding( 0174, "bar" );
        addCharacterEncoding( 0173, "braceleft" );
        addCharacterEncoding( 0175, "braceright" );
        addCharacterEncoding( 0133, "bracketleft" );
        addCharacterEncoding( 0135, "bracketright" );
        addCharacterEncoding( 0371, "breve" );
        addCharacterEncoding( 0245, "bullet" );
        addCharacterEncoding( 0143, "c" );
        addCharacterEncoding( 0377, "caron" );
        addCharacterEncoding( 0215, "ccedilla" );
        addCharacterEncoding( 0374, "cedilla" );
        addCharacterEncoding( 0242, "cent" );
        addCharacterEncoding( 0366, "circumflex" );
        addCharacterEncoding( 072, "colon" );
        addCharacterEncoding( 054, "comma" );
        addCharacterEncoding( 0251, "copyright" );
        addCharacterEncoding( 0333, "currency" );
        addCharacterEncoding( 0144, "d" );
        addCharacterEncoding( 0240, "dagger" );
        addCharacterEncoding( 0340, "daggerdbl" );
        addCharacterEncoding( 0241, "degree" );
        addCharacterEncoding( 0254, "dieresis" );
        addCharacterEncoding( 0326, "divide" );
        addCharacterEncoding( 044, "dollar" );
        addCharacterEncoding( 0372, "dotaccent" );
        addCharacterEncoding( 0365, "dotlessi" );
        addCharacterEncoding( 0145, "e" );
        addCharacterEncoding( 0216, "eacute" );
        addCharacterEncoding( 0220, "ecircumflex" );
        addCharacterEncoding( 0221, "edieresis" );
        addCharacterEncoding( 0217, "egrave" );
        addCharacterEncoding( 070, "eight" );
        addCharacterEncoding( 0311, "ellipsis" );
        addCharacterEncoding( 0321, "emdash" );
        addCharacterEncoding( 0320, "endash" );
        addCharacterEncoding( 075, "equal" );
        addCharacterEncoding( 041, "exclam" );
        addCharacterEncoding( 0301, "exclamdown" );
        addCharacterEncoding( 0146, "f" );
        addCharacterEncoding( 0336, "fi" );
        addCharacterEncoding( 065, "five" );
        addCharacterEncoding( 0337, "fl" );
        addCharacterEncoding( 0304, "florin" );
        addCharacterEncoding( 064, "four" );
        addCharacterEncoding( 0332, "fraction" );
        addCharacterEncoding( 0147, "g" );
        addCharacterEncoding( 0247, "germandbls" );
        addCharacterEncoding( 0140, "grave" );
        addCharacterEncoding( 076, "greater" );
        addCharacterEncoding( 0307, "guillemotleft" );
        addCharacterEncoding( 0310, "guillemotright" );
        addCharacterEncoding( 0334, "guilsinglleft" );
        addCharacterEncoding( 0335, "guilsinglright" );
        addCharacterEncoding( 0150, "h" );
        addCharacterEncoding( 0375, "hungarumlaut" );
        addCharacterEncoding( 055, "hyphen" );
        addCharacterEncoding( 0151, "i" );
        addCharacterEncoding( 0222, "iacute" );
        addCharacterEncoding( 0224, "icircumflex" );
        addCharacterEncoding( 0225, "idieresis" );
        addCharacterEncoding( 0223, "igrave" );
        addCharacterEncoding( 0152, "j" );
        addCharacterEncoding( 0153, "k" );
        addCharacterEncoding( 0154, "l" );
        addCharacterEncoding( 074, "less" );
        addCharacterEncoding( 0302, "logicalnot" );
        addCharacterEncoding( 0155, "m" );
        addCharacterEncoding( 0370, "macron" );
        addCharacterEncoding( 0265, "mu" );
        addCharacterEncoding( 0156, "n" );
        addCharacterEncoding( 071, "nine" );
        addCharacterEncoding( 0226, "ntilde" );
        addCharacterEncoding( 043, "numbersign" );
        addCharacterEncoding( 0157, "o" );
        addCharacterEncoding( 0227, "oacute" );
        addCharacterEncoding( 0231, "ocircumflex" );
        addCharacterEncoding( 0232, "odieresis" );
        addCharacterEncoding( 0317, "oe" );
        addCharacterEncoding( 0376, "ogonek" );
        addCharacterEncoding( 0230, "ograve" );
        addCharacterEncoding( 061, "one" );
        addCharacterEncoding( 0273, "ordfeminine" );
        addCharacterEncoding( 0274, "ordmasculine" );
        addCharacterEncoding( 0277, "oslash" );
        addCharacterEncoding( 0233, "otilde" );
        addCharacterEncoding( 0160, "p" );
        addCharacterEncoding( 0246, "paragraph" );
        addCharacterEncoding( 050, "parenleft" );
        addCharacterEncoding( 051, "parenright" );
        addCharacterEncoding( 045, "percent" );
        addCharacterEncoding( 056, "period" );
        addCharacterEncoding( 0341, "periodcentered" );
        addCharacterEncoding( 0344, "perthousand" );
        addCharacterEncoding( 053, "plus" );
        addCharacterEncoding( 0261, "plusminus" );
        addCharacterEncoding( 0161, "q" );
        addCharacterEncoding( 077, "question" );
        addCharacterEncoding( 0300, "questiondown" );
        addCharacterEncoding( 042, "quotedbl" );
        addCharacterEncoding( 0343, "quotedblbase" );
        addCharacterEncoding( 0322, "quotedblleft" );
        addCharacterEncoding( 0323, "quotedblright" );
        addCharacterEncoding( 0324, "quoteleft" );
        addCharacterEncoding( 0325, "quoteright" );
        addCharacterEncoding( 0342, "quotesinglbase" );
        addCharacterEncoding( 047, "quotesingle" );
        addCharacterEncoding( 0162, "r" );
        addCharacterEncoding( 0250, "registered" );
        addCharacterEncoding( 0373, "ring" );
        addCharacterEncoding( 0163, "s" );
        addCharacterEncoding( 0244, "section" );
        addCharacterEncoding( 073, "semicolon" );
        addCharacterEncoding( 067, "seven" );
        addCharacterEncoding( 066, "six" );
        addCharacterEncoding( 057, "slash" );
        addCharacterEncoding( 040, "space" );
        addCharacterEncoding( 0243, "sterling" );
        addCharacterEncoding( 0164, "t" );
        addCharacterEncoding( 063, "three" );
        addCharacterEncoding( 0367, "tilde" );
        addCharacterEncoding( 0252, "trademark" );
        addCharacterEncoding( 062, "two" );
        addCharacterEncoding( 0165, "u" );
        addCharacterEncoding( 0234, "uacute" );
        addCharacterEncoding( 0236, "ucircumflex" );
        addCharacterEncoding( 0237, "udieresis" );
        addCharacterEncoding( 0235, "ugrave" );
        addCharacterEncoding( 0137, "underscore" );
        addCharacterEncoding( 0166, "v" );
        addCharacterEncoding( 0167, "w" );
        addCharacterEncoding( 0170, "x" );
        addCharacterEncoding( 0171, "y" );
        addCharacterEncoding( 0330, "ydieresis" );
        addCharacterEncoding( 0264, "yen" );
        addCharacterEncoding( 0172, "z" );
        addCharacterEncoding( 060, "zero" );
    }
}