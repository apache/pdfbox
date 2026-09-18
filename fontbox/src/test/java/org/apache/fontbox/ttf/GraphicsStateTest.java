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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GraphicsState} defaults, deep copy and per-glyph reset: a shallow copy would let a
 * glyph write through to the saved post-{@code prep} template, and resetting too much or too little
 * would discard or retain state the TrueType spec is specific about.
 */
class GraphicsStateTest
{
    @Test
    void testDefaults()
    {
        GraphicsState gs = new GraphicsState();
        assertEquals(UnitVector.xAxis(), gs.getProjectionVector());
        assertEquals(UnitVector.xAxis(), gs.getFreedomVector());
        assertEquals(GraphicsState.ROUND_TO_GRID, gs.getRoundState());
        assertEquals(1, gs.getLoop());
        assertEquals(Fixed.ONE, gs.getMinimumDistance());
        assertEquals(17 * Fixed.ONE / 16, gs.getControlValueCutIn());
        assertEquals(9, gs.getDeltaBase());
        assertEquals(3, gs.getDeltaShift());
        assertEquals(1, gs.getZp0());
        assertEquals(0, gs.getRp0());
        assertTrue(gs.isAutoFlip());
    }

    @Test
    void testDeepCopyIsIndependent()
    {
        GraphicsState original = new GraphicsState();
        GraphicsState clone = original.copy();

        // the vector objects must not be shared (bug #2 guard)
        assertNotSame(original.getFreedomVector(), clone.getFreedomVector());

        // mutating the clone's vector in place must not touch the original
        clone.getFreedomVector().set(0, Fixed.ONE_F2DOT14);
        clone.setRp0(5);
        clone.setRoundState(GraphicsState.ROUND_OFF);

        assertEquals(UnitVector.xAxis(), original.getFreedomVector());
        assertEquals(0, original.getRp0());
        assertEquals(GraphicsState.ROUND_TO_GRID, original.getRoundState());
    }

    @Test
    void testResetForGlyphResetsOnlySpecMandatedFields()
    {
        GraphicsState gs = new GraphicsState();

        // simulate state left behind by prep / a previous glyph
        gs.getFreedomVector().set(0, Fixed.ONE_F2DOT14);
        gs.getProjectionVector().set(0, Fixed.ONE_F2DOT14);
        gs.setRp0(3);
        gs.setRp1(4);
        gs.setRp2(5);
        gs.setZp0(0);
        gs.setLoop(7);
        // prep-configured fields that must survive a per-glyph reset
        gs.setRoundState(GraphicsState.ROUND_OFF);
        gs.setControlValueCutIn(999);
        gs.setMinimumDistance(123);
        gs.setDeltaBase(42);

        gs.resetForGlyph();

        // reset to defaults
        assertEquals(UnitVector.xAxis(), gs.getFreedomVector());
        assertEquals(UnitVector.xAxis(), gs.getProjectionVector());
        assertEquals(0, gs.getRp0());
        assertEquals(0, gs.getRp1());
        assertEquals(0, gs.getRp2());
        assertEquals(1, gs.getZp0());
        assertEquals(1, gs.getLoop());

        // preserved from prep
        assertEquals(GraphicsState.ROUND_OFF, gs.getRoundState());
        assertEquals(999, gs.getControlValueCutIn());
        assertEquals(123, gs.getMinimumDistance());
        assertEquals(42, gs.getDeltaBase());
    }
}
