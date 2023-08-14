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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdfwriter.COSWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A test case for COSInteger
 *
 * @author Koch
 */
class TestCOSInteger extends TestCOSNumber
{
    @BeforeAll
    static void setUp()
    {
        try
        {
            testCOSBase = COSNumber.get("0");
        }
        catch (IOException e)
        {
            fail("Failed to create a COSNumber in setUp()");
        }
    }

    /**
     * Tests equals() - ensures that the Object.equals() contract is obeyed. These are tested over
     * a range of arbitrary values to ensure Consistency, Reflexivity, Symmetry, Transitivity and
     * non-nullity.
     */
    @Test
    void testEquals()
    {
        // Consistency
        for (int i = -1000; i < 3000; i += 200)
        {
            COSInteger test1 = COSInteger.get(i);
            COSInteger test2 = COSInteger.get(i);
            COSInteger test3 = COSInteger.get(i);
            // Reflexive (x == x)
            assertEquals(test1, test1);
            // Symmetric is preserved ( x==y then y===x)
            assertEquals(test2, test1);
            assertEquals(test1, test2);
            // Transitive (if x==y && y==z then x===z)
            assertEquals(test1, test2);
            assertEquals(test2, test3);
            assertEquals(test1, test3);

            COSInteger test4 = COSInteger.get(i + 1);
            assertNotEquals(test4, test1);
        }
    }

    /**
     * Tests hashCode() - ensures that the Object.hashCode() contract is obeyed over a range of
     * arbitrary values.
     */
    @Test
    void testHashCode()
    {
        for (int i = -1000; i < 3000; i += 200)
        {
            COSInteger test1 = COSInteger.get(i);
            COSInteger test2 = COSInteger.get(i);
            assertEquals(test1.hashCode(), test2.hashCode());
            
            COSInteger test3 = COSInteger.get(i + 1);
            assertNotSame(test3.hashCode(), test1.hashCode());
        }
    }

    @Override
    @Test
    void testFloatValue()
    {
        for (int i = -1000; i < 3000; i += 200)
        {
            assertEquals((float) i, COSInteger.get(i).floatValue());
        }
    }

    @Override
    @Test
    void testIntValue()
    {
        for (int i = -1000; i < 3000; i += 200)
        {
            assertEquals(i, COSInteger.get(i).intValue());
        }
    }

    @Override
    @Test
    void testLongValue()
    {
        for (int i = -1000; i < 3000; i += 200)
        {
            assertEquals((long) i, COSInteger.get(i).longValue());
        }
    }

    @Override
    @Test
    void testAccept()
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        COSWriter visitor = new COSWriter(outStream);
        int index = 0;
        try
        {
            for (int i = -1000; i < 3000; i += 200)
            {
                index = i;
                COSInteger cosInt = COSInteger.get(i);
                cosInt.accept(visitor);
                testByteArrays(String.valueOf(i).getBytes(StandardCharsets.ISO_8859_1), outStream.toByteArray());
                outStream.reset();
            }
        }
        catch (Exception e)
        {
            fail("Failed to write " + index + " exception: " + e.getMessage());
        }
    }

    /**
     * Tests writePDF() - this method takes an {@link java.io.OutputStream} and writes this object to it.
     */
    @Test
    void testWritePDF()
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int index = 0;
        try
        {
            for (int i = -1000; i < 3000; i += 200)
            {
                index = i;
                COSInteger cosInt = COSInteger.get(i);
                cosInt.writePDF(outStream);
                testByteArrays(String.valueOf(i).getBytes(StandardCharsets.ISO_8859_1), outStream.toByteArray());
                outStream.reset();
            }
        }
        catch (Exception e)
        {
            fail("Failed to write " + index + " exception: " + e.getMessage());
        }
    }
}
