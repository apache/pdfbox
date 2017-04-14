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

import java.net.URL;
import java.util.Arrays;

import org.apache.pdfbox.cos.COSName;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import java.io.InputStream;

/**
 * Allows colors to be specified according to the subtractive CMYK (cyan, magenta, yellow, black)
 * model typical of printers and other paper-based output devices.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public class PDDeviceCMYK extends PDDeviceColorSpace
{
    /**  The single instance of this class. */
    public static PDDeviceCMYK INSTANCE;
    static
    {
        INSTANCE = new PDDeviceCMYK();
    }

    private final PDColor initialColor = new PDColor(new float[] { 0, 0, 0, 1 }, this);
    private volatile ICC_ColorSpace awtColorSpace;
    private boolean usePureJavaCMYKConversion = false;

    protected PDDeviceCMYK()
    {
    }

    /**
     * Lazy load the ICC profile, because it's slow.
     */
    protected void init() throws IOException
    {
        // no need to synchronize this check as it is atomic
        if (awtColorSpace != null)
        {
            return;
        }
        synchronized (this)
        {
            // we might have been waiting for another thread, so check again
            if (awtColorSpace != null)
            {
                return;
            }
            // loads the ICC color profile for CMYK
            ICC_Profile iccProfile = getICCProfile();
            if (iccProfile == null)
            {
                throw new IOException("Default CMYK color profile could not be loaded");
            }
            awtColorSpace = new ICC_ColorSpace(iccProfile);

            // there is a JVM bug which results in a CMMException which appears to be a race
            // condition caused by lazy initialization of the color transform, so we perform
            // an initial color conversion while we're still in a static context, see PDFBOX-2184
            awtColorSpace.toRGB(new float[] { 0, 0, 0, 0 });
            usePureJavaCMYKConversion = System
                    .getProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion") != null;
        }
    }

    protected ICC_Profile getICCProfile() throws IOException
    {
        // Adobe Acrobat uses "U.S. Web Coated (SWOP) v2" as the default
        // CMYK profile, however it is not available under an open license.
        // Instead, the "ISO Coated v2 300% (basICColor)" is used, which
        // is an open alternative to the "ISO Coated v2 300% (ECI)" profile.

        String name = "org/apache/pdfbox/resources/icc/ISOcoated_v2_300_bas.icc";

        URL url = PDDeviceCMYK.class.getClassLoader().getResource(name);
        if (url == null)
        {
            throw new IOException("Error loading resource: " + name);
        }

        try (InputStream input = url.openStream())
        {
            return ICC_Profile.getInstance(input);
        }
    }

    @Override
    public String getName()
    {
        return COSName.DEVICECMYK.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 4;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1, 0, 1, 0, 1, 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value) throws IOException
    {
        init();
        return awtColorSpace.toRGB(value);
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        init();
        return toRGBImageAWT(raster, awtColorSpace);
    }

    @Override
    protected BufferedImage toRGBImageAWT(WritableRaster raster, ColorSpace colorSpace)
    {
        if (usePureJavaCMYKConversion)
        {
            BufferedImage dest = new BufferedImage(raster.getWidth(), raster.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            ColorSpace destCS = dest.getColorModel().getColorSpace();
            WritableRaster destRaster = dest.getRaster();
            float[] srcValues = new float[4];
            float[] lastValues = new float[] { -1.0f, -1.0f, -1.0f, -1.0f };
            float[] destValues = new float[3];
            int width = raster.getWidth();
            int startX = raster.getMinX();
            int height = raster.getHeight();
            int startY = raster.getMinY();
            for (int x = startX; x < width + startX; x++)
            {
                for (int y = startY; y < height + startY; y++)
                {
                    raster.getPixel(x, y, srcValues);
                    // check if the last value can be reused
                    if (!Arrays.equals(lastValues, srcValues))
                    {
                        for (int k = 0; k < 4; k++)
                        {
                            lastValues[k] = srcValues[k];
                            srcValues[k] = srcValues[k] / 255f;
                        }
                        // use CIEXYZ as intermediate format to optimize the color conversion
                        destValues = destCS.fromCIEXYZ(colorSpace.toCIEXYZ(srcValues));
                        for (int k = 0; k < destValues.length; k++)
                        {
                            destValues[k] = destValues[k] * 255f;
                        }
                    }
                    destRaster.setPixel(x, y, destValues);
                }
            }
            return dest;
        }
        else
        {
            return super.toRGBImageAWT(raster, colorSpace);
        }
    }
}
