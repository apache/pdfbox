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
package org.apache.pdfbox.pdmodel.font.encoding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * PostScript glyph list, maps glyph names to sequences of Unicode characters.
 * Instances of GlyphList are immutable.
 */
public final class GlyphList
{
    private static final Log LOG = LogFactory.getLog(GlyphList.class);

    // Adobe Glyph List (AGL)
    private static final GlyphList DEFAULT = load("glyphlist.txt", 4281);
    
    // Zapf Dingbats has its own glyph list
    private static final GlyphList ZAPF_DINGBATS = load("zapfdingbats.txt",201);
    
    /**
     * Loads a glyph list from disk.
     */
    private static GlyphList load(String filename, int numberOfEntries)
    {
        ClassLoader loader = GlyphList.class.getClassLoader();
        String path = "org/apache/pdfbox/resources/glyphlist/";
        try
        {
            return new GlyphList(loader.getResourceAsStream(path + filename), numberOfEntries);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    static
    {
        // not supported in PDFBox 2.0, but we issue a warning, see PDFBOX-2379
        try
        {
            String location = System.getProperty("glyphlist_ext");
            if (location != null)
            {
                throw new UnsupportedOperationException("glyphlist_ext is no longer supported, "
                        + "use GlyphList.DEFAULT.addGlyphs(Properties) instead");
            }
        }
        catch (SecurityException e)  // can occur on System.getProperty
        {
            // PDFBOX-1946 ignore and continue
        }
    }
    
    /**
     * Returns the Adobe Glyph List (AGL).
     */
    public static GlyphList getAdobeGlyphList()
    {
        return DEFAULT;
    }

    /**
     * Returns the Zapf Dingbats glyph list.
     */
    public static GlyphList getZapfDingbats()
    {
        return ZAPF_DINGBATS;
    }

    // read-only mappings, never modified outside GlyphList's constructor
    private final Map<String, String> nameToUnicode;
    private final Map<String, String> unicodeToName;
    
    // additional read/write cache for uniXXXX names
    private final Map<String, String> uniNameToUnicodeCache = new HashMap<String, String>();

    /**
     * Creates a new GlyphList from a glyph list file.
     *
     * @param numberOfEntries number of expected values used to preallocate the correct amount of memory
     * @param input glyph list in Adobe format
     * @throws IOException if the glyph list could not be read
     */
    public GlyphList(InputStream input, int numberOfEntries) throws IOException
    {
        nameToUnicode = new HashMap<String, String>(numberOfEntries);
        unicodeToName = new HashMap<String, String>(numberOfEntries);
        loadList(input);
    }

    /**
     * Creates a new GlyphList from multiple glyph list files.
     *
     * @param glyphList an existing glyph list to be copied
     * @param input glyph list in Adobe format
     * @throws IOException if the glyph list could not be read
     */
    public GlyphList(GlyphList glyphList, InputStream input) throws IOException
    {
        nameToUnicode = new HashMap<String, String>(glyphList.nameToUnicode);
        unicodeToName = new HashMap<String, String>(glyphList.unicodeToName);
        loadList(input);
    }

    private void loadList(InputStream input) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(input, "ISO-8859-1"));
        try
        {
            while (in.ready())
            {
                String line = in.readLine();
                if (line != null && !line.startsWith("#"))
                {
                    String[] parts = line.split(";");
                    if (parts.length < 2)
                    {
                        throw new IOException("Invalid glyph list entry: " + line);
                    }

                    String name = parts[0];
                    String[] unicodeList = parts[1].split(" ");

                    if (nameToUnicode.containsKey(name))
                    {
                        LOG.warn("duplicate value for " + name + " -> " + parts[1] + " " +
                                 nameToUnicode.get(name));
                    }

                    int[] codePoints = new int[unicodeList.length];
                    int index = 0;
                    for (String hex : unicodeList)
                    {
                        codePoints[index++] = Integer.parseInt(hex, 16);
                    }
                    String string = new String(codePoints, 0 , codePoints.length);

                    // forward mapping
                    nameToUnicode.put(name, string);

                    // reverse mapping
                    if (!unicodeToName.containsKey(string))
                    {
                        unicodeToName.put(string, name);
                    }
                }
            }
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Returns the name for the given Unicode code point.
     *
     * @param codePoint Unicode code point
     * @return PostScript glyph name, or ".notdef"
     */
    public String codePointToName(int codePoint)
    {
        String name = unicodeToName.get(new String(new int[] { codePoint }, 0 , 1));
        if (name == null)
        {
            return ".notdef";
        }
        return name;
    }

    /**
     * Returns the name for a given sequence of Unicode characters.
     *
     * @param unicodeSequence sequence of Unicode characters
     * @return PostScript glyph name, or ".notdef"
     */
    public String sequenceToName(String unicodeSequence)
    {
        String name = unicodeToName.get(unicodeSequence);
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
    public String toUnicode(String name)
    {
        if (name == null)
        {
            return null;
        }

        String unicode = nameToUnicode.get(name);
        if (unicode != null)
        {
            return unicode;
        }
        
        // separate read/write cache for thread safety
        unicode = uniNameToUnicodeCache.get(name);
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
            uniNameToUnicodeCache.put(name, unicode);
        }
        return unicode;
    }
}
