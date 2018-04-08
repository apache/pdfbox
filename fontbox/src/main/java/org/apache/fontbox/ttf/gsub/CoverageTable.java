package org.apache.fontbox.ttf.gsub;

abstract class CoverageTable
{
    int coverageFormat;

    abstract int getCoverageIndex(int gid);

    abstract int getGlyphId(int index);

    abstract int getSize();
}