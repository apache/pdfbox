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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT Paint for a tiling pattern, which consists of a small repeating graphical figure.
 *
 * @author John Hewson
 */
class TilingPaint implements Paint
{
    private static final Log LOG = LogFactory.getLog(TilingPaint.class);
    private final Paint paint;
    private final Matrix patternMatrix;
    private static final int MAXEDGE;
    private static final String DEFAULTMAXEDGE = "3000";

    static 
    {
        String s = System.getProperty("pdfbox.rendering.tilingpaint.maxedge", DEFAULTMAXEDGE);
        int val;
        try
        {
            val = Integer.parseInt(s);
        }
        catch (NumberFormatException ex)
        {
            LOG.error("Default will be used", ex);
            val = Integer.parseInt(DEFAULTMAXEDGE);
        }
        MAXEDGE = val;
    }

    /**
     * Creates a new colored tiling Paint, i.e. one that has its own colors.
     *
     * @param drawer renderer to render the page
     * @param pattern tiling pattern dictionary
     * @param xform device scale transform
     *
     * @throws java.io.IOException if something goes wrong while drawing the pattern
     */
    TilingPaint(PageDrawer drawer, PDTilingPattern pattern, AffineTransform xform)
            throws IOException
    {
        this(drawer, pattern, null, null, xform);
    }

    /**
     * Creates a new tiling Paint. The parameters color and colorSpace must be null for a colored
     * tiling Paint (because it has its own colors), and non null for an uncolored tiling Paint.
     *
     * @param drawer renderer to render the page
     * @param pattern tiling pattern dictionary
     * @param colorSpace color space for this tiling
     * @param color color for this tiling
     * @param xform device scale transform
     *
     * @throws java.io.IOException if something goes wrong while drawing the pattern
     */
    TilingPaint(PageDrawer drawer, PDTilingPattern pattern, PDColorSpace colorSpace,
                       PDColor color, AffineTransform xform) throws IOException
    {
        // pattern space -> user space
        patternMatrix = Matrix.concatenate(drawer.getInitialMatrix(), pattern.getMatrix());
        Rectangle2D anchorRect = getAnchorRect(pattern);
        paint = new TexturePaint(getImage(drawer, pattern, colorSpace, color, xform, anchorRect), anchorRect);
    }

    /**
     * Not called in TexturePaint subclasses, which is why we wrap TexturePaint.
     */
    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints)
    {
        AffineTransform xformPattern = (AffineTransform)xform.clone();

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
    private BufferedImage getImage(PageDrawer drawer, PDTilingPattern pattern, PDColorSpace colorSpace, 
            PDColor color, AffineTransform xform, Rectangle2D anchorRect) throws IOException
    {
        float width = (float) Math.abs(anchorRect.getWidth());
        float height = (float) Math.abs(anchorRect.getHeight());

        // device scale transform (i.e. DPI) (see PDFBOX-1466.pdf)
        Matrix xformMatrix = new Matrix(xform);
        float xScale = Math.abs(xformMatrix.getScalingFactorX());
        float yScale = Math.abs(xformMatrix.getScalingFactorY());
        width *= xScale;
        height *= yScale;

        int rasterWidth = Math.max(1, ceiling(width));
        int rasterHeight = Math.max(1, ceiling(height));

        BufferedImage image = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_ARGB);

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
        graphics.scale(xScale, yScale);

        // apply only the scaling from the pattern transform, doing scaling here improves the
        // image quality and prevents large scale-down factors from creating huge tiling cells.
        Matrix newPatternMatrix;
        newPatternMatrix = Matrix.getScaleInstance(
                Math.abs(patternMatrix.getScalingFactorX()),
                Math.abs(patternMatrix.getScalingFactorY()));

        // move origin to (0,0)
        PDRectangle bBox = pattern.getBBox();
        newPatternMatrix.translate(-bBox.getLowerLeftX(), -bBox.getLowerLeftY());

        // render using PageDrawer
        drawer.drawTilingPattern(graphics, pattern, colorSpace, color, newPatternMatrix);
        graphics.dispose();

        return image;
    }

    /**
     * Returns the closest integer which is larger than the given number.
     * Uses BigDecimal to avoid floating point error which would cause gaps in the tiling.
     */
    private static int ceiling(double num)
    {
        BigDecimal decimal = BigDecimal.valueOf(num);
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
    private Rectangle2D getAnchorRect(PDTilingPattern pattern) throws IOException
    {
        PDRectangle bbox = pattern.getBBox();
        if (bbox == null)
        {
            throw new IOException("Pattern /BBox is missing");
        }
        float xStep = pattern.getXStep();
        if (Float.compare(xStep, 0) == 0)
        {
            LOG.warn("/XStep is 0, using pattern /BBox width");
            xStep = bbox.getWidth();
        }

        float yStep = pattern.getYStep();
        if (Float.compare(yStep, 0) == 0)
        {
            LOG.warn("/YStep is 0, using pattern /BBox height");
            yStep = bbox.getHeight();
        }

        float xScale = patternMatrix.getScalingFactorX();
        float yScale = patternMatrix.getScalingFactorY();
        float width = xStep * xScale;
        float height = yStep * yScale;

        if (Math.abs(width * height) > MAXEDGE * MAXEDGE)
        {
            // PDFBOX-3653: prevent huge sizes
            LOG.info("Pattern surface is too large, will be clipped");
            LOG.info("width: " + width + ", height: " + height);
            LOG.info("XStep: " + xStep + ", YStep: " + yStep);
            LOG.info("bbox: " + bbox);
            LOG.info("pattern matrix: " + pattern.getMatrix());
            LOG.info("concatenated matrix: " + patternMatrix);
            width = Math.min(MAXEDGE, Math.abs(width)) * Math.signum(width);
            height = Math.min(MAXEDGE, Math.abs(height)) * Math.signum(height);
            //TODO better solution needed
        }

        // returns the anchor rect with scaling applied
        return new Rectangle2D.Float(bbox.getLowerLeftX() * xScale,
                                     bbox.getLowerLeftY() * yScale,
                                     width, height);
    }
}
