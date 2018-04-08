package org.apache.fontbox.ttf.gsub;

class LookupTypeSingleSubstFormat1 extends LookupSubTable
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