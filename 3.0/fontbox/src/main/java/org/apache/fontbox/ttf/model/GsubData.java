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

import java.util.Set;

/**
 * Model for data from the GSUB tables
 * 
 * @author Palash Ray
 *
 */
public interface GsubData
{
    /**
     * To be used when there is no GSUB data available
     */
    GsubData NO_DATA_FOUND = new GsubData()
    {

        @Override
        public boolean isFeatureSupported(String featureName)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Language getLanguage()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScriptFeature getFeature(String featureName)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getActiveScriptName()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getSupportedFeatures()
        {
            throw new UnsupportedOperationException();
        }
    };

    Language getLanguage();

    /**
     * A {@link Language} can have more than one script that is supported. However, at any given
     * point, only one of the many scripts are active.
     *
     * @return The name of the script that is active.
     */
    String getActiveScriptName();

    boolean isFeatureSupported(String featureName);

    ScriptFeature getFeature(String featureName);

    Set<String> getSupportedFeatures();

}
