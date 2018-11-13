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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class COSObjectKeyTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void compareToInputNotNullOutputZero()
    {
        // Arrange
        final COSObjectKey objectUnderTest = new COSObjectKey(0L, 0);
        final COSObjectKey other = new COSObjectKey(0L, 0);

        // Act
        final int retval = objectUnderTest.compareTo(other);

        // Assert result
        Assert.assertEquals(0, retval);
    }

    @Test
    public void compareToInputNotNullOutputPositive()
    {
        // Arrange
        final COSObjectKey objectUnderTest = new COSObjectKey(0L, 0);
        final COSObjectKey other = new COSObjectKey(-9223372036854775808L, 0);

        // Act
        final int retval = objectUnderTest.compareTo(other);

        // Assert result
        Assert.assertEquals(1, retval);
    }
}
