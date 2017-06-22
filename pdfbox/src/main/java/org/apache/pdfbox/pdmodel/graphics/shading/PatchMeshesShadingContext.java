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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * This class is extended in Type6ShadingContext and Type7ShadingContext. This
 * was done as part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
abstract class PatchMeshesShadingContext extends TriangleBasedShadingContext
{
    private static final Log LOG = LogFactory.getLog(PatchMeshesShadingContext.class);

    /**
     * patch list
     */
    private List<Patch> patchList = new ArrayList<>();
    
    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param deviceBounds device bounds
     * @param controlPoints number of control points, 12 for type 6 shading and 16 for type 7 shading
     * @throws IOException if something went wrong
     */
    protected PatchMeshesShadingContext(PDShadingType6 shading, ColorModel colorModel,
            AffineTransform xform, Matrix matrix, Rectangle deviceBounds,
            int controlPoints) throws IOException
    {
        super(shading, colorModel, xform, matrix);
        patchList = collectPatches(shading, xform, matrix, controlPoints);
        createPixelTable(deviceBounds);
    }

    /**
     * Create a patch list from a data stream, the returned list contains all the patches contained
     * in the data stream.
     *
     * @param shadingType the shading type
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param controlPoints number of control points, 12 for type 6 shading and 16 for type 7 shading
     * @return the obtained patch list
     * @throws IOException when something went wrong
     */
    final List<Patch> collectPatches(PDShadingType6 shadingType, AffineTransform xform,
            Matrix matrix, int controlPoints) throws IOException
    {
        COSDictionary dict = shadingType.getCOSObject();
        int bitsPerFlag = shadingType.getBitsPerFlag();
        PDRange rangeX = shadingType.getDecodeForParameter(0);
        PDRange rangeY = shadingType.getDecodeForParameter(1);
        PDRange[] colRange = new PDRange[numberOfColorComponents];
        for (int i = 0; i < numberOfColorComponents; ++i)
        {
            colRange[i] = shadingType.getDecodeForParameter(2 + i);
            if (colRange[i] == null)
            {
                throw new IOException("Range missing in shading /Decode entry");
            }
        }
        List<Patch> list = new ArrayList<>();
        long maxSrcCoord = (long) Math.pow(2, bitsPerCoordinate) - 1;
        long maxSrcColor = (long) Math.pow(2, bitsPerColorComponent) - 1;
        COSStream cosStream = (COSStream) dict;

        try (ImageInputStream mciis = new MemoryCacheImageInputStream(cosStream.createInputStream()))
        {
            Point2D[] implicitEdge = new Point2D[4];
            float[][] implicitCornerColor = new float[2][numberOfColorComponents];
            byte flag = 0;

            try
            {
                flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
            }
            catch (EOFException ex)
            {
                LOG.error(ex);
            }

            boolean eof = false;
            while (!eof)
            {
                try
                {
                    boolean isFree = (flag == 0);
                    Patch current = readPatch(mciis, isFree, implicitEdge, implicitCornerColor,
                            maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange, matrix, xform, controlPoints);
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
                            LOG.warn("bad flag: " + flag);
                            break;
                    }
                }
                catch (EOFException ex)
                {
                    eof = true;
                }
            }
        }
        return list;
    }

    /**
     * Read a single patch from a data stream, a patch contains information of its coordinates and
     * color parameters.
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
                              float[][] implicitCornerColor, long maxSrcCoord, long maxSrcColor,
                              PDRange rangeX, PDRange rangeY, PDRange[] colRange, Matrix matrix,
                              AffineTransform xform, int controlPoints) throws IOException
    {
        float[][] color = new float[4][numberOfColorComponents];
        Point2D[] points = new Point2D[controlPoints];
        int pStart = 4, cStart = 2;
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
                long x = input.readBits(bitsPerCoordinate);
                long y = input.readBits(bitsPerCoordinate);
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
                    long c = input.readBits(bitsPerColorComponent);
                    color[i][j] = interpolate(c, maxSrcColor, colRange[j].getMin(),
                            colRange[j].getMax());
                }
            }
        }
        catch (EOFException ex)
        {
            LOG.debug("EOF");
            return null;
        }
        return generatePatch(points, color);
    }

    /**
     * Create a patch using control points and 4 corner color values, in
     * Type6ShadingContext, a CoonsPatch is returned; in Type6ShadingContext, a
     * TensorPatch is returned.
     *
     * @param points 12 or 16 control points
     * @param color 4 corner colors
     * @return a patch instance
     */
    abstract Patch generatePatch(Point2D[] points, float[][] color);

    /**
     * Get a point coordinate on a line by linear interpolation.
     */
    private float interpolate(float x, long maxValue, float rangeMin, float rangeMax)
    {
        return rangeMin + (x / maxValue) * (rangeMax - rangeMin);
    }

    @Override
    protected Map<Point, Integer> calcPixelTable(Rectangle deviceBounds)  throws IOException
    {
        Map<Point, Integer> map = new HashMap<>();
        for (Patch it : patchList)
        {
            super.calcPixelTable(it.listOfTriangles, map, deviceBounds);
        }
        return map;
    }

    @Override
    public void dispose()
    {
        patchList = null;
        super.dispose();
    }

    @Override
    protected boolean isDataEmpty()
    {
        return patchList.isEmpty();
    }    
}
