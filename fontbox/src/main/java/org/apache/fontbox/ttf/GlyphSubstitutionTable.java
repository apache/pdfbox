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

package org.apache.fontbox.ttf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.gsub.GlyphSubstitutionDataExtractor;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.table.common.CoverageTable;
import org.apache.fontbox.ttf.table.common.CoverageTableFormat1;
import org.apache.fontbox.ttf.table.common.CoverageTableFormat2;
import org.apache.fontbox.ttf.table.common.FeatureListTable;
import org.apache.fontbox.ttf.table.common.FeatureRecord;
import org.apache.fontbox.ttf.table.common.FeatureTable;
import org.apache.fontbox.ttf.table.common.LangSysRecord;
import org.apache.fontbox.ttf.table.common.LangSysTable;
import org.apache.fontbox.ttf.table.common.LookupListTable;
import org.apache.fontbox.ttf.table.common.LookupSubTable;
import org.apache.fontbox.ttf.table.common.LookupTable;
import org.apache.fontbox.ttf.table.common.RangeRecord;
import org.apache.fontbox.ttf.table.common.ScriptRecord;
import org.apache.fontbox.ttf.table.common.ScriptTable;
import org.apache.fontbox.ttf.table.gsub.LigatureSetTable;
import org.apache.fontbox.ttf.table.gsub.LigatureTable;
import org.apache.fontbox.ttf.table.gsub.LookupTypeLigatureSubstitutionSubstFormat1;
import org.apache.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat1;
import org.apache.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat2;

/**
 * A glyph substitution 'GSUB' table in a TrueType or OpenType font.
 *
 * @author Aaron Madlon-Kay
 */
public class GlyphSubstitutionTable extends TTFTable
{
    private static final Log LOG = LogFactory.getLog(GlyphSubstitutionTable.class);

    public static final String TAG = "GSUB";

    private Map<String, ScriptTable> scriptList;
    // featureList and lookupList are not maps because we need to index into them
    private FeatureListTable featureListTable;
    private LookupListTable lookupListTable;

    private final Map<Integer, Integer> lookupCache = new HashMap<>();
    private final Map<Integer, Integer> reverseLookup = new HashMap<>();

    private String lastUsedSupportedScript;

    private GsubData gsubData;

    GlyphSubstitutionTable(TrueTypeFont font)
    {
        super(font);
    }

    @Override
    @SuppressWarnings({"squid:S1854"})
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        long start = data.getCurrentPosition();
        @SuppressWarnings({"unused"})
        int majorVersion = data.readUnsignedShort();
        int minorVersion = data.readUnsignedShort();
        int scriptListOffset = data.readUnsignedShort();
        int featureListOffset = data.readUnsignedShort();
        int lookupListOffset = data.readUnsignedShort();
        @SuppressWarnings({"unused"})
        long featureVariationsOffset = -1L;
        if (minorVersion == 1L)
        {
            featureVariationsOffset = data.readUnsignedInt();
        }

        scriptList = readScriptList(data, start + scriptListOffset);
        featureListTable = readFeatureList(data, start + featureListOffset);
        lookupListTable = readLookupList(data, start + lookupListOffset);

        GlyphSubstitutionDataExtractor glyphSubstitutionDataExtractor = new GlyphSubstitutionDataExtractor();

        gsubData = glyphSubstitutionDataExtractor
                .getGsubData(scriptList, featureListTable, lookupListTable);
    }

    private Map<String, ScriptTable> readScriptList(TTFDataStream data, long offset)
            throws IOException
    {
        data.seek(offset);
        int scriptCount = data.readUnsignedShort();
        ScriptTable[] scriptTables= new ScriptTable[scriptCount];
        int[] scriptOffsets = new int[scriptCount];
        String[] scriptTags = new String[scriptCount];
        for (int i = 0; i < scriptCount; i++)
        {
            scriptTags[i] = data.readString(4);
            scriptOffsets[i] = data.readUnsignedShort();
        }
        for (int i = 0; i < scriptCount; i++)
        {
            scriptTables[i] = readScriptTable(data, offset + scriptOffsets[i]);
        }
        Map<String, ScriptTable> resultScriptList = new LinkedHashMap<>(scriptCount);
        for (int i = 0; i < scriptCount; i++)
        {
            ScriptRecord scriptRecord = new ScriptRecord(scriptTags[i], scriptTables[i]);
            resultScriptList.put(scriptRecord.getScriptTag(), scriptRecord.getScriptTable());
        }
        return Collections.unmodifiableMap(resultScriptList);
    }

    private ScriptTable readScriptTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int defaultLangSys = data.readUnsignedShort();
        int langSysCount = data.readUnsignedShort();
        LangSysRecord[] langSysRecords = new LangSysRecord[langSysCount];
        String[] langSysTags = new String[langSysCount];
        int[] langSysOffsets = new int[langSysCount];
        for (int i = 0; i < langSysCount; i++)
        {
            langSysTags[i] = data.readString(4);
            if (i > 0 && langSysTags[i].compareTo(langSysTags[i-1]) <= 0)
            {
                // PDFBOX-4489: catch corrupt file
                // https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2#slTbl_sRec
                LOG.error("LangSysRecords not alphabetically sorted by LangSys tag: " +
                          langSysTags[i] + " <= " + langSysTags[i - 1]);
                return new ScriptTable(null, new LinkedHashMap<String, LangSysTable>());
            }
            langSysOffsets[i] = data.readUnsignedShort();
        }

        LangSysTable defaultLangSysTable = null;

        if (defaultLangSys != 0)
        {
            defaultLangSysTable = readLangSysTable(data, offset + defaultLangSys);
        }
        for (int i = 0; i < langSysCount; i++)
        {
            LangSysTable langSysTable = readLangSysTable(data, offset + langSysOffsets[i]);
            langSysRecords[i] = new LangSysRecord(langSysTags[i], langSysTable);
        }
        Map<String, LangSysTable> langSysTables = new LinkedHashMap<>(langSysCount);
        for (LangSysRecord langSysRecord : langSysRecords)
        {
            langSysTables.put(langSysRecord.getLangSysTag(),
                    langSysRecord.getLangSysTable());
        }
        return new ScriptTable(defaultLangSysTable, Collections.unmodifiableMap(langSysTables));
    }

    private LangSysTable readLangSysTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int lookupOrder = data.readUnsignedShort();
        int requiredFeatureIndex = data.readUnsignedShort();
        int featureIndexCount = data.readUnsignedShort();
        int[] featureIndices = new int[featureIndexCount];
        for (int i = 0; i < featureIndexCount; i++)
        {
            featureIndices[i] = data.readUnsignedShort();
        }
        return new LangSysTable(lookupOrder, requiredFeatureIndex, featureIndexCount,
                featureIndices);
    }

    private FeatureListTable readFeatureList(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int featureCount = data.readUnsignedShort();
        FeatureRecord[] featureRecords = new FeatureRecord[featureCount];
        int[] featureOffsets = new int[featureCount];
        String[] featureTags = new String[featureCount];
        for (int i = 0; i < featureCount; i++)
        {
            featureTags[i] = data.readString(4);
            featureOffsets[i] = data.readUnsignedShort();
        }
        for (int i = 0; i < featureCount; i++)
        {
            FeatureTable featureTable = readFeatureTable(data, offset + featureOffsets[i]);
            featureRecords[i] = new FeatureRecord(featureTags[i], featureTable);
        }
        return new FeatureListTable(featureCount, featureRecords);
    }

    private FeatureTable readFeatureTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int featureParams = data.readUnsignedShort();
        int lookupIndexCount = data.readUnsignedShort();
        int[] lookupListIndices = new int[lookupIndexCount];
        for (int i = 0; i < lookupIndexCount; i++)
        {
            lookupListIndices[i] = data.readUnsignedShort();
        }
        return new FeatureTable(featureParams, lookupIndexCount, lookupListIndices);
    }

    private LookupListTable readLookupList(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int lookupCount = data.readUnsignedShort();
        int[] lookups = new int[lookupCount];
        for (int i = 0; i < lookupCount; i++)
        {
            lookups[i] = data.readUnsignedShort();
        }
        LookupTable[] lookupTables = new LookupTable[lookupCount];
        for (int i = 0; i < lookupCount; i++)
        {
            lookupTables[i] = readLookupTable(data, offset + lookups[i]);
        }
        return new LookupListTable(lookupCount, lookupTables);
    }

    private LookupTable readLookupTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int lookupType = data.readUnsignedShort();
        int lookupFlag = data.readUnsignedShort();
        int subTableCount = data.readUnsignedShort();
        int[] subTableOffets = new int[subTableCount];
        for (int i = 0; i < subTableCount; i++)
        {
            subTableOffets[i] = data.readUnsignedShort();
        }

        int markFilteringSet;
        if ((lookupFlag & 0x0010) != 0)
        {
            markFilteringSet = data.readUnsignedShort();
        }
        else
        {
            markFilteringSet = 0;
        }
        LookupSubTable[] subTables = new LookupSubTable[subTableCount];
        switch (lookupType)
        {
        case 1: // Single
            for (int i = 0; i < subTableCount; i++)
            {
                subTables[i] = readLookupSubTable(data, offset + subTableOffets[i]);
            }
            break;
        case 4: // Ligature Substitution Subtable
            for (int i = 0; i < subTableCount; i++)
            {
                subTables[i] = readLigatureSubstitutionSubtable(data, offset + subTableOffets[i]);
            }
            break;
        default:
            // Other lookup types are not supported
            LOG.debug("Type " + lookupType
                    + " GSUB lookup table is not supported and will be ignored");
        }
        return new LookupTable(lookupType, lookupFlag, markFilteringSet, subTables);
    }

    private LookupSubTable readLookupSubTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();
        switch (substFormat)
        {
        case 1:
        {
            int coverageOffset = data.readUnsignedShort();
            short deltaGlyphID = data.readSignedShort();
            CoverageTable coverageTable = readCoverageTable(data, offset + coverageOffset);
            return new LookupTypeSingleSubstFormat1(substFormat, coverageTable, deltaGlyphID);
        }
        case 2:
        {
            int coverageOffset = data.readUnsignedShort();
            int glyphCount = data.readUnsignedShort();
            int[] substituteGlyphIDs = new int[glyphCount];
            for (int i = 0; i < glyphCount; i++)
            {
                substituteGlyphIDs[i] = data.readUnsignedShort();
            }
            CoverageTable coverageTable = readCoverageTable(data, offset + coverageOffset);
            return new LookupTypeSingleSubstFormat2(substFormat, coverageTable, substituteGlyphIDs);
        }
        default:
            throw new IOException("Unknown substFormat: " + substFormat);
        }
    }

    private LookupSubTable readLigatureSubstitutionSubtable(TTFDataStream data, long offset)
            throws IOException
    {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();

        if (substFormat != 1)
        {
            throw new IOException(
                    "The expected SubstFormat for LigatureSubstitutionTable is 1");
        }

        int coverage = data.readUnsignedShort();
        int ligSetCount = data.readUnsignedShort();

        int[] ligatureOffsets = new int[ligSetCount];

        for (int i = 0; i < ligSetCount; i++)
        {
            ligatureOffsets[i] = data.readUnsignedShort();
        }

        CoverageTable coverageTable = readCoverageTable(data, offset + coverage);

        if (ligSetCount != coverageTable.getSize())
        {
            throw new IOException(
                    "According to the OpenTypeFont specifications, the coverage count should be equal to the no. of LigatureSetTables");
        }

        LigatureSetTable[] ligatureSetTables = new LigatureSetTable[ligSetCount];

        for (int i = 0; i < ligSetCount; i++)
        {

            int coverageGlyphId = coverageTable.getGlyphId(i);

            ligatureSetTables[i] = readLigatureSetTable(data,
                    offset + ligatureOffsets[i], coverageGlyphId);
        }

        return new LookupTypeLigatureSubstitutionSubstFormat1(substFormat, coverageTable,
                ligatureSetTables);
    }

    private LigatureSetTable readLigatureSetTable(TTFDataStream data, long ligatureSetTableLocation,
            int coverageGlyphId) throws IOException
    {
        data.seek(ligatureSetTableLocation);

        int ligatureCount = data.readUnsignedShort();
        LOG.debug("ligatureCount=" + ligatureCount);

        int[] ligatureOffsets = new int[ligatureCount];
        LigatureTable[] ligatureTables = new LigatureTable[ligatureCount];

        for (int i = 0; i < ligatureOffsets.length; i++)
        {
            ligatureOffsets[i] = data.readUnsignedShort();
        }

        for (int i = 0; i < ligatureOffsets.length; i++)
        {
            int ligatureOffset = ligatureOffsets[i];
            ligatureTables[i] = readLigatureTable(data,
                    ligatureSetTableLocation + ligatureOffset, coverageGlyphId);
        }

        return new LigatureSetTable(ligatureCount, ligatureTables);
    }

    private LigatureTable readLigatureTable(TTFDataStream data, long ligatureTableLocation,
            int coverageGlyphId) throws IOException
    {
        data.seek(ligatureTableLocation);

        int ligatureGlyph = data.readUnsignedShort();

        int componentCount = data.readUnsignedShort();

        int[] componentGlyphIDs = new int[componentCount];

        componentGlyphIDs[0] = coverageGlyphId;

        for (int i = 1; i <= componentCount - 1; i++)
        {
            componentGlyphIDs[i] = data.readUnsignedShort();
        }

        return new LigatureTable(ligatureGlyph, componentCount, componentGlyphIDs);

    }

    private CoverageTable readCoverageTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int coverageFormat = data.readUnsignedShort();
        switch (coverageFormat)
        {
        case 1:
        {
            int glyphCount = data.readUnsignedShort();
            int[] glyphArray = new int[glyphCount];
            for (int i = 0; i < glyphCount; i++)
            {
                glyphArray[i] = data.readUnsignedShort();
            }
            return new CoverageTableFormat1(coverageFormat, glyphArray);
        }
        case 2:
        {
            int rangeCount = data.readUnsignedShort();
            RangeRecord[] rangeRecords = new RangeRecord[rangeCount];


            for (int i = 0; i < rangeCount; i++)
            {
                rangeRecords[i] = readRangeRecord(data);
            }

            return new CoverageTableFormat2(coverageFormat, rangeRecords);
        }
        default:
            // Should not happen (the spec indicates only format 1 and format 2)
            throw new IOException("Unknown coverage format: " + coverageFormat);
        }
    }

    /**
     * Choose from one of the supplied OpenType script tags, depending on what the font supports and potentially on
     * context.
     *
     * @param tags
     * @return The best OpenType script tag
     */
    private String selectScriptTag(String[] tags)
    {
        if (tags.length == 1)
        {
            String tag = tags[0];
            if (OpenTypeScript.INHERITED.equals(tag)
                    || (OpenTypeScript.TAG_DEFAULT.equals(tag) && !scriptList.containsKey(tag)))
            {
                // We don't know what script this should be.
                if (lastUsedSupportedScript == null)
                {
                    // We have no past context and (currently) no way to get future context so we guess.
                    lastUsedSupportedScript = scriptList.keySet().iterator().next();
                }
                // else use past context

                return lastUsedSupportedScript;
            }
        }
        for (String tag : tags)
        {
            if (scriptList.containsKey(tag))
            {
                // Use the first recognized tag. We assume a single font only recognizes one version ("ver. 2")
                // of a single script, or if it recognizes more than one that it prefers the latest one.
                lastUsedSupportedScript = tag;
                return lastUsedSupportedScript;
            }
        }
        return tags[0];
    }

    private Collection<LangSysTable> getLangSysTables(String scriptTag)
    {
        Collection<LangSysTable> result = Collections.emptyList();
        ScriptTable scriptTable = scriptList.get(scriptTag);
        if (scriptTable != null)
        {
            if (scriptTable.getDefaultLangSysTable() == null)
            {
                result = scriptTable.getLangSysTables().values();
            }
            else
            {
                result = new ArrayList<>(scriptTable.getLangSysTables().values());
                result.add(scriptTable.getDefaultLangSysTable());
            }
        }
        return result;
    }

    /**
     * Get a list of {@code FeatureRecord}s from a collection of {@code LangSysTable}s. Optionally
     * filter the returned features by supplying a list of allowed feature tags in
     * {@code enabledFeatures}.
     *
     * Note that features listed as required ({@code LangSysTable#requiredFeatureIndex}) will be
     * included even if not explicitly enabled.
     *
     * @param langSysTables The {@code LangSysTable}s indicating {@code FeatureRecord}s to search
     * for
     * @param enabledFeatures An optional whitelist of feature tags ({@code null} to allow all)
     * @return The indicated {@code FeatureRecord}s
     */
    private List<FeatureRecord> getFeatureRecords(Collection<LangSysTable> langSysTables,
            final List<String> enabledFeatures)
    {
        if (langSysTables.isEmpty())
        {
            return Collections.emptyList();
        }
        List<FeatureRecord> result = new ArrayList<>();
        for (LangSysTable langSysTable : langSysTables)
        {
            int required = langSysTable.getRequiredFeatureIndex();
            if (required != 0xffff) // if no required features = 0xFFFF
            {
                result.add(featureListTable.getFeatureRecords()[required]);
            }
            for (int featureIndex : langSysTable.getFeatureIndices())
            {
                if (enabledFeatures == null
                        || enabledFeatures.contains(
                                featureListTable.getFeatureRecords()[featureIndex].getFeatureTag()))
                {
                    result.add(featureListTable.getFeatureRecords()[featureIndex]);
                }
            }
        }

        // 'vrt2' supersedes 'vert' and they should not be used together
        // https://www.microsoft.com/typography/otspec/features_uz.htm
        if (containsFeature(result, "vrt2"))
        {
            removeFeature(result, "vert");
        }

        if (enabledFeatures != null && result.size() > 1)
        {
            Collections.sort(result, new Comparator<FeatureRecord>()
            {
                @Override
                public int compare(FeatureRecord o1, FeatureRecord o2)
                {
                    return Integer.compare(enabledFeatures.indexOf(o1.getFeatureTag()),
                            enabledFeatures.indexOf(o2.getFeatureTag()));
                }
            });
        }

        return result;
    }

    private boolean containsFeature(List<FeatureRecord> featureRecords, String featureTag)
    {
        for (FeatureRecord featureRecord : featureRecords)
        {
            if (featureRecord.getFeatureTag().equals(featureTag))
            {
                return true;
            }
        }
        return false;
    }

    private void removeFeature(List<FeatureRecord> featureRecords, String featureTag)
    {
        Iterator<FeatureRecord> iter = featureRecords.iterator();
        while (iter.hasNext())
        {
            if (iter.next().getFeatureTag().equals(featureTag))
            {
                iter.remove();
            }
        }
    }

    private int applyFeature(FeatureRecord featureRecord, int gid)
    {
        int lookupResult = gid;
        for (int lookupListIndex : featureRecord.getFeatureTable().getLookupListIndices())
        {
            LookupTable lookupTable = lookupListTable.getLookups()[lookupListIndex];
            if (lookupTable.getLookupType() != 1)
            {
                LOG.debug("Skipping GSUB feature '" + featureRecord.getFeatureTag()
                        + "' because it requires unsupported lookup table type "
                        + lookupTable.getLookupType());
                continue;
            }
            lookupResult = doLookup(lookupTable, lookupResult);
        }
        return lookupResult;
    }

    private int doLookup(LookupTable lookupTable, int gid)
    {
        for (LookupSubTable lookupSubtable : lookupTable.getSubTables())
        {
            int coverageIndex = lookupSubtable.getCoverageTable().getCoverageIndex(gid);
            if (coverageIndex >= 0)
            {
                return lookupSubtable.doSubstitution(gid, coverageIndex);
            }
        }
        return gid;
    }

    /**
     * Apply glyph substitutions to the supplied gid. The applicable substitutions are determined by
     * the {@code scriptTags} which indicate the language of the gid, and by the
     * {@code enabledFeatures} which acts as a whitelist.
     *
     * To ensure that a single gid isn't mapped to multiple substitutions, subsequent invocations
     * with the same gid will return the same result as the first, regardless of script or enabled
     * features.
     *
     * @param gid GID
     * @param scriptTags Script tags applicable to the gid (see {@link OpenTypeScript})
     * @param enabledFeatures Whitelist of features to apply
     */
    public int getSubstitution(int gid, String[] scriptTags, List<String> enabledFeatures)
    {
        if (gid == -1)
        {
            return -1;
        }
        Integer cached = lookupCache.get(gid);
        if (cached != null)
        {
            // Because script detection for indeterminate scripts (COMMON, INHERIT, etc.) depends on context,
            // it is possible to return a different substitution for the same input. However we don't want that,
            // as we need a one-to-one mapping.
            return cached;
        }
        String scriptTag = selectScriptTag(scriptTags);
        Collection<LangSysTable> langSysTables = getLangSysTables(scriptTag);
        List<FeatureRecord> featureRecords = getFeatureRecords(langSysTables, enabledFeatures);
        int sgid = gid;
        for (FeatureRecord featureRecord : featureRecords)
        {
            sgid = applyFeature(featureRecord, sgid);
        }
        lookupCache.put(gid, sgid);
        reverseLookup.put(sgid, gid);
        return sgid;
    }

    /**
     * For a substitute-gid (obtained from {@link #getSubstitution(int, String[], List)}), retrieve
     * the original gid.
     *
     * Only gids previously substituted by this instance can be un-substituted. If you are trying to
     * unsubstitute before you substitute, something is wrong.
     *
     * @param sgid Substitute GID
     */
    public int getUnsubstitution(int sgid)
    {
        Integer gid = reverseLookup.get(sgid);
        if (gid == null)
        {
            LOG.warn("Trying to un-substitute a never-before-seen gid: " + sgid);
            return sgid;
        }
        return gid;
    }

    public GsubData getGsubData()
    {
        return gsubData;
    }

    private RangeRecord readRangeRecord(TTFDataStream data) throws IOException
    {
        int startGlyphID = data.readUnsignedShort();
        int endGlyphID = data.readUnsignedShort();
        int startCoverageIndex = data.readUnsignedShort();
        return new RangeRecord(startGlyphID, endGlyphID, startCoverageIndex);
    }

}
