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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.Language;
import org.apache.fontbox.ttf.model.MapBackedGsubData;
import org.apache.fontbox.ttf.table.common.CoverageTable;
import org.apache.fontbox.ttf.table.common.FeatureListTable;
import org.apache.fontbox.ttf.table.common.FeatureRecord;
import org.apache.fontbox.ttf.table.common.LangSysTable;
import org.apache.fontbox.ttf.table.common.LookupListTable;
import org.apache.fontbox.ttf.table.common.LookupSubTable;
import org.apache.fontbox.ttf.table.common.LookupTable;
import org.apache.fontbox.ttf.table.common.ScriptTable;
import org.apache.fontbox.ttf.table.gsub.AlternateSetTable;
import org.apache.fontbox.ttf.table.gsub.LigatureSetTable;
import org.apache.fontbox.ttf.table.gsub.LigatureTable;
import org.apache.fontbox.ttf.table.gsub.LookupTypeAlternateSubstitutionFormat1;
import org.apache.fontbox.ttf.table.gsub.LookupTypeLigatureSubstitutionSubstFormat1;
import org.apache.fontbox.ttf.table.gsub.LookupTypeMultipleSubstitutionFormat1;
import org.apache.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat1;
import org.apache.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat2;
import org.apache.fontbox.ttf.table.gsub.SequenceTable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class has utility methods to extract meaningful GsubData from the highly obfuscated GSUB
 * Tables. This GsubData is then used to determine which combination of glyphs or words have to be
 * replaced.
 *
 * @author Palash Ray
 * 
 */
public class GlyphSubstitutionDataExtractor
{
    private static final Logger LOG = LogManager.getLogger(GlyphSubstitutionDataExtractor.class);

    public GsubData getGsubData(Map<String, ScriptTable> scriptList,
            FeatureListTable featureListTable, LookupListTable lookupListTable)
    {

        ScriptTableDetails scriptTableDetails = getSupportedLanguage(scriptList);

        if (scriptTableDetails == null)
        {
            return GsubData.NO_DATA_FOUND;
        }
        return buildMapBackedGsubData(featureListTable, lookupListTable, scriptTableDetails);
    }

    /**
     * Unlike {@link #getGsubData(Map, FeatureListTable, LookupListTable)}, this method doesn't iterate over supported
     * {@link Language}'s searching for the first match with the scripts of the font. Instead, it unconditionally
     * creates {@link ScriptTableDetails} instance with language left {@linkplain Language#UNSPECIFIED unspecified}.
     * 
     * @param scriptName
     * @param scriptTable
     * @param featureListTable
     * @param lookupListTable
     * @return {@link GsubData} instance built especially for the given {@code scriptName}
     */
    public GsubData getGsubData(String scriptName, ScriptTable scriptTable,
            FeatureListTable featureListTable, LookupListTable lookupListTable)
    {
        ScriptTableDetails scriptTableDetails = new ScriptTableDetails(Language.UNSPECIFIED,
                scriptName, scriptTable);

        return buildMapBackedGsubData(featureListTable, lookupListTable, scriptTableDetails);
    }

    private MapBackedGsubData buildMapBackedGsubData(FeatureListTable featureListTable,
            LookupListTable lookupListTable, ScriptTableDetails scriptTableDetails)
    {
        ScriptTable scriptTable = scriptTableDetails.getScriptTable();

        Map<String, Map<List<Integer>, List<Integer>>> gsubData = new LinkedHashMap<>();
        // the starting point is really the scriptTags
        if (scriptTable.getDefaultLangSysTable() != null)
        {
            populateGsubData(gsubData, scriptTable.getDefaultLangSysTable(), featureListTable,
                    lookupListTable);
        }
        for (LangSysTable langSysTable : scriptTable.getLangSysTables().values())
        {
            populateGsubData(gsubData, langSysTable, featureListTable, lookupListTable);
        }

        return new MapBackedGsubData(scriptTableDetails.getLanguage(),
                scriptTableDetails.getFeatureName(), gsubData);
    }

    private ScriptTableDetails getSupportedLanguage(Map<String, ScriptTable> scriptList)
    {
        for (Language lang : Language.values())
        {
            for (String scriptName : lang.getScriptNames())
            {
                ScriptTable value = scriptList.get(scriptName);
                if (value != null)
                {
                    LOG.debug("Language decided: {} {}", lang, scriptName);
                    return new ScriptTableDetails(lang, scriptName, value);
                }
            }
        }
        return null;
    }

    private void populateGsubData(Map<String, Map<List<Integer>, List<Integer>>> gsubData,
            LangSysTable langSysTable, FeatureListTable featureListTable,
            LookupListTable lookupListTable)
    {
        FeatureRecord[] featureRecords = featureListTable.getFeatureRecords();
        for (int featureIndex : langSysTable.getFeatureIndices())
        {
            if (featureIndex < featureRecords.length)
            {
                populateGsubData(gsubData, featureRecords[featureIndex], lookupListTable);
            }
        }
    }

    // Creates a Map<List<Integer>, Integer> from the lookup tables
    private void populateGsubData(Map<String, Map<List<Integer>, List<Integer>>> gsubData,
            FeatureRecord featureRecord, LookupListTable lookupListTable)
    {
        LookupTable[] lookups = lookupListTable.getLookups();
        Map<List<Integer>, List<Integer>> glyphSubstitutionMap = new LinkedHashMap<>();
        for (int lookupIndex : featureRecord.getFeatureTable().getLookupListIndices())
        {
            if (lookupIndex < lookups.length)
            {
                extractData(glyphSubstitutionMap, lookups[lookupIndex]);
            }
        }

        LOG.debug("*********** extracting GSUB data for the feature: {}, glyphSubstitutionMap: {}",
                featureRecord.getFeatureTag(), glyphSubstitutionMap);

        gsubData.put(featureRecord.getFeatureTag(),
                Collections.unmodifiableMap(glyphSubstitutionMap));
    }

    private void extractData(Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LookupTable lookupTable)
    {

        for (LookupSubTable lookupSubTable : lookupTable.getSubTables())
        {
            if (lookupSubTable instanceof LookupTypeLigatureSubstitutionSubstFormat1)
            {
                extractDataFromLigatureSubstitutionSubstFormat1Table(glyphSubstitutionMap,
                        (LookupTypeLigatureSubstitutionSubstFormat1) lookupSubTable);
            }
            else if (lookupSubTable instanceof LookupTypeAlternateSubstitutionFormat1)
            {
                extractDataFromAlternateSubstitutionSubstFormat1Table(glyphSubstitutionMap,
                        (LookupTypeAlternateSubstitutionFormat1) lookupSubTable);
            }
            else if (lookupSubTable instanceof LookupTypeSingleSubstFormat1)
            {
                extractDataFromSingleSubstTableFormat1Table(glyphSubstitutionMap,
                        (LookupTypeSingleSubstFormat1) lookupSubTable);
            }
            else if (lookupSubTable instanceof LookupTypeSingleSubstFormat2)
            {
                extractDataFromSingleSubstTableFormat2Table(glyphSubstitutionMap,
                        (LookupTypeSingleSubstFormat2) lookupSubTable);
            }
            else if (lookupSubTable instanceof LookupTypeMultipleSubstitutionFormat1)
            {
                extractDataFromMultipleSubstitutionFormat1Table(glyphSubstitutionMap,
                        (LookupTypeMultipleSubstitutionFormat1) lookupSubTable);
            }
            else
            {
                // usually null, due to being skipped in GlyphSubstitutionTable.readLookupTable()
                LOG.debug("The type {} is not yet supported, will be ignored", lookupSubTable);
            }
        }

    }

    private void extractDataFromSingleSubstTableFormat1Table(
            Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LookupTypeSingleSubstFormat1 singleSubstTableFormat1)
    {
        CoverageTable coverageTable = singleSubstTableFormat1.getCoverageTable();
        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = coverageGlyphId + singleSubstTableFormat1.getDeltaGlyphID();
            putNewSubstitutionEntry(glyphSubstitutionMap, Collections.singletonList(substituteGlyphId),
                    Collections.singletonList(coverageGlyphId));
        }
    }

    private void extractDataFromSingleSubstTableFormat2Table(
            Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LookupTypeSingleSubstFormat2 singleSubstTableFormat2)
    {

        CoverageTable coverageTable = singleSubstTableFormat2.getCoverageTable();

        if (coverageTable.getSize() != singleSubstTableFormat2.getSubstituteGlyphIDs().length)
        {
            LOG.warn(
                    "The coverage table size ({}) should be the same as the count of the substituteGlyphIDs tables ({})",
                    coverageTable.getSize(),
                    singleSubstTableFormat2.getSubstituteGlyphIDs().length);
            return;
        }

        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = singleSubstTableFormat2.getSubstituteGlyphIDs()[i];
            putNewSubstitutionEntry(glyphSubstitutionMap, Collections.singletonList(substituteGlyphId),
                    Collections.singletonList(coverageGlyphId));
        }
    }

    private void extractDataFromMultipleSubstitutionFormat1Table(
            Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LookupTypeMultipleSubstitutionFormat1 multipleSubstFormat1Subtable)
    {
        CoverageTable coverageTable = multipleSubstFormat1Subtable.getCoverageTable();

        if (coverageTable.getSize() != multipleSubstFormat1Subtable.getSequenceTables().length)
        {
            LOG.warn(
                    "The coverage table size ({}) should be the same as the count of the sequence tables ({})",
                    coverageTable.getSize(),
                    multipleSubstFormat1Subtable.getSequenceTables().length);
            return;
        }

        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            SequenceTable sequenceTable = multipleSubstFormat1Subtable.getSequenceTables()[i];
            int[] substituteGlyphIDArray = sequenceTable.getSubstituteGlyphIDs();
            List<Integer> substituteGlyphIDList = new ArrayList<>(substituteGlyphIDArray.length);
            for (int id : substituteGlyphIDArray)
            {
                substituteGlyphIDList.add(id);
            }
            putNewSubstitutionEntry(glyphSubstitutionMap,
                    substituteGlyphIDList,
                    Collections.singletonList(coverageGlyphId));
        }
    }

    private void extractDataFromLigatureSubstitutionSubstFormat1Table(
            Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LookupTypeLigatureSubstitutionSubstFormat1 ligatureSubstitutionTable)
    {

        for (LigatureSetTable ligatureSetTable : ligatureSubstitutionTable.getLigatureSetTables())
        {
            for (LigatureTable ligatureTable : ligatureSetTable.getLigatureTables())
            {
                extractDataFromLigatureTable(glyphSubstitutionMap, ligatureTable);
            }

        }

    }

    /**
     * Extracts data from the AlternateSubstitutionFormat1 (lookuptype) 3 table and puts it in the
     * glyphSubstitutionMap.
     *
     * @param glyphSubstitutionMap         the map to store the substitution data
     * @param alternateSubstitutionFormat1 the alternate substitution format 1 table
     */
    private void extractDataFromAlternateSubstitutionSubstFormat1Table(
            Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LookupTypeAlternateSubstitutionFormat1 alternateSubstitutionFormat1)
    {

        CoverageTable coverageTable = alternateSubstitutionFormat1.getCoverageTable();

        if (coverageTable.getSize() != alternateSubstitutionFormat1.getAlternateSetTables().length)
        {
            LOG.warn("The coverage table size ({}) should be the same as the count of the alternate set tables ({})",
                    coverageTable.getSize(), 
                    alternateSubstitutionFormat1.getAlternateSetTables().length);
            return;
        }

        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            AlternateSetTable sequenceTable = alternateSubstitutionFormat1.getAlternateSetTables()[i];

            // Loop through the substitute glyphs and pick the first one that is not the same as the coverage glyph
            for (int alternateGlyphId : sequenceTable.getAlternateGlyphIDs())
            {
                if (alternateGlyphId != coverageGlyphId)
                {
                    putNewSubstitutionEntry(glyphSubstitutionMap, Collections.singletonList(alternateGlyphId),
                            Collections.singletonList(coverageGlyphId));
                    break;
                }
            }
        }

    }

    private void extractDataFromLigatureTable(Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            LigatureTable ligatureTable)
    {
        int[] componentGlyphIDs = ligatureTable.getComponentGlyphIDs();
        List<Integer> glyphsToBeSubstituted = new ArrayList<>(componentGlyphIDs.length);
        for (int componentGlyphID : componentGlyphIDs)
        {
            glyphsToBeSubstituted.add(componentGlyphID);
        }

        LOG.debug("glyphsToBeSubstituted: {}", glyphsToBeSubstituted);

        putNewSubstitutionEntry(glyphSubstitutionMap, Collections.singletonList(ligatureTable.getLigatureGlyph()),
                glyphsToBeSubstituted);
    }

    private void putNewSubstitutionEntry(Map<List<Integer>, List<Integer>> glyphSubstitutionMap,
            List<Integer> newGlyphList, List<Integer> glyphsToBeSubstituted)
    {
        List<Integer> oldValues = glyphSubstitutionMap.put(glyphsToBeSubstituted, newGlyphList);

        if (oldValues != null)
        {
            LOG.debug("For the newGlyph: {}, newValue: {} is trying to override the oldValue {}",
                    newGlyphList, glyphsToBeSubstituted, oldValues);
        }
    }

    private static class ScriptTableDetails
    {
        private final Language language;
        private final String featureName;
        private final ScriptTable scriptTable;

        private ScriptTableDetails(Language language, String featureName, ScriptTable scriptTable)
        {
            this.language = language;
            this.featureName = featureName;
            this.scriptTable = scriptTable;
        }

        public Language getLanguage()
        {
            return language;
        }

        public String getFeatureName()
        {
            return featureName;
        }

        public ScriptTable getScriptTable()
        {
            return scriptTable;
        }

    }

}
