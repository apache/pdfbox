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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.NameRecord;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.junit.Test;

/**
 * A test for correctly parsing TTF files.
 */
public class TestTTFParser
{

    /**
     * Test the post table parser.
     * 
     * @throws IOException if an error occurs.
     */
    @Test
    public void testPostTable() throws IOException
    {
        InputStream arialIs = TestTTFParser.class.getClassLoader().getResourceAsStream(
                "org/apache/pdfbox/resources/ttf/ArialMT.ttf");
        Assert.assertNotNull(arialIs);

        TTFParser parser = new TTFParser();

        TrueTypeFont arial = parser.parseTTF(arialIs);

        CMAPTable cmap = arial.getCMAP();
        Assert.assertNotNull(cmap);

        CMAPEncodingEntry[] cmaps = cmap.getCmaps();
        Assert.assertNotNull(cmaps);

        CMAPEncodingEntry uc = null;

        for (CMAPEncodingEntry e : cmaps)
        {
            if (e.getPlatformId() == NameRecord.PLATFORM_WINDOWS
                    && e.getPlatformEncodingId() == NameRecord.PLATFORM_ENCODING_WINDOWS_UNICODE)
            {
                uc = e;
                break;
            }
        }

        Assert.assertNotNull(uc);

        PostScriptTable post = arial.getPostScript();
        Assert.assertNotNull(post);

        String[] glyphNames = arial.getPostScript().getGlyphNames();
        Assert.assertNotNull(glyphNames);

        Encoding enc = new WinAnsiEncoding();

        int[] charCodes = uc.getGlyphIdToCharacterCode();
        Assert.assertNotNull(charCodes);

        for (int gid = 0; gid < charCodes.length; ++gid)
        {
            int charCode = charCodes[gid];
            String name = glyphNames[gid];
            if (charCode < 0x8000 && charCode >= 32)
            {
                if ("space".equals(name) || "slash".equals(name) || "bracketleft".equals(name)
                        || "bracketright".equals(name) || "braceleft".equals(name) || "braceright".equals(name)
                        || "product".equals(name) || "integral".equals(name) || "Omega".equals(name)
                        || "radical".equals(name) || "tilde".equals(name))
                {
                    Assert.assertTrue(enc.getNameForCharacter((char) charCode).startsWith(name));
                }
                else if ("bar".equals(name))
                {
                    Assert.assertTrue(enc.getNameForCharacter((char) charCode).endsWith(name));
                }
                else if ("sfthyphen".equals(name))
                {
                    Assert.assertEquals("softhyphen", enc.getNameForCharacter((char) charCode));
                }
                else if ("periodcentered".equals(name) && !enc.getNameForCharacter((char) charCode).equals(name))
                {
                    Assert.assertEquals("bulletoperator", enc.getNameForCharacter((char) charCode));
                }
                else if ("fraction".equals(name))
                {
                    Assert.assertEquals("divisionslash", enc.getNameForCharacter((char) charCode));
                }
                else if ("mu".equals(name))
                {
                    Assert.assertEquals("mu1", enc.getNameForCharacter((char) charCode));
                }
                else if ("pi".equals(name))
                {
                    Assert.assertEquals(0x03c0, charCode);
                }
                else
                {
                    Assert.assertEquals(enc.getNameForCharacter((char) charCode), name);
                }
            }
        }
    }
}
