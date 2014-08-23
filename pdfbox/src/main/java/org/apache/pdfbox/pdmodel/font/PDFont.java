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
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cmap.CMap;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This is the base class for all PDF fonts.
 * 
 * @author Ben Litchfield
 */
public abstract class PDFont implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog(PDFont.class);

    private static final PDMatrix FONT_MATRIX_1000;
    static
    {
        COSArray array = new COSArray();
        array.add(new COSFloat(0.001f));
        array.add(COSInteger.ZERO);
        array.add(COSInteger.ZERO);
        array.add(new COSFloat(0.001f));
        array.add(COSInteger.ZERO);
        array.add(COSInteger.ZERO);
        FONT_MATRIX_1000 = new PDMatrix(array);
    }

    protected final COSDictionary dict;
    private final CMap toUnicodeCMap;
    protected PDFontDescriptor fontDescriptor;

    private List<Integer> widths;
    private float avgFontWidth;
    private float fontWidthOfSpace = -1f;

    /**
     * Constructor.
     */
    protected PDFont()
    {
        dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.FONT);
        toUnicodeCMap = null;
    }

    /**
     * Constructor.
     *
     * @param fontDictionary Font dictionary.
     */
    protected PDFont(COSDictionary fontDictionary) throws IOException
    {
        dict = fontDictionary;
        COSBase toUnicode = dict.getDictionaryObject(COSName.TO_UNICODE);
        if (toUnicode != null)
        {
            toUnicodeCMap = readCMap(toUnicode);
        }
        else
        {
            toUnicodeCMap = null;
        }
    }

    /**
     * Returns the font descriptor, may be null.
     */
    public PDFontDescriptor getFontDescriptor()
    {
        if (fontDescriptor == null)
        {
            COSDictionary fd = (COSDictionary) dict.getDictionaryObject(COSName.FONT_DESC);
            if (fd != null)
            {
                fontDescriptor = new PDFontDescriptorDictionary(fd);
            }
        }
        return fontDescriptor;
    }

    /**
     * Reads a CMap given a COS Stream or Name. May return null if a predefined CMap does not exist.
     *
     * @param base COSName or COSStream
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
                input = ((COSStream)base).getUnfilteredStream();
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

    /**
     * Returns the width of the given character.
     *
     * @param code character code
     */
    public float getWidth(int code) throws IOException
    {
        if (!isEmbedded())
        {
            // "If the font program is not embedded, Acrobat overrides the widths in the font
            // program on the conforming reader?s system with the widths specified in the font
            // dictionary." (Adobe Supplement to the ISO 32000)

            // Type1, Type1C, Type3
            int firstChar = dict.getInt(COSName.FIRST_CHAR, -1);
            int lastChar = dict.getInt(COSName.LAST_CHAR, -1);
            if (getWidths().size() > 0 && code >= firstChar && code <= lastChar)
            {
                return getWidths().get(code - firstChar).floatValue();
            }
            else
            {
                PDFontDescriptor fd = getFontDescriptor();
                if (fd instanceof PDFontDescriptorDictionary)
                {
                    return fd.getMissingWidth();
                }
                else
                {
                    // if there's nothing to override with, then obviously we fall back to the font
                    return getWidthFromFont(code);
                }
            }
        }
        else
        {
            // otherwise the fonts widths should exactly match the widths in the font dictionary
            return getWidthFromFont(code);
        }
    }

    /**
     * Returns the width of a glyph in the embedded font file.
     *
     * @param code character code
     * @return width in glyph space
     * @throws IOException if the font could not be read
     */
    protected abstract float getWidthFromFont(int code) throws IOException;

    /**
     * Returns true if the font file is embedded in the PDF.
     */
    protected abstract boolean isEmbedded();

    /**
     * This will get the font height for a character.
     * 
     * @param code character code
     * @return The height is in 1000 unit of text space, ie 333 or 777
     */
    // todo: this is not the glyph height at all! this method is *supposed* to get the y-advance
    public float getHeight(int code) throws IOException
    {
        // maybe there is already a precalculated value
        PDFontDescriptor desc = getFontDescriptor();
        if (desc != null)
        {
            // the following values are all more or less accurate at least all are average
            // values. Maybe we'll find another way to get those value for every single glyph
            // in the future if needed
            PDRectangle fontBBox = desc.getFontBoundingBox();
            float retval = 0;
            if (fontBBox != null)
            {
                retval = fontBBox.getHeight() / 2;
            }
            if (retval == 0)
            {
                retval = desc.getCapHeight();
            }
            if (retval == 0)
            {
                retval = desc.getAscent();
            }
            if (retval == 0)
            {
                retval = desc.getXHeight();
                if (retval > 0)
                {
                    retval -= desc.getDescent();
                }
            }
            return retval;
        }
        return 0;
    }

    /**
     * Returns the width of the given Unicode string.
     * 
     * @param string The string to get the width of.
     * @return The width of the string in 1000 units of text space, ie 333 567...
     * @throws IOException If there is an error getting the width information.
     */
    public float getStringWidth(String string) throws IOException
    {
        byte[] data = string.getBytes("ISO-8859-1"); // todo: *no*, these are *not* character codes
        float totalWidth = 0;
        for (int i = 0; i < data.length; i++)
        {
            totalWidth += getWidth(data[i]);
        }
        return totalWidth;
    }

    /**
     * This will get the average font width for all characters.
     * 
     * @return The width is in 1000 unit of text space, ie 333 or 777
     */
    // todo: this method is highly suspicious, the average glyph width is not usually a good metric
    public float getAverageFontWidth()
    {
        float average;
        if (avgFontWidth != 0.0f)
        {
            average = avgFontWidth;
        }
        else
        {
            float totalWidth = 0.0f;
            float characterCount = 0.0f;
            COSArray widths = (COSArray) dict.getDictionaryObject(COSName.WIDTHS);
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
     * @return Unicode character(s)
     */
    public String toUnicode(int code)
    {
        // if the font dictionary contains a ToUnicode CMap, use that CMap
        if (toUnicodeCMap != null)
        {
            if (toUnicodeCMap.getName() != null && toUnicodeCMap.getName().startsWith("Identity-"))
            {
                // handle the undocumented case of using Identity-H/V as a ToUnicode CMap, this
                // isn't  actually valid as the Identity-x CMaps are code->CID maps, not
                // code->Unicode maps. See sample_fonts_solidconvertor.pdf for an example.
                return new String(new char[] { (char) code });
            }
            else
            {
                // proceed as normal
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
     * Returns true the font is a symbolic (that is, it does not use the Adobe Standard Roman
     * character set).
     */
    public boolean isSymbolic()
    {
        if (getFontDescriptor() != null)
        {
            // fixme: isSymbolic() defaults to false if the flag is missing so we can't trust this
            return getFontDescriptor().isSymbolic();
        }
        return true;
    }

    /**
     * Returns the PostScript name of the font.
     */
    public String getBaseFont()
    {
        return dict.getNameAsString(COSName.BASE_FONT);
    }

    /**
     * The widths of the characters. This will be null for the standard 14 fonts.
     *
     * @return The widths of the characters.
     */
    private List<Integer> getWidths()
    {
        if (widths == null)
        {
            COSArray array = (COSArray) dict.getDictionaryObject(COSName.WIDTHS);
            if (array != null)
            {
                widths = COSArrayList.convertIntegerCOSArrayToList(array);
            }
            else
            {
                widths = Collections.emptyList();
            }
        }
        return widths;
    }

    /**
     * Returns the font matrix, which represents the transformation from glyph space to text space.
     */
    public PDMatrix getFontMatrix()
    {
        return FONT_MATRIX_1000;
    }

    /**
     * Determines the width of the space character.
     * 
     * @return the width of the space character
     */
    public float getSpaceWidth()
    {
        if (fontWidthOfSpace == -1f)
        {
            COSBase toUnicode = dict.getDictionaryObject(COSName.TO_UNICODE);
            try
            {
                if (toUnicode != null)
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
     * Calling this will release all cached information.
     */
    public void clear()
    {
    }

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
        return getClass().getSimpleName() + " " + getBaseFont();
    }
}
