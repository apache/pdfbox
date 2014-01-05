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
 * Adobe Standard Encoding
 *
 * @author Ben Litchfield
 *
 */
public class StandardEncoding extends Encoding
{
    /**
     * Singleton instance of this class.
     */
    public static final StandardEncoding INSTANCE = new StandardEncoding();

    /**
     * Constructor.
     */
    public StandardEncoding()
    {
        addCharacterEncoding( 0101, "A" );
        addCharacterEncoding( 0341, "AE" );
        addCharacterEncoding( 0102, "B" );
        addCharacterEncoding( 0103, "C" );
        addCharacterEncoding( 0104, "D" );
        addCharacterEncoding( 0105, "E" );
        addCharacterEncoding( 0106, "F" );
        addCharacterEncoding( 0107, "G" );
        addCharacterEncoding( 0110, "H" );
        addCharacterEncoding( 0111, "I" );
        addCharacterEncoding( 0112, "J" );
        addCharacterEncoding( 0113, "K" );
        addCharacterEncoding( 0114, "L" );
        addCharacterEncoding( 0350, "Lslash" );
        addCharacterEncoding( 0115, "M" );
        addCharacterEncoding( 0116, "N" );
        addCharacterEncoding( 0117, "O" );
        addCharacterEncoding( 0352, "OE" );
        addCharacterEncoding( 0351, "Oslash" );
        addCharacterEncoding( 0120, "P" );
        addCharacterEncoding( 0121, "Q" );
        addCharacterEncoding( 0122, "R" );
        addCharacterEncoding( 0123, "S" );
        addCharacterEncoding( 0124, "T" );
        addCharacterEncoding( 0125, "U" );
        addCharacterEncoding( 0126, "V" );
        addCharacterEncoding( 0127, "W" );
        addCharacterEncoding( 0130, "X" );
        addCharacterEncoding( 0131, "Y" );
        addCharacterEncoding( 0132, "Z" );
        addCharacterEncoding( 0141, "a" );
        addCharacterEncoding( 0302, "acute" );
        addCharacterEncoding( 0361, "ae" );
        addCharacterEncoding( 0046, "ampersand" );
        addCharacterEncoding( 0136, "asciicircum" );
        addCharacterEncoding( 0176, "asciitilde" );
        addCharacterEncoding( 0052, "asterisk" );
        addCharacterEncoding( 0100, "at" );
        addCharacterEncoding( 0142, "b" );
        addCharacterEncoding( 0134, "backslash" );
        addCharacterEncoding( 0174, "bar" );
        addCharacterEncoding( 0173, "braceleft" );
        addCharacterEncoding( 0175, "braceright" );
        addCharacterEncoding( 0133, "bracketleft" );
        addCharacterEncoding( 0135, "bracketright" );
        addCharacterEncoding( 0306, "breve" );
        addCharacterEncoding( 0267, "bullet" );
        addCharacterEncoding( 0143, "c" );
        addCharacterEncoding( 0317, "caron" );
        addCharacterEncoding( 0313, "cedilla" );
        addCharacterEncoding( 0242, "cent" );
        addCharacterEncoding( 0303, "circumflex" );
        addCharacterEncoding( 0072, "colon" );
        addCharacterEncoding( 0054, "comma" );
        addCharacterEncoding( 0250, "currency" );
        addCharacterEncoding( 0144, "d" );
        addCharacterEncoding( 0262, "dagger" );
        addCharacterEncoding( 0263, "daggerdbl" );
        addCharacterEncoding( 0310, "dieresis" );
        addCharacterEncoding( 0044, "dollar" );
        addCharacterEncoding( 0307, "dotaccent" );
        addCharacterEncoding( 0365, "dotlessi" );
        addCharacterEncoding( 0145, "e" );
        addCharacterEncoding( 0070, "eight" );
        addCharacterEncoding( 0274, "ellipsis" );
        addCharacterEncoding( 0320, "emdash" );
        addCharacterEncoding( 0261, "endash" );
        addCharacterEncoding( 0075, "equal" );
        addCharacterEncoding( 0041, "exclam" );
        addCharacterEncoding( 0241, "exclamdown" );
        addCharacterEncoding( 0146, "f" );
        addCharacterEncoding( 0256, "fi" );
        addCharacterEncoding( 0065, "five" );
        addCharacterEncoding( 0257, "fl" );
        addCharacterEncoding( 0246, "florin" );
        addCharacterEncoding( 0064, "four" );
        addCharacterEncoding( 0244, "fraction" );
        addCharacterEncoding( 0147, "g" );
        addCharacterEncoding( 0373, "germandbls" );
        addCharacterEncoding( 0301, "grave" );
        addCharacterEncoding( 0076, "greater" );
        addCharacterEncoding( 0253, "guillemotleft" );
        addCharacterEncoding( 0273, "guillemotright" );
        addCharacterEncoding( 0254, "guilsinglleft" );
        addCharacterEncoding( 0255, "guilsinglright" );
        addCharacterEncoding( 0150, "h" );
        addCharacterEncoding( 0315, "hungarumlaut" );
        addCharacterEncoding( 0055, "hyphen" );
        addCharacterEncoding( 0151, "i" );
        addCharacterEncoding( 0152, "j" );
        addCharacterEncoding( 0153, "k" );
        addCharacterEncoding( 0154, "l" );
        addCharacterEncoding( 0074, "less" );
        addCharacterEncoding( 0370, "lslash" );
        addCharacterEncoding( 0155, "m" );
        addCharacterEncoding( 0305, "macron" );
        addCharacterEncoding( 0156, "n" );
        addCharacterEncoding( 0071, "nine" );
        addCharacterEncoding( 0043, "numbersign" );
        addCharacterEncoding( 0157, "o" );
        addCharacterEncoding( 0372, "oe" );
        addCharacterEncoding( 0316, "ogonek" );
        addCharacterEncoding( 0061, "one" );
        addCharacterEncoding( 0343, "ordfeminine" );
        addCharacterEncoding( 0353, "ordmasculine" );
        addCharacterEncoding( 0371, "oslash" );
        addCharacterEncoding( 0160, "p" );
        addCharacterEncoding( 0266, "paragraph" );
        addCharacterEncoding( 0050, "parenleft" );
        addCharacterEncoding( 0051, "parenright" );
        addCharacterEncoding( 0045, "percent" );
        addCharacterEncoding( 0056, "period" );
        addCharacterEncoding( 0264, "periodcentered" );
        addCharacterEncoding( 0275, "perthousand" );
        addCharacterEncoding( 0053, "plus" );
        addCharacterEncoding( 0161, "q" );
        addCharacterEncoding( 0077, "question" );
        addCharacterEncoding( 0277, "questiondown" );
        addCharacterEncoding( 0042, "quotedbl" );
        addCharacterEncoding( 0271, "quotedblbase" );
        addCharacterEncoding( 0252, "quotedblleft" );
        addCharacterEncoding( 0272, "quotedblright" );
        addCharacterEncoding( 0140, "quoteleft" );
        addCharacterEncoding( 0047, "quoteright" );
        addCharacterEncoding( 0270, "quotesinglbase" );
        addCharacterEncoding( 0251, "quotesingle" );
        addCharacterEncoding( 0162, "r" );
        addCharacterEncoding( 0312, "ring" );
        addCharacterEncoding( 0163, "s" );
        addCharacterEncoding( 0247, "section" );
        addCharacterEncoding( 0073, "semicolon" );
        addCharacterEncoding( 0067, "seven" );
        addCharacterEncoding( 0066, "six" );
        addCharacterEncoding( 0057, "slash" );
        addCharacterEncoding( 0040, "space" );
        addCharacterEncoding( 0243, "sterling" );
        addCharacterEncoding( 0164, "t" );
        addCharacterEncoding( 0063, "three" );
        addCharacterEncoding( 0304, "tilde" );
        addCharacterEncoding( 0062, "two" );
        addCharacterEncoding( 0165, "u" );
        addCharacterEncoding( 0137, "underscore" );
        addCharacterEncoding( 0166, "v" );
        addCharacterEncoding( 0167, "w" );
        addCharacterEncoding( 0170, "x" );
        addCharacterEncoding( 0171, "y" );
        addCharacterEncoding( 0245, "yen" );
        addCharacterEncoding( 0172, "z" );
        addCharacterEncoding( 0060, "zero" );
    }
}