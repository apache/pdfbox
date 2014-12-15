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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * CIE-based colour spaces specify colours in a way that is independent of the characteristics
 * of any particular output device. They are based on an international standard for colour
 * specification created by the Commission Internationale de l'Éclairage (CIE).
 *
 * @author John Hewson
 */
public abstract class PDCIEBasedColorSpace extends PDColorSpace
{
    //
    // WARNING: this method is performance sensitive, modify with care!
    //
    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        // This method calls toRGB to convert images one pixel at a time. For matrix-based
        // CIE color spaces this is fast enough. However, it should not be used with any
        // color space which uses an ICC Profile as it will be far too slow.

        int width = raster.getWidth();
        int height = raster.getHeight();

        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();

        // always three components: ABC
        float[] abc = new float[3];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, abc);

                // 0..255 -> 0..1
                abc[0] /= 255;
                abc[1] /= 255;
                abc[2] /= 255;

                float[] rgb = toRGB(abc);

                // 0..1 -> 0..255
                rgb[0] *= 255;
                rgb[1] *= 255;
                rgb[2] *= 255;

                rgbRaster.setPixel(x, y, rgb);
            }
        }

        return rgbImage;
    }

    @Override
    public String toString()
    {
        return getName();   // TODO return more info
    }
}
