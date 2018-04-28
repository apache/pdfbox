package org.apache.fontbox.ttf.gsub;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.ttf.GlyphSubstitutionTable;
import org.apache.fontbox.ttf.GlyphSubstitutionTableTest;
import org.junit.Before;
import org.junit.Test;

public class GSUBTableDebugger
{

    private GlyphSubstitutionTable glyphSubstitutionTable;

    @Before
    public void init() throws IOException
    {
        glyphSubstitutionTable = GlyphSubstitutionTableTest
                .initGlyphSubstitutionTableWithLohitBengali();
    }

    @Test
    public void print()
    {
        GSUBTableDebugUtil gsubTableDebugUtil = new GSUBTableDebugUtil();
        Map<Integer, List<Integer>> rawGsubData = gsubTableDebugUtil
                .extractRawGSubTableData(glyphSubstitutionTable.getLookupListTable());
        // gsubTableDebugUtil.getStringToCompoundGlyph(rawGsubData, glyphSubstitutionTable.);

    }

}
