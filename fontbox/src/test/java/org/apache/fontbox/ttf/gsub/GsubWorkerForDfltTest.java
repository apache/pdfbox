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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link GsubWorkerForDflt}. Tests DFLT (default) script GSUB worker.
 *
 * <p>The DFLT script is used for script-neutral typographic features that work across
 * writing systems, particularly when text lacks a specific script (symbols, punctuation)
 * or when no script-specific table exists.</p>
 *
 * <p>Note: RobotoMono-Regular.ttf uses DFLT script but only supports the 'smcp' (small capitals)
 * feature, which is not processed by GsubWorkerForDflt. These tests verify that the worker
 * correctly handles DFLT fonts without ligature features, ensuring glyphs pass through unchanged.</p>
 *
 * <p>JosefinSans-Italic.ttf (SIL Open Font License) uses DFLT script and has standard ligatures
 * (fi, fl) which are used for testing actual GSUB transformations with ligature substitutions.</p>
 *
 * @author Palash Ray
 */
class GsubWorkerForDfltTest
{
    private static final String ROBOTO_MONO_TTF =
            "src/test/resources/ttf/RobotoMono-Regular.ttf";
    private static final String JOSEFIN_SANS_TTF =
            "src/test/resources/ttf/JosefinSans-Italic.ttf";

    private CmapLookup cmapLookup;
    private GsubWorker gsubWorkerForDflt;

    @BeforeEach
    void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(ROBOTO_MONO_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForDflt = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }

    /**
     * Test that text without ligature substitutions passes through unchanged.
     * RobotoMono doesn't have 'liga', 'ccmp', 'clig', or 'calt' features.
     */
    @Test
    void testApplyTransforms_noLigatures_fi()
    {
        // given - "fi" which would be a ligature in fonts that support it
        List<Integer> glyphsExpected = Arrays.asList(72, 75);

        // when
        List<Integer> result = gsubWorkerForDflt.applyTransforms(getGlyphIds("fi"));

        // then - should pass through unchanged
        assertEquals(glyphsExpected, result);
    }

    /**
     * Test that text passes through correctly for common text.
     */
    @Test
    void testApplyTransforms_basicText()
    {
        // given
        List<Integer> glyphsExpected = Arrays.asList(72, 81, 80, 86);

        // when
        List<Integer> result = gsubWorkerForDflt.applyTransforms(getGlyphIds("font"));

        // then
        assertEquals(glyphsExpected, result);
    }

    /**
     * Test that programming symbols pass through unchanged.
     */
    @Test
    void testApplyTransforms_programmingSymbols()
    {
        // given - "!=" which might be a ligature in programming fonts
        List<Integer> glyphsExpected = Arrays.asList(1040, 31);

        // when
        List<Integer> result = gsubWorkerForDflt.applyTransforms(getGlyphIds("!="));

        // then
        assertEquals(glyphsExpected, result);
    }

    /**
     * Test multiple character text transformation.
     */
    @Test
    void testApplyTransforms_multipleChars()
    {
        // given
        List<Integer> glyphsExpected = Arrays.asList(69, 81, 70, 71);

        // when
        List<Integer> result = gsubWorkerForDflt.applyTransforms(getGlyphIds("code"));

        // then
        assertEquals(glyphsExpected, result);
    }

    /**
     * Test that applyTransforms returns immutable list.
     */
    @Test
    void testApplyTransforms_immutableResult()
    {
        // given
        List<Integer> input = getGlyphIds("abc");

        // when
        List<Integer> result = gsubWorkerForDflt.applyTransforms(input);

        // then
        try
        {
            result.add(999);
            throw new AssertionError("Expected UnsupportedOperationException");
        }
        catch (UnsupportedOperationException e)
        {
            // Expected - the list should be immutable
        }

        try
        {
            result.remove(0);
            throw new AssertionError("Expected UnsupportedOperationException");
        }
        catch (UnsupportedOperationException e)
        {
            // Expected - the list should be immutable
        }
    }

    /**
     * Test worker type verification.
     */
    @Test
    void testCorrectWorkerType()
    {
        // then
        assertInstanceOf(GsubWorkerForDflt.class, gsubWorkerForDflt);
    }

    /**
     * Test ligature substitution with JosefinSans-Italic font (fi -> ligature).
     * JosefinSans-Italic uses DFLT script with standard ligatures.
     */
    @Test
    void testApplyTransforms_josefinSansItalic_fi() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(JOSEFIN_SANS_TTF)))
        {
            CmapLookup josefinCmap = ttf.getUnicodeCmapLookup();
            GsubWorker josefinWorker = new GsubWorkerFactory().getGsubWorker(josefinCmap, ttf.getGsubData());

            // given - "fi" which has a standard ligature in this font
            List<Integer> glyphsExpected = Arrays.asList(407);

            // when
            List<Integer> input = new ArrayList<>();
            for (char c : "fi".toCharArray())
            {
                input.add(josefinCmap.getGlyphId(c));
            }
            List<Integer> result = josefinWorker.applyTransforms(input);

            // then - should transform to ligature glyph
            assertEquals(glyphsExpected, result);
        }
    }

    /**
     * Test ligature substitution with JosefinSans-Italic font (fl -> ligature).
     */
    @Test
    void testApplyTransforms_josefinSansItalic_fl() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(JOSEFIN_SANS_TTF)))
        {
            CmapLookup josefinCmap = ttf.getUnicodeCmapLookup();
            GsubWorker josefinWorker = new GsubWorkerFactory().getGsubWorker(josefinCmap, ttf.getGsubData());

            // given - "fl" which has a standard ligature in this font
            List<Integer> glyphsExpected = Arrays.asList(408);

            // when
            List<Integer> input = new ArrayList<>();
            for (char c : "fl".toCharArray())
            {
                input.add(josefinCmap.getGlyphId(c));
            }
            List<Integer> result = josefinWorker.applyTransforms(input);

            // then - should transform to ligature glyph
            assertEquals(glyphsExpected, result);
        }
    }

    /**
     * Test ligature substitution within word with JosefinSans-Italic font.
     */
    @Test
    void testApplyTransforms_josefinSansItalic_office() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(JOSEFIN_SANS_TTF)))
        {
            CmapLookup josefinCmap = ttf.getUnicodeCmapLookup();
            GsubWorker josefinWorker = new GsubWorkerFactory().getGsubWorker(josefinCmap, ttf.getGsubData());

            // given - "office" which contains "fi" ligature
            List<Integer> glyphsExpected = Arrays.asList(293, 257, 407, 229, 237);

            // when
            List<Integer> input = new ArrayList<>();
            for (char c : "office".toCharArray())
            {
                input.add(josefinCmap.getGlyphId(c));
            }
            List<Integer> result = josefinWorker.applyTransforms(input);

            // then - "ffi" should become "f" + "fi ligature", other chars unchanged
            assertEquals(glyphsExpected, result);
        }
    }

    /**
     * Test ligature processing in multi-f sequences with JosefinSans-Italic font.
     * "ffl" should become f + fl-ligature.
     */
    @Test
    void testApplyTransforms_josefinSansItalic_ffl() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(JOSEFIN_SANS_TTF)))
        {
            CmapLookup josefinCmap = ttf.getUnicodeCmapLookup();
            GsubWorker josefinWorker = new GsubWorkerFactory().getGsubWorker(josefinCmap, ttf.getGsubData());

            // given - "ffl" which should become f + fl-ligature
            // JosefinSans-Italic has fi and fl ligatures, but not a dedicated ffl ligature
            List<Integer> glyphsExpected = Arrays.asList(257, 408);

            // when
            List<Integer> input = new ArrayList<>();
            for (char c : "ffl".toCharArray())
            {
                input.add(josefinCmap.getGlyphId(c));
            }
            List<Integer> result = josefinWorker.applyTransforms(input);

            // then - should transform fl to ligature, leaving first f unchanged
            assertEquals(glyphsExpected, result);
        }
    }

    /**
     * Test ligature processing in "ffij" sequence with JosefinSans-Italic font.
     * "ffij" should become f + fi-ligature + j.
     */
    @Test
    void testApplyTransforms_josefinSansItalic_ffij() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(JOSEFIN_SANS_TTF)))
        {
            CmapLookup josefinCmap = ttf.getUnicodeCmapLookup();
            GsubWorker josefinWorker = new GsubWorkerFactory().getGsubWorker(josefinCmap, ttf.getGsubData());

            // given - "ffij" which should become f + fi-ligature + j
            List<Integer> glyphsExpected = Arrays.asList(257, 407, 279);

            // when
            List<Integer> input = new ArrayList<>();
            for (char c : "ffij".toCharArray())
            {
                input.add(josefinCmap.getGlyphId(c));
            }
            List<Integer> result = josefinWorker.applyTransforms(input);

            // then - should transform fi to ligature
            assertEquals(glyphsExpected, result);
        }
    }

    private List<Integer> getGlyphIds(String word)
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
