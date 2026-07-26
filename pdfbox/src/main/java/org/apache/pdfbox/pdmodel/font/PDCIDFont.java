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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.fontbox.util.BoundingBox;
import org.apache.logging.log4j.LogManager;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * A CIDFont. A CIDFont is a PDF object that contains information about a CIDFont program. Although
 * its Type value is Font, a CIDFont is not actually a font.
 *
 * <p>It is not usually necessary to use this class directly, prefer {@link PDType0Font}.
 *
 * @author Ben Litchfield
 */
public abstract class PDCIDFont implements COSObjectable
{
    private static final Logger LOG = LogManager.getLogger(PDCIDFont.class);

    private final Map<Integer, Float> widths = new HashMap<>();
    private float defaultWidth;
    private float averageWidth;

    // vertical displacement, individual values
    private final Map<Integer, Float> verticalDisplacementY = new HashMap<>();
    // position vectors, individual values
    private final Map<Integer, Vector> positionVectors = new HashMap<>();
    // cid-ranges for verticalDisplacements and positionVectors
    private final List<VerticalDisplacementRange> displacementRanges = new ArrayList<>();

    private final float[] dw2 = { 880, -1000 };

    protected final COSDictionary dict;
    protected boolean isEmbedded;
    protected boolean isDamaged;

    private PDFontDescriptor fontDescriptor;

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    PDCIDFont(COSDictionary fontDictionary)
    {
        this.dict = fontDictionary;
        readWidths();
        readVerticalDisplacements();
    }

    private void readWidths()
    {
        // see 9.7.4.3, "Glyph Metrics in CIDFonts"
        COSArray wArray = dict.getCOSArray(COSName.W);
        if (wArray != null)
        {
            int size = wArray.size();
            int counter = 0;
            while (counter < size - 1)
            {
                COSBase firstCodeBase = wArray.getObject(counter++);
                if (!(firstCodeBase instanceof COSNumber))
                {
                    LOG.warn("Expected a number array member, got {}", firstCodeBase);
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
                            LOG.warn("Expected a number array member, got {}", widthBase);
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
                    if (!(secondCodeBase instanceof COSNumber) || !(rangeWidthBase instanceof COSNumber))
                    {
                        LOG.warn("Expected two numbers, got {} and {}", secondCodeBase,
                                rangeWidthBase);
                        continue;
                    }
                    COSNumber secondCode = (COSNumber) secondCodeBase;
                    COSNumber rangeWidth = (COSNumber) rangeWidthBase;
                    int startRange = firstCode.intValue();
                    int endRange = secondCode.intValue();
                    float width = rangeWidth.floatValue();
                    for (int i = startRange; i <= endRange; i++)
                    {
                        widths.put(i, width);
                    }
                }
            }
        }
    }

    private void readVerticalDisplacements()
    {
        // default position vector and vertical displacement vector
        COSArray dw2Array = dict.getCOSArray(COSName.DW2);
        if (dw2Array != null)
        {
            COSBase base0 = dw2Array.getObject(0);
            COSBase base1 = dw2Array.getObject(1);
            if (base0 instanceof COSNumber && base1 instanceof COSNumber)
            {
                dw2[0] = ((COSNumber) base0).floatValue();
                dw2[1] = ((COSNumber) base1).floatValue();
            }
        }

        // vertical metrics for individual CIDs.
        COSArray w2Array = dict.getCOSArray(COSName.W2);
        if (w2Array != null)
        {
            for (int i = 0; i < w2Array.size(); i++)
            {
                COSNumber c = (COSNumber) w2Array.getObject(i);
                COSBase next = w2Array.getObject(++i);
                if (next instanceof COSArray)
                {
                    COSArray array = (COSArray)next;
                    for (int j = 0; j < array.size(); j++)
                    {
                        int cid = c.intValue() + j / 3;
                        COSNumber w1y = (COSNumber) array.getObject(j);
                        COSNumber v1x = (COSNumber) array.getObject(++j);
                        COSNumber v1y = (COSNumber) array.getObject(++j);
                        verticalDisplacementY.put(cid, w1y.floatValue());
                        positionVectors.put(cid, new Vector(v1x.floatValue(), v1y.floatValue()));
                    }
                }
                else
                {
                    int first = c.intValue();
                    int last = ((COSNumber) next).intValue();
                    COSNumber w1y = (COSNumber) w2Array.getObject(++i);
                    COSNumber v1x = (COSNumber) w2Array.getObject(++i);
                    COSNumber v1y = (COSNumber) w2Array.getObject(++i);
                    displacementRanges.add(new VerticalDisplacementRange(first, last,
                            new Vector(v1x.floatValue(), v1y.floatValue()), w1y.floatValue()));
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
     * Returns the font descriptor, may be null.
     * 
     * @return the font descriptor or null
     */
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
     * Returns the font matrix, which represents the transformation from glyph space to text space.
     * 
     * @return the font matrix
     */
    protected abstract Matrix getFontMatrix();

    /**
     * Returns the font's bounding box.
     * 
     * @return the bounding box
     * 
     * @throws IOException if the bounding box could not be read
     */
    protected abstract BoundingBox getBoundingBox() throws IOException;

    /**
     * Returns the width of a glyph in the embedded font file.
     *
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return width in glyph space
     * @throws IOException if the font could not be read
     */
    protected abstract float getWidthFromFont(int code, PDType0Font parent) throws IOException;

    /**
     * Returns the height of the given character, in glyph space. This can be expensive to calculate. Results are only
     * approximate.
     * 
     * Warning: This method is deprecated in PDFBox 2.0 because there is no meaningful value which it can return, see
     * {@link PDFontLike#getHeight(int)}
     * 
     * @param code character code
     * @param parent the parent Type0 font.
     * @return the height of the given character
     * @throws IOException if the height could not be read
     */
    @Deprecated
    protected abstract float getHeight(int code, PDType0Font parent) throws IOException;

    /**
     * Returns the glyph path for the given character code.
     *
     * @param code character code in a PDF. Not to be confused with unicode.
     * @param parent the parent Type0 font.
     * 
     * @return the glyph path for the given character code
     * @throws java.io.IOException if the font could not be read
     */
    protected abstract GeneralPath getPath(int code, PDType0Font parent) throws IOException;

    /**
     * Returns the normalized glyph path for the given character code in a PDF. The resulting path is normalized to the
     * PostScript 1000 unit square, and fallback glyphs are returned where appropriate, e.g. for missing glyphs.
     *
     * @param code character code in a PDF. Not to be confused with unicode.
     * @param parent the parent Type0 font.
     * 
     * @return the normalized glyph path for the given character code
     * @throws java.io.IOException if the font could not be read
     */
    protected abstract GeneralPath getNormalizedPath(int code, PDType0Font parent)
            throws IOException;

    /**
     * Returns true if this font contains a glyph for the given character code in a PDF.
     *
     * @param code character code in a PDF. Not to be confused with unicode.
     * @param parent the parent Type0 font.
     * 
     * @return true if this font contains a glyph for the given character code
     * @throws java.io.IOException if the font could not be read
     */
    protected abstract boolean hasGlyph(int code, PDType0Font parent) throws IOException;

    /**
     * Returns true if the font file is embedded in the PDF.
     * 
     * @return true if the font file is embedded in the PDF
     */
    public boolean isEmbedded()
    {
        return isEmbedded;
    }

    /**
     * Returns true if the embedded font file is damaged.
     * 
     * @return true if the embedded font file is damaged
     */
    public boolean isDamaged()
    {
        return isDamaged;
    }

    /**
     * This will get the default width. The default value for the default width is 1000.
     *
     * @return The default width for the glyphs in this font.
     */
    private float getDefaultWidth()
    {
        if (Float.compare(defaultWidth, 0) == 0)
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
            width = getDefaultWidth();
        }
        return width;
    }

    /**
     * Returns true if the Font dictionary specifies an explicit width for the given glyph. This includes Width, W but
     * not default widths entries.
     * 
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return true if the Font dictionary specifies an explicit width for the given glyph
     * @throws IOException if the font could not be read
     */
    protected boolean hasExplicitWidth(int code, PDType0Font parent) throws IOException
    {
        return widths.get(codeToCID(code, parent)) != null;
    }

    /**
     * Returns the position vector (v), in text space, for the given character. This represents the position of vertical
     * origin relative to horizontal origin, for horizontal writing it will always be (0, 0). For vertical writing both
     * x and y are set.
     *
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return position vector
     */
    protected Vector getPositionVector(int code, PDType0Font parent)
    {
        int cid = codeToCID(code, parent);
        Vector v = positionVectors.get(cid);
        if (v == null)
        {
            VerticalDisplacementRange vdRange = displacementRanges.stream() //
                    .filter(vdr -> vdr.rangeMatches(cid)) //
                    .findFirst().orElse(null);
            if (vdRange != null)
            {
                v = vdRange.getPositionVector();
            }
            else
            {
                v = getDefaultPositionVector(cid);
            }
        }
        return v;
    }

    /**
     * Returns the y-component of the vertical displacement vector (w1).
     *
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return w1y
     */
    protected float getVerticalDisplacementVectorY(int code, PDType0Font parent)
    {
        int cid = codeToCID(code, parent);
        Float w1y = verticalDisplacementY.get(cid);
        if (w1y == null)
        {
            VerticalDisplacementRange vdRange = displacementRanges.stream() //
                    .filter(vdr -> vdr.rangeMatches(cid)) //
                    .findFirst().orElse(null);
            if (vdRange != null)
            {
                w1y = vdRange.getVerticalDisplacement();
            }
            else
            {
                w1y = dw2[1];
            }
        }
        return w1y;
    }

    /**
     * Returns the advance width of the given character, in glyph space.
     * <p>
     * 
     * If you want the visual bounds of the glyph then call getPath(..) on the appropriate PDFont subclass to retrieve
     * the glyph outline as a GeneralPath instead. See the cyan rectangles in the <b>DrawPrintTextLocations.java</b>
     * example to see this in action.
     *
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return the width of the given character
     * @throws IOException if the width could not be read
     */
    protected float getWidth(int code, PDType0Font parent) throws IOException
    {
        // these widths are supposed to be consistent with the actual widths given in the CIDFont
        // program, but PDFBOX-563 shows that when they are not, Acrobat overrides the embedded
        // font widths with the widths given in the font dictionary
        return getWidthForCID(codeToCID(code, parent));
    }

    // todo: this method is highly suspicious, the average glyph width is not usually a good metric
    public float getAverageFontWidth()
    {
        if (Float.compare(averageWidth, 0) == 0)
        {
            float totalWidths = 0.0f;
            int characterCount = 0;
            if (!widths.isEmpty())
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
     * 
     * @return the CIDSystemInfo, or null
     */
    public PDCIDSystemInfo getCIDSystemInfo()
    {
        COSDictionary cidSystemInfo = dict.getCOSDictionary(COSName.CIDSYSTEMINFO);
        return cidSystemInfo != null ? new PDCIDSystemInfo(cidSystemInfo) : null;
    }
    
    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return CID
     */
    protected abstract int codeToCID(int code, PDType0Font parent);

    /**
     * Returns the GID for the given character code.
     *
     * @param code character code
     * @param parent the parent Type0 font.
     * 
     * @return GID
     * @throws java.io.IOException if the mapping could not be read
     */
    protected abstract int codeToGID(int code, PDType0Font parent) throws IOException;

    protected abstract byte[] encodeGlyphId(int glyphId);

    /**
     * Encodes the given Unicode code point for use in a PDF content stream. Content streams use a multi-byte encoding
     * with 1 to 4 bytes.
     *
     * <p>
     * This method is called when embedding text in PDFs and when filling in fields.
     *
     * @param unicode Unicode code point.
     * @param parent the parent Type0 font.
     * 
     * @return Array of 1 to 4 PDF content stream bytes.
     * @throws IOException If the text could not be encoded.
     */
    protected byte[] encode(int unicode, PDType0Font parent) throws IOException
    {
        if (this instanceof PDCIDFontType0)
        {
            // todo: we can use a known character collection CMap for a CIDFont
            // and an Encoding for Type 1-equivalent
            throw new UnsupportedOperationException();
        }
        return ((PDCIDFontType2) this).encode(unicode, parent);
    }

    final int[] readCIDToGIDMap() throws IOException
    {
        int[] cid2gid = null;
        COSStream stream = dict.getCOSStream(COSName.CID_TO_GID_MAP);
        if (stream != null)
        {
            byte[] mapAsBytes;
            try (InputStream is = stream.createInputStream())
            {
                mapAsBytes = is.readAllBytes();
            }
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

    private static class VerticalDisplacementRange
    {
        final int rangeStart;
        final int rangeEnd;
        final Vector positionVector;
        final float verticalDisplacment;

        public VerticalDisplacementRange(int start, int end, Vector vector, float displacement)
        {
            rangeStart = start;
            rangeEnd = end;
            positionVector = vector;
            verticalDisplacment = displacement;
        }

        public boolean rangeMatches(int value)
        {
            return value >= rangeStart && value <= rangeEnd;
        }

        public Vector getPositionVector()
        {
            return positionVector;
        }

        public float getVerticalDisplacement()
        {
            return verticalDisplacment;
        }
    }
}
