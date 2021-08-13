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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetrics;

/**
 * The "Standard 14" PDF fonts, also known as the "base 14" fonts.
 * There are 14 font files, but Acrobat uses additional names for compatibility, e.g. Arial.
 *
 * @author John Hewson
 */
public final class Standard14Fonts
{
    /**
     * Contains all base names and alias names for the known fonts.
     * For base fonts both the key and the value will be the base name.
     * For aliases, the key is an alias, and the value is a base name.
     * We want a single lookup in the map to find the font both by a base name or an alias.
     */
    private static final Map<String, FontName> ALIASES = new HashMap<>(38);

    /**
     * Contains the font metrics for the base fonts.
     * The key is a base font name, value is a FontMetrics instance.
     * Metrics are loaded into this map on demand, only if needed.
     * @see #getAFM
     */
    private static final Map<FontName, FontMetrics> FONTS = new HashMap<>(14);

    static
    {
        // the 14 standard fonts
        mapName(FontName.COURIER);
        mapName(FontName.COURIER_BOLD);
        mapName(FontName.COURIER_BOLD_OBLIQUE);
        mapName(FontName.COURIER_OBLIQUE);
        mapName(FontName.HELVETICA);
        mapName(FontName.HELVETICA_BOLD);
        mapName(FontName.HELVETICA_BOLD_OBLIQUE);
        mapName(FontName.HELVETICA_OBLIQUE);
        mapName(FontName.TIMES_ROMAN);
        mapName(FontName.TIMES_BOLD);
        mapName(FontName.TIMES_BOLD_ITALIC);
        mapName(FontName.TIMES_ITALIC);
        mapName(FontName.SYMBOL);
        mapName(FontName.ZAPF_DINGBATS);

        // alternative names from Adobe Supplement to the ISO 32000
        mapName("CourierCourierNew", FontName.COURIER);
        mapName("CourierNew", FontName.COURIER);
        mapName("CourierNew,Italic", FontName.COURIER_OBLIQUE);
        mapName("CourierNew,Bold", FontName.COURIER_BOLD);
        mapName("CourierNew,BoldItalic", FontName.COURIER_BOLD_OBLIQUE);
        mapName("Arial", FontName.HELVETICA);
        mapName("Arial,Italic", FontName.HELVETICA_OBLIQUE);
        mapName("Arial,Bold", FontName.HELVETICA_BOLD);
        mapName("Arial,BoldItalic", FontName.HELVETICA_BOLD_OBLIQUE);
        mapName("TimesNewRoman", FontName.TIMES_ROMAN);
        mapName("TimesNewRoman,Italic", FontName.TIMES_ITALIC);
        mapName("TimesNewRoman,Bold", FontName.TIMES_BOLD);
        mapName("TimesNewRoman,BoldItalic", FontName.TIMES_BOLD_ITALIC);

        // Acrobat treats these fonts as "standard 14" too (at least Acrobat preflight says so)
        mapName("Symbol,Italic", FontName.SYMBOL);
        mapName("Symbol,Bold", FontName.SYMBOL);
        mapName("Symbol,BoldItalic", FontName.SYMBOL);
        mapName("Times", FontName.TIMES_ROMAN);
        mapName("Times,Italic", FontName.TIMES_ITALIC);
        mapName("Times,Bold", FontName.TIMES_BOLD);
        mapName("Times,BoldItalic", FontName.TIMES_BOLD_ITALIC);

        // PDFBOX-3457: PDF.js file bug864847.pdf
        mapName("ArialMT", FontName.HELVETICA);
        mapName("Arial-ItalicMT", FontName.HELVETICA_OBLIQUE);
        mapName("Arial-BoldMT", FontName.HELVETICA_BOLD);
        mapName("Arial-BoldItalicMT", FontName.HELVETICA_BOLD_OBLIQUE);
    }

    private Standard14Fonts()
    {
    }

    /**
     * Loads the metrics for the base font specified by name. Metric file must exist in the pdfbox jar under
     * /org/apache/pdfbox/resources/afm/
     *
     * @param fontName one of the standard 14 font names for which to load the metrics.
     * @throws IOException if no metrics exist for that font.
     */
    private static void loadMetrics(FontName fontName) throws IOException
    {
        String resourceName = "/org/apache/pdfbox/resources/afm/" + fontName.getName() + ".afm";
        InputStream resourceAsStream = PDType1Font.class.getResourceAsStream(resourceName);
        if (resourceAsStream == null)
        {
            throw new IOException("resource '" + resourceName + "' not found");
        }
        try (InputStream afmStream = new BufferedInputStream(resourceAsStream))
        {
            AFMParser parser = new AFMParser(afmStream);
            FontMetrics metric = parser.parse(true);
            FONTS.put(fontName, metric);
        }
    }

    /**
     * Adds a standard font name to the map of known aliases, to simplify the logic of finding
     * font metrics by name. We want a single lookup in the map to find the font both by a base name or
     * an alias.
     *
     * @see #getAFM
     * @param baseName the base name of the font; must be one of the 14 standard fonts
     */
    private static void mapName(FontName baseName)
    {
        ALIASES.put(baseName.getName(), baseName);
    }

    /**
     * Adds an alias name for a standard font to the map of known aliases to the map of aliases (alias as key, standard
     * name as value). We want a single lookup in tbaseNamehe map to find the font both by a base name or an alias.
     *
     * @param alias an alias for the font
     * @param baseName the base name of the font; must be one of the 14 standard fonts
     */
    private static void mapName(String alias, FontName baseName)
    {
        ALIASES.put(alias, baseName);
    }

    /**
     * Returns the metrics for font specified by fontName. Loads the font metrics if not already
     * loaded.
     *
     * @param fontName name of font; either a base name or alias
     * @return the font metrics or null if the name is not one of the known names
     * @throws IllegalArgumentException if no metrics exist for that font.
     */
    public static FontMetrics getAFM(String fontName)
    {
        FontName baseName = ALIASES.get(fontName);
        if (baseName == null)
        {
            return null;
        }

        if (FONTS.get(baseName) == null)
        {
            synchronized (FONTS)
            {
                if (FONTS.get(baseName) == null)
                {
                    try
                    {
                        loadMetrics(baseName);
                    }
                    catch (IOException e)
                    {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }

        return FONTS.get(baseName);
    }

    /**
     * Returns true if the given font name is one of the known names, including alias.
     *
     * @param fontName the name of font, either a base name or alias
     * @return true if the name is one of the known names
     */
    public static boolean containsName(String fontName)
    {
        return ALIASES.containsKey(fontName);
    }

    /**
     * Returns the set of known font names, including aliases.
     */
    public static Set<String> getNames()
    {
        return Collections.unmodifiableSet(ALIASES.keySet());
    }

    /**
     * Returns the base name of the font which the given font name maps to.
     *
     * @param fontName name of font, either a base name or an alias
     * @return the base name or null if this is not one of the known names
     */
    public static FontName getMappedFontName(String fontName)
    {
        return ALIASES.get(fontName);
    }

    /**
     * Enum for the names of the 14 standard fonts.
     */
    public enum FontName
    {
        TIMES_ROMAN("Times-Roman"), //
        TIMES_BOLD("Times-Bold"), //
        TIMES_ITALIC("Times-Italic"), //
        TIMES_BOLD_ITALIC("Times-BoldItalic"), //
        HELVETICA("Helvetica"), //
        HELVETICA_BOLD("Helvetica-Bold"), //
        HELVETICA_OBLIQUE("Helvetica-Oblique"), //
        HELVETICA_BOLD_OBLIQUE("Helvetica-BoldOblique"), //
        COURIER("Courier"), //
        COURIER_BOLD("Courier-Bold"), //
        COURIER_OBLIQUE("Courier-Oblique"), //
        COURIER_BOLD_OBLIQUE("Courier-BoldOblique"), //
        SYMBOL("Symbol"), //
        ZAPF_DINGBATS("ZapfDingbats");

        private final String name;

        private FontName(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
