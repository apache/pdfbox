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

}
