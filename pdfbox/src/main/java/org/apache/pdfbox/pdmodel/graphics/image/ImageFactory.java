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
package org.apache.pdfbox.pdmodel.graphics.image;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

/**
 * An image factory.
 *
 * @author John Hewson
 * @author Brigitte Mathiak
 */
class ImageFactory
{
    protected ImageFactory()
    {
    }

    // sets Image XObject properties from an AWT buffered image
    protected static void setPropertiesFromAWT(BufferedImage awtImage, PDImageXObject pdImage)
    {
        pdImage.setColorSpace(toPDColorSpace(awtImage.getColorModel().getColorSpace()));
        pdImage.setBitsPerComponent(awtImage.getColorModel().getComponentSize(0));
        pdImage.setHeight(awtImage.getHeight());
        pdImage.setWidth(awtImage.getWidth());
    }

    // returns a PDColorSpace for a given AWT ColorSpace
    protected static PDColorSpace toPDColorSpace(ColorSpace awtColorSpace)
    {
        if (awtColorSpace instanceof ICC_ColorSpace && !awtColorSpace.isCS_sRGB())
        {
            throw new UnsupportedOperationException("ICC color spaces not implemented");
        }
        else
        {
            switch (awtColorSpace.getType())
            {
                case ColorSpace.TYPE_RGB:  return PDDeviceRGB.INSTANCE;
                case ColorSpace.TYPE_GRAY: return PDDeviceGray.INSTANCE;
                case ColorSpace.TYPE_CMYK: return PDDeviceCMYK.INSTANCE;
                default: throw new UnsupportedOperationException("color space not implemented: " +
                        awtColorSpace.getType());
            }
        }
    }

    // returns the alpha channel of an image
    protected static BufferedImage getAlphaImage(BufferedImage image)
    {
        if (!image.getColorModel().hasAlpha())
        {
            return null;
        }

        // extract the alpha information
        WritableRaster alphaRaster = image.getAlphaRaster();
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, alphaRaster, false, null);
    }

    // returns the color channels of an image
    protected static BufferedImage getColorImage(BufferedImage image)
    {
        if (!image.getColorModel().hasAlpha())
        {
            return image;
        }

        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
        {
            throw new UnsupportedOperationException("only RGB color spaces are implemented");
        }

        // create an RGB image without alpha
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = rgbImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(image, 0, 0, null);

        return rgbImage;
    }
}
