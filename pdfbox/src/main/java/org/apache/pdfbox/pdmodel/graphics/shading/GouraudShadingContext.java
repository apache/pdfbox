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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * Shades Gouraud triangles for Type4ShadingContext and Type5ShadingContext.
 *
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
abstract class GouraudShadingContext extends TriangleBasedShadingContext
{
    private static final Log LOG = LogFactory.getLog(GouraudShadingContext.class);

    /**
     * triangle list.
     */
    private List<ShadedTriangle> triangleList = new ArrayList<>();

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @throws IOException if something went wrong
     */
    protected GouraudShadingContext(PDShading shading, ColorModel colorModel, AffineTransform xform,
                                    Matrix matrix) throws IOException
    {
        super(shading, colorModel, xform, matrix);
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
        LOG.debug("coord: " + String.format("[%06X,%06X] -> [%f,%f]", x, y, dstX, dstY));
        Point2D p = matrix.transformPoint(dstX, dstY);
        xform.transform(p, p);

        for (int n = 0; n < numberOfColorComponents; ++n)
        {
            int color = (int) input.readBits(bitsPerColorComponent);
            colorComponentTab[n] = interpolate(color, maxSrcColor, colRangeTab[n].getMin(),
                    colRangeTab[n].getMax());
            LOG.debug("color[" + n + "]: " + color + "/" + String.format("%02x", color)
                    + "-> color[" + n + "]: " + colorComponentTab[n]);
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

    void setTriangleList(List<ShadedTriangle> triangleList)
    {
        this.triangleList = triangleList;
    }

    @Override
    protected Map<Point, Integer> calcPixelTable(Rectangle deviceBounds) throws IOException
    {
        Map<Point, Integer> map = new HashMap<>();
        super.calcPixelTable(triangleList, map, deviceBounds);
        return map;
    }

    @Override
    public void dispose()
    {
        triangleList = null;
        super.dispose();
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

    @Override
    protected boolean isDataEmpty()
    {
        return triangleList.isEmpty();
    }
}
