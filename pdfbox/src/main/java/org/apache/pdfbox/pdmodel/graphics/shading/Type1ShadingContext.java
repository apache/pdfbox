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

import java.awt.PaintContext;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for function-based (Type 1) shading.
 * @author Andreas Lehmkühler
 * @author Tilman Hausherr
 */
class Type1ShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(Type1ShadingContext.class);

    private ColorModel outputColorModel;
    private PDColorSpace shadingColorSpace;
    private PDShadingType1 shading;
    private AffineTransform rat;
    private float[] domain;
    private Matrix matrix;
    private float[] background;
    private PDRectangle bboxRect;
    private float[] bboxTab = new float[4];

    /**
     * Constructor creates an instance to be used for fill operations.
     * @param shading the shading type to be used
     * @param cm the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     */
    public Type1ShadingContext(PDShadingType1 shading, ColorModel cm, AffineTransform xform,
                               Matrix ctm, int pageHeight) throws IOException
    {
        this.shading = shading;
        bboxRect = shading.getBBox();
        if (bboxRect != null)
        {
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
        }
        reOrder(bboxTab, 0, 2);
        reOrder(bboxTab, 1, 3);
        if (bboxTab[0] >= bboxTab[2] || bboxTab[1] >= bboxTab[3])
        {
            bboxRect = null;
        }
        
        // color space
        shadingColorSpace = this.shading.getColorSpace();
        // create the output colormodel using RGB+alpha as colorspace
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
        
        // spec p.308
        // (Optional) An array of four numbers [ xmin xmax ymin ymax ] 
        // specifying the rectangular domain of coordinates over which the 
        // color function(s) are defined. Default value: [ 0.0 1.0 0.0 1.0 ].
        if (this.shading.getDomain() != null)
        {
            domain = this.shading.getDomain().toFloatArray();
        }
        else
        {
            domain = new float[]
            {
                0, 1, 0, 1
            };
        }

        matrix = this.shading.getMatrix();
        if (matrix == null)
        {
            matrix = new Matrix();
        }

        try
        {
            // get inverse transform to be independent of 
            // shading matrix and current user / device space 
            // when handling actual pixels in getRaster()
            rat = matrix.createAffineTransform().createInverse();
            rat.concatenate(ctm.createAffineTransform().createInverse());
            rat.concatenate(xform.createInverse());
        }
        catch (NoninvertibleTransformException ex)
        {
            LOG.error(ex,ex);
        }

        // get background values if available
        COSArray bg = shading.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
        }
    }
    
    // this helper method is used to arrange the array 
    // to denote the left upper corner and right lower corner of the BBox
    // i is always < j
    private void reOrder(float[] array, int i, int j)
    {
        if (array[i] > array[j])
        {
            float tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    @Override
    public void dispose()
    {
        outputColorModel = null;
        shadingColorSpace = null;
        shading = null;
    }

    @Override
    public ColorModel getColorModel()
    {
        return outputColorModel;
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h)
    {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        int[] data = new int[w * h * 4];
        for (int j = 0; j < h; j++)
        {
            int currentY = y + j;
            if (bboxRect != null)
            {
                if (currentY < bboxTab[1] || currentY > bboxTab[3])
                {
                    continue;
                }
            }
            for (int i = 0; i < w; i++)
            {
                int currentX = x + i;
                if (bboxRect != null)
                {
                    if (currentX < bboxTab[0] || currentX > bboxTab[2])
                    {
                        continue;
                    }
                }
                int index = (j * w + i) * 4;
                boolean useBackground = false;
                float[] values = new float[]
                {
                    x + i, y + j
                };
                rat.transform(values, 0, values, 0, 1);
                if (values[0] < domain[0] || values[0] > domain[1] || values[1] < domain[2] || values[1] > domain[3])
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
                // evaluate function
                if (useBackground)
                {
                    values = background;
                }
                else
                {
                    try
                    {
                        values = shading.evalFunction(values);
                    }
                    catch (IOException exception)
                    {
                        LOG.error("error while processing a function", exception);
                    }
                }
                // convert color values from shading color space to RGB
                if (shadingColorSpace != null)
                {
                    try
                    {
                        values = shadingColorSpace.toRGB(values);
                    }
                    catch (IOException exception)
                    {
                        LOG.error("error processing color space", exception);
                    }
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

    public float[] getDomain()
    {
        return domain;
    }
}
