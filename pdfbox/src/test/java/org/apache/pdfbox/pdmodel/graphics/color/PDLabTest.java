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

import java.util.Arrays;
import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.PDRange;

/**
 *
 * @author Tilman Hausherr
 */
public class PDLabTest extends TestCase
{

    /**
     * This test checks that getting default values do not alter the object, 
     * and checks getters and setters.
     */
    public void testLAB()
    {
        PDLab pdLab = new PDLab();
        COSArray cosArray = (COSArray) pdLab.getCOSObject();
        COSDictionary dict = (COSDictionary) cosArray.getObject(1);
        
        // test with default values
        assertEquals("Lab", pdLab.getName());
        assertEquals(3, pdLab.getNumberOfComponents());
        assertNotNull(pdLab.getInitialColor());
        assertTrue(Arrays.equals(new float[]{0,0,0}, pdLab.getInitialColor().getComponents()));
        assertEquals(0f, pdLab.getBlackPoint().getX());
        assertEquals(0f, pdLab.getBlackPoint().getY());
        assertEquals(0f, pdLab.getBlackPoint().getZ());
        assertEquals(1f, pdLab.getWhitepoint().getX());
        assertEquals(1f, pdLab.getWhitepoint().getY());
        assertEquals(1f, pdLab.getWhitepoint().getZ());
        assertEquals(-100f, pdLab.getARange().getMin());
        assertEquals(100f, pdLab.getARange().getMax());
        assertEquals(-100f, pdLab.getBRange().getMin());
        assertEquals(100f, pdLab.getBRange().getMax());
        assertEquals("read operations should not change the size of /Lab objects", 0, dict.size());
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
        assertEquals(-1f, pdLab.getARange().getMin());
        assertEquals(2f, pdLab.getARange().getMax());
        assertEquals(3f, pdLab.getBRange().getMin());
        assertEquals(4f, pdLab.getBRange().getMax());
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
        assertEquals(5f, pdLab.getWhitepoint().getX());
        assertEquals(6f, pdLab.getWhitepoint().getY());
        assertEquals(7f, pdLab.getWhitepoint().getZ());
        assertEquals(8f, pdLab.getBlackPoint().getX());
        assertEquals(9f, pdLab.getBlackPoint().getY());
        assertEquals(10f, pdLab.getBlackPoint().getZ());
        assertTrue(Arrays.equals(new float[]{0,0,3}, pdLab.getInitialColor().getComponents()));
    }

}
