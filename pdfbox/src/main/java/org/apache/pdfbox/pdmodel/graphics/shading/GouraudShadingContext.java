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
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.Matrix;

/**
 *
 * Helper class that Type4ShadingContext and Type5ShadingContext must extend;
 * does the shading of Gouraud triangles.
 *
 * @author lehmi
 * @author Tilman Hausherr
 *
 */
public abstract class GouraudShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(GouraudShadingContext.class);

    private ColorModel outputColorModel;
    private PDColorSpace colorSpace;
    /**
     * number of color components.
     */
    protected int numberOfColorComponents;
    /**
     * triangle list.
     */
    protected ArrayList<GouraudTriangle> triangleList;
    /**
     * bits per coordinate.
     */
    protected int bitsPerCoordinate;
    /**
     * bits per color component.
     */
    protected int bitsPerColorComponent;
    /**
     * background values.
     */
    protected float[] background;
    private ColorSpace shadingColorSpace;
    private PDFunction shadingTinttransform;
    /**
     * color conversion function.
     */
    protected PDFunction function = null; //TODO implement common PDShadingtype for 4 and 5
    private Area area = new Area(); //TODO for later optimization

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shadingType the shading type to be used
     * @param colorModelValue the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * @throws IOException if something went wrong
     *
     */
    protected GouraudShadingContext(PDShadingResources shadingType, ColorModel colorModelValue,
            AffineTransform xform, Matrix ctm, int pageHeight) throws IOException
    {
        triangleList = new ArrayList<GouraudTriangle>();
        colorSpace = shadingType.getColorSpace();
        LOG.debug("colorSpace: " + colorSpace);
        numberOfColorComponents = colorSpace.getNumberOfComponents();
        LOG.debug("numberOfColorComponents: " + numberOfColorComponents);
        LOG.debug("BBox: " + shadingType.getBBox());
        LOG.debug("Background: " + shadingType.getBackground());

        // create the output colormodel using RGB+alpha as colorspace
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        // get the shading colorSpace
        try
        {
            if (!(colorSpace instanceof PDDeviceRGB))
            {
                // we have to create an instance of the shading colorspace if it isn't RGB
                shadingColorSpace = colorSpace.getJavaColorSpace();
                if (colorSpace instanceof PDDeviceN)
                {
                    shadingTinttransform = ((PDDeviceN) colorSpace).getTintTransform();
                }
                else if (colorSpace instanceof PDSeparation)
                {
                    shadingTinttransform = ((PDSeparation) colorSpace).getTintTransform();
                }
            }
        }
        catch (IOException exception)
        {
            LOG.error("error while creating colorSpace", exception);
        }

    }

    /**
     * Read a vertex from the bit input stream and do the interpolations
     * described in the PDF specification.
     *
     * @param input bit input stream
     * @param flag the flag or any value if not relevant
     * @param maxSrcCoord max value for source coordinate (2^bits-1)
     * @param maxSrcColor max value for source color (2^bits-1)
     * @param rangeX dest range for X
     * @param rangeY dest range for Y
     * @param colRangeTab dest range array for colors
     *
     * @return a new vertex with the flag and the interpolated values
     *
     * @throws IOException if something went wrong
     */
    protected Vertex readVertex(ImageInputStream input, byte flag, long maxSrcCoord, long maxSrcColor,
            PDRange rangeX, PDRange rangeY, PDRange[] colRangeTab) throws IOException
    {
        float[] colorComponentTab = new float[numberOfColorComponents];
        long x = input.readBits(bitsPerCoordinate);
        long y = input.readBits(bitsPerCoordinate);
        double dstX = interpolate(x, maxSrcCoord, rangeX.getMin(), rangeX.getMax());
        double dstY = interpolate(y, maxSrcCoord, rangeY.getMin(), rangeY.getMax());
        LOG.debug("coord: " + String.format("[%06X,%06X] -> [%f,%f]", x, y, dstX, dstY));
        for (int n = 0; n < numberOfColorComponents; ++n)
        {
            int color = (int) input.readBits(bitsPerColorComponent);
            colorComponentTab[n] = interpolate(color, maxSrcColor, colRangeTab[n].getMin(), colRangeTab[n].getMax());
            LOG.debug("color[" + n + "]: " + color + "/" + String.format("%02x", color)
                    + "-> color[" + n + "]: " + colorComponentTab[n]);
        }
        return new Vertex(flag, new Point2D.Double(dstX, dstY), colorComponentTab);
    }

    /**
     * transform a list of vertices from shading to user space (if applicable)
     * and from user to device space.
     *
     * @param vertexList list of vertices
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     */
    protected void transformVertices(ArrayList<Vertex> vertexList, Matrix ctm, AffineTransform xform, int pageHeight)
    {
        for (Vertex v : vertexList)
        {
            LOG.debug(v);

            // this segment "inspired" by RadialShadingContext
            if (ctm != null)
            {
                // transform from shading to user space
                ctm.createAffineTransform().transform(v.point, v.point);
                // transform from user to device space
                xform.transform(v.point, v.point);
            }
            else
            {
                // the shading is used as pattern colorspace in combination
                // with a fill-, stroke- or showText-operator
                // move the 0,0-reference including the y-translation from user to device space
                v.point = new Point.Double(v.point.getX(), pageHeight + xform.getTranslateY() - v.point.getY());
            }

            LOG.debug(v);
        }
    }

    /**
     * create a java area that includes all the triangles.
     */
    protected void createArea()
    {
        for (GouraudTriangle triangle : triangleList)
        {
            area.add(new Area(triangle.polygon));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        triangleList = null;
        outputColorModel = null;
        colorSpace = null;
        shadingColorSpace = null;
        shadingTinttransform = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public final Raster getRaster(int x, int y, int w, int h)
    {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        int[] data = new int[w * h * 4];
        for (int row = 0; row < h; row++)
        {
            for (int col = 0; col < w; col++)
            {
                Point2D p = new Point(x + col, y + row);
                GouraudTriangle triangle = null;
                for (GouraudTriangle tryTriangle : triangleList)
                {
                    if (tryTriangle.contains(p))
                    {
                        triangle = tryTriangle;
                        break;
                    }
                }
                float[] values;
                if (triangle != null)
                {
                    double[] weights = triangle.getWeights(p);
                    values = new float[numberOfColorComponents];
                    for (int i = 0; i < numberOfColorComponents; ++i)
                    {
                        values[i] = (float) (triangle.colorA[i] * weights[0]
                                + triangle.colorB[i] * weights[1]
                                + triangle.colorC[i] * weights[2]);
                    }
                }
                else
                {
                    if (background != null)
                    {
                        values = background;
                    }
                    else
                    {
                        continue;
                    }
                }

                //TODO handle function
                // convert color values from shading colorspace to RGB
                if (shadingColorSpace != null)
                {
                    if (shadingTinttransform != null)
                    {
                        try
                        {
                            values = shadingTinttransform.eval(values);
                        }
                        catch (IOException exception)
                        {
                            LOG.error("error while processing a function", exception);
                        }
                    }
                    values = shadingColorSpace.toRGB(values);
                }

                int index = (row * w + col) * 4;
                data[index] = (int) (values[0] * 255);
                data[index + 1] = (int) (values[1] * 255);
                data[index + 2] = (int) (values[2] * 255);
                data[index + 3] = 255;
            }
            raster.setPixels(0, 0, w, h, data);
        }
        return raster;
    }
}
