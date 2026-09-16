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
 * Unit tests for the point-moving opcodes: each builds a glyph zone, runs a short program
 * through the interpreter, and asserts the resulting coordinates. Coordinates are in F26Dot6 (64 per
 * pixel). Byte-exact agreement with FreeType is proven by {@link GoldenHintingTest}; these check the
 * per-opcode logic.
 */
class PointOpsTest
{
    private static TrueTypeInterpreter interpreter()
    {
        return new TrueTypeInterpreter(256, 16, 16, 2048);
    }

    /** Builds a single-contour glyph zone with the given x coordinates (original == current). */
    private static Zone lineZone(int... xs)
    {
        Zone zone = new Zone(xs.length, 1);
        for (int i = 0; i < xs.length; i++)
        {
            zone.getOriginalX()[i] = xs[i];
            zone.getCurrentX()[i] = xs[i];
            // these synthetic tests work directly in device units, so the unscaled originals (used by
            // IP for its interpolation ratio) mirror the scaled ones
            zone.getUnscaledX()[i] = xs[i];
        }
        zone.getContourEnds()[0] = xs.length - 1;
        return zone;
    }

    private static ExecutionContext context(TrueTypeInterpreter interp, Zone glyph)
    {
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        ctx.setPpem(16);
        ctx.setGlyphZone(glyph);
        return ctx;
    }

    @Test
    void testProjectionAndMove()
    {
        ExecutionContext ctx =
                new ExecutionContext(null, new GraphicsState(), 16, new int[0], null, new Zone(0, 0));
        Zone zone = lineZone(100, 0);
        // default projection/freedom is the x axis: project returns the x coordinate
        assertEquals(100, ctx.project(zone.getCurrentX()[0], zone.getCurrentY()[0]));
        ctx.movePoint(zone, 0, 28); // move +28 along x
        assertEquals(128, zone.getCurrentX()[0]);
        assertTrue(zone.getTouchedX()[0]);
        assertFalse(zone.getTouchedY()[0]);
    }

    @Test
    void testMdapRoundsToGrid()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(100); // 1.5625px
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[0] 0 ; MDAP[1] (round)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 0, 0x2F }));
        assertEquals(128, zone.getCurrentX()[0]); // rounded to 2px
        assertTrue(zone.getTouchedX()[0]);
    }

    @Test
    void testMdrpRelativeToRp0()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 100); // rp0 = point 0 at x=0, point 1 at 1.5625px
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[0] 1 ; MDRP[round] (0xC4, round bit = 0x04) - grid-rounded distance from rp0
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 1, (byte) 0xC4 }));
        assertEquals(128, zone.getCurrentX()[1]); // distance 100 rounds to 128
    }

    @Test
    void testMdrpRoundAndMinimumDistanceFlagsAreDistinct()
    {
        // guards the flag encoding: round is bit 0x04, minimum-distance is bit 0x08 (they were once
        // swapped). A small original distance (30) below the minimum (64):
        TrueTypeInterpreter interp = interpreter();
        // round only (0xC4): 30 -> round(30) = 0, no minimum clamp
        Zone roundZone = lineZone(0, 30);
        interp.run(context(interp, roundZone), new BytecodeStream(new byte[] { (byte) 0xB0, 1, (byte) 0xC4 }));
        assertEquals(0, roundZone.getCurrentX()[1]);

        // minimum-distance only (0xC8): no rounding, but clamp the distance up to the minimum (64)
        Zone minZone = lineZone(0, 30);
        interp.run(context(interp, minZone), new BytecodeStream(new byte[] { (byte) 0xB0, 1, (byte) 0xC8 }));
        assertEquals(64, minZone.getCurrentX()[1]);
    }

    @Test
    void testMirpUsesControlValue()
    {
        TrueTypeInterpreter interp = interpreter();
        // raw cvt 256 at 16ppem / 2048 upem scales to 128 (2px)
        interp.setControlValues(new int[] { 256 });
        interp.setPpem(16, 16);
        Zone zone = lineZone(0, 100);
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        ctx.setPpem(16);
        ctx.setGlyphZone(zone);
        // PUSHB[1] 1 0 (point=1 pushed first, cvtIndex=0 on top) ; MIRP[round] (0xE4, round = 0x04)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 1, 0, (byte) 0xE4 }));
        assertEquals(128, zone.getCurrentX()[1]);
    }

    @Test
    void testMsirpSetsExactDistance()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 100);
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[1] 1 64 (point=1, distance=1px) ; MSIRP[0] (0x3A)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 1, 64, 0x3A }));
        assertEquals(64, zone.getCurrentX()[1]); // exactly 1px from rp0 at x=0
    }

    @Test
    void testAlignRp()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 100); // rp0 at 0
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[0] 1 ; ALIGNRP (0x3C) - align point 1 onto rp0
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 1, 0x3C }));
        assertEquals(0, zone.getCurrentX()[1]);
    }

    @Test
    void testIupInterpolatesUntouched()
    {
        TrueTypeInterpreter interp = interpreter();
        // three points on one contour; the middle one is untouched
        Zone zone = lineZone(0, 50, 100);
        zone.getTouchedX()[0] = true;
        zone.getTouchedX()[2] = true;
        zone.getCurrentX()[2] = 120; // the right anchor was hinted +20
        ExecutionContext ctx = context(interp, zone);
        // IUP[1] (x axis)
        interp.run(ctx, new BytecodeStream(new byte[] { 0x31 }));
        // p1 interpolates proportionally: 0 + 50*(120-0)/100 = 60
        assertEquals(60, zone.getCurrentX()[1]);
        // touched anchors are never moved by IUP
        assertEquals(0, zone.getCurrentX()[0]);
        assertEquals(120, zone.getCurrentX()[2]);
    }

    @Test
    void testIupShiftsWhenSingleTouchedPoint()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 50, 100);
        zone.getTouchedX()[1] = true;
        zone.getCurrentX()[1] = 70; // the only touched point moved +20
        ExecutionContext ctx = context(interp, zone);
        interp.run(ctx, new BytecodeStream(new byte[] { 0x31 })); // IUP[1]
        // with one touched point, every other point shifts by the same delta (+20)
        assertEquals(20, zone.getCurrentX()[0]);
        assertEquals(120, zone.getCurrentX()[2]);
    }

    @Test
    void testIpInterpolatesBetweenReferencePoints()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 50, 100);
        // set rp1=0, rp2=2, move the anchors, then IP point 1
        zone.getCurrentX()[2] = 120;
        GraphicsState gs = new GraphicsState();
        gs.setRp1(0);
        gs.setRp2(2);
        ExecutionContext ctx = interp.newContext(gs);
        ctx.setPpem(16);
        ctx.setGlyphZone(zone);
        // PUSHB[0] 1 ; IP (0x39)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 1, 0x39 }));
        assertEquals(60, zone.getCurrentX()[1]);
    }

    @Test
    void testIpInterpolatesTwilightPointsUsingScaledOriginals()
    {
        // Regression (PDFBOX-3293): twilight-zone points have no font-unit source, so their unscaled
        // coordinates are (0,0). Measuring IP's interpolation ratio against those zeros collapses every
        // interpolated point onto the reference point. A prep program that builds the x-height control
        // value this way then yields 0, flattening whole glyphs onto the baseline at small ppem (e.g.
        // lowercase 'm' in a gasp-less Arial subset at 7ppem). For twilight points IP must measure the
        // scaled originals instead, exactly as FreeType's Ins_IP does.
        TrueTypeInterpreter interp = interpreter();
        GraphicsState gs = new GraphicsState();
        gs.setZp0(0); // all three zone pointers reference the twilight zone
        gs.setZp1(0);
        gs.setZp2(0);
        gs.setRp1(0);
        gs.setRp2(2);
        ExecutionContext ctx = interp.newContext(gs);
        ctx.setPpem(16);
        Zone twilight = ctx.getTwilightZone();
        // anchors at originals 0 and 100; p2's current is stretched to 120. The unscaled coordinates
        // stay (0,0) for every point, exactly as MIAP leaves freshly placed twilight points.
        int[] xs = { 0, 50, 100 };
        for (int p = 0; p < xs.length; p++)
        {
            twilight.getOriginalX()[p] = xs[p];
            twilight.getCurrentX()[p] = xs[p];
        }
        twilight.getCurrentX()[2] = 120;
        // PUSHB[0] 1 ; IP (0x39)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 1, 0x39 }));
        // interpolate by the scaled originals: 0 + 50*(120-0)/100 = 60 (the unscaled zeros would give 0)
        assertEquals(60, twilight.getCurrentX()[1]);
    }

    @Test
    void testSvtcaSetsProjectionVector()
    {
        TrueTypeInterpreter interp = interpreter();
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        // SVTCA[0] (y axis) ; GPV
        interp.run(ctx, new BytecodeStream(new byte[] { 0x00, 0x0C }));
        assertEquals(Fixed.ONE_F2DOT14, ctx.peek(0)); // pv.y
        assertEquals(0, ctx.peek(1));                 // pv.x
    }

    @Test
    void testRoundOpcode()
    {
        // PUSHB[0] 100 ; ROUND[0] (0x68) -> 128 under default round-to-grid
        ExecutionContext ctx = interpreter().executeProgram(new byte[] { (byte) 0xB0, 100, 0x68 }, 16);
        assertEquals(128, ctx.peek(0));
    }

    @Test
    void testMdMeasuresSignedDistance()
    {
        // MD measures project(zp0[p1] - zp1[p2]); p1 is the deeper operand, p2 the top (FreeType sign).
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 100); // point 0 at x=0, point 1 at x=100
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[1] 0 1 (p1=0 deeper, p2=1 top) ; MD[grid] (0x49)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 0, 1, 0x49 }));
        assertEquals(-100, ctx.peek(0)); // x0 - x1 = -100
    }

    @Test
    void testGcReadsProjectedCoordinate()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(192); // 3px
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[0] 0 ; GC[0] (0x46) current coordinate
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 0, 0x46 }));
        assertEquals(192, ctx.peek(0));
    }

    @Test
    void testIsectMovesPointToLineIntersection()
    {
        TrueTypeInterpreter interp = interpreter();
        // p0 = the point to move; line A = horizontal y=64 (p1,p2); line B = vertical x=64 (p3,p4)
        Zone zone = new Zone(5, 1);
        setPoint(zone, 0, 0, 0);
        setPoint(zone, 1, 0, 64);
        setPoint(zone, 2, 128, 64);
        setPoint(zone, 3, 64, 0);
        setPoint(zone, 4, 64, 128);
        ExecutionContext ctx = context(interp, zone);
        // push p0,a0,a1,b0,b1 = 0 1 2 3 4 ; ISECT (0x0F) pops b1,b0,a1,a0,point
        interp.run(ctx, new BytecodeStream(
                new byte[] { (byte) 0xB4, 0, 1, 2, 3, 4, 0x0F }));
        assertEquals(64, zone.getCurrentX()[0]);
        assertEquals(64, zone.getCurrentY()[0]);
        assertTrue(zone.getTouchedX()[0]);
        assertTrue(zone.getTouchedY()[0]);
    }

    // --- oracle-free invariant checks ------------------------------------

    @Test
    void testIupNeverMovesATouchedPoint()
    {
        // invariant: IUP leaves every already-touched point exactly where it is
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(0, 50, 100);
        zone.getTouchedX()[1] = true;
        zone.getCurrentX()[1] = 77; // a touched point at a deliberately off-grid position
        ExecutionContext ctx = context(interp, zone);
        interp.run(ctx, new BytecodeStream(new byte[] { 0x31 })); // IUP[x]
        assertEquals(77, zone.getCurrentX()[1]);
    }

    @Test
    void testTouchedPointsLandOnGridUnderRoundToGrid()
    {
        // invariant: under an integer round state, a rounded (touched) point lands on a grid line
        TrueTypeInterpreter interp = interpreter();
        Zone zone = lineZone(100, 150, 77); // off-grid positions
        ExecutionContext ctx = context(interp, zone); // default round state is round-to-grid
        // MDAP[1] each point (PUSHB[0] n ; MDAP[1])
        interp.run(ctx, new BytecodeStream(new byte[] {
                (byte) 0xB0, 0, 0x2F, (byte) 0xB0, 1, 0x2F, (byte) 0xB0, 2, 0x2F }));
        for (int i = 0; i < 3; i++)
        {
            assertTrue(zone.getTouchedX()[i], "point " + i + " should be touched");
            assertEquals(0, zone.getCurrentX()[i] % Fixed.ONE, "point " + i + " off the grid");
        }
    }

    private static void setPoint(Zone zone, int i, int x, int y)
    {
        zone.getCurrentX()[i] = x;
        zone.getCurrentY()[i] = y;
        zone.getOriginalX()[i] = x;
        zone.getOriginalY()[i] = y;
        zone.getUnscaledX()[i] = x;
        zone.getUnscaledY()[i] = y;
    }
}
