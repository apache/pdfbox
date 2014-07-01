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
package org.apache.fontbox.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.util.autodetect.FontFileFinder;

/**
 * This class is used as manager for local fonts. It's based on the font manager provided by Apache FOP. see
 * org.apache.fop.fonts.FontManager.java
 */

public class FontManager
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(FontManager.class);

    private static HashMap<String, String> ttfFontfiles = new HashMap<String, String>();

    private static boolean fontsLoaded = false;

    // HashMap with all known true type fonts
    private static HashMap<String, String> fontMappingTTF = new HashMap<String, String>();

    // fallback font
    private static TrueTypeFont standardFont;

    private FontManager()
    {
    }

    /**
     * Load all available fonts from the environment.
     */
    private static void loadFonts()
    {
        try
        {
            FontFileFinder fontfinder = new FontFileFinder();
            List<URI> fonts = fontfinder.find();
            for (URI font : fonts)
            {
                try
                {
                    // the URL may contain some escaped characters like spaces
                    // use the URI to decode such escape sequences
                    String fontfilename = new File(font).getPath();
                    if (fontfilename.toLowerCase().endsWith(".ttf"))
                    {
                        analyzeTTF(fontfilename);
                    }
                    else
                    {
                        LOG.debug("Unsupported font format for external font: " + fontfilename);
                    }
                }
                catch (IOException exception)
                {
                    LOG.debug("Can't read external font: " + font.getPath(), exception);
                }
            }
            addFontMapping(fontfinder.getCommonTTFMapping(), fontMappingTTF);
            createFontmapping();
        }
        catch (IOException exception)
        {
            LOG.error("An error occured when collecting external fonts.", exception);
        }
        finally
        {
            fontsLoaded = true;
        }
    }

    /**
     * Analyze the given true type font.
     * 
     * @param ttfFilename the filename of the true type type
     * @throws IOException if something went wrong
     */
    private static void analyzeTTF(String ttfFilename) throws IOException
    {
        TTFParser ttfParser = new TTFParser(false,true);
        TrueTypeFont ttfFont = ttfParser.parseTTF(ttfFilename);
        if (ttfFont != null)
        {
            NamingTable namingTable = ttfFont.getNaming();
            if (namingTable != null && namingTable.getPSName() != null)
            {
                String normalizedName = normalizeFontname(namingTable.getPSName());
                if (!ttfFontfiles.containsKey(normalizedName))
                {
                    LOG.debug("Added font mapping "+normalizedName + " -=> "+ttfFilename);
                    ttfFontfiles.put(normalizedName, ttfFilename);
                }
            }
            // take the font family name into account
            if (namingTable != null && namingTable.getFontFamily() != null)
            {
                String normalizedName = normalizeFontFamily(namingTable.getFontFamily(), namingTable.getPSName());
                if (!ttfFontfiles.containsKey(normalizedName))
                {
                    LOG.debug("Added font mapping "+normalizedName + " -=> "+ttfFilename);
                    ttfFontfiles.put(normalizedName, ttfFilename);
                }
            }
        }   
    }

    /**
     * Normalize the fontname.
     * 
     * @param fontname The name of the font.
     * 
     * @return The normalized name of the font.
     */
    private static String normalizeFontname(String fontname)
    {
        // Terminate all whitespaces, commas and hyphens
        String normalizedFontname = fontname.toLowerCase().replaceAll(" ", "").replaceAll(",", "")
                .replaceAll("-", "");
        // Terminate trailing characters up to the "+".
        // As far as I know, these characters are used in names of embedded fonts
        // If the embedded font can't be read, we'll try to find it here
        if (normalizedFontname.indexOf('+') > -1)
        {
            normalizedFontname = normalizedFontname.substring(normalizedFontname.indexOf('+') + 1);
        }
        // normalize all kinds of fonttypes. There are several possible version which have to be normalized
        // e.g. Arial,Bold Arial-BoldMT Helevtica-oblique ...
        boolean isBold = normalizedFontname.indexOf("bold") > -1;
        boolean isItalic = normalizedFontname.indexOf("italic") > -1 || normalizedFontname.indexOf("oblique") > -1;
        normalizedFontname = normalizedFontname.replaceAll("bold", "")
                .replaceAll("italic", "").replaceAll("oblique", "");
        if (isBold)
        {
            normalizedFontname += "bold";
        }
        if (isItalic)
        {
            normalizedFontname += "italic";
        }
        return normalizedFontname;
    }

    private static String normalizeFontFamily(String fontFamily, String psFontName)
    {
        String normalizedFontFamily=fontFamily.toLowerCase().replaceAll(" ", "").replaceAll(",", "").replaceAll("-", "");
        if (psFontName!=null) 
        {
            psFontName=psFontName.toLowerCase();
               
            boolean isBold = psFontName.indexOf("bold") > -1;
            boolean isItalic = psFontName.indexOf("italic") > -1 || psFontName.indexOf("oblique") > -1;
            
            if (isBold)
            {
                normalizedFontFamily += "bold";
            }
            if (isItalic)
            {
                normalizedFontFamily += "italic";
            }
        }
        return normalizedFontFamily;
    }
     
    /**
     * Add a font-mapping.
     * 
     * @param font The name of the font.
     * 
     * @param mappedName The name of the mapped font.
     */
    private static void addFontMapping(String font, String mappedName, Map<String, String> mapping)
    {
        String fontname = normalizeFontname(font);
        // is there already a font mapping ?
        if (mapping.containsKey(fontname))
        {
            return;
        }
        String mappedFontname = normalizeFontname(mappedName);
        // is there any font with the mapped name ?
        if (ttfFontfiles.containsKey(mappedFontname))
        {
            mapping.put(fontname, mappedFontname);
        }
        else
        {
            // is there any recursive mapping ?
            if (mapping.containsKey(mappedFontname))
            {
                mapping.put(fontname, mapping.get(mappedFontname));
            }
        }
    }

    /**
     * Add the given mappings to the font mapping.
     * 
     * @param fontMappingSrc the given mapping
     */
    private static void addFontMapping(Map<String, String> fontMappingSrc,
            Map<String, String> fontMappingDest)
    {
        for (String fontname : fontMappingSrc.keySet())
        {
            addFontMapping(fontname, fontMappingSrc.get(fontname), fontMappingDest);
        }
    }

    /**
     * Search for a mapped true type font name.
     * 
     * @param fontname the given font name
     * @return the mapped font name
     */
    private static String getMappedTTFName(String fontname)
    {
        String normalizedFontname = normalizeFontname(fontname);
        if (fontMappingTTF.containsKey(normalizedFontname))
        {
            return fontMappingTTF.get(normalizedFontname);
        }
        return null;
    }

    /**
     * Create a mapping for the some font families.
     */
    private static void createFontmapping()
    {
        addFontFamilyMapping("ArialNarrow", "Arial", fontMappingTTF);
        addFontFamilyMapping("ArialMT", "Arial", fontMappingTTF);
        addFontFamilyMapping("CourierNew", "Courier", fontMappingTTF);
        addFontFamilyMapping("TimesNewRomanPSMT", "TimesNewRoman", fontMappingTTF);
    }

    /**
     * Create a mapping for the given font family.
     * 
     * @param fontfamily the font family to be mapped
     * @param mappedFontfamily the mapped font family
     */
    private static void addFontFamilyMapping(String fontfamily, String mappedFontfamily,
            Map<String, String> mapping)
    {
        addFontMapping(fontfamily + ",BoldItalic", mappedFontfamily + ",BoldItalic", mapping);
        addFontMapping(fontfamily + ",Bold", mappedFontfamily + ",Bold", mapping);
        addFontMapping(fontfamily + ",Italic", mappedFontfamily + ",Italic", mapping);
        addFontMapping(fontfamily, mappedFontfamily, mapping);
    }

    /**
     * Search for a font for the given font name.
     * 
     * @param fontname the given font name
     * @return the name of the mapped font
     */
    public static String findTTFontname(String fontname)
    {
        if (!fontsLoaded)
        {
            loadFonts();
        }
        String fontfile = null;
        String normalizedFontname = normalizeFontname(fontname);
        if (ttfFontfiles.containsKey(normalizedFontname))
        {
            fontfile = ttfFontfiles.get(normalizedFontname);
        }
        if (fontfile == null)
        {
            String mappedFontname = getMappedTTFName(fontname);
            if (mappedFontname != null && ttfFontfiles.containsKey(mappedFontname))
            {
                fontfile = ttfFontfiles.get(mappedFontname);
            }
        }
        if (fontfile != null)
        {
            LOG.debug("Using ttf mapping "+fontname + " -=> "+fontfile);
        }
        else
        {
            LOG.warn("Font not found: " + fontname);
        }
        return fontfile;
    }

    /**
     * Search for a true type font for the given font name.
     * 
     * @param fontname the given font name
     * @return the mapped true type font, or null if none could be found
     * @throws IOException if something went wrong
     */
    public static TrueTypeFont findTTFont(String fontname) throws IOException
    {
        String ttfFontName = findTTFontname(fontname);
        TrueTypeFont ttfFont = null;
        if (ttfFontName != null)
        {
            TTFParser ttfParser = new TTFParser();
            InputStream fontStream = ResourceLoader.loadResource(ttfFontName);
            if (fontStream == null)
            {
                throw new IOException("Can't load external font: " + ttfFontName);
            }
            ttfFont = ttfParser.parseTTF(fontStream);
        }
        return ttfFont;
    }

    /**
     * Get the standard font from the environment.
     *
     * @return standard font
     */
    public static TrueTypeFont getStandardFont() throws IOException
    {
        if (standardFont == null)
        {
            // todo: make this configurable

            // Windows
            standardFont = findTTFont("Arial");

            if (standardFont == null)
            {
                // OS X
                standardFont = findTTFont("Helvetica");
            }

            if (standardFont == null)
            {
                // Linux
                standardFont = findTTFont("Liberation Sans");
            }

            if (standardFont == null)
            {
                throw new IOException("Could not load TTF fallback font");
            }
        }
        return standardFont;
    }
}
