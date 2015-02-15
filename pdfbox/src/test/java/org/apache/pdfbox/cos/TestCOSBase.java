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

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Test class for {@link COSBase}.
 */
public abstract class TestCOSBase extends TestCase
{
    /** The COSBase abstraction of the object being tested. */
    protected COSBase testCOSBase;

    /**
     * Tests getCOSObject() - tests that the underlying object is returned.
     */
    public void testGetCOSObject()
    {
        assertEquals(testCOSBase, testCOSBase.getCOSObject());
    }

    /**
     * Test accept() - tests the interface for visiting a document at the COS level.
     */
    public abstract void testAccept() throws IOException;

    /**
     * Tests isDirect() and setDirect() - tests the getter/setter methods.
     */
    public void testIsSetDirect()
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
    protected void testByteArrays(byte[] byteArr1, byte[] byteArr2)
    {
        assertEquals(byteArr1.length, byteArr1.length);
        for (int i = 0; i < byteArr1.length; i++)
        {
            assertEquals(byteArr1[i], byteArr2[i]);
        }
    }
}
