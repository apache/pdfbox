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

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.Language;

/**
 * Gets a {@link Language} specific instance of a {@link GsubWorker}
 * 
 * @author Palash Ray
 *
 */
public class GsubWorkerFactory
{

    public GsubWorker getGsubWorker(CmapLookup cmapLookup, GsubData gsubData)
    {
        switch (gsubData.getLanguage())
        {
        case BENGALI:
            return new GsubWorkerForBengali(cmapLookup, gsubData);
        default:
            throw new UnsupportedOperationException(
                    "The language " + gsubData.getLanguage() + " is not yet supported");
        }

    }

}
