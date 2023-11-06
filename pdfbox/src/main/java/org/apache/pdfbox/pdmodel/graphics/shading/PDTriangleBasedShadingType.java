/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.graphics.shading;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * Common resources for shading types 4,5,6 and 7
 */
abstract class PDTriangleBasedShadingType extends PDShading
{
    // an array of 2^n numbers specifying the linear mapping of sample values
    // into the range appropriate for the function's output values. Default
    // value: same as the value of Range
    private COSArray decode = null;

    private static final Logger LOG = LogManager.getLogger(PDTriangleBasedShadingType.class);

    private int bitsPerCoordinate = -1;
    private int bitsPerColorComponent = -1;
    private int numberOfColorComponents = -1;

    PDTriangleBasedShadingType(COSDictionary shadingDictionary)
    {
        super(shadingDictionary);
    }

    /**
     * The bits per component of this shading. This will return -1 if one has not been set.
     *
     * @return the number of bits per component
     */
    public int getBitsPerComponent()
    {
        if (bitsPerColorComponent == -1)
        {
            bitsPerColorComponent = getCOSObject().getInt(COSName.BITS_PER_COMPONENT, -1);
            LOG.debug("bitsPerColorComponent: {}", bitsPerColorComponent);
        }
        return bitsPerColorComponent;
    }

    /**
     * Set the number of bits per component.
     *
     * @param bitsPerComponent the number of bits per component
     */
    public void setBitsPerComponent(int bitsPerComponent)
    {
        getCOSObject().setInt(COSName.BITS_PER_COMPONENT, bitsPerComponent);
        bitsPerColorComponent = bitsPerComponent;
    }

    /**
     * The bits per coordinate of this shading. This will return -1 if one has
     * not been set.
     *
     * @return the number of bits per coordinate
     */
    public int getBitsPerCoordinate()
    {
        if (bitsPerCoordinate == -1)
        {
            bitsPerCoordinate = getCOSObject().getInt(COSName.BITS_PER_COORDINATE, -1);
            LOG.debug("bitsPerCoordinate: {}", Math.pow(2, bitsPerCoordinate) - 1);
        }
        return bitsPerCoordinate;
    }

    /**
     * Set the number of bits per coordinate.
     *
     * @param bitsPerCoordinate the number of bits per coordinate
     */
    public void setBitsPerCoordinate(int bitsPerCoordinate)
    {
        getCOSObject().setInt(COSName.BITS_PER_COORDINATE, bitsPerCoordinate);
        this.bitsPerCoordinate = bitsPerCoordinate;
    }
    
    /**
     * The number of color components of this shading.
     *
     * @return number of color components of this shading
     * 
     * @throws IOException if the data could not be read
     */
    public int getNumberOfColorComponents() throws IOException
    {
        if (numberOfColorComponents == -1)
        {
            numberOfColorComponents = getFunction() != null ? 1
                    : getColorSpace().getNumberOfComponents();
            LOG.debug("numberOfColorComponents: {}", numberOfColorComponents);
        }
        return numberOfColorComponents;
    }

    /**
     * Returns all decode values as COSArray.
     *
     * @return the decode array
     */
    private COSArray getDecodeValues()
    {
        if (decode == null)
        {
            decode = getCOSObject().getCOSArray(COSName.DECODE);
        }
        return decode;
    }

    /**
     * This will set the decode values.
     *
     * @param decodeValues the new decode values
     */
    public void setDecodeValues(COSArray decodeValues)
    {
        decode = decodeValues;
        getCOSObject().setItem(COSName.DECODE, decodeValues);
    }

    /**
     * Get the decode for the input parameter.
     *
     * @param paramNum the function parameter number
     * @return the decode parameter range or null if none is set
     */
    public PDRange getDecodeForParameter(int paramNum)
    {
        PDRange retval = null;
        COSArray decodeValues = getDecodeValues();
        if (decodeValues != null && decodeValues.size() >= paramNum * 2 + 1)
        {
            retval = new PDRange(decodeValues, paramNum);
        }
        return retval;
    }
    
    /**
     * Calculate the interpolation, see p.345 pdf spec 1.7.
     *
     * @param src src value
     * @param srcMax max src value (2^bits-1)
     * @param dstMin min dst value
     * @param dstMax max dst value
     * @return interpolated value
     */
    protected float interpolate(float src, long srcMax, float dstMin, float dstMax)
    {
        return dstMin + (src * (dstMax - dstMin) / srcMax);
    }
    
    /**
     * Read a vertex from the bit input stream performs interpolations.
     *
     * @param input bit input stream
     * @param maxSrcCoord max value for source coordinate (2^bits-1)
     * @param maxSrcColor max value for source color (2^bits-1)
     * @param rangeX dest range for X
     * @param rangeY dest range for Y
     * @param colRangeTab dest range array for colors
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param xform the affine transformation
     * @return a new vertex with the flag and the interpolated values
     * @throws IOException if something went wrong
     */
    protected Vertex readVertex(ImageInputStream input, long maxSrcCoord, long maxSrcColor,
                                PDRange rangeX, PDRange rangeY, PDRange[] colRangeTab,
                                Matrix matrix, AffineTransform xform) throws IOException
    {
        float[] colorComponentTab = new float[numberOfColorComponents];
        long x = input.readBits(bitsPerCoordinate);
        long y = input.readBits(bitsPerCoordinate);
        float dstX = interpolate(x, maxSrcCoord, rangeX.getMin(), rangeX.getMax());
        float dstY = interpolate(y, maxSrcCoord, rangeY.getMin(), rangeY.getMax());
        LOG.debug("coord: {}", () -> String.format("[%06X,%06X] -> [%f,%f]", x, y, dstX, dstY));
        Point2D p = matrix.transformPoint(dstX, dstY);
        xform.transform(p, p);

        for (int n = 0; n < numberOfColorComponents; ++n)
        {
            int color = (int) input.readBits(bitsPerColorComponent);
            colorComponentTab[n] = interpolate(color, maxSrcColor, colRangeTab[n].getMin(),
                    colRangeTab[n].getMax());
            if (LOG.isDebugEnabled())
            {
                LOG.debug("color[{}]: {}/{}-> color[{}]: {}", n, color,
                        String.format("%02x", color), n,
                        colorComponentTab[n]);
            }
        }

        // "Each set of vertex data shall occupy a whole number of bytes.
        // If the total number of bits required is not divisible by 8, the last data byte
        // for each vertex is padded at the end with extra bits, which shall be ignored."
        int bitOffset = input.getBitOffset();
        if (bitOffset != 0)
        {
            input.readBits(8 - bitOffset);
        }

        return new Vertex(p, colorComponentTab);
    }

    abstract List<ShadedTriangle> collectTriangles(AffineTransform xform, Matrix matrix) throws IOException;
    
    @Override
    public Rectangle2D getBounds(AffineTransform xform, Matrix matrix) throws IOException
    {
        Rectangle2D bounds = null;
        for (ShadedTriangle shadedTriangle : collectTriangles(xform, matrix))
        {
            if (bounds == null)
            {
                bounds = new Rectangle2D.Double(shadedTriangle.corner[0].getX(),
                        shadedTriangle.corner[0].getY(), 0, 0);
            }
            bounds.add(shadedTriangle.corner[0]);
            bounds.add(shadedTriangle.corner[1]);
            bounds.add(shadedTriangle.corner[2]);
        }
        if (bounds == null)
        {
            // Speeds up files where triangles are empty, e.g. ghostscript file 690425
            return new Rectangle2D.Float();
        }
        return bounds;
    }
}
