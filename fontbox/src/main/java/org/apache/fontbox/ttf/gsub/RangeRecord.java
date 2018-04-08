package org.apache.fontbox.ttf.gsub;

class RangeRecord
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