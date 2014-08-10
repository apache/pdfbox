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

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.Matrix;

/**
 * A base class to handle stuff that is common to all shading types.
 *
 * @author Shaola Ren
 * @author Tilman Hausherr
 */
public abstract class ShadingContext
{
    private static final Log LOG = LogFactory.getLog(ShadingContext.class);

    protected final PDShadingResources shading;
    protected final Rectangle deviceBounds;
    protected ColorSpace shadingColorSpace;
    protected PDColorSpace colorSpace;
    protected PDRectangle bboxRect;
    protected float minBBoxX, minBBoxY, maxBBoxX, maxBBoxY;
    protected ColorModel outputColorModel;
    protected PDFunction shadingTinttransform;

    public ShadingContext(PDShadingResources shading, ColorModel cm, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        this.shading = shading;
        deviceBounds = dBounds;

        colorSpace = shading.getColorSpace();

        // get the shading colorSpace
        try
        {
            if (!(colorSpace instanceof PDDeviceRGB))
            {
                // we have to create an instance of the shading colorspace if it isn't RGB
                shadingColorSpace = colorSpace.getJavaColorSpace();
                // get the tint transformation function if the colorspace has one
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
        // create the output colormodel using RGB+alpha as colorspace
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        bboxRect = shading.getBBox();
        if (bboxRect != null)
        {
            transformBBox(ctm, xform, pageHeight);
        }
    }

    private void transformBBox(Matrix ctm, AffineTransform xform, int pageHeight)
    {
        float[] bboxTab = new float[4];
        bboxTab[0] = bboxRect.getLowerLeftX();
        bboxTab[1] = bboxRect.getLowerLeftY();
        bboxTab[2] = bboxRect.getUpperRightX();
        bboxTab[3] = bboxRect.getUpperRightY();
        if (ctm != null)
        {
            // the shading is used in combination with the sh-operator
            // transform the coords from shading to user space
            ctm.createAffineTransform().transform(bboxTab, 0, bboxTab, 0, 2);
            // move the 0,0-reference
            bboxTab[1] = pageHeight - bboxTab[1];
            bboxTab[3] = pageHeight - bboxTab[3];
        }
        else
        {
            // the shading is used as pattern colorspace in combination
            // with a fill-, stroke- or showText-operator
            float translateY = (float) xform.getTranslateY();
            // move the 0,0-reference including the y-translation from user to device space
            bboxTab[1] = pageHeight + translateY - bboxTab[1];
            bboxTab[3] = pageHeight + translateY - bboxTab[3];
        }
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

    // convert color to RGB color values encoded into an integer.
    protected int convertToRGB(float[] values)
    {
        int normRGBValues = 0;
        // convert color values from shading colorspace to RGB if necessary
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
        normRGBValues = (int) (values[0] * 255);
        normRGBValues |= (((int) (values[1] * 255)) << 8);
        normRGBValues |= (((int) (values[2] * 255)) << 16);
        return normRGBValues;
    }

    /**
     * Returns the function used for the shading tint transformation.
     *
     * @return the shading tint transformation function
     */
    public PDFunction getShadingTintTransform()
    {
        return shadingTinttransform;
    }
}
