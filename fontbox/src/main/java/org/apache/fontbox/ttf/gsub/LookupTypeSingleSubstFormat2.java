package org.apache.fontbox.ttf.gsub;

import java.util.Arrays;

class LookupTypeSingleSubstFormat2 extends LookupSubTable
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