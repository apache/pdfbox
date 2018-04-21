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

import org.apache.fontbox.ttf.table.common.CoverageTable;
import org.apache.fontbox.ttf.table.common.LookupSubTable;

/**
 * This class is a part of the <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/gsub">GSUB — Glyph
 * Substitution Table</a> system of tables in the Open Type Font specs. This is a part of the <a href=
 * "https://docs.microsoft.com/en-us/typography/opentype/spec/gsub#lookuptype-4-ligature-substitution-subtable">LookupType
 * 4: Ligature Substitution Subtable</a>. It specifically models the
 * <a href= "https://docs.microsoft.com/en-us/typography/opentype/spec/gsub#41-ligature-substitution-format-1">Ligature
 * Substitution Format 1</a>.
 * 
 * @author Palash Ray
 *
 */
public class LookupTypeLigatureSubstitutionSubstFormat1 extends LookupSubTable
{
    private final LigatureSetTable[] ligatureSetTables;

    public LookupTypeLigatureSubstitutionSubstFormat1(int substFormat, CoverageTable coverageTable,
            LigatureSetTable[] ligatureSetTables)
    {
        super(substFormat, coverageTable);
        this.ligatureSetTables = ligatureSetTables;
    }

    @Override
    public int doSubstitution(int gid, int coverageIndex)
    {
        throw new UnsupportedOperationException();
    }

    public LigatureSetTable[] getLigatureSetTables()
    {
        return ligatureSetTables;
    }

    @Override
    public String toString()
    {
        return String.format("%s[substFormat=%d]",
                LookupTypeLigatureSubstitutionSubstFormat1.class.getSimpleName(), getSubstFormat());
    }
}