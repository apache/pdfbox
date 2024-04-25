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
