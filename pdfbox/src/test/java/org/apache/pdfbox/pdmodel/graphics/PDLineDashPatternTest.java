/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.pdfbox.pdmodel.graphics;

import static junit.framework.TestCase.assertEquals;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PDLineDashPatternTest
{

    /**
     * Test of getCOSObject method, of class PDLineDashPattern.
     */
    @Test
    public void testGetCOSObject()
    {
        COSArray ar = new COSArray();
        ar.add(COSInteger.ONE);
        ar.add(COSInteger.TWO);
        PDLineDashPattern dash = new PDLineDashPattern(ar, 3);
        COSArray dashBase = (COSArray) dash.getCOSObject();
        COSArray dashArray = (COSArray) dashBase.getObject(0);
        assertEquals(2, dashBase.size());
        assertEquals(2, dashArray.size());
        assertEquals(new COSFloat(1), dashArray.get(0));
        assertEquals(new COSFloat(2), dashArray.get(1));
        assertEquals(COSInteger.THREE, dashBase.get(1));
        System.out.println(dash);
    }
}
