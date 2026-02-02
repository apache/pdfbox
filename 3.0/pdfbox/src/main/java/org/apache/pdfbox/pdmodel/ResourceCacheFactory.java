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
 * Factory which provides a function to create an instance of a ResourceCache to be used to cache resources while
 * processing a pdf.
 * 
 * A DefaultResourceCache is used as default implementation. I can be replaced with an individual implementation.
 * 
 * Setting the function to null disables the cache.
 */
public class ResourceCacheFactory
{
    private static ResourceCacheCreateFunction resourceCacheCreateFunction = null;

    static
    {
        setResourceCacheCreateFunction(new DefaultResourceCacheCreateImpl());
    }

    /**
     * Use the given function to create an instance of a resource cache. Caching is disabled if a null value is
     * provided.
     */
    public static void setResourceCacheCreateFunction(ResourceCacheCreateFunction function)
    {
        resourceCacheCreateFunction = function;
    }

    /**
     * Get the current function to be used to create an instance of a resource cache.
     * 
     * @return the current function or null.
     */
    public static ResourceCacheCreateFunction getResourceCacheCreateFunction()
    {
        return resourceCacheCreateFunction;
    }

    /**
     * Create an instance of a resource cache using the provided function. Returns null if the function is set to null.
     * 
     * @return an instance of a resource cache.
     */
    public static ResourceCache createResourceCache()
    {
        return resourceCacheCreateFunction != null ? resourceCacheCreateFunction.create() : null;
    }

}
