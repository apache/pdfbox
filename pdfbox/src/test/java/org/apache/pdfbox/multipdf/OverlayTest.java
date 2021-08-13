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

package org.apache.pdfbox.multipdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class OverlayTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/multipdf");
    private static final File OUT_DIR = new File("target/test-output/overlay");
    
    public OverlayTest()
    {
    }

    @BeforeEach
    public void setUp()
    {
        OUT_DIR.mkdirs();
    }

    @Test
    void testRotatedOverlays() throws Exception
    {
        testRotatedOverlay(0);
        testRotatedOverlay(90);
        testRotatedOverlay(180);
        testRotatedOverlay(270);
    }

    private void testRotatedOverlay(int rotation) throws IOException
    {
        // do the overlaying
        try (PDDocument baseDocument = Loader.loadPDF(new File(IN_DIR, "OverlayTestBaseRot0.pdf")))
        {
            Overlay overlay = new Overlay();
            overlay.setInputPDF(baseDocument);
            try (PDDocument overlayDocument = Loader.loadPDF(new File(IN_DIR, "rot" + rotation + ".pdf")))
            {
                overlay.setDefaultOverlayPDF(overlayDocument);
                try (PDDocument overlayedResultPDF = overlay.overlay(new HashMap<>()))
                {
                    overlayedResultPDF.save(new File(OUT_DIR, "Overlayed-with-rot" + rotation + ".pdf"));
                }
            }
        }

        // render model and result
        File modelFile = new File(IN_DIR, "Overlayed-with-rot" + rotation + ".pdf");
        File resultFile = new File(OUT_DIR, "Overlayed-with-rot" + rotation + ".pdf");

        BufferedImage modelImage;
        try (PDDocument modelDocument = Loader.loadPDF(modelFile))
        {
            modelImage = new PDFRenderer(modelDocument).renderImage(0);
        }

        BufferedImage resultImage;
        try (PDDocument resultDocument = Loader.loadPDF(resultFile))
        {
            resultImage = new PDFRenderer(resultDocument).renderImage(0);
        }

        // compare images
        assertEquals(modelImage.getWidth(), resultImage.getWidth());
        assertEquals(modelImage.getHeight(), resultImage.getHeight());
        assertEquals(modelImage.getType(), resultImage.getType());

        DataBufferInt modelDataBuffer = (DataBufferInt) modelImage.getRaster().getDataBuffer();
        DataBufferInt resultDataBuffer = (DataBufferInt) resultImage.getRaster().getDataBuffer();

        assertArrayEquals(modelDataBuffer.getData(), resultDataBuffer.getData());
    }
    
    // code used to create the base file
    private void createBaseFile() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                float fontHeight = 12;
                float y = page.getMediaBox().getHeight() - fontHeight * 2;
                PDFont font = new PDType1Font(FontName.HELVETICA);
                cs.setFont(font, fontHeight);
                cs.beginText();
                cs.setLeading(fontHeight * 2 + 1);
                cs.newLineAtOffset(fontHeight * 2, y);
                while (y > fontHeight * 2)
                {
                    cs.showText("A quick movement of the enemy will jeopardize six gunboats. " +
                            "Heavy boxes perform quick waltzes and jigs.");
                    cs.newLine();
                    y -= fontHeight * 2;
                }
                cs.endText();
            }
            doc.addPage(page);
            doc.save("OverlayTestBaseRot0.pdf");
        }
    }
}
