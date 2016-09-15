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
    @Override
    public void setUp()
    {
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
     * Base class to run looped tests with float numbers.
     *
     * To use it, derive a class and just implement runTest(). Then either call
     * runTests for a series of random and pseudorandom tests, or runTest to
     * test with corner values.
     */
    abstract class BaseTester
    {
        private int low = -100000;
        private int high = 300000;
        private int step = 20000;

        public void setLoop(int low, int high, int step)
        {
            this.low = low;
            this.high = high;
            this.step = step;
        }

        // deterministic and non-deterministic test
        public void runTests()
        {
            // deterministic test
            loop(123456);

            // non-deterministic test
            loop(System.currentTimeMillis());
        }

        // look through a series of pseudorandom tests influenced by a seed
        private void loop(long seed)
        {
            Random rnd = new Random(seed);
            for (int i = low; i < high; i += step)
            {
                float num = i * rnd.nextFloat();
                try
                {
                    runTest(num);
                }
                catch (AssertionError a)
                {
                    fail("num = " + num + ", seed = " + seed);
                }
            }
        }

        abstract void runTest(float num);

    }

    /**
     * Tests equals() - ensures that the Object.equals() contract is obeyed.
     * These are tested over a range of arbitrary values to ensure Consistency,
     * Reflexivity, Symmetry, Transitivity and non-nullity.
     */
    public void testEquals()
    {
        new BaseTester()
        {
            @Override
            void runTest(float num)
            {
                COSFloat test1 = new COSFloat(num);
                COSFloat test2 = new COSFloat(num);
                COSFloat test3 = new COSFloat(num);
                // Reflexive (x == x)
                assertTrue(test1.equals(test1));
                // Symmetric is preserved ( x==y then y==x)
                assertTrue(test2.equals(test3));
                assertTrue(test1.equals(test2));
                // Transitive (if x==y && y==z then x==z)
                assertTrue(test1.equals(test2));
                assertTrue(test2.equals(test3));
                assertTrue(test1.equals(test3));

                float nf = Float.intBitsToFloat(Float.floatToIntBits(num) + 1);
                COSFloat test4 = new COSFloat(nf);
                assertFalse(test4.equals(test1));
            }
        }.runTests();
    }

    class HashCodeTester extends BaseTester
    {

        @Override
        void runTest(float num)
        {
            COSFloat test1 = new COSFloat(num);
            COSFloat test2 = new COSFloat(num);
            assertEquals(test1.hashCode(), test2.hashCode());

            float nf = Float.intBitsToFloat(Float.floatToIntBits(num) + 1);
            COSFloat test3 = new COSFloat(nf);
            assertFalse(test3.hashCode() == test1.hashCode());
        }
    }

    /**
     * Tests hashCode() - ensures that the Object.hashCode() contract is obeyed
     * over a range of arbitrary values.
     */
    public void testHashCode()
    {
        new HashCodeTester().runTests();
    }

    class FloatValueTester extends BaseTester
    {

        @Override
        void runTest(float num)
        {
            COSFloat testFloat = new COSFloat(num);
            assertEquals(num, testFloat.floatValue());
        }

    }

    @Override
    public void testFloatValue()
    {
        new FloatValueTester().runTests();
    }

    class DoubleValueTester extends BaseTester
    {

        @Override
        void runTest(float num)
        {
            COSFloat testFloat = new COSFloat(num);
            // compare the string representation instead of the numeric values 
            // as the cast from float to double adds some more fraction digits
            assertEquals(Float.toString(num), Double.toString(testFloat.doubleValue()));
        }

    }

    @Override
    public void testDoubleValue()
    {
        new DoubleValueTester().runTests();
    }

    class IntValueTester extends BaseTester
    {

        @Override
        void runTest(float num)
        {
            COSFloat testFloat = new COSFloat(num);
            assertEquals((int) num, testFloat.intValue());
        }

    }

    @Override
    public void testIntValue()
    {
        new IntValueTester().runTests();
    }

    class LongValueTester extends BaseTester
    {

        @Override
        void runTest(float num)
        {
            COSFloat testFloat = new COSFloat(num);
            assertEquals((long) num, testFloat.longValue());
        }
        
    }
    
    @Override
    public void testLongValue()
    {
        new LongValueTester().runTests();
    }

    class AcceptTester extends BaseTester
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        COSWriter visitor = new COSWriter(outStream);

        @Override
        void runTest(float num)
        {
            try
            {
                COSFloat cosFloat = new COSFloat(num);
                cosFloat.accept(visitor);
                assertEquals(floatToString(cosFloat.floatValue()), outStream.toString("ISO-8859-1"));
                testByteArrays(floatToString(num).getBytes("ISO-8859-1"), outStream.toByteArray());
                outStream.reset();
            }
            catch (IOException e)
            {
                fail("Failed to write " + num + " exception: " + e.getMessage());
            }
        }

    }

    @Override
    public void testAccept()
    {
        new AcceptTester().runTests();
    }

    class WritePDFTester extends BaseTester
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        public WritePDFTester()
        {
            setLoop(-1000, 3000, 200);
        }

        @Override
        void runTest(float num)
        {
            try
            {
                COSFloat cosFloat = new COSFloat(num);
                cosFloat.writePDF(outStream);
                assertEquals(floatToString(cosFloat.floatValue()), outStream.toString("ISO-8859-1"));
                assertEquals(floatToString(num), outStream.toString("ISO-8859-1"));
                testByteArrays(floatToString(num).getBytes("ISO-8859-1"), outStream.toByteArray());
                outStream.reset();
            }
            catch (IOException e)
            {
                fail("Failed to write " + num + " exception: " + e.getMessage());
            }
        }

    }

    /**
     * Tests writePDF() - this method takes an {@link java.io.OutputStream} and writes
     * this object to it.
     */
    public void testWritePDF()
    {
        WritePDFTester writePDFTester = new WritePDFTester();
        writePDFTester.runTests();

        // test a corner case as described in PDFBOX-1778
        writePDFTester.runTest(0.000000000000000000000000000000001f);
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
        if (value.indexOf('.') > -1 && !value.endsWith(".0"))
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
