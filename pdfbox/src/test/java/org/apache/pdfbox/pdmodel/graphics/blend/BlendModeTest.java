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

import static junit.framework.TestCase.assertEquals;
import org.apache.pdfbox.cos.COSName;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class BlendModeTest
{
    public BlendModeTest()
    {
    }

    /**
     * Check that BlendMode.* constant instances are not null. This could happen if the declaration
     * sequence is changed.
     */
    @Test
    public void testInstances()
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
    }

    /**
     * Check that COSName constants returned for BlendMode.* instances are not null. This could
     * happen if the declaration sequence is changed.
     */
    @Test
    public void testCOSNames()
    {
        assertEquals(COSName.NORMAL, BlendMode.getCOSName(BlendMode.NORMAL));
        assertEquals(COSName.NORMAL, BlendMode.getCOSName(BlendMode.COMPATIBLE));
        assertEquals(COSName.MULTIPLY, BlendMode.getCOSName(BlendMode.MULTIPLY));
        assertEquals(COSName.SCREEN, BlendMode.getCOSName(BlendMode.SCREEN));
        assertEquals(COSName.OVERLAY, BlendMode.getCOSName(BlendMode.OVERLAY));
        assertEquals(COSName.DARKEN, BlendMode.getCOSName(BlendMode.DARKEN));
        assertEquals(COSName.LIGHTEN, BlendMode.getCOSName(BlendMode.LIGHTEN));
        assertEquals(COSName.COLOR_DODGE, BlendMode.getCOSName(BlendMode.COLOR_DODGE));
        assertEquals(COSName.COLOR_BURN, BlendMode.getCOSName(BlendMode.COLOR_BURN));
        assertEquals(COSName.HARD_LIGHT, BlendMode.getCOSName(BlendMode.HARD_LIGHT));
        assertEquals(COSName.SOFT_LIGHT, BlendMode.getCOSName(BlendMode.SOFT_LIGHT));
        assertEquals(COSName.DIFFERENCE, BlendMode.getCOSName(BlendMode.DIFFERENCE));
        assertEquals(COSName.EXCLUSION, BlendMode.getCOSName(BlendMode.EXCLUSION));
        assertEquals(COSName.HUE, BlendMode.getCOSName(BlendMode.HUE));
        assertEquals(COSName.SATURATION, BlendMode.getCOSName(BlendMode.SATURATION));
        assertEquals(COSName.LUMINOSITY, BlendMode.getCOSName(BlendMode.LUMINOSITY));
        assertEquals(COSName.COLOR, BlendMode.getCOSName(BlendMode.COLOR));
    }
}
