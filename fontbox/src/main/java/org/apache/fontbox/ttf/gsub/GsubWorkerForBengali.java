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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GsubWorkerForBengali implements GsubWorker
{

    private static final Log LOG = LogFactory.getLog(GsubWorkerForBengali.class);

    private static final String GLYPH_ID_SEPARATOR = "_";

    /**
     * This sequence is very important. This has been taken from <a href=
     * "https://docs.microsoft.com/en-us/typography/script-development/bengali">https://docs.microsoft.com/en-us/typography/script-development/bengali</a>
     */
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn",
            "rphf", "blwf", "half", "pstf", "vatu", "cjct", "init", "pres", "abvs", "blws", "psts",
            "haln", "calt");

    private final Map<String, Map<List<Integer>, Integer>> glyphSubstitutionMap;

    public GsubWorkerForBengali(Map<String, Map<List<Integer>, Integer>> glyphSubstitutionMap)
    {
        this.glyphSubstitutionMap = glyphSubstitutionMap;
    }

    @Override
    public List<Integer> substituteGlyphs(List<Integer> originalGlyphIds)
    {
        String originalGlyphsAsString = convertGlyphIdsToString(originalGlyphIds);

        LOG.debug("originalGlyphsAsString " + originalGlyphsAsString);

        String intermediateGlyphsFromGsub = originalGlyphsAsString;

        for (String feature : FEATURES_IN_ORDER)
        {
            if (!glyphSubstitutionMap.containsKey(feature))
            {
                LOG.debug("the feature " + feature + " was not found");
                continue;
            }

            LOG.debug("applying the feature " + feature);

            intermediateGlyphsFromGsub = applyGsubFeature(glyphSubstitutionMap.get(feature),
                    intermediateGlyphsFromGsub);
        }

        return convertGlyphIdsToList(intermediateGlyphsFromGsub);
    }

    private String applyGsubFeature(Map<List<Integer>, Integer> featureMap, String originalGlyphs)
    {
        Map<String, Integer> modifiedFeatureMap = new HashMap<>();

        for (List<Integer> glyphs : featureMap.keySet())
        {
            modifiedFeatureMap.put(convertGlyphIdsToString(glyphs), featureMap.get(glyphs));
        }

        LOG.debug("modifiedFeatureMap: " + modifiedFeatureMap);

        List<String> tokens = new CompoundCharacterTokenizer(modifiedFeatureMap.keySet())
                .tokenize(originalGlyphs);

        StringBuilder gsubProcessedGlyphs = new StringBuilder(100);

        for (String chunk : tokens)
        {
            if (modifiedFeatureMap.containsKey(chunk))
            {
                // gsub system kicks in, you get the glyphId directly
                int glyphId = modifiedFeatureMap.get(chunk);
                gsubProcessedGlyphs.append(glyphId).append(GLYPH_ID_SEPARATOR);
            }
            else
            {
                gsubProcessedGlyphs.append(chunk);
            }
        }

        LOG.debug("originalGlyphs: " + originalGlyphs + ", gsubProcessedGlyphs: "
                + gsubProcessedGlyphs);

        return convertGlyphIdsToString(convertGlyphIdsToList(gsubProcessedGlyphs.toString()));
    }

    private String convertGlyphIdsToString(List<Integer> glyphIds)
    {
        StringBuilder sb = new StringBuilder(20);
        for (Integer glyphId : glyphIds)
        {
            sb.append(glyphId).append(GLYPH_ID_SEPARATOR);
        }
        sb.setLength(sb.length() - GLYPH_ID_SEPARATOR.length());
        return sb.toString();
    }

    private List<Integer> convertGlyphIdsToList(String glyphIdsAsString)
    {
        List<Integer> gsubProcessedGlyphsIds = new ArrayList<>();

        for (String glyphId : glyphIdsAsString.split(GLYPH_ID_SEPARATOR))
        {
            if (glyphId.trim().length() == 0)
            {
                continue;
            }
            gsubProcessedGlyphsIds.add(Integer.valueOf(glyphId));
        }

        return gsubProcessedGlyphsIds;
    }

}
