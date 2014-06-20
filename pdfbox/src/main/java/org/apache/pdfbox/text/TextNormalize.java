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
package org.apache.pdfbox.text;

import java.text.Normalizer;
import java.util.HashMap;


/**
 * This class allows a caller to normalize text in various ways.
 * 
 * @author Brian Carrier
 */
public class TextNormalize
{
    private static final HashMap<Integer, String> DIACRITICS = createDiacritics();

    // Adds non-decomposing diacritics to the hash with their related combining character.
    // These are values that the unicode spec claims are equivalent but are not mapped in the form
    // NFKC normalization method. Determined by going through the Combining Diacritical Marks
    // section of the Unicode spec and identifying which characters are not  mapped to by the
    // normalization.
    private static HashMap<Integer, String> createDiacritics()
    {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        map.put(0x0060, "\u0300");
        map.put(0x02CB, "\u0300");
        map.put(0x0027, "\u0301");
        map.put(0x02B9, "\u0301");
        map.put(0x02CA, "\u0301");
        map.put(0x005e, "\u0302");
        map.put(0x02C6, "\u0302");
        map.put(0x007E, "\u0303");
        map.put(0x02C9, "\u0304");
        map.put(0x00B0, "\u030A");
        map.put(0x02BA, "\u030B");
        map.put(0x02C7, "\u030C");
        map.put(0x02C8, "\u030D");
        map.put(0x0022, "\u030E");
        map.put(0x02BB, "\u0312");
        map.put(0x02BC, "\u0313");
        map.put(0x0486, "\u0313");
        map.put(0x055A, "\u0313");
        map.put(0x02BD, "\u0314");
        map.put(0x0485, "\u0314");
        map.put(0x0559, "\u0314");
        map.put(0x02D4, "\u031D");
        map.put(0x02D5, "\u031E");
        map.put(0x02D6, "\u031F");
        map.put(0x02D7, "\u0320");
        map.put(0x02B2, "\u0321");
        map.put(0x02CC, "\u0329");
        map.put(0x02B7, "\u032B");
        map.put(0x02CD, "\u0331");
        map.put(0x005F, "\u0332");
        map.put(0x204E, "\u0359");
        return map;
    }

    private String outputEncoding;

    /**
     * 
     * @param encoding The Encoding that the text will eventually be written as (or null)
     */
    public TextNormalize(String encoding)
    {
        outputEncoding = encoding;
    }

    /**
     * Normalize the presentation forms of characters in the string. For example, convert the
     * single "fi" ligature to "f" and "i".
     * 
     * @param str String to normalize
     * @return Normalized string
     */
    public String normalizePresentationForm(String str)
    {
        StringBuilder builder = null;
        int p = 0;
        int q = 0;
        int strLength = str.length();
        for (; q < strLength; q++) 
        {
            // We only normalize if the codepoint is in a given range.
            // Otherwise, NFKC converts too many things that would cause
            // confusion. For example, it converts the micro symbol in
            // extended Latin to the value in the Greek script. We normalize
            // the Unicode Alphabetic and Arabic A&B Presentation forms.
            char c = str.charAt(q);
            if (0xFB00 <= c && c <= 0xFDFF || 0xFE70 <= c && c <= 0xFEFF)
            {
                if (builder == null) 
                {
                    builder = new StringBuilder(strLength * 2);
                }
                builder.append(str.substring(p, q));
                // Some fonts map U+FDF2 differently than the Unicode spec.
                // They add an extra U+0627 character to compensate.
                // This removes the extra character for those fonts. 
                if(c == 0xFDF2 && q > 0 && (str.charAt(q-1) == 0x0627 || str.charAt(q-1) == 0xFE8D))
                {
                    builder.append("\u0644\u0644\u0647");
                }
                else
                {
                    // Trim because some decompositions have an extra space, such as U+FC5E
                    builder.append(Normalizer.normalize(str.substring(q, q+1), Normalizer.Form.NFKC).trim());
                }
                p = q + 1;
            }
        }
        if (builder == null) 
        {
            return str;
        } 
        else 
        {
            builder.append(str.substring(p, q));
            return builder.toString();
        }
    }

    /**
     * Normalize the diacritic, for example, convert non-combining diacritic characters to their
     * combining counterparts.
     * 
     * @param str String to normalize
     * @return Normalized string
     */
    public String normalizeDiacritic(String str)
    {
        // Unicode contains special combining forms of the diacritic characters which we want to use
        if (outputEncoding != null && outputEncoding.toUpperCase().startsWith("UTF"))
        {
            Integer c = (int) str.charAt(0);
            // convert the characters not defined in the Unicode spec
            if (DIACRITICS.containsKey(c))
            {
                return DIACRITICS.get(c);
            }
            else
            {
                return Normalizer.normalize(str, Normalizer.Form.NFKC).trim();
            }
        }
        else
        {
            return str;
        }
    }

}
