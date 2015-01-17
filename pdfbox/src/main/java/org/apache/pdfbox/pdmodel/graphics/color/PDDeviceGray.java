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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.pdfbox.cos.COSName;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * A color space with black, white, and intermediate shades of gray.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDDeviceGray extends PDDeviceColorSpace
{
    /** The single instance of this class. */
    public static final PDDeviceGray INSTANCE = new PDDeviceGray();
    
    private final PDColor initialColor = new PDColor(new float[] { 0 }, this);

    private PDDeviceGray()
    {
    }

    @Override
    public String getName()
    {
        return COSName.DEVICEGRAY.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 1;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        return new float[] { value[0], value[0], value[0] };
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        int width = raster.getWidth();
        int height = raster.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] gray = new int[1];
        int[] rgb = new int[3];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, gray);
                rgb[0] = gray[0];
                rgb[1] = gray[0];
                rgb[2] = gray[0];
                image.getRaster().setPixel(x, y, rgb);
            }
        }

        return image;
    }
}
