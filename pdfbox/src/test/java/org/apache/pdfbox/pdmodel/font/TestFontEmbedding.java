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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Tests font embedding.
 *
 * @author John Hewson
 * @author Tilman Hausherr
 */
public class TestFontEmbedding extends TestCase
{
    private static final File OUT_DIR = new File("target/test-output");

    @Override
    protected void setUp() throws Exception
    {
        OUT_DIR.mkdirs();
    }

    /**
     * Embed a TTF as CIDFontType2.
     */
    public void testCIDFontType2() throws Exception
    {
        validateCIDFontType2(false);
    }

    /**
     * Embed a TTF as CIDFontType2 with subsetting.
     */
    public void testCIDFontType2Subset() throws Exception
    {
        validateCIDFontType2(true);
    }

    /**
     * Embed a monospace TTF as vertical CIDFontType2 with subsetting.
     *
     * @throws IOException
     */
    public void testCIDFontType2VerticalSubsetMonospace() throws IOException
    {
        String text = "「ABC」";
        String expectedExtractedtext = "「\nA\nB\nC\n」";
        File pdf = new File(OUT_DIR, "CIDFontType2VM.pdf");

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        File ipafont = new File("target/fonts/ipag00303", "ipag.ttf");
        PDType0Font vfont = PDType0Font.loadVertical(document, ipafont);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(vfont, 20);
        contentStream.newLineAtOffset(50, 700);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();

        // Check the font substitution
        byte[] encode = vfont.encode(text);
        int cid = ((encode[0] & 0xFF) << 8) + (encode[1] & 0xFF);
        assertEquals(7392, cid); // it's 441 without substitution

        // Check the dictionaries
        COSDictionary fontDict = vfont.getCOSObject();
        assertEquals(COSName.IDENTITY_V, fontDict.getDictionaryObject(COSName.ENCODING));

        document.save(pdf);

        // Vertical metrics are fixed during subsetting, so do this after calling save()
        COSDictionary descFontDict = vfont.getDescendantFont().getCOSObject();
        COSArray dw2 = (COSArray) descFontDict.getDictionaryObject(COSName.DW2);
        assertNull(dw2); // This font uses default values for DW2
        COSArray w2 = (COSArray) descFontDict.getDictionaryObject(COSName.W2);
        assertEquals(0, w2.size()); // Monospaced font has no entries

        document.close();

        // Check text extraction
        String extracted = getUnicodeText(pdf);        
        assertEquals(expectedExtractedtext, extracted.replaceAll("\r", "").trim());
    }

    /**
     * Embed a proportional TTF as vertical CIDFontType2 with subsetting.
     *
     * @throws IOException
     */
    public void testCIDFontType2VerticalSubsetProportional() throws IOException
    {
        String text = "「ABC」";
        String expectedExtractedtext = "「\nA\nB\nC\n」";
        File pdf = new File(OUT_DIR, "CIDFontType2VP.pdf");

        PDDocument document = new PDDocument();
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        File ipafont = new File("target/fonts/ipagp00303", "ipagp.ttf");
        PDType0Font vfont = PDType0Font.loadVertical(document, ipafont);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(vfont, 20);
        contentStream.newLineAtOffset(50, 700);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();

        // Check the font substitution
        byte[] encode = vfont.encode(text);
        int cid = ((encode[0] & 0xFF) << 8) + (encode[1] & 0xFF);
        assertEquals(12607, cid); // it's 12461 without substitution
        // Check the dictionaries
        COSDictionary fontDict = vfont.getCOSObject();
        assertEquals(COSName.IDENTITY_V, fontDict.getDictionaryObject(COSName.ENCODING));

        document.save(pdf);

        // Vertical metrics are fixed during subsetting, so do this after calling save()
        COSDictionary descFontDict = vfont.getDescendantFont().getCOSObject();
        COSArray dw2 = (COSArray) descFontDict.getDictionaryObject(COSName.DW2);
        assertNull(dw2); // This font uses default values for DW2
        // c [ w1_1y v_1x v_1y ... w1_ny v_nx v_ny ]
        COSArray w2 = (COSArray) descFontDict.getDictionaryObject(COSName.W2);
        assertEquals(2, w2.size());
        assertEquals(12607, w2.getInt(0)); // Start CID
        COSArray metrics = (COSArray) w2.getObject(1);
        int i = 0;
        for (int n : new int[] {-570, 500, 450, -570, 500, 880})
        {
            assertEquals(n, metrics.getInt(i++));
        }
        document.close();

        // Check text extraction
        String extracted = getUnicodeText(pdf);
        assertEquals(expectedExtractedtext, extracted.replaceAll("\r", "").trim());
    }

    /**
     * Test corner case of PDFBOX-4302.
     *
     * @throws java.io.IOException
     */
    public void testMaxEntries() throws IOException
    {
        File file;
        String text;
        text = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん" +
                "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン" +
                "１２３４５６７８";

        // The test must have MAX_ENTRIES_PER_OPERATOR unique characters
        Set<Character> set = new HashSet<Character>(ToUnicodeWriter.MAX_ENTRIES_PER_OPERATOR);
        for (int i = 0; i < text.length(); ++i)
        {
            set.add(text.charAt(i));
        }
        assertEquals(ToUnicodeWriter.MAX_ENTRIES_PER_OPERATOR, set.size());

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A0);
        document.addPage(page);
        File ipafont = new File("target/fonts/ipag00303", "ipag.ttf");
        PDType0Font font = PDType0Font.load(document, ipafont);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(font, 20);
        contentStream.newLineAtOffset(50, 3000);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
        file = new File(OUT_DIR, "PDFBOX-4302-test.pdf");
        document.save(file);
        document.close();

        // check that the extracted text matches what we wrote
        String extracted = getUnicodeText(file);
        assertEquals(text, extracted.trim());
    }

    private void validateCIDFontType2(boolean useSubset) throws Exception
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        InputStream input = PDFont.class.getResourceAsStream(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
        PDType0Font font = PDType0Font.load(document, input, useSubset);

        PDPageContentStream stream = new PDPageContentStream(document, page);

        stream.beginText();
        stream.setFont(font, 12);

        String text = "Unicode русский язык Tiếng Việt";
        stream.newLineAtOffset(50, 600);
        stream.showText(text);

        stream.endText();
        stream.close();
        
        File file = new File(OUT_DIR, "CIDFontType2.pdf");
        document.save(file);
        document.close();

        // check that the extracted text matches what we wrote
        String extracted = getUnicodeText(file);
        assertEquals(text, extracted.trim());
    }

    private String getUnicodeText(File file) throws IOException
    {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    }
}
