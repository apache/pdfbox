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
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.Matrix;

/**
 * A CIDFont. A CIDFont is a PDF object that contains information about a CIDFont program. Although
 * its Type value is Font, a CIDFont is not actually a font.
 *
 * @author Ben Litchfield
 */
public abstract class PDCIDFont implements COSObjectable
{
    protected final PDType0Font parent;

    private Map<Integer, Float> widths;
    private float defaultWidth;

    protected final COSDictionary dict;
    private PDFontDescriptor fontDescriptor;

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    protected PDCIDFont(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        this.dict = fontDictionary;
        this.parent = parent;
        readWidths();
    }

    private void readWidths()
    {
        if (widths == null)
        {
            widths = new HashMap<Integer, Float>();
            COSArray widths = (COSArray) dict.getDictionaryObject(COSName.W);
            if (widths != null)
            {
                int size = widths.size();
                int counter = 0;
                while (counter < size)
                {
                    COSNumber firstCode = (COSNumber) widths.getObject(counter++);
                    COSBase next = widths.getObject(counter++);
                    if (next instanceof COSArray)
                    {
                        COSArray array = (COSArray) next;
                        int startRange = firstCode.intValue();
                        int arraySize = array.size();
                        for (int i = 0; i < arraySize; i++)
                        {
                            COSNumber width = (COSNumber) array.get(i);
                            this.widths.put(startRange + i, width.floatValue());
                        }
                    }
                    else
                    {
                        COSNumber secondCode = (COSNumber) next;
                        COSNumber rangeWidth = (COSNumber) widths.getObject(counter++);
                        int startRange = firstCode.intValue();
                        int endRange = secondCode.intValue();
                        float width = rangeWidth.floatValue();
                        for (int i = startRange; i <= endRange; i++)
                        {
                            this.widths.put(i, width);
                        }
                    }
                }
            }
        }
    }

    @Override
    public COSDictionary getCOSObject()
    {
        return dict;
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
     * This will get the font descriptor for this font. A font descriptor is required for a CIDFont.
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
        }
        return fontDescriptor;
    }

    /**
     * Returns the font matrix, which represents the transformation from glyph space to text space.
     */
    public abstract Matrix getFontMatrix();

    /**
     * Returns the Type 0 font which is the parent of this font.
     *
     * @return parent Type 0 font
     */
    public final PDType0Font getParent()
    {
        return parent;
    }

    /**
     * This will get the default width. The default value for the default width is 1000.
     *
     * @return The default width for the glyphs in this font.
     */
    private float getDefaultWidth()
    {
        if (defaultWidth == 0)
        {
            COSNumber number = (COSNumber) dict.getDictionaryObject(COSName.DW);
            if (number != null)
            {
                defaultWidth = number.floatValue();
            }
            else
            {
                defaultWidth = 1000;
            }
        }
        return defaultWidth;
    }

    /**
     * This will get the font height for a character.
     *
     * @param code character code
     * @return The height is in 1000 unit of text space, ie 333 or 777
     */
    public abstract float getHeight(int code) throws IOException;

    /**
     * Returns the width of the given character.
     *
     * @param code character code
     */
    public float getWidth(int code) throws IOException
    {
        // These widths shall be consistent with the actual widths given in the CIDFont program.
        // Note: PDFBOX-1422 contains an example showing that CIDFont widths are not overridden
        return getWidthFromFont(code);
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
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     */
    public float getAverageFontWidth()
    {
        float totalWidths = 0.0f;
        float characterCount = 0.0f;
        COSArray widths = (COSArray) dict.getDictionaryObject(COSName.W);

        if (widths != null)
        {
            for (int i = 0; i < widths.size(); i++)
            {
                COSNumber firstCode = (COSNumber) widths.getObject(i++);
                COSBase next = widths.getObject(i);
                if (next instanceof COSArray)
                {
                    COSArray array = (COSArray) next;
                    for (int j = 0; j < array.size(); j++)
                    {
                        COSNumber width = (COSNumber) array.get(j);
                        totalWidths += width.floatValue();
                        characterCount += 1;
                    }
                }
                else
                {
                    i++;
                    COSNumber rangeWidth = (COSNumber) widths.getObject(i);
                    if (rangeWidth.floatValue() > 0)
                    {
                        totalWidths += rangeWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }
        }
        float average = totalWidths / characterCount;
        if (average <= 0)
        {
            average = getDefaultWidth();
        }
        return average;
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    public abstract int codeToCID(int code);

    /**
     * Returns the GID for the given character code.
     *
     * @param code character code
     * @return GID
     */
    public abstract int codeToGID(int code) throws IOException;

    public void clear()
    {
        if (widths != null)
        {
            widths.clear();
            widths = null;
        }
    }
}
