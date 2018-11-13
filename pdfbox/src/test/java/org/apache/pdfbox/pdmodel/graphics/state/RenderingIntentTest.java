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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RenderingIntentTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fromStringInputNotNullOutputNotNull()
    {
        // Arrange
        final String value = "AbsoluteColorimetric";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        Assert.assertEquals(RenderingIntent.ABSOLUTE_COLORIMETRIC, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull2()
    {
        // Arrange
        final String value = "RelativeColorimetric";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        Assert.assertEquals(RenderingIntent.RELATIVE_COLORIMETRIC, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull3()
    {
        // Arrange
        final String value = "Perceptual";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        Assert.assertEquals(RenderingIntent.PERCEPTUAL, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull4()
    {
        // Arrange
        final String value = "Saturation";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        Assert.assertEquals(RenderingIntent.SATURATION, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull5()
    {
        // Arrange
        final String value = "";

        // Act
        final RenderingIntent retval = RenderingIntent.fromString(value);

        // Assert result
        Assert.assertEquals(RenderingIntent.RELATIVE_COLORIMETRIC, retval);
    }

    @Test
    public void stringValueOutputNotNull()
    {
        // Arrange
        final RenderingIntent objectUnderTest = RenderingIntent.ABSOLUTE_COLORIMETRIC;

        // Act
        final String retval = objectUnderTest.stringValue();

        // Assert result
        Assert.assertEquals("AbsoluteColorimetric", retval);
    }
}
