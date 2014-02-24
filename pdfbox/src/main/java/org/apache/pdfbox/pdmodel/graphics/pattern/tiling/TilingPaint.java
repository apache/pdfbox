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
package org.apache.pdfbox.pdmodel.graphics.pattern.tiling;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
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
public class TilingPaint extends TexturePaint
{
    /**
     * Creates a new colored tiling Paint.
     * @param pattern tiling pattern dictionary
     */
    public TilingPaint(PDTilingPattern pattern) throws IOException
    {
        super(getTilingImage(pattern, null ,null), getTransformedRect(pattern));
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
        super(getTilingImage(pattern, colorSpace, color), getTransformedRect(pattern));
    }

    //  gets rect in parent content stream coordinates
    private static Rectangle getTransformedRect(PDTilingPattern pattern)
    {
        AffineTransform at = pattern.getMatrix().createAffineTransform();
        Rectangle rect = new Rectangle(pattern.getBBox().createDimension());
        return at.createTransformedShape(rect).getBounds();
    }

    // gets image in parent stream coordinates
    private static BufferedImage getTilingImage(PDTilingPattern pattern, PDColorSpace colorSpace,
                                                PDColor color) throws IOException
    {
        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(outputCS, true, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        Rectangle rect = getTransformedRect(pattern);

        // create raster
        WritableRaster raster = cm.createCompatibleWritableRaster((int)rect.getWidth(),
                                                                  (int)rect.getHeight());
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        // TODO xStep and yStep

        // undo translation
        Matrix matrix = (Matrix)pattern.getMatrix().clone();
        matrix.setValue(2, 0, matrix.getValue(2, 0) - (float)rect.getX()); // tx
        matrix.setValue(2, 1, matrix.getValue(2, 1) - (float)rect.getY()); // ty

        int width = (int)rect.getWidth();
        int height = (int)rect.getHeight();

        PageDrawer drawer = new PageDrawer();
        PDRectangle pdRect = new PDRectangle(0, 0, width, height);

        Graphics2D graphics = image.createGraphics();
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
