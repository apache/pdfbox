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

package org.apache.pdfbox.pdmodel.font;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.MacRomanEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;

import junit.framework.TestCase;

/**
 * Tests font encoding.
 *
 */
public class TestFontEncoding extends TestCase
{
    /**
     * Test the add method of a font encoding.
     */
    public void testAdd() throws Exception
    {
        // see PDFDBOX-3332
        int codeForSpace = WinAnsiEncoding.INSTANCE.getNameToCodeMap().get("space");
        assertEquals(32, codeForSpace);

        codeForSpace = MacRomanEncoding.INSTANCE.getNameToCodeMap().get("space");
        assertEquals(32, codeForSpace);
    }

    public void testOverwrite() throws Exception
    {
        // see PDFDBOX-3332
        COSDictionary dictEncodingDict = new COSDictionary();
        dictEncodingDict.setItem(COSName.TYPE, COSName.ENCODING);
        dictEncodingDict.setItem(COSName.BASE_ENCODING, COSName.WIN_ANSI_ENCODING);
        COSArray differences = new COSArray();
        differences.add(COSInteger.get(32));
        differences.add(COSName.getPDFName("a"));
        dictEncodingDict.setItem(COSName.DIFFERENCES, differences);
        DictionaryEncoding dictEncoding = new DictionaryEncoding(dictEncodingDict, false, null);
        assertNull(dictEncoding.getNameToCodeMap().get("space"));
        assertEquals(32, dictEncoding.getNameToCodeMap().get("a").intValue());
    }
}
