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
    /**
     * 16ppem at 1024 units per em is a scale of exactly 1 (16 * 64 / 1024), so the synthetic zones'
     * unscaled coordinates can equal their device coordinates; MDRP and IP measure the former.
     */
    private static TrueTypeInterpreter interpreter()
    {
        return new TrueTypeInterpreter(256, 16, 16, 1024);
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

    /**
     * MDRP measures the original distance in font units and scales it, rather than projecting the
     * individually rounded scaled originals (FreeType's Ins_MDRP, orus). From DejaVu Sans Bold '1' at
     * 14ppem: points 72 units apart at 2048 upem are 31.5/64 px apart. Scaled on their own they land
     * at 653 and 622, 31 apart, which rounds to 0; the scaled distance is 32, which rounds to a pixel.
     */
    @Test
    void testMdrpScalesTheUnscaledOriginalDistance()
    {
        TrueTypeInterpreter interp = new TrueTypeInterpreter(256, 16, 16, 2048);
        interp.setPpem(14, 14);
        Zone zone = new Zone(2, 1);
        int[] unscaled = { 1493, 1421 };
        for (int i = 0; i < 2; i++)
        {
            int scaled = Fixed.scale(unscaled[i], 14, 2048);
            setPoint(zone, i, scaled, 0);
            zone.getUnscaledX()[i] = unscaled[i];
        }
        assertEquals(653, zone.getCurrentX()[0]);
        assertEquals(622, zone.getCurrentX()[1]);
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        ctx.setGlyphZone(zone);
        // PUSHB[0] 1 ; MDRP[round] (0xC4) relative to rp0 = point 0
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 1, (byte) 0xC4 }));
        assertEquals(653 - 64, zone.getCurrentX()[1]);
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
        // raw cvt 128 at 16ppem / 1024 upem scales to 128 (2px)
        interp.setControlValues(new int[] { 128 });
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

    /**
     * SPVFS takes the low 16 bits of each operand, sign-extended, and normalizes them (FreeType's
     * Ins_SPVFS); (0,0) leaves the vector as it was.
     */
    @Test
    void testSpvfsNormalizesSignExtendedOperands()
    {
        TrueTypeInterpreter interp = interpreter();
        ExecutionContext ctx = interp.newContext(new GraphicsState());
        // PUSHW[1] 0xFFFF 0x0001 (x = -1 after sign extension, y = 1) ; SPVFS ; GPV
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB9, (byte) 0xFF, (byte) 0xFF, 0x00,
                0x01, 0x0A, 0x0C }));
        assertEquals(11585, ctx.peek(0));  // pv.y
        assertEquals(-11585, ctx.peek(1)); // pv.x
        // PUSHB[1] 0 0 ; SPVFS ; GPV - unchanged
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 0, 0, 0x0A, 0x0C }));
        assertEquals(11585, ctx.peek(0));
        assertEquals(-11585, ctx.peek(1));
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

    /**
     * SPVTL takes the top point from zp2 and the one below it from zp1, and the line runs from the
     * zp2 point to the zp1 point (FreeType's Ins_SxVTL). Here that is (64,0) to (0,0), so the
     * projection vector is -x and GC of the point at x=64 reads -64.
     */
    @Test
    void testSpvtlLineRunsFromTopPointToLowerPoint()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = new Zone(2, 1);
        setPoint(zone, 0, 64, 0);
        setPoint(zone, 1, 0, 0);
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[1] 1 0 (zp1 point 1 below, zp2 point 0 on top) ; SPVTL[0] ; PUSHB[0] 0 ; GC[0]
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 1, 0, 0x06, (byte) 0xB0, 0, 0x46 }));
        assertEquals(-Fixed.ONE_F2DOT14, ctx.getGraphicsState().getProjectionVector().getX());
        assertEquals(-64, ctx.peek(0));
    }

    /**
     * SPVTL sets the dual projection vector equal to the projection vector; only SDPVTL derives it
     * from the original outline (FreeType's Ins_SPVTL). The current line here is horizontal while the
     * original line is vertical, so a dual vector taken from the originals would be the y axis.
     */
    @Test
    void testSpvtlSetsDualVectorToProjectionVector()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = new Zone(2, 1);
        setPoint(zone, 0, 0, 0);
        setPoint(zone, 1, 0, 64);
        zone.getCurrentX()[1] = 64;
        zone.getCurrentY()[1] = 0;
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[1] 1 0 ; SPVTL[0] ; PUSHB[0] 1 ; GC[1] (original, along the dual vector)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 1, 0, 0x06, (byte) 0xB0, 1, 0x47 }));
        UnitVector dual = ctx.getGraphicsState().getDualProjectionVector();
        assertEquals(Fixed.ONE_F2DOT14, dual.getX());
        assertEquals(0, dual.getY());
        assertEquals(0, ctx.peek(0)); // original (0,64) projected on the x axis
    }

    /** A zero-length line gives the x axis even for the perpendicular form (FreeType's Ins_SxVTL). */
    @Test
    void testSpvtlZeroLengthLineIsXAxisEvenWhenPerpendicular()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = new Zone(1, 1);
        setPoint(zone, 0, 64, 64);
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[1] 0 0 ; SPVTL[1] (perpendicular)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 0, 0, 0x07 }));
        UnitVector pv = ctx.getGraphicsState().getProjectionVector();
        assertEquals(Fixed.ONE_F2DOT14, pv.getX());
        assertEquals(0, pv.getY());
    }

    /**
     * SDPVTL: when the original line has zero length FreeType drops the perpendicular flag, and the
     * drop carries over to the current line computed after it, so the projection vector runs along
     * the current line rather than across it.
     */
    @Test
    void testSdpvtlZeroLengthOriginalLineDropsPerpendicularForCurrentLine()
    {
        TrueTypeInterpreter interp = interpreter();
        Zone zone = new Zone(2, 1);
        setPoint(zone, 0, 0, 0);
        setPoint(zone, 1, 0, 0);
        zone.getCurrentX()[1] = 64;
        ExecutionContext ctx = context(interp, zone);
        // PUSHB[1] 1 0 ; SDPVTL[1] (perpendicular)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB1, 1, 0, (byte) 0x87 }));
        UnitVector pv = ctx.getGraphicsState().getProjectionVector();
        UnitVector dual = ctx.getGraphicsState().getDualProjectionVector();
        assertEquals(Fixed.ONE_F2DOT14, pv.getX());
        assertEquals(0, pv.getY());
        assertEquals(Fixed.ONE_F2DOT14, dual.getX());
        assertEquals(0, dual.getY());
    }

    /** An interpreter whose cvt[0] scales to 128 (2px) at 16ppem, and a context on the given zone. */
    private static ExecutionContext contextWithCvt128(TrueTypeInterpreter interp, Zone glyph)
    {
        interp.setControlValues(new int[] { 128 });
        interp.setPpem(16, 16);
        return context(interp, glyph);
    }

    /**
     * MIRP to a twilight point first places it the control value away from rp0's original position
     * along the freedom vector (undocumented MS rasterizer behaviour, FreeType's Ins_MIRP). Without
     * that the point's stale origin at 0 would flip the control value to -128.
     */
    @Test
    void testMirpPlacesTwilightPointFromRp0()
    {
        TrueTypeInterpreter interp = interpreter();
        ExecutionContext ctx = contextWithCvt128(interp, lineZone(64)); // rp0 = glyph point 0 at 1px
        // PUSHB[0] 0 ; SZP1 (twilight) ; PUSHB[1] 3 0 ; MIRP[0] (no round, no minimum)
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 0, 0x14,
                (byte) 0xB1, 3, 0, (byte) 0xE0 }));
        Zone twilight = ctx.getTwilightZone();
        assertEquals(192, twilight.getOriginalX()[3]);
        assertEquals(192, twilight.getCurrentX()[3]);
    }

    /**
     * MSIRP to a twilight point first places its original position the distance away from rp0's
     * original position (undocumented MS rasterizer behaviour, FreeType's Ins_MSIRP).
     */
    @Test
    void testMsirpPlacesTwilightPointFromRp0()
    {
        TrueTypeInterpreter interp = interpreter();
        ExecutionContext ctx = context(interp, lineZone(64)); // rp0 = glyph point 0 at 1px
        // PUSHB[0] 0 ; SZP1 (twilight) ; PUSHB[1] 3 64 ; MSIRP[0]
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 0, 0x14,
                (byte) 0xB1, 3, 64, 0x3A }));
        Zone twilight = ctx.getTwilightZone();
        assertEquals(128, twilight.getOriginalX()[3]);
        assertEquals(128, twilight.getCurrentX()[3]);
    }

    /**
     * MIAP on a twilight point places its origin at the control value along the freedom vector, not
     * the projection vector (FreeType's Ins_MIAP). With a diagonal freedom vector and an x-axis
     * projection the difference shows in the original y.
     */
    @Test
    void testMiapPlacesTwilightPointAlongFreedomVector()
    {
        TrueTypeInterpreter interp = interpreter();
        ExecutionContext ctx = contextWithCvt128(interp, lineZone(0));
        int diagonal = 0x2D41; // 1/sqrt(2) in F2Dot14
        // PUSHB[0] 0 ; SZP0 (twilight) ; PUSHW[1] diagonal diagonal ; SFVFS ; PUSHB[1] 3 0 ; MIAP[0]
        interp.run(ctx, new BytecodeStream(new byte[] { (byte) 0xB0, 0, 0x13,
                (byte) 0xB9, 0x2D, 0x41, 0x2D, 0x41, 0x0B, (byte) 0xB1, 3, 0, 0x3E }));
        Zone twilight = ctx.getTwilightZone();
        assertEquals(Fixed.mul14(128, diagonal), twilight.getOriginalX()[3]);
        assertEquals(Fixed.mul14(128, diagonal), twilight.getOriginalY()[3]);
        assertEquals(128, twilight.getCurrentX()[3]); // then moved along fv until its x projects to 128
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
