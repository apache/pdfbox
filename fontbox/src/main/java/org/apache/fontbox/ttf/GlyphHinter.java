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

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Applies TrueType bytecode hinting (grid-fitting) to a font's glyphs, producing grid-fitted paths.
 * <p>
 * One hinter is created per {@link TrueTypeFont}. It lazily builds a {@link TrueTypeInterpreter} from
 * the font's {@code maxp}/{@code head}/{@code cvt}/{@code fpgm}/{@code prep} tables, runs the font
 * program once, and re-runs the control value program whenever the ppem changes. For each glyph it
 * scales the outline into the pixel grid (appending the phantom points), runs the glyph's instructions
 * and scales the grid-fitted result back into font units, so the rest of the rendering pipeline - which
 * scales font units to device pixels at exactly this ppem - reproduces the grid-fitting.
 * <p>
 * Hinting is best-effort: anything malformed, unsupported, or not applicable (an empty glyph, a simple
 * glyph with no instructions, a ppem below {@code head.lowestRecPPEM} or excluded by the {@code gasp}
 * table, or a {@code prep} that switches hinting off with INSTCTRL) falls back to {@code null}, and the
 * caller renders the raw outline. Composite glyphs are hinted: each component with its own
 * instructions, then the composite's. One bad glyph never disables hinting for the rest of the font.
 * Whether to hint at all is the caller's decision ({@code PDFRenderer.setHintingEnabled(boolean)}, off
 * by default); this class always grid-fits when asked.
 * <p>
 * The interpreter carries a great deal of mutable state - the storage area, the twilight zone, the
 * post-{@code prep} template, the active ppem - so every entry point here is {@code synchronized} and
 * one font hints one glyph at a time. That is correct but it does serialize: a font substituted from
 * the system is held in a process-wide cache, so several rendering threads can share one instance and
 * queue on this monitor. Embedded fonts are per-document and unaffected. If it ever measures as a
 * bottleneck the answer is a per-thread or pooled interpreter, not a weaker lock; until then the simple
 * thing is the right thing. {@code HintingConcurrencyTest} pins the current behavior.
 *
 * @author Apache PDFBox
 */
class GlyphHinter
{
    private static final Logger LOG = LogManager.getLogger(GlyphHinter.class);

    private final TrueTypeFont font;

    private boolean initialized;
    private boolean available;
    private boolean warned;
    private int failureCount;
    private String firstFailure;
    private TrueTypeInterpreter interpreter;
    private GaspTable gasp;
    private int lowestRecPpem;
    private int unitsPerEm;
    private int currentPpem = -1;
    // null until probed: whether the font's bytecode builds glyph geometry (see needsFullControl)
    private Boolean fullControl;
    // while probing, overrides the backward-compatibility decision for glyph programs
    private Boolean forcedBackwardCompatibility;

    /** The ppem the full-control probe hints its sample glyphs at. */
    private static final int PROBE_PPEM = 32;
    /** At most this many glyphs with instructions are sampled by the probe. */
    private static final int PROBE_SAMPLE = 32;
    /** Glyphs examined while looking for the sample, to bound the cost on huge CJK fonts. */
    private static final int PROBE_CANDIDATES = 512;
    /** A sampled glyph "moves" if some point is displaced by more than this (5px at PROBE_PPEM). */
    private static final int PROBE_TOLERANCE = 5 * 64;
    /** The number of moving glyphs that marks a font as needing full control. */
    private static final int PROBE_MIN_MOVING = 2;

    GlyphHinter(TrueTypeFont font)
    {
        this.font = font;
    }

    private synchronized void initialize() throws IOException
    {
        if (initialized)
        {
            return;
        }
        initialized = true;
        available = false;

        MaximumProfileTable maxp = font.getMaximumProfile();
        FontProgramTable fpgm = font.getFontProgram();
        ControlValueProgramTable prep = font.getControlValueProgram();
        ControlValueTable cvt = font.getControlValues();

        // hinting is only meaningful if the font carries a bytecode program
        if (maxp == null || (fpgm == null && prep == null))
        {
            return;
        }

        unitsPerEm = font.getUnitsPerEm();
        gasp = font.getGasp();
        lowestRecPpem = font.getHeader().getLowestRecPPEM();

        interpreter = new TrueTypeInterpreter(maxp.getMaxStackElements(), maxp.getMaxStorage(),
                maxp.getMaxTwilightPoints(), unitsPerEm);
        interpreter.setFontProgram(fpgm != null ? fpgm.getProgram() : null);
        interpreter.setControlValueProgram(prep != null ? prep.getProgram() : null);
        interpreter.setControlValues(cvt != null ? cvt.getValues() : null);
        interpreter.prepareFontProgram();
        available = true;
    }

    /**
     * Returns the grid-fitted path of the glyph at the given ppem, or {@code null} if hinting does not
     * apply and the caller should render the raw outline.
     *
     * @param gid the glyph id
     * @param ppem the pixels-per-em to grid-fit to
     * @return the hinted path in font units, or null
     */
    synchronized GeneralPath getPath(int gid, int ppem)
    {
        Hinted hinted = hint(gid, ppem);
        if (hinted == null)
        {
            return null;
        }
        // scale the grid-fitted coordinates back into font units (drop the phantom points)
        int[] hintedX = new int[hinted.pointCount];
        int[] hintedY = new int[hinted.pointCount];
        int[] curX = hinted.zone.getCurrentX();
        int[] curY = hinted.zone.getCurrentY();
        for (int i = 0; i < hinted.pointCount; i++)
        {
            hintedX[i] = toFontUnits(curX[i], ppem);
            hintedY[i] = toFontUnits(curY[i], ppem);
        }
        return new GlyphRenderer(hinted.gd, hintedX, hintedY).getPath();
    }

    /**
     * Returns the grid-fitted glyph points in F26Dot6 device coordinates (the raw interpreter output,
     * before scaling back to font units, and excluding the phantom points), or {@code null} if hinting
     * does not apply. This is the form compared against a FreeType reference dump by the golden tests.
     *
     * @param gid the glyph id
     * @param ppem the pixels-per-em to grid-fit to
     * @return a {@code {x[], y[]}} pair in F26Dot6, or null
     */
    synchronized int[][] getHintedPointsF26Dot6(int gid, int ppem)
    {
        Hinted hinted = hint(gid, ppem);
        if (hinted == null)
        {
            return null;
        }
        int[] x = new int[hinted.pointCount];
        int[] y = new int[hinted.pointCount];
        System.arraycopy(hinted.zone.getCurrentX(), 0, x, 0, hinted.pointCount);
        System.arraycopy(hinted.zone.getCurrentY(), 0, y, 0, hinted.pointCount);
        return new int[][] { x, y };
    }

    /**
     * Runs the control value program untraced, then grid-fits the glyph with an execution tracer
     * attached, so the per-instruction trace can be diffed against FreeType's {@code ttinterp} trace.
     * For development/debugging only.
     *
     * @param gid the glyph id
     * @param ppem the pixels-per-em
     * @param out where to write the trace
     * @param tracePoint a glyph point index to log per instruction, or -1
     * @throws IOException if the font could not be read
     */
    synchronized void traceGlyph(int gid, int ppem, java.io.PrintStream out, int tracePoint)
            throws IOException
    {
        initialize();
        if (!available)
        {
            return;
        }
        setActivePpem(ppem);
        interpreter.setTracer(new ExecutionTracer(out, tracePoint));
        try
        {
            hint(gid, ppem, 0);
        }
        finally
        {
            interpreter.setTracer(null);
        }
    }

    /** Maximum composite nesting depth, to bound recursion on pathological fonts. */
    private static final int MAX_COMPONENT_DEPTH = 8;

    /** Runs all gating, then grid-fits the glyph, returning the executed zone or null on fallback. */
    private Hinted hint(int gid, int ppem)
    {
        if (ppem <= 0)
        {
            return null;
        }
        try
        {
            initialize();
            if (!available)
            {
                return null;
            }
            // fonts whose bytecode builds the glyphs are hinted at every size: FreeType likewise
            // exempts its "tricky" fonts from both size gates
            boolean full = needsFullControl();
            // head.lowestRecPPEM is the vendor's "smallest readable size in pixels" for the
            // outlines. Fonts that ship bitmap strikes for small sizes (MS Mincho/Gothic say 25) set
            // it above the sizes the bitmaps cover; their instructions were never meant to run there,
            // and doing so under grayscale forces every thin stroke to a full pixel. Text fonts
            // without strikes sit at 6-9, so this never fires for them.
            if (!full && ppem < lowestRecPpem)
            {
                return null;
            }
            // gasp gate: if a gasp table is present and does not request grid-fitting here, skip.
            // (GRIDFIT without DOGRAY is deliberately NOT treated as "do not hint": Arial and
            // Liberation flag 9-17ppem that way, so it would switch hinting off for body text.)
            if (!full && gasp != null && !gasp.isGridFit(ppem))
            {
                return null;
            }
            setActivePpem(ppem);
            // INSTCTRL bit 1: prep switched hinting off at this size (fonts do this above a ppem
            // threshold); render the raw outline, as FreeType does in tt_loader_init
            if ((interpreter.getSavedState().getInstructControl() & 1) != 0)
            {
                return null;
            }
            return hint(gid, ppem, 0);
        }
        catch (IOException | HintingException e)
        {
            logFailure(gid, ppem, e);
            return null;
        }
    }

    /**
     * Reports a glyph that could not be hinted. Only the first failure in a font is a warning carrying
     * the stack trace; the rest go to debug. Hinting is attempted per {@code (glyph, ppem)} pair, so a
     * font whose bytecode never runs - a malformed program, or one using something unimplemented - would
     * otherwise emit thousands of identical stack traces for a page of CJK text.
     */
    private void logFailure(int gid, int ppem, Exception e)
    {
        failureCount++;
        if (firstFailure == null)
        {
            firstFailure = "glyph " + gid + " at " + ppem + "ppem: " + e;
        }
        if (warned)
        {
            LOG.debug("hinting failed for glyph {} at {}ppem, using raw outline", gid, ppem, e);
            return;
        }
        warned = true;
        LOG.warn("hinting failed for glyph {} at {}ppem in font {}, using raw outline; further "
                + "failures in this font are logged at debug level", gid, ppem, fontName(), e);
    }

    /**
     * @return how many (glyph, ppem) hinting attempts failed and fell back to the raw outline, as
     * opposed to not applying (no instructions, gasp, lowestRecPPEM); for tests
     */
    synchronized int getFailureCount()
    {
        return failureCount;
    }

    /** @return the first failure, "glyph &lt;gid&gt; at &lt;ppem&gt;ppem: &lt;exception&gt;", or null */
    synchronized String getFirstFailure()
    {
        return firstFailure;
    }

    /** The font's PostScript name for the warning above, best-effort - we are already handling a fault. */
    private String fontName()
    {
        try
        {
            return font.getName();
        }
        catch (IOException e)
        {
            return "<unknown>";
        }
    }

    /**
     * Whether the font's bytecode builds its glyph geometry rather than just fitting outlines to the
     * pixel grid. A small set of fonts - DynaLab CJK faces such as MingLiU and DFKai-SB, which FreeType
     * calls "tricky" - position and scale glyph components with their instructions, in x as well as y,
     * so they only render correctly with full bytecode control: no v40 backward compatibility (which
     * suppresses x moves) and no size gates.
     * <p>
     * Rather than recognise such fonts by name, this measures the behaviour itself, once per font: it
     * hints a spread of up to {@value #PROBE_SAMPLE} glyphs that carry instructions at
     * {@value #PROBE_PPEM} ppem with backward compatibility on and with it off (full control), and
     * compares them point by point with each other and with the raw outline. Ordinary hinting only
     * rounds edges to the grid, so with backward compatibility on no point moves more than a pixel or
     * two from the raw outline; a program that builds the glyphs moves components by many pixels even
     * then, and moves them differently again with it off. A glyph counts when both comparisons exceed
     * 5px (see {@link #buildsGeometry}), and {@value #PROBE_MIN_MOVING} such glyphs mark the font.
     * <p>
     * Measured at {@value #PROBE_PPEM} ppem on 267 fonts from CJK PDFs and system fonts, against
     * FreeType's own classification (second-largest glyph per font): fonts FreeType calls tricky are
     * at least 7.0px from raw with backward compatibility on and differ by at least 8.9px with it off;
     * ordinary fonts stay within 2.0px of raw - including Giovanni Book, whose broken x instructions
     * move points 90px with it off. No false positives; the only misses were two tiny subsets whose
     * hinted glyphs stay within 0.4px of the raw outline, so full control changes nothing for them.
     *
     * @return true if glyph programs must run with full control
     * @throws IOException if the glyph table cannot be read
     */
    private boolean needsFullControl() throws IOException
    {
        if (fullControl == null)
        {
            fullControl = probeFullControl();
            // the probe ran prep at its own size
            currentPpem = -1;
        }
        return fullControl;
    }

    private boolean probeFullControl() throws IOException
    {
        int moving = 0;
        for (int gid : probeSample())
        {
            try
            {
                int[][] compatible = probeHintedPoints(gid, true);
                int[][] full = probeHintedPoints(gid, false);
                if (compatible == null || full == null)
                {
                    continue;
                }
                int[][] raw = probeRawPoints(gid, full[0].length);
                if (raw != null && buildsGeometry(raw, compatible, full) && ++moving >= PROBE_MIN_MOVING)
                {
                    return true;
                }
            }
            catch (IOException | HintingException e)
            {
                // a glyph that cannot be hinted says nothing about the font
                LOG.debug("full-control probe could not hint glyph {}", gid, e);
            }
        }
        return false;
    }

    /**
     * Up to PROBE_SAMPLE glyphs with instructions, spread evenly over the font's non-empty glyphs.
     * Non-empty glyphs are found from the loca offsets without parsing them - an embedded CID subset
     * keeps its full glyph count with only a handful of glyphs present - and at most
     * PROBE_CANDIDATES of them, spread evenly, are parsed.
     */
    private int[] probeSample() throws IOException
    {
        IndexToLocationTable loca = font.getIndexToLocation();
        long[] offsets = loca != null ? loca.getOffsets() : null;
        if (offsets == null)
        {
            return new int[0];
        }
        int[] present = new int[offsets.length - 1];
        int presentCount = 0;
        for (int gid = 0; gid + 1 < offsets.length; gid++)
        {
            if (offsets[gid + 1] > offsets[gid])
            {
                present[presentCount++] = gid;
            }
        }
        int candidates = Math.min(presentCount, PROBE_CANDIDATES);
        int[] instructed = new int[candidates];
        int count = 0;
        for (int i = 0; i < candidates; i++)
        {
            int gid = present[(int) ((long) i * presentCount / candidates)];
            GlyphData glyph = font.getGlyph().getGlyph(gid);
            if (glyph == null || !(glyph.getDescription() instanceof GlyfDescript))
            {
                continue;
            }
            GlyfDescript gd = (GlyfDescript) glyph.getDescription();
            int[] instructions = gd.getInstructions();
            if (gd.getPointCount() > 0
                    && (gd.isComposite() || instructions != null && instructions.length > 0))
            {
                instructed[count++] = gid;
            }
        }
        // spread the sample over the whole font: the glyphs whose programs build geometry are the
        // ideographs, not the Latin and punctuation at the low glyph ids
        int size = Math.min(count, PROBE_SAMPLE);
        int[] sample = new int[size];
        for (int i = 0; i < size; i++)
        {
            sample[i] = instructed[(int) ((long) i * count / size)];
        }
        return sample;
    }

    /** Hints one glyph at PROBE_PPEM with backward compatibility forced, ignoring the size gates. */
    private int[][] probeHintedPoints(int gid, boolean backwardCompatibility) throws IOException
    {
        setActivePpem(PROBE_PPEM);
        forcedBackwardCompatibility = backwardCompatibility;
        try
        {
            Hinted hinted = hint(gid, PROBE_PPEM, 0);
            if (hinted == null)
            {
                return null;
            }
            int[] x = Arrays.copyOf(hinted.zone.getCurrentX(), hinted.pointCount);
            int[] y = Arrays.copyOf(hinted.zone.getCurrentY(), hinted.pointCount);
            return new int[][] { x, y };
        }
        finally
        {
            forcedBackwardCompatibility = null;
        }
    }

    /**
     * The glyph's scaled, unhinted outline at PROBE_PPEM, in the frame of the hinted points (origin
     * at x = 0, as the parsed description has it), or null if its point count differs.
     */
    private int[][] probeRawPoints(int gid, int pointCount) throws IOException
    {
        GlyphDescription gd = font.getGlyph().getGlyph(gid).getDescription();
        if (gd.isComposite())
        {
            gd.resolve();
        }
        if (gd.getPointCount() != pointCount)
        {
            return null;
        }
        int[][] raw = new int[2][pointCount];
        for (int i = 0; i < pointCount; i++)
        {
            raw[0][i] = Fixed.scale(gd.getXCoordinate(i), PROBE_PPEM, unitsPerEm);
            raw[1][i] = Fixed.scale(gd.getYCoordinate(i), PROBE_PPEM, unitsPerEm);
        }
        return raw;
    }

    /**
     * Whether a glyph's program builds its geometry rather than fitting it to the grid: backward
     * compatibility changes the result by more than PROBE_TOLERANCE, <em>and</em> even with backward
     * compatibility on the result is that far from the raw outline. The second condition tells a font
     * that needs full control (MingLiU: at least 7px from raw with it on) from one whose x
     * instructions are merely broken and are hidden by backward compatibility (Giovanni Book: about
     * 1px from raw with it on, 90px with it off) - giving the latter full control would wreck it.
     *
     * @param raw the scaled, unhinted outline
     * @param compatible the outline hinted with backward compatibility on
     * @param full the outline hinted with full control
     * @return true if the glyph counts towards needing full control
     */
    static boolean buildsGeometry(int[][] raw, int[][] compatible, int[][] full)
    {
        return pointDelta(compatible, full) > PROBE_TOLERANCE
                && pointDelta(raw, compatible) > PROBE_TOLERANCE;
    }

    /** The largest displacement of any point between two outlines of the same glyph, on either axis. */
    static int pointDelta(int[][] a, int[][] b)
    {
        int delta = 0;
        for (int i = 0; i < a[0].length; i++)
        {
            delta = Math.max(delta, Math.max(Math.abs(a[0][i] - b[0][i]), Math.abs(a[1][i] - b[1][i])));
        }
        return delta;
    }

    /** @return whether the probe found that this font's bytecode builds its glyphs; for tests */
    synchronized boolean isFullControl() throws IOException
    {
        initialize();
        return available && needsFullControl();
    }

    /**
     * Re-runs the control value program if the ppem changed. The guard is load-bearing, not just an
     * optimization: {@code setPpem} clears the storage area and twilight zone before running
     * {@code prep}, so re-running it per glyph would wipe the values {@code prep} seeded for the glyph
     * programs to read.
     */
    private void setActivePpem(int ppem) throws IOException
    {
        if (ppem != currentPpem)
        {
            // setPpem rescales the CVT and clears storage before running prep; if prep throws, the
            // interpreter no longer holds currentPpem's state, so force the next call to re-run it
            currentPpem = -1;
            interpreter.setPpem(ppem, ppem);
            currentPpem = ppem;
        }
    }

    /** Grid-fits one glyph (simple or composite), recursing into components. */
    private Hinted hint(int gid, int ppem, int depth) throws IOException
    {
        if (depth > MAX_COMPONENT_DEPTH)
        {
            return null;
        }
        GlyphData glyph = font.getGlyph().getGlyph(gid);
        if (glyph == null)
        {
            return null;
        }
        GlyphDescription gd = glyph.getDescription();
        if (!(gd instanceof GlyfDescript))
        {
            return null;
        }
        if (gd.isComposite())
        {
            gd.resolve();
            if (gd.getPointCount() == 0)
            {
                return null;
            }
            return hintComposite(glyph, (GlyfCompositeDescript) gd, gid, ppem, depth);
        }
        if (gd.getContourCount() == 0 || gd.getPointCount() == 0)
        {
            // empty glyph (e.g. space, newline): nothing to hint
            return null;
        }
        int[] instructions = ((GlyfDescript) gd).getInstructions();
        if (instructions == null || instructions.length == 0)
        {
            return null;
        }
        int pointCount = gd.getPointCount();
        Zone zone = buildZone(glyph, gd, gid, ppem, pointCount, gd.getContourCount());
        ExecutionContext ctx = runProgram(zone, instructions, ppem, false);
        // move the origin to x = 0, as FreeType does after hinting (TT_Load_Glyph translates by
        // -pp1.x). Under backward compatibility FreeType keeps the unhinted phantom points
        // (TT_Hint_Glyph), otherwise the hinted ones
        int originX = ctx.isBackwardCompatibility() ? zone.getOriginalX()[pointCount]
                : zone.getCurrentX()[pointCount];
        int[] curX = zone.getCurrentX();
        for (int i = 0; i < pointCount + 4; i++)
        {
            curX[i] -= originX;
        }
        return new Hinted(gd, zone, pointCount);
    }

    /**
     * Grid-fits a composite glyph the way FreeType does: each component is hinted on its own, then
     * transformed and offset into the composite's coordinate space, the phantom points appended, and
     * finally the composite's own instructions (if any) run over the assembled outline.
     */
    private Hinted hintComposite(GlyphData glyph, GlyfCompositeDescript composite, int gid, int ppem,
            int depth) throws IOException
    {
        int pointCount = composite.getPointCount();
        int contourCount = composite.getContourCount();
        Zone zone = new Zone(pointCount + 4, contourCount);

        for (GlyfCompositeComp comp : composite.getComponents())
        {
            assembleComponent(comp, ppem, depth, zone);
        }
        int[] ends = zone.getContourEnds();
        for (int c = 0; c < contourCount; c++)
        {
            ends[c] = composite.getEndPtOfContours(c);
        }
        appendPhantomPoints(glyph, gid, ppem, pointCount, zone);
        // FreeType's TT_Hint_Glyph: a composite's instructions "completely refer to the (already)
        // hinted subglyphs" - the unscaled coordinates (orus) are the assembled positions, i.e. the
        // originals, and the program runs at a scale of 1 (see ExecutionContext.isComposite)
        System.arraycopy(zone.getOriginalX(), 0, zone.getUnscaledX(), 0, pointCount + 4);
        System.arraycopy(zone.getOriginalY(), 0, zone.getUnscaledY(), 0, pointCount + 4);

        int[] instructions = composite.getInstructions();
        if (instructions != null && instructions.length > 0)
        {
            runProgram(zone, instructions, ppem, true);
        }
        return new Hinted(composite, zone, pointCount);
    }

    /**
     * Hints one component glyph and writes its transformed/offset points into the composite's zone
     * arrays. The component's grid-fitted outline goes to both the current and the original arrays,
     * as FreeType bakes hinted components into the composite before running its instructions.
     */
    private void assembleComponent(GlyfCompositeComp comp, int ppem, int depth, Zone zone)
            throws IOException
    {
        int componentGid = comp.getGlyphIndex();
        int first = comp.getFirstIndex();

        GlyphData componentGlyph = font.getGlyph().getGlyph(componentGid);
        GlyphDescription cgd = componentGlyph != null ? componentGlyph.getDescription() : null;
        if (cgd == null)
        {
            return;
        }
        if (cgd.isComposite())
        {
            cgd.resolve();
        }
        int count = cgd.getPointCount();
        boolean[] onCurve = zone.getOnCurve();

        // scaled-but-unhinted component points, used when the component is not hinted
        int[] cOrgX = new int[count];
        int[] cOrgY = new int[count];
        for (int k = 0; k < count; k++)
        {
            cOrgX[k] = Fixed.scale(cgd.getXCoordinate(k), ppem, unitsPerEm);
            cOrgY[k] = Fixed.scale(cgd.getYCoordinate(k), ppem, unitsPerEm);
            onCurve[first + k] = (cgd.getFlags(k) & GlyfDescript.ON_CURVE) != 0;
        }

        // grid-fitted component points (its own instructions executed); fall back to unhinted
        int[] cCurX = cOrgX;
        int[] cCurY = cOrgY;
        Hinted hintedComponent = hint(componentGid, ppem, depth + 1);
        if (hintedComponent != null && hintedComponent.pointCount == count)
        {
            cCurX = hintedComponent.zone.getCurrentX();
            cCurY = hintedComponent.zone.getCurrentY();
        }

        // device-space offset. With ROUND_XY_TO_GRID FreeType's v40 interpreter rounds only the y
        // offset to the grid, leaving x unrounded to approximate ClearType's fine horizontal grid
        // (TT_Process_Composite_Component)
        int offsetX = Fixed.scale(comp.getXTranslate(), ppem, unitsPerEm);
        int offsetY = Fixed.scale(comp.getYTranslate(), ppem, unitsPerEm);
        if ((comp.getFlags() & GlyfCompositeComp.ROUND_XY_TO_GRID) != 0)
        {
            offsetY = Fixed.round(offsetY);
        }

        int[] curX = zone.getCurrentX();
        int[] curY = zone.getCurrentY();
        int[] orgX = zone.getOriginalX();
        int[] orgY = zone.getOriginalY();
        for (int k = 0; k < count; k++)
        {
            curX[first + k] = comp.scaleX(cCurX[k], cCurY[k]) + offsetX;
            curY[first + k] = comp.scaleY(cCurX[k], cCurY[k]) + offsetY;
            // FreeType bakes each hinted component into the composite and copies cur -> org before
            // running the composite program, so the original equals the assembled hinted position
            // (a SHC/MDRP in the composite then measures zero movement for an unmoved component point)
            orgX[first + k] = curX[first + k];
            orgY[first + k] = curY[first + k];
        }
    }

    /** Clones the saved post-prep state, resets it for the glyph, and runs the program over the zone. */
    private ExecutionContext runProgram(Zone zone, int[] instructions, int ppem, boolean composite)
    {
        GraphicsState saved = interpreter.getSavedState();
        // INSTCTRL bit 2: glyph programs start from the default graphics state rather than prep's
        GraphicsState gs = (saved.getInstructControl() & 2) != 0 ? new GraphicsState() : saved.copy();
        gs.resetForGlyph();
        ExecutionContext ctx = interpreter.newContext(gs);
        ctx.setPpem(ppem);
        ctx.setGlyphZone(zone);
        // v40 grayscale "backward compatibility" applies to the glyph program only, never fpgm/prep,
        // which build control values via twilight-zone x/y moves that must not be suppressed. A
        // native-ClearType font waives it from prep with INSTCTRL bit 4; read from gs, so a bit-2
        // reset to the default state also clears the waiver, as in FreeType's tt_loader_init.
        // A font whose bytecode builds the glyph geometry gets full control, as FreeType gives its
        // "tricky" fonts; the probe forces the mode while it measures.
        if (forcedBackwardCompatibility != null)
        {
            ctx.setBackwardCompatibility(forcedBackwardCompatibility);
        }
        else
        {
            ctx.setBackwardCompatibility(!Boolean.TRUE.equals(fullControl)
                    && (gs.getInstructControl() & 4) == 0);
        }
        ctx.setComposite(composite);
        interpreter.run(ctx, new BytecodeStream(toByteArray(instructions)));
        return ctx;
    }

    /** The result of grid-fitting one glyph: its description, the executed zone, and its point count
     * (without the appended phantom points). */
    private static final class Hinted
    {
        private final GlyphDescription gd;
        private final Zone zone;
        private final int pointCount;

        Hinted(GlyphDescription gd, Zone zone, int pointCount)
        {
            this.gd = gd;
            this.zone = zone;
            this.pointCount = pointCount;
        }
    }

    private Zone buildZone(GlyphData glyph, GlyphDescription gd, int gid, int ppem, int pointCount,
            int contourCount) throws IOException
    {
        // four phantom points are appended after the glyph's own points
        Zone zone = new Zone(pointCount + 4, contourCount);
        int[] curX = zone.getCurrentX();
        int[] curY = zone.getCurrentY();
        int[] orgX = zone.getOriginalX();
        int[] orgY = zone.getOriginalY();
        int[] unsX = zone.getUnscaledX();
        int[] unsY = zone.getUnscaledY();
        boolean[] onCurve = zone.getOnCurve();
        // GlyphData shifts a simple glyph's x coordinates by lsb - xMin so its origin is at 0.
        // FreeType hints the unshifted outline, with the origin phantom point at xMin - lsb, and
        // moves the origin afterwards; undo the shift so rounding and phantom-point positions match
        HorizontalMetricsTable hmtx = font.getHorizontalMetrics();
        int leftSideBearing = hmtx != null ? hmtx.getLeftSideBearing(gid) : 0;
        int parseShift = (short) (leftSideBearing - glyph.getXMinimum());
        for (int i = 0; i < pointCount; i++)
        {
            int fx = gd.getXCoordinate(i) - parseShift;
            int fy = gd.getYCoordinate(i);
            unsX[i] = fx;
            unsY[i] = fy;
            int x = Fixed.scale(fx, ppem, unitsPerEm);
            int y = Fixed.scale(fy, ppem, unitsPerEm);
            orgX[i] = x;
            orgY[i] = y;
            curX[i] = x;
            curY[i] = y;
            onCurve[i] = (gd.getFlags(i) & GlyfDescript.ON_CURVE) != 0;
        }
        int[] ends = zone.getContourEnds();
        for (int c = 0; c < contourCount; c++)
        {
            ends[c] = gd.getEndPtOfContours(c);
        }
        appendPhantomPoints(glyph, gid, ppem, pointCount, zone);
        return zone;
    }

    private void appendPhantomPoints(GlyphData glyph, int gid, int ppem, int pointCount, Zone zone)
            throws IOException
    {
        HorizontalMetricsTable hmtx = font.getHorizontalMetrics();
        int advanceWidth = hmtx != null ? hmtx.getAdvanceWidth(gid) : unitsPerEm;
        int leftSideBearing = hmtx != null ? hmtx.getLeftSideBearing(gid) : 0;
        int originX = glyph.getXMinimum() - leftSideBearing;
        int yMax = glyph.getYMaximum();

        // pp1 = origin, pp2 = origin + advance (horizontal); pp3/pp4 are the vertical pair
        int[] px = { originX, originX + advanceWidth, 0, 0 };
        int[] py = { 0, 0, yMax, yMax - unitsPerEm };
        for (int i = 0; i < 4; i++)
        {
            int index = pointCount + i;
            zone.getUnscaledX()[index] = px[i];
            zone.getUnscaledY()[index] = py[i];
            zone.getOriginalX()[index] = Fixed.scale(px[i], ppem, unitsPerEm);
            zone.getOriginalY()[index] = Fixed.scale(py[i], ppem, unitsPerEm);
            // FreeType rounds the phantom points to the grid before running the glyph program
            zone.getCurrentX()[index] = Fixed.round(zone.getOriginalX()[index]);
            zone.getCurrentY()[index] = Fixed.round(zone.getOriginalY()[index]);
        }
    }

    /** Scales an F26Dot6 device coordinate back to font units. */
    private int toFontUnits(int f26dot6, int ppem)
    {
        long numerator = (long) f26dot6 * unitsPerEm;
        long denominator = (long) ppem * Fixed.ONE;
        long half = denominator / 2;
        return (int) ((numerator >= 0 ? numerator + half : numerator - half) / denominator);
    }

    private static byte[] toByteArray(int[] instructions)
    {
        byte[] bytes = new byte[instructions.length];
        for (int i = 0; i < instructions.length; i++)
        {
            bytes[i] = (byte) instructions[i];
        }
        return bytes;
    }
}
