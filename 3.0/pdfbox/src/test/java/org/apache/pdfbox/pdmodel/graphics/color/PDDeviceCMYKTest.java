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

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Test for power user creation of a custom default CMYK color space.
 *
 * @author John Hewson
 */
class PDDeviceCMYKTest
{
    @Test
    void testCMYK() throws IOException
    {
        PDDeviceCMYK.INSTANCE = new CustomDeviceCMYK();
    }
    
    private static class CustomDeviceCMYK extends PDDeviceCMYK
    {
        protected CustomDeviceCMYK() throws IOException
        {
        }
    }

    /**
     * PDFBOX-5787: test for problems on Mac with jdk21 homebrew. If this test fails, then use
     * another jdk.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBox5787() throws IOException
    {
        ColorConvertOp colorConvertOp = new ColorConvertOp(null);
        String resourceName = "/org/apache/pdfbox/resources/icc/CGATS001Compat-v2-micro.icc";
        ICC_Profile iccProfile;
        try (InputStream is = new BufferedInputStream(PDDeviceCMYK.class.getResourceAsStream(resourceName)))
        {
            iccProfile = ICC_Profile.getInstance(is);
        }
        ICC_ColorSpace icc_ColorSpace = new ICC_ColorSpace(iccProfile);

        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 1, 1,
                4, new Point(0, 0));

        ColorModel colorModel = new ComponentColorModel(icc_ColorSpace,
                false, false, Transparency.OPAQUE, raster.getDataBuffer().getDataType());

        BufferedImage src = new BufferedImage(colorModel, raster, false, null);
        BufferedImage dest = new BufferedImage(raster.getWidth(), raster.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        colorConvertOp.filter(src, dest);
    }
}
