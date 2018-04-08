package org.apache.fontbox.ttf.gsub;

class LookupTypeLigatureSubstitutionSubstFormat1 extends LookupSubTable
{
    LigatureSetTable[] ligatureSetTables;

    @Override
    int doSubstitution(int gid, int coverageIndex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        return String.format("%s[substFormat=%d]",
                LookupTypeLigatureSubstitutionSubstFormat1.class.getSimpleName(), substFormat);
    }
}