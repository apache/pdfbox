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
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for function-based (Type 1) shading.
 * @author Andreas Lehmkühler
 * @author Tilman Hausherr
 */
class Type1ShadingContext extends ShadingContext implements PaintContext
{
    private static final Log LOG = LogFactory.getLog(Type1ShadingContext.class);

    private PDShadingType1 shading;
    private AffineTransform rat;
    private float[] domain;
    private Matrix matrix;
    private float[] background;
    private float[] bboxTab = new float[4];

    /**
     * Constructor creates an instance to be used for fill operations.
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * @param dBounds device bounds 
     */
    public Type1ShadingContext(PDShadingType1 shading, ColorModel colorModel, AffineTransform xform,
                               Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        super(shading, colorModel, xform, ctm, pageHeight, dBounds);
        this.shading = shading;
        
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
