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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.CmapLookup;

/**
 * 
 * Bengali-specific implementation of GSUB system
 * 
 * @author Palash Ray
 *
 */
public class GsubWorkerForBengali implements GsubWorker
{

    private static final Log LOG = LogFactory.getLog(GsubWorkerForBengali.class);

    /**
     * This sequence is very important. This has been taken from <a href=
     * "https://docs.microsoft.com/en-us/typography/script-development/bengali">https://docs.microsoft.com/en-us/typography/script-development/bengali</a>
     */
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn",
            "rphf", "blwf", "half", "pstf", "vatu", "cjct", "init", "pres", "abvs", "blws", "psts",
            "haln", "calt");

    private static final char[] BEFORE_HALF_CHARS = new char[] { '\u09BF', '\u09C7', '\u09C8' };

    private final Map<String, Map<List<Integer>, Integer>> glyphSubstitutionMap;

    private final List<Integer> beforeHalfGlyphIds;

    public GsubWorkerForBengali(CmapLookup cmapLookup,
            Map<String, Map<List<Integer>, Integer>> glyphSubstitutionMap)
    {
        this.glyphSubstitutionMap = glyphSubstitutionMap;
        beforeHalfGlyphIds = getBeforeHalfGlyphIds(cmapLookup);
    }

    @Override
    public List<Integer> substituteGlyphs(List<Integer> originalGlyphIds)
    {
        List<Integer> intermediateGlyphsFromGsub = originalGlyphIds;

        for (String feature : FEATURES_IN_ORDER)
        {
            if (!glyphSubstitutionMap.containsKey(feature))
            {
                LOG.debug("the feature " + feature + " was not found");
                continue;
            }

            LOG.debug("applying the feature " + feature);

            Map<List<Integer>, Integer> featureMap = glyphSubstitutionMap.get(feature);

            intermediateGlyphsFromGsub = applyGsubFeature(featureMap, intermediateGlyphsFromGsub);
        }

        return intermediateGlyphsFromGsub;
    }

    @Override
    public List<Integer> repositionGlyphs(List<Integer> originalGlyphIds)
    {
        List<Integer> repositionedGlyphIds = new ArrayList<>(originalGlyphIds);

        for (int index = 1; index < originalGlyphIds.size(); index++)
        {
            int glyphId = originalGlyphIds.get(index);
            if (beforeHalfGlyphIds.contains(glyphId))
            {
                int previousGlyphId = originalGlyphIds.get(index - 1);
                repositionedGlyphIds.set(index, previousGlyphId);
                repositionedGlyphIds.set(index - 1, glyphId);
            }
        }
        return repositionedGlyphIds;
    }

    private List<Integer> applyGsubFeature(Map<List<Integer>, Integer> featureMap,
            List<Integer> originalGlyphs)
    {

        GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(
                featureMap.keySet());

        List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);

        List<Integer> gsubProcessedGlyphs = new ArrayList<>();

        for (List<Integer> chunk : tokens)
        {
            if (featureMap.containsKey(chunk))
            {
                // gsub system kicks in, you get the glyphId directly
                int glyphId = featureMap.get(chunk);
                gsubProcessedGlyphs.add(glyphId);
            }
            else
            {
                gsubProcessedGlyphs.addAll(chunk);
            }
        }

        LOG.debug("originalGlyphs: " + originalGlyphs + ", gsubProcessedGlyphs: "
                + gsubProcessedGlyphs);

        return gsubProcessedGlyphs;
    }

    private static List<Integer> getBeforeHalfGlyphIds(CmapLookup cmapLookup)
    {
        List<Integer> beforeHalfGlyphIds = new ArrayList<>();

        for (char beforeHalfChar : BEFORE_HALF_CHARS)
        {
            beforeHalfGlyphIds.add(cmapLookup.getGlyphId(beforeHalfChar));
        }

        return Collections.unmodifiableList(beforeHalfGlyphIds);

    }

}
