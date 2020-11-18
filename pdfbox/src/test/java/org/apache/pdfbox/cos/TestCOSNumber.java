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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link COSNumber}
 */
abstract class TestCOSNumber extends TestCOSBase
{
    /**
     * Test floatValue() - test that the correct float value is returned.
     */
    abstract void testFloatValue();

    /**
     * Test intValue() - test that the correct int value is returned.
     */
    abstract void testIntValue();

    /**
     * Test longValue() - test that the correct long value is returned.
     */
    abstract void testLongValue();

    /**
     * Tests get() - tests a static constructor for COSNumber classes.
     */
    @Test
    void testGet()
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
            assertEquals(COSInteger.get(2000), COSNumber.get("+2000"));
            // Some arbitrary floats
            assertEquals(new COSFloat(1.1f), COSNumber.get("1.1"));
            assertEquals(new COSFloat(100f), COSNumber.get("100.0"));
            assertEquals(new COSFloat(-100.001f), COSNumber.get("-100.001"));
            // according to the specs the exponential shall not be used
            // but obviously there some
            assertNotNull(COSNumber.get("-2e-006"));
            assertNotNull(COSNumber.get("-8e+05"));

            assertThrows(NullPointerException.class, () -> {
                    COSNumber.get(null);
            });
        }
        catch (IOException e)
        {
            fail("Failed to convert a number " + e.getMessage());
        }
    }

    /**
     * PDFBOX-4895: large number, too big for a long leads to a null value.
     * 
     * @throws IOException
     */
    @Test
    void testLargeNumber() throws IOException
    {
        assertNull(COSNumber.get("18446744073307448448"));
        assertNull(COSNumber.get("-18446744073307448448"));
    }

    @Test
    void testInvalidNumber()
    {
        try
        {
            COSNumber.get("18446744073307F448448");
            fail("Was expecting an IOException");
        }
        catch (IOException e)
        {
        }
    }

}
