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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link COSBase}.
 */
abstract class TestCOSBase
{
    /** The COSBase abstraction of the object being tested. */
    protected static COSBase testCOSBase;

    /**
     * Tests getCOSObject() - tests that the underlying object is returned.
     */
    @Test
    void testGetCOSObject()
    {
        assertEquals(testCOSBase, testCOSBase.getCOSObject());
    }

    /**
     * Test accept() - tests the interface for visiting a document at the COS level.
     */
    abstract void testAccept() throws IOException;

    /**
     * Tests isDirect() and setDirect() - tests the getter/setter methods.
     */
    @Test
    void testIsSetDirect()
    {
        testCOSBase.setDirect(true);
        assertTrue(testCOSBase.isDirect());
        testCOSBase.setDirect(false);
        assertFalse(testCOSBase.isDirect());
    }

    /**
     * A simple utility function to compare two byte arrays.
     * @param byteArr1 the expected byte array
     * @param byteArr2 the byte array being compared
     */
    @SuppressWarnings({"java:S5863"}) // don't flag tests for reflexivity
    protected void testByteArrays(final byte[] byteArr1, final byte[] byteArr2)
    {
        assertEquals(byteArr1.length, byteArr1.length);
        for (int i = 0; i < byteArr1.length; i++)
        {
            assertEquals(byteArr1[i], byteArr2[i]);
        }
    }
}
