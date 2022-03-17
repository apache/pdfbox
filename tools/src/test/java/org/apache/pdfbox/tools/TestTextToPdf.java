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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Test suite for TextToPDF.
 */
public class TestTextToPdf extends TestCase
{
    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     *
     * @throws IOException If there is an error creating the test.
     */
    public TestTextToPdf( String name ) throws IOException
    {
        super( name );
    }

    /**
     * This test ensures that a PDF created from an empty String is still readable by Adobe Reader
     */
    public void testCreateEmptyPdf() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        StringReader reader = new StringReader("");
        PDDocument pdfDoc = pdfCreator.createPDFFromText(reader);
        reader.close();

        // In order for the PDF document to be openable by Adobe Reader, it needs
        // to have some pages in it. So we'll check that.
        int pageCount = pdfDoc.getNumberOfPages();
        assertNotNull("All Pages was unexpectedly zero.", pageCount);
        assertEquals("Wrong number of pages.", 1, pageCount);
        pdfDoc.close();
    }
    
    /**
     * Tests that the form feed is properly processed.
     * 
     * @throws IOException 
     */
    public void testFormFeed() throws IOException
    {
        TextToPDF pdfCreator = new TextToPDF();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringReader reader = new StringReader("First page\fSecond page");
        PDDocument doc = pdfCreator.createPDFFromText(reader);
        doc.save(baos);
        doc.close();
        doc = PDDocument.load(baos.toByteArray());
        assertEquals(2, doc.getNumberOfPages());
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        assertEquals("First page", stripper.getText(doc).trim());
        stripper.setStartPage(2);
        stripper.setEndPage(2);
        assertEquals("Second page", stripper.getText(doc).trim());
        doc.close();
    }

    /**
     * Tests x overflow so that new line is used, and overflow on the y axis so new page must be
     * created.
     *
     * @throws IOException
     */
    public void testOverflow() throws IOException
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
                + "Ut wisi enim ad minim veniam, quis nostrud\n"
                + "exerci tation ullamcorper suscipit lobortis nisl ut\n"
                + "aliquip ex ea commodo consequat. Duis autem\n"
                + "vel eum iriure dolor in hendrerit in vulputate velit\n"
                + "esse molestie consequat, vel illum dolore eu\n"
                + "feugiat nulla facilisis at vero eros et accumsan et\n"
                + "iusto odio dignissim qui blandit praesent\n"
                + "luptatum zzril delenit augue duis dolore te feugait\n"
                + "nulla facilisi.\n"
                + "Nam liber tempor cum soluta nobis eleifend\n"
                + "option congue nihil imperdiet doming id quod\n"
                + "mazim placerat facer.";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDDocument doc = pdfCreator.createPDFFromText(reader);
        doc.save(baos);
        doc.close();
        doc = PDDocument.load(baos.toByteArray());
        assertEquals(2, doc.getNumberOfPages());
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setLineSeparator("\n");
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        assertEquals(expectedPage1Text, stripper.getText(doc).trim());
        stripper.setStartPage(2);
        stripper.setEndPage(2);
        assertEquals(expectedPage2Text, stripper.getText(doc).trim());
        doc.close();
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite( TestTextToPdf.class );
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestTextToPdf.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
