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

import java.util.*;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.ScriptFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Devanagari-specific implementation of GSUB system
 *
 * @author JAVAUSER
 * @author Harish
 */
public class GsubWorkerForDevanagariNepali implements GsubWorker {
    private static final Logger LOG = LogManager.getLogger(GsubWorkerForDevanagariNepali.class);

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
    private static final char[] BEFORE_REPH_CHARS = {'ा','ि' ,'ी', 'ो', 'ौ', 'े', 'ै'};

    // Devanagari vowel sign I
    private static final char BEFORE_HALF_CHAR = 'ि';

    private final CmapLookup cmapLookup;
    private final GsubData gsubData;

    private final List<Integer> rephGlyphIds;
    private final List<Integer> beforeRephGlyphIds;
    private final List<Integer> beforeHalfGlyphIds;
    private final List<Integer> noHalfCharacterGlyphIds;


    private static final List<Character> NO_HALF_CONSONANTS = Arrays.asList(
            'ङ',
            'ट',
            'ठ',
            'ड',
            'ढ',
            'द');

    GsubWorkerForDevanagariNepali(CmapLookup cmapLookup, GsubData gsubData) {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        noHalfCharacterGlyphIds = getNoHalfConsonants();
        beforeHalfGlyphIds = getBeforeHalfGlyphIds();
        rephGlyphIds = getRephGlyphIds();
        beforeRephGlyphIds = getbeforeRephGlyphIds();
    }

    @Override
    public List<Integer> applyTransforms(List<Integer> originalGlyphIds) {
        // *** reph position is adjusted
        // *** TODO
        // *** reph positioning is simply based on 1st find the र् sequence but it affects the formation of half form of rakaar
        // *** so the reph feature should be applied for the र् sequence at the start of the syllable otherwise the र् will form rakaar with the preceeding half consonant

        List<Integer> intermediateGlyphsFromGsub = adjustRephPosition(originalGlyphIds);
        intermediateGlyphsFromGsub = repositionGlyphs(intermediateGlyphsFromGsub);
        // *** ि position is adjusted
        for (String feature : FEATURES_IN_ORDER) {
            // *** all the features may not be supported by the particular script/font, the information is available in GSubData
            if (!gsubData.isFeatureSupported(feature)) {
                if (feature.equals(RKRF_FEATURE) && gsubData.isFeatureSupported(VATU_FEATURE)) {
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

    // *** applying rakaar
    private List<Integer> applyRKRFFeature(ScriptFeature rkrfGlyphsForSubstitution,
                                           List<Integer> originalGlyphIds) {
        Set<List<Integer>> rkrfGlyphIds = rkrfGlyphsForSubstitution.getAllGlyphIdsForSubstitution();
        if (rkrfGlyphIds.isEmpty()) {
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
            if (firstList.size() > 1) {
                rkrfReplacement = firstList.get(1);
                break;
            }
        }

        if (rkrfReplacement == 0) {
            LOG.debug("Cannot find rkrf candidate. The rkrfGlyphIds doesn't contain lists of two elements.");
            return originalGlyphIds;
        }

        List<Integer> rkrfList = new ArrayList<>(originalGlyphIds);
        for (int index = originalGlyphIds.size() - 1; index > 1; index--) {
            int raGlyph = originalGlyphIds.get(index);
            if (raGlyph == rephGlyphIds.get(0)) {
                int viramaGlyph = originalGlyphIds.get(index - 1);
                if (viramaGlyph == rephGlyphIds.get(1)) {
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
    // *** DONE for र्थ्यो
    // *** Resolve the issue of half form of rakaar forming reph forms: forming र्क्क instead क्र्क

    private List<Integer> adjustRephPosition(List<Integer> originalGlyphIds) {
        List<Integer> rephAdjustedList = new ArrayList<>(originalGlyphIds);
        for (int index = 0; index < originalGlyphIds.size() - 2; index++) {
            int raGlyph = originalGlyphIds.get(index);
            int viramaGlyph = originalGlyphIds.get(index + 1);
            // *** found the reph in originalGlyphIds jump to the next glyph if available
            // *** TODO
            // *** after finding the reph position check if it is at the starting of the syllable otherwise skip repositioning
            if (raGlyph == rephGlyphIds.get(0) && viramaGlyph == rephGlyphIds.get(1)) {
//                if(!(index>0 && originalGlyphIds.get(index-1)==rephGlyphIds.get(1))){
//                    continue;
//                }
                int nextIndex = index + 2;

                // *** for multiple half consonants after the reph
                while ((nextIndex + 1) < originalGlyphIds.size() && originalGlyphIds.get(nextIndex + 1) == viramaGlyph) {
                    nextIndex = nextIndex + 2;
                }
                // *** remove the reph from the original position
                for (int i = 0; i < 2; i++) {
                    rephAdjustedList.remove(index);// र
                }// ्
                // *** place the reph in the current found position
                rephAdjustedList.add(nextIndex - 1, raGlyph);
                rephAdjustedList.add(nextIndex, viramaGlyph);

                if (nextIndex + 1 < originalGlyphIds.size()) {
                    int matraGlyph = originalGlyphIds.get(nextIndex + 1);
                    if (beforeRephGlyphIds.contains(matraGlyph)) {
                        rephAdjustedList.set(nextIndex - 1, matraGlyph);
                        rephAdjustedList.set(nextIndex, raGlyph);
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
    // *** TODO
    // *** for ड्कि ङ्कि
    private List<Integer> repositionGlyphs(List<Integer> originalGlyphIds) {
        List<Integer> repositionedGlyphIds = new ArrayList<>(originalGlyphIds);
        int listSize = repositionedGlyphIds.size();
        int foundIndex = listSize - 1;
        int nextIndex = listSize - 2;
        while (nextIndex > -1) {
            int glyph = repositionedGlyphIds.get(foundIndex);
            int prevIndex = foundIndex + 1;
            if (beforeHalfGlyphIds.contains(glyph)) {
                // *** the ि is brought in front of the base character
                repositionedGlyphIds.remove(foundIndex);
                repositionedGlyphIds.add(nextIndex--, glyph);
            } else if (rephGlyphIds.get(1).equals(glyph) && prevIndex < listSize) {
                // *** if the current character is ् and it is not the last character
                // *** we check if the character next to ् is ि
                int prevGlyph = repositionedGlyphIds.get(prevIndex);

                // *** let's skip the swap for consonants that does not have half forms
                if (beforeHalfGlyphIds.contains(prevGlyph)) {

                    int nextGlyph = repositionedGlyphIds.get(nextIndex);
                    if (!noHalfCharacterGlyphIds.contains(nextGlyph)) {
                        repositionedGlyphIds.remove(prevIndex);
                        repositionedGlyphIds.add(nextIndex--, prevGlyph);
                    } else if(nextIndex>0 && Objects.equals(repositionedGlyphIds.get(nextIndex - 1), rephGlyphIds.get(1))){
                        repositionedGlyphIds.remove(prevIndex);
                        repositionedGlyphIds.add(nextIndex--, prevGlyph);
                    }
                }
            }
            foundIndex = nextIndex--;
        }
        return repositionedGlyphIds;
    }

    // ** we need the gsub feature specific implementation so, the exceptional behaviors can be handled
    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature, List<Integer> originalGlyphs) {
        // *** this is only the keyset for particular script feature in the substitution table
        Set<List<Integer>> allGlyphIdsForSubstitution = scriptFeature.getAllGlyphIdsForSubstitution();
        if (allGlyphIdsForSubstitution.isEmpty()) {
            LOG.debug("getAllGlyphIdsForSubstitution() for {} is empty", scriptFeature.getName());
            return originalGlyphs;
        }

        // *** here we prepare the regexExpression inside CompoundCharacterTokenizer where: Set<List<Integer>> ---> Set<String> ----> String or Regex Pattern
        // *** this happens for each script feature
        GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(
                allGlyphIdsForSubstitution);

        // *** this is the complex part where the pattern searching is happening
        List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);

        List<Integer> gsubProcessedGlyphs = new ArrayList<>(tokens.size());

//        tokens.forEach(chunk ->
//        {
//            // *** if there is substitution for the chunk: group of glyphs in input obtained after splitting
//            // *** the tokens contains the chunks that match the patterns from the gsub table and also the ones that are not present in the gsub table
//            if (scriptFeature.canReplaceGlyphs(chunk))
//            {
//                // *** search in the map obtained from gsub tables
//                // *** if it is replacable(i.e. found in the gsub table, then we can replace with the different glyph)
//                List<Integer> replacementForGlyphs = scriptFeature.getReplacementForGlyphs(chunk);
//
//                // *** we add up all the replacement for the glyphs in the original sequence to get the final glyph sequence
//                gsubProcessedGlyphs.addAll(replacementForGlyphs);
//            }
//            else
//            {
//                gsubProcessedGlyphs.addAll(chunk);
//            }
//        });

        for (int chunkIndex = 0; chunkIndex < tokens.size(); chunkIndex++) {
            List<Integer> chunk = tokens.get(chunkIndex);

            boolean isHalfFeature = Objects.equals(scriptFeature.getName(), "half");
            if (isHalfFeature) {
//                 *** check for last chunk: like छन् ---> [छ] [न‌ ्]
                boolean isLastChunk = (chunkIndex == tokens.size() - 1);
                if (!isLastChunk && scriptFeature.canReplaceGlyphs(chunk)) {
                    List<Integer> replacementForGlyphs = scriptFeature.getReplacementForGlyphs(chunk);
                    gsubProcessedGlyphs.addAll(replacementForGlyphs);
                } else {
                    gsubProcessedGlyphs.addAll(chunk);
                }
            } else {
                if (scriptFeature.canReplaceGlyphs(chunk)) {
                    // *** search in the map obtained from gsub tables
                    // *** if it is replacable(i.e. found in the gsub table, then we can replace with the different glyph)
                    List<Integer> replacementForGlyphs = scriptFeature.getReplacementForGlyphs(chunk);

                    // *** we add up all the replacement for the glyphs in the original sequence to get the final glyph sequence
                    gsubProcessedGlyphs.addAll(replacementForGlyphs);
                } else {
                    gsubProcessedGlyphs.addAll(chunk);
                }
            }
        }
        LOG.debug("originalGlyphs: {}, gsubProcessedGlyphs: {}", originalGlyphs, gsubProcessedGlyphs);
        return gsubProcessedGlyphs;
    }


    private List<Integer> getBeforeHalfGlyphIds() {
        return List.of(getGlyphId(BEFORE_HALF_CHAR));
    }

    private List<Integer> getNoHalfConsonants() {
        List<Integer> glyphIds = new ArrayList<>();
        for (char character : NO_HALF_CONSONANTS) {
            glyphIds.add(getGlyphId(character));
        }
        return Collections.unmodifiableList(glyphIds);
    }

    private List<Integer> getRephGlyphIds() {
        List<Integer> result = new ArrayList<>();
        for (char character : REPH_CHARS) {
            result.add(getGlyphId(character));
        }
        return Collections.unmodifiableList(result);
    }

    private List<Integer> getbeforeRephGlyphIds() {
        List<Integer> glyphIds = new ArrayList<>();
        for (char character : BEFORE_REPH_CHARS) {
            glyphIds.add(getGlyphId(character));
        }
        return Collections.unmodifiableList(glyphIds);
    }

    private Integer getGlyphId(char character) {
        return cmapLookup.getGlyphId(character);
    }
}
