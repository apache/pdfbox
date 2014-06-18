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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.cff.AFMFormatter;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Adobe CFF Font, also known as a "Type1C" font.
 * 
 * @author Villu Ruusmann
 */
public class PDType1CFont extends PDFont
{
    private static final Log LOG = LogFactory.getLog(PDType1CFont.class);
    private static final byte[] SPACE_BYTES = { (byte) 32 };

    private CFFFont cffFont = null;
    private String fontname = null;
    private Map<Integer, String> sidToName = new HashMap<Integer, String>();
    private Map<Integer, Integer> codeToSID = new HashMap<Integer, Integer>();
    private Map<Integer, String> sidToCharacter = new HashMap<Integer, String>();
    private Map<String, Integer> characterToSID = new HashMap<String, Integer>();
    private FontMetric fontMetric = null;
    private Map<String, Float> glyphWidths = new HashMap<String, Float>();
    private Map<String, Float> glyphHeights = new HashMap<String, Float>();
    private Float avgWidth = null;
    private PDRectangle fontBBox = null;

    /**
     * Constructor.
     * 
     * @param fontDictionary the corresponding dictionary
     * @throws IOException it something went wrong
     */
    public PDType1CFont(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
        load();
    }

    @Override
    public String encode(byte[] bytes, int offset, int length) throws IOException
    {
        String character = getCharacter(bytes, offset, length);
        if (character == null)
        {
            LOG.debug("No character for code " + (bytes[offset] & 0xff) + " in " + fontname);
            return null;
        }
        return character;
    }

    @Override
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
    }

    private String getCharacter(byte[] bytes, int offset, int length)
    {
        int code = getCodeFromArray(bytes, offset, length);
        String character = null;
        if (codeToSID.containsKey(code))
        {
            code = codeToSID.get(code);
        }
        if (sidToCharacter.containsKey(code))
        {
            character = sidToCharacter.get(code);
        }
        return character;
    }

    @Override
    public float getFontWidth(byte[] bytes, int offset, int length) throws IOException
    {
        String name = getName(bytes, offset, length);
        if (name == null && !Arrays.equals(SPACE_BYTES, bytes))
        {
            LOG.debug("No name for code " + (bytes[offset] & 0xff) + " in " + fontname);
            return 0;
        }

        Float width = glyphWidths.get(name);
        if (width == null)
        {
            width = getFontMetric().getCharacterWidth(name);
            glyphWidths.put(name, width);
        }

        return width;
    }

    @Override
    public float getFontHeight(byte[] bytes, int offset, int length) throws IOException
    {
        String name = getName(bytes, offset, length);
        if (name == null)
        {
            LOG.debug("No name for code " + (bytes[offset] & 0xff) + " in " + fontname);

            return 0;
        }

        float height = 0;
        if (!glyphHeights.containsKey(name))
        {
            height = getFontMetric().getCharacterHeight(name);
            glyphHeights.put(name, height);
        }
        return height;
    }

    private String getName(byte[] bytes, int offset, int length)
    {
        if (length > 2)
        {
            return null;
        }

        int code = bytes[offset] & 0xff;
        if (length == 2)
        {
            code = code * 256 + bytes[offset + 1] & 0xff;
        }

        return sidToName.get(code);
    }

    @Override
    public float getStringWidth(String string) throws IOException
    {
        float width = 0;

        for (int i = 0; i < string.length(); i++)
        {
            String character = string.substring(i, i + 1);

            Integer code = getCode(character);
            if (code == null)
            {
                LOG.debug("No code for character " + character);

                return 0;
            }

            width += getFontWidth(new byte[] { (byte) code.intValue() }, 0, 1);
        }

        return width;
    }

    private Integer getCode(String character)
    {
        return characterToSID.get(character);
    }

    @Override
    public float getAverageFontWidth() throws IOException
    {
        if (avgWidth == null)
        {
            avgWidth = getFontMetric().getAverageCharacterWidth();
        }
        return avgWidth;
    }

    @Override
    public PDRectangle getFontBoundingBox() throws IOException
    {
        if (fontBBox == null)
        {
            fontBBox = new PDRectangle(getFontMetric().getFontBBox());
        }
        return fontBBox;
    }

    @Override
    public PDMatrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers = (List<Number>) cffFont.getProperty("FontMatrix");
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
                super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    private FontMetric getFontMetric()
    {
        if (fontMetric == null)
        {
            try
            {
                fontMetric = prepareFontMetric(cffFont);
            }
            catch (IOException exception)
            {
                LOG.error("An error occured while extracting the font metrics!", exception);
            }
        }
        return fontMetric;
    }

    private void load() throws IOException
    {
        byte[] cffBytes = loadBytes();

        CFFParser cffParser = new CFFParser();
        List<CFFFont> fonts = cffParser.parse(cffBytes);

        String baseFontName = getBaseFont();
        if (fonts.size() > 1 && baseFontName != null)
        {
            for (CFFFont font : fonts)
            {
                if (baseFontName.equals(font.getName()))
                {
                    cffFont = font;
                    break;
                }
            }
        }
        if (cffFont == null)
        {
            cffFont = fonts.get(0);
        }
        // cache the font name
        fontname = cffFont.getName();

        // TODO is this really needed?
        Number defaultWidthX = (Number) this.cffFont.getProperty("defaultWidthX");
        glyphWidths.put(null, defaultWidthX.floatValue());

        // calculate some mappings to be used for rendering and text extraction
        Encoding encoding = getFontEncoding();
        Map<String, Integer> nameToCode = encoding != null ? encoding.getNameToCodeMap() : null;
        Collection<CFFFont.Mapping> mappings = cffFont.getMappings();
        Map<Integer, String> codeToNameMap = new LinkedHashMap<Integer, String>();
        for (CFFFont.Mapping mapping : mappings)
        {
            codeToNameMap.put(mapping.getCode(), mapping.getName());
        }
        for (CFFFont.Mapping mapping : mappings)
        {
            int sid = mapping.getSID();
            String name = mapping.getName();
            String character = null;
            if (nameToCode != null && nameToCode.containsKey(name))
            {
                sid = nameToCode.get(name);
                character = encoding.getCharacter(name);
            }
            if (character == null)
            {
                character = Encoding.getCharacterForName(name);
            }
            codeToSID.put(mapping.getCode(), sid);
            sidToName.put(sid, name);
            if (character != null)
            {
                sidToCharacter.put(sid, character);
                characterToSID.put(character, sid);
            }
        }
    }

    private byte[] loadBytes() throws IOException
    {
        PDFontDescriptor fd = getFontDescriptor();
        if (fd != null && fd instanceof PDFontDescriptorDictionary)
        {
            PDStream ff3Stream = ((PDFontDescriptorDictionary) fd).getFontFile3();
            if (ff3Stream != null)
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();

                InputStream is = ff3Stream.createInputStream();
                try
                {
                    byte[] buf = new byte[512];
                    while (true)
                    {
                        int count = is.read(buf);
                        if (count < 0)
                        {
                            break;
                        }
                        os.write(buf, 0, count);
                    }
                }
                finally
                {
                    is.close();
                }

                return os.toByteArray();
            }
        }
        return null;
    }

    private FontMetric prepareFontMetric(CFFFont font) throws IOException
    {
        byte[] afmBytes = AFMFormatter.format(font);

        InputStream is = new ByteArrayInputStream(afmBytes);
        try
        {
            AFMParser afmParser = new AFMParser(is);
            FontMetric result = afmParser.parse();

            // Replace default FontBBox value with a newly computed one
            BoundingBox bounds = result.getFontBBox();
            List<Integer> numbers = Arrays.asList((int) bounds.getLowerLeftX(),
                    (int) bounds.getLowerLeftY(), (int) bounds.getUpperRightX(),
                    (int) bounds.getUpperRightY());
            font.addValueToTopDict("FontBBox", numbers);

            return result;
        }
        finally
        {
            is.close();
        }
    }

    /**
     * Returns the raw data of the font as CFFFont.
     * 
     * @return the cffFont
     * @throws IOException if something went wrong
     */
    public CFFFont getCFFFont() throws IOException
    {
        return cffFont;
    }
    
    @Override
    public void clear()
    {
        super.clear();
        cffFont = null;
        fontMetric = null;
        fontBBox = null;
        if (characterToSID != null)
        {
            characterToSID.clear();
            characterToSID = null;
        }
        if (codeToSID != null)
        {
            codeToSID.clear();
            codeToSID = null;
        }
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
        if (sidToCharacter != null)
        {
            sidToCharacter.clear();
            sidToCharacter = null;
        }
        if (sidToName != null)
        {
            sidToName.clear();
            sidToName = null;
        }
    }
}
