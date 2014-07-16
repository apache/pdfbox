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
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
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
public class RadialShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(RadialShadingContext.class);

    private ColorModel outputColorModel;
    private PDColorSpace shadingColorSpace;
    private PDShadingType3 shading;

    private float[] coords;
    private float[] domain;
    private float[] background;
    private int rgbBackground;
    private boolean[] extend;
    private double x1x0; 
    private double y1y0;
    private double r1r0;
    private double x1x0pow2;
    private double y1y0pow2;
    private double r0pow2;

    private float d1d0;
    private double denom;
    
    private final double longestDistance;
    private int[] colorTable;

    /**
     * Constructor creates an instance to be used for fill operations.
     * @param shading the shading type to be used
     * @param cm the color model to be used
     * @param xform transformation for user to device space
     * @param ctm the transformation matrix
     * @param pageHeight height of the current page
     */
    public RadialShadingContext(PDShadingType3 shading, ColorModel cm, AffineTransform xform,
                                Matrix ctm, int pageHeight) throws IOException
    {
        this.shading = shading;
        coords = this.shading.getCoords().toFloatArray();

        if (ctm != null)
        {
            // transform the coords using the given matrix
            AffineTransform at = ctm.createAffineTransform();
            at.transform(coords, 0, coords, 0, 1);
            at.transform(coords, 3, coords, 3, 1);
            coords[2] *= ctm.getXScale();
            coords[5] *= ctm.getXScale();
        }
        // transform coords to device space
        xform.transform(coords, 0, coords, 0, 1);
        xform.transform(coords, 3, coords, 3, 1);
        // scale radius to device space
        coords[2] *= xform.getScaleX();
        coords[5] *= xform.getScaleX();

        // get the shading colorSpace
        shadingColorSpace = this.shading.getColorSpace();
        // create the output colormodel using RGB+alpha as colorspace
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
        // domain values
        if (this.shading.getDomain() != null)
        {
            domain = this.shading.getDomain().toFloatArray();
        }
        else
        {
            // set default values
            domain = new float[] { 0, 1 };
        }
        // extend values
        COSArray extendValues = this.shading.getExtend();
        if (this.shading.getExtend() != null)
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
     * Calculate the color on the line connects two circles' centers and store the result in an array.
     * @return an array, index denotes the relative position, the corresponding value the color
     */
    private int[] calcColorTable()
    {
        int[] map = new int[(int) longestDistance + 1];
        if (longestDistance == 0 || d1d0 == 0)
        {
            try
            {
                float[] values = shading.evalFunction(domain[0]);
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
                float t = domain[0] + d1d0 * i / (float)longestDistance;
                try
                {
                    float[] values = shading.evalFunction(t);
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
    
    // convert color to RGB color values
    private int convertToRGB(float[] values)
    {
        float[] rgbValues;
        int normRGBValues = 0;
        try
        {
            rgbValues = shadingColorSpace.toRGB(values);
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

    @Override
    public void dispose() 
    {
        outputColorModel = null;
        shading = null;
        shadingColorSpace = null;
    }

    @Override
    public ColorModel getColorModel() 
    {
        return outputColorModel;
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
                        // both are not in the domain
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
            return new float[] { root1, root2 };
        }
        else
        {
            return new float[] { root2, root1 };
        }
    }

    /**
     * Returns the coords values.
     * @return the coords values as array
     */
    public float[] getCoords() 
    {
        return coords;
    }
        
    /**
     * Returns the domain values.
     * @return the domain values as array
     */
    public float[] getDomain() 
    {
        return domain;
    }
        
    /**
     * Returns the extend values.
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
        return shading.getFunction();
    }
}
