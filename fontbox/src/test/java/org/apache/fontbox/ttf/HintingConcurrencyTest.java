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

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Hammers one shared {@link TrueTypeFont} from several threads. A system-substituted font is held in
 * a process-wide cache ({@code FontMapperImpl}), so this is how the renderer really uses it, and the
 * interpreter it drives is a pile of mutable state - the storage area, the twilight zone, the
 * post-{@code prep} template, the cached ppem - guarded only by {@link GlyphHinter}'s monitor.
 */
class HintingConcurrencyTest
{
    private static final int THREADS = 8;
    private static final int ITERATIONS = 150;
    private static final int[] PPEMS = { 11, 13, 16, 24 };
    private static final String GLYPHS = "HILEToxn";

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

    private static TrueTypeFont parse() throws IOException
    {
        try (InputStream is = HintingConcurrencyTest.class
                .getResourceAsStream("/ttf/LiberationSans-Regular.ttf"))
        {
            assertNotNull(is, "missing test font");
            return new TTFParser().parse(new RandomAccessReadBuffer(is));
        }
    }

    private static double[] flatten(GeneralPath path)
    {
        double[] coords = new double[6];
        List<Double> out = new ArrayList<>();
        for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next())
        {
            out.add((double) it.currentSegment(coords));
            for (double c : coords)
            {
                out.add(c);
            }
        }
        double[] array = new double[out.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = out.get(i);
        }
        return array;
    }

    /**
     * Every thread must get exactly what a single thread would have got. Interleaving the ppems is the
     * point: a ppem change re-runs the control value program and clears the storage area and twilight
     * zone, so an unsynchronized hinter would let one thread wipe the state another is mid-way through
     * using, and the results would drift rather than throw.
     */
    @Test
    void testConcurrentHintingMatchesSingleThreadedResults() throws Exception
    {
        TrueTypeFont reference = parse();
        int[] gids = new int[GLYPHS.length()];
        Map<Integer, double[]> expected = new HashMap<>();
        for (int g = 0; g < GLYPHS.length(); g++)
        {
            gids[g] = reference.getUnicodeCmapLookup().getGlyphId(GLYPHS.charAt(g));
            for (int ppem : PPEMS)
            {
                GeneralPath path = reference.getHintedPath(gids[g], ppem);
                assertNotNull(path, "expected '" + GLYPHS.charAt(g) + "' to hint at " + ppem + "ppem");
                expected.put(key(g, ppem), flatten(path));
            }
        }

        // a font nothing has hinted yet, so the run also races the lazy hinter creation
        TrueTypeFont shared = parse();
        Queue<String> problems = new ConcurrentLinkedQueue<>();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < THREADS; t++)
        {
            // each thread walks the glyph/ppem grid from a different offset, so the threads are
            // asking for different sizes at the same moment rather than moving in lockstep
            final int offset = t;
            futures.add(pool.submit(() ->
            {
                start.await();
                for (int i = 0; i < ITERATIONS; i++)
                {
                    int g = (i + offset) % GLYPHS.length();
                    int ppem = PPEMS[(i + offset) % PPEMS.length];
                    double[] actual = flatten(shared.getHintedPath(gids[g], ppem));
                    if (!Arrays.equals(expected.get(key(g, ppem)), actual))
                    {
                        problems.add("'" + GLYPHS.charAt(g) + "' at " + ppem + "ppem");
                    }
                }
                return null;
            }));
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(120, TimeUnit.SECONDS), "hinting threads did not finish");
        for (Future<?> future : futures)
        {
            future.get(); // surfaces anything thrown inside a worker
        }
        assertTrue(problems.isEmpty(),
                () -> problems.size() + " mismatched results, first: " + problems.peek());
    }

    private static int key(int glyphIndex, int ppem)
    {
        return glyphIndex * 1000 + ppem;
    }
}
