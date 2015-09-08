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
package org.apache.pdfbox_ai2.pdmodel.graphics.color;

import java.net.URL;
import org.apache.pdfbox_ai2.cos.COSName;

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
        }
    }

    protected ICC_Profile getICCProfile() throws IOException
    {
        // Adobe Acrobat uses "U.S. Web Coated (SWOP) v2" as the default
        // CMYK profile, however it is not available under an open license.
        // Instead, the "ISO Coated v2 300% (basICColor)" is used, which
        // is an open alternative to the "ISO Coated v2 300% (ECI)" profile.

        String name = "org/apache/pdfbox_ai2/resources/icc/ISOcoated_v2_300_bas.icc";

        URL url = PDDeviceCMYK.class.getClassLoader().getResource(name);
        if (url == null)
        {
            throw new IOException("Error loading resource: " + name);
        }

        InputStream input = url.openStream();
        ICC_Profile iccProfile = ICC_Profile.getInstance(input);
        input.close();

        return iccProfile;
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
}
