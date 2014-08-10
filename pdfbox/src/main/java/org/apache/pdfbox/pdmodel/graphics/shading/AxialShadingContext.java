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
 * AWT PaintContext for axial shading.
 *
 * Performance improvement done as part of GSoC2014, Tilman Hausherr is the
 * mentor.
 *
 * @author Andreas Lehmkühler
 * @author Shaola Ren
 *
 */
public class AxialShadingContext extends ShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(AxialShadingContext.class);

    private PDShadingType2 axialShadingType;

    private final float[] coords;
    private final float[] domain;
    private float[] background;
    private int rgbBackground;
    private final boolean[] extend;
    private final double x1x0;
    private final double y1y0;
    private final float d1d0;
    private double denom;

    private final double axialLength;
    private final int[] colorTable;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm the transformation matrix
     * @param pageHeight height of the current page
     * @param dBounds device bounds
     */
    public AxialShadingContext(PDShadingType2 shading, ColorModel colorModel, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        super(shading, colorModel, xform, ctm, pageHeight, dBounds);
        this.axialShadingType = shading;
        coords = shading.getCoords().toFloatArray();

        if (ctm != null)
        {
            // the shading is used in combination with the sh-operator
            // transform the coords from shading to user space
            ctm.createAffineTransform().transform(coords, 0, coords, 0, 2);
            // move the 0,0-reference
            coords[1] = pageHeight - coords[1];
            coords[3] = pageHeight - coords[3];
        }
        else
        {
            // the shading is used as pattern colorspace in combination
            // with a fill-, stroke- or showText-operator
            float translateY = (float) xform.getTranslateY();
            // move the 0,0-reference including the y-translation from user to device space
            coords[1] = pageHeight + translateY - coords[1];
            coords[3] = pageHeight + translateY - coords[3];
        }
        // transform the coords from user to device space
        xform.transform(coords, 0, coords, 0, 2);

        // domain values
        if (shading.getDomain() != null)
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
        x1x0 = coords[2] - coords[0];
        y1y0 = coords[3] - coords[1];
        d1d0 = domain[1] - domain[0];
        denom = Math.pow(x1x0, 2) + Math.pow(y1y0, 2);
        axialLength = Math.sqrt(denom);

        // get background values if available
        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
            rgbBackground = convertToRGB(background);
        }
        colorTable = calcColorTable();
    }

    /**
     * Calculate the color on the axial line and store them in an array.
     *
     * @return an array, index denotes the relative position, the corresponding
     * value is the color on the axial line
     */
    private int[] calcColorTable()
    {
        int[] map = new int[(int) axialLength + 1];
        if (axialLength == 0 || d1d0 == 0)
        {
            try
            {
                float[] values = axialShadingType.evalFunction(domain[0]);
                map[0] = convertToRGB(values);
            }
            catch (IOException exception)
            {
                LOG.error("error while processing a function", exception);
            }
        }
        else
        {
            for (int i = 0; i <= axialLength; i++)
            {
                float t = domain[0] + d1d0 * i / (float) axialLength;
                try
                {
                    float[] values = axialShadingType.evalFunction(t);
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
        shadingColorSpace = null;
        shadingTinttransform = null;
        axialShadingType = null;
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
                double inputValue = x1x0 * (currentX - coords[0]);
                inputValue += y1y0 * (currentY - coords[1]);
                // TODO this happens if start == end, see PDFBOX-1442
                if (denom == 0)
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
                    inputValue /= denom;
                }
                // input value is out of range
                if (inputValue < 0)
                {
                    // the shading has to be extended if extend[0] == true
                    if (extend[0])
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
                // input value is out of range
                else if (inputValue > 1)
                {
                    // the shading has to be extended if extend[1] == true
                    if (extend[1])
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
                int value;
                if (useBackground)
                {
                    // use the given backgound color values
                    value = rgbBackground;
                }
                else
                {
                    int key = (int) (inputValue * axialLength);
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
        return axialShadingType.getFunction();
    }
}
