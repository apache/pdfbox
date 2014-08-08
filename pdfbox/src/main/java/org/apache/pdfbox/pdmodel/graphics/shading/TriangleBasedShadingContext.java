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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.util.Matrix;

/**
 * Intermediate class extended by the shading types 4,5,6 and 7 that contains
 * the common methods used by these classes.
 * 
 * @author Shaola Ren
 * @author Tilman Hausherr
 */
abstract class TriangleBasedShadingContext extends ShadingContext
{
    private static final Log LOG = LogFactory.getLog(TriangleBasedShadingContext.class);
    
    protected final boolean hasFunction;
    protected final PDShading shading;

    public TriangleBasedShadingContext(PDShading shading, ColorModel cm, 
            AffineTransform xform, Matrix ctm, int pageHeight, Rectangle dBounds) 
            throws IOException
    {
        super(shading, cm, xform, ctm, pageHeight, dBounds);
        hasFunction = shading.getFunction() != null;
        this.shading = shading;
    }

    // convert color to RGB color value, using function if required, 
    // then convert from the shading colorspace to an RGB value,
    // which is encoded into an integer.
    protected int convertToRGB(float[] values)
    {
        float[] nValues = null;
        int normRGBValues = 0;
        if (hasFunction)
        {
            try
            {
                nValues = shading.evalFunction(values);
            }
            catch (IOException exception)
            {
                LOG.error("error while processing a function", exception);
            }
        }

        try
        {
            float[] rgbValues;
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
    
    // get the points from the triangles, calculate their color and add 
    // point-color mappings to the map
    protected void calcPixelTable(ArrayList<CoonsTriangle> triangleList, HashMap<Point, Integer> map)
    {
        for (CoonsTriangle tri : triangleList)
        {
            int degree = tri.getDeg();
            if (degree == 2)
            {
                Line line = tri.getLine();
                HashSet<Point> linePoints = line.linePoints;
                for (Point p : linePoints)
                {
                    map.put(p, convertToRGB(line.calcColor(p)));
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
                            map.put(p, convertToRGB(tri.calcColor(p)));
                        }
                    }
                }
            }
        }
    }
    
}
