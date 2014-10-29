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
package org.apache.pdfbox.pdmodel.graphics.pattern;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.rendering.PageDrawer;

/**
 * AWT Paint for a tiling pattern, which consists of a small repeating graphical figure.
 *
 * @author Andreas Lehmkühler
 * @author John Hewson
 */
public class TilingPaint implements Paint
{
    private final PDTilingPattern pattern;
    private final TexturePaint paint;

    /**
     * Creates a new colored tiling Paint.
     *
     * @param drawer renderer to render the page
     * @param pattern tiling pattern dictionary
     *
     * @throws java.io.IOException if something goes wrong while drawing the
     * pattern
     */
    public TilingPaint(PageDrawer drawer, PDTilingPattern pattern, AffineTransform xform)
            throws IOException
    {
        this.paint = new TexturePaint(getImage(drawer, pattern, null, null, xform),
                                      getAnchorRect(pattern));
        this.pattern = pattern;
    }

    /**
     * Creates a new uncolored tiling Paint.
     *
     * @param drawer renderer to render the page
     * @param pattern tiling pattern dictionary
     * @param colorSpace color space for this tiling
     * @param color color for this tiling
     *
     * @throws java.io.IOException if something goes wrong while drawing the pattern
     */
    public TilingPaint(PageDrawer drawer, PDTilingPattern pattern, PDColorSpace colorSpace,
                       PDColor color, AffineTransform xform) throws IOException
    {
        this.paint = new TexturePaint(getImage(drawer, pattern, colorSpace, color, xform),
                                      getAnchorRect(pattern));
        this.pattern = pattern;
    }

    // note: this is not called in TexturePaint subclasses, which is why we wrap TexturePaint
    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints)
    {
        // todo: use userBounds or deviceBounds to avoid scaling issue with Tracemonkey?
        AffineTransform xformPattern = (AffineTransform)xform.clone();
        xformPattern.concatenate(pattern.getMatrix().createAffineTransform());
        return paint.createContext(cm, deviceBounds, userBounds, xformPattern, hints);
    }

    // gets image in parent stream coordinates
    private static BufferedImage getImage(PageDrawer drawer, PDTilingPattern pattern,
                                          PDColorSpace colorSpace, PDColor color,
                                          AffineTransform xform) throws IOException
    {
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(outputCS, true, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        Rectangle2D anchor = getAnchorRect(pattern);
        float width = (float)Math.abs(anchor.getWidth());
        float height = (float)Math.abs(anchor.getHeight());

        // device transform (i.e. DPI)
        width *= (float)xform.getScaleX();
        height *= (float)xform.getScaleY();

        int rasterWidth = Math.max(1, ceiling(width));
        int rasterHeight = Math.max(1, ceiling(height));

        // create raster
        WritableRaster raster = cm.createCompatibleWritableRaster(rasterWidth, rasterHeight);
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        Graphics2D graphics = image.createGraphics();
        graphics.transform(xform); // device transform (i.e. DPI)
        drawer.drawTilingPattern(graphics, pattern, colorSpace, color);
        graphics.dispose();

        return image;
    }

    /**
     * Returns the closest integer which is larger than the given number.
     * Uses BigDecimal to avoid floating point error which would cause gaps in the tiling.
     */
    private static int ceiling(double num)
    {
        BigDecimal decimal = new BigDecimal(num);
        decimal.setScale(5, RoundingMode.CEILING); // 5 decimal places of accuracy
        return decimal.intValue();
    }

    @Override
    public int getTransparency()
    {
        return Transparency.TRANSLUCENT;
    }

    // includes XStep/YStep
    public static Rectangle2D getAnchorRect(PDTilingPattern pattern)
    {
        float xStep = pattern.getXStep();
        if (xStep == 0)
        {
            xStep = pattern.getBBox().getWidth();
        }

        float yStep = pattern.getYStep();
        if (yStep == 0)
        {
            yStep = pattern.getBBox().getHeight();
        }

        PDRectangle anchor = pattern.getBBox();
        return new Rectangle2D.Float(anchor.getLowerLeftX(), anchor.getLowerLeftY(), xStep, yStep);
    }
}
