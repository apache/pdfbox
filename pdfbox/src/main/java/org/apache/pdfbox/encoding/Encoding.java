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
package org.apache.pdfbox.encoding;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is an interface to a text encoder.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public abstract class Encoding implements COSObjectable
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(Encoding.class);

    /** Identifies a non-mapped character. */
    public static final String NOTDEF = ".notdef";

    /**
     * This is a mapping from a character code to a character name.
     */
    protected final Map<Integer, String> codeToName = new HashMap<Integer, String>();

    /**
     * This is a mapping from a character name to a character code.
     */
    protected final Map<String, Integer> nameToCode = new HashMap<String, Integer>();

    private static final Map<String, String> NAME_TO_CHARACTER = new HashMap<String, String>();

    private static final Map<String, String> CHARACTER_TO_NAME = new HashMap<String, String>();

    static
    {
        // Loads the official glyph List based on adobes glyph list
        loadGlyphProperties("org/apache/pdfbox/resources/glyphlist.properties");
        // Loads some additional glyph mappings
        loadGlyphProperties("org/apache/pdfbox/resources/additional_glyphlist.properties");

        // Load an external glyph list file that user can give as JVM property
        String location = System.getProperty("glyphlist_ext");
        if (location != null)
        {
            File external = new File(location);
            if (external.exists())
            {
                loadGlyphProperties(location);
            }
        }

        NAME_TO_CHARACTER.put(NOTDEF, "");
        NAME_TO_CHARACTER.put("fi", "fi");
        NAME_TO_CHARACTER.put("fl", "fl");
        NAME_TO_CHARACTER.put("ffi", "ffi");
        NAME_TO_CHARACTER.put("ff", "ff");
        NAME_TO_CHARACTER.put("pi", "pi");

        for (Map.Entry<String, String> entry : NAME_TO_CHARACTER.entrySet())
        {
            CHARACTER_TO_NAME.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Loads a glyph list from a given location and populates the NAME_TO_CHARACTER hashmap for character lookups.
     * 
     * @param location - The string location of the glyphlist file
     */
    private static void loadGlyphProperties(String location)
    {
        try
        {
            Properties glyphProperties = ResourceLoader.loadProperties(location, false);
            if (glyphProperties == null)
            {
                throw new MissingResourceException("Glyphlist not found: " + location, Encoding.class.getName(),
                        location);
            }
            Enumeration<?> names = glyphProperties.propertyNames();
            for (Object name : Collections.list(names))
            {
                String glyphName = name.toString();
                String unicodeValue = glyphProperties.getProperty(glyphName);
                StringTokenizer tokenizer = new StringTokenizer(unicodeValue, " ", false);
                StringBuilder value = new StringBuilder();
                while (tokenizer.hasMoreTokens())
                {
                    int characterCode = Integer.parseInt(tokenizer.nextToken(), 16);
                    value.append((char) characterCode);
                }
                if (NAME_TO_CHARACTER.containsKey(glyphName))
                {
                    LOG.warn("duplicate value for characterName=" + glyphName + "," + value);
                }
                else
                {
                    NAME_TO_CHARACTER.put(glyphName, value.toString());
                }
            }
        }
        catch (IOException io)
        {
            LOG.error("error while reading the glyph property file.", io);
        }
    }

    /**
     * Returns an unmodifiable view of the Code2Name mapping.
     * 
     * @return the Code2Name map
     */
    public Map<Integer, String> getCodeToNameMap()
    {
        return Collections.unmodifiableMap(codeToName);
    }

    /**
     * Returns an unmodifiable view of the Name2Code mapping.
     * 
     * @return the Name2Code map
     */
    public Map<String, Integer> getNameToCodeMap()
    {
        return Collections.unmodifiableMap(nameToCode);
    }

    /**
     * This will add a character encoding.
     * 
     * @param code The character code that matches the character.
     * @param name The name of the character.
     */
    public void addCharacterEncoding(int code, String name)
    {
        codeToName.put(code, name);
        nameToCode.put(name, code);
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
    public int getCode(String name) throws IOException
    {
        Integer code = nameToCode.get(name);
        if (code == null)
        {
            throw new IOException("No character code for character name '" + name + "'");
        }
        return code;
    }

    /**
     * This will take a character code and get the name from the code.
     * 
     * @param code The character code.
     * 
     * @return The name of the character.
     * 
     * @throws IOException If there is no name for the code.
     */
    public String getName(int code) throws IOException
    {
        return codeToName.get(code);
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
    public String getNameForCharacter(char c) throws IOException
    {
        String name = CHARACTER_TO_NAME.get(Character.toString(c));
        if (name == null)
        {
            throw new IOException("No name for character '" + c + "'");
        }
        return name;
    }

    /**
     * This will take a name and get the character code for that name.
     * 
     * @param name The name.
     * 
     * @return The name of the character.
     * 
     */
    public static String getCharacterForName(String name)
    {
        if (NAME_TO_CHARACTER.containsKey(name))
        {
            LOG.debug("No character for name " + name);
            return NAME_TO_CHARACTER.get(name);
        }
        return null;
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
    public String getCharacter(int code) throws IOException
    {
        String name = getName(code);
        if (name != null)
        {
            return getCharacter(getName(code));
        }
        return null;
    }

    /**
     * This will get the character from the name.
     * 
     * @param name The name of the character.
     * 
     * @return The printable character for the code.
     */
    public String getCharacter(String name)
    {
        String character = NAME_TO_CHARACTER.get(name);
        if (character == null)
        {
            // test if we have a suffix and if so remove it
            if (name.indexOf('.') > 0)
            {
                character = getCharacter(name.substring(0, name.indexOf('.')));
            }
            // test for Unicode name
            // (uniXXXX - XXXX must be a multiple of four;
            // each representing a hexadecimal Unicode code point)
            else if (name.startsWith("uni"))
            {
                int nameLength = name.length();
                StringBuilder uniStr = new StringBuilder();
                try
                {
                    for (int chPos = 3; chPos + 4 <= nameLength; chPos += 4)
                    {
                        int characterCode = Integer.parseInt(name.substring(chPos, chPos + 4), 16);

                        if (characterCode > 0xD7FF && characterCode < 0xE000)
                        {
                            LOG.warn("Unicode character name with not allowed code area: " + name);
                        }
                        else
                        {
                            uniStr.append((char) characterCode);
                        }
                    }
                    character = uniStr.toString();
                    NAME_TO_CHARACTER.put(name, character);
                }
                catch (NumberFormatException nfe)
                {
                    LOG.warn("Not a number in Unicode character name: " + name);
                    character = name;
                }
            }
            // test for an alternate Unicode name representation
            else if (name.startsWith("u"))
            {
                try
                {
                    int characterCode = Integer.parseInt(name.substring(1), 16);
                    if (characterCode > 0xD7FF && characterCode < 0xE000)
                    {
                        LOG.warn("Unicode character name with not allowed code area: " + name);
                    }
                    else
                    {
                        character = String.valueOf((char) characterCode);
                        NAME_TO_CHARACTER.put(name, character);
                    }
                }
                catch (NumberFormatException nfe)
                {
                    LOG.warn("Not a number in Unicode character name: " + name);
                    character = name;
                }
            }
            else if (nameToCode.containsKey(name))
            {
                int code = nameToCode.get(name);
                character = Character.toString((char) code);
            }
            else
            {
                character = name;
            }
        }
        return character;
    }

}
