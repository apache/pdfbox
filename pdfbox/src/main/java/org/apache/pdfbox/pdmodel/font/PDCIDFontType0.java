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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Type 0 CIDFont (CFF).
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCIDFontType0 extends PDCIDFont
{
    private static final Log LOG = LogFactory.getLog(PDCIDFontType0.class);
    private static final byte[] SPACE_BYTES = { (byte) 32 };

    private CFFCIDFont cffFont = null;
    private String fontname = null;
    private final Map<Integer, Float> glyphWidths = new HashMap<Integer, Float>();
    private final Map<Integer, Float> glyphHeights = new HashMap<Integer, Float>();
    private Float avgWidth = null;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFontType0(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        super(fontDictionary, parent);

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

        if (bytes != null)
        {
            // embedded
            CFFParser cffParser = new CFFParser();
            cffFont = (CFFCIDFont)cffParser.parse(bytes).get(0);
        }
        else
        {
            // substitute
            cffFont = ExternalFonts.getCFFCIDFont(getBaseFont());

            if (cffFont == null)
            {
                // todo: log message + substitute? But what would we substitute with?
                throw new UnsupportedOperationException("not implemented: missing CFF");
            }
        }

        // cache the font name
        fontname = cffFont.getName();
    }

    /**
     * Returns the embedded CFF CIDFont.
     */
    public CFFCIDFont getCFFCIDFont()
    {
        return cffFont;
    }

    @Override
    public String encode(byte[] bytes, int offset, int length) throws IOException
    {
        // a CIDFont does not contain an encoding, instead the CMap defines this mapping
        int code = getCodeFromArray(bytes, offset, length);
        String character = getCMap().lookupCID(code);

        if (character == null)
        {
            LOG.error("No character for code " + (bytes[offset] & 0xff) + " in " + fontname);
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

    @Override
    public float getFontWidth(byte[] bytes, int offset, int length)
    {
        int cid = codeToCID(bytes, offset, length);
        if (cid == 0 && !Arrays.equals(SPACE_BYTES, bytes))
        {
            LOG.error("No name for code " + (bytes[offset] & 0xff) + " in " + fontname);
            return 0;
        }

        Float width = glyphWidths.get(cid);
        if (width == null)
        {
            width = getCharacterWidth(cid);
            glyphWidths.put(cid, width);
        }

        return width;
    }

    @Override
    public float getFontHeight(byte[] bytes, int offset, int length)
    {
        int cid = codeToCID(bytes, offset, length);
        if (cid == 0)
        {
            LOG.error("No CID for code " + (bytes[offset] & 0xff) + " in " + fontname);
            return 0;
        }

        float height = 0;
        if (!glyphHeights.containsKey(cid))
        {
            height = getCharacterHeight(cid);
            glyphHeights.put(cid, height);
        }
        return height;
    }

    // helper
    private int codeToCID(byte[] bytes, int offset, int length)
    {
        return getCMap().lookupCID(bytes, offset, length);
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    public int codeToCID(int code)
    {
        //int length = getCMap().hasTwoByteMappings() ? 2 : 1; // todo: HACK: see PDFStreamEngine
        int length = 2; // todo: HACK always use 2-byte mappings

        byte[] bytes;
        // todo: actually codes may be variable length (1 to 4 bytes)
        if (length == 1)
        {
            bytes = new byte[] { (byte)(code & 0xff) };
        }
        else
        {
            bytes = new byte[] { (byte)(code >> 8 & 0xff), (byte)(code & 0xff) };
        }
        return getCMap().lookupCID(bytes, 0, length);
    }

    @Override
    public float getStringWidth(String string) throws IOException
    {
        // todo: CMap currently has no methods which can do this correctly
        throw new UnsupportedOperationException("not implemented");
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
            List<Number> numbers = cffFont.getFontMatrix();
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

    // todo: this is a replacement for FontMetrics method
    private float getCharacterWidth(int cid)
    {
        try
        {
            return cffFont.getType2CharString(cid).getWidth();
        }
        catch (IOException e)
        {
            // todo: HACK
            LOG.error(e);
            return 0;
        }
    }

    // todo: this is a replacement for FontMetrics method
    // todo: but in FontMetrics this method actually gets the advance-y for vertical mode
    private float getCharacterHeight(int cid)
    {
        try
        {
            return (float)cffFont.getType2CharString(cid).getBounds().getHeight();
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
        if (cffFont != null)
        {
            //cffFont.clear();
            cffFont = null;
        }
    }
}
