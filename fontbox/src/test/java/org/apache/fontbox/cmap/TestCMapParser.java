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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.Test;

/**
 * This will test the CMapParser implementation.
 *
 */
class TestCMapParser
{

    /**
     * Check whether the parser and the resulting mapping is working correct.
     *
     * @throws IOException If something went wrong
     */
    @Test
    void testLookup() throws IOException
    {
        final String resourceDir = "src/test/resources/cmap";
        File inDir = new File(resourceDir);

        CMap cMap = new CMapParser()
                .parse(new RandomAccessReadBufferedFile(new File(inDir, "CMapTest")));

        // char mappings
        byte[] bytes1 = {0, 1};
        assertEquals("A", cMap.toUnicode(bytes1), "bytes 00 01 from bfrange <0001> <0005> <0041>");

        byte[] bytes2 = {1, 00};
        String str2 = "0";
        assertEquals(str2,
                cMap.toUnicode(bytes2), "bytes 01 00 from bfrange <0100> <0109> <0030>");

        byte[] bytes3 = { 1, 32 };
        assertEquals("P", cMap.toUnicode(bytes3), "bytes 01 00 from bfrange <0100> <0109> <0030>");

        byte[] bytes4 = { 1, 33 };
        assertEquals("R", cMap.toUnicode(bytes4), "bytes 01 00 from bfrange <0100> <0109> <0030>");

        byte[] bytes5 = { 0, 10 };
        String str5 = "*";
        assertEquals(str5, cMap.toUnicode(bytes5), "bytes 00 0A from bfchar <000A> <002A>");

        byte[] bytes6 = { 1, 10 };
        String str6 = "+";
        assertEquals(str6, cMap.toUnicode(bytes6), "bytes 01 0A from bfchar <010A> <002B>");

        // CID mappings
        byte[] cid1 = { 0, 65 };
        assertEquals(65, cMap.toCID(cid1), "CID 65 from cidrange <0000> <00ff> 0 ");

        byte[] cid2 = { 1, 24 };
        int strCID2 = 0x0118;
        assertEquals(strCID2, cMap.toCID(cid2), "CID 280 from cidrange <0100> <01ff> 256");

        byte[] cid3 = { 2, 8 };
        int strCID3 = 0x0208;
        assertEquals(strCID3, cMap.toCID(cid3), "CID 520 from cidchar <0208> 520");

        byte[] cid4 = { 1, 0x2c };
        int strCID4 = 0x12C;
        assertEquals(strCID4, cMap.toCID(cid4), "CID 300 from cidrange <0300> <0300> 300");
    }

    @Test
    void testIdentity() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("Identity-H");

        assertEquals(65, cMap.toCID(new byte[] { 0, 65 }), "Indentity-H CID 65");
        assertEquals(12345, cMap.toCID(new byte[] { 0x30, 0x39 }), "Indentity-H CID 12345");
        assertEquals(0xFFFF, cMap.toCID(new byte[] { (byte) 0xFF, (byte) 0xFF }),
                "Indentity-H CID 0xFFFF");
    }

    @Test
    void testUniJIS_UTF16_H() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("UniJIS-UTF16-H");

        // the next 3 cases demonstrate the issue of possible false result values of CMap.toCID(int code)
        assertEquals(694, cMap.toCID(0xb1), "UniJIS-UTF16-H CID 0xb1 -> 694");
        assertNotEquals(694, cMap.toCID(0xb1, 1), "UniJIS-UTF16-H CID 0xb1 -> 694");
        assertEquals(694, cMap.toCID(0xb1, 2), "UniJIS-UTF16-H CID 0x00b1 -> 694");

        // 1:1 cid char mapping
        assertEquals(694, cMap.toCID(new byte[] { 0x00, (byte) 0xb1 }),
                "UniJIS-UTF16-H CID 0x00b1 -> 694");
        assertEquals(20168, cMap.toCID(new byte[] { (byte) 0xd8, 0x50, (byte) 0xdc, 0x4b }),
                "UniJIS-UTF16-H CID 0xd850dc4b -> 20168");

        // cid range mapping
        assertEquals(19223, cMap.toCID(new byte[] { 0x54, 0x34 }),
                "UniJIS-UTF16-H CID 0x5434 -> 19223");
        assertEquals(10006, cMap.toCID(new byte[] { (byte) 0xd8, 0x3c, (byte) 0xdd, 0x12 }),
                "UniJIS-UTF16-H CID 0xd83cdd12 -> 10006");

    }

    @Test
    void testUniJIS_UCS2_H() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("UniJIS-UCS2-H");

        assertEquals(34, cMap.toCID(new byte[] { 0, 65 }), "UniJIS-UCS2-H CID 65 -> 34");
    }

    @Test
    void testAdobe_GB1_UCS2() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("Adobe-GB1-UCS2");

        assertEquals("0", cMap.toUnicode(new byte[] { 0, 0x11 }),
                "Adobe-GB1-UCS2 CID 0x11 -> \"0\"");
    }

    /**
     * Test the parser against a valid, but poorly formatted CMap file.
     * @throws IOException If something went wrong
     */
    @Test
    void testParserWithPoorWhitespace() throws IOException
    {
        CMap cMap = new CMapParser().parse(new RandomAccessReadBufferedFile(
                new File("src/test/resources/cmap", "CMapNoWhitespace")));

        assertNotNull(cMap, "Failed to parse nasty CMap file");
    }

    @Test
    void testParserWithMalformedbfrange1() throws IOException
    {
        CMap cMap = new CMapParser()
                .parse(new RandomAccessReadBufferedFile(
                        new File("src/test/resources/cmap", "CMapMalformedbfrange1")));

        assertNotNull(cMap, "Failed to parse malformed CMap file");

        byte[] bytes1 = { 0, 1 };
        assertEquals("A", cMap.toUnicode(bytes1), "bytes 00 01 from bfrange <0001> <0009> <0041>");

        byte[] bytes2 = { 1, 00 };
        assertNull(cMap.toUnicode(bytes2));

    }

    @Test
    void testParserWithMalformedbfrange2() throws IOException
    {
        CMap cMap = new CMapParser()
                .parse(new RandomAccessReadBufferedFile(
                        new File("src/test/resources/cmap", "CMapMalformedbfrange2")));

        assertNotNull(cMap, "Failed to parse malformed CMap file");

        assertEquals("0", cMap.toUnicode(new byte[] { 0, 1 }),
                "bytes 00 01 from bfrange <0001> <0009> <0030>");

        assertEquals("A", cMap.toUnicode(new byte[] { 2, 0x32 }),
                "bytes 02 32 from bfrange <0232> <0432> <0041>");

        // check border values for non strict mode
        assertNotNull(cMap.toUnicode(new byte[] { 2, (byte) 0xF0 }));
        assertNotNull(cMap.toUnicode(new byte[] { 2, (byte) 0xF1 }));

        // use strict mode
        cMap = new CMapParser(true)
                .parse(new RandomAccessReadBufferedFile(
                        new File("src/test/resources/cmap", "CMapMalformedbfrange2")));
        // check border values for strict mode
        assertNotNull(cMap.toUnicode(new byte[] { 2, (byte) 0xF0 }));
        assertNull(cMap.toUnicode(new byte[] { 2, (byte) 0xF1 }));

    }

    @Test
    void testPredefinedMap() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("Adobe-Korea1-UCS2");
        assertNotNull(cMap, "Failed to parse predefined CMap Adobe-Korea1-UCS2");

        assertEquals("Adobe-Korea1-UCS2", cMap.getName(), "wrong CMap name");
        assertEquals(0, cMap.getWMode(), "wrong WMode");
        assertFalse(cMap.hasCIDMappings());
        assertTrue(cMap.hasUnicodeMappings());

        cMap = new CMapParser().parsePredefined("Identity-V");
        assertNotNull(cMap, "Failed to parse predefined CMap Identity-V");
    }

    @Test
    void testIdentitybfrange() throws IOException
    {
        // use strict mode
        CMap cMap = new CMapParser(true)
                .parse(new RandomAccessReadBufferedFile(
                        new File("src/test/resources/cmap", "Identitybfrange")));
        assertEquals("Adobe-Identity-UCS", cMap.getName(), "wrong CMap name");

        byte[] bytes = { 0, 65 };
        assertEquals(new String(bytes, StandardCharsets.UTF_16BE), cMap.toUnicode(bytes),
                "Indentity 0x0048");
        bytes = new byte[] { 0x30, 0x39 };
        assertEquals(new String(bytes, StandardCharsets.UTF_16BE), cMap.toUnicode(bytes),
                "Indentity 0x3039");
        // check border values for strict mode
        bytes = new byte[] { 0x30, (byte) 0xFF };
        assertEquals(new String(bytes, StandardCharsets.UTF_16BE), cMap.toUnicode(bytes),
                "Indentity 0x30FF");
        // check border values for strict mode
        bytes = new byte[] { 0x31, 0x00 };
        assertEquals(new String(bytes, StandardCharsets.UTF_16BE), cMap.toUnicode(bytes),
                "Indentity 0x3100");
        bytes = new byte[] { (byte) 0xFF, (byte) 0xFF };
        assertEquals(new String(bytes, StandardCharsets.UTF_16BE), cMap.toUnicode(bytes),
                "Indentity 0xFFFF");

    }
}
