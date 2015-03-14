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

import java.io.IOException;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;

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
    public void testCreateEmptyPdf() throws Exception
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
