package org.apache.fontbox.ttf.gsub;

class LookupTable
{
    int lookupType;
    int lookupFlag;
    int markFilteringSet;
    LookupSubTable[] subTables;

    @Override
    public String toString()
    {
        return String.format("LookupTable[lookupType=%d,lookupFlag=%d,markFilteringSet=%d]",
                lookupType, lookupFlag, markFilteringSet);
    }
}