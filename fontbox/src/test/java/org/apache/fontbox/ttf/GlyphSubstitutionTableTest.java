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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.Language;
import org.apache.fontbox.ttf.model.MapBackedScriptFeature;
import org.apache.fontbox.ttf.model.ScriptFeature;
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
        final MemoryTTFDataStream memoryTTFDataStream = new MemoryTTFDataStream(
                GlyphSubstitutionTableTest.class.getResourceAsStream("/ttf/Lohit-Bengali.ttf"));
        memoryTTFDataStream.seek(DATA_POSITION_FOR_GSUB_TABLE);

        final GlyphSubstitutionTable testClass = new GlyphSubstitutionTable(null);

        // when
        testClass.read(null, memoryTTFDataStream);

        // then
        final GsubData gsubData = testClass.getGsubData();
        assertNotNull(gsubData);
        assertNotEquals(GsubData.NO_DATA_FOUND, gsubData);
        assertEquals(Language.BENGALI, gsubData.getLanguage());
        assertEquals("bng2", gsubData.getActiveScriptName());

        assertEquals(new HashSet<>(EXPECTED_FEATURE_NAMES), gsubData.getSupportedFeatures());

        final String templatePathToFile = "/gsub/lohit_bengali/bng2/%s.txt";

        for (final String featureName : EXPECTED_FEATURE_NAMES)
        {
            System.out.println("******* Testing feature: " + featureName);
            final Map<List<Integer>, Integer> expectedGsubTableRawData = getExpectedGsubTableRawData(
                    String.format(templatePathToFile, featureName));
            final ScriptFeature scriptFeature = new MapBackedScriptFeature(featureName,
                    expectedGsubTableRawData);
            assertEquals(scriptFeature, gsubData.getFeature(featureName));
        }

    }

    private Map<List<Integer>, Integer> getExpectedGsubTableRawData(final String pathToResource)
            throws IOException
    {
        final Map<List<Integer>, Integer> gsubData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(TestTTFParser.class.getResourceAsStream(pathToResource))))
        {
            while (true)
            {
                final String line = br.readLine();

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
                final String[] lineSplittedByKeyValue = line.split("=");

                if (lineSplittedByKeyValue.length != 2)
                {
                    throw new IllegalArgumentException("invalid format");
                }

                final List<Integer> oldGlyphIds = new ArrayList<>();
                for (final String value : lineSplittedByKeyValue[0].split(","))
                {
                    oldGlyphIds.add(Integer.valueOf(value));
                }

                final Integer newGlyphId = Integer.valueOf(lineSplittedByKeyValue[1]);
                gsubData.put(oldGlyphIds, newGlyphId);
            }
        }
        return gsubData;
    }

}
