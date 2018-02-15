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
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put("Adlam", new String[] { "adlm" });
        map.put("Ahom", new String[] { "ahom" });
        map.put("Anatolian_Hieroglyphs", new String[] { "hluw" });
        map.put("Arabic", new String[] { "arab" });
        map.put("Armenian", new String[] { "armn" });
        map.put("Avestan", new String[] { "avst" });
        map.put("Balinese", new String[] { "bali" });
        map.put("Bamum", new String[] { "bamu" });
        map.put("Bassa_Vah", new String[] { "bass" });
        map.put("Batak", new String[] { "batk" });
        map.put("Bengali", new String[] { "bng2", "beng" });
        map.put("Bhaiksuki", new String[] { "bhks" });
        map.put("Bopomofo", new String[] { "bopo" });
        map.put("Brahmi", new String[] { "brah" });
        map.put("Braille", new String[] { "brai" });
        map.put("Buginese", new String[] { "bugi" });
        map.put("Buhid", new String[] { "buhd" });
        // Byzantine Music: byzm
        map.put("Canadian_Aboriginal", new String[] { "cans" });
        map.put("Carian", new String[] { "cari" });
        map.put("Caucasian_Albanian", new String[] { "aghb" });
        map.put("Chakma", new String[] { "cakm" });
        map.put("Cham", new String[] { "cham" });
        map.put("Cherokee", new String[] { "cher" });
        map.put("Common", new String[] { TAG_DEFAULT }); // "Default" in OpenType
        map.put("Coptic", new String[] { "copt" });
        map.put("Cuneiform", new String[] { "xsux" }); // "Sumero-Akkadian Cuneiform" in OpenType
        map.put("Cypriot", new String[] { "cprt" });
        map.put("Cyrillic", new String[] { "cyrl" });
        map.put("Deseret", new String[] { "dsrt" });
        map.put("Devanagari", new String[] { "dev2", "deva" });
        map.put("Duployan", new String[] { "dupl" });
        map.put("Egyptian_Hieroglyphs", new String[] { "egyp" });
        map.put("Elbasan", new String[] { "elba" });
        map.put("Ethiopic", new String[] { "ethi" });
        map.put("Georgian", new String[] { "geor" });
        map.put("Glagolitic", new String[] { "glag" });
        map.put("Gothic", new String[] { "goth" });
        map.put("Grantha", new String[] { "gran" });
        map.put("Greek", new String[] { "grek" });
        map.put("Gujarati", new String[] { "gjr2", "gujr" });
        map.put("Gurmukhi", new String[] { "gur2", "guru" });
        map.put("Han", new String[] { "hani" }); // "CJK Ideographic" in OpenType
        map.put("Hangul", new String[] { "hang" });
        // Hangul Jamo: jamo
        map.put("Hanunoo", new String[] { "hano" });
        map.put("Hatran", new String[] { "hatr" });
        map.put("Hebrew", new String[] { "hebr" });
        map.put("Hiragana", new String[] { "kana" });
        map.put("Imperial_Aramaic", new String[] { "armi" });
        map.put(INHERITED, new String[] { INHERITED });
        map.put("Inscriptional_Pahlavi", new String[] { "phli" });
        map.put("Inscriptional_Parthian", new String[] { "prti" });
        map.put("Javanese", new String[] { "java" });
        map.put("Kaithi", new String[] { "kthi" });
        map.put("Kannada", new String[] { "knd2", "knda" });
        map.put("Katakana", new String[] { "kana" });
        map.put("Kayah_Li", new String[] { "kali" });
        map.put("Kharoshthi", new String[] { "khar" });
        map.put("Khmer", new String[] { "khmr" });
        map.put("Khojki", new String[] { "khoj" });
        map.put("Khudawadi", new String[] { "sind" });
        map.put("Lao", new String[] { "lao " });
        map.put("Latin", new String[] { "latn" });
        map.put("Lepcha", new String[] { "lepc" });
        map.put("Limbu", new String[] { "limb" });
        map.put("Linear_A", new String[] { "lina" });
        map.put("Linear_B", new String[] { "linb" });
        map.put("Lisu", new String[] { "lisu" });
        map.put("Lycian", new String[] { "lyci" });
        map.put("Lydian", new String[] { "lydi" });
        map.put("Mahajani", new String[] { "mahj" });
        map.put("Malayalam", new String[] { "mlm2", "mlym" });
        map.put("Mandaic", new String[] { "mand" });
        map.put("Manichaean", new String[] { "mani" });
        map.put("Marchen", new String[] { "marc" });
        // Mathematical Alphanumeric Symbols: math
        map.put("Meetei_Mayek", new String[] { "mtei" });
        map.put("Mende_Kikakui", new String[] { "mend" });
        map.put("Meroitic_Cursive", new String[] { "merc" });
        map.put("Meroitic_Hieroglyphs", new String[] { "mero" });
        map.put("Miao", new String[] { "plrd" });
        map.put("Modi", new String[] { "modi" });
        map.put("Mongolian", new String[] { "mong" });
        map.put("Mro", new String[] { "mroo" });
        map.put("Multani", new String[] { "mult" });
        // Musical Symbols: musc
        map.put("Myanmar", new String[] { "mym2", "mymr" });
        map.put("Nabataean", new String[] { "nbat" });
        map.put("Newa", new String[] { "newa" });
        map.put("New_Tai_Lue", new String[] { "talu" });
        map.put("Nko", new String[] { "nko " });
        map.put("Ogham", new String[] { "ogam" });
        map.put("Ol_Chiki", new String[] { "olck" });
        map.put("Old_Italic", new String[] { "ital" });
        map.put("Old_Hungarian", new String[] { "hung" });
        map.put("Old_North_Arabian", new String[] { "narb" });
        map.put("Old_Permic", new String[] { "perm" });
        map.put("Old_Persian", new String[] { "xpeo" });
        map.put("Old_South_Arabian", new String[] { "sarb" });
        map.put("Old_Turkic", new String[] { "orkh" });
        map.put("Oriya", new String[] { "ory2", "orya" }); // "Odia (formerly Oriya)" in OpenType
        map.put("Osage", new String[] { "osge" });
        map.put("Osmanya", new String[] { "osma" });
        map.put("Pahawh_Hmong", new String[] { "hmng" });
        map.put("Palmyrene", new String[] { "palm" });
        map.put("Pau_Cin_Hau", new String[] { "pauc" });
        map.put("Phags_Pa", new String[] { "phag" });
        map.put("Phoenician", new String[] { "phnx" });
        map.put("Psalter_Pahlavi", new String[] { "phlp" });
        map.put("Rejang", new String[] { "rjng" });
        map.put("Runic", new String[] { "runr" });
        map.put("Samaritan", new String[] { "samr" });
        map.put("Saurashtra", new String[] { "saur" });
        map.put("Sharada", new String[] { "shrd" });
        map.put("Shavian", new String[] { "shaw" });
        map.put("Siddham", new String[] { "sidd" });
        map.put("SignWriting", new String[] { "sgnw" });
        map.put("Sinhala", new String[] { "sinh" });
        map.put("Sora_Sompeng", new String[] { "sora" });
        map.put("Sundanese", new String[] { "sund" });
        map.put("Syloti_Nagri", new String[] { "sylo" });
        map.put("Syriac", new String[] { "syrc" });
        map.put("Tagalog", new String[] { "tglg" });
        map.put("Tagbanwa", new String[] { "tagb" });
        map.put("Tai_Le", new String[] { "tale" });
        map.put("Tai_Tham", new String[] { "lana" });
        map.put("Tai_Viet", new String[] { "tavt" });
        map.put("Takri", new String[] { "takr" });
        map.put("Tamil", new String[] { "tml2", "taml" });
        map.put("Tangut", new String[] { "tang" });
        map.put("Telugu", new String[] { "tel2", "telu" });
        map.put("Thaana", new String[] { "thaa" });
        map.put("Thai", new String[] { "thai" });
        map.put("Tibetan", new String[] { "tibt" });
        map.put("Tifinagh", new String[] { "tfng" });
        map.put("Tirhuta", new String[] { "tirh" });
        map.put("Ugaritic", new String[] { "ugar" });
        map.put(UNKNOWN, new String[] { TAG_DEFAULT });
        map.put("Vai", new String[] { "vai " });
        map.put("Warang_Citi", new String[] { "wara" });
        map.put("Yi", new String[] { "yi  " });
        UNICODE_SCRIPT_TO_OPENTYPE_TAG_MAP = map;
    }

    private static int[] unicodeRangeStarts;
    private static String[] unicodeRangeScripts;

    static
    {
        String path = "org/apache/fontbox/unicode/Scripts.txt";
        InputStream input = null;
        try
        {
            input = OpenTypeScript.class.getClassLoader().getResourceAsStream(path);
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
