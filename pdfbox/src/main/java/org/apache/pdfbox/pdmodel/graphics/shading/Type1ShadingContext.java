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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for function-based (Type 1) shading.
 *
 * @author Tilman Hausherr
 */
class Type1ShadingContext extends ShadingContext
{
    private static final Logger LOG = LogManager.getLogger(Type1ShadingContext.class);

    private PDShadingType1 type1ShadingType;
    private AffineTransform rat;
    private final float[] domain;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     */
    Type1ShadingContext(PDShadingType1 shading, ColorModel colorModel, AffineTransform xform,
                               Matrix matrix) throws IOException
    {
        super(shading, colorModel, xform, matrix);
        this.type1ShadingType = shading;

        // (Optional) An array of four numbers [ xmin xmax ymin ymax ] 
        // specifying the rectangular domain of coordinates over which the 
        // color function(s) are defined. Default value: [ 0.0 1.0 0.0 1.0 ].
        if (shading.getDomain() != null)
        {
            domain = shading.getDomain().toFloatArray();
        }
        else
        {
            domain = new float[] { 0, 1, 0, 1 };
        }

        try
        {
            // get inverse transform to be independent of 
            // shading matrix and current user / device space 
            // when handling actual pixels in getRaster()
            rat = shading.getMatrix().createAffineTransform().createInverse();
            rat.concatenate(matrix.createAffineTransform().createInverse());
            rat.concatenate(xform.createInverse());
        }
        catch (NoninvertibleTransformException ex)
        {
            LOG.error(() -> ex.getMessage() + ", matrix: " + matrix, ex);
            rat = new AffineTransform();
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        type1ShadingType = null;
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h)
    {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        int[] data = new int[w * h * 4];
        float[] values = new float[2];
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int index = (j * w + i) * 4;
                boolean useBackground = false;
                values[0] = x + i;
                values[1] = y + j;
                rat.transform(values, 0, values, 0, 1);
                if (values[0] < domain[0] || values[0] > domain[1] ||
                    values[1] < domain[2] || values[1] > domain[3])
                {
                    if (getBackground() == null)
                    {
                        continue;
                    }
                    useBackground = true;
                }

                // evaluate function
                float[] tmpValues; // "values" can't be reused due to different length
                if (useBackground)
                {
                    tmpValues = getBackground();
                }
                else
                {
                    try
                    {
                        tmpValues = type1ShadingType.evalFunction(values);
                    }
                    catch (IOException e)
                    {
                        LOG.error("error while processing a function", e);
                        continue;
                    }
                }

                // convert color values from shading color space to RGB
                PDColorSpace shadingColorSpace = getShadingColorSpace();
                if (shadingColorSpace != null)
                {
                    try
                    {
                        tmpValues = shadingColorSpace.toRGB(tmpValues);
                    }
                    catch (IOException e)
                    {
                        LOG.error("error processing color space", e);
                        continue;
                    }
                }
                data[index] = (int) (tmpValues[0] * 255);
                data[index + 1] = (int) (tmpValues[1] * 255);
                data[index + 2] = (int) (tmpValues[2] * 255);
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
