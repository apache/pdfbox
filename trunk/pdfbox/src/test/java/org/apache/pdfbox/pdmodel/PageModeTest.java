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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PageModeTest
{
    @Test
    void fromStringInputNotNullOutputNotNull()
    {
        // Arrange
        final String value = "FullScreen";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        assertEquals(PageMode.FULL_SCREEN, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull2()
    {
        // Arrange
        final String value = "UseThumbs";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        assertEquals(PageMode.USE_THUMBS, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull3()
    {
        // Arrange
        final String value = "UseOC";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        assertEquals(PageMode.USE_OPTIONAL_CONTENT, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull4()
    {
        // Arrange
        final String value = "UseNone";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        assertEquals(PageMode.USE_NONE, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull5()
    {
        // Arrange
        final String value = "UseAttachments";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        assertEquals(PageMode.USE_ATTACHMENTS, retval);
    }

    @Test
    void fromStringInputNotNullOutputNotNull6()
    {
        // Arrange
        final String value = "UseOutlines";

        // Act
        final PageMode retval = PageMode.fromString(value);

        // Assert result
        assertEquals(PageMode.USE_OUTLINES, retval);
    }

    @Test
    void fromStringInputNotNullOutputIllegalArgumentException()
    {
        assertThrows(IllegalArgumentException.class, () -> PageMode.fromString(""));
    }

    @Test
    void fromStringInputNotNullOutputIllegalArgumentException2()
    {
        assertThrows(IllegalArgumentException.class, () -> PageMode.fromString("Dulacb`ecj"));
    }

    @Test
    void stringValueOutputNotNull()
    {
        // Arrange
        final PageMode objectUnderTest = PageMode.USE_OPTIONAL_CONTENT;

        // Act
        final String retval = objectUnderTest.stringValue();

        // Assert result
        assertEquals("UseOC", retval);
    }
}
