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
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Test the "smcp" type 2 tables of a font.
 * 
 * @author Tilman Hausherr
 */
class GsubWorkerForSmcpTest
{
    
    @Test
    void testCalibri() throws IOException
    {
        File file = new File("c:/windows/fonts/calibri.ttf");
        Assumptions.assumeTrue(file.exists(), "calibri smcp test skipped");

        CmapLookup cmapLookup;
        GsubWorker gsubWorkerForSmcp;
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(file)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForSmcp = new GsubWorkerForSmcp(cmapLookup, ttf.getGsubData());
        }

        // Values should be the same you get by looking at the GSUB lookup list 24 with a font tool
        // This one converts "ﬀ" (single-ff-ligature glyph) into "FF" small capitals
        assertEquals(Arrays.asList(165, 165),
                gsubWorkerForSmcp.applyTransforms(getGlyphIds("\ufb00", cmapLookup)));
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