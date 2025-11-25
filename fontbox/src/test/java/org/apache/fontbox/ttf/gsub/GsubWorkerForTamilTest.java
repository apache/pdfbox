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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link GsubWorkerForTamil}. Has various combinations of glyphs to test
 * proper working of the GSUB system.
 */
class GsubWorkerForTamilTest
{
    private static final String LOHIT_TAMIL_TTF = "src/test/resources/ttf/Lohit-Tamil.ttf";

    private CmapLookup cmapLookup;
    private GsubWorker gsubWorkerForTamil;

    @BeforeEach
    void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(LOHIT_TAMIL_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubWorkerForTamil = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }
    
    @Test
    void testDummy()
    {
        System.out.println("GSUB worker: " + gsubWorkerForTamil);
        assertTrue(gsubWorkerForTamil instanceof DefaultGsubWorker); // change to GsubWorkerForTamil when implemented
    }


}
