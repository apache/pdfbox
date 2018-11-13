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
package org.apache.pdfbox.pdmodel;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PageModeTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fromStringInputNotNullOutputNotNull()
    {
        // Arrange
        final String value = "FullScreen";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        Assert.assertEquals(PageMode.FULL_SCREEN, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull2()
    {
        // Arrange
        final String value = "UseThumbs";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        Assert.assertEquals(PageMode.USE_THUMBS, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull3()
    {
        // Arrange
        final String value = "UseOC";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        Assert.assertEquals(PageMode.USE_OPTIONAL_CONTENT, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull4()
    {
        // Arrange
        final String value = "UseNone";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        Assert.assertEquals(PageMode.USE_NONE, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull5()
    {
        // Arrange
        final String value = "UseAttachments";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        Assert.assertEquals(PageMode.USE_ATTACHMENTS, retval);
    }

    @Test
    public void fromStringInputNotNullOutputNotNull6()
    {
        // Arrange
        final String value = "UseOutlines";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        Assert.assertEquals(PageMode.USE_OUTLINES, retval);
    }

    @Test
    public void fromStringInputNotNullOutputIllegalArgumentException()
    {
        // Arrange
        final String value = "";

        // Act
        thrown.expect(IllegalArgumentException.class);
        PageMode.fromString(value);

        // Method is not expected to return due to exception thrown
    }

    @Test
    public void fromStringInputNotNullOutputIllegalArgumentException2()
    {
        // Arrange
        final String value = "Dulacb`ecj";

        // Act
        thrown.expect(IllegalArgumentException.class);
        PageMode.fromString(value);

        // Method is not expected to return due to exception thrown
    }

    @Test
    public void stringValueOutputNotNull()
    {
        // Arrange
        final PageMode objectUnderTest = PageMode.USE_OPTIONAL_CONTENT;

        // Act
        final String retval = objectUnderTest.stringValue();

        // Assert result
        Assert.assertEquals("UseOC", retval);
    }
}
