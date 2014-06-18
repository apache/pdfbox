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
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.util.ResourceLoader;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import java.io.InputStream;
import java.util.Properties;

/**
 * Allows colors to be specified according to the subtractive CMYK (cyan, magenta, yellow, black)
 * model typical of printers and other paper-based output devices.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDDeviceCMYK extends PDDeviceColorSpace
{
    /**  The single instance of this class. */
    public static final PDDeviceCMYK INSTANCE;
    static
    {
        try
        {
            INSTANCE = new PDDeviceCMYK();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private final ICC_ColorSpace awtColorSpace;
    private static final PDColor INITIAL_COLOR = new PDColor(new float[] { 0, 0, 0, 1 });

    private PDDeviceCMYK() throws IOException
    {
        awtColorSpace = getAWTColorSpace();
    }

    // loads the ICC color profile for CMYK
    private static ICC_ColorSpace getAWTColorSpace() throws IOException
    {
        ICC_ColorSpace colorSpace;
        InputStream profile = null;
        try
        {
            Properties properties = ResourceLoader.loadProperties(
                    "org/apache/pdfbox/resources/PDDeviceCMYK.properties", new Properties());

            profile = ResourceLoader.loadResource(properties.getProperty("DeviceCMYK"));
            if (profile == null)
            {
                throw new IOException("Default CMYK color profile could not be loaded");
            }
            ICC_Profile iccProfile = ICC_Profile.getInstance(profile);
            colorSpace = new ICC_ColorSpace(iccProfile);
        }
        finally
        {
            IOUtils.closeQuietly(profile);
        }
        return colorSpace;
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
        return INITIAL_COLOR;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        return awtColorSpace.toRGB(value);
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        return toRGBImageAWT(raster, awtColorSpace);
    }
}
