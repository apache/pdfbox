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

package org.apache.fontbox.ttf.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * A {@link Map} based simple implementation of the {@link GsubData}
 * 
 * @author Palash Ray
 *
 */
public class MapBackedGsubData implements GsubData
{

    private final Language language;
    private final String activeScriptName;
    private final Map<String, Map<List<Integer>, List<Integer>>> glyphSubstitutionMap;

    public MapBackedGsubData(Language language, String activeScriptName,
            Map<String, Map<List<Integer>, List<Integer>>> glyphSubstitutionMap)
    {
        this.language = language;
        this.activeScriptName = activeScriptName;
        this.glyphSubstitutionMap = glyphSubstitutionMap;
    }

    @Override
    public Language getLanguage()
    {
        return language;
    }

    @Override
    public String getActiveScriptName()
    {
        return activeScriptName;
    }

    @Override
    public boolean isFeatureSupported(String featureName)
    {
        return glyphSubstitutionMap.containsKey(featureName);
    }

    @Override
    public ScriptFeature getFeature(String featureName)
    {
        if (!isFeatureSupported(featureName))
        {
            throw new UnsupportedOperationException(
                    "The feature " + featureName + " is not supported!");
        }

        return new MapBackedScriptFeature(featureName, glyphSubstitutionMap.get(featureName));
    }

    @Override
    public Set<String> getSupportedFeatures()
    {
        return glyphSubstitutionMap.keySet();
    }

}
