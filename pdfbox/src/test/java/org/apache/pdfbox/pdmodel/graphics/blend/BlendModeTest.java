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

import org.apache.pdfbox.cos.COSName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class BlendModeTest
{

    /**
     * Check that BlendMode.* constant instances are not null. This could happen if the declaration
     * sequence is changed.
     */
    @Test
    void testInstances()
    {
        assertEquals(SeparableBlendMode.NORMAL, BlendMode.getInstance(COSName.NORMAL));
        assertEquals(SeparableBlendMode.NORMAL, BlendMode.getInstance(COSName.COMPATIBLE));
        assertEquals(SeparableBlendMode.MULTIPLY, BlendMode.getInstance(COSName.MULTIPLY));
        assertEquals(SeparableBlendMode.SCREEN, BlendMode.getInstance(COSName.SCREEN));
        assertEquals(SeparableBlendMode.OVERLAY, BlendMode.getInstance(COSName.OVERLAY));
        assertEquals(SeparableBlendMode.DARKEN, BlendMode.getInstance(COSName.DARKEN));
        assertEquals(SeparableBlendMode.LIGHTEN, BlendMode.getInstance(COSName.LIGHTEN));
        assertEquals(SeparableBlendMode.COLOR_DODGE, BlendMode.getInstance(COSName.COLOR_DODGE));
        assertEquals(SeparableBlendMode.COLOR_BURN, BlendMode.getInstance(COSName.COLOR_BURN));
        assertEquals(SeparableBlendMode.HARD_LIGHT, BlendMode.getInstance(COSName.HARD_LIGHT));
        assertEquals(SeparableBlendMode.SOFT_LIGHT, BlendMode.getInstance(COSName.SOFT_LIGHT));
        assertEquals(SeparableBlendMode.DIFFERENCE, BlendMode.getInstance(COSName.DIFFERENCE));
        assertEquals(SeparableBlendMode.EXCLUSION, BlendMode.getInstance(COSName.EXCLUSION));
        assertEquals(NonSeparableBlendMode.HUE, BlendMode.getInstance(COSName.HUE));
        assertEquals(NonSeparableBlendMode.SATURATION, BlendMode.getInstance(COSName.SATURATION));
        assertEquals(NonSeparableBlendMode.LUMINOSITY, BlendMode.getInstance(COSName.LUMINOSITY));
        assertEquals(NonSeparableBlendMode.COLOR, BlendMode.getInstance(COSName.COLOR));
    }

    /**
     * Check that COSName constants returned for BlendMode.* instances are not null. This could
     * happen if the declaration sequence is changed.
     */
    @Test
    void testCOSNames()
    {
        assertEquals(COSName.NORMAL, SeparableBlendMode.NORMAL.getCOSName());
        assertEquals(COSName.NORMAL, SeparableBlendMode.COMPATIBLE.getCOSName());
        assertEquals(COSName.MULTIPLY, SeparableBlendMode.MULTIPLY.getCOSName());
        assertEquals(COSName.SCREEN, SeparableBlendMode.SCREEN.getCOSName());
        assertEquals(COSName.OVERLAY, SeparableBlendMode.OVERLAY.getCOSName());
        assertEquals(COSName.DARKEN, SeparableBlendMode.DARKEN.getCOSName());
        assertEquals(COSName.LIGHTEN, SeparableBlendMode.LIGHTEN.getCOSName());
        assertEquals(COSName.COLOR_DODGE, SeparableBlendMode.COLOR_DODGE.getCOSName());
        assertEquals(COSName.COLOR_BURN, SeparableBlendMode.COLOR_BURN.getCOSName());
        assertEquals(COSName.HARD_LIGHT, SeparableBlendMode.HARD_LIGHT.getCOSName());
        assertEquals(COSName.SOFT_LIGHT, SeparableBlendMode.SOFT_LIGHT.getCOSName());
        assertEquals(COSName.DIFFERENCE, SeparableBlendMode.DIFFERENCE.getCOSName());
        assertEquals(COSName.EXCLUSION, SeparableBlendMode.EXCLUSION.getCOSName());
        assertEquals(COSName.HUE, NonSeparableBlendMode.HUE.getCOSName());
        assertEquals(COSName.SATURATION, NonSeparableBlendMode.SATURATION.getCOSName());
        assertEquals(COSName.LUMINOSITY, NonSeparableBlendMode.LUMINOSITY.getCOSName());
        assertEquals(COSName.COLOR, NonSeparableBlendMode.COLOR.getCOSName());
    }
}
