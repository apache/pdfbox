package org.apache.fontbox.ttf.gsub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.CmapLookup;

public class GlyphSubstitutionDataExtractor
{

    private static final Log LOG = LogFactory.getLog(GlyphSubstitutionDataExtractor.class);

    public Map<String, Integer> getStringToCompoundGlyph(
            Map<Integer, List<Integer>> rawGSubTableData,
            CmapLookup cmap)
    {
        Map<String, Integer> substitutionData = new HashMap<>();

        for (Integer glyphToBeSubstituted : rawGSubTableData.keySet())
        {
            List<Integer> glyphIDsToBeReplaced = rawGSubTableData.get(glyphToBeSubstituted);

            String unicodeText = getUnicodeString(rawGSubTableData, cmap, glyphIDsToBeReplaced);
            substitutionData.put(unicodeText, glyphToBeSubstituted);
        }

        return Collections.unmodifiableMap(substitutionData);

    }

    Map<Integer, List<Integer>> extractRawGSubTableData(LookupTable[] lookupTables)
    {

        Map<Integer, List<Integer>> glyphSubstitutionMap = new HashMap<>();

        for (LookupTable lookupTable : lookupTables)
        {
            extractData(glyphSubstitutionMap, lookupTable);
        }

        return Collections.unmodifiableMap(glyphSubstitutionMap);
    }

    private String getUnicodeChar(Map<Integer, List<Integer>> rawGSubTableData, CmapLookup cmap,
            Integer glyphId)
    {
        List<Integer> keyChars = cmap.getCharCodes(glyphId);

        // its a compound glyph
        if (keyChars == null)
        {
            List<Integer> constituentGlyphs = rawGSubTableData.get(glyphId);

            if (constituentGlyphs == null || constituentGlyphs.isEmpty())
            {
                LOG.warn("lookup for the glyphId: " + glyphId
                        + " failed, as no corresponding Unicode char found mapped to it");
                return "";
            }
            else
            {
                return getUnicodeString(rawGSubTableData, cmap, constituentGlyphs);
            }

        }
        else
        {
            StringBuilder sb = new StringBuilder();
            for (int unicodeChar : keyChars)
            {
                sb.append((char) unicodeChar);
            }
            return sb.toString();
        }

    }

    private String getUnicodeString(Map<Integer, List<Integer>> rawGSubTableData, CmapLookup cmap,
            List<Integer> glyphIDs)
    {
        StringBuilder sb = new StringBuilder();
        for (Integer glyphId : glyphIDs)
        {
            sb.append(getUnicodeChar(rawGSubTableData, cmap, glyphId));
        }
        return sb.toString();
    }

    private void extractData(Map<Integer, List<Integer>> glyphSubstitutionMap,
            LookupTable lookupTable)
    {

        for (LookupSubTable lookupSubTable : lookupTable.subTables)
        {
            if (lookupSubTable instanceof LookupTypeLigatureSubstitutionSubstFormat1)
            {
                extractDataFromLigatureSubstitutionSubstFormat1Table(glyphSubstitutionMap,
                        (LookupTypeLigatureSubstitutionSubstFormat1) lookupSubTable);
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
            else
            {
                LOG.warn("The type " + lookupSubTable + " is not yet supported, will be ignored");
            }
        }

    }

    private void extractDataFromSingleSubstTableFormat1Table(
            Map<Integer, List<Integer>> glyphSubstitutionMap,
            LookupTypeSingleSubstFormat1 singleSubstTableFormat1)
    {
        CoverageTable coverageTable = singleSubstTableFormat1.coverageTable;
        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = coverageGlyphId + singleSubstTableFormat1.deltaGlyphID;
            putNewSubstitutionEntry(glyphSubstitutionMap, substituteGlyphId,
                    Arrays.asList(coverageGlyphId));
        }
    }

    private void extractDataFromSingleSubstTableFormat2Table(
            Map<Integer, List<Integer>> glyphSubstitutionMap,
            LookupTypeSingleSubstFormat2 singleSubstTableFormat2)
    {

        CoverageTable coverageTable = singleSubstTableFormat2.coverageTable;

        if (coverageTable.getSize() != singleSubstTableFormat2.substituteGlyphIDs.length)
        {
            throw new IllegalArgumentException(
                    "The no. coverage table entries should be the same as the size of the substituteGlyphIDs");
        }

        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = coverageGlyphId + singleSubstTableFormat2.substituteGlyphIDs[i];
            putNewSubstitutionEntry(glyphSubstitutionMap, substituteGlyphId,
                    Arrays.asList(coverageGlyphId));
        }
    }

    private void extractDataFromLigatureSubstitutionSubstFormat1Table(
            Map<Integer, List<Integer>> glyphSubstitutionMap,
            LookupTypeLigatureSubstitutionSubstFormat1 ligatureSubstitutionTable)
    {

        for (LigatureSetTable ligatureSetTable : ligatureSubstitutionTable.ligatureSetTables)
        {
            for (LigatureTable ligatureTable : ligatureSetTable.ligatureTables)
            {
                extractDataFromLigatureTable(glyphSubstitutionMap, ligatureTable);
            }

        }

    }

    private void extractDataFromLigatureTable(Map<Integer, List<Integer>> glyphSubstitutionMap,
            LigatureTable ligatureTable)
    {

        List<Integer> glyphsToBeSubstituted = new ArrayList<>();

        for (int componentGlyphID : ligatureTable.componentGlyphIDs)
        {
            glyphsToBeSubstituted.add(componentGlyphID);
        }

        LOG.debug("glyphsToBeSubstituted: " + glyphsToBeSubstituted);

        putNewSubstitutionEntry(glyphSubstitutionMap, ligatureTable.ligatureGlyph,
                glyphsToBeSubstituted);

    }

    private void putNewSubstitutionEntry(Map<Integer, List<Integer>> glyphSubstitutionMap,
            int newGlyph, List<Integer> glyphsToBeSubstituted)
    {
        List<Integer> oldValue = glyphSubstitutionMap.put(newGlyph, glyphsToBeSubstituted);

        if (oldValue != null)
        {
            LOG.warn("oldValue: " + oldValue + " will be overridden with newValue: "
                    + glyphsToBeSubstituted);
        }
    }

}
