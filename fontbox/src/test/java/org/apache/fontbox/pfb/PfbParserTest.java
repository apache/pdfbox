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

import org.apache.fontbox.encoding.BuiltInEncoding;
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
        Assertions.assertEquals(false, font.isFixedPitch());
        Assertions.assertEquals(false, font.isForceBold());
        Assertions.assertEquals(0, font.getItalicAngle());
        Assertions.assertEquals("Book", font.getWeight());
        Assertions.assertTrue(font.getEncoding() instanceof BuiltInEncoding);
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
        Assertions.assertEquals(false, font.isFixedPitch());
        Assertions.assertEquals(false, font.isForceBold());
        Assertions.assertEquals(0, font.getItalicAngle());
        Assertions.assertEquals("Book", font.getWeight());
        Assertions.assertTrue(font.getEncoding() instanceof BuiltInEncoding);
        Assertions.assertEquals(5959, font.getASCIISegment().length);
        Assertions.assertEquals(1056090, font.getBinarySegment().length);
        Assertions.assertEquals(3399, font.getCharStringsDict().size());
    }

    /**
     * Test 0 length font.
     */
    @Test
    void testEmpty()
    {
        Assertions.assertThrows(IOException.class, () -> Type1Font.createWithPFB(new byte[0]));
    }
}
