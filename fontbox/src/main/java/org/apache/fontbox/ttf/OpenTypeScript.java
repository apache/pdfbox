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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log LOG = LogFactory.getLog(OpenTypeScript.class);

    public static final String INHERITED = "Inherited";
    public static final String UNKNOWN = "Unknown";
    public static final String TAG_DEFAULT = "DFLT";

    /**
     * A map associating Unicode scripts with one or more OpenType script tags. Script tags are not necessarily the same
     * as Unicode scripts. A single Unicode script may correspond to multiple tags, especially when there has been a
     * revision to the latter (e.g. Bengali -> [bng2, beng]). When there are multiple tags, they are ordered from newest
     * to oldest.
     *
     * @see <a href="https://www.microsoft.com/typography/otspec/scripttags.htm">Microsoft Typography: Script Tags</a>
     */
    private static final Map<String, String[]> UNICODE_SCRIPT_TO_OPENTYPE_TAG_MAP;

    static
    {
        Object[][] table = 
        {
            {"Adlam", new String[] { "adlm" }},
            {"Ahom", new String[] { "ahom" }},
            {"Anatolian_Hieroglyphs", new String[] { "hluw" }},
            {"Arabic", new String[] { "arab" }},
            {"Armenian", new String[] { "armn" }},
            {"Avestan", new String[] { "avst" }},
            {"Balinese", new String[] { "bali" }},
            {"Bamum", new String[] { "bamu" }},
            {"Bassa_Vah", new String[] { "bass" }},
            {"Batak", new String[] { "batk" }},
            {"Bengali", new String[] { "bng2", "beng" }},
            {"Bhaiksuki", new String[] { "bhks" }},
            {"Bopomofo", new String[] { "bopo" }},
            {"Brahmi", new String[] { "brah" }},
            {"Braille", new String[] { "brai" }},
            {"Buginese", new String[] { "bugi" }},
            {"Buhid", new String[] { "buhd" }},
            // Byzantine Music: byzm
            {"Canadian_Aboriginal", new String[] { "cans" }},
            {"Carian", new String[] { "cari" }},
            {"Caucasian_Albanian", new String[] { "aghb" }},
            {"Chakma", new String[] { "cakm" }},
            {"Cham", new String[] { "cham" }},
            {"Cherokee", new String[] { "cher" }},
            {"Common", new String[] { TAG_DEFAULT }}, // "Default" in OpenType
            {"Coptic", new String[] { "copt" }},
            {"Cuneiform", new String[] { "xsux" }}, // "Sumero-Akkadian Cuneiform" in OpenType
            {"Cypriot", new String[] { "cprt" }},
            {"Cyrillic", new String[] { "cyrl" }},
            {"Deseret", new String[] { "dsrt" }},
            {"Devanagari", new String[] { "dev2", "deva" }},
            {"Duployan", new String[] { "dupl" }},
            {"Egyptian_Hieroglyphs", new String[] { "egyp" }},
            {"Elbasan", new String[] { "elba" }},
            {"Ethiopic", new String[] { "ethi" }},
            {"Georgian", new String[] { "geor" }},
            {"Glagolitic", new String[] { "glag" }},
            {"Gothic", new String[] { "goth" }},
            {"Grantha", new String[] { "gran" }},
            {"Greek", new String[] { "grek" }},
            {"Gujarati", new String[] { "gjr2", "gujr" }},
            {"Gurmukhi", new String[] { "gur2", "guru" }},
            {"Han", new String[] { "hani" }}, // "CJK Ideographic" in OpenType
            {"Hangul", new String[] { "hang" }},
            // Hangul Jamo: jamo
            {"Hanunoo", new String[] { "hano" }},
            {"Hatran", new String[] { "hatr" }},
            {"Hebrew", new String[] { "hebr" }},
            {"Hiragana", new String[] { "kana" }},
            {"Imperial_Aramaic", new String[] { "armi" }},
            {INHERITED, new String[] { INHERITED }},
            {"Inscriptional_Pahlavi", new String[] { "phli" }},
            {"Inscriptional_Parthian", new String[] { "prti" }},
            {"Javanese", new String[] { "java" }},
            {"Kaithi", new String[] { "kthi" }},
            {"Kannada", new String[] { "knd2", "knda" }},
            {"Katakana", new String[] { "kana" }},
            {"Kayah_Li", new String[] { "kali" }},
            {"Kharoshthi", new String[] { "khar" }},
            {"Khmer", new String[] { "khmr" }},
            {"Khojki", new String[] { "khoj" }},
            {"Khudawadi", new String[] { "sind" }},
            {"Lao", new String[] { "lao " }},
            {"Latin", new String[] { "latn" }},
            {"Lepcha", new String[] { "lepc" }},
            {"Limbu", new String[] { "limb" }},
            {"Linear_A", new String[] { "lina" }},
            {"Linear_B", new String[] { "linb" }},
            {"Lisu", new String[] { "lisu" }},
            {"Lycian", new String[] { "lyci" }},
            {"Lydian", new String[] { "lydi" }},
            {"Mahajani", new String[] { "mahj" }},
            {"Malayalam", new String[] { "mlm2", "mlym" }},
            {"Mandaic", new String[] { "mand" }},
            {"Manichaean", new String[] { "mani" }},
            {"Marchen", new String[] { "marc" }},
            // Mathematical Alphanumeric Symbols: math
            {"Meetei_Mayek", new String[] { "mtei" }},
            {"Mende_Kikakui", new String[] { "mend" }},
            {"Meroitic_Cursive", new String[] { "merc" }},
            {"Meroitic_Hieroglyphs", new String[] { "mero" }},
            {"Miao", new String[] { "plrd" }},
            {"Modi", new String[] { "modi" }},
            {"Mongolian", new String[] { "mong" }},
            {"Mro", new String[] { "mroo" }},
            {"Multani", new String[] { "mult" }},
            // Musical Symbols: musc
            {"Myanmar", new String[] { "mym2", "mymr" }},
            {"Nabataean", new String[] { "nbat" }},
            {"Newa", new String[] { "newa" }},
            {"New_Tai_Lue", new String[] { "talu" }},
            {"Nko", new String[] { "nko " }},
            {"Ogham", new String[] { "ogam" }},
            {"Ol_Chiki", new String[] { "olck" }},
            {"Old_Italic", new String[] { "ital" }},
            {"Old_Hungarian", new String[] { "hung" }},
            {"Old_North_Arabian", new String[] { "narb" }},
            {"Old_Permic", new String[] { "perm" }},
            {"Old_Persian", new String[] { "xpeo" }},
            {"Old_South_Arabian", new String[] { "sarb" }},
            {"Old_Turkic", new String[] { "orkh" }},
            {"Oriya", new String[] { "ory2", "orya" }}, // "Odia (formerly Oriya)" in OpenType
            {"Osage", new String[] { "osge" }},
            {"Osmanya", new String[] { "osma" }},
            {"Pahawh_Hmong", new String[] { "hmng" }},
            {"Palmyrene", new String[] { "palm" }},
            {"Pau_Cin_Hau", new String[] { "pauc" }},
            {"Phags_Pa", new String[] { "phag" }},
            {"Phoenician", new String[] { "phnx" }},
            {"Psalter_Pahlavi", new String[] { "phlp" }},
            {"Rejang", new String[] { "rjng" }},
            {"Runic", new String[] { "runr" }},
            {"Samaritan", new String[] { "samr" }},
            {"Saurashtra", new String[] { "saur" }},
            {"Sharada", new String[] { "shrd" }},
            {"Shavian", new String[] { "shaw" }},
            {"Siddham", new String[] { "sidd" }},
            {"SignWriting", new String[] { "sgnw" }},
            {"Sinhala", new String[] { "sinh" }},
            {"Sora_Sompeng", new String[] { "sora" }},
            {"Sundanese", new String[] { "sund" }},
            {"Syloti_Nagri", new String[] { "sylo" }},
            {"Syriac", new String[] { "syrc" }},
            {"Tagalog", new String[] { "tglg" }},
            {"Tagbanwa", new String[] { "tagb" }},
            {"Tai_Le", new String[] { "tale" }},
            {"Tai_Tham", new String[] { "lana" }},
            {"Tai_Viet", new String[] { "tavt" }},
            {"Takri", new String[] { "takr" }},
            {"Tamil", new String[] { "tml2", "taml" }},
            {"Tangut", new String[] { "tang" }},
            {"Telugu", new String[] { "tel2", "telu" }},
            {"Thaana", new String[] { "thaa" }},
            {"Thai", new String[] { "thai" }},
            {"Tibetan", new String[] { "tibt" }},
            {"Tifinagh", new String[] { "tfng" }},
            {"Tirhuta", new String[] { "tirh" }},
            {"Ugaritic", new String[] { "ugar" }},
            {UNKNOWN, new String[] { TAG_DEFAULT }},
            {"Vai", new String[] { "vai " }},
            {"Warang_Citi", new String[] { "wara" }},
            {"Yi", new String[] { "yi  " }}
        };
        UNICODE_SCRIPT_TO_OPENTYPE_TAG_MAP = new HashMap<String, String[]>(table.length);
        for (Object obj : table)
        {
            Object[] array = (Object[]) obj;
            UNICODE_SCRIPT_TO_OPENTYPE_TAG_MAP.put((String) array[0], (String[]) array[1]);
        }
    }

    private static int[] unicodeRangeStarts;
    private static String[] unicodeRangeScripts;

    static
    {
        String path = "/org/apache/fontbox/unicode/Scripts.txt";
        InputStream input = null;
        try
        {
            input = OpenTypeScript.class.getResourceAsStream(path);
            if (input != null)
            {
                parseScriptsFile(input);
            }
            else
            {
                LOG.warn("Could not find '" + path + "', mirroring char map will be empty: ");
            }
        }
        catch (IOException e)
        {
            LOG.warn("Could not parse Scripts.txt, mirroring char map will be empty: "
                    + e.getMessage());
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException ex)
                {
                    LOG.warn("Could not close Scripts.txt");
                }
            }
        }
    }

    private OpenTypeScript()
    {
    }

    private static void parseScriptsFile(InputStream inputStream) throws IOException
    {
        Map<int[], String> unicodeRanges = new TreeMap<int[], String>(new Comparator<int[]>()
        {
            @Override
            public int compare(int[] o1, int[] o2)
            {
                return o1[0] < o2[0] ? -1 : o1[0] == o2[0] ? 0 : 1;
            };
        });
        LineNumberReader rd = new LineNumberReader(new InputStreamReader(inputStream));
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
        rd.close();

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
     * The result may contain the special value {@code #INHERITED}, which indicates that the
     * codepoint's script can only be determined by its context.
     *
     * Unknown codepoints are mapped to {@code #TAG_DEFAULT}.
     *
     * @param codePoint
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
