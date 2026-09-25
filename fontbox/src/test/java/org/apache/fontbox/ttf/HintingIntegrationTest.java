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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests: drive hinting through {@link TrueTypeFont#getHintedPath(int, int)} at the
 * {@link GeneralPath} level. LiberationSans carries a full bytecode program; Lohit-Bengali has a cvt
 * but no fpgm/prep, so it exercises the no-bytecode fallback.
 */
class HintingIntegrationTest
{
    private static TrueTypeFont parse(String resource) throws IOException
    {
        try (InputStream is = HintingIntegrationTest.class.getResourceAsStream(resource))
        {
            assertNotNull(is, "missing test resource " + resource);
            return new TTFParser().parse(new RandomAccessReadBuffer(is));
        }
    }

    private static int gid(TrueTypeFont font, int codePoint) throws IOException
    {
        return font.getUnicodeCmapLookup().getGlyphId(codePoint);
    }

    /** Flattens a path to a coordinate list so two paths can be compared point-for-point. */
    private static double[] flatten(GeneralPath path)
    {
        double[] coords = new double[6];
        java.util.List<Double> out = new java.util.ArrayList<>();
        for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next())
        {
            int type = it.currentSegment(coords);
            out.add((double) type);
            for (int i = 0; i < 6; i++)
            {
                out.add(coords[i]);
            }
        }
        double[] array = new double[out.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = out.get(i);
        }
        return array;
    }

    @Test
    void testGaspGate() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        int h = gid(font, 'H');
        // LiberationSans gasp: grid-fitting is off at <=8 ppem, on above it
        assertNull(font.getHintedPath(h, 8), "no hinting expected at 8ppem (gasp)");
        assertNotNull(font.getHintedPath(h, 16), "hinting expected at 16ppem");
    }

    /**
     * {@code head.lowestRecPPEM} is the vendor's smallest readable outline size. CJK fonts that ship
     * bitmap strikes for small sizes set it to 20-28 (MS Mincho/Gothic, MingLiU: 25; Ricoh HG*: 28)
     * and their instructions, run under grayscale at those sizes, make the text heavy; text fonts sit
     * at 6-9. No test font declares a value above its gasp threshold, so the value is raised on
     * Liberation Sans (normally 8) before the hinter first reads it.
     */
    @Test
    void testLowestRecPpemGate() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        font.getHeader().setLowestRecPPEM(20);
        int h = gid(font, 'H');
        assertNull(font.getHintedPath(h, 16), "no hinting below lowestRecPPEM");
        assertNotNull(font.getHintedPath(h, 20), "hinting expected from lowestRecPPEM up");
    }

    @Test
    void testHintedDiffersFromRawOutline() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        int h = gid(font, 'H');
        GeneralPath hinted = font.getHintedPath(h, 16);
        assertNotNull(hinted);
        GeneralPath raw = font.getGlyph().getGlyph(h).getPath();
        // grid-fitting must actually change the outline (proves the pipeline is wired in)
        assertFalse(Arrays.equals(flatten(hinted), flatten(raw)),
                "hinted path should differ from the raw outline");
    }

    @Test
    void testDeterministic() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        int h = gid(font, 'H');
        double[] first = flatten(font.getHintedPath(h, 16));
        double[] second = flatten(font.getHintedPath(h, 16));
        // re-hinting the same glyph at the same ppem is deterministic (no leaked state)
        assertTrue(Arrays.equals(first, second));
    }

    /**
     * Hinting one glyph must not change the next. The storage area and twilight zone are deliberately
     * shared across the glyphs hinted at one size (that is how {@code prep} seeds them), so this pins
     * down that nothing <em>else</em> leaks between glyphs - graphics state, zone contents, the cached
     * ppem. A composite is in the run because it re-enters the hinter for each component.
     *
     * <p>This is a property of a well-behaved font rather than a universal law: a glyph program may
     * legally write storage, and FreeType would carry that into the next glyph too. LiberationSans does
     * not, so any difference here is a bug on our side.
     */
    @Test
    void testHintingIsIndependentOfGlyphOrder() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        int h = gid(font, 'H');
        int eacute = gid(font, 0x00E9);
        assertTrue(font.getGlyph().getGlyph(eacute).getNumberOfContours() < 0,
                "expected e-acute to be a composite glyph");

        double[] before = flatten(font.getHintedPath(h, 16));
        for (char c : "oxn8".toCharArray())
        {
            font.getHintedPath(gid(font, c), 16);
        }
        font.getHintedPath(eacute, 16);
        double[] after = flatten(font.getHintedPath(h, 16));

        assertTrue(Arrays.equals(before, after),
                "hinting other glyphs must not change the result for 'H'");
    }

    /**
     * Returning to a ppem must reproduce the earlier result. A ppem change re-runs the control value
     * program, which clears the storage area and twilight zone, so this covers the round trip out of a
     * size and back into it.
     */
    @Test
    void testHintingIsIndependentOfPpemOrder() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        int h = gid(font, 'H');

        double[] before = flatten(font.getHintedPath(h, 16));
        font.getHintedPath(h, 11);
        font.getHintedPath(h, 24);
        double[] after = flatten(font.getHintedPath(h, 16));

        assertTrue(Arrays.equals(before, after),
                "hinting at other ppems must not change the result at 16ppem");
    }

    /**
     * A ppem change rescales the CVT and clears storage before {@code prep} runs, so a {@code prep}
     * that fails at one size leaves the interpreter holding that size's state. Returning to the earlier
     * size must re-run {@code prep} rather than trust the cached ppem. Liberation Sans's own
     * {@code prep} is prefixed with {@code MPPEM 11 EQ IF <invalid opcode> EIF} so it fails at 11ppem
     * only.
     */
    @Test
    void testFailedPrepDoesNotLeaveStaleState() throws IOException
    {
        TrueTypeFont font = withPrepPrefix(0x4B, 0xB0, 11, 0x54, 0x58, 0x28, 0x59);
        int h = gid(font, 'H');
        double[] before = flatten(font.getHintedPath(h, 16));
        assertNull(font.getHintedPath(h, 11), "prep fails at 11ppem, so no hinting there");
        double[] after = flatten(font.getHintedPath(h, 16));

        assertTrue(Arrays.equals(before, after),
                "a prep failure at another size must not change the result at 16ppem");
    }

    /**
     * A component with ROUND_XY_TO_GRID has its y offset rounded to the grid, but under the v40
     * interpreter not its x offset (FreeType's TT_Process_Composite_Component). In Liberation Sans the
     * '¼' glyph (no instructions of its own) places four.sups at (952, -561) font units with that flag;
     * at 11ppem that scales to (327.25, -192.84)/64 px, and FreeType offsets the component's points by exactly
     * (+327, -192).
     */
    @Test
    void testRoundXyToGridRoundsOnlyTheComponentYOffset() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        assertEquals(2048, font.getUnitsPerEm());
        GlyphHinter hinter = new GlyphHinter(font);
        GlyfCompositeDescript quarter =
                (GlyfCompositeDescript) font.getGlyph().getGlyph(gid(font, 0x00BC)).getDescription();
        quarter.resolve();
        GlyfCompositeComp four = quarter.getComponents().get(2);
        assertEquals(952, four.getXTranslate());
        assertEquals(-561, four.getYTranslate());
        assertTrue((four.getFlags() & GlyfCompositeComp.ROUND_XY_TO_GRID) != 0);

        // four.sups has no instructions, so it enters the composite as its plain scaled outline
        GlyphDescription component = font.getGlyph().getGlyph(four.getGlyphIndex()).getDescription();
        assertNull(hinter.getHintedPointsF26Dot6(four.getGlyphIndex(), 11));

        int[][] composite = hinter.getHintedPointsF26Dot6(gid(font, 0x00BC), 11);
        for (int k = 0; k < component.getPointCount(); k++)
        {
            int x = Fixed.scale(component.getXCoordinate(k), 11, 2048);
            int y = Fixed.scale(component.getYCoordinate(k), 11, 2048);
            assertEquals(327, composite[0][four.getFirstIndex() + k] - x, "x of point " + k);
            assertEquals(-192, composite[1][four.getFirstIndex() + k] - y, "y of point " + k);
        }
    }

    /** INSTCTRL(1,1) in prep switches hinting off at that size: FreeType renders the raw outline. */
    @Test
    void testInstctrlInPrepCanDisableHinting() throws IOException
    {
        TrueTypeFont font = withPrepPrefix(0xB1, 1, 1, 0x8E);
        assertNull(font.getHintedPath(gid(font, 'H'), 16));
    }

    /**
     * INSTCTRL(4,3) in prep is the native-ClearType waiver: glyph programs run without the v40
     * backward-compatibility rules, so x-direction moves are no longer suppressed and the result
     * differs from the default.
     */
    @Test
    void testInstctrlInPrepCanWaiveBackwardCompatibility() throws IOException
    {
        TrueTypeFont plain = parse("/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont waived = withPrepPrefix(0xB1, 4, 3, 0x8E);
        int h = gid(plain, 'H');
        assertFalse(Arrays.equals(flatten(plain.getHintedPath(h, 16)),
                flatten(waived.getHintedPath(h, 16))));
    }

    /**
     * Loads Liberation Sans with the given bytecode prepended to its {@code prep}, before the hinter
     * first reads the table.
     */
    private static TrueTypeFont withPrepPrefix(int... prefix) throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        byte[] prep = font.getControlValueProgram().getProgram();
        byte[] program = new byte[prefix.length + prep.length];
        for (int i = 0; i < prefix.length; i++)
        {
            program[i] = (byte) prefix[i];
        }
        System.arraycopy(prep, 0, program, prefix.length, prep.length);
        ControlValueProgramTable table = new ControlValueProgramTable();
        table.setTag(ControlValueProgramTable.TAG);
        table.setLength(program.length);
        table.read(font, new RandomAccessReadDataStream(new RandomAccessReadBuffer(program)));
        font.addTable(table);
        return font;
    }

    @Test
    void testFontWithoutBytecodeFallsBack() throws IOException
    {
        // Lohit-Bengali has a cvt but no fpgm and no prep: there is nothing to execute
        TrueTypeFont font = parse("/ttf/Lohit-Bengali.ttf");
        int gid = font.getUnicodeCmapLookup().getGlyphId(0x0985); // Bengali letter A
        assertNull(font.getHintedPath(gid, 16));
    }

    @Test
    void testEmptyGlyphFallsBack() throws IOException
    {
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        // the space glyph has no contours; hinting must fall back rather than fail
        assertNull(font.getHintedPath(gid(font, ' '), 16));
    }

    @Test
    void testCommonGlyphsHintWithoutFallingBack() throws IOException
    {
        // a real-font smoke test: every one of these simple glyphs carries instructions and must
        // grid-fit without throwing (which would silently fall back to null). This is what caught the
        // MIRP stack-order bug.
        TrueTypeFont font = parse("/ttf/LiberationSans-Regular.ttf");
        String sample = "HILEToxn0123456789";
        for (int ppem : new int[] { 11, 13, 16, 24 })
        {
            for (int i = 0; i < sample.length(); i++)
            {
                char c = sample.charAt(i);
                assertNotNull(font.getHintedPath(gid(font, c), ppem),
                        "expected '" + c + "' to hint at " + ppem + "ppem");
            }
        }
    }
}
