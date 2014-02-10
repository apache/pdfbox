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

import java.awt.PaintContext;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This class represents the PaintContext of an axial shading.
 * 
 */
public class ColoredTilingContext implements PaintContext
{

    private ColorModel outputColorModel;
    private Raster tilingImage;

    private int xstep;
    private int ystep;
    private int lowerLeftX;
    private int lowerLeftY;

    /**
     * Constructor creates an instance to be used for fill operations.
     * 
     * @param cm the colormodel to be used
     * @param image the tiling image
     * @param xStep horizontal spacing between pattern cells
     * @param yStep vertical spacing between pattern cells
     * @param bBox bounding box of the tiling image
     * 
     */
    public ColoredTilingContext(ColorModel cm, Raster image, int xStep, int yStep, PDRectangle bBox)
    {
        outputColorModel = cm;
        tilingImage = image;
        xstep = Math.abs(xStep);
        ystep = Math.abs(yStep);
        lowerLeftX = (int) bBox.getLowerLeftX();
        lowerLeftY = (int) bBox.getLowerLeftY();
    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
        outputColorModel = null;
        tilingImage = null;
    }

    /**
     * {@inheritDoc}
     */
    public ColorModel getColorModel()
    {
        return outputColorModel;
    }

    /**
     * {@inheritDoc}
     */
    public Raster getRaster(int x, int y, int w, int h)
    {
        // get underlying colorspace
        ColorSpace cs = getColorModel().getColorSpace();
        // number of color components including alpha channel
        int numComponents = cs.getNumComponents() + 1;
        // all the data, plus alpha channel
        int[] imgData = new int[w * h * (numComponents)];

        // array holding the processed pixels
        int[] pixel = new int[numComponents];

        // for each device coordinate
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                // figure out what pixel we are at relative to the image
                int xloc = (x + i) - lowerLeftX;
                int yloc = (y + j) - lowerLeftY;

                xloc %= xstep;
                yloc %= ystep;

                if (xloc < 0)
                {
                    xloc = xstep + xloc;
                }
                if (yloc < 0)
                {
                    yloc = ystep + yloc;
                }

                // check if we are inside the image
                if (xloc < tilingImage.getWidth() && yloc < tilingImage.getHeight())
                {
                    tilingImage.getPixel(xloc, yloc, pixel);
                }
                else
                {
                    Arrays.fill(pixel, 0);
                }
                int base = (j * w + i) * numComponents;
                for (int c = 0; c < numComponents; c++)
                {
                    imgData[base + c] = pixel[c];
                }
            }
        }
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        raster.setPixels(0, 0, w, h, imgData);
        return raster;
    }
}
