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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PfbParserTest
{
    /**
     * Test parsing a PFB font.
     *
     * @throws IOException 
     */
    @Test
    public void testPfb() throws IOException
    {
        InputStream is = new FileInputStream("target/fonts/OpenSans-Regular.pfb");
        Type1Font font = Type1Font.createWithPFB(is);
        is.close();
        Assert.assertEquals("1.10", font.getVersion());
        Assert.assertEquals("OpenSans-Regular", font.getFontName());
        Assert.assertEquals("Open Sans Regular", font.getFullName());
        Assert.assertEquals("Open Sans", font.getFamilyName());
        Assert.assertEquals("Digitized data copyright (c) 2010-2011, Google Corporation.", font.getNotice());
        Assert.assertEquals(false, font.isFixedPitch());
        Assert.assertEquals(false, font.isForceBold());
        Assert.assertEquals(0, font.getItalicAngle(), 0);
        Assert.assertEquals("Book", font.getWeight());
        Assert.assertTrue(font.getEncoding() instanceof BuiltInEncoding);
        Assert.assertEquals(4498, font.getASCIISegment().length);
        Assert.assertEquals(95911, font.getBinarySegment().length);
        Assert.assertEquals(938, font.getCharStringsDict().size());
        for (String s : font.getCharStringsDict().keySet())
        {
            Assert.assertNotNull(font.getPath(s));
            Assert.assertTrue(font.hasGlyph(s));
        }
    }

    /**
     * PDFBOX-5713: font with several binary segments.
     *
     * @throws IOException 
     */
    @Test
    public void testPfbPDFBox5713() throws IOException
    {
        InputStream is = new FileInputStream("target/fonts/DejaVuSerifCondensed.pfb");
        Type1Font font = Type1Font.createWithPFB(is);
        is.close();
        Assert.assertEquals("Version 2.33", font.getVersion());
        Assert.assertEquals("DejaVuSerifCondensed", font.getFontName());
        Assert.assertEquals("DejaVu Serif Condensed", font.getFullName());
        Assert.assertEquals("DejaVu Serif Condensed", font.getFamilyName());
        Assert.assertEquals("Copyright [c] 2003 by Bitstream, Inc. All Rights Reserved.", font.getNotice());
        Assert.assertEquals(false, font.isFixedPitch());
        Assert.assertEquals(false, font.isForceBold());
        Assert.assertEquals(0f, font.getItalicAngle(), 0);
        Assert.assertEquals("Book", font.getWeight());
        Assert.assertTrue(font.getEncoding() instanceof BuiltInEncoding);
        Assert.assertEquals(5959, font.getASCIISegment().length);
        Assert.assertEquals(1056090, font.getBinarySegment().length);
        Assert.assertEquals(3399, font.getCharStringsDict().size());
    }

    /**
     * Test 0 length font.
     */
    @Test(expected=IOException.class)
    public void testEmpty() throws IOException
    {
        Type1Font.createWithPFB(new byte[0]);
    }
}