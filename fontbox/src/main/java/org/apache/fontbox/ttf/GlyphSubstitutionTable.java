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
import java.util.Arrays;
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

/**
 * A glyph substitution 'GSUB' table in a TrueType or OpenType font.
 *
 * @author Aaron Madlon-Kay
 */
public class GlyphSubstitutionTable extends TTFTable
{
    private static final Log LOG = LogFactory.getLog(GlyphSubstitutionTable.class);

    public static final String TAG = "GSUB";

    private LinkedHashMap<String, ScriptTable> scriptList;
    // featureList and lookupList are not maps because we need to index into them
    private FeatureRecord[] featureList;
    private LookupTable[] lookupList;

    private final Map<Integer, Integer> lookupCache = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> reverseLookup = new HashMap<Integer, Integer>();

    private String lastUsedSupportedScript;

    GlyphSubstitutionTable(TrueTypeFont font)
    {
        super(font);
    }

    @Override
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        long start = data.getCurrentPosition();
        @SuppressWarnings("unused")
        int majorVersion = data.readUnsignedShort();
        int minorVersion = data.readUnsignedShort();
        int scriptListOffset = data.readUnsignedShort();
        int featureListOffset = data.readUnsignedShort();
        int lookupListOffset = data.readUnsignedShort();
        @SuppressWarnings("unused")
        long featureVariationsOffset = -1L;
        if (minorVersion == 1L)
        {
            featureVariationsOffset = data.readUnsignedInt();
        }

        scriptList = readScriptList(data, start + scriptListOffset);
        featureList = readFeatureList(data, start + featureListOffset);
        lookupList = readLookupList(data, start + lookupListOffset);
    }

    LinkedHashMap<String, ScriptTable> readScriptList(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int scriptCount = data.readUnsignedShort();
        ScriptRecord[] scriptRecords = new ScriptRecord[scriptCount];
        int[] scriptOffsets = new int[scriptCount];
        for (int i = 0; i < scriptCount; i++)
        {
            ScriptRecord scriptRecord = new ScriptRecord();
            scriptRecord.scriptTag = data.readString(4);
            scriptOffsets[i] = data.readUnsignedShort();
            scriptRecords[i] = scriptRecord;
        }
        for (int i = 0; i < scriptCount; i++)
        {
            scriptRecords[i].scriptTable = readScriptTable(data, offset + scriptOffsets[i]);
        }
        LinkedHashMap<String, ScriptTable> resultScriptList = new LinkedHashMap<String, ScriptTable>(scriptCount);
        for (ScriptRecord scriptRecord : scriptRecords)
        {
            resultScriptList.put(scriptRecord.scriptTag, scriptRecord.scriptTable);
        }
        return resultScriptList;
    }

    ScriptTable readScriptTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        ScriptTable scriptTable = new ScriptTable();
        int defaultLangSys = data.readUnsignedShort();
        int langSysCount = data.readUnsignedShort();
        LangSysRecord[] langSysRecords = new LangSysRecord[langSysCount];
        int[] langSysOffsets = new int[langSysCount];
        String prevLangSysTag = "";
        for (int i = 0; i < langSysCount; i++)
        {
            LangSysRecord langSysRecord = new LangSysRecord();
            langSysRecord.langSysTag = data.readString(4);
            if (i > 0 && langSysRecord.langSysTag.compareTo(prevLangSysTag) <= 0)
            {
                // PDFBOX-4489: catch corrupt file
                // https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2#slTbl_sRec
                throw new IOException("LangSysRecords not alphabetically sorted by LangSys tag: " +
                          langSysRecord.langSysTag + " <= " + prevLangSysTag);
            }
            langSysOffsets[i] = data.readUnsignedShort();
            langSysRecords[i] = langSysRecord;
            prevLangSysTag = langSysRecord.langSysTag;
        }
        if (defaultLangSys != 0)
        {
            scriptTable.defaultLangSysTable = readLangSysTable(data, offset + defaultLangSys);
        }
        for (int i = 0; i < langSysCount; i++)
        {
            langSysRecords[i].langSysTable = readLangSysTable(data, offset + langSysOffsets[i]);
        }
        scriptTable.langSysTables = new LinkedHashMap<String, LangSysTable>(langSysCount);
        for (LangSysRecord langSysRecord : langSysRecords)
        {
            scriptTable.langSysTables.put(langSysRecord.langSysTag, langSysRecord.langSysTable);
        }
        return scriptTable;
    }

    LangSysTable readLangSysTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        LangSysTable langSysTable = new LangSysTable();
        @SuppressWarnings("unused")
        int lookupOrder = data.readUnsignedShort();
        langSysTable.requiredFeatureIndex = data.readUnsignedShort();
        int featureIndexCount = data.readUnsignedShort();
        langSysTable.featureIndices = new int[featureIndexCount];
        for (int i = 0; i < featureIndexCount; i++)
        {
            langSysTable.featureIndices[i] = data.readUnsignedShort();
        }
        return langSysTable;
    }

    FeatureRecord[] readFeatureList(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int featureCount = data.readUnsignedShort();
        FeatureRecord[] featureRecords = new FeatureRecord[featureCount];
        int[] featureOffsets = new int[featureCount];
        String prevFeatureTag = "";
        for (int i = 0; i < featureCount; i++)
        {
            FeatureRecord featureRecord = new FeatureRecord();
            featureRecord.featureTag = data.readString(4);
            if (i > 0 && featureRecord.featureTag.compareTo(prevFeatureTag) < 0)
            {
                // catch corrupt file
                // https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2#flTbl
                LOG.error("FeatureRecord array not alphabetically sorted by FeatureTag: " +
                           featureRecord.featureTag + " < " + prevFeatureTag);
                return new FeatureRecord[0];
            }
            featureOffsets[i] = data.readUnsignedShort();
            featureRecords[i] = featureRecord;
            prevFeatureTag = featureRecord.featureTag;
        }
        for (int i = 0; i < featureCount; i++)
        {
            featureRecords[i].featureTable = readFeatureTable(data, offset + featureOffsets[i]);
        }
        return featureRecords;
    }

    FeatureTable readFeatureTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        FeatureTable featureTable = new FeatureTable();
        @SuppressWarnings("unused")
        int featureParams = data.readUnsignedShort();
        int lookupIndexCount = data.readUnsignedShort();
        featureTable.lookupListIndices = new int[lookupIndexCount];
        for (int i = 0; i < lookupIndexCount; i++)
        {
            featureTable.lookupListIndices[i] = data.readUnsignedShort();
        }
        return featureTable;
    }

    LookupTable[] readLookupList(TTFDataStream data, long offset) throws IOException
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
        return lookupTables;
    }

    LookupTable readLookupTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        LookupTable lookupTable = new LookupTable();
        lookupTable.lookupType = data.readUnsignedShort();
        lookupTable.lookupFlag = data.readUnsignedShort();
        int subTableCount = data.readUnsignedShort();
        int[] subTableOffets = new int[subTableCount];
        for (int i = 0; i < subTableCount; i++)
        {
            subTableOffets[i] = data.readUnsignedShort();
        }
        if ((lookupTable.lookupFlag & 0x0010) != 0)
        {
            lookupTable.markFilteringSet = data.readUnsignedShort();
        }
        lookupTable.subTables = new LookupSubTable[subTableCount];
        switch (lookupTable.lookupType)
        {
        case 1: // Single
            for (int i = 0; i < subTableCount; i++)
            {
                lookupTable.subTables[i] = readLookupSubTable(data, offset + subTableOffets[i]);
            }
            break;
        default:
            // Other lookup types are not supported
            LOG.debug("Type " + lookupTable.lookupType + " GSUB lookup table is not supported and will be ignored");
        }
        return lookupTable;
    }

    LookupSubTable readLookupSubTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();
        switch (substFormat)
        {
        case 1:
        {
            LookupTypeSingleSubstFormat1 lookupSubTable = new LookupTypeSingleSubstFormat1();
            lookupSubTable.substFormat = substFormat;
            int coverageOffset = data.readUnsignedShort();
            lookupSubTable.deltaGlyphID = data.readSignedShort();
            lookupSubTable.coverageTable = readCoverageTable(data, offset + coverageOffset);
            return lookupSubTable;
        }
        case 2:
        {
            LookupTypeSingleSubstFormat2 lookupSubTable = new LookupTypeSingleSubstFormat2();
            lookupSubTable.substFormat = substFormat;
            int coverageOffset = data.readUnsignedShort();
            int glyphCount = data.readUnsignedShort();
            lookupSubTable.substituteGlyphIDs = new int[glyphCount];
            for (int i = 0; i < glyphCount; i++)
            {
                lookupSubTable.substituteGlyphIDs[i] = data.readUnsignedShort();
            }
            lookupSubTable.coverageTable = readCoverageTable(data, offset + coverageOffset);
            return lookupSubTable;
        }
        default:
            throw new IOException("Unknown substFormat: " + substFormat);
        }
    }

    CoverageTable readCoverageTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int coverageFormat = data.readUnsignedShort();
        switch (coverageFormat)
        {
        case 1:
        {
            CoverageTableFormat1 coverageTable = new CoverageTableFormat1();
            coverageTable.coverageFormat = coverageFormat;
            int glyphCount = data.readUnsignedShort();
            coverageTable.glyphArray = new int[glyphCount];
            for (int i = 0; i < glyphCount; i++)
            {
                coverageTable.glyphArray[i] = data.readUnsignedShort();
            }
            return coverageTable;
        }
        case 2:
        {
            CoverageTableFormat2 coverageTable = new CoverageTableFormat2();
            coverageTable.coverageFormat = coverageFormat;
            int rangeCount = data.readUnsignedShort();
            coverageTable.rangeRecords = new RangeRecord[rangeCount];
            for (int i = 0; i < rangeCount; i++)
            {
                coverageTable.rangeRecords[i] = readRangeRecord(data);
            }
            return coverageTable;

        }
        default:
            // Should not happen (the spec indicates only format 1 and format 2)
            throw new IOException("Unknown coverage format: " + coverageFormat);
        }
    }


    /**
     * Choose from one of the supplied OpenType script tags, depending on what the font supports and
     * potentially on context.
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
            if (scriptTable.defaultLangSysTable == null)
            {
                result = scriptTable.langSysTables.values();
            }
            else
            {
                result = new ArrayList<LangSysTable>(scriptTable.langSysTables.values());
                result.add(scriptTable.defaultLangSysTable);
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
        List<FeatureRecord> result = new ArrayList<FeatureRecord>();
        for (LangSysTable langSysTable : langSysTables)
        {
            int required = langSysTable.requiredFeatureIndex;
            if (required != 0xffff) // if no required features = 0xFFFF
            {
                result.add(featureList[required]);
            }
            for (int featureIndex : langSysTable.featureIndices)
            {
                if (enabledFeatures == null
                        || enabledFeatures.contains(featureList[featureIndex].featureTag))
                {
                    result.add(featureList[featureIndex]);
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
                    int i1 = enabledFeatures.indexOf(o1.featureTag);
                    int i2 = enabledFeatures.indexOf(o2.featureTag);
                    return i1 < i2 ? -1 : i1 == i2 ? 0 : 1;
                }
            });
        }

        return result;
    }

    private boolean containsFeature(List<FeatureRecord> featureRecords, String featureTag)
    {
        for (FeatureRecord featureRecord : featureRecords)
        {
            if (featureRecord.featureTag.equals(featureTag))
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
            if (iter.next().featureTag.equals(featureTag))
            {
                iter.remove();
            }
        }
    }

    private int applyFeature(FeatureRecord featureRecord, int gid)
    {
        for (int lookupListIndex : featureRecord.featureTable.lookupListIndices)
        {
            LookupTable lookupTable = lookupList[lookupListIndex];
            if (lookupTable.lookupType != 1)
            {
                LOG.debug("Skipping GSUB feature '" + featureRecord.featureTag
                        + "' because it requires unsupported lookup table type " + lookupTable.lookupType);
                continue;
            }
            gid = doLookup(lookupTable, gid);
        }
        return gid;
    }

    private int doLookup(LookupTable lookupTable, int gid)
    {
        for (LookupSubTable lookupSubtable : lookupTable.subTables)
        {
            int coverageIndex = lookupSubtable.coverageTable.getCoverageIndex(gid);
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

    RangeRecord readRangeRecord(TTFDataStream data) throws IOException
    {
        RangeRecord rangeRecord = new RangeRecord();
        rangeRecord.startGlyphID = data.readUnsignedShort();
        rangeRecord.endGlyphID = data.readUnsignedShort();
        rangeRecord.startCoverageIndex = data.readUnsignedShort();
        return rangeRecord;
    }

    static class ScriptRecord
    {
        // https://www.microsoft.com/typography/otspec/scripttags.htm
        String scriptTag;
        ScriptTable scriptTable;

        @Override
        public String toString()
        {
            return String.format("ScriptRecord[scriptTag=%s]", scriptTag);
        }
    }

    static class ScriptTable
    {
        LangSysTable defaultLangSysTable;
        LinkedHashMap<String, LangSysTable> langSysTables;

        @Override
        public String toString()
        {
            return String.format("ScriptTable[hasDefault=%s,langSysRecordsCount=%d]",
                    defaultLangSysTable != null, langSysTables.size());
        }
    }

    static class LangSysRecord
    {
        // https://www.microsoft.com/typography/otspec/languagetags.htm
        String langSysTag;
        LangSysTable langSysTable;

        @Override
        public String toString()
        {
            return String.format("LangSysRecord[langSysTag=%s]", langSysTag);
        }
    }

    static class LangSysTable
    {
        int requiredFeatureIndex;
        int[] featureIndices;

        @Override
        public String toString()
        {
            return String.format("LangSysTable[requiredFeatureIndex=%d]", requiredFeatureIndex);
        }
    }

    static class FeatureRecord
    {
        String featureTag;
        FeatureTable featureTable;

        @Override
        public String toString()
        {
            return String.format("FeatureRecord[featureTag=%s]", featureTag);
        }
    }

    static class FeatureTable
    {
        int[] lookupListIndices;

        @Override
        public String toString()
        {
            return String.format("FeatureTable[lookupListIndiciesCount=%d]",
                    lookupListIndices.length);
        }
    }

    static class LookupTable
    {
        int lookupType;
        int lookupFlag;
        int markFilteringSet;
        LookupSubTable[] subTables;

        @Override
        public String toString()
        {
            return String.format("LookupTable[lookupType=%d,lookupFlag=%d,markFilteringSet=%d]",
                    lookupType, lookupFlag, markFilteringSet);
        }
    }

    static abstract class LookupSubTable
    {
        int substFormat;
        CoverageTable coverageTable;

        abstract int doSubstitution(int gid, int coverageIndex);
    }

    static class LookupTypeSingleSubstFormat1 extends LookupSubTable
    {
        short deltaGlyphID;

        @Override
        int doSubstitution(int gid, int coverageIndex)
        {
            return coverageIndex < 0 ? gid : gid + deltaGlyphID;
        }

        @Override
        public String toString()
        {
            return String.format("LookupTypeSingleSubstFormat1[substFormat=%d,deltaGlyphID=%d]",
                    substFormat, deltaGlyphID);
        }
    }

    static class LookupTypeSingleSubstFormat2 extends LookupSubTable
    {
        int[] substituteGlyphIDs;

        @Override
        int doSubstitution(int gid, int coverageIndex)
        {
            return coverageIndex < 0 ? gid : substituteGlyphIDs[coverageIndex];
        }

        @Override
        public String toString()
        {
            return String.format(
                    "LookupTypeSingleSubstFormat2[substFormat=%d,substituteGlyphIDs=%s]",
                    substFormat, Arrays.toString(substituteGlyphIDs));
        }
    }

    static abstract class CoverageTable
    {
        int coverageFormat;

        abstract int getCoverageIndex(int gid);
    }

    static class CoverageTableFormat1 extends CoverageTable
    {
        int[] glyphArray;

        @Override
        int getCoverageIndex(int gid)
        {
            return Arrays.binarySearch(glyphArray, gid);
        }

        @Override
        public String toString()
        {
            return String.format("CoverageTableFormat1[coverageFormat=%d,glyphArray=%s]",
                    coverageFormat, Arrays.toString(glyphArray));
        }
    }

    static class CoverageTableFormat2 extends CoverageTable
    {
        RangeRecord[] rangeRecords;

        @Override
        int getCoverageIndex(int gid)
        {
            for (RangeRecord rangeRecord : rangeRecords)
            {
                if (rangeRecord.startGlyphID <= gid && gid <= rangeRecord.endGlyphID)
                {
                    return rangeRecord.startCoverageIndex + gid - rangeRecord.startGlyphID;
                }
            }
            return -1;
        }

        @Override
        public String toString()
        {
            return String.format("CoverageTableFormat2[coverageFormat=%d]", coverageFormat);
        }
    }

    static class RangeRecord
    {
        int startGlyphID;
        int endGlyphID;
        int startCoverageIndex;

        @Override
        public String toString()
        {
            return String.format("RangeRecord[startGlyphID=%d,endGlyphID=%d,startCoverageIndex=%d]",
                    startGlyphID, endGlyphID, startCoverageIndex);
        }
    }
}
