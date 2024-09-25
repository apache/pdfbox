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
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.util.Matrix;

/**
 * Intermediate class extended by the shading types 4,5,6 and 7 that contains the common methods
 * used by these classes.
 *
 * @author Shaola Ren
 * @author Tilman Hausherr
 */
abstract class TriangleBasedShadingContext extends ShadingContext
{
    // array of pixels within triangles to their RGB color
    private int[][] pixelTableArray;

    // offset to be used for the array index
    private int xOffset = 0;
    private int yOffset = 0;

    /**
     * Constructor.
     *
     * @param shading the shading type to be used
     * @param cm the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @throws IOException if there is an error getting the color space or doing background color conversion.
     */
    TriangleBasedShadingContext(PDShading shading, ColorModel cm, AffineTransform xform,
                                       Matrix matrix) throws IOException
    {
        super(shading, cm, xform, matrix);
    }

    /**
     * Creates the pixel table.
     */
    protected final void createPixelTable(Rectangle deviceBounds) throws IOException
    {
        xOffset = -deviceBounds.x;
        yOffset = -deviceBounds.y;
        pixelTableArray = calcPixelTableArray(deviceBounds);
    }

    /**
     * Calculate every point and its color and store them in a two-dimensional array.
     *
     * @return an array which contains all the points' positions and colors of one image
     */
    abstract int[][] calcPixelTableArray(Rectangle deviceBounds) throws IOException;

    /**
     * Get the points from the triangles, calculate their color and add point-color mappings.
     */
    protected void calcPixelTable(List<ShadedTriangle> triangleList, int[][] array,
            Rectangle deviceBounds) throws IOException
    {
        for (ShadedTriangle tri : triangleList)
        {
            int degree = tri.getDeg();
            if (degree == 2)
            {
                addLinePoints(tri.getLine(), array);
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
                            addValueToArray(p, evalFunctionAndConvertToRGB(tri.calcColor(p)),
                                    array);
                        }
                    }
                }
                // "fatten" triangle by drawing the borders with Bresenham's line algorithm
                // Inspiration: Raph Levien in http://bugs.ghostscript.com/show_bug.cgi?id=219588
                Point p0 = new Point((int) Math.round(tri.corner[0].getX()),
                                     (int) Math.round(tri.corner[0].getY()));
                Point p1 = new Point((int) Math.round(tri.corner[1].getX()),
                                     (int) Math.round(tri.corner[1].getY()));
                Point p2 = new Point((int) Math.round(tri.corner[2].getX()),
                                     (int) Math.round(tri.corner[2].getY()));
                addLinePoints(new Line(p0, p1, tri.color[0], tri.color[1]), array);
                addLinePoints(new Line(p1, p2, tri.color[1], tri.color[2]), array);
                addLinePoints(new Line(p2, p0, tri.color[2], tri.color[0]), array);
            }
        }
    }

    private void addLinePoints(Line line, int[][] array) throws IOException
    {
        for (Point p : line.linePoints)
        {
            addValueToArray(p, evalFunctionAndConvertToRGB(line.calcColor(p)), array);
        }
    }

    private void addValueToArray(Point p, int value, int[][] array)
    {
        int xIndex = p.x + xOffset;
        int yIndex = p.y + yOffset;
        if (xIndex < 0 || yIndex < 0 || xIndex >= array.length || yIndex >= array[0].length)
        {
            return;
        }
        array[xIndex][yIndex] = value;
    }

    private int getValueFromArray(int x, int y)
    {
        int xIndex = x + xOffset;
        int yIndex = y + yOffset;
        if (xIndex < 0 || yIndex < 0 || xIndex >= pixelTableArray.length
                || yIndex >= pixelTableArray[0].length)
        {
            return -1;
        }
        return pixelTableArray[xIndex][yIndex];
    }

    /**
     * Convert color to RGB color value, using function if required, then convert from the shading
     * color space to an RGB value, which is encoded into an integer.
     */
    private int evalFunctionAndConvertToRGB(float[] values) throws IOException
    {
        if (getShading().getFunction() != null)
        {
            values = getShading().evalFunction(values);
        }
        return convertToRGB(values);
    }

    /**
     * Returns true if the shading has an empty data stream.
     */
    abstract boolean isDataEmpty();

    @Override
    public final Raster getRaster(int x, int y, int w, int h)
    {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        int[] data = new int[w * h * 4];
        if (!isDataEmpty() || getBackground() != null)
        {
            for (int row = 0; row < h; row++)
            {
                for (int col = 0; col < w; col++)
                {
                    int value = getValueFromArray(x + col, y + row);
                    if (value >= 0)
                    {
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
        }
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }
}
