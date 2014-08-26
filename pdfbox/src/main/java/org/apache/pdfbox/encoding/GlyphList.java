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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.util.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * PostScript glyph list, maps glyph names to Unicode characters.
 */
public class GlyphList
{
    private static final Log LOG = LogFactory.getLog(GlyphList.class);

    private static final Map<String, String> NAME_TO_UNICODE = new HashMap<String, String>();
    private static final Map<String, String> UNICODE_TO_NAME = new HashMap<String, String>();

    static
    {
        // Loads the official glyph List based on adobes glyph list
        loadGlyphs("org/apache/pdfbox/resources/glyphlist.properties");

        // Loads some additional glyph mappings
        loadGlyphs("org/apache/pdfbox/resources/additional_glyphlist.properties");

        // Load an external glyph list file that user can give as JVM property
        try
        {
            String location = System.getProperty("glyphlist_ext");
            if (location != null)
            {
                File external = new File(location);
                if (external.exists())
                {
                    loadGlyphs(location);
                }
            }
        }
        catch (SecurityException e)  // can occur on System.getProperty
        {
            // PDFBOX-1946 ignore and continue
        }

        // todo: this is not desirable in many cases, should be done much later, e.g. TextStripper
        NAME_TO_UNICODE.put("fi", "fi");
        NAME_TO_UNICODE.put("fl", "fl");
        NAME_TO_UNICODE.put("ffi", "ffi");
        NAME_TO_UNICODE.put("ff", "ff");
        NAME_TO_UNICODE.put("pi", "pi");

        for (Map.Entry<String, String> entry : NAME_TO_UNICODE.entrySet())
        {
            UNICODE_TO_NAME.put(entry.getValue(), entry.getKey());
        }
    }

    private static void loadGlyphs(String path)
    {
        try
        {
            Properties glyphProperties = ResourceLoader.loadProperties(path, false);
            if (glyphProperties == null)
            {
                throw new MissingResourceException("Glyphlist not found: " + path,
                        Encoding.class.getName(), path);
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
                if (NAME_TO_UNICODE.containsKey(glyphName))
                {
                    LOG.warn("duplicate value for " + glyphName + " -> " + value);
                }
                else
                {
                    NAME_TO_UNICODE.put(glyphName, value.toString());
                }
            }
        }
        catch (IOException io)
        {
            LOG.error("error while reading the glyph property file.", io);
        }
    }

    /**
     * This will take a character code and get the name from the code.
     *
     * @param c Unicode character
     * @return PostScript glyph name, or ".notdef"
     */
    public static String unicodeToName(char c)
    {
        String name = UNICODE_TO_NAME.get(Character.toString(c));
        if (name == null)
        {
            return ".notdef";
        }
        return name;
    }

    /**
     * Returns the Unicode character sequence for the given glyph name, or null if there isn't any.
     *
     * @param name PostScript glyph name
     * @return Unicode character(s), or null.
     */
    public static String toUnicode(String name)
    {
        String unicode = NAME_TO_UNICODE.get(name);
        if (unicode == null)
        {
            // test if we have a suffix and if so remove it
            if (name.indexOf('.') > 0)
            {
                unicode = toUnicode(name.substring(0, name.indexOf('.')));
            }
            else if (name.startsWith("uni") && name.length() == 7)
            {
                // test for Unicode name in the format uniXXXX where X is hex
                int nameLength = name.length();
                StringBuilder uniStr = new StringBuilder();
                try
                {
                    for (int chPos = 3; chPos + 4 <= nameLength; chPos += 4)
                    {
                        int codePoint = Integer.parseInt(name.substring(chPos, chPos + 4), 16);
                        if (codePoint > 0xD7FF && codePoint < 0xE000)
                        {
                            LOG.warn("Unicode character name with disallowed code area: " + name);
                        }
                        else
                        {
                            uniStr.append((char) codePoint);
                        }
                    }
                    unicode = uniStr.toString();
                }
                catch (NumberFormatException nfe)
                {
                    LOG.warn("Not a number in Unicode character name: " + name);
                }
            }
            else if (name.startsWith("u") && name.length() == 5)
            {
                // test for an alternate Unicode name representation uXXXX
                try
                {
                    int codePoint = Integer.parseInt(name.substring(1), 16);
                    if (codePoint > 0xD7FF && codePoint < 0xE000)
                    {
                        LOG.warn("Unicode character name with disallowed code area: " + name);
                    }
                    else
                    {
                        unicode = String.valueOf((char) codePoint);
                    }
                }
                catch (NumberFormatException nfe)
                {
                    LOG.warn("Not a number in Unicode character name: " + name);
                }
            }
            NAME_TO_UNICODE.put(name, unicode);
        }
        return unicode;
    }
}
