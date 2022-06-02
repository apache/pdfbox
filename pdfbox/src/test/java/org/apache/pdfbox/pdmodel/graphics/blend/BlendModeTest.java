/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License")); you may not use this file except in compliance with
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
package org.apache.pdfbox.pdmodel.graphics.blend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class BlendModeTest
{

    /**
     * Check that BlendMode.* constant instances are not null.
     */
    @Test
    void testInstances()
    {
        assertEquals(BlendMode.NORMAL, BlendMode.getInstance(COSName.NORMAL));
        assertEquals(BlendMode.NORMAL, BlendMode.getInstance(COSName.COMPATIBLE));
        assertEquals(BlendMode.MULTIPLY, BlendMode.getInstance(COSName.MULTIPLY));
        assertEquals(BlendMode.SCREEN, BlendMode.getInstance(COSName.SCREEN));
        assertEquals(BlendMode.OVERLAY, BlendMode.getInstance(COSName.OVERLAY));
        assertEquals(BlendMode.DARKEN, BlendMode.getInstance(COSName.DARKEN));
        assertEquals(BlendMode.LIGHTEN, BlendMode.getInstance(COSName.LIGHTEN));
        assertEquals(BlendMode.COLOR_DODGE, BlendMode.getInstance(COSName.COLOR_DODGE));
        assertEquals(BlendMode.COLOR_BURN, BlendMode.getInstance(COSName.COLOR_BURN));
        assertEquals(BlendMode.HARD_LIGHT, BlendMode.getInstance(COSName.HARD_LIGHT));
        assertEquals(BlendMode.SOFT_LIGHT, BlendMode.getInstance(COSName.SOFT_LIGHT));
        assertEquals(BlendMode.DIFFERENCE, BlendMode.getInstance(COSName.DIFFERENCE));
        assertEquals(BlendMode.EXCLUSION, BlendMode.getInstance(COSName.EXCLUSION));
        assertEquals(BlendMode.HUE, BlendMode.getInstance(COSName.HUE));
        assertEquals(BlendMode.SATURATION, BlendMode.getInstance(COSName.SATURATION));
        assertEquals(BlendMode.LUMINOSITY, BlendMode.getInstance(COSName.LUMINOSITY));
        assertEquals(BlendMode.COLOR, BlendMode.getInstance(COSName.COLOR));

        COSArray cosArrayOverlay = new COSArray();
        cosArrayOverlay.add(COSName.OVERLAY);
        assertEquals(BlendMode.OVERLAY, BlendMode.getInstance(cosArrayOverlay));

        COSArray cosArrayInteger = new COSArray();
        cosArrayInteger.add(COSInteger.get(0));
        assertEquals(BlendMode.NORMAL, BlendMode.getInstance(cosArrayInteger));

    }

    @Test
    void testBlendModeNormal()
    {
        assertTrue(BlendMode.NORMAL.isSeparableBlendMode());
        assertNull(BlendMode.NORMAL.getBlendFunction());
        assertNotNull(BlendMode.NORMAL.getBlendChannelFunction());
        assertEquals(COSName.NORMAL, BlendMode.NORMAL.getCOSName());
        assertEquals(3f, BlendMode.NORMAL.getBlendChannelFunction().blendChannel(3f, 5f));

        assertEquals(COSName.NORMAL, BlendMode.COMPATIBLE.getCOSName());
    }

    @Test
    void testBlendModeMultiply()
    {
        assertTrue(BlendMode.MULTIPLY.isSeparableBlendMode());
        assertNull(BlendMode.MULTIPLY.getBlendFunction());
        assertNotNull(BlendMode.MULTIPLY.getBlendChannelFunction());
        assertEquals(COSName.MULTIPLY, BlendMode.MULTIPLY.getCOSName());
        assertEquals(15f, BlendMode.MULTIPLY.getBlendChannelFunction().blendChannel(3f, 5f));
    }

    @Test
    void testBlendModeScreen()
    {
        assertTrue(BlendMode.SCREEN.isSeparableBlendMode());
        assertNull(BlendMode.SCREEN.getBlendFunction());
        assertNotNull(BlendMode.SCREEN.getBlendChannelFunction());
        assertEquals(COSName.SCREEN, BlendMode.SCREEN.getCOSName());
        assertEquals(-7f, BlendMode.SCREEN.getBlendChannelFunction().blendChannel(3f, 5f));
    }

    @Test
    void testBlendModeOverlay()
    {
        assertTrue(BlendMode.OVERLAY.isSeparableBlendMode());
        assertNull(BlendMode.OVERLAY.getBlendFunction());
        assertNotNull(BlendMode.OVERLAY.getBlendChannelFunction());
        assertEquals(COSName.OVERLAY, BlendMode.OVERLAY.getCOSName());
        assertEquals(0f, BlendMode.OVERLAY.getBlendChannelFunction().blendChannel(1f, 0f));
        assertEquals(0.3f, BlendMode.OVERLAY.getBlendChannelFunction().blendChannel(0.5f, 0.3f));
    }

    @Test
    void testBlendModeDarken()
    {
        assertTrue(BlendMode.DARKEN.isSeparableBlendMode());
        assertNull(BlendMode.DARKEN.getBlendFunction());
        assertNotNull(BlendMode.DARKEN.getBlendChannelFunction());
        assertEquals(COSName.DARKEN, BlendMode.DARKEN.getCOSName());
        assertEquals(3f, BlendMode.DARKEN.getBlendChannelFunction().blendChannel(3f, 5f));
    }

    @Test
    void testBlendModeLighten()
    {
        assertTrue(BlendMode.LIGHTEN.isSeparableBlendMode());
        assertNull(BlendMode.LIGHTEN.getBlendFunction());
        assertNotNull(BlendMode.LIGHTEN.getBlendChannelFunction());
        assertEquals(COSName.LIGHTEN, BlendMode.LIGHTEN.getCOSName());
        assertEquals(5f, BlendMode.LIGHTEN.getBlendChannelFunction().blendChannel(3f, 5f));
    }

    @Test
    void testBlendModeColorDodge()
    {
        assertTrue(BlendMode.COLOR_DODGE.isSeparableBlendMode());
        assertNull(BlendMode.COLOR_DODGE.getBlendFunction());
        assertNotNull(BlendMode.COLOR_DODGE.getBlendChannelFunction());
        assertEquals(COSName.COLOR_DODGE, BlendMode.COLOR_DODGE.getCOSName());
        assertEquals(0f, BlendMode.COLOR_DODGE.getBlendChannelFunction().blendChannel(1f, 0f));
        assertEquals(1f, BlendMode.COLOR_DODGE.getBlendChannelFunction().blendChannel(0.3f, 0.7f));
    }

    @Test
    void testBlendModeColorBurn()
    {
        assertTrue(BlendMode.COLOR_BURN.isSeparableBlendMode());
        assertNull(BlendMode.COLOR_BURN.getBlendFunction());
        assertNotNull(BlendMode.COLOR_BURN.getBlendChannelFunction());
        assertEquals(COSName.COLOR_BURN, BlendMode.COLOR_BURN.getCOSName());
        assertEquals(1f, BlendMode.COLOR_BURN.getBlendChannelFunction().blendChannel(0f, 1f));
        assertEquals(0f, BlendMode.COLOR_BURN.getBlendChannelFunction().blendChannel(0.7f, 0.3f));
    }

    @Test
    void testBlendModeHardLight()
    {
        assertTrue(BlendMode.HARD_LIGHT.isSeparableBlendMode());
        assertNull(BlendMode.HARD_LIGHT.getBlendFunction());
        assertNotNull(BlendMode.HARD_LIGHT.getBlendChannelFunction());
        assertEquals(COSName.HARD_LIGHT, BlendMode.HARD_LIGHT.getCOSName());
        assertEquals(0f, BlendMode.HARD_LIGHT.getBlendChannelFunction().blendChannel(0f, 0.5f));
        assertEquals(0.2f, BlendMode.HARD_LIGHT.getBlendChannelFunction().blendChannel(0.2f, 0.5f));
        assertEquals(0.52f,
                BlendMode.HARD_LIGHT.getBlendChannelFunction().blendChannel(0.6f, 0.4f));
    }

    @Test
    void testBlendModeSoftLight()
    {
        assertTrue(BlendMode.SOFT_LIGHT.isSeparableBlendMode());
        assertNull(BlendMode.SOFT_LIGHT.getBlendFunction());
        assertNotNull(BlendMode.SOFT_LIGHT.getBlendChannelFunction());
        assertEquals(COSName.SOFT_LIGHT, BlendMode.SOFT_LIGHT.getCOSName());
        assertEquals(0.25f, BlendMode.SOFT_LIGHT.getBlendChannelFunction().blendChannel(0f, 0.5f));
        assertEquals(0.35f,
                BlendMode.SOFT_LIGHT.getBlendChannelFunction().blendChannel(0.2f, 0.5f));
        assertEquals(0.2f,
                BlendMode.SOFT_LIGHT.getBlendChannelFunction().blendChannel(0.5f, 0.2f));
    }

    @Test
    void testBlendModeDifference()
    {
        assertTrue(BlendMode.DIFFERENCE.isSeparableBlendMode());
        assertNull(BlendMode.DIFFERENCE.getBlendFunction());
        assertNotNull(BlendMode.DIFFERENCE.getBlendChannelFunction());
        assertEquals(COSName.DIFFERENCE, BlendMode.DIFFERENCE.getCOSName());
        assertEquals(2f, BlendMode.DIFFERENCE.getBlendChannelFunction().blendChannel(3f, 5f));
    }

    @Test
    void testBlendModeExclusion()
    {
        assertTrue(BlendMode.EXCLUSION.isSeparableBlendMode());
        assertNull(BlendMode.EXCLUSION.getBlendFunction());
        assertNotNull(BlendMode.EXCLUSION.getBlendChannelFunction());
        assertEquals(COSName.EXCLUSION, BlendMode.EXCLUSION.getCOSName());
    }

    @Test
    void testBlendModeHue()
    {
        assertFalse(BlendMode.HUE.isSeparableBlendMode());
        assertNotNull(BlendMode.HUE.getBlendFunction());
        assertNull(BlendMode.HUE.getBlendChannelFunction());
        assertEquals(COSName.HUE, BlendMode.HUE.getCOSName());
    }

    @Test
    void testBlendModeSaturation()
    {
        assertFalse(BlendMode.SATURATION.isSeparableBlendMode());
        assertNotNull(BlendMode.SATURATION.getBlendFunction());
        assertNull(BlendMode.SATURATION.getBlendChannelFunction());
        assertEquals(COSName.SATURATION, BlendMode.SATURATION.getCOSName());
    }

    @Test
    void testBlendModeLuminosity()
    {
        assertFalse(BlendMode.LUMINOSITY.isSeparableBlendMode());
        assertNotNull(BlendMode.LUMINOSITY.getBlendFunction());
        assertNull(BlendMode.LUMINOSITY.getBlendChannelFunction());
        assertEquals(COSName.LUMINOSITY, BlendMode.LUMINOSITY.getCOSName());
    }

    @Test
    void testBlendModeColor()
    {
        assertFalse(BlendMode.COLOR.isSeparableBlendMode());
        assertNotNull(BlendMode.COLOR.getBlendFunction());
        assertNull(BlendMode.COLOR.getBlendChannelFunction());
        assertEquals(COSName.COLOR, BlendMode.COLOR.getCOSName());
    }

}
