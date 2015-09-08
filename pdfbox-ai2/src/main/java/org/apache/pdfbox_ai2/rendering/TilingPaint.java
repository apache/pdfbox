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
package org.apache.pdfbox_ai2.rendering;

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

import org.apache.pdfbox_ai2.pdmodel.common.PDRectangle;
import org.apache.pdfbox_ai2.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox_ai2.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox_ai2.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox_ai2.util.Matrix;

/**
 * AWT Paint for a tiling pattern, which consists of a small repeating graphical figure.
 *
 * @author John Hewson
 */
class TilingPaint implements Paint
{
    private final PDTilingPattern pattern;
    private final TexturePaint paint;
    private final PageDrawer drawer;

    /**
     * Creates a new colored tiling Paint.
     *
     * @param drawer renderer to render the page
     * @param pattern tiling pattern dictionary
     *
     * @throws java.io.IOException if something goes wrong while drawing the
     * pattern
     */
    TilingPaint(PageDrawer drawer, PDTilingPattern pattern, AffineTransform xform)
            throws IOException
    {
        this.drawer = drawer;
        this.pattern = pattern;
        this.paint = new TexturePaint(getImage(null, null, xform), getAnchorRect());
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
    TilingPaint(PageDrawer drawer, PDTilingPattern pattern, PDColorSpace colorSpace,
                       PDColor color, AffineTransform xform) throws IOException
    {
        this.drawer = drawer;
        this.pattern = pattern;
        this.paint = new TexturePaint(getImage(colorSpace, color, xform), getAnchorRect());
    }

    /**
     * Not called in TexturePaint subclasses, which is why we wrap TexturePaint.
     */
    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints)
    {
        AffineTransform xformPattern = (AffineTransform)xform.clone();

        // pattern space -> user space
        Matrix patternMatrix = Matrix.concatenate(drawer.getInitialMatrix(), pattern.getMatrix());

        // applies the pattern matrix with scaling removed
        AffineTransform patternNoScale = patternMatrix.createAffineTransform();
        patternNoScale.scale(1 / patternMatrix.getScalingFactorX(),
                             1 / patternMatrix.getScalingFactorY());
        xformPattern.concatenate(patternNoScale);

        return paint.createContext(cm, deviceBounds, userBounds, xformPattern, hints);
    }

    /**
     * Returns the pattern image in parent stream coordinates.
     */
    private BufferedImage getImage(PDColorSpace colorSpace, PDColor color,
                                          AffineTransform xform) throws IOException
    {
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(outputCS, true, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        Rectangle2D anchor = getAnchorRect();
        float width = (float)Math.abs(anchor.getWidth());
        float height = (float)Math.abs(anchor.getHeight());

        // device scale transform (i.e. DPI) (see PDFBOX-1466.pdf)
        Matrix xformMatrix = new Matrix(xform);
        width *= xformMatrix.getScalingFactorX();
        height *= xformMatrix.getScalingFactorY();

        int rasterWidth = Math.max(1, ceiling(width));
        int rasterHeight = Math.max(1, ceiling(height));

        // create raster
        WritableRaster raster = cm.createCompatibleWritableRaster(rasterWidth, rasterHeight);
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        Graphics2D graphics = image.createGraphics();

        // flip a -ve YStep around its own axis (see gs-bugzilla694385.pdf)
        if (pattern.getYStep() < 0)
        {
            graphics.translate(0, rasterHeight);
            graphics.scale(1, -1);
        }

        // flip a -ve XStep around its own axis
        if (pattern.getXStep() < 0)
        {
            graphics.translate(rasterWidth, 0);
            graphics.scale(-1, 1);
        }

        // device scale transform (i.e. DPI)
        graphics.scale(xformMatrix.getScalingFactorX(), xformMatrix.getScalingFactorY());

        // pattern space -> user space
        Matrix patternMatrix = Matrix.concatenate(drawer.getInitialMatrix(), pattern.getMatrix());

        // apply only the scaling from the pattern transform, doing scaling here improves the
        // image quality and prevents large scale-down factors from creating huge tiling cells.
        patternMatrix = Matrix.getScaleInstance(
                Math.abs(patternMatrix.getScalingFactorX()),
                Math.abs(patternMatrix.getScalingFactorY()));

        // move origin to (0,0)
        patternMatrix.concatenate(
                Matrix.getTranslateInstance(-pattern.getBBox().getLowerLeftX(),
                        -pattern.getBBox().getLowerLeftY()));

        // render using PageDrawer
        drawer.drawTilingPattern(graphics, pattern, colorSpace, color, patternMatrix);
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
        decimal = decimal.setScale(5, RoundingMode.CEILING); // 5 decimal places of accuracy
        return decimal.intValue();
    }

    @Override
    public int getTransparency()
    {
        return Transparency.TRANSLUCENT;
    }

    /**
     * Returns the anchor rectangle, which includes the XStep/YStep and scaling.
     */
    private Rectangle2D getAnchorRect()
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

        // pattern space -> user space
        Matrix patternMatrix = Matrix.concatenate(drawer.getInitialMatrix(), pattern.getMatrix());

        float xScale = patternMatrix.getScalingFactorX();
        float yScale = patternMatrix.getScalingFactorY();

        // returns the anchor rect with scaling applied
        PDRectangle anchor = pattern.getBBox();
        return new Rectangle2D.Float(anchor.getLowerLeftX() * xScale,
                                     anchor.getLowerLeftY() * yScale,
                                     xStep * xScale, yStep * yScale);
    }
}
