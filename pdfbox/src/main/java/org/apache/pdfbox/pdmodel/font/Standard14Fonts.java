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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
final class Standard14Fonts
{
    private Standard14Fonts()
    {
    }

    private static final Set<String> STANDARD_14_NAMES = new HashSet<>(34);
    private static final Map<String, String> STANDARD_14_MAPPING = new HashMap<>(34);
    private static final Map<String, FontMetrics> STANDARD14_AFM_MAP =  new HashMap<>(34);
    static
    {
        try
        {
            addAFM("Courier-Bold");
            addAFM("Courier-BoldOblique");
            addAFM("Courier");
            addAFM("Courier-Oblique");
            addAFM("Helvetica");
            addAFM("Helvetica-Bold");
            addAFM("Helvetica-BoldOblique");
            addAFM("Helvetica-Oblique");
            addAFM("Symbol");
            addAFM("Times-Bold");
            addAFM("Times-BoldItalic");
            addAFM("Times-Italic");
            addAFM("Times-Roman");
            addAFM("ZapfDingbats");

            // alternative names from Adobe Supplement to the ISO 32000
            addAFM("CourierCourierNew", "Courier");
            addAFM("CourierNew", "Courier");
            addAFM("CourierNew,Italic", "Courier-Oblique");
            addAFM("CourierNew,Bold", "Courier-Bold");
            addAFM("CourierNew,BoldItalic", "Courier-BoldOblique");
            addAFM("Arial", "Helvetica");
            addAFM("Arial,Italic", "Helvetica-Oblique");
            addAFM("Arial,Bold", "Helvetica-Bold");
            addAFM("Arial,BoldItalic", "Helvetica-BoldOblique");
            addAFM("TimesNewRoman", "Times-Roman");
            addAFM("TimesNewRoman,Italic", "Times-Italic");
            addAFM("TimesNewRoman,Bold", "Times-Bold");
            addAFM("TimesNewRoman,BoldItalic", "Times-BoldItalic");

            // Acrobat treats these fonts as "standard 14" too (at least Acrobat preflight says so)
            addAFM("Symbol,Italic", "Symbol");
            addAFM("Symbol,Bold", "Symbol");
            addAFM("Symbol,BoldItalic", "Symbol");
            addAFM("Times", "Times-Roman");
            addAFM("Times,Italic", "Times-Italic");
            addAFM("Times,Bold", "Times-Bold");
            addAFM("Times,BoldItalic", "Times-BoldItalic");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void addAFM(String fontName) throws IOException
    {
        addAFM(fontName, fontName);
    }

    private static void addAFM(String fontName, String afmName) throws IOException
    {
        STANDARD_14_NAMES.add(fontName);
        STANDARD_14_MAPPING.put(fontName, afmName);

        if (STANDARD14_AFM_MAP.containsKey(afmName))
        {
            STANDARD14_AFM_MAP.put(fontName, STANDARD14_AFM_MAP.get(afmName));
        }

        String resourceName = "org/apache/pdfbox/resources/afm/" + afmName + ".afm";
        URL url = PDType1Font.class.getClassLoader().getResource(resourceName);
        if (url != null)
        {
            try (InputStream afmStream = url.openStream())
            {
                AFMParser parser = new AFMParser(afmStream);
                FontMetrics metric = parser.parse(true);
                STANDARD14_AFM_MAP.put(fontName, metric);
            }
        }
        else
        {
            throw new IOException(resourceName + " not found");
        }
    }

    /**
     * Returns the AFM for the given font.
     * @param baseName base name of font
     */
    public static FontMetrics getAFM(String baseName)
    {
        return STANDARD14_AFM_MAP.get(baseName);
    }

    /**
     * Returns true if the given font name a Standard 14 font.
     * @param baseName base name of font
     */
    public static boolean containsName(String baseName)
    {
        return STANDARD_14_NAMES.contains(baseName);
    }

    /**
     * Returns the set of Standard 14 font names, including additional names.
     */
    public static Set<String> getNames()
    {
        return Collections.unmodifiableSet(STANDARD_14_NAMES);
    }

    /**
     * Returns the name of the actual font which the given font name maps to.
     * @param baseName base name of font
     */
    public static String getMappedFontName(String baseName)
    {
        return STANDARD_14_MAPPING.get(baseName);
    }
}
