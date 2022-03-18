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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

    /**
     * Tests that the form feed is properly processed.
     * 
     * @throws IOException 
     */
    @Test
    void testFormFeed() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringReader reader = new StringReader("First page\fSecond page\f\nThird page");
        try (PDDocument doc = pdfCreator.createPDFFromText(reader))
        {
            doc.save(baos);
        }
        try (PDDocument doc = Loader.loadPDF(baos.toByteArray()))
        {
            assertEquals(3, doc.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            assertEquals("First page", stripper.getText(doc).trim());
            stripper.setStartPage(2);
            stripper.setEndPage(2);
            assertEquals("Second page", stripper.getText(doc).trim());
            stripper.setStartPage(3);
            stripper.setEndPage(3);
            assertEquals("Third page", stripper.getText(doc).trim());
        }
    }

    /**
     * Tests x overflow so that new line is used, and overflow on the y axis so new page must be
     * created.
     *
     * @throws IOException
     */
    @Test
    void testOverflow() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        pdfCreator.setMediaBox(PDRectangle.A6);
        StringReader reader = new StringReader("Lorem ipsum dolor sit amet, consetetur sadipscing "
                + "elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam "
                + "erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. "
                + "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. "
                + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                + "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. "
                + "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd "
                + "gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem "
                + "ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod "
                + "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. "
                + "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd "
                + "gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\n"
                + "\n"
                + "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie "
                + "consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan "
                + "et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue "
                + "duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, "
                + "consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut "
                + "laoreet dolore magna aliquam erat volutpat.\n"
                + "\n"
                + "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper "
                + "suscipit lobortis nisl ut aliquip ex ea commodo consequat. "
                + "Duis autem vel eum iriure dolor in hendrerit in vulputate "
                + "velit esse molestie consequat, vel illum dolore eu feugiat nulla "
                + "facilisis at vero eros et accumsan et iusto odio dignissim qui blandit "
                + "praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.\n"
                + "\n"
                + "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming "
                + "id quod mazim placerat facer.");
        String expectedPage1Text
                = "Lorem ipsum dolor sit amet, consetetur\n"
                + "sadipscing elitr, sed diam nonumy eirmod\n"
                + "tempor invidunt ut labore et dolore magna\n"
                + "aliquyam erat, sed diam voluptua. At vero eos et\n"
                + "accusam et justo duo dolores et ea rebum. Stet\n"
                + "clita kasd gubergren, no sea takimata sanctus\n"
                + "est Lorem ipsum dolor sit amet. Lorem ipsum\n"
                + "dolor sit amet, consetetur sadipscing elitr, sed\n"
                + "diam nonumy eirmod tempor invidunt ut labore et\n"
                + "dolore magna aliquyam erat, sed diam voluptua.\n"
                + "At vero eos et accusam et justo duo dolores et\n"
                + "ea rebum. Stet clita kasd gubergren, no sea\n"
                + "takimata sanctus est Lorem ipsum dolor sit amet.\n"
                + "Lorem ipsum dolor sit amet, consetetur\n"
                + "sadipscing elitr, sed diam nonumy eirmod\n"
                + "tempor invidunt ut labore et dolore magna\n"
                + "aliquyam erat, sed diam voluptua. At vero eos et\n"
                + "accusam et justo duo dolores et ea rebum. Stet\n"
                + "clita kasd gubergren, no sea takimata sanctus\n"
                + "est Lorem ipsum dolor sit amet.\n"
                + "\n"
                + "Duis autem vel eum iriure dolor in hendrerit in\n"
                + "vulputate velit esse molestie consequat, vel illum\n"
                + "dolore eu feugiat nulla facilisis at vero eros et\n"
                + "accumsan et iusto odio dignissim qui blandit\n"
                + "praesent luptatum zzril delenit augue duis dolore\n"
                + "te feugait nulla facilisi. Lorem ipsum dolor sit\n"
                + "amet, consectetuer adipiscing elit, sed diam\n"
                + "nonummy nibh euismod tincidunt ut laoreet";
        String expectedPage2Text
                = "dolore magna aliquam erat volutpat.\n"
                + "\n"
                + "Ut wisi enim ad minim veniam, quis nostrud\n"
                + "exerci tation ullamcorper suscipit lobortis nisl ut\n"
                + "aliquip ex ea commodo consequat. Duis autem\n"
                + "vel eum iriure dolor in hendrerit in vulputate velit\n"
                + "esse molestie consequat, vel illum dolore eu\n"
                + "feugiat nulla facilisis at vero eros et accumsan et\n"
                + "iusto odio dignissim qui blandit praesent\n"
                + "luptatum zzril delenit augue duis dolore te feugait\n"
                + "nulla facilisi.\n"
                + "\n"
                + "Nam liber tempor cum soluta nobis eleifend\n"
                + "option congue nihil imperdiet doming id quod\n"
                + "mazim placerat facer.";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = pdfCreator.createPDFFromText(reader))
        {
            doc.save(baos);
        }
        try (PDDocument doc = Loader.loadPDF(baos.toByteArray()))
        {
            assertEquals(2, doc.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            stripper.setParagraphStart("\n");
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            assertEquals(expectedPage1Text, stripper.getText(doc).trim());
            stripper.setStartPage(2);
            stripper.setEndPage(2);
            assertEquals(expectedPage2Text, stripper.getText(doc).trim());
        }
    }

    /**
     * Test that leading and trailing spaces and newlines are preserved.
     * 
     * @throws IOException 
     */
    @Test
    void testLeadingTrailingSpaces() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String text = "Lorem ipsum dolor sit amet,\n"
                + "    consectetur adipiscing \n"
                + "\n"
                + "elit. sed do eiusmod";
        StringReader reader = new StringReader(text);
        try (PDDocument doc = pdfCreator.createPDFFromText(reader))
        {
            doc.save(baos);
        }
        try (PDDocument doc = Loader.loadPDF(baos.toByteArray()))
        {
            assertEquals(1, doc.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            stripper.setParagraphStart("\n");
            assertEquals(text, stripper.getText(doc).trim());
        }
    }
}
