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

package org.apache.fontbox.ttf.advanced;

/**
 * <p>The <code>GlyphCoverageMapping</code> interface provides glyph identifier to coverage
 * index mapping support.</p>
 *
 * @author Glenn Adams
 */
public interface GlyphCoverageMapping {

    /**
     * Obtain size of coverage table, i.e., ciMax + 1, where ciMax is the maximum
     * coverage index.
     * @return size of coverage table
     */
    int getCoverageSize();

    /**
     * Map glyph identifier (code) to coverge index. Returns -1 if glyph identifier is not in the domain of
     * the coverage table.
     * @param gid glyph identifier (code)
     * @return non-negative glyph coverage index or -1 if glyph identifiers is not mapped by table
     */
    int getCoverageIndex(int gid);

}
