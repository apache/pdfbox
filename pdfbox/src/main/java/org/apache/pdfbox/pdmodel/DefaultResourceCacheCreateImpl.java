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

package org.apache.pdfbox.pdmodel;

/**
 * Implementation of the functional interface ResourceCacheCreateFunction to create an instance of a
 * DefaultResourceCache.
 */
public class DefaultResourceCacheCreateImpl implements ResourceCacheCreateFunction
{
    private final boolean stableCacheEnabled;

    /**
     * Default constructor.
     */
    public DefaultResourceCacheCreateImpl()
    {
        this(true);
    }

    /**
     * Constructor providing a parameter to enable/disable the stable object cache.
     * 
     * @param enableStableCache enables/disables the stable object cache
     * 
     */
    public DefaultResourceCacheCreateImpl(boolean enableStableCache)
    {
        stableCacheEnabled = enableStableCache;
    }

    @Override
    public ResourceCache create()
    {
        return new DefaultResourceCache(stableCacheEnabled);
    }
}

