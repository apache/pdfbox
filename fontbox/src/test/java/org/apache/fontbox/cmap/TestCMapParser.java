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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * This will test the CMapParser implementation.
 *
 */
public class TestCMapParser extends TestCase 
{

    /**
     * Check whether the parser and the resulting mapping is working correct.
     * @throws IOException If something went wrong
     */
    public void testLookup() throws IOException
    {
        final String resourceDir= "src/test/resources/cmap"; 
        File inDir = new File(resourceDir);
        
        CMapParser parser = new CMapParser();
        CMap cMap = parser.parse( resourceDir, new FileInputStream(new File(inDir,"CMapTest")));
        
        // code space range
        assertEquals("codeSpaceRanges size", 1, cMap.getCodeSpaceRanges().size());
        final byte[] expectedStart = {0, 0}; // 00 00
        final byte[] expectedEnd = {2, -1}; // 02 FF
        final byte[] actualStart = cMap.getCodeSpaceRanges().get(0).getStart();
        final byte[] actualEnd = cMap.getCodeSpaceRanges().get(0).getEnd();
        assertTrue("codeSpaceRange start", Arrays.equals(expectedStart, actualStart));
        assertTrue("codeSpaceRange end", Arrays.equals(expectedEnd, actualEnd));
        
        // char mappings
        byte[] bytes1 = {0,1};
        assertEquals("bytes 00 01 from bfrange <0001> <0009> <0041>", "A", cMap.lookup(bytes1, 0, 2));

        byte[] bytes2 = {1,00};
        String str2 = "0";
        assertEquals("bytes 01 00 from bfrange <0100> <0109> <0030>", str2, cMap.lookup(bytes2, 0, 2));

        byte[] bytes3 = {0,10};
        String str3 = "*";
        assertEquals("bytes 00 0A from bfchar <000A> <002A>", str3, cMap.lookup(bytes3, 0, 2));

        byte[] bytes4 = {1,10};
        String str4 = "+";
        assertEquals("bytes 01 0A from bfchar <010A> <002B>", str4, cMap.lookup(bytes4, 0, 2));

        // CID mappings
        int cid1 = 65;
        assertEquals("CID 65 from cidrange <0000> <00ff> 0 ", "A", cMap.lookupCID(cid1));

        int cid2 = 280;
        String strCID2 = "\u0118";
        assertEquals("CID 280 from cidrange <0100> <01ff> 256", strCID2, cMap.lookupCID(cid2));
        
        int cid3 = 520;
        String strCID3 = "\u0208";
        assertEquals("CID 520 from cidchar <0208> 520", strCID3, cMap.lookupCID(cid3));
    }

}
