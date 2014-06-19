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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is the base class for all PDF fonts.
 * 
 * @author Ben Litchfield
 */
public abstract class PDFont implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog(PDFont.class);
    private static final byte[] SPACE_BYTES = { (byte) 32 }; // formerly in PDSimpleFont

    protected static final String resourceRootCMAP = "org/apache/pdfbox/resources/cmap/";
    protected static Map<String, CMap> cmapObjects =
            Collections.synchronizedMap(new HashMap<String, CMap>()); // todo: why synchronized?

    private static final String[] SINGLE_CHAR_STRING = new String[256];
    private static final String[][] DOUBLE_CHAR_STRING = new String[256][256];
    static
    {
        for (int i = 0; i < 256; i++)
        {
            try
            {
                SINGLE_CHAR_STRING[i] = new String(new byte[] { (byte) i }, "ISO-8859-1");
            }
            catch (UnsupportedEncodingException e)
            {
                // Nothing should happen here
                LOG.error(e,e);
            }
            for (int j = 0; j < 256; j++)
            {
                try
                {
                    DOUBLE_CHAR_STRING[i][j] = new String(new byte[] { (byte) i, (byte) j },
                            "UTF-16BE");
                }
                catch (UnsupportedEncodingException e)
                {
                    // Nothing should happen here
                    LOG.error(e, e);
                }
            }
        }
    }

    private static String getStringFromArray(byte[] c, int offset, int length) throws IOException
    {
        String retval;
        if (length == 1)
        {
            retval = SINGLE_CHAR_STRING[(c[offset] + 256) % 256];
        }
        else if (length == 2)
        {
            retval = DOUBLE_CHAR_STRING[(c[offset] + 256) % 256][(c[offset + 1] + 256) % 256];
        }
        else
        {
            throw new IOException("Error:Unknown character length:" + length);
        }
        return retval;
    }

    /**
     * The Font dictionary.
     */
    protected COSDictionary dict;

    /**
     * The font matrix.
     */
    protected PDMatrix fontMatrix = null;

    // CMap / Encoding
    protected CMap cmap = null; // only used when this is a Type0 font with a CMap
    protected Encoding fontEncoding = null; // only used when this font has an encoding

    // the CMap holding the ToUnicode mapping
    private CMap toUnicodeCmap = null;
    private boolean hasToUnicode = false;

    private List<Integer> widths = null;

    private PDFontDescriptor fontDescriptor = null;
    private boolean widthsAreMissing = false;

    // formerly in PDSimpleFont
    private final HashMap<Integer, Float> fontSizes = new HashMap<Integer, Float>(128);
    private float avgFontWidth = 0.0f;
    private float avgFontHeight = 0.0f;
    private float fontWidthOfSpace = -1f;

    /**
     * This will clear AFM resources that are stored statically. This is usually not a problem
     * unless you want to reclaim resources for a long running process.
     *
     * SPECIAL NOTE: The font calculations are currently in COSObject, which is where they will
     * reside until PDFont is mature enough to take them over. PDFont is the appropriate place for
     * them and not in COSObject but we need font calculations for text extraction. THIS METHOD WILL
     * BE MOVED OR REMOVED TO ANOTHER LOCATION IN A FUTURE VERSION OF PDFBOX.
     *
     * @deprecated This method will be removed in a future version of PDFBox.
     */
    @Deprecated
    public static void clearResources()
    {
        cmapObjects.clear();
    }

    /**
     * Constructor.
     */
    protected PDFont()
    {
        dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.FONT);
    }

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    protected PDFont(COSDictionary fontDictionary)
    {
        dict = fontDictionary;
        determineEncoding();
    }

    /**
     * This will get the font descriptor for this font.
     * 
     * @return The font descriptor for this font.
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
            else
            {
                FontMetric afm = getAFM();
                if (afm != null)
                {
                    fontDescriptor = new PDFontDescriptorAFM(afm);
                }
                // it shouldn't be possible to reach this point...
            }
        }
        return fontDescriptor;
    }

    /**
     * Determines the encoding for the font. This method as to be overwritten, as there are
     * different possibilities to define a mapping.
     */
    protected void determineEncoding()
    {
        COSBase encoding = dict.getDictionaryObject(COSName.ENCODING);
        Encoding fontEncoding = null;
        if (encoding != null)
        {
            if (encoding instanceof COSName)
            {
                COSName encodingName = (COSName)encoding;
                try
                {
                    fontEncoding = Encoding.getInstance(encodingName);
                }
                catch (IOException exception)
                {
                    LOG.debug("Debug: Could not find encoding for " + encodingName);
                }
            }
            else if (encoding instanceof COSDictionary)
            {
                try
                {
                    fontEncoding = new DictionaryEncoding((COSDictionary) encoding);
                }
                catch (IOException exception)
                {
                    LOG.error("Error: Could not create the DictionaryEncoding");
                }
            }
        }
        this.fontEncoding = fontEncoding;
        extractToUnicodeEncoding();
    }

    protected final void extractToUnicodeEncoding()
    {
        COSName encodingName;
        String cmapName;
        COSBase toUnicode = dict.getDictionaryObject(COSName.TO_UNICODE);
        if (toUnicode != null)
        {
            hasToUnicode = true;
            if (toUnicode instanceof COSStream)
            {
                try
                {
                    InputStream is = ((COSStream) toUnicode).getUnfilteredStream();
                    toUnicodeCmap = parseCmap(resourceRootCMAP, is);
                    IOUtils.closeQuietly(is);
                }
                catch (IOException exception)
                {
                    LOG.error("Error: Could not load embedded ToUnicode CMap");
                }
            }
            else if (toUnicode instanceof COSName)
            {
                encodingName = (COSName) toUnicode;
                toUnicodeCmap = cmapObjects.get(encodingName.getName());
                if (toUnicodeCmap == null)
                {
                    cmapName = encodingName.getName();
                    String resourceName = resourceRootCMAP + cmapName;
                    try
                    {
                        toUnicodeCmap = parseCmap(resourceRootCMAP,
                                ResourceLoader.loadResource(resourceName));
                    }
                    catch (IOException exception)
                    {
                        LOG.error("Error: Could not find predefined ToUnicode CMap file for '" +
                                cmapName + "'");
                    }
                    if (toUnicodeCmap == null)
                    {
                        LOG.error("Error: Could not parse predefined ToUnicode CMap file for '" +
                                cmapName + "'");
                    }
                }
            }
        }
    }

    @Override
    public COSBase getCOSObject()
    {
        return dict;
    }

    /**
     * This will get the font width for a character.
     * 
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     * @return The width is in 1000 unit of text space, ie 333 or 777
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontWidth(byte[] c, int offset, int length) throws IOException
    {
        int code = getCodeFromArray(c, offset, length);
        Float fontWidth = fontSizes.get(code);
        if (fontWidth == null)
        {
            fontWidth = getFontWidth(code);
            if (fontWidth <= 0)
            {
                // TODO should this be in PDType1Font??
                fontWidth = getFontWidthFromAFMFile(code);
            }
            fontSizes.put(code, fontWidth);
        }
        return fontWidth;
    }

    /**
     * This will get the font height for a character.
     * 
     * @param c The character code to get the height for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     * @return The height is in 1000 unit of text space, ie 333 or 777
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontHeight(byte[] c, int offset, int length) throws IOException
    {
        // maybe there is already a precalculated value
        if (avgFontHeight > 0)
        {
            return avgFontHeight;
        }
        float retval = 0;
        FontMetric metric = getAFM();
        if (metric != null)
        {
            int code = getCodeFromArray(c, offset, length);
            Encoding encoding = getFontEncoding();
            String characterName = encoding.getName(code);
            retval = metric.getCharacterHeight(characterName);
        }
        else
        {
            PDFontDescriptor desc = getFontDescriptor();
            if (desc != null)
            {
                // the following values are all more or less accurate at least all are average
                // values. Maybe we'll find another way to get those value for every single glyph
                // in the future if needed
                PDRectangle fontBBox = desc.getFontBoundingBox();
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
                avgFontHeight = retval;
            }
        }
        return retval;
    }

    /**
     * This will get the width of this string for this font.
     * 
     * @param string The string to get the width of.
     * @return The width of the string in 1000 units of text space, ie 333 567...
     * @throws IOException If there is an error getting the width information.
     */
    public float getStringWidth(String string) throws IOException
    {
        byte[] data = string.getBytes("ISO-8859-1");
        float totalWidth = 0;
        for (int i = 0; i < data.length; i++)
        {
            totalWidth += getFontWidth(data, i, 1);
        }
        return totalWidth;
    }

    /**
     * This will get the average font width for all characters.
     * 
     * @return The width is in 1000 unit of text space, ie 333 or 777
     * @throws IOException If an error occurs while parsing.
     */
    public float getAverageFontWidth() throws IOException
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
                average = getAverageFontWidthFromAFMFile();
            }
            avgFontWidth = average;
        }
        return average;
    }

    /**
     * Used for multibyte encodings.
     * 
     * @param data The array of data.
     * @param offset The offset into the array.
     * @param length The number of bytes to use.
     * @return The int value of data from the array.
     */
    public int getCodeFromArray(byte[] data, int offset, int length)
    {
        int code = 0;
        for (int i = 0; i < length; i++)
        {
            code <<= 8;
            code |= (data[offset + i] + 256) % 256;
        }
        return code;
    }

    /**
     * This will attempt to get the font width from an AFM file.
     * 
     * @param code The character code we are trying to get.
     * @return The font width from the AFM file.
     * @throws IOException if we cannot find the width.
     */
    private float getFontWidthFromAFMFile(int code) throws IOException
    {
        float retval = 0;
        FontMetric metric = getAFM();
        if (metric != null)
        {
            String characterName = fontEncoding.getName(code);
            retval = metric.getCharacterWidth(characterName);
        }
        return retval;
    }

    /**
     * This will attempt to get the average font width from an AFM file.
     * 
     * @return The average font width from the AFM file.
     * @throws IOException if we cannot find the width.
     */
    private float getAverageFontWidthFromAFMFile() throws IOException
    {
        float retval = 0;
        FontMetric metric = getAFM();
        if (metric != null)
        {
            retval = metric.getAverageCharacterWidth();
        }
        return retval;
    }

    /**
     * This will get an AFM object if one exists.
     * 
     * @return The afm object from the name.
     */
    protected FontMetric getAFM()
    {
        return null;
    }

    /**
     * Encode the given value using the CMap of the font.
     * 
     * @param code the code to encode.
     * @param length the byte length of the given code.
     * @param isCIDFont indicates that the used font is a CID font.
     * 
     * @return The value of the encoded character.
     * @throws IOException if something went wrong
     */
    protected final String cmapEncoding(int code, int length, boolean isCIDFont, CMap sourceCmap)
            throws IOException
    {
        String retval = null;
        // there is not sourceCmap if this is a descendant font
        if (sourceCmap == null)
        {
            sourceCmap = cmap;
        }
        if (sourceCmap != null)
        {
            retval = sourceCmap.lookup(code, length);
            if (retval == null && isCIDFont)
            {
                retval = sourceCmap.lookupCID(code);
            }
        }
        return retval;
    }

    /**
     * This will perform the encoding of a character if needed.
     * 
     * @param c The character to encode.
     * @param offset The offset into the array to get the data
     * @param length The number of bytes to read.
     * @return The value of the encoded character.
     * @throws IOException If there is an error during the encoding.
     */
    public String encode(byte[] c, int offset, int length) throws IOException
    {
        String retval = null;
        int code = getCodeFromArray(c, offset, length);
        if (toUnicodeCmap != null)
        {
            retval = cmapEncoding(code, length, false, toUnicodeCmap);
        }
        if (retval == null && cmap != null)
        {
            retval = cmapEncoding(code, length, false, cmap);
        }

        // there is no cmap but probably an encoding with a suitable mapping
        if (retval == null)
        {
            if (fontEncoding != null)
            {
                retval = fontEncoding.getCharacter(code);
            }
            if (retval == null && (cmap == null || length == 2))
            {
                retval = getStringFromArray(c, offset, length);
            }
        }
        return retval;
    }

    public int encodeToCID(byte[] c, int offset, int length) throws IOException
    {
        int code = -1;
        if (encode(c, offset, length) != null)
        {
            code = getCodeFromArray(c, offset, length);
        }
        return code;
    }

    /**
     * Parse the given CMap.
     * 
     * @param cmapRoot the root path pointing to the provided CMaps
     * @param cmapStream the CMap to be read
     * @return the parsed CMap
     */
    protected final CMap parseCmap(String cmapRoot, InputStream cmapStream)
    {
        CMap targetCmap = null;
        if (cmapStream != null)
        {
            CMapParser parser = new CMapParser();
            try
            {
                targetCmap = parser.parse(cmapRoot, cmapStream);
                // limit the cache to external CMaps
                if (cmapRoot != null)
                {
                    cmapObjects.put(targetCmap.getName(), targetCmap);
                }
            }
            catch (IOException exception)
            {
                LOG.error("An error occurs while reading a CMap", exception);
            }
        }
        return targetCmap;
    }

    /**
     * This will get or create the encoder.
     * 
     * @return The encoding to use.
     */
    public Encoding getFontEncoding()
    {
        return fontEncoding;
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
     * 
     * @return The type of font that this is.
     */
    public String getSubType()
    {
        return dict.getNameAsString(COSName.SUBTYPE);
    }

    /**
     * Determines if the font is a type 1 font.
     * 
     * @return returns true if the font is a type 1 font
     */
    public boolean isType1Font()
    {
        return "Type1".equals(getSubType());
    }

    /**
     * Determines if the font is a type 3 font.
     * 
     * @return returns true if the font is a type 3 font
     */
    public boolean isType3Font()
    {
        return "Type3".equals(getSubType());
    }

    /**
     * Determines if the font is a type 0 font.
     * 
     * @return returns true if the font is a type 0 font
     */
    public boolean isType0Font()
    {
        return "Type0".equals(getSubType());
    }

    /**
     * Determines if the font is a true type font.
     * 
     * @return returns true if the font is a true type font
     */
    public boolean isTrueTypeFont()
    {
        return "TrueType".equals(getSubType());
    }

    /**
     * Determines if the font is a symbolic font.
     * 
     * @return returns true if the font is a symbolic font
     */
    public boolean isSymbolicFont()
    {
        return getFontDescriptor().isSymbolic();
    }

    /**
     * The PostScript name of the font.
     * 
     * @return The postscript name of the font.
     */
    public String getBaseFont()
    {
        return dict.getNameAsString(COSName.BASE_FONT);
    }

    /**
     * The code for the first char or -1 if there is none.
     * 
     * @return The code for the first character.
     */
    public int getFirstChar()
    {
        return dict.getInt(COSName.FIRST_CHAR, -1);
    }

    /**
     * The code for the last char or -1 if there is none.
     * 
     * @return The code for the last character.
     */
    public int getLastChar()
    {
        return dict.getInt(COSName.LAST_CHAR, -1);
    }

    /**
     * The widths of the characters. This will be null for the standard 14 fonts.
     * 
     * @return The widths of the characters.
     */
    public List<Integer> getWidths()
    {
        if (widths == null && !widthsAreMissing)
        {
            COSArray array = (COSArray) dict.getDictionaryObject(COSName.WIDTHS);
            if (array != null)
            {
                widths = COSArrayList.convertIntegerCOSArrayToList(array);
            }
            else
            {
                widthsAreMissing = true;
            }
        }
        return widths;
    }

    /**
     * This will get the matrix that is used to transform glyph space to text space. By default
     * there are 1000 glyph units to 1 text space unit, but type3 fonts can use any value.
     * 
     * Note: If this is a type3 font then it can be modified via the PDType3Font.setFontMatrix,
     * otherwise this is a read-only property.
     * 
     * @return The matrix to transform from glyph space to text space.
     */
    public PDMatrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            COSArray array = (COSArray) dict.getDictionaryObject(COSName.FONT_MATRIX);
            if (array == null)
            {
                array = new COSArray();
                array.add(new COSFloat(0.001f));
                array.add(COSInteger.ZERO);
                array.add(COSInteger.ZERO);
                array.add(new COSFloat(0.001f));
                array.add(COSInteger.ZERO);
                array.add(COSInteger.ZERO);
            }
            fontMatrix = new PDMatrix(array);
        }
        return fontMatrix;
    }

    /**
     * This will get the fonts bounding box.
     * 
     * @return The fonts bounding box.
     * @throws IOException If there is an error getting the bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        return getFontDescriptor().getFontBoundingBox();
    }

    /**
     * Determines the width of the given character.
     * 
     * @param charCode the code of the given character
     * @return the width of the character
     */
    public float getFontWidth(int charCode)
    {
        float width = -1;
        int firstChar = getFirstChar();
        int lastChar = getLastChar();
        if (charCode >= firstChar && charCode <= lastChar)
        {
            // maybe the font doesn't provide any widths
            if (!widthsAreMissing)
            {
                getWidths();
                if (widths != null)
                {
                    width = widths.get(charCode - firstChar).floatValue();
                }
            }
        }
        else
        {
            PDFontDescriptor fd = getFontDescriptor();
            if (fd instanceof PDFontDescriptorDictionary)
            {
                width = fd.getMissingWidth();
            }
        }
        return width;
    }

    /**
     * Determines if a font as a ToUnicode entry.
     * 
     * @return true if the font has a ToUnicode entry
     */
    public boolean hasToUnicode()
    {
        return hasToUnicode;
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
                    int spaceMapping = toUnicodeCmap.getSpaceMapping();
                    if (spaceMapping > -1)
                    {
                        fontWidthOfSpace = getFontWidth(spaceMapping);
                    }
                }
                else
                {
                    fontWidthOfSpace = getFontWidth(SPACE_BYTES, 0, 1);
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
     * Returns the toUnicode mapping if present.
     * 
     * @return the CMap representing the toUnicode mapping
     */
    public CMap getToUnicodeCMap()
    {
        return toUnicodeCmap;
    }

    /**
     * Returns the CMap if present.
     * 
     * @return the CMap representing the character encoding
     */
    public CMap getCMap()
    {
        return cmap;
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
}
