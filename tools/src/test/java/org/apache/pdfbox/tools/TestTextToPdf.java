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
    
    public void testWrap() throws IOException
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
        PDDocument doc = pdfCreator.createPDFFromText(reader);
        doc.save(baos);
        doc.close();

        doc = PDDocument.load(baos.toByteArray());
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setLineSeparator("\n");
        String text = stripper.getText(doc);
        assertEquals(expectedText, text.trim());
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
