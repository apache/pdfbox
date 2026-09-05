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
import org.apache.pdfbox.io.RandomAccessReadBuffer;

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

    /**
     * Test that parsing a CMap with empty byte arrays in bfrange does not throw
     * ArrayIndexOutOfBoundsException. Empty hex strings produce zero-length byte
     * arrays, causing increment() to be called with position -1.
     */
    @Test
    void testBadIncrement() throws IOException
    {
        byte[] cmapData = "1 beginbfrange\n<> <> <2223>\nendbfrange".getBytes("US-ASCII");
        CMapParser parser = new CMapParser();
        CMap cmap = parser.parse(new RandomAccessReadBuffer(cmapData));
        assertNotNull(cmap);
    }

    /**
     * A CMap that redefines a code it inherits through usecmap must win over the CMap it uses.
     *
     * ETenms-B5-H exists only to do that: it uses ETen-B5-H and then remaps 0x20-0x7E to the
     * proportional latin CIDs 1-95, where the parent maps them to the full width forms at 13648+.
     */
    @Test
    void testUseCmapOwnMappingsWin() throws IOException
    {
        CMap parent = new CMapParser().parsePredefined("ETen-B5-H");
        assertEquals(13681, parent.toCID(0x41, 1), "ETen-B5-H maps 0x41 to the full width form");

        CMap cMap = new CMapParser().parsePredefined("ETenms-B5-H");
        assertEquals(34, cMap.toCID(0x41, 1), "ETenms-B5-H overrides 0x41 to the proportional form");
        assertEquals(1, cMap.toCID(0x20, 1), "ETenms-B5-H overrides 0x20 to the proportional form");

        // codes the CMap does not redefine still come from the one it uses
        assertEquals(parent.toCID(new byte[] { (byte) 0xA1, 0x40 }),
                cMap.toCID(new byte[] { (byte) 0xA1, 0x40 }),
                "an inherited code is unaffected");

        // the byte[] overload repeats the lookup order of the int one, so check the override there too
        assertEquals(13681, parent.toCID(new byte[] { 0x41 }));
        assertEquals(34, cMap.toCID(new byte[] { 0x41 }),
                "the byte[] overload has to prefer the CMap's own mapping as well");

        // UniJIS-UCS2-HW-H likewise overrides its parent's proportional latin with the half width forms
        CMap halfWidth = new CMapParser().parsePredefined("UniJIS-UCS2-HW-H");
        assertEquals(34, new CMapParser().parsePredefined("UniJIS-UCS2-H").toCID(0x41, 2),
                "UniJIS-UCS2-H maps 0x0041 to the proportional form");
        assertEquals(264, halfWidth.toCID(0x41, 2),
                "UniJIS-UCS2-HW-H overrides 0x0041 to the half width form");
    }

    /**
     * The override has to survive a chain of usecmap: ETenms-B5-V uses ETenms-B5-H, which in turn
     * uses ETen-B5-H. A code that only the middle CMap redefines has to keep that redefinition.
     */
    @Test
    void testUseCmapChainKeepsNearestMapping() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("ETenms-B5-V");

        assertEquals(1, cMap.getWMode(), "ETenms-B5-V is vertical");
        assertEquals(34, cMap.toCID(0x41, 1),
                "ETenms-B5-V inherits the proportional override from ETenms-B5-H, not ETen-B5-H");
    }

    /**
     * Both kinds of mapping a CMap declares have to beat the ranges it inherits. ETenms-B5-V
     * declares six cidchars and twelve cidranges for the punctuation whose vertical form differs,
     * on top of the horizontal forms it inherits from ETenms-B5-H and ETen-B5-H.
     *
     * The cidchars were already resolved correctly before this was fixed, a cidchar being consulted
     * ahead of any range either way, so they are here as a guard rather than as a second
     * reproducer.
     */
    @Test
    void testUseCmapOwnMappingsBeatInheritedRanges() throws IOException
    {
        CMap horizontal = new CMapParser().parsePredefined("ETenms-B5-H");
        CMap vertical = new CMapParser().parsePredefined("ETenms-B5-V");

        // the horizontal forms come from an inherited range in both CMaps
        assertEquals(110, horizontal.toCID(0xA14B, 2));
        assertEquals(111, horizontal.toCID(0xA14C, 2));
        assertEquals(121, horizontal.toCID(0xA156, 2));

        // ETenms-B5-V's own cidchars replace them with the vertical forms
        assertEquals(13646, vertical.toCID(0xA14B, 2), "own cidchar has to beat the inherited range");
        assertEquals(109, vertical.toCID(0xA14C, 2), "own cidchar has to beat the inherited range");
        assertEquals(312, vertical.toCID(0xA156, 2), "own cidchar has to beat the inherited range");

        // and its own cidranges likewise, two usecmap levels down
        assertEquals(128, horizontal.toCID(0xA15D, 2));
        assertEquals(130, vertical.toCID(0xA15D, 2), "own cidrange has to beat the inherited range");
    }

    /**
     * Identity-V is the one predefined CMap that declares no cid mappings at all, it only uses
     * Identity-H. Every lookup it answers is therefore an inherited one, which also makes it the
     * case that proves hasCIDMappings has to account for what a CMap inherited.
     */
    @Test
    void testUseCmapOnlyInheritedMappings() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("Identity-V");

        assertEquals(1, cMap.getWMode(), "Identity-V is vertical");
        assertTrue(cMap.hasCIDMappings(), "Identity-V has cid mappings, all of them inherited");

        assertEquals(65, cMap.toCID(new byte[] { 0, 65 }), "Identity-V CID 65");
        assertEquals(12345, cMap.toCID(new byte[] { 0x30, 0x39 }), "Identity-V CID 12345");
        assertEquals(0xFFFF, cMap.toCID(new byte[] { (byte) 0xFF, (byte) 0xFF }),
                "Identity-V CID 0xFFFF");
        assertEquals(12345, cMap.toCID(0x3039, 2), "Identity-V CID 12345");
    }

    /**
     * A CMap holds on to the CMaps it uses rather than copying their mappings, so it must never
     * write into one: adding a mapping to the importing CMap must not reach back into the used one.
     */
    @Test
    void testUseCmapDoesNotShareMappingsWithTheUsedCMap() throws IOException
    {
        CMap used = new CMap();
        used.addCIDMapping(new byte[] { 0x41 }, 100);
        used.addCIDRange(new byte[] { 0x50 }, new byte[] { 0x5F }, 200);

        CMap cMap = new CMap();
        cMap.useCmap(used);
        assertEquals(100, cMap.toCID(0x41, 1), "the mapping is inherited");
        assertEquals(205, cMap.toCID(0x55, 1), "the range is inherited");

        cMap.addCIDMapping(new byte[] { 0x41 }, 300);
        cMap.addCIDRange(new byte[] { 0x50 }, new byte[] { 0x5F }, 400);

        assertEquals(300, cMap.toCID(0x41, 1), "the CMap's own mapping wins");
        assertEquals(405, cMap.toCID(0x55, 1), "the CMap's own range wins");
        assertEquals(100, used.toCID(0x41, 1), "the used CMap must not have been modified");
        assertEquals(205, used.toCID(0x55, 1), "the used CMap must not have been modified");
    }

    /**
     * Everything a CMap declares outranks everything it inherits, so a cidrange of its own has to
     * beat an inherited cidchar too, not just an inherited cidrange. No predefined CMap pairs the
     * two that way round, hence the hand built pair here.
     */
    @Test
    void testUseCmapOwnRangeBeatsInheritedChar() throws IOException
    {
        CMap used = new CMap();
        used.addCIDMapping(new byte[] { 0x41 }, 100);

        CMap cMap = new CMap();
        cMap.useCmap(used);
        cMap.addCIDRange(new byte[] { 0x40 }, new byte[] { 0x4F }, 200);

        assertEquals(201, cMap.toCID(0x41, 1), "the CMap's own range has to beat the inherited char");
        assertEquals(200, cMap.toCID(0x40, 1), "a code the used CMap says nothing about");
        assertEquals(100, used.toCID(0x41, 1), "the used CMap must not have been modified");
    }

    /**
     * A usecmap chain is resolved nearest first: a CMap is asked for its own mappings, and only if
     * it has none for the code does it pass the question on to the CMap it uses. So a range in the
     * nearer CMap outranks a cidchar in the one behind it, even though a cidchar outranks a range
     * within a single CMap.
     */
    @Test
    void testUseCmapNearerCMapWins()
    {
        CMap far = new CMap();
        far.addCIDMapping(new byte[] { 0x41 }, 100);

        CMap near = new CMap();
        near.useCmap(far);
        near.addCIDRange(new byte[] { 0x40 }, new byte[] { 0x4F }, 200);

        CMap cMap = new CMap();
        cMap.useCmap(near);

        assertEquals(201, cMap.toCID(0x41, 1), "the nearer CMap's range has to beat the farther "
                + "CMap's cidchar");
        assertEquals(100, far.toCID(0x41, 1), "a used CMap answers for itself unchanged");
    }

    /**
     * "CMap files can be nested to five levels", so a redefinition has to survive that depth, and
     * each level has to be able to redefine what the level below it declared.
     */
    @Test
    void testUseCmapNestedToFiveLevels()
    {
        CMap cMap = new CMap();
        cMap.addCIDMapping(new byte[] { 0x01 }, 10);
        for (int level = 2; level <= 5; level++)
        {
            CMap nested = new CMap();
            nested.useCmap(cMap);
            // redefine the code the level below just defined, and add one of its own
            nested.addCIDMapping(new byte[] { (byte) (level - 1) }, 10 * level);
            nested.addCIDMapping(new byte[] { (byte) level }, 10 * level);
            cMap = nested;
        }

        assertTrue(cMap.hasCIDMappings());
        // every code but the last was redefined one level up, the last one wasn't
        assertEquals(20, cMap.toCID(0x01, 1));
        assertEquals(30, cMap.toCID(0x02, 1));
        assertEquals(40, cMap.toCID(0x03, 1));
        assertEquals(50, cMap.toCID(0x04, 1));
        assertEquals(50, cMap.toCID(0x05, 1));
    }

    /**
     * A CMap with no cid mappings of its own answers with the ones of the CMap it uses, five levels
     * down if need be. Identity-V is the predefined case of this, testUseCmapOnlyInheritedMappings
     * covers that one.
     */
    @Test
    void testUseCmapPassesThroughEmptyLevels()
    {
        CMap cMap = new CMap();
        cMap.addCIDMapping(new byte[] { 0x41 }, 100);
        for (int level = 2; level <= 5; level++)
        {
            CMap nested = new CMap();
            nested.useCmap(cMap);
            cMap = nested;
        }

        assertTrue(cMap.hasCIDMappings(), "the mappings are five levels down but they are there");
        assertEquals(100, cMap.toCID(0x41, 1));
        assertEquals(0, cMap.toCID(0x42, 1), "a code no level in the chain maps");
    }

    /**
     * The specification gives a CMap one usecmap, but nothing here has to break if a file carries
     * more than one. Every used CMap is kept, and they are asked in the order they were declared.
     */
    @Test
    void testUseCmapSeveralUsedCMaps()
    {
        CMap first = new CMap();
        first.addCIDMapping(new byte[] { 0x41 }, 100);
        first.addCIDMapping(new byte[] { 0x42 }, 101);

        CMap second = new CMap();
        second.addCIDMapping(new byte[] { 0x42 }, 200);
        second.addCIDMapping(new byte[] { 0x43 }, 201);

        CMap cMap = new CMap();
        cMap.useCmap(first);
        cMap.useCmap(second);
        cMap.addCIDMapping(new byte[] { 0x41 }, 300);

        assertEquals(300, cMap.toCID(0x41, 1), "the CMap's own mapping beats both");
        assertEquals(101, cMap.toCID(0x42, 1), "a code both used CMaps map comes from the first");
        assertEquals(201, cMap.toCID(0x43, 1), "a code only the second maps still resolves");
        assertEquals(0, cMap.toCID(0x44, 1), "a code none of them maps");
    }

    /**
     * CID 0 is the .notdef glyph, and a CMap may map a code to it deliberately. That is a mapping,
     * not the absence of one, so it has to outrank whatever the CMap it uses says about the code.
     */
    @Test
    void testUseCmapOwnMappingToCidZeroIsNotAFallthrough()
    {
        CMap used = new CMap();
        used.addCIDRange(new byte[] { 0x00 }, new byte[] { (byte) 0xFF }, 500);

        CMap cMap = new CMap();
        cMap.useCmap(used);
        cMap.addCIDRange(new byte[] { 0x41 }, new byte[] { 0x41 }, 0);

        assertEquals(0, cMap.toCID(0x41, 1), "the CMap's own .notdef has to win");
        assertEquals(0, cMap.toCID(new byte[] { 0x41 }), "the byte[] overload as well");
        assertEquals(566, cMap.toCID(0x42, 1), "a code it doesn't redefine still comes from the parent");
        assertEquals(565, used.toCID(0x41, 1), "the used CMap answers for itself unchanged");
    }

    /**
     * The length guessing overload probes the code lengths shortest first. A code mapped to CID 0 at
     * the shortest length is mapped, so the probing stops there rather than running on to a longer
     * length that happens to map the same value to something else.
     */
    @Test
    void testToCidZeroAtShortestLengthStopsTheLengthProbing()
    {
        CMap cMap = new CMap();
        cMap.addCIDMapping(new byte[] { 0x41 }, 0);
        cMap.addCIDMapping(new byte[] { 0x00, 0x41 }, 700);

        assertEquals(0, cMap.toCID(0x41, 1), "the one byte code maps to .notdef");
        assertEquals(700, cMap.toCID(0x41, 2), "the two byte code maps to 700");
        assertEquals(0, cMap.toCID(0x41), "the shortest length maps the code, so that is the answer");
    }
}
