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
 * Integration test for {@link GsubWorkerForDevanagari}. Has various combinations of glyphs to test
 * proper working of the GSUB system.
 * <p>
 * Not all tests are enabled at this time (12/2023). Please read the comment of 10/Dec/23 in
 * PDFBOX-5729.
 *
 * @author JAVAUSER
 *
 */
class GsubWorkerForDevanagariTest
{
    private static final String LOHIT_DEVANAGARI_TTF =
            "src/test/resources/ttf/Lohit-Devanagari.ttf";

    private CmapLookup cmapLookup;
    private GsubWorker gsubWorkerForDevanagari;

    @BeforeEach
    public void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(LOHIT_DEVANAGARI_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForDevanagari = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }

    @Test
    void testApplyTransforms_locl()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(642);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("प्त"));
        System.out.println("result: " + result);

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_nukt()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(400,396,393);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("य़ज़क़"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_akhn()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(520,521);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("क्षज्ञ"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_rphf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(513);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("र्"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_rkrf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(588,597,595,602);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("क्रब्रप्रह्र"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_blwf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(602,336,516);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("ह्रट्र"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_half()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(558,557,546,537);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("ह्स्भ्त्"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_vatu()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(517,593,601,665);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("श्रत्रस्रघ्र"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_cjct()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(638,688,636,640,639);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("द्मद्ध्र्यब्दद्वद्य"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_pres()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(603,605,617,652);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("शृक्तज्जह्ण"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_abvs()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(353,512,353,675,353,673);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("र्रैंरौंर्रो"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_blws()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(660,663,336,584,336,583);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("दृहृट्रूट्रु"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_psts()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(326,704,326,582,661,662);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("किंर्कींरुरू"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_haln()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(539);

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds("द्"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    void testApplyTransforms_calt()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList();

        // when
        List<Integer> result = gsubWorkerForDevanagari.applyTransforms(getGlyphIds(""));

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
