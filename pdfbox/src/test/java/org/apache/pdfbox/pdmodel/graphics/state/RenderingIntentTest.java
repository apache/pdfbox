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
package org.apache.pdfbox.pdmodel.graphics.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RenderingIntentTest
{
    @Test
    void fromStringInputNotNullOutputNotNull()
    {
        // Arrange
        final String value = "AbsoluteColorimetric";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        assertEquals(RenderingIntent.ABSOLUTE_COLORIMETRIC, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull2()
    {
        // Arrange
        final String value = "RelativeColorimetric";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        assertEquals(RenderingIntent.RELATIVE_COLORIMETRIC, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull3()
    {
        // Arrange
        final String value = "Perceptual";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        assertEquals(RenderingIntent.PERCEPTUAL, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull4()
    {
        // Arrange
        final String value = "Saturation";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        assertEquals(RenderingIntent.SATURATION, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull5()
    {
        // Arrange
        final String value = "";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        assertEquals(RenderingIntent.RELATIVE_COLORIMETRIC, retval);
    }

    @Test
    void stringValueOutputNotNull()
    {
        // Arrange
        final RenderingIntent objectUnderTest = RenderingIntent.ABSOLUTE_COLORIMETRIC;

        // Act
        final String retval = objectUnderTest.stringValue();

        // Assert result
        assertEquals("AbsoluteColorimetric", retval);
    }

    @Test
    void testIsFill()
    {
        // Arrange
        final RenderingMode objectUnderTest = RenderingMode.FILL;

        // Act
        final boolean retval = objectUnderTest.isFill();

        // Assert result
        assertEquals(true, retval);
    }
}
