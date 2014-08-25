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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.Type2CharString;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Type 0 CIDFont (CFF).
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCIDFontType0 extends PDCIDFont
{
    private final CFFCIDFont cidFont;  // Top DICT that uses CIDFont operators
    private final CFFType1Font t1Font; // Top DICT that does not use CIDFont operators

    private final Map<Integer, Float> glyphHeights = new HashMap<Integer, Float>();
    private final boolean isEmbedded;

    private Float avgWidth = null;
    private PDMatrix fontMatrix;

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
            CFFFont cffFont = cffParser.parse(bytes).get(0);
            if (cffFont instanceof CFFCIDFont)
            {
                cidFont = (CFFCIDFont)cffFont;
                t1Font = null;
            }
            else
            {
                cidFont = null;
                t1Font = (CFFType1Font)cffFont;
            }
            isEmbedded = true;
        }
        else
        {
            // substitute
            cidFont = ExternalFonts.getCFFCIDFont(getBaseFont());
            t1Font = null;

            if (cidFont == null)
            {
                // todo: log message + substitute? But what would we substitute with?
                throw new UnsupportedOperationException("not implemented: missing CFF");
            }
            isEmbedded = false;
        }
    }

    @Override
    public PDMatrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers;
            if (cidFont != null)
            {
                numbers = cidFont.getFontMatrix();
            }
            else
            {
                numbers = t1Font.getFontMatrix();
            }

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
                // default 1000 upem
                COSArray array = new COSArray();
                array.add(new COSFloat(0.001f));
                array.add(COSInteger.ZERO);
                array.add(COSInteger.ZERO);
                array.add(new COSFloat(0.001f));
                array.add(COSInteger.ZERO);
                array.add(COSInteger.ZERO);
                fontMatrix = new PDMatrix(array);
            }
        }
        return fontMatrix;
    }

    /**
     * Returns the embedded CFF CIDFont.
     */
    public CFFFont getCFFFont()
    {
        if (cidFont != null)
        {
            return cidFont;
        }
        else
        {
            return t1Font;
        }
    }

    /**
     * Returns the Type 2 charstring for the given CID.
     *
     * @param cid CID
     * @throws IOException if the charstring could not be read
     */
    public Type2CharString getType2CharString(int cid) throws IOException
    {
        if (cidFont != null)
        {
            return cidFont.getType2CharString(cid);
        }
        else
        {
            return t1Font.getType2CharString(cid);
        }
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    public int codeToCID(int code)
    {
        return parent.getCMap().toCID(code);
    }

    @Override
    public int codeToGID(int code)
    {
        int cid = codeToCID(code);
        if (cidFont != null)
        {
            // The CIDs shall be used to determine the GID value for the glyph procedure using the
            // charset table in the CFF program
            return cidFont.getCharset().getGIDForCID(cid);
        }
        else
        {
            // The CIDs shall be used directly as GID values
            return cid;
        }
    }

    @Override
    protected float getWidthFromFont(int code) throws IOException
    {
        int cid = codeToCID(code);
        int width = getType2CharString(cid).getWidth();
        float scaleX = (cidFont != null ? cidFont : t1Font).getFontMatrix().get(0).floatValue();
        return width * scaleX * 1000;
    }

    @Override
    protected boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        int cid = codeToCID(code);

        float height = 0;
        if (!glyphHeights.containsKey(cid))
        {
            height =  (float) getType2CharString(cid).getBounds().getHeight();
            glyphHeights.put(cid, height);
        }
        return height;
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

    // todo: this is a replacement for FontMetrics method
    private float getAverageCharacterWidth()
    {
        // todo: not implemented, highly suspect
        return 500;
    }
}
