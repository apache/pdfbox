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
package org.apache.fontbox.cff.charset;


/**
 * This is specialized CFFCharset. It's used if the CharsetId of a font is set to 0.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class CFFISOAdobeCharset extends CFFCharset
{

    private CFFISOAdobeCharset()
    {
    }

    /**
     * Returns an instance of the CFFExpertSubsetCharset class.
     * @return an instance of CFFExpertSubsetCharset
     */
    public static CFFISOAdobeCharset getInstance()
    {
        return CFFISOAdobeCharset.INSTANCE;
    }

    private static final CFFISOAdobeCharset INSTANCE = new CFFISOAdobeCharset();

    static
    {
        INSTANCE.register(1, "space");
        INSTANCE.register(2, "exclam");
        INSTANCE.register(3, "quotedbl");
        INSTANCE.register(4, "numbersign");
        INSTANCE.register(5, "dollar");
        INSTANCE.register(6, "percent");
        INSTANCE.register(7, "ampersand");
        INSTANCE.register(8, "quoteright");
        INSTANCE.register(9, "parenleft");
        INSTANCE.register(10, "parenright");
        INSTANCE.register(11, "asterisk");
        INSTANCE.register(12, "plus");
        INSTANCE.register(13, "comma");
        INSTANCE.register(14, "hyphen");
        INSTANCE.register(15, "period");
        INSTANCE.register(16, "slash");
        INSTANCE.register(17, "zero");
        INSTANCE.register(18, "one");
        INSTANCE.register(19, "two");
        INSTANCE.register(20, "three");
        INSTANCE.register(21, "four");
        INSTANCE.register(22, "five");
        INSTANCE.register(23, "six");
        INSTANCE.register(24, "seven");
        INSTANCE.register(25, "eight");
        INSTANCE.register(26, "nine");
        INSTANCE.register(27, "colon");
        INSTANCE.register(28, "semicolon");
        INSTANCE.register(29, "less");
        INSTANCE.register(30, "equal");
        INSTANCE.register(31, "greater");
        INSTANCE.register(32, "question");
        INSTANCE.register(33, "at");
        INSTANCE.register(34, "A");
        INSTANCE.register(35, "B");
        INSTANCE.register(36, "C");
        INSTANCE.register(37, "D");
        INSTANCE.register(38, "E");
        INSTANCE.register(39, "F");
        INSTANCE.register(40, "G");
        INSTANCE.register(41, "H");
        INSTANCE.register(42, "I");
        INSTANCE.register(43, "J");
        INSTANCE.register(44, "K");
        INSTANCE.register(45, "L");
        INSTANCE.register(46, "M");
        INSTANCE.register(47, "N");
        INSTANCE.register(48, "O");
        INSTANCE.register(49, "P");
        INSTANCE.register(50, "Q");
        INSTANCE.register(51, "R");
        INSTANCE.register(52, "S");
        INSTANCE.register(53, "T");
        INSTANCE.register(54, "U");
        INSTANCE.register(55, "V");
        INSTANCE.register(56, "W");
        INSTANCE.register(57, "X");
        INSTANCE.register(58, "Y");
        INSTANCE.register(59, "Z");
        INSTANCE.register(60, "bracketleft");
        INSTANCE.register(61, "backslash");
        INSTANCE.register(62, "bracketright");
        INSTANCE.register(63, "asciicircum");
        INSTANCE.register(64, "underscore");
        INSTANCE.register(65, "quoteleft");
        INSTANCE.register(66, "a");
        INSTANCE.register(67, "b");
        INSTANCE.register(68, "c");
        INSTANCE.register(69, "d");
        INSTANCE.register(70, "e");
        INSTANCE.register(71, "f");
        INSTANCE.register(72, "g");
        INSTANCE.register(73, "h");
        INSTANCE.register(74, "i");
        INSTANCE.register(75, "j");
        INSTANCE.register(76, "k");
        INSTANCE.register(77, "l");
        INSTANCE.register(78, "m");
        INSTANCE.register(79, "n");
        INSTANCE.register(80, "o");
        INSTANCE.register(81, "p");
        INSTANCE.register(82, "q");
        INSTANCE.register(83, "r");
        INSTANCE.register(84, "s");
        INSTANCE.register(85, "t");
        INSTANCE.register(86, "u");
        INSTANCE.register(87, "v");
        INSTANCE.register(88, "w");
        INSTANCE.register(89, "x");
        INSTANCE.register(90, "y");
        INSTANCE.register(91, "z");
        INSTANCE.register(92, "braceleft");
        INSTANCE.register(93, "bar");
        INSTANCE.register(94, "braceright");
        INSTANCE.register(95, "asciitilde");
        INSTANCE.register(96, "exclamdown");
        INSTANCE.register(97, "cent");
        INSTANCE.register(98, "sterling");
        INSTANCE.register(99, "fraction");
        INSTANCE.register(100, "yen");
        INSTANCE.register(101, "florin");
        INSTANCE.register(102, "section");
        INSTANCE.register(103, "currency");
        INSTANCE.register(104, "quotesingle");
        INSTANCE.register(105, "quotedblleft");
        INSTANCE.register(106, "guillemotleft");
        INSTANCE.register(107, "guilsinglleft");
        INSTANCE.register(108, "guilsinglright");
        INSTANCE.register(109, "fi");
        INSTANCE.register(110, "fl");
        INSTANCE.register(111, "endash");
        INSTANCE.register(112, "dagger");
        INSTANCE.register(113, "daggerdbl");
        INSTANCE.register(114, "periodcentered");
        INSTANCE.register(115, "paragraph");
        INSTANCE.register(116, "bullet");
        INSTANCE.register(117, "quotesinglbase");
        INSTANCE.register(118, "quotedblbase");
        INSTANCE.register(119, "quotedblright");
        INSTANCE.register(120, "guillemotright");
        INSTANCE.register(121, "ellipsis");
        INSTANCE.register(122, "perthousand");
        INSTANCE.register(123, "questiondown");
        INSTANCE.register(124, "grave");
        INSTANCE.register(125, "acute");
        INSTANCE.register(126, "circumflex");
        INSTANCE.register(127, "tilde");
        INSTANCE.register(128, "macron");
        INSTANCE.register(129, "breve");
        INSTANCE.register(130, "dotaccent");
        INSTANCE.register(131, "dieresis");
        INSTANCE.register(132, "ring");
        INSTANCE.register(133, "cedilla");
        INSTANCE.register(134, "hungarumlaut");
        INSTANCE.register(135, "ogonek");
        INSTANCE.register(136, "caron");
        INSTANCE.register(137, "emdash");
        INSTANCE.register(138, "AE");
        INSTANCE.register(139, "ordfeminine");
        INSTANCE.register(140, "Lslash");
        INSTANCE.register(141, "Oslash");
        INSTANCE.register(142, "OE");
        INSTANCE.register(143, "ordmasculine");
        INSTANCE.register(144, "ae");
        INSTANCE.register(145, "dotlessi");
        INSTANCE.register(146, "lslash");
        INSTANCE.register(147, "oslash");
        INSTANCE.register(148, "oe");
        INSTANCE.register(149, "germandbls");
        INSTANCE.register(150, "onesuperior");
        INSTANCE.register(151, "logicalnot");
        INSTANCE.register(152, "mu");
        INSTANCE.register(153, "trademark");
        INSTANCE.register(154, "Eth");
        INSTANCE.register(155, "onehalf");
        INSTANCE.register(156, "plusminus");
        INSTANCE.register(157, "Thorn");
        INSTANCE.register(158, "onequarter");
        INSTANCE.register(159, "divide");
        INSTANCE.register(160, "brokenbar");
        INSTANCE.register(161, "degree");
        INSTANCE.register(162, "thorn");
        INSTANCE.register(163, "threequarters");
        INSTANCE.register(164, "twosuperior");
        INSTANCE.register(165, "registered");
        INSTANCE.register(166, "minus");
        INSTANCE.register(167, "eth");
        INSTANCE.register(168, "multiply");
        INSTANCE.register(169, "threesuperior");
        INSTANCE.register(170, "copyright");
        INSTANCE.register(171, "Aacute");
        INSTANCE.register(172, "Acircumflex");
        INSTANCE.register(173, "Adieresis");
        INSTANCE.register(174, "Agrave");
        INSTANCE.register(175, "Aring");
        INSTANCE.register(176, "Atilde");
        INSTANCE.register(177, "Ccedilla");
        INSTANCE.register(178, "Eacute");
        INSTANCE.register(179, "Ecircumflex");
        INSTANCE.register(180, "Edieresis");
        INSTANCE.register(181, "Egrave");
        INSTANCE.register(182, "Iacute");
        INSTANCE.register(183, "Icircumflex");
        INSTANCE.register(184, "Idieresis");
        INSTANCE.register(185, "Igrave");
        INSTANCE.register(186, "Ntilde");
        INSTANCE.register(187, "Oacute");
        INSTANCE.register(188, "Ocircumflex");
        INSTANCE.register(189, "Odieresis");
        INSTANCE.register(190, "Ograve");
        INSTANCE.register(191, "Otilde");
        INSTANCE.register(192, "Scaron");
        INSTANCE.register(193, "Uacute");
        INSTANCE.register(194, "Ucircumflex");
        INSTANCE.register(195, "Udieresis");
        INSTANCE.register(196, "Ugrave");
        INSTANCE.register(197, "Yacute");
        INSTANCE.register(198, "Ydieresis");
        INSTANCE.register(199, "Zcaron");
        INSTANCE.register(200, "aacute");
        INSTANCE.register(201, "acircumflex");
        INSTANCE.register(202, "adieresis");
        INSTANCE.register(203, "agrave");
        INSTANCE.register(204, "aring");
        INSTANCE.register(205, "atilde");
        INSTANCE.register(206, "ccedilla");
        INSTANCE.register(207, "eacute");
        INSTANCE.register(208, "ecircumflex");
        INSTANCE.register(209, "edieresis");
        INSTANCE.register(210, "egrave");
        INSTANCE.register(211, "iacute");
        INSTANCE.register(212, "icircumflex");
        INSTANCE.register(213, "idieresis");
        INSTANCE.register(214, "igrave");
        INSTANCE.register(215, "ntilde");
        INSTANCE.register(216, "oacute");
        INSTANCE.register(217, "ocircumflex");
        INSTANCE.register(218, "odieresis");
        INSTANCE.register(219, "ograve");
        INSTANCE.register(220, "otilde");
        INSTANCE.register(221, "scaron");
        INSTANCE.register(222, "uacute");
        INSTANCE.register(223, "ucircumflex");
        INSTANCE.register(224, "udieresis");
        INSTANCE.register(225, "ugrave");
        INSTANCE.register(226, "yacute");
        INSTANCE.register(227, "ydieresis");
        INSTANCE.register(228, "zcaron");
    }
}