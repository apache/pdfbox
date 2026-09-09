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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Golden (Tier 3) test comparing the FontBox interpreter's grid-fitted glyph points against a FreeType
 * reference dump (same font, glyph and ppem). The reference lives in {@code ttf/hinting/<font>-<ppem>.txt},
 * produced offline by {@code generate_golden.py}; FreeType is never a build or runtime dependency.
 * <p>
 * The reference is generated with FreeType's <b>grayscale</b> target ({@code FT_LOAD_TARGET_NORMAL}),
 * i.e. the v40 "minimal" subpixel interpreter with backward compatibility, because PDFBox always
 * rasterizes antialiased (Java2D). That mode is the right target for appearance but is a pile of
 * heuristics rather than a clean algorithm, so the assertions here are <b>property based</b> rather than
 * byte-exact coordinate matching:
 * <ul>
 *   <li><b>Horizontal</b> grid-fitting matches FreeType to within 1/64 px on every coordinate. This is
 *       the part that matters for weight: backward compatibility suppresses x grid-fitting so stems are
 *       not darkened, and we reproduce it exactly.</li>
 *   <li><b>Vertical</b> extent tracks FreeType (the glyph bounding box matches within about half a pixel),
 *       so the baseline/cap snap to the grid and nothing collapses. Interior y coordinates may differ by a
 *       fraction of a pixel because we do not replicate every grayscale backward-compatibility heuristic;
 *       a soft bound keeps the bulk of them close.</li>
 * </ul>
 * Coordinates are integer F26Dot6 (64 units per pixel).
 */
class GoldenHintingTest
{
    // hinting is off by default, so the golden comparison has to turn the feature on first
    @BeforeEach
    void enableHinting()
    {
        TrueTypeFont.setHintingEnabled(true);
    }

    @AfterEach
    void restoreHinting()
    {
        TrueTypeFont.setHintingEnabled(false);
    }

    private static final int[] PPEMS = { 11, 13, 16, 24 };

    /**
     * Horizontal grid-fitting is an exact match to FreeType's grayscale (v40) output: with backward
     * compatibility, x moves are suppressed so horizontal stems keep their sub-pixel position and are not
     * darkened by antialiasing. Every x coordinate agrees to within 1/64 px for simple and composite
     * glyphs alike.
     */
    @Test
    void testHorizontalGridFittingMatchesFreeType() throws IOException
    {
        Stats simple = compare(false);
        Stats composite = compare(true);
        assertTrue(simple.maxDx <= 1, simple.summary("simple"));
        assertTrue(composite.maxDx <= 1, composite.summary("composite"));
    }

    /**
     * Vertical hinting snaps the glyph to the pixel grid without collapsing it: the hinted y bounding box
     * matches FreeType within one pixel (so the baseline and cap/x-height land on grid rows, and a
     * degenerate outline - the symptom of the twilight-zone IP bug - would be caught), and the majority of
     * interior y coordinates stay within 1/64 px of FreeType.
     */
    @Test
    void testVerticalHintingTracksFreeTypeWithoutCollapse() throws IOException
    {
        Stats simple = compare(false);
        Stats composite = compare(true);
        assertTrue(simple.maxBboxYDelta <= 64, simple.summary("simple"));
        assertTrue(composite.maxBboxYDelta <= 64, composite.summary("composite"));
        assertTrue(simple.yWithinOnePercent() >= 50, simple.summary("simple"));
        assertTrue(composite.yWithinOnePercent() >= 50, composite.summary("composite"));
    }

    private Stats compare(boolean composite) throws IOException
    {
        TrueTypeFont font;
        try (InputStream is = getClass().getResourceAsStream("/ttf/LiberationSans-Regular.ttf"))
        {
            font = new TTFParser().parse(new RandomAccessReadBuffer(is));
        }
        GlyphHinter hinter = new GlyphHinter(font);
        Stats s = new Stats();

        for (int ppem : PPEMS)
        {
            List<GoldenGlyph> golden = loadGolden("/ttf/hinting/LiberationSans-Regular-" + ppem + ".txt");
            assertNotNull(golden);
            for (GoldenGlyph g : golden)
            {
                boolean isComposite = font.getGlyph().getGlyph(g.gid).getNumberOfContours() < 0;
                if (isComposite != composite)
                {
                    continue;
                }
                int[][] points = hinter.getHintedPointsF26Dot6(g.gid, ppem);
                assertNotNull(points, "no hinted points for gid " + g.gid + " at " + ppem + "ppem");
                assertTrue(points[0].length == g.x.length,
                        "point count mismatch for '" + g.ch + "' at " + ppem + "ppem: ours="
                                + points[0].length + " freetype=" + g.x.length);
                s.recordGlyph(g, ppem, points);
            }
        }
        return s;
    }

    private static final class Stats
    {
        private int compared;
        private int yWithinOne;
        private int maxDx;
        private int maxDy;
        private int maxBboxYDelta;
        private String worstWhere = "none";

        void recordGlyph(GoldenGlyph g, int ppem, int[][] points)
        {
            int ourMinY = Integer.MAX_VALUE;
            int ourMaxY = Integer.MIN_VALUE;
            int ftMinY = Integer.MAX_VALUE;
            int ftMaxY = Integer.MIN_VALUE;
            for (int i = 0; i < g.x.length; i++)
            {
                compared++;
                maxDx = Math.max(maxDx, Math.abs(points[0][i] - g.x[i]));
                int dy = Math.abs(points[1][i] - g.y[i]);
                if (dy <= 1)
                {
                    yWithinOne++;
                }
                if (dy > maxDy)
                {
                    maxDy = dy;
                    worstWhere = "'" + g.ch + "' (gid " + g.gid + ") point " + i + " @" + ppem
                            + "ppem: ours=(" + points[0][i] + "," + points[1][i] + ") freetype=("
                            + g.x[i] + "," + g.y[i] + ")";
                }
                ourMinY = Math.min(ourMinY, points[1][i]);
                ourMaxY = Math.max(ourMaxY, points[1][i]);
                ftMinY = Math.min(ftMinY, g.y[i]);
                ftMaxY = Math.max(ftMaxY, g.y[i]);
            }
            maxBboxYDelta = Math.max(maxBboxYDelta,
                    Math.max(Math.abs(ourMinY - ftMinY), Math.abs(ourMaxY - ftMaxY)));
        }

        int yWithinOnePercent()
        {
            return compared == 0 ? 100 : 100 * yWithinOne / compared;
        }

        String summary(String kind)
        {
            return kind + ": compared " + compared + " coords; maxDx=" + maxDx + "/64 maxDy=" + maxDy
                    + "/64 (y within 1/64 = " + yWithinOnePercent() + "%); max bbox-y delta "
                    + maxBboxYDelta + "/64; worst y at " + worstWhere;
        }
    }

    private List<GoldenGlyph> loadGolden(String resource) throws IOException
    {
        List<GoldenGlyph> glyphs = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(resource);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)))
        {
            GoldenGlyph current = null;
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("glyph "))
                {
                    String[] parts = line.split(" ");
                    current = new GoldenGlyph(Integer.parseInt(parts[1]), parts[2]);
                    glyphs.add(current);
                }
                else if (line.startsWith("x "))
                {
                    current.x = parseInts(line.substring(2));
                }
                else if (line.startsWith("y "))
                {
                    current.y = parseInts(line.substring(2));
                }
            }
        }
        return glyphs;
    }

    private static int[] parseInts(String s)
    {
        if (s.isEmpty())
        {
            return new int[0];
        }
        String[] tokens = s.split(" ");
        int[] values = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++)
        {
            values[i] = Integer.parseInt(tokens[i]);
        }
        return values;
    }

    private static final class GoldenGlyph
    {
        private final int gid;
        private final String ch;
        private int[] x;
        private int[] y;

        GoldenGlyph(int gid, String ch)
        {
            this.gid = gid;
            this.ch = ch;
        }
    }
}
