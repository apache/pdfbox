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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link GsubWorkerForBengali}. Has various combinations of glyphs to test
 * proper working of the GSUB system.
 *
 * @author Palash Ray
 *
 */
class GsubWorkerForBengaliTest
{
    private static final String LOHIT_BENGALI_TTF =
            "src/test/resources/ttf/Lohit-Bengali.ttf";

    private CmapLookup cmapLookup;
    private GsubWorker gsubWorkerForBengali;

    @BeforeEach
    public void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(LOHIT_BENGALI_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForBengali = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }

    @Test
    void testApplyTransforms_simple_hosshoi_kar()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(56, 102, 91);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("আমি"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_ja_phala()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(89, 156, 101, 97);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ব্যাস"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_e_kar()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(438, 89, 94, 101);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("বেলা"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_o_kar()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(108, 89, 101, 97);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("বোস"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    void testApplyTransforms_o_kar_repeated_1_not_working_yet()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(108, 96, 101, 108, 94, 101);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ষোলো"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    void testApplyTransforms_o_kar_repeated_2_not_working_yet()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(108, 73, 101, 108, 77, 101);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ছোটো"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_ou_kar()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(108, 91, 114, 94);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("মৌল"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_oi_kar()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(439, 89, 93);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("বৈর"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_kha_e_murddhana_swa_e_khiwa()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(167, 103, 438, 93, 93);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ক্ষীরের"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_ra_phala()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(274, 82);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("দ্রুত"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_ref()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(85, 104, 440, 82);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("ধুর্ত"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_ra_e_hosshu()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(352, 108, 87, 101);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("রুপো"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_la_e_la_e()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(67, 108, 369, 101, 94);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("কল্লোল"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_khanda_ta()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(98, 78, 101, 113);

        // when
        List<Integer> result = gsubWorkerForBengali.applyTransforms(getGlyphIds("হঠাৎ"));

        // then
        assertEquals(glyphsAfterGsub, result);
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
