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
package org.apache.pdfbox.glyphlayout.fop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 *
 * @author Tilman Hausherr
 * @author Volker Kunert
 */
class TestBase
{
    void checkRenderIdent(String outputName) throws IOException, URISyntaxException
    {
        BufferedImage expectedImage;
        BufferedImage actualImage;
        try (PDDocument doc = Loader.loadPDF(new File("target/" + outputName)))
        {
            PDFRenderer r = new PDFRenderer(doc);
            expectedImage = r.renderImage(0);
        }
        try (PDDocument doc = Loader.loadPDF(new File(TestBase.class.getResource("/pdf/" + outputName).toURI())))
        {
            PDFRenderer r = new PDFRenderer(doc);
            actualImage = r.renderImage(0);
        }

        // copied from ValidateXImage.checkIdent()
        int w = expectedImage.getWidth();
        int h = expectedImage.getHeight();
        assertEquals(w, actualImage.getWidth());
        assertEquals(h, actualImage.getHeight());
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                int p1 = expectedImage.getRGB(x, y);
                int p2 = actualImage.getRGB(x, y);
                if (p1 != p2)
                {
                    String errMsg = String.format("(%d,%d) expected: <%08X> but was: <%08X>; ", 
                            x, y, p1, p2);
                    fail(errMsg);
                }
            }
        }
    }

    /**
     * Create the PDType0Font font
     */
    PDType0Font createPdType0Font(GlyphLayoutProcessorFop glyphLayoutProcessor, PDDocument doc,
            String fontPath) throws IOException
    {
        try (InputStream fontStream = this.getClass().getResourceAsStream(fontPath))
        {
            return glyphLayoutProcessor.loadFont(doc, fontStream);
        }
    }


    /*
     * show one line
     */
    void showCompositesLine(PDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String line) throws IOException
    {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(line);
        cs.endText();
    }
}
