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
import java.util.Set;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.ScriptFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Gujarati-specific implementation of GSUB system.
 * 
 * @author JAVAUSER
 *
 */
public class GsubWorkerForGujarati implements GsubWorker
{
    private static final Logger LOG = LogManager.getLogger(GsubWorkerForGujarati.class);
    
    private static final String RKRF_FEATURE = "rkrf";
    private static final String VATU_FEATURE = "vatu";
    
    /**
     * This sequence is very important. This has been taken from <a href=
     * "https://docs.microsoft.com/en-us/typography/script-development/gujarati">https://docs.microsoft.com/en-us/typography/script-development/gujarati</a>
     */
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn",
            "rphf", RKRF_FEATURE,"blwf", "half", VATU_FEATURE, "cjct", "pres", "abvs", "blws",
            "psts", "haln", "calt");
    
    // Reph glyphs
    private static final char[] REPH_CHARS = {'\u0AB0', '\u0ACD'};
    // Glyphs to precede reph
    private static final char[] BEFORE_REPH_CHARS={'\u0ABE','\u0AC0'};
    
    // Gujarati vowel sign I
    private static final char BEFORE_HALF_CHAR = '\u0ABF';

    private final CmapLookup cmapLookup;
    private final GsubData gsubData;
    
    private final List<Integer> rephGlyphIds;
    private final List<Integer> beforeRephGlyphIds;
    private final List<Integer> beforeHalfGlyphIds;

    GsubWorkerForGujarati(CmapLookup cmapLookup, GsubData gsubData)
    {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        beforeHalfGlyphIds = getBeforeHalfGlyphIds();
        rephGlyphIds = getRephGlyphIds();
        beforeRephGlyphIds=getbeforeRephGlyphIds();
    }

    @Override
    public List<Integer> applyTransforms(List<Integer> originalGlyphIds)
    {
        List<Integer> intermediateGlyphsFromGsub = adjustRephPosition(originalGlyphIds);
        intermediateGlyphsFromGsub = repositionGlyphs(intermediateGlyphsFromGsub);
        for (String feature : FEATURES_IN_ORDER)
        {
            if (!gsubData.isFeatureSupported(feature))
            {
                if (feature.equals(RKRF_FEATURE) && gsubData.isFeatureSupported(VATU_FEATURE))
                {
                    // Create your own rkrf feature from vatu feature
                    intermediateGlyphsFromGsub = applyRKRFFeature(
                            gsubData.getFeature(VATU_FEATURE),
                            intermediateGlyphsFromGsub);
                }
                LOG.debug("the feature {} was not found", feature);
                continue;
            }
            LOG.debug("applying the feature {}", feature);
            ScriptFeature scriptFeature = gsubData.getFeature(feature);
            intermediateGlyphsFromGsub = applyGsubFeature(scriptFeature,
                    intermediateGlyphsFromGsub);
        }
        return Collections.unmodifiableList(intermediateGlyphsFromGsub);
    }

    private List<Integer> applyRKRFFeature(ScriptFeature rkrfGlyphsForSubstitution,
            List<Integer> originalGlyphIds)
    {
        Set<List<Integer>> rkrfGlyphIds = rkrfGlyphsForSubstitution.getAllGlyphIdsForSubstitution();
        if (rkrfGlyphIds.isEmpty())
        {
            LOG.debug("Glyph substitution list for {} is empty.", rkrfGlyphsForSubstitution.getName());
            return originalGlyphIds;
        }

        // Replace this with better implementation to get second GlyphId from rkrfGlyphIds
        int rkrfReplacement = 0;
        for (List<Integer> firstList : rkrfGlyphIds)
        {
            if (firstList.size() > 1)
            {
                rkrfReplacement = firstList.get(1);
                break;
            }
        }
        if (rkrfReplacement == 0)
        {
            LOG.debug("Cannot find rkrf candidate. The rkrfGlyphIds doesn't contain lists of two elements.");
            return originalGlyphIds;
        }

        List<Integer> rkrfList = new ArrayList<>(originalGlyphIds);
        for (int index = originalGlyphIds.size() - 1; index > 1; index--)
        {
            int raGlyph = originalGlyphIds.get(index);
            if (raGlyph == rephGlyphIds.get(0))
            {
                int viramaGlyph = originalGlyphIds.get(index - 1);
                if (viramaGlyph == rephGlyphIds.get(1))
                {
                    rkrfList.set(index - 1, rkrfReplacement);
                    rkrfList.remove(index);
                }
            }
        }
        return rkrfList;
    }

    private List<Integer> repositionGlyphs(List<Integer> originalGlyphIds)
    {
        List<Integer> repositionedGlyphIds = new ArrayList<>(originalGlyphIds);
        int listSize = repositionedGlyphIds.size();
        int foundIndex = listSize - 1;
        int nextIndex = listSize - 2;
        while (nextIndex > -1)
        {
            int glyph = repositionedGlyphIds.get(foundIndex);
            int prevIndex = foundIndex + 1;
            if (beforeHalfGlyphIds.contains(glyph))
            {
                repositionedGlyphIds.remove(foundIndex);
                repositionedGlyphIds.add(nextIndex--, glyph);
            }
            else if (rephGlyphIds.get(1).equals(glyph) && prevIndex < listSize)
            {
                int prevGlyph = repositionedGlyphIds.get(prevIndex);
                if (beforeHalfGlyphIds.contains(prevGlyph))
                {
                    repositionedGlyphIds.remove(prevIndex);
                    repositionedGlyphIds.add(nextIndex--, prevGlyph);
                }
            }
            foundIndex = nextIndex--;
        }
        return repositionedGlyphIds;
    }

    private List<Integer> adjustRephPosition(List<Integer> originalGlyphIds)
    {
        List<Integer> rephAdjustedList = new ArrayList<>(originalGlyphIds);
        for (int index = 0; index < originalGlyphIds.size() - 2; index++)
        {
            int raGlyph = originalGlyphIds.get(index);
            int viramaGlyph = originalGlyphIds.get(index + 1);
            if (raGlyph == rephGlyphIds.get(0) && viramaGlyph == rephGlyphIds.get(1))
            {
                int nextConsonantGlyph = originalGlyphIds.get(index + 2);
                rephAdjustedList.set(index, nextConsonantGlyph);
                rephAdjustedList.set(index + 1, raGlyph);
                rephAdjustedList.set(index + 2, viramaGlyph);

                if (index + 3 < originalGlyphIds.size())
                {
                    int matraGlyph = originalGlyphIds.get(index + 3);
                    if (beforeRephGlyphIds.contains(matraGlyph))
                    {
                        rephAdjustedList.set(index + 1, matraGlyph);
                        rephAdjustedList.set(index + 2, raGlyph);
                        rephAdjustedList.set(index + 3, viramaGlyph);
                    }
                }
            }
        }
        return rephAdjustedList;
    }

    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature, List<Integer> originalGlyphs)
    {
        Set<List<Integer>> allGlyphIdsForSubstitution = scriptFeature.getAllGlyphIdsForSubstitution();
        if (allGlyphIdsForSubstitution.isEmpty())
        {
            LOG.debug("getAllGlyphIdsForSubstitution() for {} is empty", scriptFeature.getName());
            return originalGlyphs;
        }
        GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(
                allGlyphIdsForSubstitution);
        List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);
        List<Integer> gsubProcessedGlyphs = new ArrayList<>(tokens.size());
        tokens.forEach(chunk ->
        {
            if (scriptFeature.canReplaceGlyphs(chunk))
            {
                List<Integer> replacementForGlyphs = scriptFeature.getReplacementForGlyphs(chunk);
                gsubProcessedGlyphs.addAll(replacementForGlyphs);
            }
            else
            {
                gsubProcessedGlyphs.addAll(chunk);
            }
        });
        LOG.debug("originalGlyphs: {}, gsubProcessedGlyphs: {}", originalGlyphs, gsubProcessedGlyphs);
        return gsubProcessedGlyphs;
    }

    private List<Integer> getBeforeHalfGlyphIds()
    {
        return List.of(getGlyphId(BEFORE_HALF_CHAR));
    }

    private List<Integer> getRephGlyphIds()
    {
        List<Integer> result = new ArrayList<>();
        for (char character : REPH_CHARS)
        {
            result.add(getGlyphId(character));
        }
        return Collections.unmodifiableList(result);
    }

    private List<Integer> getbeforeRephGlyphIds()
    {
        List<Integer> glyphIds = new ArrayList<>();
        for (char character : BEFORE_REPH_CHARS)
        {
            glyphIds.add(getGlyphId(character));
        }
        return Collections.unmodifiableList(glyphIds);
    }

    private Integer getGlyphId(char character)
    {
        return cmapLookup.getGlyphId(character);
    }
}
