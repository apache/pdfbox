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
public abstract class GsubWorkerBase implements GsubWorker
{

    private static final Log LOG = LogFactory.getLog(GsubWorkerBase.class);

    protected static final String INIT_FEATURE = "init";

    private final CmapLookup cmapLookup;
    private final GsubData gsubData;

    private final List<Integer> beforeHalfGlyphIds;
    private final Map<Integer, BeforeAndAfterSpanComponent> beforeAndAfterSpanGlyphIds;


    GsubWorkerBase(CmapLookup cmapLookup, GsubData gsubData)
    {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        beforeHalfGlyphIds = getBeforeHalfGlyphIds();
        beforeAndAfterSpanGlyphIds = getBeforeAndAfterSpanGlyphIds();
    }
    
    public abstract char[] getBeforeHalfChars(); 
    
    public abstract List<String> getFeaturesInOrder();
    
    public abstract BeforeAndAfterSpanComponent[] getBeforeAfterSpanChars();
    
    @Override
    public List<Integer> applyTransforms(List<Integer> originalGlyphIds)
    {
        List<Integer> intermediateGlyphsFromGsub = originalGlyphIds;

        for (String feature : getFeaturesInOrder())
        {
            if (!gsubData.isFeatureSupported(feature))
            {
                LOG.debug("the feature " + feature + " was not found");
                continue;
            }

            LOG.debug("applying the feature " + feature);

            ScriptFeature scriptFeature = gsubData.getFeature(feature);

            intermediateGlyphsFromGsub = applyGsubFeature(scriptFeature,
                    intermediateGlyphsFromGsub);
        }

        return Collections.unmodifiableList(repositionGlyphs(intermediateGlyphsFromGsub));
    }

    private List<Integer> repositionGlyphs(List<Integer> originalGlyphIds)
    {
        List<Integer> glyphsRepositionedByBeforeHalf = repositionBeforeHalfGlyphIds(
                originalGlyphIds);
        return repositionBeforeAndAfterSpanGlyphIds(glyphsRepositionedByBeforeHalf);
    }

    private List<Integer> repositionBeforeHalfGlyphIds(List<Integer> originalGlyphIds)
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

    private List<Integer> repositionBeforeAndAfterSpanGlyphIds(List<Integer> originalGlyphIds)
    {
        List<Integer> repositionedGlyphIds = new ArrayList<>(originalGlyphIds);

        for (int index = 1; index < originalGlyphIds.size(); index++)
        {
            int glyphId = originalGlyphIds.get(index);
            if (beforeAndAfterSpanGlyphIds.containsKey(glyphId))
            {
                BeforeAndAfterSpanComponent beforeAndAfterSpanComponent = beforeAndAfterSpanGlyphIds
                        .get(glyphId);
                int previousGlyphId = originalGlyphIds.get(index - 1);
                repositionedGlyphIds.set(index, previousGlyphId);
                repositionedGlyphIds.set(index - 1,
                        getGlyphId(beforeAndAfterSpanComponent.beforeComponentCharacter));
                repositionedGlyphIds.add(index + 1,
                        getGlyphId(beforeAndAfterSpanComponent.afterComponentCharacter));
            }
        }
        return repositionedGlyphIds;
    }

    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature,
            List<Integer> originalGlyphs)
    {

        GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(
                scriptFeature.getAllGlyphIdsForSubstitution());

        List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);

        List<Integer> gsubProcessedGlyphs = new ArrayList<>();

        for (List<Integer> chunk : tokens)
        {
            if (scriptFeature.canReplaceGlyphs(chunk))
            {
                // gsub system kicks in, you get the glyphId directly
                int glyphId = scriptFeature.getReplacementForGlyphs(chunk);
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
        List<Integer> glyphIds = new ArrayList<>();

        for (char character : getBeforeHalfChars())
        {
            glyphIds.add(getGlyphId(character));
        }

        if (gsubData.isFeatureSupported(INIT_FEATURE))
        {
            ScriptFeature feature = gsubData.getFeature(INIT_FEATURE);
            for (List<Integer> glyphCluster : feature.getAllGlyphIdsForSubstitution())
            {
                glyphIds.add(feature.getReplacementForGlyphs(glyphCluster));
            }
        }

        return Collections.unmodifiableList(glyphIds);

    }

    private Integer getGlyphId(char character)
    {
        return cmapLookup.getGlyphId(character);
    }

    private Map<Integer, BeforeAndAfterSpanComponent> getBeforeAndAfterSpanGlyphIds()
    {
        Map<Integer, BeforeAndAfterSpanComponent> result = new HashMap<>();

        for (BeforeAndAfterSpanComponent beforeAndAfterSpanComponent : getBeforeAfterSpanChars())
        {
            result.put(
                    getGlyphId(beforeAndAfterSpanComponent.originalCharacter),
                    beforeAndAfterSpanComponent);
        }

        return Collections.unmodifiableMap(result);
    }
}
