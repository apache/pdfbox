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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class OverlayTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/multipdf");
    private static final File OUT_DIR = new File("target/test-output/overlay");

    @BeforeAll
    static void setUp()
    {
        OUT_DIR.mkdirs();
    }

    @Test
    void testRotatedOverlays() throws IOException
    {
        testRotatedOverlay(0);
        testRotatedOverlay(90);
        testRotatedOverlay(180);
        testRotatedOverlay(270);
    }

    @Test
    void testRotatedOverlaysMap() throws IOException
    {
        // multiply base image
        try (PDDocument baseDocument = Loader.loadPDF(new File(IN_DIR, "OverlayTestBaseRot0.pdf"));
             PDDocument doc = new PDDocument())
        {
            for (int p = 0; p < 4; ++p)
            {
                doc.importPage(baseDocument.getPage(0));
            }
            doc.save(new File(OUT_DIR, "OverlayTestBaseRot0_4Pages.pdf"));
        }

        // do the overlaying
        try (PDDocument baseDocument = Loader.loadPDF(new File(OUT_DIR, "OverlayTestBaseRot0_4Pages.pdf"));
             Overlay overlay = new Overlay())
        {
            Map<Integer, String> specificPageOverlayMap = new HashMap<>();
            Assertions.assertThrows(IllegalArgumentException.class, () -> overlay.overlay(specificPageOverlayMap));
            specificPageOverlayMap.put(1, new File(IN_DIR, "rot0.pdf").getAbsolutePath());
            specificPageOverlayMap.put(2, new File(IN_DIR, "rot90.pdf").getAbsolutePath());
            specificPageOverlayMap.put(3, new File(IN_DIR, "rot180.pdf").getAbsolutePath());
            specificPageOverlayMap.put(4, new File(IN_DIR, "rot270.pdf").getAbsolutePath());
            overlay.setInputPDF(baseDocument);
            try (PDDocument overlayedResultPDF = overlay.overlay(specificPageOverlayMap))
            {
                List<PDDocument> documentList = new Splitter().split(overlayedResultPDF);
                documentList.get(0).save(new File(OUT_DIR, "Overlayed-with-rot0.pdf"));
                documentList.get(1).save(new File(OUT_DIR, "Overlayed-with-rot90.pdf"));
                documentList.get(2).save(new File(OUT_DIR, "Overlayed-with-rot180.pdf"));
                documentList.get(3).save(new File(OUT_DIR, "Overlayed-with-rot270.pdf"));

                checkIdenticalRendering(new File(IN_DIR, "Overlayed-with-rot0.pdf"),
                                        new File(OUT_DIR, "Overlayed-with-rot0.pdf"));
                checkIdenticalRendering(new File(IN_DIR, "Overlayed-with-rot90.pdf"),
                                        new File(OUT_DIR, "Overlayed-with-rot90.pdf"));
                checkIdenticalRendering(new File(IN_DIR, "Overlayed-with-rot180.pdf"),
                                        new File(OUT_DIR, "Overlayed-with-rot180.pdf"));
                checkIdenticalRendering(new File(IN_DIR, "Overlayed-with-rot270.pdf"),
                                        new File(OUT_DIR, "Overlayed-with-rot270.pdf"));
            }
        }
        new File(OUT_DIR, "OverlayTestBaseRot0_4Pages.pdf").delete();
    }

    @Test
    void testOverlayOnRotatedSourcePages() throws IOException
    {
        try (Overlay overlay = new Overlay())
        {
            overlay.setInputFile(IN_DIR + "/PDFBOX-6049-Source.pdf");
            overlay.setDefaultOverlayFile(IN_DIR + "/PDFBOX-6049-Overlay.pdf");
            overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
            overlay.setAdjustRotation(true);
            try (PDDocument resultDoc = overlay.overlay(Collections.emptyMap()))
            {
                resultDoc.save(OUT_DIR + "/PDFBOX-6049-Result.pdf");
            }
            checkIdenticalRendering(new File(IN_DIR + "/PDFBOX-6049-ExpectedResult.pdf"), new File(OUT_DIR, "PDFBOX-6049-Result.pdf"));
            new File(OUT_DIR, "PDFBOX-6049-Result.pdf").delete();
        }
    }

    private void testRotatedOverlay(int rotation) throws IOException
    {
        // do the overlaying
        try (PDDocument baseDocument = Loader.loadPDF(new File(IN_DIR, "OverlayTestBaseRot0.pdf"));
             Overlay overlay = new Overlay())
        {
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

        checkIdenticalRendering(modelFile, resultFile);
    }

    private void checkIdenticalRendering(File modelFile, File resultFile) throws IOException
    {
        try (PDDocument modelDocument = Loader.loadPDF(modelFile);
             PDDocument resultDocument = Loader.loadPDF(resultFile))
        {
            assertEquals(modelDocument.getNumberOfPages(), resultDocument.getNumberOfPages());
            for (int page = 0; page < modelDocument.getNumberOfPages(); ++page)
            {
                BufferedImage modelImage = new PDFRenderer(modelDocument).renderImage(page);
                BufferedImage resultImage = new PDFRenderer(resultDocument).renderImage(page);

                // compare images
                assertEquals(modelImage.getWidth(), resultImage.getWidth());
                assertEquals(modelImage.getHeight(), resultImage.getHeight());
                assertEquals(modelImage.getType(), resultImage.getType());

                DataBufferInt modelDataBuffer = (DataBufferInt) modelImage.getRaster().getDataBuffer();
                DataBufferInt resultDataBuffer = (DataBufferInt) resultImage.getRaster().getDataBuffer();

                assertArrayEquals(modelDataBuffer.getData(), resultDataBuffer.getData());
            }
        }
        resultFile.delete();
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
