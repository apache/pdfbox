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

import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
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
abstract class PatchMeshesShadingContext extends TriangleBasedShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(PatchMeshesShadingContext.class);

    protected float[] background; // background values.
    protected int rgbBackground;
    protected final PDShadingResources patchMeshesShadingType;

    // the following fields are not intialized in this abstract class
    protected ArrayList<Patch> patchList; // patch list
    protected int bitsPerFlag; // bits per flag
    protected HashMap<Point, Integer> pixelTable;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * @param dBounds device bounds
     * @throws IOException if something went wrong
     */
    protected PatchMeshesShadingContext(PDShadingResources shading, ColorModel colorModel, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        super(shading, colorModel, xform, ctm, pageHeight, dBounds);
        patchMeshesShadingType = shading;
        bitsPerFlag = ((PDShadingType6) shading).getBitsPerFlag();
        patchList = new ArrayList<Patch>();

        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
            rgbBackground = convertToRGB(background);
        }
    }

    /**
     * Create a patch list from a data stream, the returned list contains all
     * the patches contained in the data stream.
     *
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param cosDictionary dictionary object to give the image information
     * @param rangeX range for coordinate x
     * @param rangeY range for coordinate y
     * @param colRange range for color
     * @param numP number of control points, 12 for type 6 shading and 16 for
     * type 7 shading
     * @return the obtained patch list
     * @throws IOException when something went wrong
     */
    protected ArrayList<Patch> getPatchList(AffineTransform xform, Matrix ctm, COSDictionary cosDictionary,
            PDRange rangeX, PDRange rangeY, PDRange[] colRange, int numP) throws IOException
    {
        ArrayList<Patch> list = new ArrayList<Patch>();
        long maxSrcCoord = (long) Math.pow(2, bitsPerCoordinate) - 1;
        long maxSrcColor = (long) Math.pow(2, bitsPerColorComponent) - 1;
        COSStream cosStream = (COSStream) cosDictionary;

        ImageInputStream mciis = new MemoryCacheImageInputStream(cosStream.getUnfilteredStream());

        Point2D[] implicitEdge = new Point2D[4];
        float[][] implicitCornerColor = new float[2][numberOfColorComponents];

        byte flag = (byte) 0;

        try
        {
            flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
        }
        catch (EOFException ex)
        {
            LOG.error(ex);
        }

        while (true)
        {
            try
            {
                boolean isFree = (flag == 0);
                Patch current = readPatch(mciis, isFree, implicitEdge, implicitCornerColor,
                        maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange, ctm, xform, numP);
                if (current == null)
                {
                    break;
                }
                list.add((Patch) current);
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
                break;
            }
        }
        mciis.close();
        return list;
    }

    /**
     * Read a single patch from a data stream, a patch contains information of
     * its coordinates and color parameters.
     *
     * @param input the image source data stream
     * @param isFree whether this is a free patch
     * @param implicitEdge implicit edge when a patch is not free, otherwise
     * it's not used
     * @param implicitCornerColor implicit colors when a patch is not free,
     * otherwise it's not used
     * @param maxSrcCoord the maximum coordinate value calculated from source
     * data
     * @param maxSrcColor the maximum color value calculated from source data
     * @param rangeX range for coordinate x
     * @param rangeY range for coordinate y
     * @param colRange range for color
     * @param ctm current transformation matrix
     * @param xform transformation for user to device space
     * @param numP number of control points, 12 for type 6 shading and 16 for
     * type 7 shading
     * @return a single patch
     * @throws IOException when something went wrong
     */
    protected Patch readPatch(ImageInputStream input, boolean isFree, Point2D[] implicitEdge,
            float[][] implicitCornerColor, long maxSrcCoord, long maxSrcColor,
            PDRange rangeX, PDRange rangeY, PDRange[] colRange,
            Matrix ctm, AffineTransform xform, int numP) throws IOException
    {
        float[][] color = new float[4][numberOfColorComponents];
        Point2D[] points = new Point2D[numP];
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
            for (int i = pStart; i < numP; i++)
            {
                long x = input.readBits(bitsPerCoordinate);
                long y = input.readBits(bitsPerCoordinate);
                double px = interpolate(x, maxSrcCoord, rangeX.getMin(), rangeX.getMax());
                double py = interpolate(y, maxSrcCoord, rangeY.getMin(), rangeY.getMax());
                Point2D tmp = new Point2D.Double(px, py);
                transformPoint(tmp, ctm, xform);
                points[i] = tmp;
            }
            for (int i = cStart; i < 4; i++)
            {
                for (int j = 0; j < numberOfColorComponents; j++)
                {
                    long c = input.readBits(bitsPerColorComponent);
                    color[i][j] = (float) interpolate(c, maxSrcColor, colRange[j].getMin(), colRange[j].getMax());
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

    // get a point coordinate on a line by linear interpolation
    private double interpolate(double x, long maxValue, float rangeMin, float rangeMax)
    {
        return rangeMin + (x / maxValue) * (rangeMax - rangeMin);
    }

    /**
     * Calculate every point and its color and store them in a Hash table.
     *
     * @return a Hash table which contains all the points' positions and colors
     * of one image
     */
    protected HashMap<Point, Integer> calcPixelTable()
    {
        HashMap<Point, Integer> map = new HashMap<Point, Integer>();
        for (Patch it : patchList)
        {
            super.calcPixelTable(it.listOfTriangles, map);
        }
        return map;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
        patchList = null;
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
     * {@inheritDoc}
     */
    public final Raster getRaster(int x, int y, int w, int h)
    {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        int[] data = new int[w * h * 4];
        if (!patchList.isEmpty() || background != null)
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
                    Point p = new Point(x + col, y + row);
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
