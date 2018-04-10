package org.apache.fontbox.ttf.gsub;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GlyphSubstitutionDataExtractor
{

    private static final Log LOG = LogFactory.getLog(GlyphSubstitutionDataExtractor.class);


    private Map<String, Integer> glyphSubstitutionMap;

    private int maxGlyphsToBeSubstituted = -1;

    Map<String, Integer> getGlyphSubstitutionMap()
    {
        if (glyphSubstitutionMap == null || maxGlyphsToBeSubstituted == -1)
        {
            throw new IllegalStateException("data not initialized!");
        }
        return Collections.unmodifiableMap(glyphSubstitutionMap);
    }

    int getMaxGlyphsToBeSubstituted()
    {
        if (glyphSubstitutionMap == null || maxGlyphsToBeSubstituted == -1)
        {
            throw new IllegalStateException("data not initialized!");
        }
        return maxGlyphsToBeSubstituted;
    }

    void populateData(LookupTable[] lookupTables)
    {
        if (glyphSubstitutionMap != null || maxGlyphsToBeSubstituted != -1)
        {
            throw new IllegalStateException(
                    "when this method is called, all the state is expected to be un-initialized!");
        }

        glyphSubstitutionMap = new HashMap<>();
        maxGlyphsToBeSubstituted = 0;

        for (LookupTable lookupTable : lookupTables)
        {
            extractData(lookupTable);
        }
    }

    private void extractData(LookupTable lookupTable)
    {

        for (LookupSubTable lookupSubTable : lookupTable.subTables)
        {
            if (lookupSubTable instanceof LookupTypeLigatureSubstitutionSubstFormat1)
            {
                extractDataFromLigatureSubstitutionSubstFormat1Table(
                        (LookupTypeLigatureSubstitutionSubstFormat1) lookupSubTable);
            }
            else if (lookupSubTable instanceof LookupTypeSingleSubstFormat1)
            {
                extractDataFromSingleSubstTableFormat1Table(
                        (LookupTypeSingleSubstFormat1) lookupSubTable);
            }
            else if (lookupSubTable instanceof LookupTypeSingleSubstFormat2)
            {
                extractDataFromSingleSubstTableFormat2Table(
                        (LookupTypeSingleSubstFormat2) lookupSubTable);
            }
            else
            {
                LOG.warn("The type " + lookupSubTable.getClass()
                        + " is not yet supported, will be ignored");
            }
        }

    }

    private void extractDataFromSingleSubstTableFormat1Table(
            LookupTypeSingleSubstFormat1 singleSubstTableFormat1)
    {
        compareAndSetMaxGlyphsToBeSubstituted(1);
        CoverageTable coverageTable = singleSubstTableFormat1.coverageTable;
        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = coverageGlyphId + singleSubstTableFormat1.deltaGlyphID;
            putNewSubstitutionEntry(Integer.toString(coverageGlyphId), substituteGlyphId);
        }
    }

    private void extractDataFromSingleSubstTableFormat2Table(
            LookupTypeSingleSubstFormat2 singleSubstTableFormat2)
    {

        CoverageTable coverageTable = singleSubstTableFormat2.coverageTable;

        if (coverageTable.getSize() != singleSubstTableFormat2.substituteGlyphIDs.length)
        {
            throw new IllegalArgumentException(
                    "The no. coverage table entries should be the same as the size of the substituteGlyphIDs");
        }

        compareAndSetMaxGlyphsToBeSubstituted(1);

        for (int i = 0; i < coverageTable.getSize(); i++)
        {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = coverageGlyphId + singleSubstTableFormat2.substituteGlyphIDs[i];
            putNewSubstitutionEntry(Integer.toString(coverageGlyphId), substituteGlyphId);
        }
    }

    private void extractDataFromLigatureSubstitutionSubstFormat1Table(
            LookupTypeLigatureSubstitutionSubstFormat1 ligatureSubstitutionTable)
    {

        for (LigatureSetTable ligatureSetTable : ligatureSubstitutionTable.ligatureSetTables)
        {
            for (LigatureTable ligatureTable : ligatureSetTable.ligatureTables)
            {
                extractDataFromLigatureTable(ligatureTable);
            }

        }

    }

    private void extractDataFromLigatureTable(LigatureTable ligatureTable)
    {

        LOG.debug("componentCount: " + ligatureTable.componentCount);

        compareAndSetMaxGlyphsToBeSubstituted(ligatureTable.componentCount);

        StringBuilder sb = new StringBuilder(100);

        for (int componentGlyphID : ligatureTable.componentGlyphIDs)
        {
            sb.append(componentGlyphID).append(GlyphSubstitutionTable.GLYPH_ID_SEPARATOR);
        }

        sb.setLength(sb.length() - 1);

        String key = sb.toString();

        LOG.debug("key: " + key);

        putNewSubstitutionEntry(key, ligatureTable.ligatureGlyph);

    }

    private void putNewSubstitutionEntry(String key, int value)
    {
        Integer oldValue = glyphSubstitutionMap.put(key, value);

        if (oldValue != null)
        {
            LOG.warn("oldValue: " + oldValue + " will be overridden with newValue: " + value);
        }
    }

    private void compareAndSetMaxGlyphsToBeSubstituted(int newVlaue)
    {
        if (maxGlyphsToBeSubstituted < newVlaue)
        {
            maxGlyphsToBeSubstituted = newVlaue;

        }
    }

}
