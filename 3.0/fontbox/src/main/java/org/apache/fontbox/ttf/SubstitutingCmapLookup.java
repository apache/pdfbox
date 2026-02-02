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
package org.apache.fontbox.ttf;

import java.util.List;

/**
 * A cmap lookup that performs substitution via the 'GSUB' table.
 *
 * @author Aaron Madlon-Kay
 */
public class SubstitutingCmapLookup implements CmapLookup
{
    private final CmapSubtable cmap;
    private final GlyphSubstitutionTable gsub;
    private final List<String> enabledFeatures;

    public SubstitutingCmapLookup(CmapSubtable cmap, GlyphSubstitutionTable gsub,
            List<String> enabledFeatures)
    {
        this.cmap = cmap;
        this.gsub = gsub;
        this.enabledFeatures = enabledFeatures;
    }

    @Override
    public int getGlyphId(int characterCode)
    {
        int gid = cmap.getGlyphId(characterCode);
        String[] scriptTags = OpenTypeScript.getScriptTags(characterCode);
        return gsub.getSubstitution(gid, scriptTags, enabledFeatures);
    }

    @Override
    public List<Integer> getCharCodes(int gid)
    {
        return cmap.getCharCodes(gsub.getUnsubstitution(gid));
    }
}
