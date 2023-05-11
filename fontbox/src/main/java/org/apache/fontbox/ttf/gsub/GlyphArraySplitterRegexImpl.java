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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is an in-efficient implementation based on regex, which helps split the array.
 * 
 * @author Palash Ray
 *
 */
public class GlyphArraySplitterRegexImpl implements GlyphArraySplitter
{
    private static final String GLYPH_ID_SEPARATOR = "_";

    private final CompoundCharacterTokenizer compoundCharacterTokenizer;

    public GlyphArraySplitterRegexImpl(Set<List<Integer>> matchers)
    {
        compoundCharacterTokenizer = new CompoundCharacterTokenizer(getMatchersAsStrings(matchers));
    }

    @Override
    public List<List<Integer>> split(List<Integer> glyphIds)
    {
        String originalGlyphsAsText = convertGlyphIdsToString(glyphIds);
        List<String> tokens = compoundCharacterTokenizer.tokenize(originalGlyphsAsText);

        List<List<Integer>> modifiedGlyphs = new ArrayList<>(tokens.size());
        tokens.forEach(token -> modifiedGlyphs.add(convertGlyphIdsToList(token)));
        return modifiedGlyphs;
    }

    private Set<String> getMatchersAsStrings(Set<List<Integer>> matchers)
    {
        Set<String> stringMatchers = new TreeSet<>((String s1, String s2) ->
        {
            // comparator to ensure that strings with the same beginning
            // put the larger string first        
            if (s1.length() == s2.length())
            {
                return s2.compareTo(s1);
            }
            return s2.length() - s1.length();
        });
        matchers.forEach(glyphIds -> stringMatchers.add(convertGlyphIdsToString(glyphIds)));
        return stringMatchers;
    }

    private String convertGlyphIdsToString(List<Integer> glyphIds)
    {
        StringBuilder sb = new StringBuilder(20);
        sb.append(GLYPH_ID_SEPARATOR);
        glyphIds.forEach(glyphId -> sb.append(glyphId).append(GLYPH_ID_SEPARATOR));
        return sb.toString();
    }

    private List<Integer> convertGlyphIdsToList(String glyphIdsAsString)
    {
        List<Integer> gsubProcessedGlyphsIds = new ArrayList<>();

        for (String glyphId : glyphIdsAsString.split(GLYPH_ID_SEPARATOR))
        {
            glyphId = glyphId.trim();
            if (glyphId.isEmpty())
            {
                continue;
            }
            gsubProcessedGlyphsIds.add(Integer.valueOf(glyphId));
        }

        return gsubProcessedGlyphsIds;
    }

}
