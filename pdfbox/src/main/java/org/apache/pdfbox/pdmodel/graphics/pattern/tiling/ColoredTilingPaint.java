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

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPatternResources;
import org.apache.pdfbox.util.Matrix;

/**
 * This represents the Paint of an axial shading.
 * 
 */
public class ColoredTilingPaint implements Paint
{
    private PDTilingPatternResources patternResources;
    private ColorModel outputColorModel;

    /**
     * Constructor.
     * 
     * @param resources tiling pattern resources
     * 
     */
    public ColoredTilingPaint(PDTilingPatternResources resources)
    {
        patternResources = resources;
    }

    /**
     * {@inheritDoc}
     */
    public int getTransparency()
    {
        return Transparency.TRANSLUCENT;
    }

    /**
     * {@inheritDoc}
     */
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
            AffineTransform xform, RenderingHints hints)
    {
        // get the pattern matrix
        Matrix patternMatrix = patternResources.getMatrix();
        AffineTransform patternAT = patternMatrix != null ? patternMatrix.createAffineTransform() : null;

        // get the bounding box
        PDRectangle box = patternResources.getBBox();
        Rectangle2D rect = new Rectangle((int) box.getLowerLeftX(), (int) box.getLowerLeftY(),
                (int) box.getWidth(), (int) box.getHeight());
        
        rect = xform.createTransformedShape(rect).getBounds2D();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        
        if (patternAT != null)
        {
            rect = patternAT.createTransformedShape(rect).getBounds2D();
        }
        PDRectangle bBox = new PDRectangle((float) rect.getMinX(), (float) rect.getMinY(), (float) rect.getMaxX(),
                (float) rect.getMaxY());

        // xStep + yStep
        double[] steps = new double[] { patternResources.getXStep(), patternResources.getYStep() };
        xform.deltaTransform(steps, 0, steps, 0, 1);
        if (patternAT != null)
        {
            patternAT.deltaTransform(steps, 0, steps, 0, 1);
        }
        int xStep = (int) (steps[0]);
        int yStep = (int) (steps[1]);

        ColorSpace outputCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        outputColorModel = new ComponentColorModel(outputCS, true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
        WritableRaster raster = outputColorModel.createCompatibleWritableRaster(width, height);

        BufferedImage image = new BufferedImage(outputColorModel, raster, false, null);
        BufferedImage tilingImage = null;
        try
        {
            PageDrawer drawer = new PageDrawer();
            drawer.drawStream(image.getGraphics(), (COSStream) patternResources.getCOSObject(),
                    patternResources.getResources(), box);
            drawer.dispose();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        AffineTransform imageTransform = null;
        if (patternAT != null && !patternAT.isIdentity())
        {
            // get the scaling factor for each dimension
            imageTransform = AffineTransform.getScaleInstance(patternMatrix.getXScale(), patternMatrix.getYScale());
        }
        else
        {
            imageTransform = new AffineTransform();
        }
        imageTransform.scale(1.0, -1.0);
        imageTransform.translate(0, -height);
        AffineTransformOp scaleOP = new AffineTransformOp(imageTransform, AffineTransformOp.TYPE_BILINEAR);
        tilingImage = scaleOP.filter(image, null);

        return new ColoredTilingContext(outputColorModel, tilingImage.getData(), xStep, yStep, bBox);
    }
}
