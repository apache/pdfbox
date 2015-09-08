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
package org.apache.pdfbox_ai2.pdmodel.font;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.fontbox_ai2.util.BoundingBox;
import org.apache.pdfbox_ai2.cos.COSArray;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.cos.COSNumber;
import org.apache.pdfbox_ai2.pdmodel.common.COSObjectable;
import org.apache.pdfbox_ai2.util.Matrix;
import org.apache.pdfbox_ai2.util.Vector;

/**
 * A CIDFont. A CIDFont is a PDF object that contains information about a CIDFont program. Although
 * its Type value is Font, a CIDFont is not actually a font.
 *
 * <p>It is not usually necessary to use this class directly, prefer {@link PDType0Font}.
 *
 * @author Ben Litchfield
 */
public abstract class PDCIDFont implements COSObjectable, PDFontLike, PDVectorFont
{
    protected final PDType0Font parent;

    private Map<Integer, Float> widths;
    private float defaultWidth;

    private final Map<Integer, Float> verticalDisplacementY = new HashMap<Integer, Float>(); // w1y
    private final Map<Integer, Vector> positionVectors = new HashMap<Integer, Vector>();     // v
    private float[] dw2;

    protected final COSDictionary dict;
    private PDFontDescriptor fontDescriptor;

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    PDCIDFont(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        this.dict = fontDictionary;
        this.parent = parent;
        readWidths();
        readVerticalDisplacements();
    }

    private void readWidths()
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

    private void readVerticalDisplacements()
    {
        // default position vector and vertical displacement vector
        COSArray cosDW2 = (COSArray) dict.getDictionaryObject(COSName.DW2);
        if (cosDW2 != null)
        {
            dw2 = new float[2];
            dw2[0] = ((COSNumber)cosDW2.get(0)).floatValue();
            dw2[1] = ((COSNumber)cosDW2.get(1)).floatValue();
        }
        else
        {
            dw2 = new float[] { 880, -1000 };
        }

        // vertical metrics for individual CIDs.
        COSArray w2 = (COSArray) dict.getDictionaryObject(COSName.W2);
        if (w2 != null)
        {
            for (int i = 0; i < w2.size(); i++)
            {
                COSNumber c = (COSNumber)w2.get(i);
                COSBase next = w2.get(++i);
                if (next instanceof COSArray)
                {
                    COSArray array = (COSArray)next;
                    for (int j = 0; j < array.size(); j++)
                    {
                        int cid = c.intValue() + j;
                        COSNumber w1y = (COSNumber) array.get(j);
                        COSNumber v1x = (COSNumber) array.get(++j);
                        COSNumber v1y = (COSNumber) array.get(++j);
                        verticalDisplacementY.put(cid, w1y.floatValue());
                        positionVectors.put(cid, new Vector(v1x.floatValue(), v1y.floatValue()));
                    }
                }
                else
                {
                    int first = c.intValue();
                    int last = ((COSNumber) next).intValue();
                    COSNumber w1y = (COSNumber) w2.get(++i);
                    COSNumber v1x = (COSNumber) w2.get(++i);
                    COSNumber v1y = (COSNumber) w2.get(++i);
                    for (int cid = first; cid <= last; cid++)
                    {
                        verticalDisplacementY.put(cid, w1y.floatValue());
                        positionVectors.put(cid, new Vector(v1x.floatValue(), v1y.floatValue()));
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

    @Override
    public String getName()
    {
        return getBaseFont();
    }

    @Override
    public PDFontDescriptor getFontDescriptor()
    {
        if (fontDescriptor == null)
        {
            COSDictionary fd = (COSDictionary) dict.getDictionaryObject(COSName.FONT_DESC);
            if (fd != null)
            {
                fontDescriptor = new PDFontDescriptor(fd);
            }
        }
        return fontDescriptor;
    }

    @Override
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

    @Override
    public abstract BoundingBox getBoundingBox() throws IOException;

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
     * Returns the default position vector (v).
     *
     * @param cid CID
     */
    private Vector getDefaultPositionVector(int cid)
    {
        float w0;
        if (widths.containsKey(cid))
        {
            Float w = widths.get(cid);
            if (w != null)
            {
                w0 = w;
            }
            else
            {
                w0 = getDefaultWidth();
            }
        }
        else
        {
            w0 = getDefaultWidth();
        }

        return new Vector(w0 / 2, dw2[0]);
    }

    @Override
    public Vector getPositionVector(int code)
    {
        int cid = codeToCID(code);
        Vector v = positionVectors.get(cid);
        if (v != null)
        {
            return v;
        }
        else
        {
            return getDefaultPositionVector(cid);
        }
    }

    /**
     * Returns the y-component of the vertical displacement vector (w1).
     *
     * @param code character code
     * @return w1y
     */
    public float getVerticalDisplacementVectorY(int code)
    {
        int cid = codeToCID(code);
        Float w1y = verticalDisplacementY.get(cid);
        if (w1y != null)
        {
            return w1y;
        }
        else
        {
            return dw2[1];
        }
    }

    @Override
    public abstract float getHeight(int code) throws IOException;

    @Override
    public float getWidth(int code) throws IOException
    {
        // these widths are supposed to be consistent with the actual widths given in the CIDFont
        // program, but PDFBOX-563 shows that when they are not, Acrobat overrides the embedded
        // font widths with the widths given in the font dictionary

        int cid = codeToCID(code);
        if (widths.containsKey(cid))
        {
            Float w = widths.get(cid);
            if (w != null)
            {
                return w;
            }
            else
            {
                return getDefaultWidth();
            }
        }
        else
        {
            return getWidthFromFont(code);
        }
    }

    @Override
    public abstract float getWidthFromFont(int code) throws IOException;

    @Override
    public abstract boolean isEmbedded();

    @Override
    // todo: this method is highly suspicious, the average glyph width is not usually a good metric
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
     * Returns the CIDSystemInfo, or null if it is missing (which isn't allowed but could happen).
     */
    public PDCIDSystemInfo getCIDSystemInfo()
    {
        COSDictionary cidSystemInfoDict = (COSDictionary)
                dict.getDictionaryObject(COSName.CIDSYSTEMINFO);

        PDCIDSystemInfo cidSystemInfo = null;
        if (cidSystemInfoDict != null)
        {
            cidSystemInfo = new PDCIDSystemInfo(cidSystemInfoDict);
        }

        return cidSystemInfo;
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

    /**
     * Encodes the given Unicode code point for use in a PDF content stream.
     * Content streams use a multi-byte encoding with 1 to 4 bytes.
     *
     * <p>This method is called when embedding text in PDFs and when filling in fields.
     *
     * @param unicode Unicode code point.
     * @return Array of 1 to 4 PDF content stream bytes.
     * @throws IOException If the text could not be encoded.
     */
    protected abstract byte[] encode(int unicode) throws IOException;
}
