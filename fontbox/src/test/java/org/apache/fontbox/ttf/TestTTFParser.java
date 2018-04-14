/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * This will test the TTFParser implementation.
 *
 * @author Tim Allison
 */
public class TestTTFParser extends TestCase
{

    /**
     * Check whether the creation date is UTC
     *
     * @throws IOException If something went wrong
     */
    public void testUTCDate() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        // Before PDFBOX-2122, TTFDataStream was using the default TimeZone
        // Set the default to something not UTC and see if a UTC timeZone is returned
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los Angeles"));
        TTFParser parser = new TTFParser();
        TrueTypeFont ttf = parser.parse(testFile);
        Calendar created = ttf.getHeader().getCreated();
        assertEquals(created.getTimeZone(), utc);

        Calendar target = Calendar.getInstance(utc);
        target.set(2012, 9, 4, 11, 2, 31);
        target.set(Calendar.MILLISECOND, 0);
        assertEquals(target, created);
    }

    public void testGlyphSubstitutionTables() throws IOException
    {
        // given
        InputStream fontFileAsStream = TestTTFParser.class
                .getResourceAsStream("/ttf/Lohit-Bengali.ttf");
        TTFParser testClass = new TTFParser();

        // when
        TrueTypeFont ttf = testClass.parse(fontFileAsStream);

        // then
        assertNotNull(ttf);
        Map<Integer, List<Integer>> gsub = ttf.getGsub().getRawGSubData();
        assertEquals(getExpectedGsubTableRawData(), gsub);

        Map<String, Integer> glyphSubstitutionMap = ttf.getGlyphSubstitutionMap();
        Map<String, Integer> expected = getExpectedGlyphSubstitutionMap();
        assertEquals(expected.size(), glyphSubstitutionMap.size());
        assertEquals(expected, glyphSubstitutionMap);

    }

    private Map<String, Integer> getExpectedGlyphSubstitutionMap() throws IOException
    {
        String testResource = "/gsub/expected_glyph_substitution_map_jdk";

        String jdkVersion = System.getProperty("java.version");

        if (jdkVersion.startsWith("1.7"))
        {
            testResource += "7";
        }
        else
        {
            testResource += "8";
        }

        testResource += ".txt";

        System.out.println(
                "using the testResource: " + testResource + " for jdk version " + jdkVersion);

        Map<String, Integer> map = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(TestTTFParser.class
                .getResourceAsStream(testResource))))
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

                String[] unicodeChars = lineSplittedByKeyValue[0].split(",");

                StringBuilder unicodeTextBuilder = new StringBuilder();
                for (String unicodeChar : unicodeChars)
                {
                    unicodeTextBuilder.append((char) Integer.parseInt(unicodeChar));
                }

                Integer glyphId = Integer.valueOf(lineSplittedByKeyValue[1]);

                map.put(unicodeTextBuilder.toString(), glyphId);

            }
        }

        return map;
    }

    private Map<Integer, List<Integer>> getExpectedGsubTableRawData() throws IOException
    {
        Map<Integer, List<Integer>> gsubData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(TestTTFParser.class
                .getResourceAsStream("/gsub/correct_raw_gsub_table_data_lohit_bengali.txt")));)
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

                Integer key = Integer.valueOf(lineSplittedByKeyValue[0]);
                List<Integer> values = new ArrayList<>();
                for (String value : lineSplittedByKeyValue[1].split(","))
                {
                    values.add(Integer.valueOf(value));
                }

                gsubData.put(key, values);

            }
        }

        return gsubData;
    }

}
