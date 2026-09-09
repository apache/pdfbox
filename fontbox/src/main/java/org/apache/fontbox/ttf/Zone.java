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

import java.util.Arrays;

/**
 * A set of points the interpreter can manipulate - either zone 0 (the twilight zone, holding phantom
 * reference points) or zone 1 (the glyph's own outline points plus its appended phantom points). Each
 * point has its scaled-but-unhinted "original" position and a "current" position the bytecode moves,
 * both in F26Dot6, plus per-axis touch flags used by interpolation.
 *
 * @author Apache PDFBox
 */
class Zone
{
    private final int[] currentX;
    private final int[] currentY;
    private final int[] originalX;
    private final int[] originalY;
    private final int[] unscaledX;
    private final int[] unscaledY;
    private final boolean[] touchedX;
    private final boolean[] touchedY;
    private final boolean[] onCurve;
    private final int[] contourEnds;

    /**
     * Allocates a zone of the given size.
     *
     * @param pointCount number of points (including any phantom points)
     * @param contourCount number of contours (0 for the twilight zone)
     */
    public Zone(int pointCount, int contourCount)
    {
        currentX = new int[pointCount];
        currentY = new int[pointCount];
        originalX = new int[pointCount];
        originalY = new int[pointCount];
        unscaledX = new int[pointCount];
        unscaledY = new int[pointCount];
        touchedX = new boolean[pointCount];
        touchedY = new boolean[pointCount];
        onCurve = new boolean[pointCount];
        contourEnds = new int[contourCount];
    }

    /**
     * Zeroes every coordinate and flag. The twilight zone outlives a single program run - it is owned by
     * the interpreter so values {@code prep} puts there survive into each glyph program - so it is reset
     * rather than reallocated when the size changes, as FreeType does in {@code tt_size_run_prep}.
     */
    public void reset()
    {
        Arrays.fill(currentX, 0);
        Arrays.fill(currentY, 0);
        Arrays.fill(originalX, 0);
        Arrays.fill(originalY, 0);
        Arrays.fill(unscaledX, 0);
        Arrays.fill(unscaledY, 0);
        Arrays.fill(touchedX, false);
        Arrays.fill(touchedY, false);
        Arrays.fill(onCurve, false);
        Arrays.fill(contourEnds, 0);
    }

    /** @return the number of points in this zone */
    public int getPointCount()
    {
        return currentX.length;
    }

    /** @return the current (hinted) x coordinates in F26Dot6 */
    public int[] getCurrentX()
    {
        return currentX;
    }

    /** @return the current (hinted) y coordinates in F26Dot6 */
    public int[] getCurrentY()
    {
        return currentY;
    }

    /** @return the original (scaled, unhinted) x coordinates in F26Dot6 */
    public int[] getOriginalX()
    {
        return originalX;
    }

    /** @return the original (scaled, unhinted) y coordinates in F26Dot6 */
    public int[] getOriginalY()
    {
        return originalY;
    }

    /**
     * @return the original <em>unscaled</em> x coordinates in font units. Interpolation and relative
     * measurements use these for the ratio, matching FreeType's {@code orus}, because the unrounded
     * font-unit values preserve precision the scaled F26Dot6 originals would lose.
     */
    public int[] getUnscaledX()
    {
        return unscaledX;
    }

    /** @return the original unscaled y coordinates in font units */
    public int[] getUnscaledY()
    {
        return unscaledY;
    }

    /** @return per-point touch flags for the x axis */
    public boolean[] getTouchedX()
    {
        return touchedX;
    }

    /** @return per-point touch flags for the y axis */
    public boolean[] getTouchedY()
    {
        return touchedY;
    }

    /** @return per-point on-curve flags */
    public boolean[] getOnCurve()
    {
        return onCurve;
    }

    /** @return the index of the last point of each contour */
    public int[] getContourEnds()
    {
        return contourEnds;
    }
}
