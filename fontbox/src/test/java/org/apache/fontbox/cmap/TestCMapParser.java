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

import static org.apache.fontbox.cmap.CMap.toInt;

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
                cMap.toUnicode(toInt(bytes1, bytes1.length)));

        byte[] bytes2 = {1, 00};
        String str2 = "0";
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", str2,
                cMap.toUnicode(toInt(bytes2, bytes2.length)));

        byte[] bytes3 = { 1, 32 };
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", "P",
                cMap.toUnicode(toInt(bytes3, bytes3.length)));

        byte[] bytes4 = { 1, 33 };
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", "R",
                cMap.toUnicode(toInt(bytes4, bytes4.length)));

        byte[] bytes5 = { 0, 10 };
        String str5 = "*";
        assertEquals("bytes 00 0A from bfchar <000A> <002A>", str5,
                cMap.toUnicode(toInt(bytes5, bytes5.length)));

        byte[] bytes6 = { 1, 10 };
        String str6 = "+";
        assertEquals("bytes 01 0A from bfchar <010A> <002B>", str6,
                cMap.toUnicode(toInt(bytes6, bytes6.length)));

        // CID mappings
        int cid1 = 65;
        assertEquals("CID 65 from cidrange <0000> <00ff> 0 ", 65, cMap.toCID(cid1));

        int cid2 = 280;
        int strCID2 = 0x0118;
        assertEquals("CID 280 from cidrange <0100> <01ff> 256", strCID2, cMap.toCID(cid2));

        int cid3 = 520;
        int strCID3 = 0x0208;
        assertEquals("CID 520 from cidchar <0208> 520", strCID3, cMap.toCID(cid3));

        int cid4 = 300;
        int strCID4 = 0x12C;
        assertEquals("CID 300 from cidrange <0300> <0300> 300", strCID4, cMap.toCID(cid4));
    }

    public void testIdentity() throws IOException
    {
        final String resourceDir = "src/main/resources/org/apache/fontbox/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "Identity-H"));

        assertEquals("Indentity-H CID 65", 65, cMap.toCID(65));
        assertEquals("Indentity-H CID 12345", 12345, cMap.toCID(12345));
        assertEquals("Indentity-H CID 0xFFFF", 0xFFFF, cMap.toCID(0xFFFF));
    }

    public void testUniJIS_UCS2_H() throws IOException
    {
        final String resourceDir = "src/main/resources/org/apache/fontbox/cmap";
        File inDir = new File(resourceDir);

        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse(new File(inDir, "UniJIS-UCS2-H"));

        assertEquals("UniJIS-UCS2-H CID 65 -> 34", 34, cMap.toCID(65));
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
                cMap.toUnicode(toInt(bytes1, bytes1.length)));

        byte[] bytes2 = { 1, 00 };
        assertNull(cMap.toUnicode(toInt(bytes2, bytes2.length)));

    }

    public void testParserWithMalformedbfrange2() throws IOException
    {
        CMap cMap = new CMapParser()
                .parse(new File("src/test/resources/cmap", "CMapMalformedbfrange2"));

        assertNotNull("Failed to parse malformed CMap file", cMap);

        byte[] bytes1 = { 0, 1 };
        assertEquals("bytes 00 01 from bfrange <0001> <0009> <0041>", "A",
                cMap.toUnicode(toInt(bytes1, bytes1.length)));

        byte[] bytes2 = { 2, 00 };
        assertNull(cMap.toUnicode(toInt(bytes2, bytes2.length)));

    }

}
