package org.apache.fontbox.ttf.gsub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GlyphSubstitutionDataExtractor
{

    private static final Log LOG = LogFactory.getLog(GlyphSubstitutionDataExtractor.class);

    Map<Integer, List<Integer>> populateData(LookupTable[] lookupTables)
    {

        Map<Integer, List<Integer>> glyphSubstitutionMap = new HashMap<>();

        for (LookupTable lookupTable : lookupTables)
        {
            extractData(glyphSubstitutionMap, lookupTable);
        }

        return glyphSubstitutionMap;
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
