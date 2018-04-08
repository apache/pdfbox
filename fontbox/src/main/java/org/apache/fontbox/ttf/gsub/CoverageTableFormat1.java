package org.apache.fontbox.ttf.gsub;

import java.util.Arrays;

class CoverageTableFormat1 extends CoverageTable
{
    int[] glyphArray;

    @Override
    int getCoverageIndex(int gid)
    {
        return Arrays.binarySearch(glyphArray, gid);
    }

    @Override
    int getGlyphId(int index)
    {
        return glyphArray[index];
    }

    @Override
    int getSize()
    {
        return glyphArray.length;
    }

    @Override
    public String toString()
    {
        return String.format("CoverageTableFormat1[coverageFormat=%d,glyphArray=%s]",
                coverageFormat, Arrays.toString(glyphArray));
    }


}