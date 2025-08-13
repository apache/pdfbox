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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class OverlayTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/multipdf");
    private static final File OUT_DIR = new File("target/test-output/overlay");

    @BeforeClass
    static public void setUp()
    {
        OUT_DIR.mkdirs();
    }

    @Test
    public void testOverlayOnRotatedSourcePages() throws IOException
    {
        Overlay overlay = new Overlay();
        overlay.setInputFile(IN_DIR + "/PDFBOX-6049-Source.pdf");
        overlay.setDefaultOverlayFile(IN_DIR + "/PDFBOX-6049-Overlay.pdf");
        overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
        overlay.setAdjustRotation(true);
        PDDocument resultDoc = overlay.overlay(Collections.EMPTY_MAP);
        resultDoc.save(OUT_DIR + "/PDFBOX-6049-Result.pdf");
        resultDoc.close();
        checkIdenticalRendering(new File(IN_DIR + "/PDFBOX-6049-ExpectedResult.pdf"), new File(OUT_DIR, "PDFBOX-6049-Result.pdf"));
        new File(OUT_DIR, "PDFBOX-6049-Result.pdf").delete();
        overlay.close();
    }

    private void checkIdenticalRendering(File modelFile, File resultFile) throws IOException
    {
        PDDocument modelDocument = PDDocument.load(modelFile);
        PDDocument resultDocument = PDDocument.load(resultFile);

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
        modelDocument.close();
        resultDocument.close();
        resultFile.delete();
    }
}
