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
package org.apache.fontbox.pfb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.fontbox.encoding.BuiltInEncoding;
import org.apache.fontbox.encoding.StandardEncoding;
import org.apache.fontbox.type1.Type1Font;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class PfbParserTest
{
    /**
     * Test parsing a PFB font.
     *
     * @throws IOException 
     */
    @Test
    void testPfb() throws IOException
    {
        Type1Font font;
        try (InputStream is = new FileInputStream("target/fonts/OpenSans-Regular.pfb"))
        {
            font = Type1Font.createWithPFB(is);
        }
        Assertions.assertEquals("1.10", font.getVersion());
        Assertions.assertEquals("OpenSans-Regular", font.getFontName());
        Assertions.assertEquals("Open Sans Regular", font.getFullName());
        Assertions.assertEquals("Open Sans", font.getFamilyName());
        Assertions.assertEquals("Digitized data copyright (c) 2010-2011, Google Corporation.", font.getNotice());
        Assertions.assertFalse(font.isFixedPitch());
        Assertions.assertFalse(font.isForceBold());
        Assertions.assertEquals(0, font.getItalicAngle());
        Assertions.assertEquals("Book", font.getWeight());
        Assertions.assertInstanceOf(BuiltInEncoding.class, font.getEncoding());
        Assertions.assertEquals(4498, font.getASCIISegment().length);
        Assertions.assertEquals(95911, font.getBinarySegment().length);
        Assertions.assertEquals(938, font.getCharStringsDict().size());
        for (String s : font.getCharStringsDict().keySet())
        {
            Assertions.assertNotNull(font.getPath(s));
            Assertions.assertTrue(font.hasGlyph(s));
        }
    }

    /**
     * PDFBOX-5713: font with several binary segments.
     *
     * @throws IOException 
     */
    @Test
    void testPfbPDFBox5713() throws IOException
    {
        Type1Font font;
        try (InputStream is = new FileInputStream("target/fonts/DejaVuSerifCondensed.pfb"))
        {
            font = Type1Font.createWithPFB(is);
        }
        Assertions.assertEquals("Version 2.33", font.getVersion());
        Assertions.assertEquals("DejaVuSerifCondensed", font.getFontName());
        Assertions.assertEquals("DejaVu Serif Condensed", font.getFullName());
        Assertions.assertEquals("DejaVu Serif Condensed", font.getFamilyName());
        Assertions.assertEquals("Copyright [c] 2003 by Bitstream, Inc. All Rights Reserved.", font.getNotice());
        Assertions.assertFalse(font.isFixedPitch());
        Assertions.assertFalse(font.isForceBold());
        Assertions.assertEquals(0, font.getItalicAngle());
        Assertions.assertEquals("Book", font.getWeight());
        Assertions.assertInstanceOf(BuiltInEncoding.class, font.getEncoding());
        Assertions.assertEquals(5959, font.getASCIISegment().length);
        Assertions.assertEquals(1056090, font.getBinarySegment().length);
        Assertions.assertEquals(3399, font.getCharStringsDict().size());
    }

    /**
     * PDFBOX-3654: font with hex encoded binary segment.
     *
     * @throws IOException 
     */
    @Test
    void testPfbPDFBox3654() throws IOException
    {
        byte[] ba = Files.readAllBytes(Paths.get("target/fonts/KIX-Barcode-Regular.pfb"));
        Type1Font font = Type1Font.createWithSegments(Arrays.copyOfRange(ba, 0, 1039), 
                                  Arrays.copyOfRange(ba, 1039, 1039 + 26868));
        Assertions.assertEquals("001.000", font.getVersion());
        Assertions.assertEquals("KIX-Barcode-Regular", font.getFontName());
        Assertions.assertEquals("KIX-Barcode-Regular", font.getFullName());
        Assertions.assertEquals("KIX-Barcode", font.getFamilyName());
        Assertions.assertEquals("", font.getNotice());
        Assertions.assertFalse(font.isFixedPitch());
        Assertions.assertFalse(font.isForceBold());
        Assertions.assertEquals(0, font.getItalicAngle());
        Assertions.assertEquals("Regular", font.getWeight());
        Assertions.assertInstanceOf(StandardEncoding.class, font.getEncoding());
        Assertions.assertEquals(1039, font.getASCIISegment().length);
        Assertions.assertEquals(26868, font.getBinarySegment().length);
        Assertions.assertEquals(257, font.getCharStringsDict().size());
    }

    /**
     * Test 0 length font.
     */
    @Test
    void testEmpty()
    {
        IOException ex1 = Assertions.assertThrows(IOException.class,
                () -> Type1Font.createWithPFB(new byte[0]));
        Assertions.assertEquals("Start marker missing", ex1.getMessage());
    }

    /**
     * Test some bad fonts.
     */
    @Test
    void testMiscBadFonts()
    {
        byte[] ba = new byte[PfbParser.PFB_HEADER_LENGTH + 1];
        IOException ex1 = Assertions.assertThrows(IOException.class,
                () -> Type1Font.createWithPFB(ba));
        Assertions.assertEquals("Start marker missing", ex1.getMessage());
        ba[0] = (byte) PfbParser.START_MARKER;
        ba[1] = 33;
        IOException ex2 = Assertions.assertThrows(IOException.class,
                () -> Type1Font.createWithPFB(ba));
        Assertions.assertEquals("Incorrect record type: 33", ex2.getMessage());
    }

    /**
     * Test that a PFB with a negative size field (integer overflow) throws IOException
     * instead of NegativeArraySizeException. A crafted 18-byte PFB with size bytes
     * 01 00 00 FF overflows the signed int to -16777215, bypassing the upper-bound check.
     */
    @Test
    void testNegativeRecordSize()
    {
        // 18-byte crafted PFB: start marker 0x80, ASCII type 0x01,
        // size field 0x01 0x00 0x00 0xFF = -16777215 as signed int
        byte[] crashInput = {
            (byte) 0x80, 0x01,                         // header
            0x01, 0x00, 0x00, (byte) 0xFF,             // size: overflows to negative
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,     // garbage data
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            0x27, 0x05, (byte) 0xF8, (byte) 0xFF,
            (byte) 0xD2, 0x40
        };
        IOException ex = Assertions.assertThrows(IOException.class, () -> new PfbParser(crashInput));
        Assertions.assertEquals("record size -16777215 is negative", ex.getMessage());
    }

    /**
     * Test that a PFB with a high size field throws an exception
     */
    @Test
    void testHighRecordSize()
    {
        // 18-byte crafted PFB: start marker 0x80, ASCII type 0x01,
        // size field 0x7f 0x00 0x00 0x00 = 0x7f
        byte[] crashInput = {
            (byte) 0x80, 0x01,                         // header
            0x7f, 0x00, 0x00, 0x00,                    // size too high
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,     // garbage data
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            0x27, 0x05, (byte) 0xF8, (byte) 0xFF,
            (byte) 0xD2, 0x40
        };
        IOException ex = Assertions.assertThrows(IOException.class, () -> new PfbParser(crashInput));
        Assertions.assertEquals("EOF while reading PFB font", ex.getMessage());
    }

    /**
     * Test that a PFB with only 1 short segment throws an exception
     */
    @Test
    void test1SegmentOnly()
    {
        // 18-byte crafted PFB: start marker 0x80, ASCII type 0x01,
        // size field 0x04 0x00 0x00 0x00 = 0x04
        byte[] crashInput = {
            (byte) 0x80, 0x01,                         // header
            0x03, 0x00, 0x00, 0x00,                    // size
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF      // garbage data
        };
        IOException ex = Assertions.assertThrows(IOException.class, () -> new PfbParser(crashInput));
        Assertions.assertEquals("PFB header missing", ex.getMessage());
    }

    /**
     * Misc tests for better coverage.
     *
     * @throws IOException 
     */
    @Test
    void testPfbParser() throws IOException
    {
        PfbParser pfbParser = new PfbParser("target/fonts/OpenSans-Regular.pfb");
        int[] lengths = pfbParser.getLengths();
        Assertions.assertArrayEquals(new int[]{4498, 95911, 533}, lengths);
        byte[] pfbData = pfbParser.getPfbdata();
        Assertions.assertEquals(pfbParser.size(), pfbData.length);
        byte[] seg1 = pfbParser.getSegment1();
        byte[] seg2 = pfbParser.getSegment2();
        Assertions.assertEquals(pfbData.length, seg1.length + seg2.length + lengths[2]);
        Assertions.assertEquals(seg1.length, lengths[0]);
        Assertions.assertEquals(seg2.length, lengths[1]);
        Assertions.assertArrayEquals(pfbData, pfbParser.getInputStream().readAllBytes());
        byte [] ba = Files.readAllBytes(Paths.get("target/fonts/OpenSans-Regular.pfb"));
        Assertions.assertArrayEquals(
                seg1,
                Arrays.copyOfRange(ba, 6, 6 + lengths[0]));
        Assertions.assertArrayEquals(
                seg2,
                Arrays.copyOfRange(ba, 6 + lengths[0] + 6, 6 + lengths[0] + 6 + lengths[1]));
    }
}
