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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.ttf.Type1Equivalent;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.GlyphList;
import org.apache.pdfbox.encoding.Type1Encoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Matrix;

/**
 * Type 1-equivalent CFF font.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class PDType1CFont extends PDSimpleFont implements PDType1Equivalent
{
    private static final Log LOG = LogFactory.getLog(PDType1CFont.class);

    private Map<String, Float> glyphHeights = new HashMap<String, Float>();
    private Float avgWidth = null;
    private PDRectangle fontBBox = null;
    private Matrix fontMatrix;
    private AffineTransform fontMatrixTransform;

    private final CFFType1Font cffFont; // embedded font
    private final Type1Equivalent type1Equivalent; // embedded or system font for rendering
    private final boolean isEmbedded;

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
            isEmbedded = true;
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
                type1Equivalent = ExternalFonts.getFallbackFont(getFontDescriptor());
            }
            isEmbedded = false;
        }
        readEncoding();
        fontMatrixTransform = getFontMatrix().createAffineTransform();
        fontMatrixTransform.scale(1000, 1000);
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
        return getEncoding().getName(code);
    }

    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        return Type1Encoding.fromFontBox(type1Equivalent.getEncoding());
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return in.read();
    }

    @Override
    public Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers = cffFont.getFontMatrix();
            if (numbers != null && numbers.size() == 6)
            {
                fontMatrix = new Matrix(numbers.get(0).floatValue(), numbers.get(1).floatValue(),
                                        numbers.get(2).floatValue(), numbers.get(3).floatValue(),
                                        numbers.get(4).floatValue(), numbers.get(5).floatValue());
            }
            else
            {
                return super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    @Override
    protected float getWidthFromFont(int code) throws IOException
    {
        String name = codeToName(code);
        int width = cffFont.getType1CharString(name).getWidth();

        Point2D p = new Point2D.Float(width, 0);
        fontMatrixTransform.transform(p, p);
        return (float)p.getX();
    }

    @Override
    public boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        String name = codeToName(code);
        float height = 0;
        if (!glyphHeights.containsKey(name))
        {
            height = (float)cffFont.getType1CharString(name).getBounds().getHeight(); // todo: cffFont could be null
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
            String name = GlyphList.unicodeToName(character.charAt(0));
            width += cffFont.getType1CharString(name).getWidth();
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
    private float getAverageCharacterWidth()
    {
        // todo: not implemented, highly suspect
        return 500;
    }

    @Override
    public void clear()
    {
        super.clear();
        fontBBox = null;
        if (glyphHeights != null)
        {
            glyphHeights.clear();
            glyphHeights = null;
        }
    }
}
