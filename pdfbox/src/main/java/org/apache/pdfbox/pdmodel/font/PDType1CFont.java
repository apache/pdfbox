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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.Type1CharString;
import org.apache.fontbox.ttf.Type1Equivalent;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.Type1Encoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Type 1-equivalent CFF font.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class PDType1CFont extends PDFont implements PDType1Equivalent
{
    private static final Log LOG = LogFactory.getLog(PDType1CFont.class);
    private static final byte[] SPACE_BYTES = { (byte) 32 };

    private String fontName = null;
    private Map<String, Float> glyphWidths = new HashMap<String, Float>();
    private Map<String, Float> glyphHeights = new HashMap<String, Float>();
    private Float avgWidth = null;
    private PDRectangle fontBBox = null;

    private CFFType1Font cffFont; // embedded font
    private final Type1Equivalent type1Equivalent; // embedded or system font for rendering

    /**
     * Constructor.
     * 
     * @param fontDictionary the corresponding dictionary
     * @throws IOException it something went wrong
     */
    public PDType1CFont(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);

        PDFontDescriptor fd = getFontDescriptor();
        byte[] bytes = null;
        if (fd != null && fd instanceof PDFontDescriptorDictionary) // <-- todo: must be true
        {
            PDStream ff3Stream = ((PDFontDescriptorDictionary) fd).getFontFile3();
            if (ff3Stream != null)
            {
                bytes = IOUtils.toByteArray(ff3Stream.createInputStream());
            }
        }

        CFFParser cffParser = new CFFParser();
        cffFont = (CFFType1Font)cffParser.parse(bytes).get(0);

        if (cffFont != null)
        {
            type1Equivalent = cffFont;
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

        // cache the font name
        fontName = cffFont.getName();

        determineEncoding();
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
    public GeneralPath getPath(String name) throws IOException
    {
        return type1Equivalent.getPath(name);
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
        String name = getFontEncoding().getName(code);
        if (name != null)
        {
            return name;
        }
        else
        {
            return ".notdef";
        }
    }

    // todo: Encoding could encapsulate this behaviour (e.g. containsName)
    private boolean isNotDef(String name)
    {
        return ".notdef".equals(name);
    }

    @Override
    protected void determineEncoding()
    {
        super.determineEncoding();
        Encoding fontEncoding = getFontEncoding();
        if (fontEncoding == null)
        {
            // extract from CFF/substitute
            this.fontEncoding = new Type1Encoding(type1Equivalent.getEncoding());
        }
    }

    @Override
    public String encode(byte[] bytes, int offset, int length) throws IOException
    {
        String character = getUnicode(bytes, offset, length);
        if (character == null)
        {
            // todo: message is for debugging, remove in long term
            LOG.warn("No character for code " + (bytes[offset] & 0xff) + " in " + fontName);
            return null;
        }
        return character;
    }

    /*@Override
    public int encodeToCID(byte[] bytes, int offset, int length)
    {
        if (length > 2)
        {
            return -1;
        }
        int code = bytes[offset] & 0xff;
        if (length == 2)
        {
            code = code * 256 + bytes[offset + 1] & 0xff;
        }
        return code;
    }*/

    // helper
    private String getUnicode(byte[] bytes, int offset, int length) throws IOException
    {
        int code = getCodeFromArray(bytes, offset, length);
        String character = getFontEncoding().getCharacter(code);
        if (character == null)
        {
            // todo: message is for debugging, remove in long term
            LOG.warn("Could not get character " + code);
        }
        return character;
    }

    @Override
    public float getFontWidth(byte[] bytes, int offset, int length)
    {
        int code = bytes[offset] & 0xff;
        String name = codeToName(code);

        Float width = glyphWidths.get(name);
        if (width == null)
        {
            width = getCharacterWidth(name);
            glyphWidths.put(name, width);
        }

        return width;
    }

    @Override
    public float getFontHeight(byte[] bytes, int offset, int length)
    {
        int code = bytes[offset] & 0xff;
        String name = codeToName(code);

        if (isNotDef(name))
        {
            // todo: message is for debugging, remove in long term
            LOG.warn("No name for code " + (bytes[offset] & 0xff) + " in " + fontName);
            return 0;
        }

        float height = 0;
        if (!glyphHeights.containsKey(name))
        {
            height = getCharacterHeight(name);
            glyphHeights.put(name, height);
        }
        return height;
    }

    @Override
    public float getStringWidth(String string) throws IOException
    {
        float width = 0;
        for (int i = 0; i < string.length(); i++)
        {
            String character = string.substring(i, i + 1);
            String name = getFontEncoding().getNameForCharacter(character.charAt(0));
            if (isNotDef(name))
            {
                // todo: message is for debugging, remove in long term
                LOG.warn("No code for character " + character);
                return 0;
            }
            width += getCharacterWidth(name);
        }
        return width;
    }

    @Override
    public float getAverageFontWidth()
    {
        if (avgWidth == null)
        {
            avgWidth = getAverageCharacterWidth();
        }
        return avgWidth;
    }

    @Override
    public PDMatrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers = cffFont.getFontMatrix(); // todo: cffFont could be null
            if (numbers != null && numbers.size() == 6)
            {
                COSArray array = new COSArray();
                for (Number number : numbers)
                {
                    array.add(new COSFloat(number.floatValue()));
                }
                fontMatrix = new PDMatrix(array);
            }
            else
            {
                // todo: the font should always have a Matrix, so why fallback?
                super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    /**
     * Returns the embedded Type 1-equivalent CFF font.
     * 
     * @return the cffFont
     */
    public CFFType1Font getCFFType1Font()
    {
        return cffFont;
    }

    // todo: this is a replacement for FontMetrics method
    private float getCharacterWidth(String name)
    {
        try
        {
            // todo: for debugging we check for .notdef
            Type1CharString notdef = cffFont.getType1CharString(".notdef"); // todo: cffFont could be null
            Type1CharString charstring = cffFont.getType1CharString(name);
            if (charstring == notdef)
            {
                // todo: message is for debugging, remove in long term
                LOG.warn("No width for character " + name + ", using .notdef");
            }
            return charstring.getWidth();
        }
        catch (IOException e)
        {
            // todo: HACK
            LOG.error(e);
        }
        return 0;
    }

    // todo: this is a replacement for FontMetrics method
    // todo: but in FontMetrics this method actually gets the advance-y for vertical mode
    private float getCharacterHeight(String name)
    {
        try
        {
            return (float)cffFont.getType1CharString(name).getBounds().getHeight(); // todo: cffFont could be null
        }
        catch (IOException e)
        {
            // todo: HACK
            LOG.error(e);
            return 0;
        }
    }

    // todo: this is a replacement for FontMetrics method
    private float getAverageCharacterWidth()
    {
        // todo: not implemented, highly suspect
        return 500;
    }

    @Override
    public void clear()
    {
        super.clear();
        cffFont = null;
        fontBBox = null;
        if (glyphHeights != null)
        {
            glyphHeights.clear();
            glyphHeights = null;
        }
        if (glyphWidths != null)
        {
            glyphWidths.clear();
            glyphWidths = null;
        }
    }
}
