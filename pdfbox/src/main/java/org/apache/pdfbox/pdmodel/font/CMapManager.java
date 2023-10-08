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
package org.apache.pdfbox.pdmodel.font;

import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.pdfbox.io.RandomAccessRead;

import java.io.IOException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CMap resource loader and cache.
 */
final class CMapManager
{
    private static final Map<String, CMap> CMAP_CACHE = new ConcurrentHashMap<>();

    private CMapManager()
    {
    }

    /**
     * Fetches the predefined CMap from disk (or cache).
     *
     * @param cMapName CMap name
     * @return The predefined CMap, never null.
     * @throws IOException 
     */
    public static CMap getPredefinedCMap(String cMapName) throws IOException
    {
        CMap cmap = CMAP_CACHE.get(cMapName);
        if (cmap != null)
        {
            return cmap;
        }

        CMap targetCmap = new CMapParser().parsePredefined(cMapName);

        // limit the cache to predefined CMaps
        CMAP_CACHE.put(targetCmap.getName(), targetCmap);
        return targetCmap;
    }

    /**
     * Parse the given CMap.
     *
     * @param randomAccessRead the source of the CMap to be read
     * @return the parsed CMap
     */
    public static CMap parseCMap(RandomAccessRead randomAccessRead) throws IOException
    {
        CMap targetCmap = null;
        if (randomAccessRead != null)
        {
            targetCmap = new CMapParser().parse(randomAccessRead);
        }
        return targetCmap;
    }
}
