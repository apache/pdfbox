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

package org.apache.pdfbox.pdmodel.graphics.shading;

import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.Matrix;

/**
 * Shades Gouraud triangles for Type4ShadingContext and Type5ShadingContext.
 * @author Andreas Lehmkühler
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
abstract class GouraudShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(GouraudShadingContext.class);

    private ColorModel outputColorModel;
    private PDColorSpace shadingColorSpace;
    private final Rectangle deviceBounds;

    /** number of color components. */
    protected int numberOfColorComponents;

    /** triangle list. */
    protected ArrayList<CoonsTriangle> triangleList;

    /** bits per coordinate. */
    protected int bitsPerCoordinate;

    /** bits per color component. */
    protected int bitsPerColorComponent;

    /** background values.*/
    protected float[] background;
    protected int rgbBackground;

    private final boolean hasFunction;
    protected final PDShading gouraudShadingType;
    private PDRectangle bboxRect;
    private float[] bboxTab = new float[4];
    
    protected HashMap<Point, Integer> pixelTable;

    /**
     * Constructor creates an instance to be used for fill operations.
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * @throws IOException if something went wrong
     */
    protected GouraudShadingContext(PDShading shading, ColorModel colorModel, AffineTransform xform,
                                    Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        gouraudShadingType = shading;
        deviceBounds = dBounds;
        triangleList = new ArrayList<CoonsTriangle>();
        hasFunction = shading.getFunction() != null;

        shadingColorSpace = shading.getColorSpace();
        LOG.debug("colorSpace: " + shadingColorSpace);
        numberOfColorComponents = hasFunction ? 1 : shadingColorSpace.getNumberOfComponents();

        bboxRect = shading.getBBox();
        if (bboxRect != null)
        {
            bboxTab[0] = bboxRect.getLowerLeftX();
            bboxTab[1] = bboxRect.getLowerLeftY();
            bboxTab[2] = bboxRect.getUpperRightX();
            bboxTab[3] = bboxRect.getUpperRightY();
            if (ctm != null)
            {
                // transform the coords using the given matrix
                ctm.createAffineTransform().transform(bboxTab, 0, bboxTab, 0, 2);
            }
            xform.transform(bboxTab, 0, bboxTab, 0, 2);
        }
        reOrder(bboxTab, 0, 2);
        reOrder(bboxTab, 1, 3);
        LOG.debug("BBox: " + shading.getBBox());
        LOG.debug("Background: " + shading.getBackground());
        if (bboxTab[0] >= bboxTab[2] || bboxTab[1] >= bboxTab[3])
        {
            bboxRect = null;
        }

        // create the output color model using RGB+alpha as color space
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
            rgbBackground = convertToRGB(background);
        }
    }

    /**
     * Read a vertex from the bit input stream performs interpolations.
     * @param input bit input stream
     * @param flag the flag or any value if not relevant
     * @param maxSrcCoord max value for source coordinate (2^bits-1)
     * @param maxSrcColor max value for source color (2^bits-1)
     * @param rangeX dest range for X
     * @param rangeY dest range for Y
     * @param colRangeTab dest range array for colors
     * @return a new vertex with the flag and the interpolated values
     * @throws IOException if something went wrong
     */
    protected Vertex readVertex(ImageInputStream input, long maxSrcCoord, long maxSrcColor,
                                PDRange rangeX, PDRange rangeY, PDRange[] colRangeTab, Matrix ctm, 
                                AffineTransform xform) throws IOException
    {
        float[] colorComponentTab = new float[numberOfColorComponents];
        long x = input.readBits(bitsPerCoordinate);
        long y = input.readBits(bitsPerCoordinate);
        double dstX = interpolate(x, maxSrcCoord, rangeX.getMin(), rangeX.getMax());
        double dstY = interpolate(y, maxSrcCoord, rangeY.getMin(), rangeY.getMax());
        LOG.debug("coord: " + String.format("[%06X,%06X] -> [%f,%f]", x, y, dstX, dstY));
        Point2D tmp = new Point2D.Double(dstX, dstY);
        transformPoint(tmp, ctm, xform);
        
        for (int n = 0; n < numberOfColorComponents; ++n)
        {
            int color = (int) input.readBits(bitsPerColorComponent);
            colorComponentTab[n] = (float) interpolate(color, maxSrcColor, colRangeTab[n].getMin(), colRangeTab[n].getMax());
            LOG.debug("color[" + n + "]: " + color + "/" + String.format("%02x", color)
                    + "-> color[" + n + "]: " + colorComponentTab[n]);
        }
        return new Vertex(tmp, colorComponentTab);
    }
    
    // this method is used to arrange the array to denote the left upper corner and right lower corner of the BBox
    private void reOrder(float[] array, int i, int j)
    {
        if (i < j && array[i] <= array[j])
        {
        }
        else
        {
            float tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    // transform a point from source space to device space
    private void transformPoint(Point2D p, Matrix ctm, AffineTransform xform)
    {
        if (ctm != null)
        {
            ctm.createAffineTransform().transform(p, p);
        }
        xform.transform(p, p);
    }
    
    protected HashMap<Point, Integer> calcPixelTable()
    {
        HashMap<Point, Integer> map = new HashMap<Point, Integer>();
        for (CoonsTriangle tri : triangleList)
        {
            int degree = tri.getDeg();
            if (degree == 2)
            {
                Line line = tri.getLine();
                HashSet<Point> linePoints = line.linePoints;
                for (Point p : linePoints)
                {
                    float[] values = line.getColor(p);
                    map.put(p, convertToRGB(values));
                }
            }
            else
            {
                int[] boundary = tri.getBoundary();
                boundary[0] = Math.max(boundary[0], deviceBounds.x);
                boundary[1] = Math.min(boundary[1], deviceBounds.x + deviceBounds.width);
                boundary[2] = Math.max(boundary[2], deviceBounds.y);
                boundary[3] = Math.min(boundary[3], deviceBounds.y + deviceBounds.height);
                for (int x = boundary[0]; x <= boundary[1]; x++)
                {
                    for (int y = boundary[2]; y <= boundary[3]; y++)
                    {
                        Point p = new Point(x, y);
                        if (tri.contains(p))
                        {
                            float[] values = tri.getColor(p);
                            map.put(p, convertToRGB(values));
                        }
                    }
                }
            }
        }
        return map;
    }
    
    // convert color to RGB color values
    private int convertToRGB(float[] values)
    {
        float[] nValues = null;
        float[] rgbValues = null;
        int normRGBValues = 0;
        if (hasFunction)
        {
            try
            {
                nValues = gouraudShadingType.evalFunction(values);
            }
            catch (IOException exception)
            {
                LOG.error("error while processing a function", exception);
            }
        }

        try
        {
            if (nValues == null)
            {
                rgbValues = shadingColorSpace.toRGB(values);
            }
            else
            {
                rgbValues = shadingColorSpace.toRGB(nValues);
            }
            normRGBValues = (int) (rgbValues[0] * 255);
            normRGBValues |= (((int) (rgbValues[1] * 255)) << 8);
            normRGBValues |= (((int) (rgbValues[2] * 255)) << 16);
        }
        catch (IOException exception)
        {
            LOG.error("error processing color space", exception);
        }
        return normRGBValues;
    }
    
    @Override
    public void dispose()
    {
        triangleList = null;
        outputColorModel = null;
        shadingColorSpace = null;
    }

    @Override
    public final ColorModel getColorModel()
    {
        return outputColorModel;
    }

    /**
     * Calculate the interpolation, see p.345 pdf spec 1.7.
     * @param src src value
     * @param srcMax max src value (2^bits-1)
     * @param dstMin min dst value
     * @param dstMax max dst value
     * @return interpolated value
     */
    private float interpolate(float src, long srcMax, float dstMin, float dstMax)
    {
        return dstMin + (src * (dstMax - dstMin) / srcMax);
    }

    @Override
    public final Raster getRaster(int x, int y, int w, int h)
    {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        int[] data = new int[w * h * 4];
        if (!triangleList.isEmpty() || background != null)
        {
            for (int row = 0; row < h; row++)
            {
                int currentY = y + row;
                if (bboxRect != null)
                {
                    if (currentY < bboxTab[1] || currentY > bboxTab[3])
                    {
                        continue;
                    }
                }
                for (int col = 0; col < w; col++)
                {
                    int currentX = x + col;
                    if (bboxRect != null)
                    {
                        if (currentX < bboxTab[0] || currentX > bboxTab[2])
                        {
                            continue;
                        }
                    }
                    Point p = new Point(currentX, currentY);
                    int value;
                    if (pixelTable.containsKey(p))
                    {
                        value = pixelTable.get(p);
                    }
                    else
                    {
                        if (background != null)
                        {
                            value = rgbBackground;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    int index = (row * w + col) * 4;
                    data[index] = value & 255;
                    value >>= 8;
                    data[index + 1] = value & 255;
                    value >>= 8;
                    data[index + 2] = value & 255;
                    data[index + 3] = 255;
                }
            }
        }
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }
}
