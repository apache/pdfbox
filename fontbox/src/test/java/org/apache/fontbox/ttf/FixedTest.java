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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Fixed} fixed-point math used by the interpreter.
 */
class FixedTest
{
    @Test
    void testIntRoundTrip()
    {
        for (int n = -1000; n <= 1000; n++)
        {
            assertEquals(n, Fixed.toInt(Fixed.fromInt(n)), "round-trip " + n);
        }
        assertEquals(64, Fixed.fromInt(1));
        assertEquals(-128, Fixed.fromInt(-2));
    }

    @Test
    void testFloorCeilRound()
    {
        assertEquals(64, Fixed.floor(100));   // 1.5625px -> 1px
        assertEquals(128, Fixed.ceil(100));   // -> 2px
        assertEquals(64, Fixed.round(70));    // just above 1px rounds to 1px
        assertEquals(128, Fixed.round(96));   // 1.5px rounds up to 2px
        assertEquals(0, Fixed.round(31));     // just below half a pixel rounds to 0
        assertEquals(64, Fixed.round(32));    // exactly half rounds up
    }

    @Test
    void testMulDiv()
    {
        // 2.0 * 3.0 == 6.0
        assertEquals(Fixed.fromInt(6), Fixed.mul(Fixed.fromInt(2), Fixed.fromInt(3)));
        // 6.0 / 2.0 == 3.0
        assertEquals(Fixed.fromInt(3), Fixed.div(Fixed.fromInt(6), Fixed.fromInt(2)));
        // division by zero is defined as zero
        assertEquals(0, Fixed.div(Fixed.fromInt(5), 0));
        // signed rounding
        assertEquals(-Fixed.fromInt(6), Fixed.mul(Fixed.fromInt(-2), Fixed.fromInt(3)));
    }

    @Test
    void testMul14()
    {
        // multiplying by the F2Dot14 unit (1.0) is the identity
        assertEquals(Fixed.fromInt(5), Fixed.mul14(Fixed.fromInt(5), Fixed.ONE_F2DOT14));
        // multiplying by 0.5 in F2Dot14 halves the value
        assertEquals(Fixed.fromInt(5) / 2, Fixed.mul14(Fixed.fromInt(5), Fixed.ONE_F2DOT14 / 2));
    }

    @Test
    void testScale()
    {
        // 1000 font units at 16 ppem with unitsPerEm 2048 == 500 subpixel units == 7.8125px
        assertEquals(500, Fixed.scale(1000, 16, 2048));
        // a flat *64 would be wrong: it must depend on unitsPerEm
        assertEquals(Fixed.fromInt(16), Fixed.scale(2048, 16, 2048)); // one em == ppem pixels
        assertEquals(0, Fixed.scale(1234, 16, 0)); // guard against zero unitsPerEm
    }
}
