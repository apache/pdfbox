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

/**
 * Examples for ligatures and kerning
 * See <a href="https://issues.apache.org/jira/browse/PDFBOX-4951">PDFBOX-4951</a>
 *
 * The default processing of GlyphLayoutProcessor is with ligatures and kerning disabled.
 * You can enable ligatures and kerning using FontOptions, see below.
 *
 * @author Volker Kunert
 */

import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

class GlyphLayoutLigaturesAndKerningTest extends TestBase
{
    static final String FIRACODE_STRING = "!= == === >= <=";
    static final String DEJAVU_STRING =  "AVATAR, effective, affiliation, float, film, affluent";
    static final String BENGALI_STRING =  "আমি কোন পথে ক্ষীরের লক্ষ্মী ষন্ড পুতুল রুপো গঙ্গা ঋষি";
    
    // for code coverage at the end of showTextUni
    // happens only if font size 20 and the spaces
    static final String BENGALI_STRING2 =  "    আর সবই গেছে ঋণে।";

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
                y = showComposites(cs, lohitBengaliFont, fontSize, x, y, BENGALI_STRING + " (ভারত)");
                showComposites(cs, lohitBengaliFont, 20, x, y - 20, BENGALI_STRING2 + " (ভারত)");
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

        s = s.replaceAll("\t", "    ");
        String[] lines = s.split("[\n]");

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
