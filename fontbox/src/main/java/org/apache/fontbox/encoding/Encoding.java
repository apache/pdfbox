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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an interface to a text encoder.
 *
 * @author Ben Litchfield
 * 
 */
public abstract class Encoding
{
    /**
     * The number of standard mac glyph names.
     */
    public static final int NUMBER_OF_MAC_GLYPHS = 258;
    
    /**
     * The 258 standard mac glyph names a used in 'post' format 1 and 2.
     */
    public static final String[] MAC_GLYPH_NAMES = new String[] 
    { 
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
    public static Map<String,Integer> MAC_GLYPH_NAMES_INDICES;
    
    static 
    {
        MAC_GLYPH_NAMES_INDICES = new HashMap<String,Integer>();
        for (int i=0;i<Encoding.NUMBER_OF_MAC_GLYPHS;++i) 
        {
            MAC_GLYPH_NAMES_INDICES.put(Encoding.MAC_GLYPH_NAMES[i],i);
        }
    }

    /**
     * Identifies a non-mapped character. 
     */
    private static final String NOTDEF = ".notdef";

    /**
     * This is a mapping from a character code to a character name.
     */
    protected Map<Integer,String> codeToName = new HashMap<Integer,String>();
    /**
     * This is a mapping from a character name to a character code.
     */
    protected Map<String,Integer> nameToCode = new HashMap<String,Integer>();

    private static final Map<String,String> NAME_TO_CHARACTER = new HashMap<String,String>();
    private static final Map<String,String> CHARACTER_TO_NAME = new HashMap<String,String>();

    /**
     * This will add a character encoding.
     *
     * @param code The character code that matches the character.
     * @param name The name of the character.
     */
    protected void addCharacterEncoding( int code, String name )
    {
        codeToName.put( code, name );
        nameToCode.put( name, code );
    }

    /**
     * This will get the character code for the name.
     *
     * @param name The name of the character.
     *
     * @return The code for the character.
     *
     * @throws IOException If there is no character code for the name.
     */
    public int getCode( String name ) throws IOException
    {
        Integer code = nameToCode.get( name );
        if( code == null )
        {
            throw new IOException( "No character code for character name '" + name + "'" );
        }
        return code.intValue();
    }

    /**
     * This will take a character code and get the name from the code.
     *
     * @param code The character code.
     *
     * @return The name of the character.
     */
    public String getName( int code )
    {
        String name = codeToName.get( code );
        if( name == null )
        {
            name = NOTDEF;
        }
        return name;
    }

    /**
     * This will take a character code and get the name from the code.
     *
     * @param c The character.
     *
     * @return The name of the character.
     *
     * @throws IOException If there is no name for the character.
     */
    public String getNameFromCharacter( char c ) throws IOException
    {
        String name = CHARACTER_TO_NAME.get( c );
        if( name == null )
        {
            throw new IOException( "No name for character '" + c + "'" );
        }
        return name;
    }

    /**
     * This will get the character from the code.
     *
     * @param code The character code.
     *
     * @return The printable character for the code.
     *
     * @throws IOException If there is not name for the character.
     */
    public String getCharacter( int code ) throws IOException
    {
        return getCharacter( getName( code ) );
    }

    /**
     * This will get the character from the name.
     *
     * @param name The name of the character.
     *
     * @return The printable character for the code.
     */
    public static String getCharacter( String name )
    {
        String character = NAME_TO_CHARACTER.get( name );
        if( character == null )
        {
            character = name;
        }
        return character;
    }
}