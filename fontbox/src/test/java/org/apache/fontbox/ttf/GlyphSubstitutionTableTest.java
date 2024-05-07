/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.Language;
import org.apache.fontbox.ttf.model.MapBackedScriptFeature;
import org.apache.fontbox.ttf.model.ScriptFeature;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Test;

class GlyphSubstitutionTableTest
{

    static final int DATA_POSITION_FOR_GSUB_TABLE = 120544;

    private static final Collection<String> EXPECTED_FEATURE_NAMES = Arrays.asList("abvs", "akhn",
            "blwf", "blws", "half", "haln", "init", "nukt", "pres", "pstf", "rphf", "vatu");

    @Test
    void testGetGsubData() throws IOException
    {
        // given
        RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(
                GSUBTableDebugger.class.getResourceAsStream("/ttf/Lohit-Bengali.ttf"));
        RandomAccessReadDataStream randomAccessReadBufferDataStream = new RandomAccessReadDataStream(
                randomAccessReadBuffer);
        randomAccessReadBufferDataStream.seek(DATA_POSITION_FOR_GSUB_TABLE);

        GlyphSubstitutionTable testClass = new GlyphSubstitutionTable();

        // when
        testClass.read(null, randomAccessReadBufferDataStream);

        // then
        GsubData gsubData = testClass.getGsubData();
        assertNotNull(gsubData);
        assertNotEquals(GsubData.NO_DATA_FOUND, gsubData);
        assertEquals(Language.BENGALI, gsubData.getLanguage());
        assertEquals("bng2", gsubData.getActiveScriptName());

        assertEquals(new HashSet<>(EXPECTED_FEATURE_NAMES), gsubData.getSupportedFeatures());

        String templatePathToFile = "/gsub/lohit_bengali/bng2/%s.txt";

        for (String featureName : EXPECTED_FEATURE_NAMES)
        {
            System.out.println("******* Testing feature: " + featureName);
            Map<List<Integer>, List<Integer>> expectedGsubTableRawData = getExpectedGsubTableRawData(
                    String.format(templatePathToFile, featureName));
            ScriptFeature scriptFeature = new MapBackedScriptFeature(featureName,
                    expectedGsubTableRawData);
            assertEquals(scriptFeature, gsubData.getFeature(featureName));
        }

    }

    private Map<List<Integer>, List<Integer>> getExpectedGsubTableRawData(String pathToResource)
            throws IOException
    {
        Map<List<Integer>, List<Integer>> gsubData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(
             new InputStreamReader(TestTTFParser.class.getResourceAsStream(pathToResource))))
        {
            while (true)
            {
                String line = br.readLine();

                if (line == null)
                {
                    break;
                }

                if (line.trim().length() == 0)
                {
                    continue;
                }

                if (line.startsWith("#"))
                {
                    continue;
                }
                String[] lineSplittedByKeyValue = line.split("=");

                if (lineSplittedByKeyValue.length != 2)
                {
                    throw new IllegalArgumentException("invalid format");
                }

                List<Integer> oldGlyphIds = new ArrayList<>();
                for (String value : lineSplittedByKeyValue[0].split(","))
                {
                    oldGlyphIds.add(Integer.valueOf(value));
                }

                Integer newGlyphId = Integer.valueOf(lineSplittedByKeyValue[1]);
                gsubData.put(oldGlyphIds, Collections.singletonList(newGlyphId));
            }
        }
        return gsubData;
    }

}
