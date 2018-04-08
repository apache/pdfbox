package org.apache.fontbox.ttf.gsub;

class LangSysTable
{
    int requiredFeatureIndex;
    int[] featureIndices;

    @Override
    public String toString()
    {
        return String.format("LangSysTable[requiredFeatureIndex=%d]", requiredFeatureIndex);
    }
}