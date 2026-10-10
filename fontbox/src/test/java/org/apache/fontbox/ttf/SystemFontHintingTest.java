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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.Test;

/**
 * Hinting checks against fonts that cannot be committed but are installed on many machines. Each test
 * is skipped when its font is missing, so these run locally (Windows, or Linux with the Microsoft core
 * fonts package) and not on CI.
 * <p>
 * Different releases of the same font carry different bytecode, so the exact expected points are
 * keyed by the file's {@code head.checkSumAdjustment}. For a release without an entry only the
 * version-independent checks run; to add one, dump FreeType 2.13.2's points with
 * {@code ft_points_dump.py} (or freetype-py with {@code FT_LOAD_NO_AUTOHINT | FT_LOAD_TARGET_NORMAL})
 * and add the file's checkSumAdjustment with its x and y arrays.
 */
class SystemFontHintingTest
{
    private static final String[] ARIAL = {
        "c:/windows/fonts/arial.ttf",
        "/usr/share/fonts/truetype/msttcorefonts/arial.ttf",
        "/usr/share/fonts/truetype/msttcorefonts/Arial.ttf",
        "/Library/Fonts/Arial.ttf",
        "/System/Library/Fonts/Supplemental/Arial.ttf" };

    /** FreeType 2.13.2 grid-fitted points of Arial 'n' (gid 81) at 13ppem, per font release. */
    private static final Map<Long, int[][]> ARIAL_N_13 = new HashMap<>();

    static
    {
        // Arial 2.82 (Microsoft core fonts for the web)
        ARIAL_N_13.put(0x7b86e499L, new int[][] {
            { 55, 55, 121, 121, 168, 258, 297, 362, 394, 401, 405, 405, 405, 332, 332, 332, 315, 272,
                242, 196, 128, 128, 128 },
            { 0, 448, 448, 387, 448, 448, 448, 420, 373, 342, 321, 269, 0, 0, 267, 312, 357, 384,
                384, 384, 324, 240, 0 } });
    }

    /**
     * Arial 'n' at 13ppem. In the Windows release of Arial its program runs FLIPRGON/FLIPRGOFF and
     * NPUSHW, which no font that can be committed exercises; in 2.82, FLIPRGON. Checks that the glyph
     * is hinted without failure, that its baseline and x-height land on whole pixels, and, for a
     * known release, that every point matches FreeType.
     */
    @Test
    void testArialN() throws IOException
    {
        File file = firstExisting(ARIAL);
        assumeTrue(file != null, "Arial not installed");
        try (TrueTypeFont font = new TTFParser().parse(new RandomAccessReadBufferedFile(file)))
        {
            int gid = 81;
            int ppem = 13;
            assertEquals(gid, font.getUnicodeCmapLookup().getGlyphId('n'), "gid 81 should be 'n'");

            GeneralPath hintedPath = font.getHintedPath(gid, ppem);
            assertNotNull(hintedPath, "'n' should be hinted at 13ppem");

            GlyphHinter hinter = new GlyphHinter(font);
            int[][] points = hinter.getHintedPointsF26Dot6(gid, ppem);
            assertNotNull(points);
            assertEquals(0, hinter.getFailureCount(), hinter.getFirstFailure());

            // the baseline and the x-height are grid-fitted to whole pixels
            int minY = Arrays.stream(points[1]).min().getAsInt();
            int maxY = Arrays.stream(points[1]).max().getAsInt();
            assertEquals(0, minY, "baseline");
            assertEquals(0, maxY % 64, "x-height " + maxY + " should be on a whole pixel");

            int[][] expected = ARIAL_N_13.get(font.getHeader().getCheckSumAdjustment());
            if (expected != null)
            {
                assertArrayEquals(expected[0], points[0], "x, " + file);
                assertArrayEquals(expected[1], points[1], "y, " + file);
            }
        }
    }

    private static File firstExisting(String... paths)
    {
        for (String path : paths)
        {
            File file = new File(path);
            if (file.isFile())
            {
                return file;
            }
        }
        return null;
    }
}
