package org.apache.fontbox.ttf.gsub;

abstract class LookupSubTable
{
    int substFormat;
    CoverageTable coverageTable;

    abstract int doSubstitution(int gid, int coverageIndex);
}