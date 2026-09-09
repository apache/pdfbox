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

    @Test
    void testDegenerateTransformIsNotHinted()
    {
        assertEquals(0, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0, 0)));
        // sub-half-pixel em rounds to 0 ppem -> no hinting
        assertEquals(0, PageDrawer.hintingPpem(AffineTransform.getScaleInstance(0.0004, 0.0004)));
    }
}
