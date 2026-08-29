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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.Test;

/**
 * This will test the TTFParser implementation.
 *
 * @author Tim Allison
 */
class TestTTFParser
{

    /**
     * Check whether the creation date is UTC
     *
     * @throws IOException If something went wrong
     */
    @Test
    void testUTCDate() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        //Before PDFBOX-2122, TTFDataStream was using the default TimeZone
        //Set the default to something not UTC and see if a UTC timeZone is returned
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los Angeles"));
        TTFParser parser = new TTFParser();
        TrueTypeFont ttf = parser.parse(new RandomAccessReadBufferedFile(testFile));
        Calendar created = ttf.getHeader().getCreated();
        assertEquals(created.getTimeZone(), utc);

        Calendar target = Calendar.getInstance(utc);
        target.set(2010, 5, 18, 10, 23, 22);
        target.set(Calendar.MILLISECOND, 0);
        assertEquals(target, created);
    }

    /**
     * Test the post table parser.
     * 
     * @throws IOException if an error occurs.
     */
    @Test
    void testPostTable() throws IOException
    {
        TrueTypeFont font;
        try (InputStream is = TestTTFParser.class.getResourceAsStream("/ttf/LiberationSans-Regular.ttf"))
        {
            TTFParser parser = new TTFParser();
            font = parser.parse(new RandomAccessReadBuffer(is));
        }

        CmapTable cmapTable = font.getCmap();
        assertNotNull(cmapTable);

        CmapSubtable[] cmaps = cmapTable.getCmaps();
        assertNotNull(cmaps);

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

        assertNotNull(cmap);

        PostScriptTable post = font.getPostScript();
        assertNotNull(post);

        String[] glyphNames = font.getPostScript().getGlyphNames();
        assertNotNull(glyphNames);

        // test a WGL4 (Macintosh standard) name
        int gid = cmap.getGlyphId(0x2122); // TRADE MARK SIGN
        assertEquals("trademark", glyphNames[gid]);

        // test an additional name
        gid = cmap.getGlyphId(0x20AC); // EURO SIGN
        assertEquals("Euro", glyphNames[gid]);
    }
    
    @Test
    void testParseMisc() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TTFParser parser = new TTFParser();
        TrueTypeFont ttf = parser.parse(new RandomAccessReadBufferedFile(testFile));
        KerningSubtable horizontalKerningSubtable = ttf.getKerning().getHorizontalKerningSubtable();
        assertNull(ttf.getVerticalHeader());
        assertNull(ttf.getVerticalMetrics());
        assertNull(ttf.getVerticalOrigin());
        assertTrue(horizontalKerningSubtable.isHorizontalKerning());
        assertEquals(-113, horizontalKerningSubtable.getKerning(3, 36)); // first
        assertEquals(-68, horizontalKerningSubtable.getKerning(2026, 987)); // last
        assertEquals(2048, ttf.getUnitsPerEm());
        assertEquals(1139, ttf.getAdvanceWidth(19));
        assertEquals(250, ttf.getAdvanceHeight(19)); // default
        assertEquals("[-543.9453,-303.22266,1301.7578,979.98047]", ttf.getFontBBox().toString());
        assertEquals("[4.8828125E-4, 0, 0, 4.8828125E-4, 0, 0]", ttf.getFontMatrix().toString());
        assertTrue(ttf.hasGlyph("A"));
        assertFalse(ttf.hasGlyph("blubb"));
        assertEquals("LiberationSans", ttf.toString());
        assertTrue(ttf.isEnableGsub());
        ttf.setEnableGsub(false);
        assertFalse(ttf.isEnableGsub());
    }

    @Test
    public void testParseVertical() throws IOException
    {
        File ipaFont = new File("target/fonts/ipag00303", "ipag.ttf");
        TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(ipaFont));
        VerticalHeaderTable verticalHeader = ttf.getVerticalHeader();
        assertEquals(1802, verticalHeader.getAscender());
        assertEquals(2048, verticalHeader.getAdvanceHeightMax());
        assertEquals(0, verticalHeader.getCaretSlopeRise());
        assertEquals(1, verticalHeader.getCaretSlopeRun());
        assertEquals(0, verticalHeader.getCaretOffset());
        assertEquals(246, verticalHeader.getDescender());
        assertEquals(0, verticalHeader.getLineGap());
        assertEquals(0, verticalHeader.getMetricDataFormat());
        assertEquals(-103, verticalHeader.getMinTopSideBearing());
        assertEquals(-325, verticalHeader.getMinBottomSideBearing());
        assertEquals(1f, verticalHeader.getVersion());
        assertEquals(2373, verticalHeader.getYMaxExtent());
        VerticalMetricsTable verticalMetrics = ttf.getVerticalMetrics();
        assertEquals(2048, verticalMetrics.getAdvanceHeight(19));
        assertEquals(290, verticalMetrics.getTopSideBearing(19));
        assertNull(ttf.getVerticalOrigin());
        assertEquals(1290, ttf.getAdvanceWidth(19));
        assertEquals(2048, ttf.getAdvanceHeight(19));
    }
    
    @Test
    void testParseHeaders() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TTFParser parser1 = new TTFParser();
        TrueTypeFont ttf = parser1.parse(new RandomAccessReadBufferedFile(testFile));
        TTFParser parser2 = new TTFParser();
        FontHeaders headers = parser2.parseTableHeaders(new RandomAccessReadBufferedFile(testFile));
        assertEquals(ttf.getName(), headers.getName());
        assertFalse(headers.isOpenTypePostScript());
        assertEquals(ttf.getNaming().getFontFamily(), headers.getFontFamily());
        assertEquals(ttf.getNaming().getFontSubFamily(), headers.getFontSubFamily());
        assertEquals(ttf.getOS2Windows().getCapHeight(),headers.getOS2Windows().getCapHeight());
        assertEquals(ttf.getOS2Windows().getHeight(),headers.getOS2Windows().getHeight());
        assertEquals(ttf.getOS2Windows().getWeightClass(),headers.getOS2Windows().getWeightClass());
        assertEquals(ttf.getOS2Windows().getWidthClass(),headers.getOS2Windows().getWidthClass());
    }

}
