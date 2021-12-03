/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.ttf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * This will test the TTFParser implementation.
 *
 * @author Tim Allison
 */
public class TestTTFParser
{

    /**
     * Check whether the creation date is UTC
     *
     * @throws IOException If something went wrong
     */
    @Test
    public void testUTCDate() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        //Before PDFBOX-2122, TTFDataStream was using the default TimeZone
        //Set the default to something not UTC and see if a UTC timeZone is returned
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los Angeles"));
        TTFParser parser = new TTFParser();
        TrueTypeFont ttf = parser.parse(testFile);
        Calendar created = ttf.getHeader().getCreated();
        Assert.assertEquals(created.getTimeZone(), utc);

        Calendar target = Calendar.getInstance(utc);
        target.set(2010, 5, 18, 10, 23, 22);
        target.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(target, created);
    }

    /**
     * Test the post table parser.
     * 
     * @throws IOException if an error occurs.
     */
    @Test
    public void testPostTable() throws IOException
    {
        InputStream input = TestTTFParser.class.getResourceAsStream(
                "/ttf/LiberationSans-Regular.ttf");
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