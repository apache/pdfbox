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
import java.util.Collections;
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
    protected PatchMeshesShadingContext(final PDShadingType6 shading, final ColorModel colorModel,
                                        final AffineTransform xform, final Matrix matrix, final Rectangle deviceBounds,
                                        final int controlPoints) throws IOException
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
    @SuppressWarnings({"squid:S2583","squid:S1166"})
    final List<Patch> collectPatches(final PDShadingType6 shadingType, final AffineTransform xform,
                                     final Matrix matrix, final int controlPoints) throws IOException
    {
        final COSDictionary dict = shadingType.getCOSObject();
        if (!(dict instanceof COSStream))
        {
            return Collections.emptyList();
        }
        final PDRange rangeX = shadingType.getDecodeForParameter(0);
        final PDRange rangeY = shadingType.getDecodeForParameter(1);
        if (Float.compare(rangeX.getMin(), rangeX.getMax()) == 0 ||
            Float.compare(rangeY.getMin(), rangeY.getMax()) == 0)
        {
            return Collections.emptyList();
        }
        final int bitsPerFlag = shadingType.getBitsPerFlag();
        final PDRange[] colRange = new PDRange[numberOfColorComponents];
        for (int i = 0; i < numberOfColorComponents; ++i)
        {
            colRange[i] = shadingType.getDecodeForParameter(2 + i);
            if (colRange[i] == null)
            {
                throw new IOException("Range missing in shading /Decode entry");
            }
        }
        final List<Patch> list = new ArrayList<>();
        final long maxSrcCoord = (long) Math.pow(2, bitsPerCoordinate) - 1;
        final long maxSrcColor = (long) Math.pow(2, bitsPerColorComponent) - 1;
        final COSStream cosStream = (COSStream) dict;

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
                    final boolean isFree = (flag == 0);
                    final Patch current = readPatch(mciis, isFree, implicitEdge, implicitCornerColor,
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
    protected Patch readPatch(final ImageInputStream input, final boolean isFree, final Point2D[] implicitEdge,
                              final float[][] implicitCornerColor, final long maxSrcCoord, final long maxSrcColor,
                              final PDRange rangeX, final PDRange rangeY, final PDRange[] colRange, final Matrix matrix,
                              final AffineTransform xform, final int controlPoints) throws IOException
    {
        final float[][] color = new float[4][numberOfColorComponents];
        final Point2D[] points = new Point2D[controlPoints];
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
                final long x = input.readBits(bitsPerCoordinate);
                final long y = input.readBits(bitsPerCoordinate);
                final float px = interpolate(x, maxSrcCoord, rangeX.getMin(), rangeX.getMax());
                final float py = interpolate(y, maxSrcCoord, rangeY.getMin(), rangeY.getMax());
                final Point2D p = matrix.transformPoint(px, py);
                xform.transform(p, p);
                points[i] = p;
            }
            for (int i = cStart; i < 4; i++)
            {
                for (int j = 0; j < numberOfColorComponents; j++)
                {
                    final long c = input.readBits(bitsPerColorComponent);
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
    private float interpolate(final float x, final long maxValue, final float rangeMin, final float rangeMax)
    {
        return rangeMin + (x / maxValue) * (rangeMax - rangeMin);
    }

    @Override
    protected Map<Point, Integer> calcPixelTable(final Rectangle deviceBounds)  throws IOException
    {
        final Map<Point, Integer> map = new HashMap<>();
        for (final Patch it : patchList)
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
