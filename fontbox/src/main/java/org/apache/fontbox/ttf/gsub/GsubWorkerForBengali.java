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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.ScriptFeature;

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

    private static final String INIT_FEATURE = "init";

    /**
     * This sequence is very important. This has been taken from <a href=
     * "https://docs.microsoft.com/en-us/typography/script-development/bengali">https://docs.microsoft.com/en-us/typography/script-development/bengali</a>
     */
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn",
            "rphf", "blwf", "pstf", "half", "vatu", "cjct", INIT_FEATURE, "pres", "abvs", "blws",
            "psts", "haln", "calt");

    private static final char[] BEFORE_HALF_CHARS = new char[] { '\u09BF', '\u09C7', '\u09C8' };
    private static final BeforeAndAfterSpanComponent[] BEFORE_AND_AFTER_SPAN_CHARS = new BeforeAndAfterSpanComponent[] {
            new BeforeAndAfterSpanComponent('\u09CB', '\u09C7', '\u09BE'),
            new BeforeAndAfterSpanComponent('\u09CC', '\u09C7', '\u09D7') };

    private final CmapLookup cmapLookup;
    private final GsubData gsubData;

    private final List<Integer> beforeHalfGlyphIds;
    private final Map<Integer, BeforeAndAfterSpanComponent> beforeAndAfterSpanGlyphIds;


    GsubWorkerForBengali(final CmapLookup cmapLookup, final GsubData gsubData)
    {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        beforeHalfGlyphIds = getBeforeHalfGlyphIds();
        beforeAndAfterSpanGlyphIds = getBeforeAndAfterSpanGlyphIds();
    }

    @Override
    public List<Integer> applyTransforms(final List<Integer> originalGlyphIds)
    {
        List<Integer> intermediateGlyphsFromGsub = originalGlyphIds;

        for (final String feature : FEATURES_IN_ORDER)
        {
            if (!gsubData.isFeatureSupported(feature))
            {
                LOG.debug("the feature " + feature + " was not found");
                continue;
            }

            LOG.debug("applying the feature " + feature);

            final ScriptFeature scriptFeature = gsubData.getFeature(feature);

            intermediateGlyphsFromGsub = applyGsubFeature(scriptFeature,
                    intermediateGlyphsFromGsub);
        }

        return Collections.unmodifiableList(repositionGlyphs(intermediateGlyphsFromGsub));
    }

    private List<Integer> repositionGlyphs(final List<Integer> originalGlyphIds)
    {
        final List<Integer> glyphsRepositionedByBeforeHalf = repositionBeforeHalfGlyphIds(
                originalGlyphIds);
        return repositionBeforeAndAfterSpanGlyphIds(glyphsRepositionedByBeforeHalf);
    }

    private List<Integer> repositionBeforeHalfGlyphIds(final List<Integer> originalGlyphIds)
    {
        final List<Integer> repositionedGlyphIds = new ArrayList<>(originalGlyphIds);

        for (int index = 1; index < originalGlyphIds.size(); index++)
        {
            final int glyphId = originalGlyphIds.get(index);
            if (beforeHalfGlyphIds.contains(glyphId))
            {
                final int previousGlyphId = originalGlyphIds.get(index - 1);
                repositionedGlyphIds.set(index, previousGlyphId);
                repositionedGlyphIds.set(index - 1, glyphId);
            }
        }
        return repositionedGlyphIds;
    }

    private List<Integer> repositionBeforeAndAfterSpanGlyphIds(final List<Integer> originalGlyphIds)
    {
        final List<Integer> repositionedGlyphIds = new ArrayList<>(originalGlyphIds);

        for (int index = 1; index < originalGlyphIds.size(); index++)
        {
            final int glyphId = originalGlyphIds.get(index);
            if (beforeAndAfterSpanGlyphIds.containsKey(glyphId))
            {
                final BeforeAndAfterSpanComponent beforeAndAfterSpanComponent = beforeAndAfterSpanGlyphIds
                        .get(glyphId);
                final int previousGlyphId = originalGlyphIds.get(index - 1);
                repositionedGlyphIds.set(index, previousGlyphId);
                repositionedGlyphIds.set(index - 1,
                        getGlyphId(beforeAndAfterSpanComponent.beforeComponentCharacter));
                repositionedGlyphIds.add(index + 1,
                        getGlyphId(beforeAndAfterSpanComponent.afterComponentCharacter));
            }
        }
        return repositionedGlyphIds;
    }

    private List<Integer> applyGsubFeature(final ScriptFeature scriptFeature,
                                           final List<Integer> originalGlyphs)
    {

        final GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(
                scriptFeature.getAllGlyphIdsForSubstitution());

        final List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);

        final List<Integer> gsubProcessedGlyphs = new ArrayList<>();

        for (final List<Integer> chunk : tokens)
        {
            if (scriptFeature.canReplaceGlyphs(chunk))
            {
                // gsub system kicks in, you get the glyphId directly
                final int glyphId = scriptFeature.getReplacementForGlyphs(chunk);
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

    private List<Integer> getBeforeHalfGlyphIds()
    {
        final List<Integer> glyphIds = new ArrayList<>();

        for (final char character : BEFORE_HALF_CHARS)
        {
            glyphIds.add(getGlyphId(character));
        }

        if (gsubData.isFeatureSupported(INIT_FEATURE))
        {
            final ScriptFeature feature = gsubData.getFeature(INIT_FEATURE);
            for (final List<Integer> glyphCluster : feature.getAllGlyphIdsForSubstitution())
            {
                glyphIds.add(feature.getReplacementForGlyphs(glyphCluster));
            }
        }

        return Collections.unmodifiableList(glyphIds);

    }

    private Integer getGlyphId(final char character)
    {
        return cmapLookup.getGlyphId(character);
    }

    private Map<Integer, BeforeAndAfterSpanComponent> getBeforeAndAfterSpanGlyphIds()
    {
        final Map<Integer, BeforeAndAfterSpanComponent> result = new HashMap<>();

        for (final BeforeAndAfterSpanComponent beforeAndAfterSpanComponent : BEFORE_AND_AFTER_SPAN_CHARS)
        {
            result.put(
                    getGlyphId(beforeAndAfterSpanComponent.originalCharacter),
                    beforeAndAfterSpanComponent);
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Models characters like O-kar (\u09CB) and OU-kar (\u09CC). Since these 2 characters is
     * represented by 2 components, one before and one after the Vyanjan Varna on which this is
     * used, this glyph has to be replaced by these 2 glyphs. For O-kar, it has to be replaced by
     * E-kar (\u09C7) and AA-kar (\u09BE). For OU-kar, it has be replaced by E-kar (\u09C7) and
     * \u09D7.
     *
     */
    private static class BeforeAndAfterSpanComponent {
        private final char originalCharacter;
        private final char beforeComponentCharacter;
        private final char afterComponentCharacter;

        BeforeAndAfterSpanComponent(final char originalCharacter, final char beforeComponentCharacter,
                                    final char afterComponentCharacter)
        {
            this.originalCharacter = originalCharacter;
            this.beforeComponentCharacter = beforeComponentCharacter;
            this.afterComponentCharacter = afterComponentCharacter;
        }

    }

}
