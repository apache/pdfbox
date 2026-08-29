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
        InputStream is = TestTTFParser.class.getResourceAsStream("/ttf/LiberationSans-Regular.ttf");
        TTFParser parser = new TTFParser();
        TrueTypeFont font = parser.parse(is);
        is.close();

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

    @Test
    public void testParseMisc() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TTFParser parser = new TTFParser();
        TrueTypeFont ttf = parser.parse(testFile);
        KerningSubtable horizontalKerningSubtable = ttf.getKerning().getHorizontalKerningSubtable();
        Assert.assertNull(ttf.getVerticalHeader());
        Assert.assertNull(ttf.getVerticalMetrics());
        Assert.assertNull(ttf.getVerticalOrigin());
        Assert.assertTrue(horizontalKerningSubtable.isHorizontalKerning());
        Assert.assertEquals(-113, horizontalKerningSubtable.getKerning(3, 36)); // first
        Assert.assertEquals(-68, horizontalKerningSubtable.getKerning(2026, 987)); // last
        Assert.assertEquals(2048, ttf.getUnitsPerEm());
        Assert.assertEquals(1139, ttf.getAdvanceWidth(19));
        Assert.assertEquals(250, ttf.getAdvanceHeight(19)); // default
        Assert.assertEquals("[-543.9453,-303.22266,1301.7578,979.98047]", ttf.getFontBBox().toString());
        Assert.assertEquals("[4.8828125E-4, 0, 0, 4.8828125E-4, 0, 0]", ttf.getFontMatrix().toString());
        Assert.assertTrue(ttf.hasGlyph("A"));
        Assert.assertFalse(ttf.hasGlyph("blubb"));
        Assert.assertEquals("LiberationSans", ttf.toString());
    }

    @Test
    public void testParseVertical() throws IOException
    {
        File ipaFont = new File("target/fonts/ipag00303", "ipag.ttf");
        TrueTypeFont ttf = new TTFParser().parse(ipaFont);
        VerticalHeaderTable verticalHeader = ttf.getVerticalHeader();
        Assert.assertEquals(1802, verticalHeader.getAscender());
        Assert.assertEquals(2048, verticalHeader.getAdvanceHeightMax());
        Assert.assertEquals(0, verticalHeader.getCaretSlopeRise());
        Assert.assertEquals(1, verticalHeader.getCaretSlopeRun());
        Assert.assertEquals(0, verticalHeader.getCaretOffset());
        Assert.assertEquals(246, verticalHeader.getDescender());
        Assert.assertEquals(0, verticalHeader.getLineGap());
        Assert.assertEquals(0, verticalHeader.getMetricDataFormat());
        Assert.assertEquals(-103, verticalHeader.getMinTopSideBearing());
        Assert.assertEquals(-325, verticalHeader.getMinBottomSideBearing());
        Assert.assertEquals(1f, verticalHeader.getVersion(), 0);
        Assert.assertEquals(2373, verticalHeader.getYMaxExtent());
        VerticalMetricsTable verticalMetrics = ttf.getVerticalMetrics();
        Assert.assertEquals(2048, verticalMetrics.getAdvanceHeight(19));
        Assert.assertEquals(290, verticalMetrics.getTopSideBearing(19));
        Assert.assertNull(ttf.getVerticalOrigin());
        Assert.assertEquals(1290, ttf.getAdvanceWidth(19));
        Assert.assertEquals(2048, ttf.getAdvanceHeight(19));
    }
}