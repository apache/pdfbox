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
package org.apache.fontbox.ttf;

/**
 * Integer fixed-point math for the TrueType bytecode interpreter.
 * <p>
 * The interpreter keeps coordinates in 26.6 fixed point (F26Dot6: 26 integer bits, 6 fractional bits,
 * so 1 pixel == 64) and the projection/freedom vectors in 2.14 fixed point (F2Dot14). Mirroring
 * FreeType's all-integer arithmetic - rather than mixing {@code float}/{@code double} - is what makes
 * byte-exact comparison against a FreeType reference dump possible.
 *
 * @author Apache PDFBox
 */
final class Fixed
{
    /** One pixel in F26Dot6. */
    public static final int ONE = 64;

    /** Half a pixel in F26Dot6. */
    public static final int HALF = 32;

    /** 1.0 in F2Dot14. */
    public static final int ONE_F2DOT14 = 0x4000;

    private Fixed()
    {
    }

    /**
     * Converts an integer to F26Dot6.
     *
     * @param value an integer pixel value
     * @return the value in F26Dot6
     */
    public static int fromInt(int value)
    {
        return value << 6;
    }

    /**
     * Converts an F26Dot6 value back to an integer, rounding to nearest.
     *
     * @param value an F26Dot6 value
     * @return the nearest integer
     */
    public static int toInt(int value)
    {
        return (value + HALF) >> 6;
    }

    /**
     * Rounds an F26Dot6 value down to the pixel grid (towards negative infinity).
     *
     * @param value an F26Dot6 value
     * @return the floored value, still in F26Dot6
     */
    public static int floor(int value)
    {
        return value & ~63;
    }

    /**
     * Rounds an F26Dot6 value up to the pixel grid.
     *
     * @param value an F26Dot6 value
     * @return the ceiling value, still in F26Dot6
     */
    public static int ceil(int value)
    {
        return (value + 63) & ~63;
    }

    /**
     * Rounds an F26Dot6 value to the nearest pixel grid line (round-to-grid).
     *
     * @param value an F26Dot6 value
     * @return the rounded value, still in F26Dot6
     */
    public static int round(int value)
    {
        return floor(value + HALF);
    }

    /**
     * Computes {@code round(a * b / c)} in 64-bit with correct sign handling, matching FreeType's
     * {@code FT_MulDiv}. Used to build the F26Dot6 and F2Dot14 operators below.
     *
     * @param a first operand
     * @param b second operand
     * @param c divisor
     * @return the rounded result
     */
    public static int mulDiv(int a, int b, int c)
    {
        long la = a;
        long lb = b;
        long lc = c;
        int sign = 1;
        if (la < 0)
        {
            la = -la;
            sign = -sign;
        }
        if (lb < 0)
        {
            lb = -lb;
            sign = -sign;
        }
        if (lc < 0)
        {
            lc = -lc;
            sign = -sign;
        }
        long result = lc != 0 ? (la * lb + lc / 2) / lc : 0x7FFFFFFFL;
        return (int) (sign * result);
    }

    /**
     * Multiplies two F26Dot6 values, returning an F26Dot6 result (the TrueType {@code MUL} operator).
     *
     * @param a first F26Dot6 operand
     * @param b second F26Dot6 operand
     * @return {@code a * b} in F26Dot6
     */
    public static int mul(int a, int b)
    {
        return mulDiv(a, b, ONE);
    }

    /**
     * Divides two F26Dot6 values, returning an F26Dot6 result (the TrueType {@code DIV} operator).
     * Division by zero yields zero. Unlike {@link #mul(int, int)} this <em>truncates</em> toward zero
     * rather than rounding, matching FreeType's {@code DIV} opcode (which uses {@code FT_MulDiv_No_Round}).
     *
     * @param a F26Dot6 dividend
     * @param b F26Dot6 divisor
     * @return {@code a / b} in F26Dot6, or 0 if {@code b == 0}
     */
    public static int div(int a, int b)
    {
        if (b == 0)
        {
            return 0;
        }
        long la = a;
        long lb = b;
        int sign = 1;
        if (la < 0)
        {
            la = -la;
            sign = -sign;
        }
        if (lb < 0)
        {
            lb = -lb;
            sign = -sign;
        }
        return (int) (sign * (la * ONE / lb));
    }

    /**
     * Multiplies an F26Dot6 value by an F2Dot14 value, returning F26Dot6. This is the building block
     * for projecting a distance onto the projection/freedom vector.
     *
     * @param a an F26Dot6 value
     * @param b an F2Dot14 value
     * @return {@code a * b} in F26Dot6
     */
    public static int mul14(int a, int b)
    {
        return mulDiv(a, b, ONE_F2DOT14);
    }

    /**
     * Scales a coordinate from font units to F26Dot6 device pixels at the given ppem:
     * {@code round(funits * ppem * 64 / unitsPerEm)}. Note this is <em>not</em> a flat {@code * 64} -
     * that would only be correct when {@code unitsPerEm == ppem}. Both control values and glyph point
     * coordinates are scaled with this.
     *
     * @param funits a value in font units
     * @param ppem the active pixels-per-em
     * @param unitsPerEm the font's unitsPerEm (from the head table)
     * @return the value in F26Dot6 device pixels
     */
    public static int scale(int funits, int ppem, int unitsPerEm)
    {
        if (unitsPerEm == 0)
        {
            return 0;
        }
        long numerator = (long) funits * ppem * ONE;
        long rounded = numerator >= 0 ? numerator + unitsPerEm / 2 : numerator - unitsPerEm / 2;
        return (int) (rounded / unitsPerEm);
    }
}
