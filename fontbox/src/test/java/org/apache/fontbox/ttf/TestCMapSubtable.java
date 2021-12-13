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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class TestCMapSubtable
{
    /**
     * Test that we get multiple encodings from cmap table
     */
    @Test
    void testPDFBox5328() throws IOException
    {
        List<Integer> expectedCharCodes = Arrays.asList(19981, 63847);
        int gid = 8712;
        File fontFile = new File("target/pdfs", "NotoSansSC-Regular.otf");
        OTFParser otfParser = new OTFParser(false);
        OpenTypeFont otf = otfParser.parse(fontFile);

        CmapLookup unicodeCmapLookup = otf.getUnicodeCmapLookup();
        List<Integer> charCodes = unicodeCmapLookup.getCharCodes(gid);
        Assertions.assertEquals(expectedCharCodes, charCodes);

        CmapTable cmapTable = otf.getCmap();

        CmapSubtable unicodeFullCmapTable = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE, CmapTable.ENCODING_UNICODE_2_0_FULL);
        CmapSubtable unicodeBmpCmapTable = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE, CmapTable.ENCODING_UNICODE_2_0_BMP);

        List<Integer> unicodeBmpCharCodes = unicodeBmpCmapTable.getCharCodes(gid);
        List<Integer> unicodeFullCharCodes = unicodeFullCmapTable.getCharCodes(gid);

        Assertions.assertEquals(expectedCharCodes, unicodeBmpCharCodes);
        Assertions.assertEquals(expectedCharCodes, unicodeFullCharCodes);
    }
}
