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

import org.apache.pdfbox.cos.COSName;

/**
 * Colours in the DeviceRGB colour space are specified according to the additive
 * RGB (red-green-blue) colour model.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDDeviceRGB extends PDDeviceColorSpace
{
    /**  This is the single instance of this class. */
    public static final PDDeviceRGB INSTANCE = new PDDeviceRGB();
    
    private final PDColor initialColor = new PDColor(new float[] { 0, 0, 0 }, this);

    private PDDeviceRGB()
    {
    }

    @Override
    public String getName()
    {
        return COSName.DEVICERGB.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfComponents()
    {
        return 3;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, 1, 0, 1, 0, 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        return value;
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        //
        // WARNING: this method is performance sensitive, modify with care!
        //
        // Please read PDFBOX-3854 and PDFBOX-2092 and look at the related commits first.
        // The current code returns TYPE_INT_RGB images which prevents slowness due to threads
        // blocking each other when TYPE_CUSTOM images are used.
        BufferedImage image = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.setData(raster);
        return image;
    }

    @Override
    public BufferedImage toRawImage(WritableRaster raster) throws IOException
    {
        // Device RGB is not specified, as its the colors of whatever device you use. The user
        // should use the toRGBImage().
        return null;
    }
}
