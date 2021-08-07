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
package org.apache.pdfbox.pdmodel.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class PDImmutableRectangleTest
{
    private PDRectangle rect = PDRectangle.A4;

    PDImmutableRectangleTest()
    {
    }

    /**
     * Test that class PDImmutableRectangle is used for predefined.
     */
    @Test
    void testClass()
    {
        Assertions.assertTrue(rect instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A0 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A1 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A2 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A3 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A4 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A5 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.A6 instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.LEGAL instanceof PDImmutableRectangle);
        Assertions.assertTrue(PDRectangle.LETTER instanceof PDImmutableRectangle);
    }

    /**
     * Test of setUpperRightY method, of class PDImmutableRectangle.
     */
    @Test
    void testSetUpperRightY()
    {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> rect.setUpperRightY(0));
    }

    /**
     * Test of setUpperRightX method, of class PDImmutableRectangle.
     */
    @Test
    void testSetUpperRightX()
    {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> rect.setUpperRightX(0));
    }

    /**
     * Test of setLowerLeftY method, of class PDImmutableRectangle.
     */
    @Test
    void testSetLowerLeftY()
    {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> rect.setLowerLeftY(0));
    }

    /**
     * Test of setLowerLeftX method, of class PDImmutableRectangle.
     */
    @Test
    void testSetLowerLeftX()
    {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> rect.setLowerLeftX(0));
    }
    
}
