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
 * Integration test for {@link GsubWorkerForGujarati}. Has various combinations of glyphs to test
 * proper working of the GSUB system.
 *
 * @author JAVAUSER
 *
 */
class GsubWorkerForGujaratiTest
{

    private static final String LOHIT_GUJARATI_TTF =
            "src/test/resources/ttf/Lohit-Gujarati.ttf";

    private CmapLookup cmapLookup;
    private GsubWorker gsubWorkerForGujarati;

    @BeforeEach
    public void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(LOHIT_GUJARATI_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForGujarati = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }

    @Test
    void testApplyTransforms_akhn()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(330,331,304, 251);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("ક્ષજ્ઞત્તશ્ર"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_rphf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(98,335);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("ર્સ"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_rkrf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(242,228,250);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("પ્રક્રવ્ર"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_blwf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(76,332);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("ટ્ર"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_half()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(205,195,206);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("ત્ચ્થ્"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_vatu()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(237,245,233);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("ત્રભ્રજ્ર"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_cjct()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(309,312,305);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("દ્ધદ્નદ્ય"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_pres()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(284,294,314,315);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("ગ્નટ્ટપ્તલ્લ"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_abvs()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(92,255,92,258,91,102,336);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("રેંરૈંર્યાં"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_blws()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(278,76,333,337,276);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("હૃટ્રુણુરુ"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_psts()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(280, 273, 92, 261);

        // when
        List<Integer> result = gsubWorkerForGujarati.applyTransforms(getGlyphIds("જીઈંરીં"));

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
