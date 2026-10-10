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
 * {@link UnitVector#normalize}: the vectors steer every diagonal move, and a one-unit difference in
 * F2Dot14 shifts points by 1/64 px, so the results are checked against the unit vectors FreeType
 * produces for the same deltas.
 */
class UnitVectorTest
{
    /**
     * {dx, dy, x, y}: the expected values are the unit vectors FreeType 2.13.2 computes for these
     * deltas (its output, measured offline). Nearly axis-aligned vectors show the truncation to
     * F2Dot14: (1000, -7) gives (16383, -114), not the rounded (16384, -115).
     */
    private static final int[][] FREETYPE = {
        { 3, 4, 9830, 13107 },
        { 1, 1, 11585, 11585 },
        { -64, 128, -7327, 14654 },
        { 1000, -7, 16383, -114 },
        { 7, -1000, 114, -16383 },
        { -5, -5, -11585, -11585 },
        { 30000, 1, 16384, 0 },
        { 1, 2, 7327, 14654 },
    };

    @Test
    void testNormalizeMatchesFreeType()
    {
        for (int[] c : FREETYPE)
        {
            UnitVector v = UnitVector.normalize(c[0], c[1]);
            assertEquals(c[2], v.getX(), "x of (" + c[0] + "," + c[1] + ")");
            assertEquals(c[3], v.getY(), "y of (" + c[0] + "," + c[1] + ")");
        }
    }

    @Test
    void testNormalizeAxisAndZero()
    {
        assertEquals(Fixed.ONE_F2DOT14, UnitVector.normalize(5, 0).getX());
        assertEquals(-Fixed.ONE_F2DOT14, UnitVector.normalize(0, -5).getY());
        assertEquals(Fixed.ONE_F2DOT14, UnitVector.normalize(0, 0).getX());
    }
}
