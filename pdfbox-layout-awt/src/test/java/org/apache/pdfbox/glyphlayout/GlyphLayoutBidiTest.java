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
package org.apache.pdfbox.glyphlayout;

import org.junit.jupiter.api.Test;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Examples for bidirectional text with GlyphLayoutProcessorAwt
 *
 * @author Volker Kunert
 */
public class GlyphLayoutBidiTest
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

    @Test
    void testGlyphLayoutBidi() throws IOException, FontFormatException
    {
        GlyphLayoutProcessorAwt glyphLayoutProcessorAwt = new GlyphLayoutProcessorAwt();

        String outputFilename = "target/GlyphLayoutBidi.pdf";
        String arabicPath = "/ttf/NotoSansArabic-Regular.ttf";
        String lgcPath = "/ttf/DejaVuSans.ttf";

        float fontSize = 12.0f;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font arabicFont = createPdType0Font(glyphLayoutProcessorAwt, doc, arabicPath);
            PDType0Font lgcFont = createPdType0Font(glyphLayoutProcessorAwt, doc, lgcPath);

            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessorAwt);
                
                float x = page.getBBox().getLowerLeftX() + fontSize;
                float y = page.getBBox().getUpperRightY() - fontSize;
                
                y = showLine(cs, arabicFont, fontSize, x, y, TEXT1);
                showLine(cs, new PDType0Font[]{ lgcFont, arabicFont, lgcFont }, fontSize, x, y, new String[]{ TEXT2, TEXT3, TEXT4 });
            }
            doc.save(outputFilename);
        }
        //TODO add rendering comparison
        try (PDDocument doc = Loader.loadPDF(new File(outputFilename)))
        {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    /*
     * Create the PDType0Font font
     */
    private PDType0Font createPdType0Font(GlyphLayoutProcessorAwt glyphLayoutProcessorAwt, PDDocument doc,
            String fontPath) throws IOException, FontFormatException
    {
        InputStream fontStream = this.getClass().getResourceAsStream(fontPath);
        return glyphLayoutProcessorAwt.loadFont(doc, fontStream);
    }
}
