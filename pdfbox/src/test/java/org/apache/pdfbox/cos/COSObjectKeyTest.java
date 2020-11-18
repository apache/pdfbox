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
package org.apache.pdfbox.cos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class COSObjectKeyTest
{
    @Test
    void compareToInputNotNullOutputZero()
    {
        // Arrange
        final COSObjectKey objectUnderTest = new COSObjectKey(0L, 0);
        final COSObjectKey other = new COSObjectKey(0L, 0);

        // Act
        final int retval = objectUnderTest.compareTo(other);

        // Assert result
        assertEquals(0, retval);
    }

    @Test
    void compareToInputNotNullOutputPositive()
    {
        // Arrange
        final COSObjectKey objectUnderTest = new COSObjectKey(0L, 0);
        final COSObjectKey other = new COSObjectKey(-9_223_372_036_854_775_808L, 0);

        // Act
        final int retval = objectUnderTest.compareTo(other);

        // Assert result
        assertEquals(1, retval);
    }

    @Test
    void checkHashCode()
    {
        // same object number 100 0
        assertEquals(new COSObjectKey(100, 0).hashCode(),
                new COSObjectKey(100, 0).hashCode());

        // different object numbers/same generation numbers 100 0 vs. 200 0
        assertNotEquals(new COSObjectKey(100, 0).hashCode(),
                new COSObjectKey(200, 0).hashCode());

        // different object numbers/different generation numbers/ sum of both numbers are equal 100 0 vs. 99 1
        assertNotEquals(new COSObjectKey(100, 0).hashCode(),
                new COSObjectKey(99, 1).hashCode());
    }
}
