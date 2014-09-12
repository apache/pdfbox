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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for radial shading.
 *
 * Performance improvement done as part of GSoC2014, Tilman Hausherr is the
 * mentor.
 *
 * @author Andreas Lehmkühler
 * @author Shaola Ren
 */
public class RadialShadingContext extends ShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(RadialShadingContext.class);

    private PDShadingType3 radialShadingType;

    private final float[] coords;
    private final float[] domain;
    private float[] background;
    private int rgbBackground;
    private final boolean[] extend;
    private final double x1x0;
    private final double y1y0;
    private final double r1r0;
    private final double x1x0pow2;
    private final double y1y0pow2;
    private final double r0pow2;
    private final float d1d0;
    private final double denom;

    private final double longestDistance;
    private final int[] colorTable;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm the transformation matrix
     * @param dBounds device bounds
     * @param pageHeight height of the current page
     */
    public RadialShadingContext(PDShadingType3 shading, ColorModel colorModel, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        super(shading, colorModel, xform, ctm, pageHeight, dBounds);
        this.radialShadingType = shading;
        coords = shading.getCoords().toFloatArray();

        if (ctm != null)
        {
            // the shading is used in combination with the sh-operator
            // transform the coords from shading to user space
            ctm.createAffineTransform().transform(coords, 0, coords, 0, 1);
            ctm.createAffineTransform().transform(coords, 3, coords, 3, 1);
            // scale radius to user space
            coords[2] *= ctm.getXScale();
            coords[5] *= ctm.getXScale();

            // move the 0,0-reference
            coords[1] = pageHeight - coords[1];
            coords[4] = pageHeight - coords[4];
        }
        else
        {
            // the shading is used as pattern colorspace in combination
            // with a fill-, stroke- or showText-operator
            float translateY = (float) xform.getTranslateY();
            // move the 0,0-reference including the y-translation from user to device space
            coords[1] = pageHeight + translateY - coords[1];
            coords[4] = pageHeight + translateY - coords[4];
        }

        // transform the coords from user to device space
        xform.transform(coords, 0, coords, 0, 1);
        xform.transform(coords, 3, coords, 3, 1);

        // scale radius to device space
        coords[2] *= xform.getScaleX();
        coords[5] *= xform.getScaleX();

        // a radius is always positive
        coords[2] = Math.abs(coords[2]);
        coords[5] = Math.abs(coords[5]);

        // domain values
        if (this.radialShadingType.getDomain() != null)
        {
            domain = shading.getDomain().toFloatArray();
        }
        else
        {
            // set default values
            domain = new float[]
            {
                0, 1
            };
        }

        // extend values
        COSArray extendValues = shading.getExtend();
        if (shading.getExtend() != null)
        {
            extend = new boolean[2];
            extend[0] = ((COSBoolean) extendValues.get(0)).getValue();
            extend[1] = ((COSBoolean) extendValues.get(1)).getValue();
        }
        else
        {
            // set default values
            extend = new boolean[]
            {
                false, false
            };
        }
        // calculate some constants to be used in getRaster
        x1x0 = coords[3] - coords[0];
        y1y0 = coords[4] - coords[1];
        r1r0 = coords[5] - coords[2];
        x1x0pow2 = Math.pow(x1x0, 2);
        y1y0pow2 = Math.pow(y1y0, 2);
        r0pow2 = Math.pow(coords[2], 2);
        denom = x1x0pow2 + y1y0pow2 - Math.pow(r1r0, 2);
        d1d0 = domain[1] - domain[0];

        // get background values if available
        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
            rgbBackground = convertToRGB(background);
        }
        longestDistance = getLongestDis();
        colorTable = calcColorTable();
    }

    // get the longest distance of two points which are located on these two circles
    private double getLongestDis()
    {
        double centerToCenter = Math.sqrt(x1x0pow2 + y1y0pow2);
        double rmin, rmax;
        if (coords[2] < coords[5])
        {
            rmin = coords[2];
            rmax = coords[5];
        }
        else
        {
            rmin = coords[5];
            rmax = coords[2];
        }
        if (centerToCenter + rmin <= rmax)
        {
            return 2 * rmax;
        }
        else
        {
            return rmin + centerToCenter + coords[5];
        }
    }

    /**
     * Calculate the color on the line connects two circles' centers and store
     * the result in an array.
     *
     * @return an array, index denotes the relative position, the corresponding
     * value the color
     */
    private int[] calcColorTable()
    {
        int[] map = new int[(int) longestDistance + 1];
        if (longestDistance == 0 || d1d0 == 0)
        {
            try
            {
                float[] values = radialShadingType.evalFunction(domain[0]);
                map[0] = convertToRGB(values);
            }
            catch (IOException exception)
            {
                LOG.error("error while processing a function", exception);
            }
        }
        else
        {
            for (int i = 0; i <= longestDistance; i++)
            {
                float t = domain[0] + d1d0 * i / (float) longestDistance;
                try
                {
                    float[] values = radialShadingType.evalFunction(t);
                    map[i] = convertToRGB(values);
                }
                catch (IOException exception)
                {
                    LOG.error("error while processing a function", exception);
                }
            }
        }
        return map;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
        outputColorModel = null;
        radialShadingType = null;
        shadingColorSpace = null;
        shadingTinttransform = null;
    }

    /**
     * {@inheritDoc}
     */
    public ColorModel getColorModel()
    {
        return outputColorModel;
    }

    /**
     * {@inheritDoc}
     */
    public Raster getRaster(int x, int y, int w, int h)
    {
        // create writable raster
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        float inputValue = -1;
        boolean useBackground;
        int[] data = new int[w * h * 4];
        for (int j = 0; j < h; j++)
        {
            double currentY = y + j;
            if (bboxRect != null)
            {
                if (currentY < minBBoxY || currentY > maxBBoxY)
                {
                    continue;
                }
            }
            for (int i = 0; i < w; i++)
            {
                double currentX = x + i;
                if (bboxRect != null)
                {
                    if (currentX < minBBoxX || currentX > maxBBoxX)
                    {
                        continue;
                    }
                }
                useBackground = false;
                float[] inputValues = calculateInputValues(x + i, y + j);
                if (Float.isNaN(inputValues[0]) && Float.isNaN(inputValues[1]))
                {
                    if (background != null)
                    {
                        useBackground = true;
                    }
                    else
                    {
                        continue;
                    }
                }
                else
                {
                    // choose 1 of the 2 values
                    if (inputValues[0] >= 0 && inputValues[0] <= 1)
                    {
                        // both values are in the range -> choose the larger one
                        if (inputValues[1] >= 0 && inputValues[1] <= 1)
                        {
                            inputValue = Math.max(inputValues[0], inputValues[1]);
                        }
                        // first value is in the range, the second not -> choose first value
                        else
                        {
                            inputValue = inputValues[0];
                        }
                    }
                    else
                    {
                        // first value is not in the range, 
                        // but the second -> choose second value
                        if (inputValues[1] >= 0 && inputValues[1] <= 1)
                        {
                            inputValue = inputValues[1];
                        }
                        // both are not in the range
                        else
                        {
                            if (extend[0] && extend[1])
                            {
                                inputValue = Math.max(inputValues[0], inputValues[1]);
                            }
                            else if (extend[0])
                            {
                                inputValue = inputValues[0];
                            }
                            else if (extend[1])
                            {
                                inputValue = inputValues[1];
                            }
                            else if (background != null)
                            {
                                useBackground = true;
                            }
                            else
                            {
                                continue;
                            }
                        }
                    }
                    // input value is out of range
                    if (inputValue > 1)
                    {
                        // extend shading if extend[1] is true and nonzero radius
                        if (extend[1] && coords[5] > 0)
                        {
                            inputValue = 1;
                        }
                        else
                        {
                            if (background != null)
                            {
                                useBackground = true;
                            }
                            else
                            {
                                continue;
                            }
                        }
                    }
                    // input value is out of range
                    else if (inputValue < 0)
                    {
                        // extend shading if extend[0] is true and nonzero radius
                        if (extend[0] && coords[2] > 0)
                        {
                            inputValue = 0;
                        }
                        else
                        {
                            if (background != null)
                            {
                                useBackground = true;
                            }
                            else
                            {
                                continue;
                            }
                        }
                    }
                }
                int value;
                if (useBackground)
                {
                    // use the given backgound color values
                    value = rgbBackground;
                }
                else
                {
                    int key = (int) (inputValue * longestDistance);
                    value = colorTable[key];
                }
                int index = (j * w + i) * 4;
                data[index] = value & 255;
                value >>= 8;
                data[index + 1] = value & 255;
                value >>= 8;
                data[index + 2] = value & 255;
                data[index + 3] = 255;
            }
        }
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }

    private float[] calculateInputValues(int x, int y)
    {
        // According to Adobes Technical Note #5600 we have to do the following
        //
        // x0, y0, r0 defines the start circle x1, y1, r1 defines the end circle
        //
        // The parametric equations for the center and radius of the gradient fill circle moving
        // between the start circle and the end circle as a function of s are as follows:
        //
        // xc(s) = x0 + s * (x1 - x0) yc(s) = y0 + s * (y1 - y0) r(s) = r0 + s * (r1 - r0)
        //
        // Given a geometric coordinate position (x, y) in or along the gradient fill, the
        // corresponding value of s can be determined by solving the quadratic constraint equation:
        //
        // [x - xc(s)]2 + [y - yc(s)]2 = [r(s)]2
        //
        // The following code calculates the 2 possible values of s
        //
        double p = -(x - coords[0]) * x1x0 - (y - coords[1]) * y1y0 - coords[2] * r1r0;
        double q = (Math.pow(x - coords[0], 2) + Math.pow(y - coords[1], 2) - r0pow2);
        double root = Math.sqrt(p * p - denom * q);
        float root1 = (float) ((-p + root) / denom);
        float root2 = (float) ((-p - root) / denom);
        if (denom < 0)
        {
            return new float[]
            {
                root1, root2
            };
        }
        else
        {
            return new float[]
            {
                root2, root1
            };
        }
    }

    /**
     * Returns the coords values.
     *
     * @return the coords values as array
     */
    public float[] getCoords()
    {
        return coords;
    }

    /**
     * Returns the domain values.
     *
     * @return the domain values as array
     */
    public float[] getDomain()
    {
        return domain;
    }

    /**
     * Returns the extend values.
     *
     * @return the extend values as array
     */
    public boolean[] getExtend()
    {
        return extend;
    }

    /**
     * Returns the function.
     *
     * @return the function
     * @throws IOException if something goes wrong
     */
    public PDFunction getFunction() throws IOException
    {
        return radialShadingType.getFunction();
    }
}
