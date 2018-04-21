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

/**
 * This class is a part of the <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/gsub">GSUB — Glyph
 * Substitution Table</a> system of tables in the Open Type Font specs. This is a part of the <a href=
 * "https://docs.microsoft.com/en-us/typography/opentype/spec/gsub#lookuptype-4-ligature-substitution-subtable">LookupType
 * 4: Ligature Substitution Subtable</a>. It specifically models the
 * <a href= "https://docs.microsoft.com/en-us/typography/opentype/spec/gsub#41-ligature-substitution-format-1">Ligature
 * table: Glyph components for one ligature</a>.
 * 
 * @author Palash Ray
 *
 */
public class LigatureTable
{
    private final int ligatureGlyph;
    private final int componentCount;
    private final int[] componentGlyphIDs;

    public LigatureTable(int ligatureGlyph, int componentCount, int[] componentGlyphIDs)
    {
        this.ligatureGlyph = ligatureGlyph;
        this.componentCount = componentCount;
        this.componentGlyphIDs = componentGlyphIDs;
    }

    public int getLigatureGlyph()
    {
        return ligatureGlyph;
    }

    public int getComponentCount()
    {
        return componentCount;
    }

    public int[] getComponentGlyphIDs()
    {
        return componentGlyphIDs;
    }

    @Override
    public String toString()
    {
        return String.format("%s[ligatureGlyph=%d, componentCount=%d]",
                LigatureTable.class.getSimpleName(), ligatureGlyph, componentCount);
    }
}
