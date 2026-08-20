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

import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Examples for ligatures and kerning
 * See <a href="https://issues.apache.org/jira/browse/PDFBOX-4951">PDFBOX-4951</a>
 *
 * The default processing of GlyphLayoutProcessor is with ligatures and kerning disabled.
 * You can enable ligatures and kerning using FontOptions, see below.
 *
 * @author Volker Kunert
 * @author Tilman Hausherr
 */
class GlyphLayoutLigaturesAndKerningTest extends TestBase
{
    static final String FIRACODE_STRING = "!= == === >= <=";
    static final String DEJAVU_STRING =  "AVATAR, effective, affiliation, float, film, affluent";
    static final String BENGALI_STRING =  "আমি কোন পথে ক্ষীরের লক্ষ্মী ষন্ড পুতুল রুপো গঙ্গা ঋষি";
    static final String THAI_STRING =  "กูกินก้งปิ้งอยู่ในถ้ำ";
    static final String BENGALI_STRING2 =  "হ্যালো ওয়ার্ল্ড";

    /**
     * Check that missing glyph is caught like in main pdfbox.
     *
     * @throws IOException
     * @throws FontFormatException
     */
    @Test
    void testMissingGlyph() throws IOException, FontFormatException
    {
        GlyphLayoutProcessorAwt glyphLayoutProcessor = new GlyphLayoutProcessorAwt();

        String lohitBengaliPath = "/ttf/Lohit-Bengali.ttf";

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font lohitBengaliFont = createPdType0Font(glyphLayoutProcessor, doc, lohitBengaliPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions());

            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                        showComposites(cs, lohitBengaliFont, 1, 0, 0, "123ABC"));
                assertEquals("Missing glyph in font 'Lohit Bengali' for the character 'A', codePoint: 65 (U+0041).", ex.getMessage());

                // Ignore the "You did not call endText()" warning, this is because of the premature close
            }
        }
    }

    @Test
    void testLigaturesAndKerning() throws IOException, FontFormatException, URISyntaxException
    {
        GlyphLayoutProcessorAwt glyphLayoutProcessor = new GlyphLayoutProcessorAwt();

        String outputName = "GlyphLayoutLigaturesAndKerning.pdf";
        String outputFilename = "target/" + outputName;
        String firaPath = "/ttf/FiraCode-Regular.ttf";
        String dejavuPath = "/ttf/DejaVuSans.ttf"; // ligatures not in Liberation nor in Arimo
        String thaiPath = "/ttf/NotoSansThai-Regular.ttf";
        String lohitBengaliPath = "/ttf/Lohit-Bengali.ttf";

        float fontSize = 12.0f;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font firaFont = createPdType0Font(glyphLayoutProcessor, doc, firaPath);
            PDType0Font firaLigFont = createPdType0Font(glyphLayoutProcessor, doc, firaPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions().setLigaturesOn());

            PDType0Font dejavuFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath);

            PDType0Font dejavuLigFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions().setLigaturesOn());

            PDType0Font dejavuKernFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions().setKerningOn());

            PDType0Font dejavuLigKernFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions().setLigaturesOn().setKerningOn());

            PDType0Font thaiFont = createPdType0Font(glyphLayoutProcessor, doc, thaiPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions().setLigaturesOn().setKerningOn());

            PDType0Font lohitBengaliFont = createPdType0Font(glyphLayoutProcessor, doc, lohitBengaliPath,
                    new GlyphLayoutFontLoaderAwt.FontOptions());
            
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);

                float x = page.getBBox().getLowerLeftX() + fontSize;
                float y = page.getBBox().getUpperRightY() - fontSize;
                y = showComposites(cs, firaFont, fontSize, x, y, FIRACODE_STRING);
                y = showComposites(cs, firaLigFont, fontSize, x, y, FIRACODE_STRING + " (Ligatures)");
                y = showComposites(cs, dejavuFont, fontSize, x, y, DEJAVU_STRING);
                y = showComposites(cs, dejavuLigFont, fontSize, x, y, DEJAVU_STRING + " (Ligatures)");
                y = showComposites(cs, dejavuKernFont, fontSize, x, y, DEJAVU_STRING + " (Kerning)");
                y = showComposites(cs, dejavuLigKernFont, fontSize, x, y, DEJAVU_STRING + " (Ligatures and kerning)");
                y = showComposites(cs, thaiFont, fontSize, x, y, THAI_STRING);
                y = showComposites(cs, lohitBengaliFont, fontSize, x, y - 5, BENGALI_STRING + " (ভারত)");

                // Test code coverage at the end of showTextUni ("adjust the end position")
                // Visual comparison would fail without that adjustment.
                cs.beginText();
                cs.setFont(lohitBengaliFont, 20);
                cs.newLineAtOffset(x, y - 20);
                cs.showText(BENGALI_STRING2);
                cs.showText(" ");
                cs.showText(BENGALI_STRING2);
                cs.endText();

                // Test code for the string widths - the widths be different due to kerning
                float f1 = dejavuFont.getStringWidth(DEJAVU_STRING) * dejavuFont.getFontMatrix().getScaleX() * fontSize;
                float f2 = dejavuLigKernFont.getStringWidth(DEJAVU_STRING) * dejavuLigKernFont.getFontMatrix().getScaleX() * fontSize;
                float f3 = glyphLayoutProcessor.getStringWidth(dejavuFont, fontSize, DEJAVU_STRING);
                float f4 = glyphLayoutProcessor.getStringWidth(dejavuLigKernFont, fontSize, DEJAVU_STRING);

                // equality is expected here, but this shows that the ordinary getStringWidth() isn't helpful
                assertEquals(f1, f2);

                // kerning output is obviously smaller
                assertTrue(f4 < f1);
                assertTrue(f4 < f3);

                cs.moveTo(x, 737);
                cs.lineTo(x + f3, 737);
                cs.stroke();
                cs.moveTo(x, 676);
                cs.lineTo(x + f4, 676);
                cs.stroke();
            }
            doc.save(outputFilename);
        }
        checkRenderIdent(outputName);
    }

    /**
     * break the text into lines and show them
     */
    private float showComposites(PDPageContentStream cs, PDType0Font font, float fontSize,
                                 float x, float y, String s) throws IOException
    {

        s = s.replace("\t", "    ");
        String[] lines = s.split("[\\n]");

        float height = font.getBoundingBox().getHeight();

        for (String line : lines)
        {
            if (!line.isEmpty())
            {
                showCompositesLine(cs, font, fontSize, x, y, line);
                y -= height / 1000f * fontSize;
            }
        }
        return y;
    }
}
