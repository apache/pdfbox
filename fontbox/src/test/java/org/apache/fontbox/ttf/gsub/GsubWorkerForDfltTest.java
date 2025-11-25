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

package org.apache.fontbox.ttf.gsub;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Integration test for {@link GsubWorkerForDflt}. Tests DFLT (default) script GSUB worker.
 *
 * <p>The DFLT script is used for script-neutral typographic features that work across
 * writing systems, particularly when text lacks a specific script (symbols, punctuation)
 * or when no script-specific table exists.</p>
 *
 * <p>JosefinSans-Italic.ttf (SIL Open Font License) uses DFLT script and has standard ligatures
 * (fi, fl) which are used for testing GSUB transformations. Words without ligature sequences
 * (like "font" or "code") pass through unchanged, while words containing "fi" or "fl" are
 * transformed to use ligature glyphs.</p>
 *
 */
class GsubWorkerForDfltTest
{
    private static final String JOSEFIN_SANS_TTF = "src/test/resources/ttf/JosefinSans-Italic.ttf";

    private static CmapLookup cmapLookup;
    private static GsubWorker gsubWorkerForDflt;

    @BeforeAll
    static void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(JOSEFIN_SANS_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForDflt = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }

    @Test
    void testCorrectWorkerType()
    {
        assertInstanceOf(GsubWorkerForDflt.class, gsubWorkerForDflt);
    }

    static Stream<Arguments> provideTransformTestCases()
    {
        return Stream.of(
                // No ligature - text passes through unchanged
                Arguments.of("code", Arrays.asList(229, 293, 235, 237), "no ligature sequences"),
                // Simple ligature
                Arguments.of("fi", Collections.singletonList(407), "fi -> ligature"),
                // Ligature within word
                Arguments.of("office", Arrays.asList(293, 257, 407, 229, 237), "ffi -> f + fi-ligature"),
                // Multi-f sequence
                Arguments.of("ffl", Arrays.asList(257, 408), "ffl -> f + fl-ligature")
        );
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("provideTransformTestCases")
    void testApplyTransforms(String input, List<Integer> expectedGlyphs, String description)
    {
        List<Integer> result = gsubWorkerForDflt.applyTransforms(getGlyphIds(input));
        assertEquals(expectedGlyphs, result);
    }

    @Test
    void testApplyTransforms_immutableResult()
    {
        List<Integer> result = gsubWorkerForDflt.applyTransforms(getGlyphIds("abc"));

        assertThrows(UnsupportedOperationException.class, () -> result.add(999));
        assertThrows(UnsupportedOperationException.class, () -> result.remove(0));
    }

    private static List<Integer> getGlyphIds(String word)
    {
        List<Integer> originalGlyphIds = new ArrayList<>();

        for (char unicodeChar : word.toCharArray())
        {
            int glyphId = cmapLookup.getGlyphId(unicodeChar);
            assertTrue(glyphId > 0);
            originalGlyphIds.add(glyphId);
        }

        return originalGlyphIds;
    }
}