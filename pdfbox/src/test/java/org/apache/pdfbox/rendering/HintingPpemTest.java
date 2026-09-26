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
package org.apache.pdfbox.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.AffineTransform;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link PageDrawer#hintingPpem(AffineTransform)} - the derivation of the grid-fitting ppem from
 * the glyph-space-to-device transform, including the rule that rotated/sheared transforms are not
 * hinted. The input transform maps 1000-units-per-em glyph coordinates to device pixels.
 */
class HintingPpemTest
{
    @Test
    void testUprightScaleGivesPpem()
    {
        // 16px em: one normalized unit is 16/1000 device pixels
        assertEquals(16, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.016, 0.016)));
        assertEquals(11, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.011, 0.011)));
    }

    @Test
    void testYFlipIsStillHinted()
    {
        // typical PDF-to-device flip has a negative y scale but is still axis-aligned
        AffineTransform at = new AffineTransform(0.024, 0, 0, -0.024, 100, 200);
        assertEquals(24, PageDrawer.hintingPpem(at));
    }

    @Test
    void testAnisotropicUsesVerticalPpem()
    {
        // ppem is taken from the vertical scale
        AffineTransform at = new AffineTransform(0.020, 0, 0, -0.016, 0, 0);
        assertEquals(16, PageDrawer.hintingPpem(at));
    }

    @Test
    void testRotationIsStillHinted()
    {
        // rotated text (e.g. 90-degree vertical CJK) is grid-fit in upright glyph space at the
        // transform's scale; the rotation is applied afterwards. ppem is the rotation-invariant scale.
        AffineTransform at = AffineTransform.getScaleInstance(0.016, 0.016);
        at.rotate(Math.toRadians(30));
        assertEquals(16, PageDrawer.hintingPpem(at));

        AffineTransform vertical = new AffineTransform(0, 0.016, -0.016, 0, 0, 0); // 90-degree rotation
        assertEquals(16, PageDrawer.hintingPpem(vertical));
    }

    @Test
    void testShearTakesVerticalScale()
    {
        // a sheared (fake-italic) transform is hinted at its vertical-basis magnitude
        AffineTransform at = new AffineTransform(0.016, 0, 0.006, 0.016, 0, 0);
        assertEquals((int) Math.round(1000 * Math.hypot(0.006, 0.016)), PageDrawer.hintingPpem(at));
    }

    /**
     * Fractional device sizes round to the nearest integer ppem the way FreeType rounds a 26.6 size
     * ({@code (x + 32) >> 6}, i.e. half up), never truncate.
     */
    @Test
    void testFractionalScaleRoundsToNearestPpem()
    {
        assertEquals(19, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.01864, 0.01864))); // 18.64
        assertEquals(14, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.01408, 0.01408))); // 14.08
        assertEquals(13, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.0125, 0.0125)));   // 12.5 -> 13
        assertEquals(12, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.01249, 0.01249))); // 12.49 -> 12
        assertEquals(1, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.0005, 0.0005)));    // 0.5 -> 1
        assertEquals(0, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.00049, 0.00049)));  // 0.49 -> 0
    }

    /**
     * The transform PageDrawer actually hands over is device-xform composed with the text
     * rendering matrix and the font matrix: the ppem must come out as the real device pixels per em,
     * not the point size. 7pt at 300dpi is 29.17px -> 29; 10.5pt at 96dpi is exactly 14.
     */
    @Test
    void testComposedDeviceTransformGivesDevicePpem()
    {
        assertEquals(29, PageDrawer.hintingPpem(deviceGlyphTransform(300, 7, 0)));
        assertEquals(14, PageDrawer.hintingPpem(deviceGlyphTransform(96, 10.5, 0)));
        assertEquals(7, PageDrawer.hintingPpem(deviceGlyphTransform(72, 7, 0)));
        assertEquals(12, PageDrawer.hintingPpem(deviceGlyphTransform(96, 9, 0)));
    }

    /** Rotation composed with the device flip and a DPI scale: still the device pixels per em. */
    @Test
    void testRotationUnderDeviceTransform()
    {
        for (int degrees : new int[] { 0, 30, 45, 90, 180, 270, -90 })
        {
            assertEquals(29, PageDrawer.hintingPpem(deviceGlyphTransform(300, 7, degrees)),
                    "rotation " + degrees);
            assertEquals(14, PageDrawer.hintingPpem(deviceGlyphTransform(96, 10.5, degrees)),
                    "rotation " + degrees);
        }
    }

    /**
     * Horizontal text scaling (Tz) or a non-uniform text matrix changes only x: the ppem, which is
     * the vertical size, must be unaffected either way round.
     */
    @Test
    void testAnisotropicUnderDeviceTransform()
    {
        AffineTransform narrow = deviceGlyphTransform(96, 12, 0);
        narrow.scale(0.5, 1); // Tz 50
        assertEquals(16, PageDrawer.hintingPpem(narrow));
        AffineTransform wide = deviceGlyphTransform(96, 12, 0);
        wide.scale(2, 1);
        assertEquals(16, PageDrawer.hintingPpem(wide));
        AffineTransform tall = deviceGlyphTransform(96, 12, 0);
        tall.scale(1, 1.5);
        assertEquals(24, PageDrawer.hintingPpem(tall));
    }

    /**
     * Builds the glyph-space-to-device transform PageDrawer would see: PDFRenderer's device xform
     * (dpi scale and y flip) composed with a text rendering matrix at {@code sizePt} rotated by
     * {@code degrees}, composed with the 1/1000 font matrix. Normalized glyph space is 1000/em.
     */
    private static AffineTransform deviceGlyphTransform(double dpi, double sizePt, double degrees)
    {
        AffineTransform xform = new AffineTransform();
        xform.scale(dpi / 72, dpi / 72);
        xform.translate(0, 842);
        xform.scale(1, -1);
        AffineTransform trm = AffineTransform.getTranslateInstance(72, 700);
        trm.rotate(Math.toRadians(degrees));
        trm.scale(sizePt, sizePt);
        AffineTransform at = new AffineTransform(xform);
        at.concatenate(trm);
        at.scale(0.001, 0.001);
        return at;
    }

    @Test
    void testDegenerateTransformIsNotHinted()
    {
        assertEquals(0, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0, 0)));
        // sub-half-pixel em rounds to 0 ppem -> no hinting
        assertEquals(0, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.0004, 0.0004)));
    }
}
