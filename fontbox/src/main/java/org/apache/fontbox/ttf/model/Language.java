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

import org.apache.fontbox.ttf.gsub.GsubWorker;
import org.apache.fontbox.ttf.gsub.GsubWorkerFactory;

/**
 * Enumerates the languages supported for GSUB operation. In order to support a new language, you
 * need to add it here and then implement the {@link GsubWorker} for the given language and return
 * the same from the
 * {@link GsubWorkerFactory#getGsubWorker(org.apache.fontbox.ttf.CmapLookup, GsubData)}
 *
 * @author Palash Ray
 *
 */
public enum Language
{

    BENGALI(new String[] { "bng2", "beng" }),
    DEVANAGARI(new String[] { "dev2", "deva" }),
    GUJARATI(new String[] { "gjr2", "gujr" }),
    LATIN(new String[] { "latn" }),

    /**
     * An entry explicitly denoting the absence of any concrete language. May be useful when no actual glyph
     * substitution is required but only the content of GSUB table is of interest.
     *
     * Must be the last one as it is not a language per se.
     */
    UNSPECIFIED(new String[0]);

    private final String[] scriptNames;

    private Language(String[] scriptNames)
    {
        this.scriptNames = scriptNames;
    }

    /**
     * ScriptNames form the basis of identification of the language. This method gets the ScriptNames that the given
     * Language supports, in the order of preference, Index 0 being the most preferred. These names should match the
     * script record in the GSUB system.
     * 
     * @return an array containing all supported languages
     */
    public String[] getScriptNames()
    {
        return scriptNames;
    }

}
