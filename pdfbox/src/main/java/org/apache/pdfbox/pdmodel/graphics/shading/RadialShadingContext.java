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
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.Matrix;

/**
 * This class represents the PaintContext of an radial shading.
 * 
 */
public class RadialShadingContext implements PaintContext 
{
    private ColorModel outputColorModel;
    private PDColorSpace shadingColorSpace;
    private PDShadingType3 shadingType;

    private float[] coords;
    private float[] domain;
    private float[] background;
    private boolean[] extend;
    private double x1x0; 
    private double y1y0;
    private double r1r0;
    private double x1x0pow2;
    private double y1y0pow2;
    private double r0pow2;

    private float d1d0;
    private double denom;
    
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(RadialShadingContext.class);

    /**
     * Constructor creates an instance to be used for fill operations.
     * 
     * @param shadingType3 the shading type to be used
     * @param colorModelValue the color model to be used
     * @param xform transformation for user to device space
     * @param ctm the transformation matrix
     * @param pageHeight height of the current page
     * 
     */
    public RadialShadingContext(PDShadingType3 shadingType3, ColorModel colorModelValue, 
            AffineTransform xform, Matrix ctm, int pageHeight) throws IOException
    {
        shadingType = shadingType3;
        coords = shadingType.getCoords().toFloatArray();

        if (ctm != null)
        {
            // transform the coords using the given matrix
            AffineTransform at = ctm.createAffineTransform();
            at.transform(coords, 0, coords, 0, 1);
            at.transform(coords, 3, coords, 3, 1);
            coords[2] *= at.getScaleX();
            coords[5] *= at.getScaleX();
        }
        // transform coords to device space
        xform.transform(coords, 0, coords, 0, 1);
        xform.transform(coords, 3, coords, 3, 1);
        // scale radius to device space
        coords[2] *= xform.getScaleX();
        coords[5] *= xform.getScaleX();

        // get the shading colorSpace
        shadingColorSpace = shadingType.getColorSpace();
        // create the output colormodel using RGB+alpha as colorspace
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
        // domain values
        if (shadingType.getDomain() != null)
        {
            domain = shadingType.getDomain().toFloatArray();
        }
        else
        {
            // set default values
            domain = new float[] { 0, 1 };
        }
        // extend values
        COSArray extendValues = shadingType.getExtend();
        if (shadingType.getExtend() != null)
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
        x1x0pow2 = Math.pow(x1x0, 2);
        y1y0pow2 = Math.pow(y1y0, 2);
        r0pow2 = Math.pow(coords[2], 2);
        denom = x1x0pow2 + y1y0pow2 - Math.pow(r1r0, 2);
        d1d0 = domain[1] - domain[0];

        // get background values if available
        COSArray bg = shadingType3.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
        }
    }
    /**
     * {@inheritDoc}
     */
    public void dispose() 
    {
        outputColorModel = null;
        shadingType = null;
        shadingColorSpace = null;
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
            for (int i = 0; i < w; i++)
            {
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
                    if (inputValues[0] >= domain[0] && inputValues[0] <= domain[1])
                    {
                        // both values are in the domain -> choose the larger one
                        if (inputValues[1] >= domain[0] && inputValues[1] <= domain[1])
                        {
                            inputValue = Math.max(inputValues[0], inputValues[1]);
                        }
                        // first value is in the domain, the second not -> choose first value
                        else
                        {
                            inputValue = inputValues[0];
                        }
                    }
                    else
                    {
                        // first value is not in the domain, but the second -> choose second value
                        if (inputValues[1] >= domain[0] && inputValues[1] <= domain[1])
                        {
                            inputValue = inputValues[1];
                        }
                        // both are not in the domain (is this correct, or is there some miscalculation??)
                        else
                        {
                            // TODO don't know what to do in such cases. The following works
                            // with the given examples
                            // choose the smaller if extend[0] is set to true
                            if (extend[0])
                            {
                                inputValue = Math.min(inputValues[0], inputValues[1]);
                            }
                            // choose the bigger one in all other cases
                            else
                            {
                                inputValue = Math.max(inputValues[0], inputValues[1]);
                            }
                        }
                    }
                    // input value is out of range
                    if (inputValue > domain[1])
                    {
                        // the shading has to be extended if extend[1] == true
                        if (extend[1])
                        {
                            inputValue = domain[1];
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
                    else if (inputValue < domain[0])
                    {
                        // the shading has to be extended if extend[0] == true
                        if (extend[0])
                        {
                            inputValue = domain[0];
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
                float[] values = null;
                int index = (j * w + i) * 4;
                if (useBackground)
                {
                    // use the given backgound color values
                    values = background;
                }
                else
                {
                    try
                    {
                        float input = (float) (domain[0] + (d1d0 * inputValue));
                        values = shadingType.evalFunction(input);
                    }
                    catch (IOException exception)
                    {
                        LOG.error("error while processing a function", exception);
                    }
                }
                // convert color values from shading color space to RGB
                try
                {
                    values = shadingColorSpace.toRGB(values);
                }
                catch (IOException exception)
                {
                    LOG.error("error processing color space", exception);
                }
                data[index] = (int) (values[0] * 255);
                data[index + 1] = (int) (values[1] * 255);
                data[index + 2] = (int) (values[2] * 255);
                data[index + 3] = 255;
            }
        }
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }

    private float[] calculateInputValues(int x, int y)
    {

        /**
         * According to Adobes Technical Note #5600 we have to do the following
         * 
         * x0, y0, r0 defines the start circle x1, y1, r1 defines the end circle
         * 
         * The parametric equations for the center and radius of the gradient fill circle moving between the start
         * circle and the end circle as a function of s are as follows:
         * 
         * xc(s) = x0 + s * (x1 - x0) yc(s) = y0 + s * (y1 - y0) r(s) = r0 + s * (r1 - r0)
         * 
         * Given a geometric coordinate position (x, y) in or along the gradient fill, the corresponding value of s can
         * be determined by solving the quadratic constraint equation:
         * 
         * [x - xc(s)]2 + [y - yc(s)]2 = [r(s)]2
         * 
         * The following code calculates the 2 possible values of s
         */
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
}
