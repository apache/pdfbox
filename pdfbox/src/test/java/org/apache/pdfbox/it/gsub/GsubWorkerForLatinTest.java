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

package org.apache.pdfbox.it.gsub;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.gsub.GsubWorker;
import org.apache.fontbox.ttf.gsub.GsubWorkerFactory;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

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
    void testApplyLigatures() throws IOException
    {
        File file = new File("c:/windows/fonts/calibri.ttf");
        Assumptions.assumeTrue(file.exists(), "calibri ligature test skipped");
        try (PDDocument doc = new PDDocument())
        {
            PDType0Font font = PDType0Font.load(doc, file);

            CmapLookup cmapLookup = font.getCmapLookup();
            GsubWorker gsubWorkerForLatin = new GsubWorkerFactory().getGsubWorker(cmapLookup, font.getGsubData());

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