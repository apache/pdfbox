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

package org.apache.pdfbox.rendering;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * A Paint which applies a soft mask to an underlying Paint.
 * 
 * @author John Hewson
 */
class SoftMask implements Paint
{
    private static final ColorModel ARGB_COLOR_MODEL =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getColorModel();

    private final Paint paint;
    private final BufferedImage mask;
    private final Rectangle2D bboxDevice;

    /**
     * Creates a new soft mask paint.
     *
     * @param paint underlying paint.
     * @param mask soft mask
     * @param bboxDevice bbox of the soft mask in the underlying Graphics2D device space
     */
    SoftMask(Paint paint, BufferedImage mask, Rectangle2D bboxDevice)
    {
        this.paint = paint;
        this.mask = mask;
        this.bboxDevice = bboxDevice;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
                                      Rectangle2D userBounds, AffineTransform xform,
                                      RenderingHints hints)
    {
        PaintContext ctx = paint.createContext(cm, deviceBounds, userBounds, xform, hints);
        return new SoftPaintContext(cm, deviceBounds, userBounds, xform, hints, ctx);
    }

    @Override
    public int getTransparency()
    {
        return TRANSLUCENT;
    }

    private class SoftPaintContext implements PaintContext
    {
        private final PaintContext context;

        SoftPaintContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                         AffineTransform xform, RenderingHints hints, PaintContext context)
        {
            this.context = context;
        }

        @Override
        public ColorModel getColorModel()
        {
            return ARGB_COLOR_MODEL;
        }

        @Override
        public Raster getRaster(int x1, int y1, int w, int h)
        {
            WritableRaster raster = (WritableRaster)context.getRaster(x1, y1, w, h);
            ColorModel rasterCM = context.getColorModel();

            // buffer
            WritableRaster output = getColorModel().createCompatibleWritableRaster(w, h);

            // the soft mask has its own bbox
            x1 = x1 - (int)bboxDevice.getX();
            y1 = y1 - (int)bboxDevice.getY();

            int[] gray = new int[4];
            Object pixelInput = null;
            int[] pixelOutput = new int[4];
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    pixelInput = raster.getDataElements(x, y, pixelInput);

                    pixelOutput[0] = rasterCM.getRed(pixelInput);
                    pixelOutput[1] = rasterCM.getGreen(pixelInput);
                    pixelOutput[2] = rasterCM.getBlue(pixelInput);
                    pixelOutput[3] = rasterCM.getAlpha(pixelInput);
                    
                    // get the alpha value from the gray mask, if within mask bounds
                    gray[0] = 0;
                    if (x1 + x >= 0 && y1 + y >= 0 && x1 + x < mask.getWidth() && y1 + y < mask.getHeight())
                    {
                        mask.getRaster().getPixel(x1 + x, y1 + y, gray);

                        pixelOutput[3] = Math.round(pixelOutput[3] * (gray[0] / 255f));
                    }
                    output.setPixel(x, y, pixelOutput);
                }
            }

            return output;
        }

        @Override
        public void dispose()
        {
        }
    }
}
