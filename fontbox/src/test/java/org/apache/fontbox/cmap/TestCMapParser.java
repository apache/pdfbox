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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * This will test the CMapParser implementation.
 *
 */
public class TestCMapParser extends TestCase
{

    /**
     * Check whether the parser and the resulting mapping is working correct.
     *
     * @throws IOException If something went wrong
     */
    public void testLookup() throws IOException
    {
        final String resourceDir = "src/test/resources/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "CMapTest"));

        // char mappings
        byte[] bytes1 = {0, 1};
        assertEquals("bytes 00 01 from bfrange <0001> <0005> <0041>", "A",
                cMap.toUnicode(bytes1));

        byte[] bytes2 = {1, 00};
        String str2 = "0";
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", str2,
                cMap.toUnicode(bytes2));

        byte[] bytes3 = { 1, 32 };
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", "P",
                cMap.toUnicode(bytes3));

        byte[] bytes4 = { 1, 33 };
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", "R",
                cMap.toUnicode(bytes4));

        byte[] bytes5 = { 0, 10 };
        String str5 = "*";
        assertEquals("bytes 00 0A from bfchar <000A> <002A>", str5,
                cMap.toUnicode(bytes5));

        byte[] bytes6 = { 1, 10 };
        String str6 = "+";
        assertEquals("bytes 01 0A from bfchar <010A> <002B>", str6,
                cMap.toUnicode(bytes6));

        // CID mappings
        byte[] cid1 = new byte[] { 0, 65 };
        assertEquals("CID 65 from cidrange <0000> <00ff> 0 ", 65, cMap.toCID(cid1));

        byte[] cid2 = new byte[] { 1, 24 };
        int strCID2 = 0x0118;
        assertEquals("CID 280 from cidrange <0100> <01ff> 256", strCID2, cMap.toCID(cid2));

        byte[] cid3 = new byte[] { 2, 8 };
        int strCID3 = 0x0208;
        assertEquals("CID 520 from cidchar <0208> 520", strCID3, cMap.toCID(cid3));

        byte[] cid4 = new byte[] { 1, 0x2c };
        int strCID4 = 0x12C;
        assertEquals("CID 300 from cidrange <0300> <0300> 300", strCID4, cMap.toCID(cid4));
    }

    public void testIdentity() throws IOException
    {
        final String resourceDir = "src/main/resources/org/apache/fontbox/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "Identity-H"));

        assertEquals("Indentity-H CID 65", 65, cMap.toCID(new byte[] { 0, 65 }));
        assertEquals("Indentity-H CID 12345", 12345, cMap.toCID(new byte[] { 0x30, 0x39 }));
        assertEquals("Indentity-H CID 0xFFFF", 0xFFFF,
                cMap.toCID(new byte[] { (byte) 0xFF, (byte) 0xFF }));
    }

    public void testUniJIS_UTF16_H() throws IOException
    {
        final String resourceDir = "src/main/resources/org/apache/fontbox/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "UniJIS-UTF16-H"));

        // the next 3 cases demonstrate the issue of possible false result values of CMap.toCID(int code)
        assertEquals("UniJIS-UTF16-H CID 0xb1 -> 694", 694, cMap.toCID(0xb1));
        assertFalse("UniJIS-UTF16-H CID 0xb1 -> 694", cMap.toCID(0xb1, 1) == 694);
        assertEquals("UniJIS-UTF16-H CID 0x00b1 -> 694", 694, cMap.toCID(0xb1, 2));

        // 1:1 cid char mapping
        assertEquals("UniJIS-UTF16-H CID 0x00b1 -> 694",
                694,
                cMap.toCID(new byte[] { 0x00, (byte) 0xb1 }));
        assertEquals(
                "UniJIS-UTF16-H CID 0xd850dc4b -> 20168",
                20168,
                cMap.toCID(new byte[] { (byte) 0xd8, 0x50, (byte) 0xdc, 0x4b }));

        // cid range mapping
        assertEquals(
                "UniJIS-UTF16-H CID 0x5434 -> 19223",
                19223,
                cMap.toCID(new byte[] { 0x54, 0x34 }));
        assertEquals("UniJIS-UTF16-H CID 0xd83cdd12 -> 10006",
                10006,
                cMap.toCID(new byte[] { (byte) 0xd8, 0x3c, (byte) 0xdd, 0x12 }));

    }

    public void testUniJIS_UCS2_H() throws IOException
    {
        final String resourceDir = "src/main/resources/org/apache/fontbox/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "UniJIS-UCS2-H"));

        assertEquals("UniJIS-UCS2-H CID 65 -> 34", 34, cMap.toCID(new byte[] { 0, 65 }));
    }

    public void testAdobe_GB1_UCS2() throws IOException
    {
        final String resourceDir = "src/main/resources/org/apache/fontbox/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "Adobe-GB1-UCS2"));

        assertEquals("Adobe-GB1-UCS2 CID 0x11 -> \"0\"", "0",
                cMap.toUnicode(new byte[] { 0, 0x11 }));
    }

    /**
     * Test the parser against a valid, but poorly formatted CMap file.
     * @throws IOException If something went wrong
     */
    public void testParserWithPoorWhitespace() throws IOException 
    {
        CMap cMap = new CMapParser().parse(new File("src/test/resources/cmap", "CMapNoWhitespace"));

        assertNotNull("Failed to parse nasty CMap file", cMap);
    }

    public void testParserWithMalformedbfrange1() throws IOException
    {
        CMap cMap = new CMapParser()
                .parse(new File("src/test/resources/cmap", "CMapMalformedbfrange1"));

        assertNotNull("Failed to parse malformed CMap file", cMap);

        byte[] bytes1 = { 0, 1 };
        assertEquals("bytes 00 01 from bfrange <0001> <0009> <0041>", "A",
                cMap.toUnicode(bytes1));

        byte[] bytes2 = { 1, 00 };
        assertNull(cMap.toUnicode(bytes2));

    }

    public void testParserWithMalformedbfrange2() throws IOException
    {
        CMap cMap = new CMapParser()
                .parse(new File("src/test/resources/cmap", "CMapMalformedbfrange2"));

        assertNotNull("Failed to parse malformed CMap file", cMap);

        assertEquals("bytes 00 01 from bfrange <0001> <0009> <0030>", "0",
                cMap.toUnicode(new byte[] { 0, 1 }));

        assertEquals("bytes 02 32 from bfrange <0232> <0432> <0041>", "A",
                cMap.toUnicode(new byte[] { 2, 0x32 }));

        // check border values
        assertNotNull(cMap.toUnicode(new byte[] { 2, (byte) 0xF0 }));
        assertNull(cMap.toUnicode(new byte[] { 2, (byte) 0xF1 }));

    }

}
