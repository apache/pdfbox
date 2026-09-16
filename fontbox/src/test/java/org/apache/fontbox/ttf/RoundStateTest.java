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
        gs.setSuperRound(Fixed.ONE, 0x48); // 01 period=1.0, 00 phase=0, 1000 threshold=4*p/8=half
        assertEquals(128, gs.round(100));
        assertEquals(64, gs.round(95));
    }
}
