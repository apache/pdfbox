package org.apache.fontbox.ttf.gsub;

class CoverageTableFormat2 extends CoverageTableFormat1
{
    RangeRecord[] rangeRecords;

    @Override
    public String toString()
    {
        return String.format("CoverageTableFormat2[coverageFormat=%d]", coverageFormat);
    }
}