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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.FontMetrics;
import org.apache.fontbox.cmap.CMap;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * This is the base class for all PDF fonts.
 * 
 * @author Ben Litchfield
 */
public abstract class PDFont implements COSObjectable, PDFontLike
{
    private static final Log LOG = LogFactory.getLog(PDFont.class);
    protected static final Matrix DEFAULT_FONT_MATRIX = new Matrix(0.001f, 0, 0, 0.001f, 0, 0);

    protected final COSDictionary dict;
    private final CMap toUnicodeCMap;
    
    /**
     * AFM for standard 14 fonts
     */
    private final FontMetrics afmStandard14;

    private PDFontDescriptor fontDescriptor;
    private List<Float> widths;
    private float avgFontWidth;
    private float fontWidthOfSpace = -1f;
    private final Map<Integer, Float> codeToWidthMap;

    /**
     * Constructor for embedding.
     */
    PDFont()
    {
        dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.FONT);
        toUnicodeCMap = null;
        fontDescriptor = null;
        afmStandard14 = null;
        codeToWidthMap = new HashMap<>();
    }

    /**
     * Constructor for Standard 14.
     */
    PDFont(String baseFont)
    {
        dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.FONT);
        toUnicodeCMap = null;
        afmStandard14 = Standard14Fonts.getAFM(baseFont);
        if (afmStandard14 == null)
        {
            throw new IllegalArgumentException("No AFM for font " + baseFont);
        }
        fontDescriptor = PDType1FontEmbedder.buildFontDescriptor(afmStandard14);
        // standard 14 fonts may be accessed concurrently, as they are singletons
        codeToWidthMap = new ConcurrentHashMap<>();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary Font dictionary.
     */
    protected PDFont(COSDictionary fontDictionary)
    {
        dict = fontDictionary;
        codeToWidthMap = new HashMap<>();

        // standard 14 fonts use an AFM
        afmStandard14 = Standard14Fonts.getAFM(getName()); // may be null (it usually is)
        fontDescriptor = loadFontDescriptor();
        toUnicodeCMap = loadUnicodeCmap();
    }

    private PDFontDescriptor loadFontDescriptor()
    {
        COSDictionary fd = dict.getCOSDictionary(COSName.FONT_DESC);
        if (fd != null)
        {
            return new PDFontDescriptor(fd);
        }
        else if (afmStandard14 != null)
        {
            // build font descriptor from the AFM
            return PDType1FontEmbedder.buildFontDescriptor(afmStandard14);
        }
        else
        {
            return null;
        }
    }

    private CMap loadUnicodeCmap()
    {
        COSBase toUnicode = dict.getDictionaryObject(COSName.TO_UNICODE);
        if (toUnicode == null)
        {
            return null;
        }
        CMap cmap = null;
        try
        {
            cmap = readCMap(toUnicode);
            if (cmap != null && !cmap.hasUnicodeMappings())
            {
                LOG.warn("Invalid ToUnicode CMap in font " + getName());
                String cmapName = cmap.getName() != null ? cmap.getName() : "";
                String ordering = cmap.getOrdering() != null ? cmap.getOrdering() : "";
                COSName encoding = dict.getCOSName(COSName.ENCODING);
                if (cmapName.contains("Identity") //
                        || ordering.contains("Identity") //
                        || COSName.IDENTITY_H.equals(encoding) //
                        || COSName.IDENTITY_V.equals(encoding))
                {
                    // assume that if encoding is identity, then the reverse is also true
                    cmap = CMapManager.getPredefinedCMap(COSName.IDENTITY_H.getName());
                    LOG.warn("Using predefined identity CMap instead");
                }
            }
        }
        catch (IOException ex)
        {
            LOG.error("Could not read ToUnicode CMap in font " + getName(), ex);
        }
        return cmap;
    }

    /**
     * Returns the AFM if this is a Standard 14 font.
     */
    protected final FontMetrics getStandard14AFM()
    {
        return afmStandard14;
    }

    @Override
    public PDFontDescriptor getFontDescriptor()
    {
        return fontDescriptor;
    }

    /**
     * Sets the font descriptor when embedding a font.
     *
     * @param fontDescriptor
     */
    protected final void setFontDescriptor(PDFontDescriptor fontDescriptor)
    {
        this.fontDescriptor = fontDescriptor;
    }

    /**
     * Reads a CMap given a COS Stream or Name. May return null if a predefined CMap does not exist.
     *
     * @param base COSName or COSStream
     * @throws IOException
     */
    protected final CMap readCMap(COSBase base) throws IOException
    {
        if (base instanceof COSName)
        {
            // predefined CMap
            String name = ((COSName)base).getName();
            return CMapManager.getPredefinedCMap(name);
        }
        else if (base instanceof COSStream)
        {
            // embedded CMap
            InputStream input = null;
            try
            {
                input = ((COSStream)base).createInputStream();
                return CMapManager.parseCMap(input);
            }
            finally
            {
                IOUtils.closeQuietly(input);
            }
        }
        else
        {
            throw new IOException("Expected Name or Stream");
        }
    }

    @Override
    public COSDictionary getCOSObject()
    {
        return dict;
    }

    @Override
    public Vector getPositionVector(int code)
    {
        throw new UnsupportedOperationException("Horizontal fonts have no position vector");
    }

    /**
     * Returns the displacement vector (w0, w1) in text space, for the given character.
     * For horizontal text only the x component is used, for vertical text only the y component.
     *
     * @param code character code
     * @return displacement vector
     * @throws IOException
     */
    public Vector getDisplacement(int code) throws IOException
    {
        return new Vector(getWidth(code) / 1000, 0);
    }

    @Override
    public float getWidth(int code) throws IOException
    {
        Float width = codeToWidthMap.get(code);
        if (width != null)
        {
            return width;
        }
        
        // Acrobat overrides the widths in the font program on the conforming reader's system with
        // the widths specified in the font dictionary." (Adobe Supplement to the ISO 32000)
        //
        // Note: The Adobe Supplement says that the override happens "If the font program is not
        // embedded", however PDFBOX-427 shows that it also applies to embedded fonts.

        // Type1, Type1C, Type3
        if (dict.getDictionaryObject(COSName.WIDTHS) != null
                || dict.containsKey(COSName.MISSING_WIDTH))
        {
            int firstChar = dict.getInt(COSName.FIRST_CHAR, -1);
            int lastChar = dict.getInt(COSName.LAST_CHAR, -1);
            int siz = getWidths().size();
            int idx = code - firstChar;
            if (siz > 0 && code >= firstChar && code <= lastChar && idx < siz)
            {
                width = getWidths().get(idx);
                if (width == null)
                {
                    width = 0f;
                }
                codeToWidthMap.put(code, width);
                return width;
            }

            PDFontDescriptor fd = getFontDescriptor();
            if (fd != null)
            {
                // get entry from /MissingWidth entry
                width = fd.getMissingWidth();
                codeToWidthMap.put(code, width);
                return width;
            }
        }

        // standard 14 font widths are specified by an AFM
        if (isStandard14())
        {
            width = getStandard14Width(code);
            codeToWidthMap.put(code, width);
            return width;
        }
        
        // if there's nothing to override with, then obviously we fall back to the font
        width = getWidthFromFont(code);
        codeToWidthMap.put(code, width);
        return width;
    }

    /**
     * Returns the glyph width from the AFM if this is a Standard 14 font.
     * 
     * @param code character code
     * @return width in 1/1000 text space
     */
    protected abstract float getStandard14Width(int code);

    /**
     * Encodes the given string for use in a PDF content stream.
     *
     * @param text Any Unicode text.
     * @return Array of PDF content stream bytes.
     * @throws IOException If the text could not be encoded.
     * @throws IllegalArgumentException if a character isn't supported by the font.
     */
    public final byte[] encode(String text) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        while (offset < text.length())
        {
            int codePoint = text.codePointAt(offset);

            // multi-byte encoding with 1 to 4 bytes
            byte[] bytes = encode(codePoint);
            out.write(bytes);

            offset += Character.charCount(codePoint);
        }
        return out.toByteArray();
    }

    /**
     * Encodes the given Unicode code point for use in a PDF content stream.
     * Content streams use a multi-byte encoding with 1 to 4 bytes.
     *
     * <p>This method is called when embedding text in PDFs and when filling in fields.
     *
     * @param unicode Unicode code point.
     * @return Array of 1 to 4 PDF content stream bytes.
     * @throws IOException If the text could not be encoded.
     * @throws IllegalArgumentException if a character isn't supported by the font.
     */
    protected abstract byte[] encode(int unicode) throws IOException;

    /**
     * Returns the width of the given Unicode string.
     *
     * @param text The text to get the width of.
     * @return The width of the string in 1/1000 units of text space.
     * @throws IOException If there is an error getting the width information.
     * @throws IllegalArgumentException if a character isn't supported by the font.
     */
    public float getStringWidth(String text) throws IOException
    {
        byte[] bytes = encode(text);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        
        float width = 0;
        while (in.available() > 0)
        {
            int code = readCode(in);
            width += getWidth(code);
        }
        
        return width;
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     */
    // todo: this method is highly suspicious, the average glyph width is not usually a good metric
    @Override
    public float getAverageFontWidth()
    {
        float average;
        if (Float.compare(avgFontWidth, 0.0f) != 0)
        {
            average = avgFontWidth;
        }
        else
        {
            float totalWidth = 0.0f;
            float characterCount = 0.0f;
            COSArray widths = dict.getCOSArray(COSName.WIDTHS);
            if (widths != null)
            {
                for (int i = 0; i < widths.size(); i++)
                {
                    COSNumber fontWidth = (COSNumber) widths.getObject(i);
                    if (fontWidth.floatValue() > 0)
                    {
                        totalWidth += fontWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }

            if (totalWidth > 0)
            {
                average = totalWidth / characterCount;
            }
            else
            {
                average = 0;
            }
            avgFontWidth = average;
        }
        return average;
    }

    /**
     * Reads a character code from a content stream string. Codes may be up to 4 bytes long.
     *
     * @param in string stream
     * @return character code
     * @throws IOException if the CMap or stream cannot be read
     */
    public abstract int readCode(InputStream in) throws IOException;

    /**
     * Returns the Unicode character sequence which corresponds to the given character code.
     *
     * @param code character code
     * @param customGlyphList a custom glyph list to use instead of the Adobe Glyph List
     * @return Unicode character(s)
     */
    public String toUnicode(int code, GlyphList customGlyphList)
    {
        return toUnicode(code);
    }

    /**
     * Returns the Unicode character sequence which corresponds to the given character code.
     *
     * @param code character code
     * @return Unicode character(s)
     */
    public String toUnicode(int code)
    {
        // if the font dictionary containsName a ToUnicode CMap, use that CMap
        if (toUnicodeCMap != null)
        {
            if (toUnicodeCMap.getName() != null && 
                toUnicodeCMap.getName().startsWith("Identity-") && 
                    (dict.getCOSName(COSName.TO_UNICODE) != null
                            || !toUnicodeCMap.hasUnicodeMappings()))
            {
                // handle the undocumented case of using Identity-H/V as a ToUnicode CMap, this
                // isn't actually valid as the Identity-x CMaps are code->CID maps, not
                // code->Unicode maps. See sample_fonts_solidconvertor.pdf for an example.
                // PDFBOX-3123: do this only if the /ToUnicode entry is a name
                // PDFBOX-4322: identity streams are OK too
                return new String(new char[] { (char) code });
            }
            else
            {
                if (code < 256 && !(this instanceof PDType0Font))
                {
                    COSName encoding = dict.getCOSName(COSName.ENCODING);
                    if (encoding != null && !encoding.getName().startsWith("Identity"))
                    {
                        // due to the conversion to an int it is no longer possible to determine
                        // if the code is based on a one or two byte value. We should consider to
                        // refactor that part of the code.
                        // However, simple fonts with a predefined encoding are using one byte codes so that
                        // we can limit the CMap mappings to one byte codes by passing the origin length
                        return toUnicodeCMap.toUnicode(code, 1);
                    }
                }
                return toUnicodeCMap.toUnicode(code);
            }
        }

        // if no value has been produced, there is no way to obtain Unicode for the character.
        // this behaviour can be overridden is subclasses, but this method *must* return null here
        return null;
    }

    /**
     * This will always return "Font" for fonts.
     * 
     * @return The type of object that this is.
     */
    public String getType()
    {
        return dict.getNameAsString(COSName.TYPE);
    }

    /**
     * This will get the subtype of font.
     */
    public String getSubType()
    {
        return dict.getNameAsString(COSName.SUBTYPE);
    }

    /**
     * The widths of the characters. This will be null for the standard 14 fonts.
     *
     * @return The widths of the characters.
     */
    protected final List<Float> getWidths()
    {
        if (widths == null)
        {
            COSArray array = dict.getCOSArray(COSName.WIDTHS);
            if (array != null)
            {
                widths = array.toCOSNumberFloatList();
            }
            else
            {
                widths = Collections.emptyList();
            }
        }
        return widths;
    }

    @Override
    public Matrix getFontMatrix()
    {
        return DEFAULT_FONT_MATRIX;
    }

    /**
     * Determines the width of the space character.
     * 
     * @return the width of the space character
     */
    public float getSpaceWidth()
    {
        if (Float.compare(fontWidthOfSpace, -1f) == 0)
        {
            try
            {
                if (dict.containsKey(COSName.TO_UNICODE) && toUnicodeCMap != null)
                {
                    int spaceMapping = toUnicodeCMap.getSpaceMapping();
                    if (spaceMapping > -1)
                    {
                        fontWidthOfSpace = getWidth(spaceMapping);
                    }
                }
                else
                {
                    fontWidthOfSpace = getWidth(32);
                }
                
                // try to get it from the font itself
                if (fontWidthOfSpace <= 0)
                {
                    fontWidthOfSpace = getWidthFromFont(32);
                }
                // use the average font width as fall back
                if (fontWidthOfSpace <= 0)
                {
                    fontWidthOfSpace = getAverageFontWidth();
                }
            }
            catch (Exception e)
            {
                LOG.error("Can't determine the width of the space character, assuming 250", e);
                fontWidthOfSpace = 250f;
            }
        }
        return fontWidthOfSpace;
    }

    /**
     * Returns true if the font uses vertical writing mode.
     */
    public abstract boolean isVertical();

    /**
     * Returns true if this font is one of the "Standard 14" fonts and receives special handling.
     */
    public boolean isStandard14()
    {
        // this logic is based on Acrobat's behaviour, see PDFBOX-2372

        // embedded fonts never get special treatment
        if (isEmbedded())
        {
            return false;
        }

        // if the name matches, this is a Standard 14 font
        return Standard14Fonts.containsName(getName());
    }

    /**
     * Adds the given Unicode point to the subset.
     * 
     * @param codePoint Unicode code point
     */
    public abstract void addToSubset(int codePoint);
    
    /**
     * Replaces this font with a subset containing only the given Unicode characters.
     *
     * @throws IOException if the subset could not be written
     */
    public abstract void subset() throws IOException;

    /**
     * Returns true if this font will be subset when embedded.
     */
    public abstract boolean willBeSubset();

    @Override
    public boolean equals(Object other)
    {
        return other instanceof PDFont && ((PDFont) other).getCOSObject() == this.getCOSObject();
    }

    @Override
    public int hashCode()
    {
        return this.getCOSObject().hashCode();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " " + getName();
    }

    /**
     * Get the /ToUnicode CMap.
     *
     * @return The /ToUnicode CMap or null if there is none.
     */
    protected CMap getToUnicodeCMap()
    {
        return toUnicodeCMap;
    }
}
