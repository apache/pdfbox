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
 * Table-driven tests of the {@link GraphicsState#round(int)} state machine across all round states.
 */
class RoundStateTest
{
    private static int round(int state, int distance)
    {
        GraphicsState gs = new GraphicsState();
        gs.setRoundState(state);
        return gs.round(distance);
    }

    @Test
    void testRoundToGrid()
    {
        assertEquals(128, round(GraphicsState.ROUND_TO_GRID, 100)); // 1.56px -> 2px
        assertEquals(64, round(GraphicsState.ROUND_TO_GRID, 95));   // 1.48px -> 1px
        assertEquals(128, round(GraphicsState.ROUND_TO_GRID, 96));  // 1.5px rounds up
        assertEquals(-128, round(GraphicsState.ROUND_TO_GRID, -100));
    }

    @Test
    void testRoundDownAndUp()
    {
        assertEquals(64, round(GraphicsState.ROUND_DOWN_TO_GRID, 100));
        assertEquals(64, round(GraphicsState.ROUND_DOWN_TO_GRID, 127));
        assertEquals(128, round(GraphicsState.ROUND_UP_TO_GRID, 65));
        assertEquals(64, round(GraphicsState.ROUND_UP_TO_GRID, 64));
    }

    @Test
    void testRoundOff()
    {
        assertEquals(100, round(GraphicsState.ROUND_OFF, 100));
        assertEquals(-37, round(GraphicsState.ROUND_OFF, -37));
    }

    @Test
    void testRoundToDoubleGrid()
    {
        // double grid snaps to multiples of half a pixel (32)
        assertEquals(96, round(GraphicsState.ROUND_TO_DOUBLE_GRID, 80));  // 1.25px -> 1.5px
        assertEquals(64, round(GraphicsState.ROUND_TO_DOUBLE_GRID, 70));  // 1.09px -> 1.0px
    }

    @Test
    void testRoundToHalfGrid()
    {
        // half grid snaps to (n + 0.5) pixels, i.e. 32, 96, 160, ...
        assertEquals(96, round(GraphicsState.ROUND_TO_HALF_GRID, 100)); // -> 1.5px
        assertEquals(32, round(GraphicsState.ROUND_TO_HALF_GRID, 50));  // -> 0.5px
    }

    @Test
    void testSuperRoundReducesToGrid()
    {
        // SROUND with period=1px, phase=0, threshold=half is equivalent to round-to-grid
        GraphicsState gs = new GraphicsState();
        gs.setSuperRound(0x4000, 0x48); // 01 period=1.0, 00 phase=0, 1000 threshold=4*p/8=half
        assertEquals(128, gs.round(100));
        assertEquals(64, gs.round(95));
    }
    /**
     * Every mode mirrors about zero the way FreeType's Round_* functions do: a negative distance is
     * rounded as {@code -round(-d)}, and a result that would cross zero is clamped to zero (or to
     * the phase for half-grid). Stems measured right-to-left produce negative distances, so this is
     * not an edge case.
     */
    @Test
    void testNegativeDistancesMirrorFreeType()
    {
        assertEquals(-64, round(GraphicsState.ROUND_TO_GRID, -95));
        assertEquals(-128, round(GraphicsState.ROUND_TO_GRID, -96));
        assertEquals(0, round(GraphicsState.ROUND_TO_GRID, -31));
        assertEquals(-64, round(GraphicsState.ROUND_DOWN_TO_GRID, -127)); // toward zero
        assertEquals(0, round(GraphicsState.ROUND_DOWN_TO_GRID, -63));
        assertEquals(-128, round(GraphicsState.ROUND_UP_TO_GRID, -65));  // away from zero
        assertEquals(-64, round(GraphicsState.ROUND_UP_TO_GRID, -1));
        assertEquals(-96, round(GraphicsState.ROUND_TO_DOUBLE_GRID, -80));
        assertEquals(-64, round(GraphicsState.ROUND_TO_DOUBLE_GRID, -70));
        assertEquals(-32, round(GraphicsState.ROUND_TO_DOUBLE_GRID, -20));
        assertEquals(-96, round(GraphicsState.ROUND_TO_HALF_GRID, -100));
        assertEquals(-32, round(GraphicsState.ROUND_TO_HALF_GRID, -50));
        assertEquals(-32, round(GraphicsState.ROUND_TO_HALF_GRID, -1));  // floor(1)+32, negated
    }

    /** Zero and exact grid values are fixed points of every mode. */
    @Test
    void testGridValuesAreFixedPoints()
    {
        for (int state : new int[] { GraphicsState.ROUND_TO_GRID, GraphicsState.ROUND_DOWN_TO_GRID,
                GraphicsState.ROUND_UP_TO_GRID, GraphicsState.ROUND_TO_DOUBLE_GRID })
        {
            assertEquals(0, round(state, 0), "state " + state);
            assertEquals(128, round(state, 128), "state " + state);
            assertEquals(-128, round(state, -128), "state " + state);
        }
        assertEquals(32, round(GraphicsState.ROUND_TO_HALF_GRID, 0));
        assertEquals(96, round(GraphicsState.ROUND_TO_HALF_GRID, 64));
    }

    /**
     * S45ROUND parameters are derived in F2Dot14 and shifted, as FreeType does: with the diagonal
     * period 0x2D41 the "period - 1" threshold is (0x2D41 - 1) >> 8 = 45, not (45 - 1) = 44.
     */
    @Test
    void testSuperRound45Parameters()
    {
        GraphicsState gs = new GraphicsState();
        gs.setSuperRound(0x2D41, 0x40); // period = diagonal, phase 0, threshold = period - 1
        // period 45, threshold 45: any positive distance rounds up to the next multiple of 45
        assertEquals(45, gs.round(1));
        assertEquals(90, gs.round(46));
        assertEquals(-45, gs.round(-1));

        gs.setSuperRound(0x2D41, 0x58); // period = diagonal, phase = period/4 = 11, threshold = half
        assertEquals(11, gs.round(0));
        assertEquals(56, gs.round(40)); // 45 + 11
    }
}
