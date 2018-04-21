package org.apache.fontbox.ttf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
import java.util.Set;

import org.junit.Test;

public class GlyphSubstitutionTableTest
{

    private static final int DATA_POSITION_FOR_GSUB_TABLE = 120544;

    private static final Collection<String> EXPECTED_FEATURE_NAMES = Arrays.asList("abvs", "akhn",
            "blwf", "blws", "half", "haln", "init", "nukt", "pres", "pstf", "rphf", "vatu");

    @Test
    public void testGetRawGSubData() throws IOException
    {
        // given
        MemoryTTFDataStream memoryTTFDataStream = new MemoryTTFDataStream(
                GlyphSubstitutionTableTest.class.getResourceAsStream("/ttf/Lohit-Bengali.ttf"));
        memoryTTFDataStream.seek(DATA_POSITION_FOR_GSUB_TABLE);

        GlyphSubstitutionTable testClass = new GlyphSubstitutionTable(null);

        // when
        testClass.read(null, memoryTTFDataStream);

        // then
        Map<String, Map<List<Integer>, Integer>> rawGsubData = testClass.getRawGSubData();
        assertNotNull(rawGsubData);
        assertFalse(rawGsubData.isEmpty());

        Set<String> featureNames = rawGsubData.keySet();
        assertEquals(new HashSet<>(EXPECTED_FEATURE_NAMES), featureNames);

        String templatePathToFile = "/gsub/lohit_bengali/bng2/%s.txt";

        for (String featureName : EXPECTED_FEATURE_NAMES)
        {
            System.out.println("******* Testing feature: " + featureName);
            Map<List<Integer>, Integer> expectedGsubTableRawData = getExpectedGsubTableRawData(
                    String.format(templatePathToFile, featureName));
            assertEquals(expectedGsubTableRawData, rawGsubData.get(featureName));
        }

    }

    private Map<List<Integer>, Integer> getExpectedGsubTableRawData(String pathToResource)
            throws IOException
    {
        Map<List<Integer>, Integer> gsubData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(TestTTFParser.class.getResourceAsStream(pathToResource)));)
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

                gsubData.put(oldGlyphIds, newGlyphId);

            }
        }

        return gsubData;
    }

}
