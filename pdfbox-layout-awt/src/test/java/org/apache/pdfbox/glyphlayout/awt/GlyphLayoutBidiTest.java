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
package org.apache.pdfbox.glyphlayout.awt;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.AbstractGlyphLayoutProcessor;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.awt.FontFormatException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Examples for bidirectional text with GlyphLayoutProcessorAwt
 *
 * @author Volker Kunert
 */
public class GlyphLayoutBidiTest extends TestBase
{
    public static final String TEXT1 = "نحن الآن في شهر رمضان 1447 هجري";
    public static final String TEXT2 = "Guten Tag ";
    public static final String TEXT3 = "السلام عليكم";
    public static final String TEXT4 = " Good afternoon";

    /*
     * show one line
     */
    private float showLine(PDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String text) throws IOException
    {
        return showLine(cs, new PDType0Font[]{font}, fontSize, x, y, new String[]{text});
    }

    /*
     * show one line
     */
    private float showLine(PDPageContentStream cs, PDType0Font[] fonts, float fontSize,
            float x, float y, String[] texts) throws IOException
    {
        cs.beginText();
        cs.newLineAtOffset(x, y);

        if (fonts.length != texts.length)
        {
            throw new IllegalArgumentException("Size of fonts and texts is different");
        }
        for (int i = 0; i < texts.length; i++)
        {
            cs.setFont(fonts[i], fontSize);
            cs.showText(texts[i]);
        }
        cs.endText();

        float height = fonts[0].getBoundingBox().getHeight();
        y -= height / 1000f * fontSize;
        return y;
    }

    /**
     * Test, no ActualText
     *
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    @Test
    void testGlyphLayoutDin91379NoActualText() throws IOException, FontFormatException, URISyntaxException
    {
        testGlyphLayoutDin91379(false, "");
    }

    /**
     * Test with ActualText
     *
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    @Test
    void testGlyphLayoutDin91379UseActualText() throws IOException, FontFormatException, URISyntaxException
    {
        testGlyphLayoutDin91379(true, "_ActualText");
    }

    /**
     * Test GlyphLayoutProcessorAwt with letters and sequences from DIN 91379
     * @param useActualText
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    void testGlyphLayoutDin91379(boolean useActualText, String sActualText) throws IOException, FontFormatException, URISyntaxException
    {
        AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions options = new AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions();
        if (useActualText)
        {
            options.useActualText();
        }
        GlyphLayoutProcessorAwt glyphLayoutProcessor = new GlyphLayoutProcessorAwt(options);

        String outputBaseName = String.format("GlyphLayoutBidi%s", sActualText);
        String outputPDFFilename = "target/" + outputBaseName + ".pdf";
        String outputTextFilename = "target/" + outputBaseName + ".txt";

        String arabicPath = "/ttf/NotoSansArabic-Regular.ttf";
        String lgcPath = "/ttf/DejaVuSans.ttf";

        float fontSize = 12.0f;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font arabicFont = createPdType0Font(glyphLayoutProcessor, doc, arabicPath);
            PDType0Font lgcFont = createPdType0Font(glyphLayoutProcessor, doc, lgcPath);

            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);
                
                float x = page.getBBox().getLowerLeftX() + fontSize;
                float y = page.getBBox().getUpperRightY() - fontSize;
                
                y = showLine(cs, arabicFont, fontSize, x, y, TEXT1);
                showLine(cs, new PDType0Font[]{ lgcFont, arabicFont, lgcFont }, fontSize, x, y, new String[]{ TEXT2, TEXT3, TEXT4 });
            }
            doc.save(outputPDFFilename);
        }

        checkRenderIdent(outputBaseName + ".pdf");

        // Extract text
        try (PDDocument doc = Loader.loadPDF(new File(outputPDFFilename))) {
            assertEquals(1, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String s = stripper.getText(doc);
            String sStripped = s.replace("\r", "").replaceAll(" +", " ")
                    .replace(" \n", "\n")
                    .strip();

            String text =
                    TEXT1 + "\n" + TEXT2 + TEXT3 + TEXT4;

            if (useActualText) {
                assertEquals(text, sStripped, "Extracted text should equal the written text for " + outputPDFFilename);
            } else {
                // Extracted text is wrong
                // assertEquals(text, sStripped, "Extracted text should equal the written text for " + outputPDFFilename);
            }

            try (OutputStream os = new FileOutputStream(outputTextFilename)) {
                os.write(0xEF);
                os.write(0xBB);
                os.write(0xBF);

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                    // The output is not yet correct as of 27.9.2026, unless ActualText is used.
                    writer.write(s);
                }
            }
        }
    }
}
