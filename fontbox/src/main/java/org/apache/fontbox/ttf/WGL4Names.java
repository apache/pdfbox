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

import java.util.HashMap;
import java.util.Map;

/**
 * Windows Glyph List 4 (WGL4) names for Mac glyphs.
 */
public final class WGL4Names
{
    /**
     * The number of standard mac glyph names.
     */
    public static final int NUMBER_OF_MAC_GLYPHS = 258;

    /**
     * The 258 standard mac glyph names a used in 'post' format 1 and 2.
     */
    private static final String[] MAC_GLYPH_NAMES = {
        ".notdef",".null", "nonmarkingreturn", "space", "exclam", "quotedbl",
        "numbersign", "dollar", "percent", "ampersand", "quotesingle",
        "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen",
        "period", "slash", "zero", "one", "two", "three", "four", "five",
        "six", "seven", "eight", "nine", "colon", "semicolon", "less",
        "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F",
        "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
        "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash",
        "bracketright", "asciicircum", "underscore", "grave", "a", "b",
        "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
        "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft",
        "bar", "braceright", "asciitilde", "Adieresis", "Aring",
        "Ccedilla", "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute",
        "agrave", "acircumflex", "adieresis", "atilde", "aring",
        "ccedilla", "eacute", "egrave", "ecircumflex", "edieresis",
        "iacute", "igrave", "icircumflex", "idieresis", "ntilde", "oacute",
        "ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave",
        "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling",
        "section", "bullet", "paragraph", "germandbls", "registered",
        "copyright", "trademark", "acute", "dieresis", "notequal", "AE",
        "Oslash", "infinity", "plusminus", "lessequal", "greaterequal",
        "yen", "mu", "partialdiff", "summation", "product", "pi",
        "integral", "ordfeminine", "ordmasculine", "Omega", "ae", "oslash",
        "questiondown", "exclamdown", "logicalnot", "radical", "florin",
        "approxequal", "Delta", "guillemotleft", "guillemotright",
        "ellipsis", "nonbreakingspace", "Agrave", "Atilde", "Otilde", "OE",
        "oe", "endash", "emdash", "quotedblleft", "quotedblright",
        "quoteleft", "quoteright", "divide", "lozenge", "ydieresis",
        "Ydieresis", "fraction", "currency", "guilsinglleft",
        "guilsinglright", "fi", "fl", "daggerdbl", "periodcentered",
        "quotesinglbase", "quotedblbase", "perthousand", "Acircumflex",
        "Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute",
        "Icircumflex", "Idieresis", "Igrave", "Oacute", "Ocircumflex",
        "apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi",
        "circumflex", "tilde", "macron", "breve", "dotaccent", "ring",
        "cedilla", "hungarumlaut", "ogonek", "caron", "Lslash", "lslash",
        "Scaron", "scaron", "Zcaron", "zcaron", "brokenbar", "Eth", "eth",
        "Yacute", "yacute", "Thorn", "thorn", "minus", "multiply",
        "onesuperior", "twosuperior", "threesuperior", "onehalf",
        "onequarter", "threequarters", "franc", "Gbreve", "gbreve",
        "Idotaccent", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron",
        "ccaron", "dcroat"
    };

    /**
     * The indices of the standard mac glyph names.
     */
    private static final Map<String, Integer> MAC_GLYPH_NAMES_INDICES;

    static
    {
        MAC_GLYPH_NAMES_INDICES = new HashMap<>(NUMBER_OF_MAC_GLYPHS);
        for (int i = 0; i < NUMBER_OF_MAC_GLYPHS; ++i)
        {
            MAC_GLYPH_NAMES_INDICES.put(MAC_GLYPH_NAMES[i],i);
        }
    }
    
    private WGL4Names()
    {
    }

    /**
     * Returns the index of the glyph with the given name.
     * 
     * @param name the name of the glyph
     * @return the index of the given glyph name or null for an invalid glyph name
     */
    public static Integer getGlyphIndex(String name)
    {
        return MAC_GLYPH_NAMES_INDICES.get(name);
    }

    /**
     * Returns the name of the glyph at the given index.
     * 
     * @param index the index of the glyph
     * @return the name of the glyph at the given index or null fo an invalid glyph index
     */
    public static String getGlyphName(int index)
    {
        return index >= 0 && index < NUMBER_OF_MAC_GLYPHS ? MAC_GLYPH_NAMES[index] : null;
    }

    /**
     * Returns a new array with all glyph names.
     * 
     * @return the array with all glyph names
     */
    public static String[] getAllNames()
    {
        String[] glyphNames = new String[NUMBER_OF_MAC_GLYPHS];
        System.arraycopy(MAC_GLYPH_NAMES, 0, glyphNames, 0, NUMBER_OF_MAC_GLYPHS);
        return glyphNames;
    }
}
