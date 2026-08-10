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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/**
 * Examples for Supplementary Multilingual Plane with GlyphLayoutProcessorAwt that require a
 * representation with two characters in Java. This is not a new functionality but a regression test
 * that needs to be checked. (Output is identical with plain PDFBox)
 *
 * @author Volker Kunert
 */
@EnabledForJreRange(min = JRE.JAVA_9)
class GlyphLayoutSMPTest extends TestBase
{
    private static final String TEXT_INTRO =
            "Test of Letters from the Supplementary Multilingual Plane, Mathematical Alphanumeric Symbols";

    private static final int[] MATHEMATICAL_CODEPOINTS = new int[]{0x1D504, 0x1D505, 0x212D, 0x1D507, 0x1D508, 0x1D509,
            0x1D50A, 0x210C, 0x2111, 0x1D50D, 0x1D50E, 0x1D50F, 0x1D510, 0x1D511, 0x1D512, 0x1D513, 0x1D514, 0x211C,
            0x1D516, 0x1D517, 0x1D518, 0x1D519, 0x1D51A, 0x1D51B, 0x1D51C, 0x2128, 0x0A, 0x1D51E, 0x1D51F, 0x1D520,
            0x1D521, 0x1D522, 0x1D523, 0x1D524, 0x1D525, 0x1D526, 0x1D527, 0x1D528, 0x1D529, 0x1D52A, 0x1D52B, 0x1D52C,
            0x1D52D, 0x1D52E, 0x1D52F, 0x1D530, 0x1D531, 0x1D532, 0x1D533, 0x1D534, 0x1D535, 0x1D536, 0x1D537,
            0x0A, 0x0A,
            0x1D7D8, 0x1D7D9, 0x1D7DA, 0x1D7DB, 0x1D7DC, 0x1D7DD, 0x1D7DE, 0x1D7DF, 0x1D7E0, 0x1D7E1,
            0x0A, 0x0A,
            0x1D49C, 0x212C, 0x1D49E, 0x1D49F, 0x2130, 0x2131, 0x1D4A2, 0x210B, 0x2110, 0x1D4A5, 0x1D4A6, 0x2112,
            0x2133, 0x1D4A9, 0x1D4AA, 0x1D4AB, 0x1D4AC, 0x211B, 0x1D4AE, 0x1D4AF, 0x1D4B0, 0x1D4B1, 0x1D4B2, 0x1D4B3,
            0x1D4B4, 0x1D4B5,
            0x0A,
            0x1D4B6, 0x1D4B7, 0x1D4B8, 0x1D4B9, 0x212F, 0x1D4BB, 0x210A, 0x1D4BD, 0x1D4BE,
            0x1D4BF, 0x1D4C0, 0x1D4C1, 0x1D4C2, 0x1D4C3, 0x2134, 0x1D4C5, 0x1D4C6, 0x1D4C7, 0x1D4C8, 0x1D4C9, 0x1D4CA,
            0x1D4CB, 0x1D4CC, 0x1D4CD, 0x1D4CE, 0x1D4CF,
            0x0A, 0x0A};

    private static final String MATHEMATICAL = new String(MATHEMATICAL_CODEPOINTS, 0, MATHEMATICAL_CODEPOINTS.length);

    /*
     * break the text into lines and show them
     */
    private float showLines(PDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String s) throws IOException
    {

        s = s.replace("\t", "    ");
        String[] lines = s.split("[\\n]");

        for (String line : lines)
        {
            if (!line.isEmpty())
            {
                y = showLine(cs, font, fontSize, x, y, line);
            }
        }
        return y;
    }


    /*
     * show one line
     */
    private float showLine(PDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String text) throws IOException
    {
        cs.beginText();
        cs.newLineAtOffset(x, y);

        cs.setFont(font, fontSize);
        cs.showText(text);
        cs.endText();

        float height = font.getBoundingBox().getHeight();
        y -= height / 1000f * fontSize;
        return y;
    }

    @Test
    void testGlyphLayoutSMP() throws IOException, FontFormatException, URISyntaxException
    {
        GlyphLayoutProcessorAwt glyphLayoutProcessor = new GlyphLayoutProcessorAwt();

        String outputName = "GlyphLayoutSMP.pdf";
        String outputFilename = "target/" + outputName;
        String sansFontPath = "/ttf/Arimo-Regular.ttf";
        String mathFontPath = "/ttf/NotoSansMath-Regular.ttf";

        float fontSize = 12.0f;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font sansFont = createPdType0Font(glyphLayoutProcessor, doc, sansFontPath);
            PDType0Font mathFont = createPdType0Font(glyphLayoutProcessor, doc, mathFontPath);

            PDPage blankPage = new PDPage();
            doc.addPage(blankPage);
            try (PDPageContentStream cs = new PDPageContentStream(doc, doc.getPage(0),
                    PDPageContentStream.AppendMode.APPEND, true))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);

                float x = blankPage.getBBox().getLowerLeftX() + fontSize;
                float y = blankPage.getBBox().getUpperRightY() - fontSize;

                y = showLine(cs, sansFont, fontSize, x, y, TEXT_INTRO);
                y = showLine(cs, sansFont, fontSize, x, y, "Font used: " + mathFont.getName());
                showLines(cs, mathFont, fontSize, x, y, MATHEMATICAL);
            }
            doc.save(outputFilename);
        }
        checkRenderIdent(outputName);
    }
}
