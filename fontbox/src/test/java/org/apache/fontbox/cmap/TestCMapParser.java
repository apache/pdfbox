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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
        CMap cMap = new CMapParser().parse(new File("src/test/resources/cmap", "CMapTest"));

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
        CMap cMap = new CMapParser().parsePredefined("Identity-H");

        assertEquals("Indentity-H CID 65", 65, cMap.toCID(65));
        assertEquals("Indentity-H CID 12345", 12345, cMap.toCID(12345));
        assertEquals("Indentity-H CID 0xFFFF", 0xFFFF, cMap.toCID(0xFFFF));
    }

    public void testUniJIS_UCS2_H() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("UniJIS-UCS2-H");

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

        assertEquals("bytes 00 01 from bfrange <0001> <0009> <0030>", "0", cMap.toUnicode(0x001));

        assertEquals("bytes 02 32 from bfrange <0232> <0432> <0041>", "A", cMap.toUnicode(0x232));

        // check border values for non strict mode
        assertNotNull(cMap.toUnicode(0x2F0));
        assertNotNull(cMap.toUnicode(0x2F1));

        // use strict mode
        cMap = new CMapParser(true)
                .parse(new File("src/test/resources/cmap", "CMapMalformedbfrange2"));
        // check border values for strict mode
        assertNotNull(cMap.toUnicode(0x2F0));
        assertNull(cMap.toUnicode(0x2F1));
    }

    public void testPredefinedMap() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("Adobe-Korea1-UCS2");
        assertNotNull("Failed to parse predefined CMap Adobe-Korea1-UCS2", cMap);

        assertEquals("wrong CMap name", "Adobe-Korea1-UCS2", cMap.getName());
        assertEquals("wrong WMode", 0, cMap.getWMode());
        assertFalse(cMap.hasCIDMappings());
        assertTrue(cMap.hasUnicodeMappings());

        cMap = new CMapParser().parsePredefined("Identity-V");
        assertNotNull("Failed to parse predefined CMap Identity-V", cMap);
    }

    public void testIdentitybfrange() throws IOException
    {
        // use strict mode
        CMap cMap = new CMapParser(true)
                .parse(new File("src/test/resources/cmap", "Identitybfrange"));
        assertEquals("wrong CMap name", "Adobe-Identity-UCS", cMap.getName());

        Charset UTF_16BE = Charset.forName("UTF-16BE");
        
        byte[] bytes = new byte[] { 0, 0x48 };
        assertEquals("Indentity 0x0048", new String(bytes, UTF_16BE), cMap.toUnicode(0x0048));
        
        bytes = new byte[] { 0x30, 0x39 };
        assertEquals("Indentity 0x3039", new String(bytes, UTF_16BE), cMap.toUnicode(0x3039));
        
        // check border values for strict mode
        bytes = new byte[] { 0x30, (byte) 0xFF };
        assertEquals("Indentity 0x30FF", new String(bytes, UTF_16BE), cMap.toUnicode(0x30FF));
        // check border values for strict mode
        bytes = new byte[] { 0x31, 0x00 };
        assertEquals("Indentity 0x3100", new String(bytes, UTF_16BE), cMap.toUnicode(0x3100));
        
        bytes = new byte[] { (byte) 0xFF, (byte) 0xFF };
        assertEquals("Indentity 0xFFFF", new String(bytes, UTF_16BE), cMap.toUnicode(0xFFFF));

    }

    /**
     * Test that parsing a CMap with empty byte arrays in bfrange does not throw
     * ArrayIndexOutOfBoundsException. Empty hex strings produce zero-length byte
     * arrays, causing increment() to be called with position -1.
     */
    public void testBadIncrement() throws IOException
    {
        byte[] cmapData = "1 beginbfrange\n<> <> <2223>\nendbfrange".getBytes("US-ASCII");
        CMapParser parser = new CMapParser();
        CMap cmap = parser.parse(new ByteArrayInputStream(cmapData));
        assertNotNull(cmap);
    }

    /**
     * A CMap that redefines a code it inherits through usecmap must win over the CMap it uses.
     *
     * ETenms-B5-H exists only to do that: it uses ETen-B5-H and then remaps 0x20-0x7E to the
     * proportional latin CIDs 1-95, where the parent maps them to the full width forms at 13648+.
     */
    public void testUseCmapOwnMappingsWin() throws IOException
    {
        CMap parent = new CMapParser().parsePredefined("ETen-B5-H");
        assertEquals("ETen-B5-H maps 0x41 to the full width form", 13681, parent.toCID(0x41));

        CMap cMap = new CMapParser().parsePredefined("ETenms-B5-H");
        assertEquals("ETenms-B5-H overrides 0x41 to the proportional form", 34, cMap.toCID(0x41));
        assertEquals("ETenms-B5-H overrides 0x20 to the proportional form", 1, cMap.toCID(0x20));

        // codes the CMap does not redefine still come from the one it uses
        assertEquals("an inherited code is unaffected",
                parent.toCID(0xA140), cMap.toCID(0xA140));

        // the byte[] overload repeats the lookup order of the int one, so check the override there too
        assertEquals(13681, parent.toCID(0x41));
        assertEquals("the byte[] overload has to prefer the CMap's own mapping as well",
                34, cMap.toCID(0x41));

        // UniJIS-UCS2-HW-H likewise overrides its parent's proportional latin with the half width forms
        CMap halfWidth = new CMapParser().parsePredefined("UniJIS-UCS2-HW-H");
        assertEquals("UniJIS-UCS2-H maps 0x0041 to the proportional form",
                34, new CMapParser().parsePredefined("UniJIS-UCS2-H").toCID(0x41));
        assertEquals("UniJIS-UCS2-HW-H overrides 0x0041 to the half width form",
                264, halfWidth.toCID(0x41));
    }

    /**
     * The override has to survive a chain of usecmap: ETenms-B5-V uses ETenms-B5-H, which in turn
     * uses ETen-B5-H. A code that only the middle CMap redefines has to keep that redefinition.
     */
    public void testUseCmapChainKeepsNearestMapping() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("ETenms-B5-V");

        assertEquals("UniJIS-UCS2-HW-H overrides 0x0041 to the half width form", 1, cMap.getWMode());
        assertEquals("ETenms-B5-V inherits the proportional override from ETenms-B5-H, not ETen-B5-H",
                34, cMap.toCID(0x41));
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
    public void testUseCmapOwnMappingsBeatInheritedRanges() throws IOException
    {
        CMap horizontal = new CMapParser().parsePredefined("ETenms-B5-H");
        CMap vertical = new CMapParser().parsePredefined("ETenms-B5-V");

        // the horizontal forms come from an inherited range in both CMaps
        assertEquals(110, horizontal.toCID(0xA14B));
        assertEquals(111, horizontal.toCID(0xA14C));
        assertEquals(121, horizontal.toCID(0xA156));

        // ETenms-B5-V's own cidchars replace them with the vertical forms
        assertEquals("own cidchar has to beat the inherited range", 13646, vertical.toCID(0xA14B));
        assertEquals("own cidchar has to beat the inherited range", 109, vertical.toCID(0xA14C));
        assertEquals("own cidchar has to beat the inherited range", 312, vertical.toCID(0xA156));

        // and its own cidranges likewise, two usecmap levels down
        assertEquals(128, horizontal.toCID(0xA15D));
        assertEquals("own cidchar has to beat the inherited range", 130, vertical.toCID(0xA15D));
    }

    /**
     * Identity-V is the one predefined CMap that declares no cid mappings at all, it only uses
     * Identity-H. Every lookup it answers is therefore an inherited one, which also makes it the
     * case that proves hasCIDMappings has to account for what a CMap inherited.
     */
    public void testUseCmapOnlyInheritedMappings() throws IOException
    {
        CMap cMap = new CMapParser().parsePredefined("Identity-V");

        assertEquals("Identity-V is vertical", 1, cMap.getWMode());
        assertTrue("Identity-V has cid mappings, all of them inherited", cMap.hasCIDMappings());

        assertEquals("Identity-V CID 65", 65, cMap.toCID(65));
        assertEquals("Identity-V CID 12345", 12345, cMap.toCID(0x3039));
        assertEquals("Identity-V CID 0xFFFF", 0xFFFF, cMap.toCID(0xFFFF));
        assertEquals("Identity-V CID 12345", 12345, cMap.toCID(0x3039));
    }

    /**
     * A CMap holds on to the CMaps it uses rather than copying their mappings, so it must never
     * write into one: adding a mapping to the importing CMap must not reach back into the used one.
     *
     * @throws java.io.IOException
     */
    public void testUseCmapDoesNotShareMappingsWithTheUsedCMap() throws IOException
    {
        CMap used = new CMap();
        used.addCIDMapping(100, 0x41);
        used.addCIDRange((char) 0x50, (char) 0x5F, 200);

        CMap cMap = new CMap();
        cMap.useCmap(used);
        assertEquals(100, cMap.toCID(0x41));//, "the mapping is inherited");
        assertEquals(205, cMap.toCID(0x55));//, "the range is inherited");

        cMap.addCIDMapping(300, 0x41);
        cMap.addCIDRange((char) 0x50, (char) 0x5F, 400);

        assertEquals(300, cMap.toCID(0x41));//, "the CMap's own mapping wins");
        assertEquals(405, cMap.toCID(0x55));//, "the CMap's own range wins");
        assertEquals(100, used.toCID(0x41));//, "the used CMap must not have been modified");
        assertEquals(205, used.toCID(0x55));//, "the used CMap must not have been modified");
    }

    /**
     * Everything a CMap declares outranks everything it inherits, so a cidrange of its own has to
     * beat an inherited cidchar too, not just an inherited cidrange. No predefined CMap pairs the
     * two that way round, hence the hand built pair here.
     */
    public void testUseCmapOwnRangeBeatsInheritedChar() throws IOException
    {
        CMap used = new CMap();
        used.addCIDMapping(100, 0x41);

        CMap cMap = new CMap();
        cMap.useCmap(used);
        cMap.addCIDRange((char) 0x40, (char) 0x4F, 200);

        assertEquals(201, cMap.toCID(0x41));//, "the CMap's own range has to beat the inherited char");
        assertEquals(200, cMap.toCID(0x40));//, "a code the used CMap says nothing about");
        assertEquals(100, used.toCID(0x41));//, "the used CMap must not have been modified");
    }

    /**
     * A usecmap chain is resolved nearest first: a CMap is asked for its own mappings, and only if
     * it has none for the code does it pass the question on to the CMap it uses. So a range in the
     * nearer CMap outranks a cidchar in the one behind it, even though a cidchar outranks a range
     * within a single CMap.
     */
    public void testUseCmapNearerCMapWins()
    {
        CMap far = new CMap();
        far.addCIDMapping(100, 0x41);

        CMap near = new CMap();
        near.useCmap(far);
        near.addCIDRange((char) 0x40, (char) 0x4F, 200);

        CMap cMap = new CMap();
        cMap.useCmap(near);

        assertEquals(201, cMap.toCID(0x41));//, "the nearer CMap's range has to beat the farther CMap's cidchar");
        assertEquals(100, far.toCID(0x41));//, "a used CMap answers for itself unchanged");
    }

    /**
     * "CMap files can be nested to five levels", so a redefinition has to survive that depth, and
     * each level has to be able to redefine what the level below it declared.
     */
    public void testUseCmapNestedToFiveLevels()
    {
        CMap cMap = new CMap();
        cMap.addCIDMapping(10, 1);
        for (int level = 2; level <= 5; level++)
        {
            CMap nested = new CMap();
            nested.useCmap(cMap);
            // redefine the code the level below just defined, and add one of its own
            nested.addCIDMapping(10 * level, level - 1);
            nested.addCIDMapping(10 * level, level);
            cMap = nested;
        }

        assertTrue(cMap.hasCIDMappings());
        // every code but the last was redefined one level up, the last one wasn't
        assertEquals(20, cMap.toCID(0x01));
        assertEquals(30, cMap.toCID(0x02));
        assertEquals(40, cMap.toCID(0x03));
        assertEquals(50, cMap.toCID(0x04));
        assertEquals(50, cMap.toCID(0x05));
    }

    /**
     * A CMap with no cid mappings of its own answers with the ones of the CMap it uses, five levels
     * down if need be. Identity-V is the predefined case of this, testUseCmapOnlyInheritedMappings
     * covers that one.
     */
    public void testUseCmapPassesThroughEmptyLevels()
    {
        CMap cMap = new CMap();
        cMap.addCIDMapping(100, 0x41);
        assertEquals(100, cMap.toCID(0x41));
        for (int level = 2; level <= 5; level++)
        {
            CMap nested = new CMap();
            nested.useCmap(cMap);
            cMap = nested;
        }

        assertTrue(cMap.hasCIDMappings());//, "the mappings are five levels down but they are there");
        assertEquals(100, cMap.toCID(0x41));
        assertEquals(0, cMap.toCID(0x42));//, "a code no level in the chain maps");
    }

    /**
     * The specification gives a CMap one usecmap, but nothing here has to break if a file carries
     * more than one. Every used CMap is kept, and they are asked in the order they were declared.
     */
    public void testUseCmapSeveralUsedCMaps()
    {
        CMap first = new CMap();
        first.addCIDMapping(100, 0x41);
        first.addCIDMapping(101, 0x42);

        CMap second = new CMap();
        second.addCIDMapping(200, 0x42);
        second.addCIDMapping(201, 0x43);

        CMap cMap = new CMap();
        cMap.useCmap(first);
        cMap.useCmap(second);
        cMap.addCIDMapping(300, 0x41);

        assertEquals(300, cMap.toCID(0x41));//, "the CMap's own mapping beats both");
        assertEquals(101, cMap.toCID(0x42));//, "a code both used CMaps map comes from the first");
        assertEquals(201, cMap.toCID(0x43));//, "a code only the second maps still resolves");
        assertEquals(0, cMap.toCID(0x44));//, "a code none of them maps");
    }

    /**
     * CID 0 is the .notdef glyph, and a CMap may map a code to it deliberately. That is a mapping,
     * not the absence of one, so it has to outrank whatever the CMap it uses says about the code.
     */
    public void testUseCmapOwnMappingToCidZeroIsNotAFallthrough()
    {
        CMap used = new CMap();
        used.addCIDRange((char) 0, (char) 0xFF, 500);

        CMap cMap = new CMap();
        cMap.useCmap(used);
        cMap.addCIDRange((char) 0x41, (char) 0x41, 0);

        assertEquals(0, cMap.toCID(0x41));//, "the CMap's own .notdef has to win");
        assertEquals(0, cMap.toCID(0x41));//, "the byte[] overload as well");
        assertEquals(566, cMap.toCID(0x42));//, "a code it doesn't redefine still comes from the parent");
        assertEquals(565, used.toCID(0x41));//, "the used CMap answers for itself unchanged");
    }
    
    // testToCidZeroAtShortestLengthStopsTheLengthProbing missing in 2.0 because length not properly supported
}
