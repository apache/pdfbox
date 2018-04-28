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

import org.apache.fontbox.ttf.gsub.GSUBTablePrintUtil;
import org.apache.fontbox.ttf.model.GsubData;
import org.junit.Test;

/**
 * This class is to be used mainly for debugging purposes. Prints the GSUB Feature table for debugging.
 * 
 * @author Palash Ray
 */
public class GSUBTableDebugger
{

    private static final String LOHIT_BENGALI_FONT_FILE = "/ttf/Lohit-Bengali.ttf";

    @Test
    public void printLohitBengaliTTF() throws IOException
    {
        MemoryTTFDataStream memoryTTFDataStream = new MemoryTTFDataStream(
                GSUBTableDebugger.class.getResourceAsStream(LOHIT_BENGALI_FONT_FILE));

        memoryTTFDataStream.seek(GlyphSubstitutionTableTest.DATA_POSITION_FOR_GSUB_TABLE);

        GlyphSubstitutionTable glyphSubstitutionTable = new GlyphSubstitutionTable(null);

        glyphSubstitutionTable.read(null, memoryTTFDataStream);

        TrueTypeFont trueTypeFont = new TTFParser()
                .parse(GSUBTableDebugger.class.getResourceAsStream(LOHIT_BENGALI_FONT_FILE));

        GsubData gsubData = glyphSubstitutionTable.getGsubData();
        new GSUBTablePrintUtil().printCharacterToGlyph(gsubData,
                trueTypeFont.getUnicodeCmapLookup());
    }

}
