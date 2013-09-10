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

import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This is the base class for all PDF fonts.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public abstract class PDFont implements COSObjectable
{

    /**
     * The cos dictionary for this font.
     */
    protected COSDictionary font;

    /**
     * This is only used if this is a font object and it has an encoding.
     */
    private Encoding fontEncoding = null;

    /**
     * The descriptor of the font.
     */
    private PDFontDescriptor fontDescriptor = null;

    /**
     * The font matrix.
     */
    protected PDMatrix fontMatrix = null;

    /**
     * This is only used if this is a font object and it has an encoding and it is a type0 font with a cmap.
     */
    protected CMap cmap = null;

    /**
     * The CMap holding the ToUnicode mapping.
     */
    protected CMap toUnicodeCmap = null;

    private boolean hasToUnicode = false;

    protected static Map<String, CMap> cmapObjects = Collections.synchronizedMap(new HashMap<String, CMap>());

    /**
     * A list a floats representing the widths.
     */
    private List<Integer> widths = null;

    protected static final String resourceRootCMAP = "org/apache/pdfbox/resources/cmap/";

    /**
     * This will clear AFM resources that are stored statically. This is usually not a problem unless you want to
     * reclaim resources for a long running process.
     * 
     * SPECIAL NOTE: The font calculations are currently in COSObject, which is where they will reside until PDFont is
     * mature enough to take them over. PDFont is the appropriate place for them and not in COSObject but we need font
     * calculations for text extraction. THIS METHOD WILL BE MOVED OR REMOVED TO ANOTHER LOCATION IN A FUTURE VERSION OF
     * PDFBOX.
     */
    public static void clearResources()
    {
        cmapObjects.clear();
    }

    /**
     * Constructor.
     */
    public PDFont()
    {
        font = new COSDictionary();
        font.setItem(COSName.TYPE, COSName.FONT);
    }

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDFont(COSDictionary fontDictionary)
    {
        font = fontDictionary;
        determineEncoding();
    }

    /**
     * This will get the font descriptor for this font.
     * 
     * @return The font descriptor for this font.
     * 
     */
    public PDFontDescriptor getFontDescriptor()
    {
        if (fontDescriptor == null)
        {
            COSDictionary fd = (COSDictionary) font.getDictionaryObject(COSName.FONT_DESC);
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
            }
        }
        return fontDescriptor;
    }

    /**
     * This will set the font descriptor.
     * 
     * @param fdDictionary The font descriptor.
     */
    public void setFontDescriptor(PDFontDescriptorDictionary fdDictionary)
    {
        COSDictionary dic = null;
        if (fdDictionary != null)
        {
            dic = fdDictionary.getCOSDictionary();
        }
        font.setItem(COSName.FONT_DESC, dic);
        fontDescriptor = fdDictionary;
    }

    /**
     * Determines the encoding for the font. This method as to be overwritten, as there are different possibilities to
     * define a mapping.
     */
    protected abstract void determineEncoding();

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return font;
    }

    /**
     * This will get the font width for a character.
     * 
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     * 
     * @return The width is in 1000 unit of text space, ie 333 or 777
     * 
     * @throws IOException If an error occurs while parsing.
     */
    public abstract float getFontWidth(byte[] c, int offset, int length) throws IOException;

    /**
     * This will get the font width for a character.
     * 
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     * 
     * @return The width is in 1000 unit of text space, ie 333 or 777
     * 
     * @throws IOException If an error occurs while parsing.
     */
    public abstract float getFontHeight(byte[] c, int offset, int length) throws IOException;

    /**
     * This will get the width of this string for this font.
     * 
     * @param string The string to get the width of.
     * 
     * @return The width of the string in 1000 units of text space, ie 333 567...
     * 
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
     * 
     * @throws IOException If an error occurs while parsing.
     */
    public abstract float getAverageFontWidth() throws IOException;

    /**
     * Used for multibyte encodings.
     * 
     * @param data The array of data.
     * @param offset The offset into the array.
     * @param length The number of bytes to use.
     * 
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
     * 
     * @return The font width from the AFM file.
     * 
     * @throws IOException if we cannot find the width.
     */
    protected float getFontWidthFromAFMFile(int code) throws IOException
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
     * 
     * @throws IOException if we cannot find the width.
     */
    protected float getAverageFontWidthFromAFMFile() throws IOException
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
     * 
     */
    protected FontMetric getAFM()
    {
        return null;
    }

    private COSBase encoding = null;

    /**
     * cache the {@link COSName#ENCODING} object from the font's dictionary since it is called so often.
     * <p>
     * Use this method instead of
     * 
     * <pre>
     * font.getDictionaryObject(COSName.ENCODING);
     * </pre>
     * 
     * @return the encoding
     */
    protected COSBase getEncoding()
    {
        if (encoding == null)
        {
            encoding = font.getDictionaryObject(COSName.ENCODING);
        }
        return encoding;
    }

    /**
     * Set the encoding object from the fonts dictionary.
     * 
     * @param encodingValue the given encoding.
     */
    protected void setEncoding(COSBase encodingValue)
    {
        font.setItem(COSName.ENCODING, encodingValue);
        encoding = encodingValue;
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
    protected String cmapEncoding(int code, int length, boolean isCIDFont, CMap sourceCmap) throws IOException
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
     * 
     * @return The value of the encoded character.
     * 
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
                e.printStackTrace();
            }
            for (int j = 0; j < 256; j++)
            {
                try
                {
                    DOUBLE_CHAR_STRING[i][j] = new String(new byte[] { (byte) i, (byte) j }, "UTF-16BE");
                }
                catch (UnsupportedEncodingException e)
                {
                    // Nothing should happen here
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getStringFromArray(byte[] c, int offset, int length) throws IOException
    {
        String retval = null;
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
     * Parse the given CMap.
     * 
     * @param cmapRoot the root path pointing to the provided CMaps
     * @param cmapStream the CMap to be read
     * @return the parsed CMap
     */
    protected CMap parseCmap(String cmapRoot, InputStream cmapStream)
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
            }
        }
        return targetCmap;
    }

    /**
     * The will set the encoding for this font.
     * 
     * @param enc The font encoding.
     */
    public void setFontEncoding(Encoding enc)
    {
        fontEncoding = enc;
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
        return font.getNameAsString(COSName.TYPE);
    }

    // Memorized values to avoid repeated dictionary lookups
    private String subtype = null;
    private boolean type1Font;
    private boolean type3Font;
    private boolean trueTypeFont;
    private boolean type0Font;

    /**
     * This will get the subtype of font, Type1, Type3, ...
     * 
     * @return The type of font that this is.
     */
    public String getSubType()
    {
        if (subtype == null)
        {
            subtype = font.getNameAsString(COSName.SUBTYPE);
            type1Font = "Type1".equals(subtype);
            trueTypeFont = "TrueType".equals(subtype);
            type0Font = "Type0".equals(subtype);
            type3Font = "Type3".equals(subtype);
        }
        return subtype;
    }

    /**
     * Determines if the font is a type 1 font.
     * 
     * @return returns true if the font is a type 1 font
     */
    public boolean isType1Font()
    {
        getSubType();
        return type1Font;
    }

    /**
     * Determines if the font is a type 3 font.
     * 
     * @return returns true if the font is a type 3 font
     */
    public boolean isType3Font()
    {
        getSubType();
        return type3Font;
    }

    /**
     * Determines if the font is a type 0 font.
     * 
     * @return returns true if the font is a type 0 font
     */
    public boolean isType0Font()
    {
        getSubType();
        return type0Font;
    }

    /**
     * Determines if the font is a true type font.
     * 
     * @return returns true if the font is a true type font
     */
    public boolean isTrueTypeFont()
    {
        getSubType();
        return trueTypeFont;
    }

    /**
     * Determines if the font is a symbolic font.
     * 
     * @return returns true if the font is a symbolic font
     */
    public boolean isSymbolicFont()
    {
        if (getFontDescriptor() != null)
        {
            return getFontDescriptor().isSymbolic();
        }
        return false;
    }

    /**
     * The PostScript name of the font.
     * 
     * @return The postscript name of the font.
     */
    public String getBaseFont()
    {
        return font.getNameAsString(COSName.BASE_FONT);
    }

    /**
     * Set the PostScript name of the font.
     * 
     * @param baseFont The postscript name for the font.
     */
    public void setBaseFont(String baseFont)
    {
        font.setName(COSName.BASE_FONT, baseFont);
    }

    /**
     * The code for the first char or -1 if there is none.
     * 
     * @return The code for the first character.
     */
    public int getFirstChar()
    {
        return font.getInt(COSName.FIRST_CHAR, -1);
    }

    /**
     * Set the first character this font supports.
     * 
     * @param firstChar The first character.
     */
    public void setFirstChar(int firstChar)
    {
        font.setInt(COSName.FIRST_CHAR, firstChar);
    }

    /**
     * The code for the last char or -1 if there is none.
     * 
     * @return The code for the last character.
     */
    public int getLastChar()
    {
        return font.getInt(COSName.LAST_CHAR, -1);
    }

    /**
     * Set the last character this font supports.
     * 
     * @param lastChar The last character.
     */
    public void setLastChar(int lastChar)
    {
        font.setInt(COSName.LAST_CHAR, lastChar);
    }

    /**
     * The widths of the characters. This will be null for the standard 14 fonts.
     * 
     * @return The widths of the characters.
     */
    public List<Integer> getWidths()
    {
        if (widths == null)
        {
            COSArray array = (COSArray) font.getDictionaryObject(COSName.WIDTHS);
            if (array != null)
            {
                widths = COSArrayList.convertIntegerCOSArrayToList(array);
            }
        }
        return widths;
    }

    /**
     * Set the widths of the characters code.
     * 
     * @param widthsList The widths of the character codes.
     */
    public void setWidths(List<Integer> widthsList)
    {
        widths = widthsList;
        font.setItem(COSName.WIDTHS, COSArrayList.converterToCOSArray(widths));
    }

    /**
     * This will get the matrix that is used to transform glyph space to text space. By default there are 1000 glyph
     * units to 1 text space unit, but type3 fonts can use any value.
     * 
     * Note:If this is a type3 font then it can be modified via the PDType3Font.setFontMatrix, otherwise this is a
     * read-only property.
     * 
     * @return The matrix to transform from glyph space to text space.
     */
    public PDMatrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            COSArray array = (COSArray) font.getDictionaryObject(COSName.FONT_MATRIX);
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
     * 
     * @throws IOException If there is an error getting the bounding box.
     */
    public abstract PDRectangle getFontBoundingBox() throws IOException;

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other)
    {
        return other instanceof PDFont && ((PDFont) other).getCOSObject() == this.getCOSObject();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return this.getCOSObject().hashCode();
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
            getWidths();
            if (widths != null)
            {
                width = widths.get(charCode - firstChar).floatValue();
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
     * Sets hasToUnicode to the given value.
     * 
     * @param hasToUnicodeValue the given value for hasToUnicode
     */
    protected void setHasToUnicode(boolean hasToUnicodeValue)
    {
        hasToUnicode = hasToUnicodeValue;
    }

    /**
     * Determines the width of the space character.
     * 
     * @return the width of the space character
     */
    public abstract float getSpaceWidth();

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

}
