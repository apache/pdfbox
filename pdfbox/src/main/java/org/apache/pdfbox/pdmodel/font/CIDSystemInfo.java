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

/**
 * Represents a CIDSystemInfo for the FontMapper API.
 *
 * @author John Hewson
 */
public final class CIDSystemInfo
{
    private final String registry;
    private final String ordering;
    private final int supplement;

    CIDSystemInfo(String registry, String ordering, int supplement)
    {
        this.registry = registry;
        this.ordering = ordering;
        this.supplement = supplement;
    }
    
    public String getRegistry()
    {
        return registry;
    }

    public String getOrdering()
    {
        return ordering;
    }

    public int getSupplement()
    {
        return supplement;
    }

    @Override
    public String toString()
    {
        return getRegistry() + "-" + getOrdering() + "-" + getSupplement();
    }
}
