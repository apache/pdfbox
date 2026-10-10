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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pins the v40 "backward compatibility" movement rules in {@link ExecutionContext#movePoint}, which
 * are a transcription of FreeType's {@code Direct_Move} under {@code TT_SUPPORT_SUBPIXEL_HINTING_MINIMAL}:
 * <ul>
 *   <li>with the flag off, {@code movePoint} is the plain spec move on both axes;</li>
 *   <li>with the flag on, x moves are suppressed (the point is still marked touched, so IUP treats it
 *       as a reference) and y moves are allowed until IUP has run on both axes, after which the glyph
 *       is frozen.</li>
 * </ul>
 * The flag is only ever set for the glyph program ({@code GlyphHinter.runProgram}); fpgm/prep run
 * with it off.
 */
class BackwardCompatibilityMoveTest
{
    private static final int MOVE = 100; // 1.56 px

    private static ExecutionContext context(boolean backwardCompatibility)
    {
        TrueTypeInterpreter interp = new TrueTypeInterpreter(32, 8, 0, 1000);
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        Zone zone = new Zone(2, 1);
        zone.getContourEnds()[0] = 1;
        ctx.setGlyphZone(zone);
        ctx.setBackwardCompatibility(backwardCompatibility);
        return ctx;
    }

    private static void freedomY(ExecutionContext ctx)
    {
        ctx.getGraphicsState().getFreedomVector().set(0, Fixed.ONE_F2DOT14);
        ctx.getGraphicsState().getProjectionVector().set(0, Fixed.ONE_F2DOT14);
    }

    @Test
    void testFlagOffIsThePlainMove()
    {
        ExecutionContext ctx = context(false);
        Zone zone = ctx.getGlyphZone();
        ctx.movePoint(zone, 0, MOVE);
        assertEquals(MOVE, zone.getCurrentX()[0]);
        assertTrue(zone.getTouchedX()[0]);
        assertFalse(zone.getTouchedY()[0]);

        freedomY(ctx);
        ctx.movePoint(zone, 1, MOVE);
        assertEquals(MOVE, zone.getCurrentY()[1]);
        assertTrue(zone.getTouchedY()[1]);

        // IUP on both axes does not freeze anything when the flag is off
        ctx.setIupxCalled();
        ctx.setIupyCalled();
        ctx.movePoint(zone, 1, MOVE);
        assertEquals(2 * MOVE, zone.getCurrentY()[1]);
    }

    @Test
    void testFlagOnSuppressesXButStillTouches()
    {
        ExecutionContext ctx = context(true);
        Zone zone = ctx.getGlyphZone();
        ctx.movePoint(zone, 0, MOVE);
        assertEquals(0, zone.getCurrentX()[0], "x must not be grid-fit under backward compatibility");
        assertTrue(zone.getTouchedX()[0], "the point still counts as touched for IUP");
    }

    @Test
    void testFlagOnAllowsYUntilIupHasRunOnBothAxes()
    {
        ExecutionContext ctx = context(true);
        Zone zone = ctx.getGlyphZone();
        freedomY(ctx);

        ctx.movePoint(zone, 0, MOVE);
        assertEquals(MOVE, zone.getCurrentY()[0], "y moves are allowed before IUP");

        ctx.setIupxCalled(); // only one axis so far: still allowed
        ctx.movePoint(zone, 0, MOVE);
        assertEquals(2 * MOVE, zone.getCurrentY()[0]);

        ctx.setIupyCalled(); // both axes: frozen
        ctx.movePoint(zone, 0, MOVE);
        assertEquals(2 * MOVE, zone.getCurrentY()[0], "y is frozen once IUP ran on both axes");
        assertTrue(zone.getTouchedY()[0]);
    }

    /** A diagonal freedom vector is gated per axis: its x part is dropped, its y part applied. */
    @Test
    void testDiagonalFreedomVectorIsGatedPerAxis()
    {
        ExecutionContext ctx = context(true);
        Zone zone = ctx.getGlyphZone();
        int d = (int) Math.round(Fixed.ONE_F2DOT14 / Math.sqrt(2));
        ctx.getGraphicsState().getFreedomVector().set(d, d);
        ctx.getGraphicsState().getProjectionVector().set(d, d);
        ctx.movePoint(zone, 0, MOVE);
        assertEquals(0, zone.getCurrentX()[0]);
        assertTrue(zone.getCurrentY()[0] > 0);
        assertTrue(zone.getTouchedX()[0]);
        assertTrue(zone.getTouchedY()[0]);
    }
    /**
     * SHPIX under backward compatibility is gated like DELTAP (FreeType Ins_SHPIX): a glyph-zone
     * point moves in y only if it was already touched in y and IUP has not run; twilight points
     * always move. With the flag off SHPIX is unconditional.
     */
    @Test
    void testShpixIsGatedLikeDeltap()
    {
        TrueTypeInterpreter interp = new TrueTypeInterpreter(32, 8, 4, 1000);
        byte[] shpixPoint0 = { 0x00 /* SVTCA[0] y */, (byte) 0xB1, 0, 100, 0x38 /* SHPIX */ };

        // flag off: moves
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        Zone zone = new Zone(2, 1);
        zone.getContourEnds()[0] = 1;
        ctx.setGlyphZone(zone);
        interp.run(ctx, new BytecodeStream(shpixPoint0));
        assertEquals(100, zone.getCurrentY()[0]);

        // flag on, point untouched: blocked
        ctx = interp.newContext(new GraphicsState());
        zone = new Zone(2, 1);
        zone.getContourEnds()[0] = 1;
        ctx.setGlyphZone(zone);
        ctx.setBackwardCompatibility(true);
        interp.run(ctx, new BytecodeStream(shpixPoint0));
        assertEquals(0, zone.getCurrentY()[0], "untouched point must not be moved by SHPIX");

        // flag on, point touched in y: moves
        zone.getTouchedY()[0] = true;
        interp.run(ctx, new BytecodeStream(shpixPoint0));
        assertEquals(100, zone.getCurrentY()[0]);

        // flag on, after IUP on both axes: blocked even though touched
        ctx.setIupxCalled();
        ctx.setIupyCalled();
        interp.run(ctx, new BytecodeStream(shpixPoint0));
        assertEquals(100, zone.getCurrentY()[0]);

        // flag on, twilight zone: always moves (SZP2 0 first)
        ctx = interp.newContext(new GraphicsState());
        ctx.setGlyphZone(zone);
        ctx.setBackwardCompatibility(true);
        byte[] twilight = { 0x00, (byte) 0xB0, 0, 0x15 /* SZP2 */, (byte) 0xB1, 1, 100, 0x38 };
        interp.run(ctx, new BytecodeStream(twilight));
        assertEquals(100, ctx.getTwilightZone().getCurrentY()[1]);
    }
}
