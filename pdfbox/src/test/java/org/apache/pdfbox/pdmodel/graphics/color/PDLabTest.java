/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class PDLabTest
{

    /**
     * This test checks that getting default values do not alter the object, 
     * and checks getters and setters.
     */
    @Test
    void testLAB()
    {
        PDLab pdLab = new PDLab();
        COSArray cosArray = (COSArray) pdLab.getCOSObject();
        COSDictionary dict = (COSDictionary) cosArray.getObject(1);
        
        // test with default values
        assertEquals("Lab", pdLab.getName());
        assertEquals(3, pdLab.getNumberOfComponents());
        assertNotNull(pdLab.getInitialColor());
        assertTrue(Arrays.equals(new float[]{0,0,0}, pdLab.getInitialColor().getComponents()));
        assertEquals(0f, pdLab.getBlackPoint().getX(), 0f);
        assertEquals(0f, pdLab.getBlackPoint().getY(), 0f);
        assertEquals(0f, pdLab.getBlackPoint().getZ(), 0f);
        assertEquals(1f, pdLab.getWhitepoint().getX(), 0f);
        assertEquals(1f, pdLab.getWhitepoint().getY(), 0f);
        assertEquals(1f, pdLab.getWhitepoint().getZ(), 0f);
        assertEquals(-100f, pdLab.getARange().getMin(), 0f);
        assertEquals(100f, pdLab.getARange().getMax(), 0f);
        assertEquals(-100f, pdLab.getBRange().getMin(), 0f);
        assertEquals(100f, pdLab.getBRange().getMax(), 0f);
        assertEquals(0, dict.size(), "read operations should not change the size of /Lab objects");
        dict.toString(); // rev 1571125 did a stack overflow here

        // test setting specific values
        PDRange pdRange = new PDRange();
        pdRange.setMin(-1);
        pdRange.setMax(2);
        pdLab.setARange(pdRange);
        pdRange = new PDRange();
        pdRange.setMin(3);
        pdRange.setMax(4);
        pdLab.setBRange(pdRange);
        assertEquals(-1f, pdLab.getARange().getMin(), 0f);
        assertEquals(2f, pdLab.getARange().getMax(), 0f);
        assertEquals(3f, pdLab.getBRange().getMin(), 0f);
        assertEquals(4f, pdLab.getBRange().getMax(), 0f);
        PDTristimulus pdTristimulus = new PDTristimulus();
        pdTristimulus.setX(5);
        pdTristimulus.setY(6);
        pdTristimulus.setZ(7);
        pdLab.setWhitePoint(pdTristimulus);
        pdTristimulus = new PDTristimulus();
        pdTristimulus.setX(8);
        pdTristimulus.setY(9);
        pdTristimulus.setZ(10);
        pdLab.setBlackPoint(pdTristimulus);
        assertEquals(5f, pdLab.getWhitepoint().getX(), 0f);
        assertEquals(6f, pdLab.getWhitepoint().getY(), 0f);
        assertEquals(7f, pdLab.getWhitepoint().getZ(), 0f);
        assertEquals(8f, pdLab.getBlackPoint().getX(), 0f);
        assertEquals(9f, pdLab.getBlackPoint().getY(), 0f);
        assertEquals(10f, pdLab.getBlackPoint().getZ(), 0f);
        assertTrue(Arrays.equals(new float[]{0,0,3}, pdLab.getInitialColor().getComponents()));
    }

    @Test
    void testClamp()
    {
        PDLab lab = new PDLab();
        assertEquals(-100, lab.getARange().getMin(), 0f);
        assertEquals(100, lab.getARange().getMax(), 0f);
        assertEquals(-100, lab.getBRange().getMin(), 0f);
        assertEquals(100, lab.getBRange().getMax(), 0f);
        assertEquals("[0.0, 100.0, -100.0, 100.0, -100.0, 100.0]", 
                Arrays.toString(lab.getDefaultDecode(1)));
        PDRange aRange = new PDRange();
        aRange.setMin(-160);
        aRange.setMax(160);
        lab.setARange(aRange);
        PDRange bRange = new PDRange();
        bRange.setMin(-160);
        bRange.setMax(160);
        lab.setBRange(bRange);
        float[] lab1 = new float[]{-101, -161, -161};
        float[] lab2 = new float[]{-100, -160, -160};
        float[] lab3 = new float[]{0, 0, 0};
        float[] lab4 = new float[]{100, 160, 160};
        float[] lab5 = new float[]{101, 161, 161};
        lab.clamp(lab1);
        lab.clamp(lab2);
        lab.clamp(lab3);
        lab.clamp(lab4);
        lab.clamp(lab5);
        assertEquals("[0.0, -160.0, -160.0]", Arrays.toString(lab1));
        assertEquals("[0.0, -160.0, -160.0]", Arrays.toString(lab2));
        assertEquals("[0.0, 0.0, 0.0]", Arrays.toString(lab3));
        assertEquals("[100.0, 160.0, 160.0]", Arrays.toString(lab4));
        assertEquals("[100.0, 160.0, 160.0]", Arrays.toString(lab5));
    }

    @Test
    void testToRGB()
    {
        // Author: ChatGPT
        PDLab lab = new PDLab();

        // PDF Lab values: L* is 0..100, a* and b* are -100..100.
        float[] black = lab.toRGB(new float[] { 0, 0, 0 });
        float[] white = lab.toRGB(new float[] { 100, 0, 0 });
        float[] red = lab.toRGB(new float[] { 53, 80, 67 });

        // Black and white should be at the ends of the RGB range.
        assertArrayEquals(new float[] { 0, 0, 0 }, black, 0.01f);
        assertArrayEquals(new float[] { 1, 1, 1 }, white, 0.02f);

        // Red should have a higher red component than green and blue.
        assertTrue(red[0] > red[1]);
        assertTrue(red[0] > red[2]);

        // All RGB components must be in the valid range.
        for (float component : red)
        {
            assertTrue(component >= 0 && component <= 1);
        }
    }
}
