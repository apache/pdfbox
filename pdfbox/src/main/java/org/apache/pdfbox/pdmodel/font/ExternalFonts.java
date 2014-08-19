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
package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.ttf.Type1Equivalent;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;

/**
 * External font service, locates non-embedded fonts via a pluggable FontProvider.
 *
 * @author John Hewson
 */
public final class ExternalFonts
{
    private ExternalFonts() {}

    private static final Log log = LogFactory.getLog(ExternalFonts.class);
    private static FontProvider fontProvider;

    /**
     * Sets the font service provider.
     */
    public static void setProvider(FontProvider fontProvider)
    {
        ExternalFonts.fontProvider = fontProvider;
    }

    /**
     * Gets the font service provider. Defaults to using FileSystemFontProvider.
     */
    private static FontProvider getProvider()
    {
        if (fontProvider == null)
        {
            fontProvider = new FileSystemFontProvider();
        }
        return fontProvider;
    }

    // todo: we could just rely on the system to provide Times rather than ship our own
    /** Fallback font, used as as a last resort */
    private static TrueTypeFont fallbackFont;
    static
    {
        try
        {
            String name = "org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";
            TTFParser ttfParser = new TTFParser();
            InputStream fontStream = org.apache.fontbox.util.ResourceLoader.loadResource(name);
            if (fontStream == null)
            {
                throw new IOException("Error loading resource: " + name);
            }
            fallbackFont = ttfParser.parseTTF(fontStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /** Map of PostScript name substitutes, in priority order. */
    private final static Map<String, List<String>> substitutes = new HashMap<String, List<String>>();
    static
    {
        // substitutes for standard 14 fonts
        substitutes.put("Courier",
                Arrays.asList("CourierNew", "CourierNewPSMT"));
        substitutes.put("Courier-Bold",
                Arrays.asList("CourierNewPS-BoldMT", "CourierNew-Bold"));
        substitutes.put("Courier-Oblique",
                Arrays.asList("CourierNewPS-ItalicMT","CourierNew-Italic"));
        substitutes.put("Helvetica",
                Arrays.asList("ArialMT", "Arial"));
        substitutes.put("Helvetica-Bold",
                Arrays.asList("Arial-BoldMT", "Arial-Bold"));
        substitutes.put("Helvetica-Oblique",
                Arrays.asList("Arial-ItalicMT", "Arial-Italic", "Helvetica-Italic"));
        substitutes.put("Helvetica-BoldOblique",
                Arrays.asList("Helvetica-BoldItalic"));
        substitutes.put("Times-Roman",
                Arrays.asList("TimesNewRomanPSMT", "TimesNewRoman", "TimesNewRomanPS"));
        substitutes.put("Times-Bold",
                Arrays.asList("TimesNewRomanPS-BoldMT", "TimesNewRomanPS-Bold",
                              "TimesNewRoman-Bold"));
        substitutes.put("Times-Italic",
                Arrays.asList("TimesNewRomanPS-ItalicMT", "TimesNewRomanPS-Italic",
                              "TimesNewRoman-Italic"));
        substitutes.put("Times-BoldItalic",
                Arrays.asList("TimesNewRomanPS-BoldItalicMT", "TimesNewRomanPS-BoldItalic",
                             "TimesNewRoman-BoldItalic"));
        substitutes.put("Symbol",Arrays.asList("SymbolMT"));
        substitutes.put("ZapfDingbats", Arrays.asList("ZapfDingbatsITC"));

        // the Adobe Supplement to the ISO 32000 specifies some alternative names for some
        // of the standard 14 fonts, so we map these to our fallbacks above
        substitutes.put("CourierCourierNew", substitutes.get("Courier"));
        substitutes.put("CourierNew", substitutes.get("Courier"));
        substitutes.put("CourierNew,Italic", substitutes.get("Courier-Oblique"));
        substitutes.put("CourierNew,Bold", substitutes.get("Courier-Bold"));
        substitutes.put("CourierNew,BoldItalic", substitutes.get("Courier-BoldOblique"));
        substitutes.put("Arial", substitutes.get("Helvetica"));
        substitutes.put("Arial,Italic", substitutes.get("Helvetica-Oblique"));
        substitutes.put("Arial,Bold", substitutes.get("Helvetica-Bold"));
        substitutes.put("Arial,BoldItalic", substitutes.get("Helvetica-BoldOblique"));
        substitutes.put("TimesNewRoman", substitutes.get("Times-Roman"));
        substitutes.put("TimesNewRoman,Italic", substitutes.get("Times-Italic"));
        substitutes.put("TimesNewRoman,Bold", substitutes.get("Times-Bold"));
        substitutes.put("TimesNewRoman,BoldItalic", substitutes.get("Times-BoldItalic"));
    }

    /**
     * Adds a top-priority substitute for the given font.
     *
     * @param match PostScript name of the font to match
     * @param replace PostScript name of the font to use as a replacement
     */
    public static void addSubstitute(String match, String replace)
    {
        if (!substitutes.containsKey(match))
        {
            substitutes.put(match, new ArrayList<String>());
        }
        substitutes.get(match).add(replace);
    }

    /**
     * Returns the substitutes for a given font.
     */
    private static List<String> getSubstitutes(String postScriptName)
    {
        List<String> subs = substitutes.get(postScriptName);
        if (subs != null)
        {
            return subs;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    /**
     * Windows name (ArialNarrow,Bold) to PostScript name (ArialNarrow-Bold)
     */
    private static String windowsToPs(String windowsName)
    {
        return windowsName.replace(",", "-");
    }

    /**
     * Returns the fallback font, used for rendering when no other fonts are available.
     */
    public static TrueTypeFont getFallbackFont()
    {
        // todo: add FontDescriptor to the parameters for this method for "smart fallback" to
        //       standard 14 fonts via the FontProvider
        return fallbackFont;
    }

    /**
     * Finds a TrueType font with the given PostScript name, or a suitable substitute, or null.
     *
     * @param postScriptName PostScript font name
     */
    public static TrueTypeFont getTrueTypeFont(String postScriptName)
    {
        // first ask the font provider for the font
        TrueTypeFont ttf = getProvider().getTrueTypeFont(postScriptName);
        if (ttf == null)
        {
            // then try substitutes
            for (String substituteName : getSubstitutes(postScriptName))
            {
                ttf = getProvider().getTrueTypeFont(substituteName);
                if (ttf != null)
                {
                    return ttf;
                }
            }
            // then Windows name
            ttf = getProvider().getTrueTypeFont(windowsToPs(postScriptName));
        }
        return ttf;
    }

    /**
     * Finds a TrueType font with the given PostScript name, or a suitable substitute, or null.
     *
     * @param postScriptName PostScript font name
     */
    public static Type1Font getType1Font(String postScriptName)
    {
        // first ask the font provider for the font
        Type1Font t1 = getProvider().getType1Font(postScriptName);
        if (t1 == null)
        {
            // then try substitutes
            for (String substituteName : getSubstitutes(postScriptName))
            {
                t1 = getProvider().getType1Font(substituteName);
                if (t1 != null)
                {
                    return t1;
                }
            }
            // then Windows name
            t1 = getProvider().getType1Font(windowsToPs(postScriptName));
        }
        return t1;
    }

    /**
     * Finds a CFF Type 1 font with the given PostScript name, or a suitable substitute, or null.
     *
     * @param postScriptName PostScript font name
     */
    public static CFFType1Font getCFFType1Font(String postScriptName)
    {
        CFFFont cff = getCFFFont(postScriptName);
        if (cff instanceof CFFType1Font)
        {
            return (CFFType1Font)cff;
        }
        return null;
    }

    /**
     * Finds a CFF CID-Keyed font with the given PostScript name, or a suitable substitute, or null.
     *
     * @param postScriptName PostScript font name
     */
    public static CFFCIDFont getCFFCIDFont(String postScriptName)
    {
        CFFFont cff = getCFFFont(postScriptName);
        if (cff instanceof CFFCIDFont)
        {
            return (CFFCIDFont)cff;
        }
        return null;
    }

    /**
     * Finds a CFF font with the given PostScript name, or a suitable substitute, or null.
     *
     * @param postScriptName PostScript font name
     */
    private static CFFFont getCFFFont(String postScriptName)
    {
        // first ask the font provider for the font
        CFFFont cff = getProvider().getCFFFont(postScriptName);
        if (cff == null)
        {
            // then try substitutes
            for (String substituteName : getSubstitutes(postScriptName))
            {
                cff = getProvider().getCFFFont(substituteName);
                if (cff != null)
                {
                    return cff;
                }
            }
            // then Windows name
            cff = getProvider().getCFFFont(windowsToPs(postScriptName));
        }
        return cff;
    }

    /**
     * Finds a Type 1-equivalent font with the given PostScript name, or a suitable substitute,
     * or null. This allows a Type 1 font to be substituted with a PFB, TTF or OTF.
     *
     * @param postScriptName PostScript font name
     */
    public static Type1Equivalent getType1EquivalentFont(String postScriptName)
    {
        Type1Font t1 = getType1Font(postScriptName);
        if (t1 != null)
        {
            return t1;
        }

        CFFType1Font cff = getCFFType1Font(postScriptName);
        if (cff != null)
        {
            return cff;
        }

        TrueTypeFont ttf = getTrueTypeFont(postScriptName);
        if (ttf != null)
        {
            return ttf;
        }

        return null;
    }
}
