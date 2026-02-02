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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class GsubWorkerForLatinTest
{
    @Test
    void testApplyLigaturesCalibri() throws IOException
    {
        File file = new File("c:/windows/fonts/calibri.ttf");
        Assumptions.assumeTrue(file.exists(), "calibri ligature test skipped");

        CmapLookup cmapLookup;
        GsubWorker gsubWorkerForLatin;
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(file)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForLatin = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }

        assertEquals(Arrays.asList(286, 299, 286, 272, 415, 448, 286),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("effective", cmapLookup)));
        assertEquals(Arrays.asList(258, 427, 410, 437, 282, 286), 
                gsubWorkerForLatin.applyTransforms(getGlyphIds("attitude", cmapLookup)));
        assertEquals(Arrays.asList(258, 312, 367, 349, 258, 410, 286),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("affiliate", cmapLookup)));
        assertEquals(Arrays.asList(302, 367, 373),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("film", cmapLookup)));
        assertEquals(Arrays.asList(327, 381, 258, 410),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("float", cmapLookup)));
        assertEquals(Arrays.asList(393, 367, 258, 414, 381, 396, 373),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("platform", cmapLookup)));
    }

    @Test
    void testApplyLigaturesFoglihtenNo07() throws IOException
    {
        CmapLookup cmapLookup;
        GsubWorker gsubWorkerForLatin;
        try (TrueTypeFont ttf = new OTFParser().parse(
                new RandomAccessReadBufferedFile("src/test/resources/otf/FoglihtenNo07.otf")))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForLatin = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }

        assertEquals(Arrays.asList(66, 1590, 645, 70),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("affine", cmapLookup)));
        assertEquals(Arrays.asList(538, 633, 85, 86, 69, 70),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("attitude", cmapLookup)));
        assertEquals(Arrays.asList(66, 1590, 525, 74, 683),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("affiliate", cmapLookup)));
        assertEquals(Arrays.asList(542, 1, 1591, 498),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("The film", cmapLookup)));
        assertEquals(Arrays.asList(542, 1, 45, 703, 85),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("The Last", cmapLookup)));
        assertEquals(Arrays.asList(81, 77, 538, 71, 80, 83, 78),
                gsubWorkerForLatin.applyTransforms(getGlyphIds("platform", cmapLookup)));
    }

    private List<Integer> getGlyphIds(String word, CmapLookup cmapLookup)
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