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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.Matrix;

/**
 * A base class to handle what is common to all shading types.
 *
 * @author Shaola Ren
 * @author Tilman Hausherr
 */
public abstract class ShadingContext
{
    private static final Log LOG = LogFactory.getLog(ShadingContext.class);

    protected PDRectangle bboxRect;
    protected float minBBoxX, minBBoxY, maxBBoxX, maxBBoxY;

    private float[] background;
    private int rgbBackground;
    private final PDShading shading;
    private ColorModel outputColorModel;
    private PDColorSpace shadingColorSpace;

    /**
     * Constructor.
     *
     * @param shading the shading type to be used
     * @param cm the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @throws java.io.IOException if there is an error getting the color space
     * or doing background color conversion.
     */
    public ShadingContext(PDShading shading, ColorModel cm, AffineTransform xform,
                          Matrix matrix) throws IOException
    {
        this.shading = shading;
        shadingColorSpace = shading.getColorSpace();

        // create the output color model using RGB+alpha as color space
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        bboxRect = shading.getBBox();
        if (bboxRect != null)
        {
            transformBBox(matrix, xform);
        }
        
        // get background values if available
        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
            rgbBackground = convertToRGB(background);
        }
    }

    PDColorSpace getShadingColorSpace()
    {
        return shadingColorSpace;
    }

    PDShading getShading()
    {
        return shading;
    }

    float[] getBackground()
    {
        return background;
    }

    int getRgbBackground()
    {
        return rgbBackground;
    }
    
    private void transformBBox(Matrix matrix, AffineTransform xform)
    {
        float[] bboxTab = new float[4];
        bboxTab[0] = bboxRect.getLowerLeftX();
        bboxTab[1] = bboxRect.getLowerLeftY();
        bboxTab[2] = bboxRect.getUpperRightX();
        bboxTab[3] = bboxRect.getUpperRightY();

        // transform the coords using the given matrix
        matrix.createAffineTransform().transform(bboxTab, 0, bboxTab, 0, 2);

        xform.transform(bboxTab, 0, bboxTab, 0, 2);
        minBBoxX = Math.min(bboxTab[0], bboxTab[2]);
        minBBoxY = Math.min(bboxTab[1], bboxTab[3]);
        maxBBoxX = Math.max(bboxTab[0], bboxTab[2]);
        maxBBoxY = Math.max(bboxTab[1], bboxTab[3]);
        if (minBBoxX >= maxBBoxX || minBBoxY >= maxBBoxY)
        {
            LOG.warn("empty BBox is ignored");
            bboxRect = null;
        }
    }

    /**
     * Convert color values from shading colorspace to RGB color values encoded
     * into an integer.
     *
     * @param values color values in shading colorspace.
     * @return RGB values encoded in an integer.
     * @throws java.io.IOException if the color conversion fails.
     */
    final int convertToRGB(float[] values) throws IOException
    {
        int normRGBValues;

        float[] rgbValues = shadingColorSpace.toRGB(values);
        normRGBValues = (int) (rgbValues[0] * 255);
        normRGBValues |= (int) (rgbValues[1] * 255) << 8;
        normRGBValues |= (int) (rgbValues[2] * 255) << 16;

        return normRGBValues;
    }
    
    ColorModel getColorModel()
    {
        return outputColorModel;
    }

    void dispose()
    {
        outputColorModel = null;
        shadingColorSpace = null;
    }

}
