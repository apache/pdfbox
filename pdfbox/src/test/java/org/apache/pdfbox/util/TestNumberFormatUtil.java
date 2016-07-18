/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Michael Doswald
 */
public class TestNumberFormatUtil extends TestCase
{

    private final byte[] buffer = new byte[64];

    public void testFormatOfIntegerValues()
    {
        assertEquals(2, NumberFormatUtil.formatFloatFast(51, 5, buffer));
        assertArrayEquals(new byte[]{'5', '1'}, Arrays.copyOfRange(buffer, 0, 2));

        assertEquals(3, NumberFormatUtil.formatFloatFast(-51, 5, buffer));
        assertArrayEquals(new byte[]{'-', '5', '1'}, Arrays.copyOfRange(buffer, 0, 3));

        assertEquals(1, NumberFormatUtil.formatFloatFast(0, 5, buffer));
        assertArrayEquals(new byte[]{'0'}, Arrays.copyOfRange(buffer, 0, 1));

        assertEquals(19, NumberFormatUtil.formatFloatFast(Long.MAX_VALUE, 5, buffer));
        assertArrayEquals(new byte[]{'9', '2', '2', '3', '3', '7', '2', '0', '3', '6', '8', '5', 
                                     '4', '7', '7', '5', '8', '0', '7'}, 
                          Arrays.copyOfRange(buffer, 0, 19));

        //Note: Integer.MAX_VALUE would be 2147483647, but when converting to float, we have 
        //      precision errors. NumberFormat.getIntegerInstance() does also print 2147483648 for 
        //      such a float
        assertEquals(10, NumberFormatUtil.formatFloatFast(Integer.MAX_VALUE, 5, buffer));
        assertArrayEquals(new byte[]{'2', '1', '4', '7', '4', '8', '3', '6', '4', '8'}, 
                          Arrays.copyOfRange(buffer, 0, 10));

        assertEquals(11, NumberFormatUtil.formatFloatFast(Integer.MIN_VALUE, 5, buffer));
        assertArrayEquals(new byte[]{'-', '2', '1', '4', '7', '4', '8', '3', '6', '4', '8'}, 
                          Arrays.copyOfRange(buffer, 0, 11));
    }

    public void testFormatOfRealValues()
    {
        assertEquals(3, NumberFormatUtil.formatFloatFast(0.7f, 5, buffer));
        assertArrayEquals(new byte[]{'0', '.', '7'}, Arrays.copyOfRange(buffer, 0, 3));

        assertEquals(4, NumberFormatUtil.formatFloatFast(-0.7f, 5, buffer));
        assertArrayEquals(new byte[]{'-', '0', '.', '7'}, Arrays.copyOfRange(buffer, 0, 4));

        assertEquals(5, NumberFormatUtil.formatFloatFast(0.003f, 5, buffer));
        assertArrayEquals(new byte[]{'0', '.', '0', '0', '3'}, Arrays.copyOfRange(buffer, 0, 5));

        assertEquals(6, NumberFormatUtil.formatFloatFast(-0.003f, 5, buffer));
        assertArrayEquals(new byte[]{'-', '0', '.', '0', '0', '3'}, 
                          Arrays.copyOfRange(buffer, 0, 6));
    }

    public void testFormatOfRealValuesReturnsMinusOneIfItCannotBeFormatted()
    {
        assertEquals("NaN should not be formattable", -1, 
                     NumberFormatUtil.formatFloatFast(Float.NaN, 5, buffer));
        assertEquals("+Infinity should not be formattable", -1, 
                     NumberFormatUtil.formatFloatFast(Float.POSITIVE_INFINITY, 5, buffer));
        assertEquals("-Infinity should not be formattable", -1, 
                     NumberFormatUtil.formatFloatFast(Float.NEGATIVE_INFINITY, 5, buffer));

        assertEquals("Too big number should not be formattable", -1, 
                     NumberFormatUtil.formatFloatFast(((float) Long.MAX_VALUE) + 1000000000000f, 5, buffer));
        assertEquals("Too big negative number should not be formattable", -1, 
                     NumberFormatUtil.formatFloatFast(Long.MIN_VALUE, 5, buffer));
    }

    public void testRoundingUp()
    {
        assertEquals(1, NumberFormatUtil.formatFloatFast(0.999999f, 5, buffer));
        assertArrayEquals(new byte[]{'1'}, Arrays.copyOfRange(buffer, 0, 1));
        
        assertEquals(4, NumberFormatUtil.formatFloatFast(0.125f, 2, buffer));
        assertArrayEquals(new byte[]{'0','.','1','3'}, Arrays.copyOfRange(buffer, 0, 4));
        
        assertEquals(2, NumberFormatUtil.formatFloatFast(-0.999999f, 5, buffer));
        assertArrayEquals(new byte[]{'-','1'}, Arrays.copyOfRange(buffer, 0, 2));
    }
    
    public void testRoundingDown()
    {
        assertEquals(4, NumberFormatUtil.formatFloatFast(0.994f, 2, buffer));
        assertArrayEquals(new byte[]{'0','.','9','9'}, Arrays.copyOfRange(buffer, 0, 4));
    }

    /**
     * Formats all floats in a defined range, parses them back with the BigDecimal constructor and
     * compares them to the expected result. The test only tests a small range for performance 
     * reasons. It works for ranges up to at least A0 size:
     * 
     * <ul>
     *   <li>PDF uses 72 dpi resolution</li>
     *   <li>A0 size is 841mm x 1189mm, this equals to about 2472 x 3495 in dot resolution</li>
     * </ul>
     */
    public void testFormattingInRange()
    {
        //Define a range to test
        BigDecimal minVal = new BigDecimal("-10");
        BigDecimal maxVal = new BigDecimal("10");
        BigDecimal maxDelta = BigDecimal.ZERO;
        
        Pattern pattern = Pattern.compile("^\\-?\\d+(\\.\\d+)?$");
        
        byte[] formatBuffer = new byte[32];
        
        for (int maxFractionDigits = 0; maxFractionDigits <= 5; maxFractionDigits++)
        {
            BigDecimal increment =  new BigDecimal(10).pow(-maxFractionDigits, MathContext.DECIMAL128);
            
            for (BigDecimal value = minVal; value.compareTo(maxVal) < 0; value = value.add(increment))
            {
                //format with the formatFloatFast method and parse back
                int byteCount = NumberFormatUtil.formatFloatFast(value.floatValue(), maxFractionDigits, formatBuffer);
                assertFalse(byteCount == -1);
                String newStringResult = new String(formatBuffer, 0, byteCount, Charsets.US_ASCII);
                BigDecimal formattedDecimal = new BigDecimal(newStringResult);
                
                //create new BigDecimal with float representation. This is needed because the float
                //may not represent the 'value' BigDecimal precisely, in which case the formatFloatFast
                //would get a different result.
                BigDecimal expectedDecimal = new BigDecimal(value.floatValue());
                expectedDecimal = expectedDecimal.setScale(maxFractionDigits, RoundingMode.HALF_UP);
                
                BigDecimal diff = formattedDecimal.subtract(expectedDecimal).abs();
                
                assertTrue(pattern.matcher(newStringResult).matches());
                
                //Fail if diff is greater than maxDelta.
                if (diff.compareTo(maxDelta) > 0)
                {
                    fail("Expected: " + expectedDecimal + ", actual: " + newStringResult + ", diff: " + diff);
                }
            }
        }
    }
    
    private void assertArrayEquals(byte[] expected, byte[] actual)
    {
        assertEquals("Length of byte array not equal", expected.length, actual.length);
        for (int idx = 0; idx < expected.length; idx++)
        {
            if (expected[idx] != actual[idx])
            {
                fail(String.format("Byte at index %d not equal. Expected '%02X' but got '%02X'",
                        idx, expected[idx], actual[idx]));
            }
        }
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite(TestNumberFormatUtil.class);
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        String[] arg =
        {
            TestNumberFormatUtil.class.getName()
        };
        junit.textui.TestRunner.main(arg);
    }
}
