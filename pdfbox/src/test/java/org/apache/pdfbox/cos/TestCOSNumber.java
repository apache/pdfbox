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

import java.io.IOException;

/**
 * Test class for {@link COSNumber}
 */
public abstract class TestCOSNumber extends TestCOSBase
{
    /**
     * Test floatValue() - test that the correct float value is returned.
     */
    public abstract void testFloatValue();

    /**
     * Test doubleValue() - test that the correct double value is returned.
     */
    public abstract void testDoubleValue();

    /**
     * Test intValue() - test that the correct int value is returned.
     */
    public abstract void testIntValue();

    /**
     * Test longValue() - test that the correct long value is returned.
     */
    public abstract void testLongValue();

    /**
     * Tests get() - tests a static constructor for COSNumber classes.
     */
    public void testGet()
    {
        try
        {
            // Ensure the basic static numbers are recognized
            assertEquals(COSInteger.ZERO, COSNumber.get("0"));
            assertEquals(COSInteger.ONE, COSNumber.get("1"));
            assertEquals(COSInteger.TWO, COSNumber.get("2"));
            assertEquals(COSInteger.THREE, COSNumber.get("3"));
            // Test some arbitrary ints
            assertEquals(COSInteger.get(100), COSNumber.get("100"));
            assertEquals(COSInteger.get(256), COSNumber.get("256"));
            assertEquals(COSInteger.get(-1000), COSNumber.get("-1000"));
            // Some arbitrary floats
            assertEquals(new COSFloat(1.1f), COSNumber.get("1.1"));
            assertEquals(new COSFloat(100f), COSNumber.get("100.0"));
            assertEquals(new COSFloat(-100.001f), COSNumber.get("-100.001"));
            try
            {
                assertEquals("Null Value...", COSNumber.get(null));
                fail("Failed to throw a NullPointerException");
            }
            catch (NullPointerException e)
            {
                // PASS
            }

        }
        catch (IOException e)
        {
            fail("Failed to convert a number " + e.getMessage());
        }
    }
}
