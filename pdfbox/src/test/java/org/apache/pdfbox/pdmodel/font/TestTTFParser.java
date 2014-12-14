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
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.CmapTable;
import org.apache.fontbox.ttf.NameRecord;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.junit.Assert;
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
        InputStream input = TestTTFParser.class.getClassLoader().getResourceAsStream(
                "org/apache/pdfbox/ttf/LiberationSans-Regular.ttf");
        Assert.assertNotNull(input);

        TTFParser parser = new TTFParser();
        TrueTypeFont font = parser.parse(input);

        CmapTable cmapTable = font.getCmap();
        Assert.assertNotNull(cmapTable);

        CmapSubtable[] cmaps = cmapTable.getCmaps();
        Assert.assertNotNull(cmaps);

        CmapSubtable cmap = null;

        for (CmapSubtable e : cmaps)
        {
            if (e.getPlatformId() == NameRecord.PLATFORM_WINDOWS
                    && e.getPlatformEncodingId() == NameRecord.ENCODING_WINDOWS_UNICODE_BMP)
            {
                cmap = e;
                break;
            }
        }

        Assert.assertNotNull(cmap);

        PostScriptTable post = font.getPostScript();
        Assert.assertNotNull(post);

        String[] glyphNames = font.getPostScript().getGlyphNames();
        Assert.assertNotNull(glyphNames);

        // test a WGL4 (Macintosh standard) name
        int gid = cmap.getGlyphId(0x2122); // TRADE MARK SIGN
        Assert.assertEquals("trademark", glyphNames[gid]);

        // test an additional name
        gid = cmap.getGlyphId(0x20AC); // EURO SIGN
        Assert.assertEquals("Euro", glyphNames[gid]);
    }
}
