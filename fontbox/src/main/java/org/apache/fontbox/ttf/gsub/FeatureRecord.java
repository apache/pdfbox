package org.apache.fontbox.ttf.gsub;

class FeatureRecord
{
    String featureTag;
    FeatureTable featureTable;

    @Override
    public String toString()
    {
        return String.format("FeatureRecord[featureTag=%s]", featureTag);
    }
}