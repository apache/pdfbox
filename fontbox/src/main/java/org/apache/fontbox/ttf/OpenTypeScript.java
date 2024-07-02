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
package org.apache.fontbox.ttf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A class for mapping Unicode codepoints to OpenType script tags
 *
 * @author Aaron Madlon-Kay
 *
 * @see <a href="https://www.microsoft.com/typography/otspec/scripttags.htm">Microsoft Typography:
 * Script Tags</a>
 * @see <a href="https://www.unicode.org/reports/tr24/">Unicode Script Property</a>
 */
public final class OpenTypeScript
{
    private static final Logger LOG = LogManager.getLogger(OpenTypeScript.class);

    public static final String INHERITED = "Inherited";
    public static final String UNKNOWN = "Unknown";
    public static final String TAG_DEFAULT = "DFLT";

    /**
     * A map associating Unicode scripts with one or more OpenType script tags. Script tags are not
     * necessarily the same as Unicode scripts. A single Unicode script may correspond to multiple
     * tags, especially when there has been a revision to the latter (e.g. Bengali -> [bng2, beng]).
     * When there are multiple tags, they are ordered from newest to oldest.
     *
     * @see <a href="https://www.microsoft.com/typography/otspec/scripttags.htm">Microsoft
     * Typography: Script Tags</a>
     */
    private static final Map<String, String[]> UNICODE_SCRIPT_TO_OPENTYPE_TAG_MAP = Map.ofEntries(
            Map.entry("Adlam", new String[] { "adlm" }),
            Map.entry("Ahom", new String[] { "ahom" }),
            Map.entry("Anatolian_Hieroglyphs", new String[] { "hluw" }),
            Map.entry("Arabic", new String[] { "arab" }),
            Map.entry("Armenian", new String[] { "armn" }),
            Map.entry("Avestan", new String[] { "avst" }),
            Map.entry("Balinese", new String[] { "bali" }),
            Map.entry("Bamum", new String[] { "bamu" }),
            Map.entry("Bassa_Vah", new String[] { "bass" }),
            Map.entry("Batak", new String[] { "batk" }),
            Map.entry("Bengali", new String[] { "bng2", "beng" }),
            Map.entry("Bhaiksuki", new String[] { "bhks" }),
            Map.entry("Bopomofo", new String[] { "bopo" }),
            Map.entry("Brahmi", new String[] { "brah" }),
            Map.entry("Braille", new String[] { "brai" }),
            Map.entry("Buginese", new String[] { "bugi" }),
            Map.entry("Buhid", new String[] { "buhd" }),
            // Byzantine Music: byzm
            Map.entry("Canadian_Aboriginal", new String[] { "cans" }),
            Map.entry("Carian", new String[] { "cari" }),
            Map.entry("Caucasian_Albanian", new String[] { "aghb" }),
            Map.entry("Chakma", new String[] { "cakm" }),
            Map.entry("Cham", new String[] { "cham" }),
            Map.entry("Cherokee", new String[] { "cher" }),
            Map.entry("Common", new String[] { TAG_DEFAULT }), // "Default" in OpenType
            Map.entry("Coptic", new String[] { "copt" }),
            Map.entry("Cuneiform", new String[] { "xsux" }), // "Sumero-Akkadian Cuneiform" in OpenType
            Map.entry("Cypriot", new String[] { "cprt" }),
            Map.entry("Cyrillic", new String[] { "cyrl" }),
            Map.entry("Deseret", new String[] { "dsrt" }),
            Map.entry("Devanagari", new String[] { "dev2", "deva" }),
            Map.entry("Duployan", new String[] { "dupl" }),
            Map.entry("Egyptian_Hieroglyphs", new String[] { "egyp" }),
            Map.entry("Elbasan", new String[] { "elba" }),
            Map.entry("Ethiopic", new String[] { "ethi" }),
            Map.entry("Georgian", new String[] { "geor" }),
            Map.entry("Glagolitic", new String[] { "glag" }),
            Map.entry("Gothic", new String[] { "goth" }),
            Map.entry("Grantha", new String[] { "gran" }),
            Map.entry("Greek", new String[] { "grek" }),
            Map.entry("Gujarati", new String[] { "gjr2", "gujr" }),
            Map.entry("Gurmukhi", new String[] { "gur2", "guru" }),
            Map.entry("Han", new String[] { "hani" }), // "CJK Ideographic" in OpenType
            Map.entry("Hangul", new String[] { "hang" }),
            // Hangul Jamo: jamo
            Map.entry("Hanunoo", new String[] { "hano" }),
            Map.entry("Hatran", new String[] { "hatr" }),
            Map.entry("Hebrew", new String[] { "hebr" }),
            Map.entry("Hiragana", new String[] { "kana" }),
            Map.entry("Imperial_Aramaic", new String[] { "armi" }),
            Map.entry(INHERITED, new String[] { INHERITED }),
            Map.entry("Inscriptional_Pahlavi", new String[] { "phli" }),
            Map.entry("Inscriptional_Parthian", new String[] { "prti" }),
            Map.entry("Javanese", new String[] { "java" }),
            Map.entry("Kaithi", new String[] { "kthi" }),
            Map.entry("Kannada", new String[] { "knd2", "knda" }),
            Map.entry("Katakana", new String[] { "kana" }),
            Map.entry("Kayah_Li", new String[] { "kali" }),
            Map.entry("Kharoshthi", new String[] { "khar" }),
            Map.entry("Khmer", new String[] { "khmr" }),
            Map.entry("Khojki", new String[] { "khoj" }),
            Map.entry("Khudawadi", new String[] { "sind" }),
            Map.entry("Lao", new String[] { "lao " }),
            Map.entry("Latin", new String[] { "latn" }),
            Map.entry("Lepcha", new String[] { "lepc" }),
            Map.entry("Limbu", new String[] { "limb" }),
            Map.entry("Linear_A", new String[] { "lina" }),
            Map.entry("Linear_B", new String[] { "linb" }),
            Map.entry("Lisu", new String[] { "lisu" }),
            Map.entry("Lycian", new String[] { "lyci" }),
            Map.entry("Lydian", new String[] { "lydi" }),
            Map.entry("Mahajani", new String[] { "mahj" }),
            Map.entry("Malayalam", new String[] { "mlm2", "mlym" }),
            Map.entry("Mandaic", new String[] { "mand" }),
            Map.entry("Manichaean", new String[] { "mani" }),
            Map.entry("Marchen", new String[] { "marc" }),
            // Mathematical Alphanumeric Symbols: math
            Map.entry("Meetei_Mayek", new String[] { "mtei" }),
            Map.entry("Mende_Kikakui", new String[] { "mend" }),
            Map.entry("Meroitic_Cursive", new String[] { "merc" }),
            Map.entry("Meroitic_Hieroglyphs", new String[] { "mero" }),
            Map.entry("Miao", new String[] { "plrd" }),
            Map.entry("Modi", new String[] { "modi" }),
            Map.entry("Mongolian", new String[] { "mong" }),
            Map.entry("Mro", new String[] { "mroo" }),
            Map.entry("Multani", new String[] { "mult" }),
            // Musical Symbols: musc
            Map.entry("Myanmar", new String[] { "mym2", "mymr" }),
            Map.entry("Nabataean", new String[] { "nbat" }),
            Map.entry("Newa", new String[] { "newa" }),
            Map.entry("New_Tai_Lue", new String[] { "talu" }),
            Map.entry("Nko", new String[] { "nko " }),
            Map.entry("Ogham", new String[] { "ogam" }),
            Map.entry("Ol_Chiki", new String[] { "olck" }),
            Map.entry("Old_Italic", new String[] { "ital" }),
            Map.entry("Old_Hungarian", new String[] { "hung" }),
            Map.entry("Old_North_Arabian", new String[] { "narb" }),
            Map.entry("Old_Permic", new String[] { "perm" }),
            Map.entry("Old_Persian", new String[] { "xpeo" }),
            Map.entry("Old_South_Arabian", new String[] { "sarb" }),
            Map.entry("Old_Turkic", new String[] { "orkh" }),
            Map.entry("Oriya", new String[] { "ory2", "orya" }), // "Odia (formerly Oriya)" in OpenType
            Map.entry("Osage", new String[] { "osge" }),
            Map.entry("Osmanya", new String[] { "osma" }),
            Map.entry("Pahawh_Hmong", new String[] { "hmng" }),
            Map.entry("Palmyrene", new String[] { "palm" }),
            Map.entry("Pau_Cin_Hau", new String[] { "pauc" }),
            Map.entry("Phags_Pa", new String[] { "phag" }),
            Map.entry("Phoenician", new String[] { "phnx" }),
            Map.entry("Psalter_Pahlavi", new String[] { "phlp" }),
            Map.entry("Rejang", new String[] { "rjng" }),
            Map.entry("Runic", new String[] { "runr" }),
            Map.entry("Samaritan", new String[] { "samr" }),
            Map.entry("Saurashtra", new String[] { "saur" }),
            Map.entry("Sharada", new String[] { "shrd" }),
            Map.entry("Shavian", new String[] { "shaw" }),
            Map.entry("Siddham", new String[] { "sidd" }),
            Map.entry("SignWriting", new String[] { "sgnw" }),
            Map.entry("Sinhala", new String[] { "sinh" }),
            Map.entry("Sora_Sompeng", new String[] { "sora" }),
            Map.entry("Sundanese", new String[] { "sund" }),
            Map.entry("Syloti_Nagri", new String[] { "sylo" }),
            Map.entry("Syriac", new String[] { "syrc" }),
            Map.entry("Tagalog", new String[] { "tglg" }),
            Map.entry("Tagbanwa", new String[] { "tagb" }),
            Map.entry("Tai_Le", new String[] { "tale" }),
            Map.entry("Tai_Tham", new String[] { "lana" }),
            Map.entry("Tai_Viet", new String[] { "tavt" }),
            Map.entry("Takri", new String[] { "takr" }),
            Map.entry("Tamil", new String[] { "tml2", "taml" }),
            Map.entry("Tangut", new String[] { "tang" }),
            Map.entry("Telugu", new String[] { "tel2", "telu" }),
            Map.entry("Thaana", new String[] { "thaa" }),
            Map.entry("Thai", new String[] { "thai" }),
            Map.entry("Tibetan", new String[] { "tibt" }),
            Map.entry("Tifinagh", new String[] { "tfng" }),
            Map.entry("Tirhuta", new String[] { "tirh" }),
            Map.entry("Ugaritic", new String[] { "ugar" }),
            Map.entry(UNKNOWN, new String[] { TAG_DEFAULT }),
            Map.entry("Vai", new String[] { "vai " }),
            Map.entry("Warang_Citi", new String[] { "wara" }),
            Map.entry("Yi", new String[] { "yi  " })
    );

    private static int[] unicodeRangeStarts;
    private static String[] unicodeRangeScripts;

    static
    {
        String path = "/org/apache/fontbox/unicode/Scripts.txt";
        try (InputStream resourceAsStream = OpenTypeScript.class.getResourceAsStream(path);
             InputStream input = new BufferedInputStream(resourceAsStream))
        {
            parseScriptsFile(input);
        }
        catch (IOException e)
        {
            LOG.warn(() -> "Could not parse Scripts.txt, mirroring char map will be empty: " + 
                    e.getMessage(), e);
        }
    }

    private OpenTypeScript()
    {
    }

    private static void parseScriptsFile(InputStream inputStream) throws IOException
    {
        Map<int[], String> unicodeRanges = new TreeMap<>(Comparator.comparingInt(o -> o[0]));
        try (LineNumberReader rd = new LineNumberReader(new InputStreamReader(inputStream)))
        {
            int[] lastRange = { Integer.MIN_VALUE, Integer.MIN_VALUE };
            String lastScript = null;
            do
            {
                String s = rd.readLine();
                if (s == null)
                {
                    break;
                }
                
                // ignore comments
                int comment = s.indexOf('#');
                if (comment != -1)
                {
                    s = s.substring(0, comment);
                }
                
                if (s.length() < 2)
                {
                    continue;
                }
                
                StringTokenizer st = new StringTokenizer(s, ";");
                int nFields = st.countTokens();
                if (nFields < 2)
                {
                    continue;
                }
                String characters = st.nextToken().trim();
                String script = st.nextToken().trim();
                int[] range = new int[2];
                int rangeDelim = characters.indexOf("..");
                if (rangeDelim == -1)
                {
                    range[0] = range[1] = Integer.parseInt(characters, 16);
                }
                else
                {
                    range[0] = Integer.parseInt(characters.substring(0, rangeDelim), 16);
                    range[1] = Integer.parseInt(characters.substring(rangeDelim + 2), 16);
                }
                if (range[0] == lastRange[1] + 1 && script.equals(lastScript))
                {
                    // Combine with previous range
                    lastRange[1] = range[1];
                }
                else
                {
                    unicodeRanges.put(range, script);
                    lastRange = range;
                    lastScript = script;
                }
            }
            while (true);
        }

        unicodeRangeStarts = new int[unicodeRanges.size()];
        unicodeRangeScripts = new String[unicodeRanges.size()];
        int i = 0;
        for (Entry<int[], String> e : unicodeRanges.entrySet())
        {
            unicodeRangeStarts[i] = e.getKey()[0];
            unicodeRangeScripts[i] = e.getValue();
            i++;
        }
    }

    /**
     * Obtain the Unicode script associated with the given Unicode codepoint.
     *
     * @param codePoint
     * @return A Unicode script string, or {@code #UNKNOWN} if unknown
     */
    private static String getUnicodeScript(int codePoint)
    {
        ensureValidCodePoint(codePoint);
        int type = Character.getType(codePoint);
        if (type == Character.UNASSIGNED)
        {
            return UNKNOWN;
        }
        int scriptIndex = Arrays.binarySearch(unicodeRangeStarts, codePoint);
        if (scriptIndex < 0)
        {
            scriptIndex = -scriptIndex - 2;
        }
        return unicodeRangeScripts[scriptIndex];
    }

    /**
     * Obtain the OpenType script tags associated with the given Unicode codepoint.
     *
     * The result may contain the special value {@code #INHERITED}, which indicates that the codepoint's script can only
     * be determined by its context.
     *
     * Unknown codepoints are mapped to {@code #TAG_DEFAULT}.
     *
     * @param codePoint the unicode codepoint of the OpenType script tag
     * @return An array of four-char script tags
     */
    public static String[] getScriptTags(int codePoint)
    {
        ensureValidCodePoint(codePoint);
        String unicode = getUnicodeScript(codePoint);
        return UNICODE_SCRIPT_TO_OPENTYPE_TAG_MAP.get(unicode);
    }

    private static void ensureValidCodePoint(int codePoint)
    {
        if (codePoint < Character.MIN_CODE_POINT || codePoint > Character.MAX_CODE_POINT)
        {
            throw new IllegalArgumentException("Invalid codepoint: " + codePoint);
        }
    }
}
