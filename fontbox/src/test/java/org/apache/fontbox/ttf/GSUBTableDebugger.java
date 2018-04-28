/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fontbox.ttf;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * This class is to be used mainly for debugging purposes
 */
public class GSUBTableDebugger
{

    private static final String LOHIT_BENGALI_FONT_FILE = "/ttf/Lohit-Bengali.ttf";

    private GlyphSubstitutionTable glyphSubstitutionTable;
    private TrueTypeFont trueTypeFont;

    @Before
    public void init() throws IOException
    {
        MemoryTTFDataStream memoryTTFDataStream = new MemoryTTFDataStream(
                GSUBTableDebugger.class.getResourceAsStream(LOHIT_BENGALI_FONT_FILE));

        memoryTTFDataStream.seek(GlyphSubstitutionTableTest.DATA_POSITION_FOR_GSUB_TABLE);

        glyphSubstitutionTable = new GlyphSubstitutionTable(null);

        glyphSubstitutionTable.read(null, memoryTTFDataStream);

        trueTypeFont = new TTFParser()
                .parse(GSUBTableDebugger.class.getResourceAsStream(LOHIT_BENGALI_FONT_FILE));

    }

    @Test
    public void print() throws IOException
    {
        GSUBTableDebugUtil gsubTableDebugUtil = new GSUBTableDebugUtil();
        Map<Integer, List<Integer>> rawGsubData = gsubTableDebugUtil
                .extractRawGSubTableData(glyphSubstitutionTable.getLookupListTable());
        System.out.println("----------------------rawGsubData:\n" + rawGsubData);
        Map<String, Integer> substitutionData = gsubTableDebugUtil
                .getStringToCompoundGlyph(rawGsubData, trueTypeFont.getUnicodeCmapLookup());
        System.out.println("----------------------substitutionData:\n" + substitutionData);

    }

}
