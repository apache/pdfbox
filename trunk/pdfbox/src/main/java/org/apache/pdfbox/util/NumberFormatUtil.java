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

/**
 * This class contains methods to format numbers.
 *
 * @author Michael Doswald
 */
public class NumberFormatUtil
{
    /**
     * Maximum number of fraction digits supported by the format methods
     */
    private static final int MAX_FRACTION_DIGITS = 5;

    /**
     * Contains the power of ten values for fast lookup in the format methods
     */
    private static final long[] POWER_OF_TENS;
    private static final int[] POWER_OF_TENS_INT;

    static
    {
        POWER_OF_TENS = new long[19];
        POWER_OF_TENS[0] = 1;

        for (int exp = 1; exp < POWER_OF_TENS.length; exp++)
        {
            POWER_OF_TENS[exp] = POWER_OF_TENS[exp - 1] * 10;
        }

        POWER_OF_TENS_INT = new int[10];
        POWER_OF_TENS_INT[0] = 1;

        for (int exp = 1; exp < POWER_OF_TENS_INT.length; exp++)
        {
            POWER_OF_TENS_INT[exp] = POWER_OF_TENS_INT[exp - 1] * 10;
        }
    }

    private NumberFormatUtil()
    {
    }

    /**
     * Fast variant to format a floating point value to a ASCII-string. The format will fail if the
     * value is greater than {@link Long#MAX_VALUE}, smaller or equal to {@link Long#MIN_VALUE}, is
     * {@link Float#NaN}, infinite or the number of requested fraction digits is greater than
     * {@link #MAX_FRACTION_DIGITS}.
     * 
     * When the number contains more fractional digits than {@code maxFractionDigits} the value will
     * be rounded. Rounding is done to the nearest possible value, with the tie breaking rule of 
     * rounding away from zero.
     * 
     * @param value The float value to format
     * @param maxFractionDigits The maximum number of fraction digits used
     * @param asciiBuffer The output buffer to write the formatted value to
     *
     * @return The number of bytes used in the buffer or {@code -1} if formatting failed
     */
    public static int formatFloatFast(float value, int maxFractionDigits, byte[] asciiBuffer)
    {
        if (Float.isNaN(value) ||
                Float.isInfinite(value) ||
                value > Long.MAX_VALUE ||
                value <= Long.MIN_VALUE ||
                maxFractionDigits > MAX_FRACTION_DIGITS)
        {
            return -1;
        }

        int offset = 0;
        long integerPart = (long) value;

        //handle sign
        if (value < 0)
        {
            asciiBuffer[offset++] = '-';
            integerPart = -integerPart;
        }
        
        //extract fraction part 
        long fractionPart = (long) ((Math.abs((double)value) - integerPart) * POWER_OF_TENS[maxFractionDigits] + 0.5d);
        
        //Check for rounding to next integer
        if (fractionPart >= POWER_OF_TENS[maxFractionDigits]) {
            integerPart++;
            fractionPart -= POWER_OF_TENS[maxFractionDigits];
        }

        //format integer part
        offset = formatPositiveNumber(integerPart, getExponent(integerPart), false, asciiBuffer, offset);
        
        if (fractionPart > 0 && maxFractionDigits > 0)
        {
            asciiBuffer[offset++] = '.';
            offset = formatPositiveNumber(fractionPart, maxFractionDigits - 1, true, asciiBuffer, offset);
        }

        return offset;
    }

    /**
     * Formats a positive integer number starting with the digit at {@code 10^exp}.
     *
     * @param number The number to format
     * @param exp The start digit
     * @param omitTrailingZeros Whether the formatting should stop if only trailing zeros are left.
     * This is needed e.g. when formatting fractions of a number.
     * @param asciiBuffer The buffer to write the ASCII digits to
     * @param startOffset The start offset into the buffer to start writing
     *
     * @return The offset into the buffer which contains the first byte that was not filled by the
     * method
     */
    private static int formatPositiveNumber(long number, int exp, boolean omitTrailingZeros, byte[] asciiBuffer, int startOffset)
    {
        int offset = startOffset;
        long remaining = number;

        while (remaining > Integer.MAX_VALUE && (!omitTrailingZeros || remaining > 0))
        {
            long digit = remaining / POWER_OF_TENS[exp];
            remaining -= (digit * POWER_OF_TENS[exp]);

            asciiBuffer[offset++] = (byte) ('0' + digit);
            exp--;
        }

        //If the remaining fits into an integer, use int arithmetic as it is faster
        int remainingInt = (int) remaining;
        while (exp >= 0 && (!omitTrailingZeros || remainingInt > 0))
        {
            int digit = remainingInt / POWER_OF_TENS_INT[exp];
            remainingInt -= (digit * POWER_OF_TENS_INT[exp]);

            asciiBuffer[offset++] = (byte) ('0' + digit);
            exp--;
        }

        return offset;
    }

    /**
     * Returns the highest exponent of 10 where {@code 10^exp < number} for numbers > 0
     */
    private static int getExponent(long number)
    {
        for (int exp = 0; exp < (POWER_OF_TENS.length - 1); exp++)
        {
            if (number < POWER_OF_TENS[exp + 1])
            {
                return exp;
            }
        }

        return POWER_OF_TENS.length - 1;
    }

}
