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
 * Devanagari-specific implementation of GSUB system
 * 
 * @author JAVAUSER
 *
 */
public class GsubWorkerForDevanagari implements GsubWorker
{
    private static final Logger LOG = LogManager.getLogger(GsubWorkerForDevanagari.class);
    
    private static final String RKRF_FEATURE = "rkrf";
    private static final String VATU_FEATURE = "vatu";
    
    /**
     * This sequence is very important. This has been taken from <a href=
     * "https://docs.microsoft.com/en-us/typography/script-development/devanagari">https://docs.microsoft.com/en-us/typography/script-development/devanagari</a>
     */
    private static final List<String> FEATURES_IN_ORDER =
            Arrays.asList(
                    "locl",
                    "nukt",
                    "akhn",
                    "rphf",
                    RKRF_FEATURE,
                    "blwf",
                    "half",
                    VATU_FEATURE,
                    "cjct",
                    "pres",
                    "abvs",
                    "blws",
                    "psts",
                    "haln",
                    "calt");

    // Reph glyphs
    private static final char[] REPH_CHARS = {'र', '्'};
    // Glyphs to precede reph
    // *** TODO
    // *** This may need correction, other dependent vowels should be added
    // *** [DONE] added other BEFORE_REPH_CHARS
    private static final char[] BEFORE_REPH_CHARS={'ा','ी','ो'};

    // Devanagari vowel sign I
    private static final char BEFORE_HALF_CHAR = 'ि';

    private final CmapLookup cmapLookup;
    private final GsubData gsubData;
    
    private final List<Integer> rephGlyphIds;
    private final List<Integer> beforeRephGlyphIds;
    private final List<Integer> beforeHalfGlyphIds;

    GsubWorkerForDevanagari(CmapLookup cmapLookup, GsubData gsubData)
    {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        beforeHalfGlyphIds = getBeforeHalfGlyphIds();
        rephGlyphIds = getRephGlyphIds();
        beforeRephGlyphIds = getbeforeRephGlyphIds();
    }

    @Override
    public List<Integer> applyTransforms(List<Integer> originalGlyphIds)
    {
        List<Integer> intermediateGlyphsFromGsub = adjustRephPosition(originalGlyphIds);
        // *** reph position is adjusted
        intermediateGlyphsFromGsub = repositionGlyphs(intermediateGlyphsFromGsub);
        // *** ि position is adjusted
        for (String feature : FEATURES_IN_ORDER)
        {
            // *** all the features may not be supported by the particular script/font, the information is available in GSubData
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

    // *** applying rakar
    private List<Integer> applyRKRFFeature(ScriptFeature rkrfGlyphsForSubstitution,
            List<Integer> originalGlyphIds)
    {
        Set<List<Integer>> rkrfGlyphIds = rkrfGlyphsForSubstitution.getAllGlyphIdsForSubstitution();
        if (rkrfGlyphIds.isEmpty())
        {
            // *** no substitution is available for rkrf feature
            LOG.debug("Glyph substitution list for {} is empty.", rkrfGlyphsForSubstitution.getName());
            return originalGlyphIds;
        }
        // Replace this with better implementation to get second GlyphId from rkrfGlyphIds
        int rkrfReplacement = 0;
        for (List<Integer> firstList : rkrfGlyphIds)
        // *** TOD0
        // *** Look for the features in the rkrf table
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
                    // *** found an ् + र form which takes the rkrf
                    // *** the replacement is available as script feature
                    rkrfList.set(index - 1, rkrfReplacement);
                    rkrfList.remove(index);
                }
            }
        }
        return rkrfList;
    }

    // *** TODO
    // *** This function requires improvement
    // *** It works for र्यो but doesn't work for र्थ्यो or र्न्थ्यो
    private List<Integer> adjustRephPosition(List<Integer> originalGlyphIds)
    {
        List<Integer> rephAdjustedList = new ArrayList<>(originalGlyphIds);
        for (int index = 0; index < originalGlyphIds.size() - 2; index++)
        {
            int raGlyph = originalGlyphIds.get(index);
            int viramaGlyph = originalGlyphIds.get(index + 1);
            if (raGlyph == rephGlyphIds.get(0) && viramaGlyph == rephGlyphIds.get(1) )
            {
//                int nextConsonantGlyph = originalGlyphIds.get(index + 2);
//                rephAdjustedList.set(index, nextConsonantGlyph);
//                rephAdjustedList.set(index + 1, raGlyph);
//                rephAdjustedList.set(index + 2, viramaGlyph);
                int nextIndex = index +2;

                while((nextIndex+1)<originalGlyphIds.size() && originalGlyphIds.get(nextIndex+1)==viramaGlyph){
                    nextIndex=nextIndex+2;
                }
                rephAdjustedList.remove(index);// र
                rephAdjustedList.remove(index);// ्
                rephAdjustedList.add(nextIndex-1,raGlyph);
                rephAdjustedList.add(nextIndex,viramaGlyph);

                if (nextIndex + 1 < originalGlyphIds.size())
                {
                    int matraGlyph = originalGlyphIds.get(nextIndex + 1);
                    if (beforeRephGlyphIds.contains(matraGlyph))
                    {
                        rephAdjustedList.set(nextIndex -1, matraGlyph);
                        rephAdjustedList.set(nextIndex , raGlyph);
                        rephAdjustedList.set(nextIndex + 1, viramaGlyph);
                    }
                }
            }
        }
        return rephAdjustedList;
    }

    // *** ि as beforeHalfGlyph
    // *** TODO
    // *** does it handle the situation where there are multiple half consonants before a consonant followed by ि
    // *** DONE : works perfectly for न्थ्यि but not for र्न्थ्यि
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
                // *** the ि is brought in front of the base character
                repositionedGlyphIds.remove(foundIndex);
                repositionedGlyphIds.add(nextIndex--, glyph);
            }
            else if (rephGlyphIds.get(1).equals(glyph) && prevIndex < listSize)
            {
                // *** if the current character is ् and it is not the last character
                // *** we check if the character next to ् is ि
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

    // ** we need the gsub feature specific implementation so, the exceptional behaviors can be handled
    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature, List<Integer> originalGlyphs)
    {
        // *** this is only the keyset for particular script feature in the substitution table
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
            // *** if there is substitution for the chunk: group of glyphs in input obtained after splitting
            if (scriptFeature.canReplaceGlyphs(chunk))
            {
                // ** search in the map obtained from gsub tables
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
