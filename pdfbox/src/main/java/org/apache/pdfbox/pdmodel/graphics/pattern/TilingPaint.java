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
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.TilingPatternDrawer;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT Paint for a tiling pattern, which consists of a small repeating graphical
 * figure.
 *
 * @author Andreas Lehmkühler
 * @author John Hewson
 */
public class TilingPaint extends TexturePaint
{
    /**
     * Creates a new colored tiling Paint.
     *
     * @param renderer renderer to render the page
     * @param pattern tiling pattern dictionary
     * @param matrix initial substream transformation matrix
     * @param xform initial graphics transform of the page
     * 
     * @throws java.io.IOException if something goes wrong while drawing the
     * pattern
     */
    public TilingPaint(PDFRenderer renderer, PDTilingPattern pattern, Matrix matrix, AffineTransform xform) throws IOException
    {
        super(getImage(renderer, pattern, null, null, matrix, xform), getTransformedRect(pattern, matrix));
    }

    /**
     * Creates a new uncolored tiling Paint.
     *
     * @param renderer renderer to render the page
     * @param pattern tiling pattern dictionary
     * @param colorSpace color space for this tiling
     * @param color color for this tiling
     * @param matrix initial substream transformation matrix
     * @param xform initial graphics transform of the page
     * 
     * @throws java.io.IOException if something goes wrong while drawing the pattern
     */
    public TilingPaint(PDFRenderer renderer, PDTilingPattern pattern, PDColorSpace colorSpace,
            PDColor color, Matrix matrix, AffineTransform xform) throws IOException
    {
        super(getImage(renderer, pattern, colorSpace, color, matrix, xform), getTransformedRect(pattern, matrix));
    }

    //  gets rect in parent content stream coordinates
    private static Rectangle2D getTransformedRect(PDTilingPattern pattern, Matrix matrix)
    {
        float x = pattern.getBBox().getLowerLeftX();
        float y = pattern.getBBox().getLowerLeftY();
        float width = pattern.getBBox().getWidth();
        float height = pattern.getBBox().getHeight();

        // xStep and yStep, but ignore 32767 steps
        if (pattern.getXStep() != 0 && pattern.getXStep() != Short.MAX_VALUE)
        {
            width = pattern.getXStep();
        }
        if (pattern.getYStep() != 0 && pattern.getYStep() != Short.MAX_VALUE)
        {
            height = pattern.getYStep();
        }

        Rectangle2D rectangle;
        AffineTransform at = matrix.createAffineTransform();
        Point2D p1 = new Point2D.Float(x, y);
        Point2D p2 = new Point2D.Float(x + width, y + height);
        at.transform(p1, p1);
        at.transform(p2, p2);
        // at.createTransformedShape(rect).getBounds2D() gets empty rectangle
        // when negative numbers, so we do it the hard way
        rectangle = new Rectangle2D.Float(
                (float) Math.min(p1.getX(), p2.getX()),
                (float) Math.min(p1.getY(), p2.getY()),
                (float) Math.abs(p2.getX() - p1.getX()),
                (float) Math.abs(p2.getY() - p1.getY()));
        return rectangle;
    }

    // get lower left coord of bbox
    private static Point2D getTransformedPoint(PDTilingPattern pattern, Matrix matrix)
    {
        float x = pattern.getBBox().getLowerLeftX();
        float y = pattern.getBBox().getLowerLeftY();
        float width = pattern.getBBox().getWidth();
        float height = pattern.getBBox().getHeight();

        AffineTransform at = matrix.createAffineTransform();
        Point2D p1 = new Point2D.Float(x, y);
        Point2D p2 = new Point2D.Float(x + width, y + height);
        at.transform(p1, p1);
        at.transform(p2, p2);
        return new Point2D.Float(
                (float) Math.min(p1.getX(), p2.getX()),
                (float) Math.min(p1.getY(), p2.getY()));
    }

    // gets image in parent stream coordinates
    private static BufferedImage getImage(PDFRenderer renderer, PDTilingPattern pattern,
            PDColorSpace colorSpace, PDColor color, Matrix matrix, AffineTransform xform) throws IOException
    {
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(outputCS, true, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        Rectangle2D rect = getTransformedRect(pattern, matrix);
        float width = (float)rect.getWidth();
        float height = (float)rect.getHeight();

        int rasterWidth = Math.max(1, ceiling(width * Math.abs(xform.getScaleX())));
        int rasterHeight = Math.max(1, ceiling(height * Math.abs(xform.getScaleY())));

        // create raster
        WritableRaster raster = cm.createCompatibleWritableRaster(rasterWidth, rasterHeight);
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        matrix = matrix.clone();
        Point2D p = getTransformedPoint(pattern, matrix);
        matrix.setValue(2, 0, matrix.getValue(2, 0) - (float) p.getX()); // tx
        matrix.setValue(2, 1, matrix.getValue(2, 1) - (float) p.getY()); // ty

        // TODO: need to make it easy to use a custom TilingPatternDrawer
        PageDrawer drawer = new TilingPatternDrawer(renderer);
        PDRectangle pdRect = new PDRectangle(0, 0, width, height);

        Graphics2D graphics = image.createGraphics();
        // transform without the translation
        AffineTransform at = new AffineTransform(
                xform.getScaleX(), xform.getShearY(),
                -xform.getShearX(), xform.getScaleY(),
                0, 0);
        graphics.transform(at);
        drawer.drawTilingPattern(graphics, pattern, pdRect, matrix, colorSpace, color);
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
}
