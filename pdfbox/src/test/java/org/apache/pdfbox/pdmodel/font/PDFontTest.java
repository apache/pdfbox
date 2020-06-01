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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author adam
 * @author Tilman Hausherr
 */
public class PDFontTest
{
    private static final File OUT_DIR = new File("target/test-output");

    @Before
    public void setUp() throws Exception
    {
        OUT_DIR.mkdirs();
    }

    /**
     * Test of the error reported in PDFBOX-988
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testPDFBox988() throws IOException, URISyntaxException
    {
        try (PDDocument doc = 
                Loader.loadPDF(new File(PDFontTest.class.getResource("F001u_3_7j.pdf").toURI())))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            renderer.renderImage(0);
            // the allegation is that renderImage() will crash the JVM or hang
        }
    }

    /**
     * PDFBOX-3747: Test that using "-" with Calibri in Windows 7 has "-" in text extraction and not
     * \u2010, which was because of a wrong ToUnicode mapping because prior to the bugfix,
     * CmapSubtable#getCharCodes provided values in random order.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox3747() throws IOException
    {
        File file = new File("c:/windows/fonts", "calibri.ttf");
        if (!file.exists())
        {
            System.out.println("testPDFBox3747 skipped");
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDFont font = PDType0Font.load(doc, file);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.beginText();
                cs.setFont(font, 10);
                cs.showText("PDFBOX-3747");
                cs.endText();
            }
            doc.save(baos);
        }

        try (PDDocument doc = Loader.loadPDF(baos.toByteArray()))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            Assert.assertEquals("PDFBOX-3747", text.trim());
        }
    }

    /**
     * PDFBOX-3826: Test ability to reuse a TrueTypeFont created from a file or a stream for several
     * PDFs to avoid parsing it over and over again. Also check that full or partial embedding is
     * done, and do render and text extraction.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testPDFBox3826() throws IOException, URISyntaxException
    {
        URL url = PDFont.class.getResource(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
        File fontFile = new File(url.toURI());

        try (TrueTypeFont ttf1 = new TTFParser().parse(fontFile))
        {
            testPDFBox3826checkFonts(testPDFBox3826createDoc(ttf1), fontFile);
        }

        try (TrueTypeFont ttf2 = new TTFParser().parse(new FileInputStream(fontFile)))
        {
            testPDFBox3826checkFonts(testPDFBox3826createDoc(ttf2), fontFile);
        }
    }

    /**
     * PDFBOX-4115: Test ability to create PDF with german umlaut glyphs with a type 1 font.
     * Test for everything that went wrong before this was fixed.
     *
     * @throws IOException 
     */
    @Test
    public void testPDFBOX4115() throws IOException
    {
        File fontFile = new File("target/fonts", "n019003l.pfb");
        File outputFile = new File(OUT_DIR, "FontType1.pdf");
        String text = "äöüÄÖÜ";

        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page))
            {
                PDType1Font font = new PDType1Font(doc, new FileInputStream(fontFile), WinAnsiEncoding.INSTANCE);

                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(10, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            doc.addPage(page);

            doc.save(outputFile);
        }
        try (PDDocument doc = Loader.loadPDF(outputFile))
        {
            PDType1Font font = (PDType1Font) doc.getPage(0).getResources().getFont(COSName.getPDFName("F1"));
            Assert.assertEquals(WinAnsiEncoding.INSTANCE, font.getEncoding());
            
            for (char c : text.toCharArray())
            {
                String name = font.getEncoding().getName(c);
                Assert.assertEquals("dieresis", name.substring(1));
                Assert.assertFalse(font.getPath(name).getBounds2D().isEmpty());
            }

            PDFTextStripper stripper = new PDFTextStripper();
            Assert.assertEquals(text, stripper.getText(doc).trim());
        }
    }

    /**
     * Test whether bug from PDFBOX-4318 is fixed, which had the wrong cache key.
     * @throws java.io.IOException
     */
    @Test
    public void testPDFox4318() throws IOException
    {
        Assert.assertThrows("should have thrown IllegalArgumentException",
                            IllegalArgumentException.class,
                            () -> PDType1Font.HELVETICA_BOLD.encode("\u0080"));
        PDType1Font.HELVETICA_BOLD.encode("€");
        Assert.assertThrows("should have thrown IllegalArgumentException",
                            IllegalArgumentException.class,
                            () -> PDType1Font.HELVETICA_BOLD.encode("\u0080"));
    }

    private void testPDFBox3826checkFonts(byte[] byteArray, File fontFile) throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(byteArray))
        {
            PDPage page2 = doc.getPage(0);
            
            // F1 = type0 subset
            PDType0Font fontF1 = (PDType0Font) page2.getResources().getFont(COSName.getPDFName("F1"));
            Assert.assertTrue(fontF1.getName().contains("+"));
            Assert.assertTrue(fontFile.length() > fontF1.getFontDescriptor().getFontFile2().toByteArray().length);
            
            // F2 = type0 full embed
            PDType0Font fontF2 = (PDType0Font) page2.getResources().getFont(COSName.getPDFName("F2"));
            Assert.assertFalse(fontF2.getName().contains("+"));
            Assert.assertEquals(fontFile.length(), fontF2.getFontDescriptor().getFontFile2().toByteArray().length);
            
            // F3 = tt full embed
            PDTrueTypeFont fontF3 = (PDTrueTypeFont) page2.getResources().getFont(COSName.getPDFName("F3"));
            Assert.assertFalse(fontF2.getName().contains("+"));
            Assert.assertEquals(fontFile.length(), fontF3.getFontDescriptor().getFontFile2().toByteArray().length);
            
            new PDFRenderer(doc).renderImage(0);
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            String text = stripper.getText(doc);
            Assert.assertEquals("testMultipleFontFileReuse1\ntestMultipleFontFileReuse2\ntestMultipleFontFileReuse3", text.trim());
        }
    }

    private byte[] testPDFBox3826createDoc(TrueTypeFont ttf) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            // type 0 subset embedding
            PDFont font = PDType0Font.load(doc, ttf, true);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.beginText();
                cs.newLineAtOffset(10, 700);
                cs.setFont(font, 10);
                cs.showText("testMultipleFontFileReuse1");
                cs.endText();
                // type 0 full embedding
                font = PDType0Font.load(doc, ttf, false);
                cs.beginText();
                cs.newLineAtOffset(10, 650);
                cs.setFont(font, 10);
                cs.showText("testMultipleFontFileReuse2");
                cs.endText();
                // tt full embedding but only WinAnsiEncoding
                font = PDTrueTypeFont.load(doc, ttf, WinAnsiEncoding.INSTANCE);
                cs.beginText();
                cs.newLineAtOffset(10, 600);
                cs.setFont(font, 10);
                cs.showText("testMultipleFontFileReuse3");
                cs.endText();
            }

            doc.save(baos);
        }
        return baos.toByteArray();
    }

    /**
     * Check that font can be deleted after usage.
     * 
     * @throws IOException 
     */
    @Test
    public void testDeleteFont() throws IOException
    {
        File tempFontFile = new File(OUT_DIR, "LiberationSans-Regular.ttf");
        File tempPdfFile = new File(OUT_DIR, "testDeleteFont.pdf");
        String text = "Test PDFBOX-4823";

        try (InputStream is = PDFont.class.getResourceAsStream(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"))
        {
            Files.copy(is, tempFontFile.toPath());
        }

        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                PDFont font = PDType0Font.load(doc, tempFontFile);
                cs.beginText();
                cs.setFont(font, 50);
                cs.newLineAtOffset(50, 700);
                cs.showText(text);
                cs.endText();
            }
            doc.save(tempPdfFile);
        }

        Files.delete(tempFontFile.toPath());    

        try (PDDocument doc = Loader.loadPDF(tempPdfFile))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(doc);
            Assert.assertEquals(text, extractedText.trim());
        }

        Files.delete(tempPdfFile.toPath());    
    }
}
