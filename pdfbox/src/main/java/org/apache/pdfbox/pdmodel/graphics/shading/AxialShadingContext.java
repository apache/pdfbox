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
import java.awt.geom.NoninvertibleTransformException;
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
 * Performance improvement done as part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
public class AxialShadingContext extends ShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(AxialShadingContext.class);

    private PDShadingType2 axialShadingType;

    private final float[] coords;
    private final float[] domain;
    private final boolean[] extend;
    private final double x1x0;
    private final double y1y0;
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
    public AxialShadingContext(PDShadingType2 shading, ColorModel colorModel, AffineTransform xform,
                               Matrix matrix, Rectangle deviceBounds) throws IOException
    {
        super(shading, colorModel, xform, matrix);
        this.axialShadingType = shading;
        coords = shading.getCoords().toFloatArray();

        // domain values
        if (shading.getDomain() != null)
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
        if (extendValues != null)
        {
            extend = new boolean[2];
            extend[0] = ((COSBoolean) extendValues.getObject(0)).getValue();
            extend[1] = ((COSBoolean) extendValues.getObject(1)).getValue();
        }
        else
        {
            // set default values
            extend = new boolean[] { false, false };
        }
        // calculate some constants to be used in getRaster
        x1x0 = coords[2] - coords[0];
        y1y0 = coords[3] - coords[1];
        d1d0 = domain[1] - domain[0];
        denom = Math.pow(x1x0, 2) + Math.pow(y1y0, 2);

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
     * Calculate the color on the axial line and store them in an array.
     *
     * @return an array, index denotes the relative position, the corresponding
     * value is the color on the axial line
     * @throws IOException if the color conversion fails.
     */
    private int[] calcColorTable() throws IOException
    {
        int[] map = new int[factor + 1];
        if (factor == 0 || d1d0 == 0)
        {
            float[] values = axialShadingType.evalFunction(domain[0]);
            map[0] = convertToRGB(values);
        }
        else
        {
            for (int i = 0; i <= factor; i++)
            {
                float t = domain[0] + d1d0 * i / factor;
                float[] values = axialShadingType.evalFunction(t);
                map[i] = convertToRGB(values);
            }
        }
        return map;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        axialShadingType = null;
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
                useBackground = false;
                float[] values = new float[] { x + i, y + j };
                rat.transform(values, 0, values, 0, 1);
                currentX = values[0];
                currentY = values[1];
                double inputValue = x1x0 * (currentX - coords[0]) + y1y0 * (currentY - coords[1]);
                // TODO this happens if start == end, see PDFBOX-1442
                if (denom == 0)
                {
                    if (getBackground() == null)
                    {
                        continue;
                    }
                    useBackground = true;
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
                        if (getBackground() == null)
                        {
                            continue;
                        }
                        useBackground = true;
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
                        if (getBackground() == null)
                        {
                            continue;
                        }
                        useBackground = true;
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
        return axialShadingType.getFunction();
    }
}
