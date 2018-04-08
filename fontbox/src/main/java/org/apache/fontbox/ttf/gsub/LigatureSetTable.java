package org.apache.fontbox.ttf.gsub;

class LigatureSetTable
{
    int ligatureCount;
    LigatureTable[] ligatureTables;

    @Override
    public String toString()
    {
        return String.format("%s[ligatureCount=%d]", LigatureSetTable.class.getSimpleName(),
                ligatureCount);
    }
}