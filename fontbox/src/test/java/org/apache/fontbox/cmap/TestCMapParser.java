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

import junit.framework.TestCase;

/**
 * This will test the CMapParser implementation.
 *
 * @version $Revision$
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
        
        // char mappings
        byte[] bytes1 = {0,1};
        assertTrue("A".equals(cMap.lookup(bytes1, 0, 2)));

        byte[] bytes2 = {1,00};
        String str2 = "0";
        assertTrue(str2.equals(cMap.lookup(bytes2, 0, 2)));

        byte[] bytes3 = {0,10};
        String str3 = "*";
        assertTrue(str3.equals(cMap.lookup(bytes3, 0, 2)));

        byte[] bytes4 = {1,10};
        String str4 = "+";
        assertTrue(str4.equals(cMap.lookup(bytes4, 0, 2)));

        // CID mappings
        int cid1 = 65;
        assertTrue("A".equals(cMap.lookupCID(cid1)));

        int cid2 = 280;
        String strCID2 = "\u0118";
        assertTrue(strCID2.equals(cMap.lookupCID(cid2)));
        
        int cid3 = 520;
        String strCID3 = "\u0208";
        assertTrue(strCID3.equals(cMap.lookupCID(cid3)));
    }

}
