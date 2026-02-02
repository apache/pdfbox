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
package org.apache.fontbox.ttf.table.gsub;

import java.util.Arrays;

/**
 * LookupType 3: Alternate Substitution Subtable
 * as described in OpenType spec: <a href="https://learn.microsoft.com/en-us/typography/opentype/spec/gsub#31-alternate-substitution-format-1">...</a>
 */
public class AlternateSetTable
{
    private final int glyphCount;
    private final int[] alternateGlyphIDs;

    public AlternateSetTable(int glyphCount, int[] alternateGlyphIDs)
    {
        this.glyphCount = glyphCount;
        this.alternateGlyphIDs = alternateGlyphIDs;
    }

    public int getGlyphCount()
    {
        return glyphCount;
    }

    public int[] getAlternateGlyphIDs()
    {
        return alternateGlyphIDs;
    }

    @Override
    public String toString()
    {
        return "AlternateSetTable{" + "glyphCount=" + glyphCount + ", alternateGlyphIDs=" + Arrays.toString(alternateGlyphIDs) + '}';
    }
}
