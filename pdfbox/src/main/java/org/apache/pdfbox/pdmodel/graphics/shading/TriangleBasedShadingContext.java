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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
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

    /**
     * bits per coordinate.
     */
    protected int bitsPerCoordinate;

    /**
     * number of color components.
     */
    protected int numberOfColorComponents;

    /**
     * bits per color component
     */
    protected int bitsPerColorComponent;

    final protected boolean hasFunction;

    public TriangleBasedShadingContext(PDShadingResources shading, ColorModel cm,
            AffineTransform xform, Matrix ctm, int pageHeight, Rectangle dBounds)
            throws IOException
    {
        super(shading, cm, xform, ctm, pageHeight, dBounds);
        PDTriangleBasedShadingType triangleBasedShadingType = (PDTriangleBasedShadingType) shading;
        hasFunction = shading.getFunction() != null;
        bitsPerCoordinate = triangleBasedShadingType.getBitsPerCoordinate();
        LOG.debug("bitsPerCoordinate: " + (Math.pow(2, bitsPerCoordinate) - 1));
        bitsPerColorComponent = triangleBasedShadingType.getBitsPerComponent();
        LOG.debug("bitsPerColorComponent: " + bitsPerColorComponent);
        numberOfColorComponents = hasFunction ? 1 : colorSpace.getNumberOfComponents();
        if (colorSpace instanceof PDSeparation)
        {
            // bug? PDSeparation.getNumberOfComponents() returns the number of components 
            // of the target colorant
            numberOfColorComponents = 1;
        }
    }

    // get the points from the triangles, calculate their color and add 
    // point-color mappings to the map
    protected void calcPixelTable(ArrayList<ShadedTriangle> triangleList, HashMap<Point, Integer> map)
    {
        for (ShadedTriangle tri : triangleList)
        {
            int degree = tri.getDeg();
            if (degree == 2)
            {
                Line line = tri.getLine();
                for (Point p : line.linePoints)
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

    // transform a point from source space to device space
    protected void transformPoint(Point2D p, Matrix ctm, AffineTransform xform)
    {
        if (ctm != null)
        {
            ctm.createAffineTransform().transform(p, p);
        }
        xform.transform(p, p);
    }

    // convert color to RGB color value, using function if required,
    // then convert from the shading colorspace to an RGB value,
    // which is encoded into an integer.
    @Override
    protected int convertToRGB(float[] values)
    {
        if (hasFunction)
        {
            try
            {
                values = shading.evalFunction(values);
            }
            catch (IOException exception)
            {
                LOG.error("error while processing a function", exception);
            }
        }
        return super.convertToRGB(values);
    }

}
