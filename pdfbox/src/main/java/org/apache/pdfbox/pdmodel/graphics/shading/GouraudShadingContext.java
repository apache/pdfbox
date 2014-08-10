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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * Shades Gouraud triangles for Type4ShadingContext and Type5ShadingContext.
 *
 * @author Andreas Lehmkühler
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
abstract class GouraudShadingContext extends TriangleBasedShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(GouraudShadingContext.class);

    /**
     * triangle list.
     */
    protected ArrayList<ShadedTriangle> triangleList;

    /**
     * background values.
     */
    protected float[] background;
    protected int rgbBackground;

    protected HashMap<Point, Integer> pixelTable;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * @throws IOException if something went wrong
     */
    protected GouraudShadingContext(PDShadingResources shading, ColorModel colorModel, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        super(shading, colorModel, xform, ctm, pageHeight, dBounds);
        triangleList = new ArrayList<ShadedTriangle>();
        LOG.debug("Background: " + shading.getBackground());
        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
            rgbBackground = convertToRGB(background);
        }
    }

    /**
     * Read a vertex from the bit input stream performs interpolations.
     *
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

    protected HashMap<Point, Integer> calcPixelTable()
    {
        HashMap<Point, Integer> map = new HashMap<Point, Integer>();
        super.calcPixelTable(triangleList, map);
        return map;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
        triangleList = null;
        outputColorModel = null;
        shadingColorSpace = null;
        shadingTinttransform = null;
    }

    /**
     * {@inheritDoc}
     */
    public final ColorModel getColorModel()
    {
        return outputColorModel;
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
    private float interpolate(float src, long srcMax, float dstMin, float dstMax)
    {
        return dstMin + (src * (dstMax - dstMin) / srcMax);
    }

    /**
     * {@inheritDoc}
     */
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
                    if (currentY < minBBoxY || currentY > maxBBoxY)
                    {
                        continue;
                    }
                }
                for (int col = 0; col < w; col++)
                {
                    int currentX = x + col;
                    if (bboxRect != null)
                    {
                        if (currentX < minBBoxX || currentX > maxBBoxX)
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
