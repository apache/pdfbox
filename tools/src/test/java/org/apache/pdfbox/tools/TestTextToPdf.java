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
package org.apache.pdfbox.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Test suite for TextToPDF.
 */
class TestTextToPdf
{
    /**
     * This test ensures that a PDF created from an empty String is still readable by Adobe Reader
     */
    @Test
    void testCreateEmptyPdf() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        PDDocument pdfDoc;
        try (StringReader reader = new StringReader(""))
        {
            pdfDoc = pdfCreator.createPDFFromText(reader);
        }

        // In order for the PDF document to be openable by Adobe Reader, it needs
        // to have some pages in it. So we'll check that.
        int pageCount = pdfDoc.getNumberOfPages();
        assertTrue(pageCount > 0, "All Pages was unexpectedly zero.");
        assertEquals(1, pageCount, "Wrong number of pages.");
        pdfDoc.close();
    }
    
    @Test
    void testWrap() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        // single line
        StringReader reader = new StringReader("Lorem ipsum dolor sit amet, consetetur "
                + "sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore "
                + "magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo "
                + "dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est "
                + "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing "
                + "elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam "
                + "erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea "
                + "rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
                + "dolor sit amet.");
        String expectedText = 
                  "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                + "eirmod tempor invidunt ut labore et dolore\n"
                + "magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo "
                + "dolores et ea rebum. Stet clita kasd\n"
                + "gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum "
                + "dolor sit amet, consetetur sadipscing\n"
                + "elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam "
                + "erat, sed diam voluptua. At vero eos\n"
                + "et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, "
                + "no sea takimata sanctus est Lorem ipsum dolor\n"
                + "sit amet.";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = pdfCreator.createPDFFromText(reader))
        {
            doc.save(baos);
        }
        try (PDDocument doc = Loader.loadPDF(baos.toByteArray()))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            String text = stripper.getText(doc);
            assertEquals(expectedText, text.trim());
        }
    }
    
}
