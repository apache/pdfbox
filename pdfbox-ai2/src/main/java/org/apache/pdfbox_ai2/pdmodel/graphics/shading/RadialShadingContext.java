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
package org.apache.pdfbox_ai2.pdmodel.graphics.shading;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox_ai2.cos.COSArray;
import org.apache.pdfbox_ai2.cos.COSBoolean;
import org.apache.pdfbox_ai2.pdmodel.common.function.PDFunction;
import org.apache.pdfbox_ai2.util.Matrix;

/**
 * AWT PaintContext for radial shading.
 *
 * Performance improvement done as part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
public class RadialShadingContext extends ShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(RadialShadingContext.class);

    private PDShadingType3 radialShadingType;

    private final float[] coords;
    private final float[] domain;
    private final boolean[] extend;
    private final double x1x0;
    private final double y1y0;
    private final double r1r0;
    private final double r0pow2;
    private final float d1d0;
    private final double denom;

    private final int factor;
    private final int[] colorTable;

    private AffineTransform rat;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param deviceBounds the bounds of the area to paint, in device units
     * @throws IOException if there is an error getting the color space or doing color conversion.
     */
    public RadialShadingContext(PDShadingType3 shading, ColorModel colorModel,
                                AffineTransform xform, Matrix matrix, Rectangle deviceBounds)
                                throws IOException
    {
        super(shading, colorModel, xform, matrix);
        this.radialShadingType = shading;
        coords = shading.getCoords().toFloatArray();

        // domain values
        if (this.radialShadingType.getDomain() != null)
        {
            domain = shading.getDomain().toFloatArray();
        }
        else
        {
            // set default values
            domain = new float[] { 0, 1 };
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
            extend = new boolean[] { false, false };
        }
        // calculate some constants to be used in getRaster
        x1x0 = coords[3] - coords[0];
        y1y0 = coords[4] - coords[1];
        r1r0 = coords[5] - coords[2];
        r0pow2 = Math.pow(coords[2], 2);
        denom = Math.pow(x1x0, 2) + Math.pow(y1y0, 2) - Math.pow(r1r0, 2);
        d1d0 = domain[1] - domain[0];

        try
        {
            // get inverse transform to be independent of current user / device space 
            // when handling actual pixels in getRaster()
            rat = matrix.createAffineTransform().createInverse();
            rat.concatenate(xform.createInverse());
        }
        catch (NoninvertibleTransformException ex)
        {
            LOG.error(ex, ex);
        }

        // shading space -> device space
        AffineTransform shadingToDevice = (AffineTransform)xform.clone();
        shadingToDevice.concatenate(matrix.createAffineTransform());

        // worst case for the number of steps is opposite diagonal corners, so use that
        double dist = Math.sqrt(Math.pow(deviceBounds.getMaxX() - deviceBounds.getMinX(), 2) +
                                Math.pow(deviceBounds.getMaxY() - deviceBounds.getMinY(), 2));
        factor = (int) Math.ceil(dist);

        // build the color table for the given number of steps
        colorTable = calcColorTable();
    }

    /**
     * Calculate the color on the line that connects two circles' centers and store the result in an
     * array.
     *
     * @return an array, index denotes the relative position, the corresponding value the color
     */
    private int[] calcColorTable() throws IOException
    {
        int[] map = new int[factor + 1];
        if (factor == 0 || d1d0 == 0)
        {
            float[] values = radialShadingType.evalFunction(domain[0]);
            map[0] = convertToRGB(values);
        }
        else
        {
            for (int i = 0; i <= factor; i++)
            {
                float t = domain[0] + d1d0 * i / factor;
                float[] values = radialShadingType.evalFunction(t);
                map[i] = convertToRGB(values);
            }
        }
        return map;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        radialShadingType = null;
    }

    @Override
    public ColorModel getColorModel()
    {
        return super.getColorModel();
    }

    @Override
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
            if (bboxRect != null && (currentY < minBBoxY || currentY > maxBBoxY))
            {
                continue;
            }
            for (int i = 0; i < w; i++)
            {
                double currentX = x + i;
                if (bboxRect != null && (currentX < minBBoxX || currentX > maxBBoxX))
                {
                    continue;
                }

                float[] values = new float[] { x + i, y + j };
                rat.transform(values, 0, values, 0, 1);
                currentX = values[0];
                currentY = values[1];

                useBackground = false;
                float[] inputValues = calculateInputValues(currentX, currentY);
                if (Float.isNaN(inputValues[0]) && Float.isNaN(inputValues[1]))
                {
                    if (getBackground() == null)
                    {
                        continue;
                    }
                    useBackground = true;
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
                            else if (getBackground() != null)
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
                            if (getBackground() == null)
                            {
                                continue;
                            }
                            useBackground = true;
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
                            if (getBackground() == null)
                            {
                                continue;
                            }
                            useBackground = true;
                        }
                    }
                }
                int value;
                if (useBackground)
                {
                    // use the given backgound color values
                    value = getRgbBackground();
                }
                else
                {
                    int key = (int) (inputValue * factor);
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

    private float[] calculateInputValues(double x, double y)
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
            return new float[] { root1, root2 };
        }
        else
        {
            return new float[] { root2, root1 };
        }
    }

    /**
     * Returns the coords values.
     */
    public float[] getCoords()
    {
        return coords;
    }

    /**
     * Returns the domain values.
     */
    public float[] getDomain()
    {
        return domain;
    }

    /**
     * Returns the extend values.
     */
    public boolean[] getExtend()
    {
        return extend;
    }

    /**
     * Returns the function.
     *
     * @throws java.io.IOException if we were not able to create the function.
     */
    public PDFunction getFunction() throws IOException
    {
        return radialShadingType.getFunction();
    }
}
