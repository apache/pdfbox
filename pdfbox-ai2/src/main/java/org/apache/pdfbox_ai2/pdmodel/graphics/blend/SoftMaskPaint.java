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
package org.apache.pdfbox_ai2.pdmodel.graphics.blend;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * AWT Paint that adds a soft mask to the alpha channel of the existing parent paint. If the parent
 * paint does not have an alpha channel, a new raster is created.
 * 
 * @author Kühn & Weyh Software, GmbH
 */
public final class SoftMaskPaint implements Paint
{
    private final Paint parentPaint;
    private final Raster softMaskRaster;

    /**
     * Applies the soft mask to the parent.
     */
    public SoftMaskPaint(Paint parentPaint, Raster softMaskRaster)
    {
        this.parentPaint = parentPaint;
        this.softMaskRaster = softMaskRaster;
    }

    @Override
    public int getTransparency()
    {
        return Transparency.TRANSLUCENT;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
            Rectangle2D userBounds, AffineTransform at, RenderingHints hints)
    {
        try
        {
            PaintContext parentContext = parentPaint.createContext(null, deviceBounds, userBounds,
                    at, hints);
            return new Context(parentContext);
        }
        catch (IOException e)
        {
            return null; // context cannot be created
        }
    }

    private class Context implements PaintContext
    {
        private final PaintContext parentContext;
        private final ColorModel colorModel;
        private final int numColorComponents;
        private final ColorModel parentColorModel;

        Context(PaintContext parentContext) throws IOException
        {
            this.parentContext = parentContext;
            parentColorModel = parentContext.getColorModel();
            if (parentContext.getColorModel().hasAlpha())
            {
                colorModel = parentColorModel;
            }
            else
            {
                colorModel = new ComponentColorModel(parentContext.getColorModel()
                        .getColorSpace(), true, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            }
            numColorComponents = colorModel.getNumColorComponents();
        }

        @Override
        public ColorModel getColorModel()
        {
            return colorModel;
        }

        @Override
        public Raster getRaster(int x, int y, int w, int h)
        {
            Raster parentRaster = parentContext.getRaster(x, y, w, h);

            // getRaster can return the raster with origin (0,0) even if we applied for (x,y)
            int parentMinX = parentRaster.getMinX();
            int parentMinY = parentRaster.getMinY();

            WritableRaster result;
            if (parentRaster instanceof WritableRaster)
            {
                if (parentColorModel.equals(colorModel))
                {
                    result = parentRaster.createCompatibleWritableRaster();
                    result.setDataElements(-parentMinX, -parentMinY, parentRaster);
                }
                else
                {
                    BufferedImage parentImage = new BufferedImage(parentColorModel,
                            (WritableRaster) parentRaster,
                            parentColorModel.isAlphaPremultiplied(), null);
                    result = Raster.createWritableRaster(
                            colorModel.createCompatibleSampleModel(w, h), new Point(0, 0));
                    BufferedImage resultImage = new BufferedImage(colorModel, result, false, null);
                    Graphics graphics = resultImage.getGraphics();
                    graphics.drawImage(parentImage, 0, 0, null);
                    graphics.dispose();
                }
            }
            else
            {
                result = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, w, h, getColorModel()
                        .getNumComponents(), new Point(0, 0));
                ColorConvertOp colorConvertOp = new ColorConvertOp(
                        parentColorModel.getColorSpace(), colorModel.getColorSpace(), null);
                colorConvertOp.filter(parentRaster, result);
            }

            int softMaskMinX = softMaskRaster.getMinX();
            int softMaskMinY = softMaskRaster.getMinY();
            int softMaskMaxX = softMaskMinX + softMaskRaster.getWidth();
            int softMaskMaxY = softMaskMinY + softMaskRaster.getHeight();

            for (int j = 0; j < h; j++)
            {
                for (int i = 0; i < w; i++)
                {
                    int rx = x + i;
                    int ry = y + j;

                    int alpha;
                    if ((rx >= softMaskMinX) && (rx < softMaskMaxX) && (ry >= softMaskMinY)
                            && (ry < softMaskMaxY))
                    {
                        alpha = softMaskRaster.getSample(rx, ry, 0);
                    }
                    else
                    {
                        alpha = 0;
                    }
                    alpha = alpha * result.getSample(i, j, numColorComponents) / 255;
                    result.setSample(i, j, numColorComponents, alpha);
                }
            }

            return result;
        }

        @Override
        public void dispose()
        {
            // do nothing
        }
    }
}
