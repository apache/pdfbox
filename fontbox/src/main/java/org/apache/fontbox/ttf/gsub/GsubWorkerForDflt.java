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
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.ScriptFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DFLT (Default) script-specific implementation of GSUB system.
 *
 * <p>According to the OpenType specification, a Script table with the script tag 'DFLT' (default)
 * is used in fonts to define features that are not script-specific. Applications should use the
 * DFLT script table when no script table exists for the specific script of the text being
 * processed, or when text lacks a defined script (containing only symbols or punctuation).</p>
 *
 * <p>This implementation applies common, script-neutral typographic features that work across
 * writing systems. The feature order follows standard OpenType recommendations for universal
 * glyph substitutions.</p>
 *
 * <p>Reference:
 * <a href="https://learn.microsoft.com/en-us/typography/opentype/spec/chapter2#scriptlist-table">
 * OpenType ScriptList Table Specification</a></p>
 */
public class GsubWorkerForDflt implements GsubWorker
{
    private static final Logger LOG = LogManager.getLogger(GsubWorkerForDflt.class);

    /**
     * Script-neutral features in recommended processing order.
     *
     * <ul>
     * <li>ccmp - Glyph Composition/Decomposition (must be first)</li>
     * <li>liga - Standard Ligatures</li>
     * <li>clig - Contextual Ligatures</li>
     * <li>calt - Contextual Alternates</li>
     * </ul>
     *
     * Note: This feature list focuses on common GSUB (substitution) features.
     * GPOS features like 'kern', 'mark', 'mkmk' are handled separately.
     */
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("ccmp", "liga", "clig", "calt");

    private final GsubData gsubData;

    private Map<String,GlyphArraySplitter> map = new WeakHashMap<>();

    GsubWorkerForDflt(GsubData gsubData)
    {
        this.gsubData = gsubData;
    }

    @Override
    public List<Integer> applyTransforms(List<Integer> originalGlyphIds)
    {
        List<Integer> intermediateGlyphsFromGsub = originalGlyphIds;

        for (String feature : FEATURES_IN_ORDER)
        {
            if (!gsubData.isFeatureSupported(feature))
            {
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

    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature,
            List<Integer> originalGlyphs)
    {
        Set<List<Integer>> allGlyphIdsForSubstitution = scriptFeature.getAllGlyphIdsForSubstitution();
        if (allGlyphIdsForSubstitution.isEmpty())
        {
            LOG.debug("getAllGlyphIdsForSubstitution() for {} is empty", scriptFeature.getName());
            return originalGlyphs;
        }

        GlyphArraySplitter glyphArraySplitter =
                map.computeIfAbsent(scriptFeature.getName(), k -> new GlyphArraySplitterRegexImpl(allGlyphIdsForSubstitution));

        List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);
        List<Integer> gsubProcessedGlyphs = new ArrayList<>();

        for (List<Integer> chunk : tokens)
        {
            if (scriptFeature.canReplaceGlyphs(chunk))
            {
                // gsub system kicks in, you get the glyphId directly
                List<Integer> replacementForGlyphs = scriptFeature.getReplacementForGlyphs(chunk);
                gsubProcessedGlyphs.addAll(replacementForGlyphs);
            }
            else
            {
                gsubProcessedGlyphs.addAll(chunk);
            }
        }

        LOG.debug("originalGlyphs: {} gsubProcessedGlyphs: {}", originalGlyphs, gsubProcessedGlyphs);

        return gsubProcessedGlyphs;
    }
}
