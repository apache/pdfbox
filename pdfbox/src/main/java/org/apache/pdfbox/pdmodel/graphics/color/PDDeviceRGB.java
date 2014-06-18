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

import java.awt.Transparency;
import java.awt.color.ColorSpace;

import java.awt.image.BufferedImage;

import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

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

    private static final ColorSpace COLOR_SPACE_RGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    private static final PDColor INITIAL_COLOR = new PDColor(new float[] { 0, 0, 0 });

    private PDDeviceRGB()
    {
    }

    @Override
    public String getName()
    {
        return COSName.DEVICERGB.getName();
    }

    /**
     * @inheritDoc
     */
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
        return INITIAL_COLOR;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        return COLOR_SPACE_RGB.toRGB(value);
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        ColorModel colorModel = new ComponentColorModel(COLOR_SPACE_RGB,
                false, false, Transparency.OPAQUE, raster.getDataBuffer().getDataType());

        return new BufferedImage(colorModel, raster, false, null);
    }
}
