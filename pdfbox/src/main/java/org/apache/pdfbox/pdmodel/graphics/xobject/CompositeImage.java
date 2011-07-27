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
package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;


/**
 * This class is responsible for combining a base image with an SMask-based transparency
 * image to form a composite image.
 * See section 11.5 of the pdf specification for details on Soft Masks.
 * <p/>
 * Briefly however, an Smask is a supplementary greyscale image whose RGB-values define
 * a transparency mask which, when combined appropriately with the base image,
 * allows per-pixel transparency to be applied.
 * <p/>
 * Note that Smasks are not required for any image and if the smask is not present
 * in the pdf file, the image will have no transparent pixels.
 *
 * @author Neil McErlean
 */
public class CompositeImage
{
    private BufferedImage baseImage;
    private BufferedImage smaskImage;

    /**
     * Standard constructor.
     * @param baseImage the base Image.
     * @param smaskImage the transparency image.
     *
     */
    public CompositeImage(BufferedImage baseImage, BufferedImage smaskImage)
    {
        this.baseImage = baseImage;
        this.smaskImage = smaskImage;
    }

    /**
     * This method applies the specified transparency mask to a given image and returns a new BufferedImage
     * whose alpha values are computed from the transparency mask (smask) image.
     */
    public BufferedImage createMaskedImage(COSArray decodeArray) throws IOException
    {
        // The decode array should only be [0 1] or [1 0]. See PDF spec.
        // [0 1] means the smask's RGB values give transparency. Default: see PDF spec section 8.9.5.1
        // [1 0] means the smask's RGB values give opacity.

        boolean isOpaque = false;
        if (decodeArray != null)
        {
            isOpaque = decodeArray.getInt(0) > decodeArray.getInt(1);
        }

        final int baseImageWidth = baseImage.getWidth();
        final int baseImageHeight = baseImage.getHeight();

        BufferedImage result = new BufferedImage(baseImageWidth, baseImageHeight, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < baseImageWidth; x++)
        {
            for (int y = 0; y < baseImageHeight; y++)
            {
                int rgb = baseImage.getRGB(x, y);
                int alpha = smaskImage.getRGB(x, y);

                // The smask image defines a transparency mask but it has no alpha values itself, instead
                // using the greyscale values to indicate transparency.
                // 0xAARRGGBB

                // We need to remove any alpha value in the main image.
                int rgbOnly = 0x00FFFFFF & rgb;

                // We need to use one of the rgb values as the new alpha value for the main image.
                // It seems the mask is greyscale, so it shouldn't matter whether we use R, G or B
                // as the indicator of transparency.
                if (isOpaque)
                {
                    alpha = ~alpha;
                }
                int alphaOnly = alpha << 24;

                result.setRGB(x, y, rgbOnly | alphaOnly);
            }
        }
        return result;
    }
}
