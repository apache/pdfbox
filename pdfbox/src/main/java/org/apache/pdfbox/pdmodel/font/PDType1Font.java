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

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetrics;
import org.apache.fontbox.ttf.Type1Equivalent;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.StandardEncoding;
import org.apache.pdfbox.encoding.Type1Encoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * A PostScript Type 1 Font.
 *
 * @author Ben Litchfield
 */
public class PDType1Font extends PDSimpleFont implements PDType1Equivalent
{
    private static final Log LOG = LogFactory.getLog(PDType1Font.class);

    /**
     * The static map of the default Adobe font metrics.
     */
    private static final Map<String, FontMetrics> AFM_MAP;
    static
    {
        AFM_MAP = new HashMap<String, FontMetrics>();
        addMetric("Courier-Bold");
        addMetric("Courier-BoldOblique");
        addMetric("Courier");
        addMetric("Courier-Oblique");
        addMetric("Helvetica");
        addMetric("Helvetica-Bold");
        addMetric("Helvetica-BoldOblique");
        addMetric("Helvetica-Oblique");
        addMetric("Symbol");
        addMetric("Times-Bold");
        addMetric("Times-BoldItalic");
        addMetric("Times-Italic");
        addMetric("Times-Roman");
        addMetric("ZapfDingbats");
    }

    private static void addMetric(String name)
    {
        String prefix = name; // todo: HACK
        try
        {
            String resource = "org/apache/pdfbox/resources/afm/" + prefix + ".afm";
            InputStream afmStream = ResourceLoader.loadResource(resource);
            if (afmStream != null)
            {
                try
                {
                    AFMParser parser = new AFMParser(afmStream);
                    FontMetrics metric = parser.parse();
                    AFM_MAP.put(name, metric);
                }
                finally
                {
                    afmStream.close();
                }
            }
        }
        catch (Exception e)
        {
            LOG.error("Something went wrong when reading the adobe afm files", e);
        }
    }

    // todo: replace with enum? or getters?
    public static final PDType1Font TIMES_ROMAN = new PDType1Font("Times-Roman");
    public static final PDType1Font TIMES_BOLD = new PDType1Font("Times-Bold");
    public static final PDType1Font TIMES_ITALIC = new PDType1Font("Times-Italic");
    public static final PDType1Font TIMES_BOLD_ITALIC = new PDType1Font("Times-BoldItalic");
    public static final PDType1Font HELVETICA = new PDType1Font("Helvetica");
    public static final PDType1Font HELVETICA_BOLD = new PDType1Font("Helvetica-Bold");
    public static final PDType1Font HELVETICA_OBLIQUE = new PDType1Font("Helvetica-Oblique");
    public static final PDType1Font HELVETICA_BOLD_OBLIQUE = new PDType1Font("Helvetica-BoldOblique");
    public static final PDType1Font COURIER = new PDType1Font("Courier");
    public static final PDType1Font COURIER_BOLD = new PDType1Font("Courier-Bold");
    public static final PDType1Font COURIER_OBLIQUE = new PDType1Font("Courier-Oblique");
    public static final PDType1Font COURIER_BOLD_OBLIQUE = new PDType1Font("Courier-BoldOblique");
    public static final PDType1Font SYMBOL = new PDType1Font("Symbol");
    public static final PDType1Font ZAPF_DINGBATS = new PDType1Font("ZapfDingbats");

    private final FontMetrics afm; // for standard 14 fonts
    private final Type1Font type1font; // embedded font
    private final Type1Equivalent type1Equivalent; // embedded or system font for rendering
    private final boolean isEmbedded;

    /**
     * Creates a Type 1 standard 14 font for embedding.
     *
     * @param baseFont One of the standard 14 PostScript names
     */
    private PDType1Font(String baseFont)
    {
        dict.setItem(COSName.SUBTYPE, COSName.TYPE1);
        dict.setName(COSName.BASE_FONT, baseFont);
        encoding = new WinAnsiEncoding();
        dict.setItem(COSName.ENCODING, COSName.WIN_ANSI_ENCODING);

        afm = getAFMFromBaseFont(baseFont);
        if (afm == null)
        {
            throw new IllegalArgumentException("No AFM for font " + baseFont);
        }

        // todo: could load the PFB font here if we wanted to support Standard 14 embedding
        type1font = null;
        type1Equivalent = null;
        isEmbedded = false;
    }

    /**
     * Creates a new Type 1 font for embedding.
     *
     * @param doc PDF document to write to
     * @param afmIn AFM file stream
     * @param pfbIn PFB file stream
     * @throws IOException
     */
    public PDType1Font(PDDocument doc, InputStream afmIn, InputStream pfbIn) throws IOException
    {
        PDType1FontEmbedder embedder = new PDType1FontEmbedder(doc, dict, afmIn, pfbIn);
        encoding = embedder.getFontEncoding();
        afm = null; // only used for standard 14 fonts, not AFM fonts as we already have the PFB
        type1font = embedder.getType1Font();
        type1Equivalent = embedder.getType1Font();
        isEmbedded = true;
    }

    /**
     * Creates a Type 1 font from a Font dictionary in a PDF.
     * 
     * @param fontDictionary font dictionary
     */
    public PDType1Font(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
        PDFontDescriptor fd = getFontDescriptor();
        Type1Font t1 = null;
        if (fd != null && fd instanceof PDFontDescriptorDictionary) // <-- todo: must be true
        {
            // a Type1 font may contain a Type1C font
            PDStream fontFile3 = ((PDFontDescriptorDictionary) fd).getFontFile3();
            if (fontFile3 != null)
            {
                throw new IllegalArgumentException("Use PDType1CFont for FontFile3");
            }

            // or it may contain a PFB
            PDStream fontFile = ((PDFontDescriptorDictionary) fd).getFontFile();
            if (fontFile != null)
            {
                try
                {
                    COSStream stream = fontFile.getStream();
                    int length1 = stream.getInt(COSName.LENGTH1);
                    int length2 = stream.getInt(COSName.LENGTH2);

                    // the PFB embedded as two segments back-to-back
                    byte[] bytes = fontFile.getByteArray();
                    byte[] segment1 = Arrays.copyOfRange(bytes, 0, length1);
                    byte[] segment2 = Arrays.copyOfRange(bytes, length1, length1 + length2);

                    t1 =  Type1Font.createWithSegments(segment1, segment2);
                }
                catch (IOException e)
                {
                    LOG.error("Can't read the embedded Type1 font " + fd.getFontName(), e);
                }
            }
        }
        isEmbedded = t1 != null;

        // try to find a suitable .pfb font to substitute
        if (t1 == null)
        {
            t1 = ExternalFonts.getType1Font(getBaseFont());
        }

        type1font = t1;

        // find a type 1-equivalent font to use for rendering, could even be a .ttf
        if (type1font != null)
        {
            type1Equivalent = type1font;
        }
        else
        {
            Type1Equivalent t1Equiv = ExternalFonts.getType1EquivalentFont(getBaseFont());
            if (t1Equiv != null)
            {
                type1Equivalent = t1Equiv;
            }
            else
            {
                LOG.warn("Using fallback font for " + getBaseFont());
                type1Equivalent = ExternalFonts.getFallbackFont();
            }
        }

        // todo: for standard 14 only. todo: move this to a subclass "PDStandardType1Font" ?
        afm = getAFMFromBaseFont(getBaseFont()); // may be null (it usually is)

        readEncoding();
    }

    // todo: move this to a subclass?
    private FontMetrics getAFMFromBaseFont(String baseFont)
    {
        if (baseFont != null)
        {
            if (baseFont.contains("+"))
            {
                baseFont = baseFont.substring(baseFont.indexOf('+') + 1);
            }
            return AFM_MAP.get(baseFont);
        }
        return null;
    }

    @Override
    public PDFontDescriptor getFontDescriptor()
    {
        PDFontDescriptor fd = super.getFontDescriptor();
        if (fd == null)
        {
            if (afm != null)
            {
                // this is for embedding fonts into PDFs, rather than for reading, though it works.
                fd = new PDFontDescriptorAFM(afm);
                setFontDescriptor(fd);
            }
        }
        return fd;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        if (afm != null)
        {
            String characterName = getEncoding().getName(code);
            return afm.getCharacterHeight(characterName); // todo: isn't this the y-advance, not the height?
        }
        return super.getHeight(code);
    }

    @Override
    protected float getWidthFromFont(int code) throws IOException
    {
        String name = codeToName(code);
        if (afm != null)
        {
            return afm.getCharacterWidth(name);
        }
        else
        {
            return type1Equivalent.getWidth(name);
        }
    }

    @Override
    protected boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public float getAverageFontWidth()
    {
        if (afm != null)
        {
            return afm.getAverageCharacterWidth();
        }
        else
        {
            return super.getAverageFontWidth();
        }
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return in.read();
    }

    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        if (afm != null)
        {
            // read from AFM
            return new Type1Encoding(afm);
        }
        else
        {
            // extract from Type1 font/substitute
            if (type1Equivalent.getEncoding() != null)
            {
                return Type1Encoding.fromFontBox(type1Equivalent.getEncoding());
            }
            else
            {
                // default (only happens with TTFs)
                return StandardEncoding.INSTANCE;
            }
        }
    }

    /**
     * Returns the embedded or substituted Type 1 font, or null if there is none.
     */
    public Type1Font getType1Font()
    {
        return type1font;
    }

    /**
     * Returns the embedded or system font for rendering. This font is a Type 1-equivalent, but
     * may not be a Type 1 font, it could be a CFF font or TTF font. If there is no suitable font
     * then the fallback font will be returned: this method never returns null.
     */
    public Type1Equivalent getFontForRendering()
    {
        return type1Equivalent;
    }

    @Override
    public String getName()
    {
        return getBaseFont();
    }

    @Override
    public boolean hasGlyph(String name) throws IOException
    {
        return type1Equivalent.hasGlyph(name);
    }

    @Override
    public String codeToName(int code)
    {
        String name = getEncoding().getName(code);
        if (name != null)
        {
            return name;
        }
        else
        {
            return ".notdef";
        }
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        return type1Equivalent.getPath(name);
    }
}
