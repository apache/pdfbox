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

import java.awt.Color;
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

import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT Paint for a tiling pattern, which consists of a small repeating graphical figure.
 * @author Andreas Lehmkühler
 * @author John Hewson
 */
public class TilingPaint implements Paint
{
    private static Color TRANSPARENT = new Color(0, 0, 0, 0);

    private final PDTilingPattern pattern;
    private final PDColorSpace colorSpace;
    private final PDColor color;

    /**
     * Creates a new colored tiling Paint.
     * @param pattern tiling pattern dictionary
     */
    public TilingPaint(PDTilingPattern pattern) throws IOException
    {
        this.pattern = pattern;
        this.colorSpace = null;
        this.color = null;
    }

    /**
     * Creates a new uncolored tiling Paint.
     * @param pattern tiling pattern dictionary
     * @param colorSpace color space for this tiling
     * @param color color for this tiling
     */
    public TilingPaint(PDTilingPattern pattern, PDColorSpace colorSpace, PDColor color)
            throws IOException
    {
        this.pattern = pattern;
        this.colorSpace = colorSpace;
        this.color = color;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
                                      Rectangle2D userBounds, AffineTransform xform,
                                      RenderingHints hints)
    {
        System.out.println("TilingPaint#createContext" +
                " rectangle: " + deviceBounds +
                " affineTransform: " + xform);
        try
        {
            ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm2 = new ComponentColorModel(outputCS, true, false,
                    Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

            // DISABLED
            //return new TilingPaintContext(cm2 /*HACK*/, deviceBounds, xform, getTilingImage(cm, xform));

            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // EXPERIMENT
            // ... CRAZY HACK:
            TexturePaint paint = new TexturePaint(getTilingImage(cm, xform), getTransformedRect(xform, false));
            return paint.createContext(cm, deviceBounds, userBounds, xform, hints); // ^^^^^ USE SPACE RECT, NO XFORM.
        }
        catch (IOException e)
        {
            e.printStackTrace(); // TODO !!!!!!
            // TODO: log
            return TRANSPARENT.createContext(cm, deviceBounds, userBounds,
                    xform, hints);
        }
    }

    //  gets rect in parent content stream coordinates
    private Rectangle getTransformedRect(AffineTransform transform, boolean applyCTM)
    {
        // pattern matrix
        Rectangle rect;
        if (pattern.getMatrix() == null)
        {
            rect = new Rectangle(pattern.getBBox().createDimension());
        }
        else
        {
            AffineTransform at = pattern.getMatrix().createAffineTransform();
            rect = new Rectangle(pattern.getBBox().createDimension());
            rect = at.createTransformedShape(rect).getBounds();
        }

        // x/y step
        if (pattern.getMatrix() != null)
        {
            // TODO can be -ve
            rect.width = Math.round(pattern.getXStep() * Math.abs(pattern.getMatrix().getXScale()));
            rect.height = Math.round(pattern.getYStep() * Math.abs(pattern.getMatrix().getYScale()));
        }
        else
        {
            rect.width = pattern.getXStep();
            rect.height = pattern.getYStep();
        }

        // ctm
        if (applyCTM)
        {
            rect = transform.createTransformedShape(rect).getBounds();
        }

        return rect;
    }

    // gets image in parent stream coordinates
    private BufferedImage getTilingImage(ColorModel colorModel, AffineTransform transform)
            throws IOException
    {
        // TODO use colorModel parameter
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(outputCS, true, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        Rectangle rect = getTransformedRect(transform, true);
        int width = 1000;
        int height = 1000;

        // create raster
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        // matrix
        AffineTransform patternMatrix;
        if (pattern.getMatrix() == null)
        {
            // identity
            patternMatrix = new AffineTransform();
        }
        else
        {
            // pattern matrix
            patternMatrix = pattern.getMatrix().createAffineTransform();

            // flip -ve x-scale
            if (patternMatrix.getScaleX() < -0)
            {
                patternMatrix.scale(-1, 1);
            }
        }

        // ************************************************************************************************************
        // I've figured this out: the pattern is drawn only once over the page background and then sampled. PDFBOX-1094
        // ************************************************************************************************************

        // ****TODO****  This code works for tiling_pattern1 but not for PDFBOX-1094 (instrument the fill commands to find out why)

        // NEW HACK: ASSUME STREAM TRANSFORM IS IDENTITY
        transform = AffineTransform.getScaleInstance(transform.getScaleX(), -transform.getScaleY()); // <--- HACK SCALING TAKEN FROM CTM (but should be from stream start)


        // TODO supposed to be relative to the parent stream's initial CTM, not its current CTM
        //patternMatrix.scale(transform.getScaleX(), -transform.getScaleY());
        transform.preConcatenate(patternMatrix);

        Matrix matrix = new Matrix();
        matrix.setFromAffineTransform(transform); // !!

        Graphics2D graphics = image.createGraphics();
        PageDrawer drawer = new PageDrawer();
        PDRectangle pdRect = new PDRectangle(0, 0, width, height);

        // NEW HACK: this is usually done in drawPage
        graphics.scale(1, -1);
        graphics.translate(0, -height);

        drawer.drawTilingPattern(graphics, pattern, pdRect, matrix, colorSpace, color);
        drawer.dispose();
        graphics.dispose();

        return image;
    }

    @Override
    public int getTransparency()
    {
        return Transparency.TRANSLUCENT;
    }
}
