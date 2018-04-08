package org.apache.fontbox.ttf.gsub;

class FeatureTable
{
    int[] lookupListIndices;

    @Override
    public String toString()
    {
        return String.format("FeatureTable[lookupListIndiciesCount=%d]",
                lookupListIndices.length);
    }
}