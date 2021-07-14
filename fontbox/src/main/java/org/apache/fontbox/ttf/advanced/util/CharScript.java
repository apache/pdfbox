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

package org.apache.fontbox.ttf.advanced.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>Script related utilities.</p>
 *
 * @author Glenn Adams
 */
@SuppressWarnings("unchecked") 
public final class CharScript {

    // CSOFF: LineLength

    //
    // The following script codes are based on ISO 15924. Codes less than 1000 are
    // official assignments from 15924; those equal to or greater than 1000 are FOP
    // implementation specific.
    //
    /** hebrew script constant */
    public static final int SCRIPT_HEBREW                               = 125;  // 'hebr'
    /** mongolian script constant */
    public static final int SCRIPT_MONGOLIAN                            = 145;  // 'mong'
    /** arabic script constant */
    public static final int SCRIPT_ARABIC                               = 160;  // 'arab'
    /** greek script constant */
    public static final int SCRIPT_GREEK                                = 200;  // 'grek'
    /** latin script constant */
    public static final int SCRIPT_LATIN                                = 215;  // 'latn'
    /** cyrillic script constant */
    public static final int SCRIPT_CYRILLIC                             = 220;  // 'cyrl'
    /** georgian script constant */
    public static final int SCRIPT_GEORGIAN                             = 240;  // 'geor'
    /** bopomofo script constant */
    public static final int SCRIPT_BOPOMOFO                             = 285;  // 'bopo'
    /** hangul script constant */
    public static final int SCRIPT_HANGUL                               = 286;  // 'hang'
    /** gurmukhi script constant */
    public static final int SCRIPT_GURMUKHI                             = 310;  // 'guru'
    /** gurmukhi 2 script constant */
    public static final int SCRIPT_GURMUKHI_2                           = 1310; // 'gur2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** devanagari script constant */
    public static final int SCRIPT_DEVANAGARI                           = 315;  // 'deva'
    /** devanagari 2 script constant */
    public static final int SCRIPT_DEVANAGARI_2                         = 1315; // 'dev2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** gujarati script constant */
    public static final int SCRIPT_GUJARATI                             = 320;  // 'gujr'
    /** gujarati 2 script constant */
    public static final int SCRIPT_GUJARATI_2                           = 1320; // 'gjr2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** bengali script constant */
    public static final int SCRIPT_BENGALI                              = 326;  // 'beng'
    /** bengali 2 script constant */
    public static final int SCRIPT_BENGALI_2                            = 1326; // 'bng2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** oriya script constant */
    public static final int SCRIPT_ORIYA                                = 327;  // 'orya'
    /** oriya 2 script constant */
    public static final int SCRIPT_ORIYA_2                              = 1327; // 'ory2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** tibetan script constant */
    public static final int SCRIPT_TIBETAN                              = 330;  // 'tibt'
    /** telugu script constant */
    public static final int SCRIPT_TELUGU                               = 340;  // 'telu'
    /** telugu 2 script constant */
    public static final int SCRIPT_TELUGU_2                             = 1340; // 'tel2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** kannada script constant */
    public static final int SCRIPT_KANNADA                              = 345;  // 'knda'
    /** kannada 2 script constant */
    public static final int SCRIPT_KANNADA_2                            = 1345; // 'knd2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** tamil script constant */
    public static final int SCRIPT_TAMIL                                = 346;  // 'taml'
    /** tamil 2 script constant */
    public static final int SCRIPT_TAMIL_2                              = 1346; // 'tml2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** malayalam script constant */
    public static final int SCRIPT_MALAYALAM                            = 347;  // 'mlym'
    /** malayalam 2 script constant */
    public static final int SCRIPT_MALAYALAM_2                          = 1347; // 'mlm2'       -- MSFT (pseudo) script tag for variant shaping semantics
    /** sinhalese script constant */
    public static final int SCRIPT_SINHALESE                            = 348;  // 'sinh'
    /** burmese script constant */
    public static final int SCRIPT_BURMESE                              = 350;  // 'mymr'
    /** thai script constant */
    public static final int SCRIPT_THAI                                 = 352;  // 'thai'
    /** khmer script constant */
    public static final int SCRIPT_KHMER                                = 355;  // 'khmr'
    /** lao script constant */
    public static final int SCRIPT_LAO                                  = 356;  // 'laoo'
    /** hiragana script constant */
    public static final int SCRIPT_HIRAGANA                             = 410;  // 'hira'
    /** ethiopic script constant */
    public static final int SCRIPT_ETHIOPIC                             = 430;  // 'ethi'
    /** han script constant */
    public static final int SCRIPT_HAN                                  = 500;  // 'hani'
    /** katakana script constant */
    public static final int SCRIPT_KATAKANA                             = 410;  // 'kana'
    /** math script constant */
    public static final int SCRIPT_MATH                                 = 995;  // 'zmth'
    /** symbol script constant */
    public static final int SCRIPT_SYMBOL                               = 996;  // 'zsym'
    /** undetermined script constant */
    public static final int SCRIPT_UNDETERMINED                         = 998;  // 'zyyy'
    /** uncoded script constant */
    public static final int SCRIPT_UNCODED                              = 999;  // 'zzzz'

    /**
      * A static (class) parameter indicating whether V2 indic shaping
      * rules apply or not, with default being <code>true</code>.
      */
    private static final boolean USE_V2_INDIC = true;

    private CharScript() {
    }

    /**
     * Determine if character c is punctuation.
     * @param c a character represented as a unicode scalar value
     * @return true if character is punctuation
     */
    public static boolean isPunctuation(int c) {
        if ((c >= 0x0021) && (c <= 0x002F)) {             // basic latin punctuation
            return true;
        } else if ((c >= 0x003A) && (c <= 0x0040)) {      // basic latin punctuation
            return true;
        } else if ((c >= 0x005F) && (c <= 0x0060)) {      // basic latin punctuation
            return true;
        } else if ((c >= 0x007E) && (c <= 0x007E)) {      // basic latin punctuation
            return true;
        } else if ((c >= 0x007E) && (c <= 0x007E)) {      // basic latin punctuation
            return true;
        } else if ((c >= 0x00A1) && (c <= 0x00BF)) {      // latin supplement punctuation
            return true;
        } else if ((c >= 0x00D7) && (c <= 0x00D7)) {      // latin supplement punctuation
            return true;
        } else if ((c >= 0x00F7) && (c <= 0x00F7)) {      // latin supplement punctuation
            return true;
        } else if ((c >= 0x2000) && (c <= 0x206F)) {      // general punctuation
            return true;
        } else {                                                // [TBD] - not complete
            return false;
        }
    }

    /**
     * Determine if character c is a digit.
     * @param c a character represented as a unicode scalar value
     * @return true if character is a digit
     */
    public static boolean isDigit(int c) {
        if ((c >= 0x0030) && (c <= 0x0039)) {             // basic latin digits
            return true;
        } else {                                                // [TBD] - not complete
            return false;
        }
    }

    /**
     * Determine if character c belong to the hebrew script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to hebrew script
     */
    public static boolean isHebrew(int c) {
        if ((c >= 0x0590) && (c <= 0x05FF)) {             // hebrew block
            return true;
        } else if ((c >= 0xFB00) && (c <= 0xFB4F)) {      // hebrew presentation forms block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the mongolian script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to mongolian script
     */
    public static boolean isMongolian(int c) {
        if ((c >= 0x1800) && (c <= 0x18AF)) {             // mongolian block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the arabic script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to arabic script
     */
    public static boolean isArabic(int c) {
        if ((c >= 0x0600) && (c <= 0x06FF)) {             // arabic block
            return true;
        } else if ((c >= 0x0750) && (c <= 0x077F)) {      // arabic supplement block
            return true;
        } else if ((c >= 0xFB50) && (c <= 0xFDFF)) {      // arabic presentation forms a block
            return true;
        } else if ((c >= 0xFE70) && (c <= 0xFEFF)) {      // arabic presentation forms b block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the greek script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to greek script
     */
    public static boolean isGreek(int c) {
        if ((c >= 0x0370) && (c <= 0x03FF)) {             // greek (and coptic) block
            return true;
        } else if ((c >= 0x1F00) && (c <= 0x1FFF)) {      // greek extended block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the latin script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to latin script
     */
    public static boolean isLatin(int c) {
        if ((c >= 0x0041) && (c <= 0x005A)) {             // basic latin upper case
            return true;
        } else if ((c >= 0x0061) && (c <= 0x007A)) {      // basic latin lower case
            return true;
        } else if ((c >= 0x00C0) && (c <= 0x00D6)) {      // latin supplement upper case
            return true;
        } else if ((c >= 0x00D8) && (c <= 0x00DF)) {      // latin supplement upper case
            return true;
        } else if ((c >= 0x00E0) && (c <= 0x00F6)) {      // latin supplement lower case
            return true;
        } else if ((c >= 0x00F8) && (c <= 0x00FF)) {      // latin supplement lower case
            return true;
        } else if ((c >= 0x0100) && (c <= 0x017F)) {      // latin extended a
            return true;
        } else if ((c >= 0x0180) && (c <= 0x024F)) {      // latin extended b
            return true;
        } else if ((c >= 0x1E00) && (c <= 0x1EFF)) {      // latin extended additional
            return true;
        } else if ((c >= 0x2C60) && (c <= 0x2C7F)) {      // latin extended c
            return true;
        } else if ((c >= 0xA720) && (c <= 0xA7FF)) {      // latin extended d
            return true;
        } else if ((c >= 0xFB00) && (c <= 0xFB0F)) {      // latin ligatures
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the cyrillic script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to cyrillic script
     */
    public static boolean isCyrillic(int c) {
        if ((c >= 0x0400) && (c <= 0x04FF)) {             // cyrillic block
            return true;
        } else if ((c >= 0x0500) && (c <= 0x052F)) {      // cyrillic supplement block
            return true;
        } else if ((c >= 0x2DE0) && (c <= 0x2DFF)) {      // cyrillic extended-a block
            return true;
        } else if ((c >= 0xA640) && (c <= 0xA69F)) {      // cyrillic extended-b block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the georgian script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to georgian script
     */
    public static boolean isGeorgian(int c) {
        if ((c >= 0x10A0) && (c <= 0x10FF)) {             // georgian block
            return true;
        } else if ((c >= 0x2D00) && (c <= 0x2D2F)) {      // georgian supplement block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the hangul script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to hangul script
     */
    public static boolean isHangul(int c) {
        if ((c >= 0x1100) && (c <= 0x11FF)) {             // hangul jamo
            return true;
        } else if ((c >= 0x3130) && (c <= 0x318F)) {      // hangul compatibility jamo
            return true;
        } else if ((c >= 0xA960) && (c <= 0xA97F)) {      // hangul jamo extended a
            return true;
        } else if ((c >= 0xAC00) && (c <= 0xD7A3)) {      // hangul syllables
            return true;
        } else if ((c >= 0xD7B0) && (c <= 0xD7FF)) {      // hangul jamo extended a
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the gurmukhi script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to gurmukhi script
     */
    public static boolean isGurmukhi(int c) {
        if ((c >= 0x0A00) && (c <= 0x0A7F)) {             // gurmukhi block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the devanagari script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to devanagari script
     */
    public static boolean isDevanagari(int c) {
        if ((c >= 0x0900) && (c <= 0x097F)) {             // devangari block
            return true;
        } else if ((c >= 0xA8E0) && (c <= 0xA8FF)) {      // devangari extended block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the gujarati script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to gujarati script
     */
    public static boolean isGujarati(int c) {
        if ((c >= 0x0A80) && (c <= 0x0AFF)) {             // gujarati block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the bengali script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to bengali script
     */
    public static boolean isBengali(int c) {
        if ((c >= 0x0980) && (c <= 0x09FF)) {             // bengali block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the oriya script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to oriya script
     */
    public static boolean isOriya(int c) {
        if ((c >= 0x0B00) && (c <= 0x0B7F)) {             // oriya block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the tibetan script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to tibetan script
     */
    public static boolean isTibetan(int c) {
        if ((c >= 0x0F00) && (c <= 0x0FFF)) {             // tibetan block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the telugu script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to telugu script
     */
    public static boolean isTelugu(int c) {
        if ((c >= 0x0C00) && (c <= 0x0C7F)) {             // telugu block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the kannada script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to kannada script
     */
    public static boolean isKannada(int c) {
        if ((c >= 0x0C00) && (c <= 0x0C7F)) {             // kannada block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the tamil script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to tamil script
     */
    public static boolean isTamil(int c) {
        if ((c >= 0x0B80) && (c <= 0x0BFF)) {             // tamil block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the malayalam script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to malayalam script
     */
    public static boolean isMalayalam(int c) {
        if ((c >= 0x0D00) && (c <= 0x0D7F)) {             // malayalam block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the sinhalese script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to sinhalese script
     */
    public static boolean isSinhalese(int c) {
        if ((c >= 0x0D80) && (c <= 0x0DFF)) {             // sinhala block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the burmese script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to burmese script
     */
    public static boolean isBurmese(int c) {
        if ((c >= 0x1000) && (c <= 0x109F)) {             // burmese (myanmar) block
            return true;
        } else if ((c >= 0xAA60) && (c <= 0xAA7F)) {      // burmese (myanmar) extended block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the thai script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to thai script
     */
    public static boolean isThai(int c) {
        if ((c >= 0x0E00) && (c <= 0x0E7F)) {             // thai block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the khmer script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to khmer script
     */
    public static boolean isKhmer(int c) {
        if ((c >= 0x1780) && (c <= 0x17FF)) {             // khmer block
            return true;
        } else if ((c >= 0x19E0) && (c <= 0x19FF)) {      // khmer symbols block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the lao script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to lao script
     */
    public static boolean isLao(int c) {
        if ((c >= 0x0E80) && (c <= 0x0EFF)) {             // lao block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the ethiopic (amharic) script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to ethiopic (amharic) script
     */
    public static boolean isEthiopic(int c) {
        if ((c >= 0x1200) && (c <= 0x137F)) {             // ethiopic block
            return true;
        } else if ((c >= 0x1380) && (c <= 0x139F)) {      // ethoipic supplement block
            return true;
        } else if ((c >= 0x2D80) && (c <= 0x2DDF)) {      // ethoipic extended block
            return true;
        } else if ((c >= 0xAB00) && (c <= 0xAB2F)) {      // ethoipic extended-a block
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the han (unified cjk) script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to han (unified cjk) script
     */
    public static boolean isHan(int c) {
        if ((c >= 0x3400) && (c <= 0x4DBF)) {
            return true; // cjk unified ideographs extension a
        } else if ((c >= 0x4E00) && (c <= 0x9FFF)) {
            return true; // cjk unified ideographs
        } else if ((c >= 0xF900) && (c <= 0xFAFF)) {
            return true; // cjk compatibility ideographs
        } else if ((c >= 0x20000) && (c <= 0x2A6DF)) {
            return true; // cjk unified ideographs extension b
        } else if ((c >= 0x2A700) && (c <= 0x2B73F)) {
            return true; // cjk unified ideographs extension c
        } else if ((c >= 0x2F800) && (c <= 0x2FA1F)) {
            return true; // cjk compatibility ideographs supplement
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the bopomofo script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to bopomofo script
     */
    public static boolean isBopomofo(int c) {
        if ((c >= 0x3100) && (c <= 0x312F)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the hiragana script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to hiragana script
     */
    public static boolean isHiragana(int c) {
        if ((c >= 0x3040) && (c <= 0x309F)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if character c belong to the katakana script.
     * @param c a character represented as a unicode scalar value
     * @return true if character belongs to katakana script
     */
    public static boolean isKatakana(int c) {
        if ((c >= 0x30A0) && (c <= 0x30FF)) {
            return true;
        } else if ((c >= 0x31F0) && (c <= 0x31FF)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtain ISO15924 numeric script code of character. If script is not or cannot be determined,
     * then the script code 998 ('zyyy') is returned.
     * @param c the character to obtain script
     * @return an ISO15924 script code
     */
    public static int scriptOf(int c) { // [TBD] - needs optimization!!!
        if (isAnySpace(c)) {
            return SCRIPT_UNDETERMINED;
        } else if (isPunctuation(c)) {
            return SCRIPT_UNDETERMINED;
        } else if (isDigit(c)) {
            return SCRIPT_UNDETERMINED;
        } else if (isLatin(c)) {
            return SCRIPT_LATIN;
        } else if (isCyrillic(c)) {
            return SCRIPT_CYRILLIC;
        } else if (isGreek(c)) {
            return SCRIPT_GREEK;
        } else if (isHan(c)) {
            return SCRIPT_HAN;
        } else if (isBopomofo(c)) {
            return SCRIPT_BOPOMOFO;
        } else if (isKatakana(c)) {
            return SCRIPT_KATAKANA;
        } else if (isHiragana(c)) {
            return SCRIPT_HIRAGANA;
        } else if (isHangul(c)) {
            return SCRIPT_HANGUL;
        } else if (isArabic(c)) {
            return SCRIPT_ARABIC;
        } else if (isHebrew(c)) {
            return SCRIPT_HEBREW;
        } else if (isMongolian(c)) {
            return SCRIPT_MONGOLIAN;
        } else if (isGeorgian(c)) {
            return SCRIPT_GEORGIAN;
        } else if (isGurmukhi(c)) {
            return useV2IndicRules(SCRIPT_GURMUKHI);
        } else if (isDevanagari(c)) {
            return useV2IndicRules(SCRIPT_DEVANAGARI);
        } else if (isGujarati(c)) {
            return useV2IndicRules(SCRIPT_GUJARATI);
        } else if (isBengali(c)) {
            return useV2IndicRules(SCRIPT_BENGALI);
        } else if (isOriya(c)) {
            return useV2IndicRules(SCRIPT_ORIYA);
        } else if (isTibetan(c)) {
            return SCRIPT_TIBETAN;
        } else if (isTelugu(c)) {
            return useV2IndicRules(SCRIPT_TELUGU);
        } else if (isKannada(c)) {
            return useV2IndicRules(SCRIPT_KANNADA);
        } else if (isTamil(c)) {
            return useV2IndicRules(SCRIPT_TAMIL);
        } else if (isMalayalam(c)) {
            return useV2IndicRules(SCRIPT_MALAYALAM);
        } else if (isSinhalese(c)) {
            return SCRIPT_SINHALESE;
        } else if (isBurmese(c)) {
            return SCRIPT_BURMESE;
        } else if (isThai(c)) {
            return SCRIPT_THAI;
        } else if (isKhmer(c)) {
            return SCRIPT_KHMER;
        } else if (isLao(c)) {
            return SCRIPT_LAO;
        } else if (isEthiopic(c)) {
            return SCRIPT_ETHIOPIC;
        } else {
            return SCRIPT_UNDETERMINED;
        }
    }

    /**
     * Obtain the V2 indic script code corresponding to V1 indic script code SC if
     * and only iff V2 indic rules apply; otherwise return SC.
     * @param sc a V1 indic script code
     * @return either SC or the V2 flavor of SC if V2 indic rules apply
     */
    public static int useV2IndicRules(int sc) {
        if (USE_V2_INDIC) {
            return (sc < 1000) ? (sc + 1000) : sc;
        } else {
            return sc;
        }
    }

    /**
     * Obtain the  script codes of each character in a character sequence. If script
     * is not or cannot be determined for some character, then the script code 998
     * ('zyyy') is returned.
     * @param cs the character sequence
     * @return a (possibly empty) array of script codes
     */
    public static int[] scriptsOf(CharSequence cs) {
        Set s = new HashSet();
        for (int i = 0, n = cs.length(); i < n; i++) {
            s.add(Integer.valueOf(scriptOf(cs.charAt(i))));
        }
        int[] sa = new int [ s.size() ];
        int ns = 0;
        for (Iterator it = s.iterator(); it.hasNext();) {
            sa [ ns++ ] = ((Integer) it.next()) .intValue();
        }
        Arrays.sort(sa);
        return sa;
    }

    /**
     * Determine the dominant script of a character sequence.
     * @param cs the character sequence
     * @return the dominant script or SCRIPT_UNDETERMINED
     */
    public static int dominantScript(CharSequence cs) {
        Map m = new HashMap();
        for (int i = 0, n = cs.length(); i < n; i++) {
            int c = cs.charAt(i);
            int s = scriptOf(c);
            Integer k = Integer.valueOf(s);
            Integer v = (Integer) m.get(k);
            if (v != null) {
                m.put(k, Integer.valueOf(v.intValue() + 1));
            } else {
                m.put(k, Integer.valueOf(0));
            }
        }
        int sMax = -1;
        int cMax = -1;
        for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            Integer k = (Integer) e.getKey();
            int s = k.intValue();
            switch (s) {
            case SCRIPT_UNDETERMINED:
            case SCRIPT_UNCODED:
                break;
            default:
                Integer v = (Integer) e.getValue();
                assert v != null;
                int c = v.intValue();
                if (c > cMax) {
                    cMax = c;
                    sMax = s;
                }
                break;
            }
        }
        if (sMax < 0) {
            sMax = SCRIPT_UNDETERMINED;
        }
        return sMax;
    }

    /**
     * Determine if script tag denotes an 'Indic' script, where a
     * script is an 'Indic' script if it is intended to be processed by
     * the generic 'Indic' Script Processor.
     * @param script a script tag
     * @return true if script tag is a designated 'Indic' script
     */
    public static boolean isIndicScript(String script) {
        return isIndicScript(scriptCodeFromTag(script));
    }

    /**
     * Determine if script tag denotes an 'Indic' script, where a
     * script is an 'Indic' script if it is intended to be processed by
     * the generic 'Indic' Script Processor.
     * @param script a script code
     * @return true if script code is a designated 'Indic' script
     */
    public static boolean isIndicScript(int script) {
        switch (script) {
        case SCRIPT_BENGALI:
        case SCRIPT_BENGALI_2:
        case SCRIPT_BURMESE:
        case SCRIPT_DEVANAGARI:
        case SCRIPT_DEVANAGARI_2:
        case SCRIPT_GUJARATI:
        case SCRIPT_GUJARATI_2:
        case SCRIPT_GURMUKHI:
        case SCRIPT_GURMUKHI_2:
        case SCRIPT_KANNADA:
        case SCRIPT_KANNADA_2:
        case SCRIPT_MALAYALAM:
        case SCRIPT_MALAYALAM_2:
        case SCRIPT_ORIYA:
        case SCRIPT_ORIYA_2:
        case SCRIPT_TAMIL:
        case SCRIPT_TAMIL_2:
        case SCRIPT_TELUGU:
        case SCRIPT_TELUGU_2:
            return true;
        default:
            return false;
        }
    }

    /**
     * Determine the script tag associated with an internal script code.
     * @param code the script code
     * @return a  script tag
     */
    public static String scriptTagFromCode(int code) {
        Map<Integer, String> m = getScriptTagsMap();
        if (m != null) {
            String tag;
            if ((tag = m.get(Integer.valueOf(code))) != null) {
                return tag;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Determine the internal script code associated with a script tag.
     * @param tag the script tag
     * @return a script code
     */
    public static int scriptCodeFromTag(String tag) {
        Map<String, Integer> m = getScriptCodeMap();
        if (m != null) {
            Integer c;
            if ((c = m.get(tag)) != null) {
                return (int) c;
            } else {
                return SCRIPT_UNDETERMINED;
            }
        } else {
            return SCRIPT_UNDETERMINED;
        }
    }

    private static Map<Integer, String> scriptTagsMap;
    private static Map<String, Integer> scriptCodeMap;

    private static void putScriptTag(Map tm, Map cm, int code, String tag) {
        assert tag != null;
        assert tag.length() != 0;
        assert code >= 0;
        assert code <  2000;
        tm.put(Integer.valueOf(code), tag);
        cm.put(tag, Integer.valueOf(code));
    }

    private static void makeScriptMaps() {
        HashMap<Integer, String> tm = new HashMap<Integer, String>();
        HashMap<String, Integer> cm = new HashMap<String, Integer>();
        putScriptTag(tm, cm, SCRIPT_HEBREW, "hebr");
        putScriptTag(tm, cm, SCRIPT_MONGOLIAN, "mong");
        putScriptTag(tm, cm, SCRIPT_ARABIC, "arab");
        putScriptTag(tm, cm, SCRIPT_GREEK, "grek");
        putScriptTag(tm, cm, SCRIPT_LATIN, "latn");
        putScriptTag(tm, cm, SCRIPT_CYRILLIC, "cyrl");
        putScriptTag(tm, cm, SCRIPT_GEORGIAN, "geor");
        putScriptTag(tm, cm, SCRIPT_BOPOMOFO, "bopo");
        putScriptTag(tm, cm, SCRIPT_HANGUL, "hang");
        putScriptTag(tm, cm, SCRIPT_GURMUKHI, "guru");
        putScriptTag(tm, cm, SCRIPT_GURMUKHI_2, "gur2");
        putScriptTag(tm, cm, SCRIPT_DEVANAGARI, "deva");
        putScriptTag(tm, cm, SCRIPT_DEVANAGARI_2, "dev2");
        putScriptTag(tm, cm, SCRIPT_GUJARATI, "gujr");
        putScriptTag(tm, cm, SCRIPT_GUJARATI_2, "gjr2");
        putScriptTag(tm, cm, SCRIPT_BENGALI, "beng");
        putScriptTag(tm, cm, SCRIPT_BENGALI_2, "bng2");
        putScriptTag(tm, cm, SCRIPT_ORIYA, "orya");
        putScriptTag(tm, cm, SCRIPT_ORIYA_2, "ory2");
        putScriptTag(tm, cm, SCRIPT_TIBETAN, "tibt");
        putScriptTag(tm, cm, SCRIPT_TELUGU, "telu");
        putScriptTag(tm, cm, SCRIPT_TELUGU_2, "tel2");
        putScriptTag(tm, cm, SCRIPT_KANNADA, "knda");
        putScriptTag(tm, cm, SCRIPT_KANNADA_2, "knd2");
        putScriptTag(tm, cm, SCRIPT_TAMIL, "taml");
        putScriptTag(tm, cm, SCRIPT_TAMIL_2, "tml2");
        putScriptTag(tm, cm, SCRIPT_MALAYALAM, "mlym");
        putScriptTag(tm, cm, SCRIPT_MALAYALAM_2, "mlm2");
        putScriptTag(tm, cm, SCRIPT_SINHALESE, "sinh");
        putScriptTag(tm, cm, SCRIPT_BURMESE, "mymr");
        putScriptTag(tm, cm, SCRIPT_THAI, "thai");
        putScriptTag(tm, cm, SCRIPT_KHMER, "khmr");
        putScriptTag(tm, cm, SCRIPT_LAO, "laoo");
        putScriptTag(tm, cm, SCRIPT_HIRAGANA, "hira");
        putScriptTag(tm, cm, SCRIPT_ETHIOPIC, "ethi");
        putScriptTag(tm, cm, SCRIPT_HAN, "hani");
        putScriptTag(tm, cm, SCRIPT_KATAKANA, "kana");
        putScriptTag(tm, cm, SCRIPT_MATH, "zmth");
        putScriptTag(tm, cm, SCRIPT_SYMBOL, "zsym");
        putScriptTag(tm, cm, SCRIPT_UNDETERMINED, "zyyy");
        putScriptTag(tm, cm, SCRIPT_UNCODED, "zzzz");
        scriptTagsMap = tm;
        scriptCodeMap = cm;
    }

    private static Map<Integer, String> getScriptTagsMap() {
        if (scriptTagsMap == null) {
            makeScriptMaps();
        }
        return scriptTagsMap;
    }

    private static Map<String, Integer> getScriptCodeMap() {
        if (scriptCodeMap == null) {
            makeScriptMaps();
        }
        return scriptCodeMap;
    }

    private static boolean isAnySpace(int c) {
        return (isBreakableSpace(c) || isNonBreakableSpace(c));
    }

    private static boolean isBreakableSpace(int c) {
        return (c == '\u0020' || isFixedWidthSpace(c));
    }

    private static boolean isFixedWidthSpace(int c) {
        return (c >= '\u2000' && c <= '\u200B') || c == '\u3000';
    }

    private static boolean isNonBreakableSpace(int c) {
        return
            (c == '\u00A0'      // no-break space
            || c == '\u202F'    // narrow no-break space
            || c == '\u2060'    // word joiner
            || c == '\u3000'    // ideographic space
            || c == '\uFEFF');  // zero width no-break space
    }

}
