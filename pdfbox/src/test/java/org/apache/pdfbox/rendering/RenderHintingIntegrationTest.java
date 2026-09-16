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
package org.apache.pdfbox.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Proves that hinting is actually wired into the render path: when hinting is enabled, a page of
 * embedded TrueType text rasterizes to a different image than with it disabled.
 */
@Isolated // TrueTypeFont hinting is a global switch; other classes must not render while it is on
class RenderHintingIntegrationTest
{
    private static final File FONT =
            new File("src/test/resources/org/apache/pdfbox/ttf/LiberationSans-Regular.ttf");

    @AfterEach
    void restoreHinting()
    {
        TrueTypeFont.setHintingEnabled(false);
    }

    @Test
    void testHintingChangesRenderedPixels() throws IOException
    {
        byte[] pdf = buildPdf();
        BufferedImage off = render(pdf);
        TrueTypeFont.setHintingEnabled(true);
        BufferedImage on = render(pdf);

        assertEquals(off.getWidth(), on.getWidth());
        assertEquals(off.getHeight(), on.getHeight());
        assertTrue(countDifferences(off, on) > 0,
                "enabling hinting should change the rendered glyph pixels");
    }

    /** A page must rasterize identically across two renders when hinting stays disabled. */
    @Test
    void testDisabledHintingIsDeterministic() throws IOException
    {
        byte[] pdf = buildPdf();
        assertEquals(0, countDifferences(render(pdf), render(pdf)));
    }

    private static byte[] buildPdf() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(new PDRectangle(160, 60));
            doc.addPage(page);
            PDTrueTypeFont font = PDTrueTypeFont.load(doc, FONT, WinAnsiEncoding.INSTANCE);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.beginText();
                cs.setFont(font, 11);
                cs.newLineAtOffset(8, 24);
                cs.showText("Hamburgefons 123");
                cs.endText();
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static BufferedImage render(byte[] pdf) throws IOException
    {
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdf))
        {
            return new PDFRenderer(doc).renderImageWithDPI(0, 96);
        }
    }

    private static int countDifferences(BufferedImage a, BufferedImage b)
    {
        int diff = 0;
        for (int y = 0; y < a.getHeight(); y++)
        {
            for (int x = 0; x < a.getWidth(); x++)
            {
                if (a.getRGB(x, y) != b.getRGB(x, y))
                {
                    diff++;
                }
            }
        }
        return diff;
    }
}
