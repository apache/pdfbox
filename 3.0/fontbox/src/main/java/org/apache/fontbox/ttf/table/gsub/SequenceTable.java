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
 * This class is a part of the
 * <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/gsub">GSUB — Glyph
 * Substitution Table</a> system of tables in the Open Type Font specs. This is a part of the <a href=
 * "https://learn.microsoft.com/en-us/typography/opentype/spec/gsub#lookuptype-2-multiple-substitution-subtable">LookupType
 * 2: Multiple Substitution Subtable</a>. It specifically models the <a href=
 * "https://learn.microsoft.com/en-us/typography/opentype/spec/gsub#21-multiple-substitution-format-1">Sequence
 * table</a>.
 *
 * @author Tilman Hausherr
 *
 */
public class SequenceTable
{
    private final int glyphCount;
    private final int[] substituteGlyphIDs;

    public SequenceTable(int glyphCount, int[] substituteGlyphIDs)
    {
        this.glyphCount = glyphCount;
        this.substituteGlyphIDs = substituteGlyphIDs;
    }

    public int getGlyphCount()
    {
        return glyphCount;
    }

    public int[] getSubstituteGlyphIDs()
    {
        return substituteGlyphIDs;
    }

    @Override
    public String toString()
    {
        return "SequenceTable{" + "glyphCount=" + glyphCount + ", substituteGlyphIDs=" + Arrays.toString(substituteGlyphIDs) + '}';
    }
}
