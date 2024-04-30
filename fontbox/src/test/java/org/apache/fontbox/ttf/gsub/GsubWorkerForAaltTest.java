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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Test the "aalt" type 3 tables of a font.
 * 
 * @author Tilman Hausherr
 */
class GsubWorkerForAaltTest
{
    
    @Test
    void testFoglihtenNo07() throws IOException
    {
        CmapLookup cmapLookup;
        GsubWorker gsubWorkerForAlt;
        try (TrueTypeFont ttf = new OTFParser().parse(
                new RandomAccessReadBufferedFile("src/test/resources/otf/FoglihtenNo07.otf")))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForAlt = new GsubWorkerForAalt(cmapLookup, ttf.getGsubData());
        }

        // Values should be the same you get by looking at the GSUB lookup lists 12 or 13 with 
        // a font tool
        assertEquals(Arrays.asList(1139, 1562, 1477),
                gsubWorkerForAlt.applyTransforms(getGlyphIds("Abc", cmapLookup)));
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