package org.apache.fontbox.ttf.table.gsub;

import org.apache.fontbox.ttf.table.common.CoverageTable;
import org.apache.fontbox.ttf.table.common.LookupSubTable;

/**
 * Lookup Type 3: Alternate Substitution Subtable
 * as described in OpenType spec: <a href="https://learn.microsoft.com/en-us/typography/opentype/spec/gsub#31-alternate-substitution-format-1">...</a>
 */
public class LookupTypeAlternateSubstitutionFormat1 extends LookupSubTable {
    private final AlternateSetTable[] alternateSetTables;

    public LookupTypeAlternateSubstitutionFormat1(
            int substFormat, CoverageTable coverageTable, AlternateSetTable[] alternateSetTables) {
        super(substFormat, coverageTable);
        this.alternateSetTables = alternateSetTables;
    }

    public AlternateSetTable[] getAlternateSetTables() {
        return alternateSetTables;
    }

    @Override
    public int doSubstitution(int gid, int coverageIndex) {
        throw new UnsupportedOperationException("not applicable");
    }
}
