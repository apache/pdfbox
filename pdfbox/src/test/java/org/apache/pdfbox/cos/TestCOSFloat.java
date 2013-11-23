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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdfwriter.COSWriter;

/**
 * Tests {@link COSFloat}.
 */
public class TestCOSFloat extends TestCOSNumber
{
    // Use random number to ensure various float values are expressed in the test
    private Random rnd;

    public void setUp()
    {
        rnd = new Random();
        try
        {
            testCOSBase = COSNumber.get("1.1");
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
    public void testEquals()
    {
        // Consistency
        for (int i = -100000; i < 300000; i += 20000)
        {
            float num = i * rnd.nextFloat();
            COSFloat test1 = new COSFloat(num);
            COSFloat test2 = new COSFloat(num);
            COSFloat test3 = new COSFloat(num);
            // Reflexive (x == x)
            assertTrue(test1.equals(test1));
            // Symmetric is preserved ( x==y then y==x)
            assertTrue(test2.equals(test1));
            assertTrue(test1.equals(test2));
            // Transitive (if x==y && y==z then x==z)
            assertTrue(test1.equals(test2));
            assertTrue(test2.equals(test3));
            assertTrue(test1.equals(test3));
            // Non-nullity
            assertFalse(test1.equals(null));
            assertFalse(test2.equals(null));
            assertFalse(test3.equals(null));
            
            float nf = Float.intBitsToFloat(Float.floatToIntBits(num)+1);
            COSFloat test4 = new COSFloat(nf);
            assertFalse(test4.equals(test1));
        }
    }

    /**
     * Tests hashCode() - ensures that the Object.hashCode() contract is obeyed over a range of
     * arbitrary values.
     */
    public void testHashCode()
    {
        for (int i = -100000; i < 300000; i += 20000)
        {
            float num = i * rnd.nextFloat();
            COSFloat test1 = new COSFloat(num);
            COSFloat test2 = new COSFloat(num);
            assertEquals(test1.hashCode(), test2.hashCode());
            
            float nf = Float.intBitsToFloat(Float.floatToIntBits(num)+1);
            COSFloat test3 = new COSFloat(nf);
            assertFalse(test3.hashCode()==test1.hashCode());
        }
    }

    @Override
    public void testFloatValue()
    {
        for (int i = -100000; i < 300000; i += 20000)
        {
            float num = i * rnd.nextFloat();
            COSFloat testFloat = new COSFloat(num);
            assertEquals(num, testFloat.floatValue());
        }
    }

    @Override
    public void testDoubleValue()
    {
        for (int i = -100000; i < 300000; i += 20000)
        {
            float num = i * rnd.nextFloat();
            COSFloat testFloat = new COSFloat(num);
            // compare the string representation instead of the numeric values 
            // as the cast from float to double adds some more fraction digits
            assertEquals(Float.toString(num), Double.toString(testFloat.doubleValue()));
        }
    }

    @Override
    public void testIntValue()
    {
        for (int i = -100000; i < 300000; i += 20000)
        {
            float num = i * rnd.nextFloat();
            COSFloat testFloat = new COSFloat(num);
            assertEquals((int) num, testFloat.intValue());
        }
    }

    @Override
    public void testLongValue()
    {
        for (int i = -100000; i < 300000; i += 20000)
        {
            float num = i * rnd.nextFloat();
            COSFloat testFloat = new COSFloat(num);
            assertEquals((long) num, testFloat.longValue());
        }
    }

    @Override
    public void testAccept()
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        COSWriter visitor = new COSWriter(outStream);
        float num = 0;
        try
        {
            for (int i = -100000; i < 300000; i += 20000)
            {
                num = i * rnd.nextFloat();
                COSFloat cosFloat = new COSFloat(num);
                cosFloat.accept(visitor);
                assertEquals(floatToString(cosFloat.floatValue()), outStream.toString("ISO-8859-1"));
                testByteArrays(floatToString(num).getBytes("ISO-8859-1"),
                        outStream.toByteArray());
                outStream.reset();
            }
        }
        catch (Exception e)
        {
            fail("Failed to write " + num + " exception: " + e.getMessage());
        }
    }

    /**
     * Tests writePDF() - this method takes an {@link OutputStream} and writes this object to it.
     */
    public void testWritePDF()
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        float num = 0;
        try
        {
            for (int i = -1000; i < 3000; i += 200)
            {
                num = i * rnd.nextFloat();
                COSFloat cosFloat = new COSFloat(num);
                cosFloat.writePDF(outStream);
                assertEquals(floatToString(cosFloat.floatValue()), outStream.toString("ISO-8859-1"));
                testByteArrays(floatToString(num).getBytes("ISO-8859-1"),
                        outStream.toByteArray());
                outStream.reset();
            }
            // test a corner case as described in PDFBOX-1778
            num = 0.000000000000000000000000000000001f;
            COSFloat test = new COSFloat(num);
            test.writePDF(outStream);
            assertEquals(floatToString(num), outStream.toString("ISO-8859-1"));
            outStream.reset();
        }
        catch (IOException e)
        {
            fail("Failed to write " + num + " exception: " + e.getMessage());
        }
    }

    private String floatToString(float value)
    {
        // use a BigDecimal as intermediate state to avoid 
        // a floating point string representation of the float value
        return removeTrailingNull(new BigDecimal(String.valueOf(value)).toPlainString()); 
    }
    
    private String removeTrailingNull(String value)
    {
        // remove fraction digit "0" only
        if (value.indexOf(".") > -1 && !value.endsWith(".0"))
        {
            while (value.endsWith("0") && !value.endsWith(".0"))
            {
                value = value.substring(0,value.length()-1);
            }
        }
        return value;
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite(TestCOSFloat.class);
    }
}
