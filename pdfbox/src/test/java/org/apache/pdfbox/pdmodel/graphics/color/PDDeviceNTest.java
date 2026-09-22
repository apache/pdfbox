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
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;

import org.junit.Test;

/**
 * Tests the conversion of DeviceN colorspaces that have a single spot colorant in the attributes.
 */
public class PDDeviceNTest
{
    private static final String COLORANT = "PANTONE 342 C";
    
    private static COSArray cosArrayOf(float... floats)
    {
        COSArray ar = new COSArray();
        for (float f : floats)
        {
            ar.add(new COSFloat(f));
        }
        return ar;
    }

    // 1 input, exponent 1: a linear ramp from c0 to c1
    private static COSDictionary linearFunction(float[] c0, float[] c1)
    {
        COSDictionary function = new COSDictionary();
        function.setInt(COSName.FUNCTION_TYPE, 2);
        function.setItem(COSName.DOMAIN, cosArrayOf(0, 1));
        function.setItem(COSName.C0, cosArrayOf(c0));
        function.setItem(COSName.C1, cosArrayOf(c1));
        function.setInt(COSName.N, 1);
        return function;
    }

    // DeviceN with CMYK as alternate and a spot colorant (Separation with RGB as alternate)
    private static PDDeviceN createDeviceN(float[] spotAtZero, float[] spotAtOne,
            float[] cmykAtOne) throws IOException
    {
        COSArray separation = new COSArray();
        separation.add(COSName.SEPARATION);
        separation.add(COSName.getPDFName(COLORANT));
        separation.add(COSName.DEVICERGB);
        separation.add(linearFunction(spotAtZero, spotAtOne));

        COSDictionary colorants = new COSDictionary();
        colorants.setItem(COSName.getPDFName(COLORANT), separation);
        COSDictionary attributes = new COSDictionary();
        attributes.setItem(COSName.COLORANTS, colorants);

        COSArray deviceN = new COSArray();
        deviceN.add(COSName.DEVICEN);
        COSArray nameArray = new COSArray();
        nameArray.add(COSName.getPDFName(COLORANT));
        deviceN.add(nameArray);
        deviceN.add(COSName.DEVICECMYK);
        deviceN.add(linearFunction(new float[] { 0, 0, 0, 0 }, cmykAtOne));
        deviceN.add(attributes);
        return new PDDeviceN(deviceN, null);
    }

    private static float[] viaTintTransform(PDDeviceN deviceN, float tint) throws IOException
    {
        float[] alt = deviceN.getTintTransform().eval(new float[] { tint });
        return deviceN.getAlternateColorSpace().toRGB(alt);
    }

    /**
     * PDFBOX-5074: the spot colorant is black at zero tint, which would make the colorant (and
     * therefore the whole space) dark even if no ink is used. The tint transform must be used.
     */
    @Test
    public void testSpotColorantNotWhiteAtZeroTint() throws IOException
    {
        PDDeviceN deviceN = createDeviceN(new float[] { 0, 0, 0 }, new float[] { 0, 0.41f, 0.31f },
                new float[] { 1, 0.09f, 0.66f, 0.41f });

        float[] zero = deviceN.toRGB(new float[] { 0 });
        for (float component : zero)
        {
            assertTrue("no ink must be white, but was " + component, component > 0.9f);
        }
        assertArrayEquals(viaTintTransform(deviceN, 0), zero, 0.0001f);

        float[] tint = deviceN.toRGB(new float[] { 0.4f });
        assertArrayEquals(viaTintTransform(deviceN, 0.4f), tint, 0.0001f);
        // the attributes would have resulted in a dark green: 0, 0.16, 0.12
        assertTrue("expected a light color, but red was " + tint[0], tint[0] > 0.3f);
    }

    /**
     * Same for images: white where there is no tint, and not the black of the colorant.
     */
    @Test
    public void testSpotColorantNotWhiteAtZeroTintImage() throws IOException
    {
        PDDeviceN deviceN = createDeviceN(new float[] { 0, 0, 0 }, new float[] { 0, 0.41f, 0.31f },
                new float[] { 1, 0.09f, 0.66f, 0.41f });

        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, 2, 1, 1, null);
        raster.setSample(0, 0, 0, 0);
        raster.setSample(1, 0, 0, 102); // 0.4
        BufferedImage image = deviceN.toRGBImage(raster);

        int noInk = image.getRGB(0, 0);
        assertEquals(0xFF, (noInk >> 16) & 0xFF, 2);
        assertEquals(0xFF, (noInk >> 8) & 0xFF, 2);
        assertEquals(0xFF, noInk & 0xFF, 2);

        float[] expected = viaTintTransform(deviceN, 0.4f);
        int tint = image.getRGB(1, 0);
        assertEquals(expected[0] * 255, (tint >> 16) & 0xFF, 2);
        assertEquals(expected[1] * 255, (tint >> 8) & 0xFF, 2);
        assertEquals(expected[2] * 255, tint & 0xFF, 2);
    }

    /**
     * A spot colorant that is white at zero tint is what the attributes expect, they must still be
     * used and not the tint transform (which is red here to make the difference obvious).
     */
    @Test
    public void testSpotColorantWhiteAtZeroTint() throws IOException
    {
        PDDeviceN deviceN = createDeviceN(new float[] { 1, 1, 1 }, new float[] { 0, 0.41f, 0.31f },
                new float[] { 0, 1, 1, 0 });

        float[] tint = deviceN.toRGB(new float[] { 0.5f });
        assertEquals(0.5f, tint[0], 0.0001f);
        assertEquals(0.705f, tint[1], 0.0001f);
        assertEquals(0.655f, tint[2], 0.0001f);

        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, 1, 1, 1, null);
        raster.setSample(0, 0, 0, 255); // 1.0
        int rgb = deviceN.toRGBImage(raster).getRGB(0, 0);
        assertEquals(0, (rgb >> 16) & 0xFF, 2);
        assertEquals(0.41f * 255, (rgb >> 8) & 0xFF, 2);
        assertEquals(0.31f * 255, rgb & 0xFF, 2);
    }

    /**
     * Checking whether the spot colorant is white must not change later conversions: the result
     * of a Separation is cached per tint, and that must not make a tint near zero look like zero.
     */
    @Test
    public void testZeroTintCheckDoesNotAffectNearZeroTints() throws IOException
    {
        PDDeviceN deviceN = createDeviceN(new float[] { 1, 1, 1 }, new float[] { 0, 0, 0 },
                new float[] { 0, 1, 1, 0 });

        // 0.003 * 255 < 1, so this tint is in the same cache slot as zero
        float[] nearZero = deviceN.toRGB(new float[] { 0.003f });
        assertEquals(1 - 0.003f, nearZero[0], 0.0001f);
    }
}
