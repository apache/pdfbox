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
package org.apache.fontbox.cmap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.Test;

/**
 * This will test the CMap implementation.
 *
 */
class TestCMap
{

    /**
     * Check whether the mapping is working correct.
     * @throws IOException If something went wrong during adding a mapping
     */
    @Test
    void testLookup() throws IOException
    {
        byte[] bs = new byte[] { (byte) 200 };
        CMap cMap = new CMap();
        cMap.addCharMapping(bs, "a");
        assertEquals("a", cMap.toUnicode(bs));
    }

    /**
     * PDFBOX-3997: test unicode that is above the basic multilingual plane, here: helicopter
     * symbol, or D83D DE81 in the Noto Emoji font.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox3997() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser()
                .parse(new RandomAccessReadBufferedFile("target/pdfs/NotoEmoji-Regular.ttf")))
        {
            CmapLookup cmap = ttf.getUnicodeCmapLookup(false);
            assertEquals(886, cmap.getGlyphId(0x1F681));
        }
    }
}
