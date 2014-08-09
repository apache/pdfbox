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
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
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

    protected final Rectangle deviceBounds;
    protected PDColorSpace shadingColorSpace;
    protected PDRectangle bboxRect;
    protected float minBBoxX, minBBoxY, maxBBoxX, maxBBoxY;
    protected ColorModel outputColorModel;

    public ShadingContext(PDShading shading, ColorModel cm, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        deviceBounds = dBounds;
        shadingColorSpace = shading.getColorSpace();
        
        // create the output color model using RGB+alpha as color space
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);        
        
        bboxRect = shading.getBBox();
        if (bboxRect != null)
        {
            transformBBox(ctm, xform);
        }
    }

    private void transformBBox(Matrix ctm, AffineTransform xform)
    {
        float[] bboxTab = new float[4];
        bboxTab[0] = bboxRect.getLowerLeftX();
        bboxTab[1] = bboxRect.getLowerLeftY();
        bboxTab[2] = bboxRect.getUpperRightX();
        bboxTab[3] = bboxRect.getUpperRightY();
        if (ctm != null)
        {
            // transform the coords using the given matrix
            ctm.createAffineTransform().transform(bboxTab, 0, bboxTab, 0, 2);
        }
        xform.transform(bboxTab, 0, bboxTab, 0, 2);
        minBBoxX = Math.min(bboxTab[0],bboxTab[2]);
        minBBoxY = Math.min(bboxTab[1],bboxTab[3]);
        maxBBoxX = Math.max(bboxTab[0],bboxTab[2]);
        maxBBoxY = Math.max(bboxTab[1],bboxTab[3]);
        if (minBBoxX >= maxBBoxX || minBBoxY >= maxBBoxY)
        {
            LOG.warn("empty BBox is ignored");
            bboxRect = null;
        }
    }

}
