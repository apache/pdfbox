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

package org.apache.fontbox.ttf.advanced;

/**
 * <p>Script tags defined by OTF specification. Note that this set and their
 * values do not correspond with ISO 15924 or Unicode Script names.</p>
 *
 * @author Glenn Adams
 */
public final class OTFScript {
    public static final String ARABIC                           = "arab";
    public static final String ARMENIAN                         = "armn";
    public static final String AVESTAN                          = "avst";
    public static final String BALINESE                         = "bali";
    public static final String BAMUM                            = "bamu";
    public static final String BATAK                            = "batk";
    public static final String BENGALI                          = "beng";
    public static final String BENGALI_V2                       = "bng2";
    public static final String BOPOMOFO                         = "bopo";
    public static final String BRAILLE                          = "brai";
    public static final String BRAHMI                           = "brah";
    public static final String BUGINESE                         = "bugi";
    public static final String BUHID                            = "buhd";
    public static final String BYZANTINE_MUSIC                  = "byzm";
    public static final String CANADIAN_SYLLABICS               = "cans";
    public static final String CARIAN                           = "cari";
    public static final String CHAKMA                           = "cakm";
    public static final String CHAM                             = "cham";
    public static final String CHEROKEE                         = "cher";
    public static final String CJK_IDEOGRAPHIC                  = "hani";
    public static final String COPTIC                           = "copt";
    public static final String CYPRIOT_SYLLABARY                = "cprt";
    public static final String CYRILLIC                         = "cyrl";
    public static final String DEFAULT                          = "DFLT";
    public static final String DESERET                          = "dsrt";
    public static final String DEVANAGARI                       = "deva";
    public static final String DEVANAGARI_V2                    = "dev2";
    public static final String EGYPTIAN_HEIROGLYPHS             = "egyp";
    public static final String ETHIOPIC                         = "ethi";
    public static final String GEORGIAN                         = "geor";
    public static final String GLAGOLITIC                       = "glag";
    public static final String GOTHIC                           = "goth";
    public static final String GREEK                            = "grek";
    public static final String GUJARATI                         = "gujr";
    public static final String GUJARATI_V2                      = "gjr2";
    public static final String GURMUKHI                         = "guru";
    public static final String GURMUKHI_V2                      = "gur2";
    public static final String HANGUL                           = "hang";
    public static final String HANGUL_JAMO                      = "jamo";
    public static final String HANUNOO                          = "hano";
    public static final String HEBREW                           = "hebr";
    public static final String HIRAGANA                         = "kana";
    public static final String IMPERIAL_ARAMAIC                 = "armi";
    public static final String INSCRIPTIONAL_PAHLAVI            = "phli";
    public static final String INSCRIPTIONAL_PARTHIAN           = "prti";
    public static final String JAVANESE                         = "java";
    public static final String KAITHI                           = "kthi";
    public static final String KANNADA                          = "knda";
    public static final String KANNADA_V2                       = "knd2";
    public static final String KATAKANA                         = "kana";
    public static final String KAYAH_LI                         = "kali";
    public static final String KHAROSTHI                        = "khar";
    public static final String KHMER                            = "khmr";
    public static final String LAO                              = "lao";
    public static final String LATIN                            = "latn";
    public static final String LEPCHA                           = "lepc";
    public static final String LIMBU                            = "limb";
    public static final String LINEAR_B                         = "linb";
    public static final String LISU                             = "lisu";
    public static final String LYCIAN                           = "lyci";
    public static final String LYDIAN                           = "lydi";
    public static final String MALAYALAM                        = "mlym";
    public static final String MALAYALAM_V2                     = "mlm2";
    public static final String MANDAIC                          = "mand";
    public static final String MATHEMATICAL_ALPHANUMERIC_SYMBOLS = "math";
    public static final String MEITEI                           = "mtei";
    public static final String MEROITIC_CURSIVE                 = "merc";
    public static final String MEROITIC_HIEROGLYPHS             = "mero";
    public static final String MONGOLIAN                        = "mong";
    public static final String MUSICAL_SYMBOLS                  = "musc";
    public static final String MYANMAR                          = "mymr";
    public static final String NEW_TAI_LUE                      = "talu";
    public static final String NKO                              = "nko";
    public static final String OGHAM                            = "ogam";
    public static final String OL_CHIKI                         = "olck";
    public static final String OLD_ITALIC                       = "ital";
    public static final String OLD_PERSIAN_CUNEIFORM            = "xpeo";
    public static final String OLD_SOUTH_ARABIAN                = "sarb";
    public static final String OLD_TURKIC                       = "orkh";
    public static final String ORIYA                            = "orya";
    public static final String ORIYA_V2                         = "ory2";
    public static final String OSMANYA                          = "osma";
    public static final String PHAGS_PA                         = "phag";
    public static final String PHOENICIAN                       = "phnx";
    public static final String REJANG                           = "rjng";
    public static final String RUNIC                            = "runr";
    public static final String SAMARITAN                        = "samr";
    public static final String SAURASHTRA                       = "saur";
    public static final String SHARADA                          = "shrd";
    public static final String SHAVIAN                          = "shaw";
    public static final String SINHALA                          = "sinh";
    public static final String SORA_SOMPENG                     = "sora";
    public static final String SUMERO_AKKADIAN_CUNEIFORM        = "xsux";
    public static final String SUNDANESE                        = "sund";
    public static final String SYLOTI_NAGRI                     = "sylo";
    public static final String SYRIAC                           = "syrc";
    public static final String TAGALOG                          = "tglg";
    public static final String TAGBANWA                         = "tagb";
    public static final String TAI_LE                           = "tale";
    public static final String TAI_THAM                         = "lana";
    public static final String TAI_VIET                         = "tavt";
    public static final String TAKRI                            = "takr";
    public static final String TAMIL                            = "taml";
    public static final String TAMIL_V2                         = "tml2";
    public static final String TELUGU                           = "telu";
    public static final String TELUGU_V2                        = "tel2";
    public static final String THAANA                           = "thaa";
    public static final String THAI                             = "thai";
    public static final String TIBETAN                          = "tibt";
    public static final String TIFINAGH                         = "tfng";
    public static final String UGARITIC_CUNEIFORM               = "ugar";
    public static final String VAI                              = "vai";
    public static final String WILDCARD                         = "*";
    public static final String YI                               = "yi";

    public static boolean isDefault(String script) {
        return (script != null) && script.equals(DEFAULT);
    }

    public static boolean isWildCard(String script) {
        return (script != null) && script.equals(DEFAULT);
    }

    private OTFScript() {
    }
}
