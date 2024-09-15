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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * Common resources for shading types 6 and 7
 */
abstract class PDMeshBasedShadingType extends PDShadingType4
{

    private static final Logger LOG = LogManager.getLogger(PDMeshBasedShadingType.class);

    PDMeshBasedShadingType(COSDictionary shadingDictionary)
    {
        super(shadingDictionary);
    }

    /**
     * Create a patch list from a data stream, the returned list contains all the patches contained in the data stream.
     *
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param controlPoints number of control points, 12 for type 6 shading and 16 for type 7 shading
     * @return the obtained patch list
     * @throws IOException when something went wrong
     */
    @SuppressWarnings({ "squid:S2583", "squid:S1166" })
    final List<Patch> collectPatches(AffineTransform xform, Matrix matrix, int controlPoints)
            throws IOException
    {
        COSDictionary dict = getCOSObject();
        if (!(dict instanceof COSStream))
        {
            return Collections.emptyList();
        }
        PDRange rangeX = getDecodeForParameter(0);
        PDRange rangeY = getDecodeForParameter(1);
        if (rangeX == null || rangeY == null ||
            Float.compare(rangeX.getMin(), rangeX.getMax()) == 0 ||
            Float.compare(rangeY.getMin(), rangeY.getMax()) == 0)
        {
            return Collections.emptyList();
        }
        int bitsPerFlag = getBitsPerFlag();
        PDRange[] colRange = new PDRange[getNumberOfColorComponents()];
        for (int i = 0; i < colRange.length; ++i)
        {
            colRange[i] = getDecodeForParameter(2 + i);
            if (colRange[i] == null)
            {
                throw new IOException("Range missing in shading /Decode entry");
            }
        }
        List<Patch> list = new ArrayList<>();
        long maxSrcCoord = (long) Math.pow(2, getBitsPerCoordinate()) - 1;
        long maxSrcColor = (long) Math.pow(2, getBitsPerComponent()) - 1;

        // MemoryCacheImageInputStream doesn't close the wrapped stream
        try (InputStream imageStream = ((COSStream) dict).createInputStream())
        {
            try (ImageInputStream mciis = new MemoryCacheImageInputStream(imageStream))
            {
                Point2D[] implicitEdge = new Point2D[4];
                float[][] implicitCornerColor = new float[2][colRange.length];
                byte flag = 0;
    
                try
                {
                    flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                }
                catch (EOFException ex)
                {
                    LOG.error(ex);
                    return list;
                }
    
                boolean eof = false;
                while (!eof)
                {
                    try
                    {
                        boolean isFree = (flag == 0);
                        Patch current = readPatch(mciis, isFree, implicitEdge, implicitCornerColor,
                                maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange, matrix, xform,
                                controlPoints);
                        if (current == null)
                        {
                            break;
                        }
                        list.add(current);
                        flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                        switch (flag)
                        {
                        case 0:
                            break;
                        case 1:
                            implicitEdge = current.getFlag1Edge();
                            implicitCornerColor = current.getFlag1Color();
                            break;
                        case 2:
                            implicitEdge = current.getFlag2Edge();
                            implicitCornerColor = current.getFlag2Color();
                            break;
                        case 3:
                            implicitEdge = current.getFlag3Edge();
                            implicitCornerColor = current.getFlag3Color();
                            break;
                        default:
                            LOG.warn("bad flag: {}", flag);
                            break;
                        }
                    }
                    catch (EOFException ex)
                    {
                        eof = true;
                    }
                }
            }
        }
        return list;
    }

    /**
     * Read a single patch from a data stream, a patch contains information of its coordinates and color parameters.
     *
     * @param input the image source data stream
     * @param isFree whether this is a free patch
     * @param implicitEdge implicit edge when a patch is not free, otherwise it's not used
     * @param implicitCornerColor implicit colors when a patch is not free, otherwise it's not used
     * @param maxSrcCoord the maximum coordinate value calculated from source data
     * @param maxSrcColor the maximum color value calculated from source data
     * @param rangeX range for coordinate x
     * @param rangeY range for coordinate y
     * @param colRange range for color
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param xform transformation for user to device space
     * @param controlPoints number of control points, 12 for type 6 shading and 16 for type 7 shading
     * @return a single patch
     * @throws IOException when something went wrong
     */
    protected Patch readPatch(ImageInputStream input, boolean isFree, Point2D[] implicitEdge,
            float[][] implicitCornerColor, long maxSrcCoord, long maxSrcColor, PDRange rangeX,
            PDRange rangeY, PDRange[] colRange, Matrix matrix, AffineTransform xform,
            int controlPoints) throws IOException
    {
        int numberOfColorComponents = getNumberOfColorComponents();
        float[][] color = new float[4][numberOfColorComponents];
        Point2D[] points = new Point2D[controlPoints];
        int pStart = 4;
        int cStart = 2;
        if (isFree)
        {
            pStart = 0;
            cStart = 0;
        }
        else
        {
            points[0] = implicitEdge[0];
            points[1] = implicitEdge[1];
            points[2] = implicitEdge[2];
            points[3] = implicitEdge[3];

            for (int i = 0; i < numberOfColorComponents; i++)
            {
                color[0][i] = implicitCornerColor[0][i];
                color[1][i] = implicitCornerColor[1][i];
            }
        }

        try
        {
            for (int i = pStart; i < controlPoints; i++)
            {
                long x = input.readBits(getBitsPerCoordinate());
                long y = input.readBits(getBitsPerCoordinate());
                float px = interpolate(x, maxSrcCoord, rangeX.getMin(), rangeX.getMax());
                float py = interpolate(y, maxSrcCoord, rangeY.getMin(), rangeY.getMax());
                Point2D p = matrix.transformPoint(px, py);
                xform.transform(p, p);
                points[i] = p;
            }
            for (int i = cStart; i < 4; i++)
            {
                for (int j = 0; j < numberOfColorComponents; j++)
                {
                    long c = input.readBits(getBitsPerComponent());
                    color[i][j] = interpolate(c, maxSrcColor, colRange[j].getMin(),
                            colRange[j].getMax());
                }
            }
        }
        catch (EOFException ex)
        {
            LOG.debug("EOF", ex);
            return null;
        }
        return generatePatch(points, color);
    }

    /**
     * Create a patch using control points and 4 corner color values, in Type6ShadingContext, a CoonsPatch is returned;
     * in Type6ShadingContext, a TensorPatch is returned.
     *
     * @param points 12 or 16 control points
     * @param color 4 corner colors
     * @return a patch instance
     */
    abstract Patch generatePatch(Point2D[] points, float[][] color);

    @Override
    public abstract Rectangle2D getBounds(AffineTransform xform, Matrix matrix) throws IOException;

    Rectangle2D getBounds(AffineTransform xform, Matrix matrix, int controlPoints)
            throws IOException
    {
        Rectangle2D bounds = null;
        for (Patch patch : collectPatches(xform, matrix, controlPoints))
        {
            for (ShadedTriangle shadedTriangle : patch.listOfTriangles)
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
        }
        return bounds;
    }
}
