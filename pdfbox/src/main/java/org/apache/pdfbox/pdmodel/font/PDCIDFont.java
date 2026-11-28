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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.Vector;

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
    private static final Log LOG = LogFactory.getLog(PDCIDFont.class);

    protected final PDType0Font parent;

    private Map<Integer, Float> widths;
    private float defaultWidth;
    private float averageWidth;

    private final Map<Integer, Float> verticalDisplacementY = new HashMap<Integer, Float>(); // w1y
    private final Map<Integer, Vector> positionVectors = new HashMap<Integer, Vector>();     // v
    // cid-ranges for horizontal widths
    private final List<WidthRange> widthRanges = new ArrayList<WidthRange>();
    // Using ranges instead of just storing the widths was done because of OOM conditions
    // in PDFBOX-6319 and PDFBOX-6175.
    // The code changes result in different behavior if ranges overlap, or
    // if ranges and individual widths overlap), and individual widths are considered first.

    private float[] dw2 = new float[] { 880, -1000 };

    protected final COSDictionary dict;
    private PDFontDescriptor fontDescriptor;

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    PDCIDFont(COSDictionary fontDictionary, PDType0Font parent)
    {
        this.dict = fontDictionary;
        this.parent = parent;
        readWidths();
        readVerticalDisplacements();
    }

    private void readWidths()
    {
        // see 9.7.4.3, "Glyph Metrics in CIDFonts"
        widths = new HashMap<Integer, Float>();
        COSBase wBase = dict.getDictionaryObject(COSName.W);
        if (wBase instanceof COSArray)
        {
            COSArray wArray = (COSArray) wBase;
            int size = wArray.size();
            int counter = 0;
            while (counter < size - 1)
            {
                COSBase firstCodeBase = wArray.getObject(counter++);
                if (!(firstCodeBase instanceof COSNumber))
                {
                    LOG.warn("Expected a number array member, got " + firstCodeBase);
                    continue;
                }
                COSNumber firstCode = (COSNumber) firstCodeBase;
                COSBase next = wArray.getObject(counter++);
                if (next instanceof COSArray)
                {
                    COSArray array = (COSArray) next;
                    int startRange = firstCode.intValue();
                    int arraySize = array.size();
                    for (int i = 0; i < arraySize; i++)
                    {
                        COSBase widthBase = array.getObject(i);
                        if (widthBase instanceof COSNumber)
                        {
                            COSNumber width = (COSNumber) widthBase;
                            widths.put(startRange + i, width.floatValue());
                        }
                        else
                        {
                            LOG.warn("Expected a number array member, got " + widthBase);
                        }
                    }
                }
                else
                {
                    if (counter >= size)
                    {
                        LOG.warn("premature end of widths array");
                        break;
                    }
                    COSBase secondCodeBase = next;
                    COSBase rangeWidthBase = wArray.getObject(counter++);
                    if (!(secondCodeBase instanceof COSNumber && rangeWidthBase instanceof COSNumber))
                    {
                        LOG.warn("Expected two numbers, got " + secondCodeBase + " and " + rangeWidthBase);
                        continue;
                    }
                    COSNumber secondCode = (COSNumber) secondCodeBase;
                    COSNumber rangeWidth = (COSNumber) rangeWidthBase;
                    int startRange = firstCode.intValue();
                    int endRange = secondCode.intValue();
                    float width = rangeWidth.floatValue();
                    if (endRange >= startRange)
                    {
                        widthRanges.add(new WidthRange(startRange, endRange, width));
                    }
                }
            }
        }
    }

    private void readVerticalDisplacements()
    {
        // default position vector and vertical displacement vector
        COSBase dw2Base = dict.getDictionaryObject(COSName.DW2);
        if (dw2Base instanceof COSArray)
        {
            COSArray dw2Array = (COSArray) dw2Base;
            COSBase base0 = dw2Array.getObject(0);
            COSBase base1 = dw2Array.getObject(1);
            if (base0 instanceof COSNumber && base1 instanceof COSNumber)
            {
                dw2[0] = ((COSNumber) base0).floatValue();
                dw2[1] = ((COSNumber) base1).floatValue();
            }
        }

        // vertical metrics for individual CIDs.
        COSBase w2Base = dict.getDictionaryObject(COSName.W2);
        if (w2Base instanceof COSArray)
        {
            COSArray w2Array = (COSArray) w2Base;
            for (int i = 0; i < w2Array.size(); i++)
            {
                COSBase base = w2Array.getObject(i);
                if (!(base instanceof COSNumber))
                {
                    continue;
                }
                COSNumber c = (COSNumber) base;
                COSBase next = w2Array.getObject(++i);
                if (next instanceof COSArray)
                {
                    COSArray array = (COSArray)next;
                    for (int j = 0; j < array.size() - 2; j++)
                    {
                        int cid = c.intValue() + j / 3;
                        COSBase w1y = array.getObject(j);
                        COSBase v1x = array.getObject(++j);
                        COSBase v1y = array.getObject(++j);
                        if (!(w1y instanceof COSNumber && v1x instanceof COSNumber && v1y instanceof COSNumber))
                        {
                            LOG.warn("Expected three numbers, got " + w1y + ", " + v1x + ", " + v1y);
                            break;
                        }
                        verticalDisplacementY.put(cid, ((COSNumber) w1y).floatValue());
                        positionVectors.put(cid, new Vector(((COSNumber) v1x).floatValue(), ((COSNumber) v1y).floatValue()));
                    }
                }
                else if (next instanceof COSNumber)
                {
                    if (i + 3 >= w2Array.size())
                    {
                        LOG.warn("Aborting read of 3 elements of /W2 table at index " + i + " with size " + w2Array.size());
                        break;
                    }
                    int first = c.intValue();
                    int last = ((COSNumber) next).intValue();
                    COSBase w1y = w2Array.getObject(++i);
                    COSBase v1x = w2Array.getObject(++i);
                    COSBase v1y = w2Array.getObject(++i);
                    if (!(w1y instanceof COSNumber && v1x instanceof COSNumber && v1y instanceof COSNumber))
                    {
                        LOG.warn("Expected three numbers, got " + w1y + ", " + v1x + ", " + v1y);
                        break;
                    }
                    for (int cid = first; cid <= last; cid++)
                    {
                        verticalDisplacementY.put(cid, ((COSNumber) w1y).floatValue());
                        positionVectors.put(cid, new Vector(((COSNumber) v1x).floatValue(), ((COSNumber) v1y).floatValue()));
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
            COSDictionary fd = dict.getCOSDictionary(COSName.FONT_DESC);
            if (fd != null)
            {
                fontDescriptor = new PDFontDescriptor(fd);
            }
        }
        return fontDescriptor;
    }

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
            COSBase base = dict.getDictionaryObject(COSName.DW);
            if (base instanceof COSNumber)
            {
                defaultWidth = ((COSNumber) base).floatValue();
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
        return new Vector(getWidthForCID(cid) / 2, dw2[0]);
    }

    private float getWidthForCID(int cid)
    {
        Float width = widths.get(cid);
        if (width == null)
        {
            for (WidthRange widthRange : widthRanges)
            {
                if (widthRange.rangeMatches(cid))
                {
                    return widthRange.getWidth();
                }
            }
            width = getDefaultWidth();
        }
        return width;
    }

    @Override
    public boolean hasExplicitWidth(int code) throws IOException
    {
        int cid = codeToCID(code);
        if (widths.containsKey(cid))
        {
            return true;
        }
        for (WidthRange wr : widthRanges)
        {
            if (wr.rangeMatches(cid))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Vector getPositionVector(int code)
    {
        int cid = codeToCID(code);
        Vector v = positionVectors.get(cid);
        if (v == null)
        {
            v = getDefaultPositionVector(cid);
        }
        return v;
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
        if (w1y == null)
        {
            w1y = dw2[1];
        }
        return w1y;
    }

    @Override
    public float getWidth(int code) throws IOException
    {
        // these widths are supposed to be consistent with the actual widths given in the CIDFont
        // program, but PDFBOX-563 shows that when they are not, Acrobat overrides the embedded
        // font widths with the widths given in the font dictionary
        return getWidthForCID(codeToCID(code));
    }

    @Override
    // todo: this method is highly suspicious, the average glyph width is not usually a good metric
    public float getAverageFontWidth()
    {
        if (averageWidth == 0)
        {
            float totalWidths = 0.0f;
            long characterCount = 0;
            if (widths != null)
            {
                for (Float width : widths.values())
                {
                    if (width > 0)
                    {
                        totalWidths += width;
                        ++characterCount;
                    }
                }
            }
            for (WidthRange widthRange : widthRanges)
            {
                if (widthRange.width > 0)
                {
                    long rangeSize = widthRange.rangeEnd - widthRange.rangeStart + 1;
                    totalWidths += rangeSize * widthRange.width;
                    characterCount += rangeSize;
                }
            }
            if (characterCount != 0)
            {
                averageWidth = totalWidths / characterCount;
            }
            if (averageWidth <= 0 || Float.isNaN(averageWidth))
            {
                averageWidth = getDefaultWidth();
            }
        }
        return averageWidth;
    }

    /**
     * Returns the CIDSystemInfo, or null if it is missing (which isn't allowed but could happen).
     */
    public PDCIDSystemInfo getCIDSystemInfo()
    {
        COSBase base = dict.getDictionaryObject(COSName.CIDSYSTEMINFO);
        if (base instanceof COSDictionary)
        {
            return new PDCIDSystemInfo((COSDictionary) base);
        }
        return null;
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
     * @throws java.io.IOException
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

    final int[] readCIDToGIDMap() throws IOException
    {
        int[] cid2gid = null;
        COSBase map = dict.getDictionaryObject(COSName.CID_TO_GID_MAP);
        if (map instanceof COSStream)
        {
            COSStream stream = (COSStream) map;

            InputStream is = stream.createInputStream();
            byte[] mapAsBytes = IOUtils.toByteArray(is);
            IOUtils.closeQuietly(is);
            int numberOfInts = mapAsBytes.length / 2;
            cid2gid = new int[numberOfInts];
            int offset = 0;
            for (int index = 0; index < numberOfInts; index++)
            {
                int gid = (mapAsBytes[offset] & 0xff) << 8 | mapAsBytes[offset + 1] & 0xff;
                cid2gid[index] = gid;
                offset += 2;
            }
        }
        return cid2gid;
    }

    private static class WidthRange
    {
        final int rangeStart;
        final int rangeEnd;
        final float width;

        public WidthRange(int start, int end, float width)
        {
            rangeStart = start;
            rangeEnd = end;
            this.width = width;
        }

        public boolean rangeMatches(int value)
        {
            return value >= rangeStart && value <= rangeEnd;
        }

        public float getWidth()
        {
            return width;
        }
    }
}
