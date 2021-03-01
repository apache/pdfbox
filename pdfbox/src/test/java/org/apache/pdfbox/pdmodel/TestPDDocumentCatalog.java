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
package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test PDDocument Catalog functionality.
 *
 */
class TestPDDocumentCatalog
{

    /**
     * Test getPageLabels().
     * 
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-90"
     *   >PDFBOX-90</a> - Support explicit retrieval of page labels.
     *   
     * @throws IOException in case the document can not be parsed.
     */
    @Test
    void retrievePageLabels() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(
                TestPDDocumentCatalog.class.getResourceAsStream("test_pagelabels.pdf")))
        {
            PDDocumentCatalog cat = doc.getDocumentCatalog();
            String[] labels = cat.getPageLabels().getLabelsByPageIndices();
            assertEquals(12, labels.length);
            assertEquals("A1", labels[0]);
            assertEquals("A2", labels[1]);
            assertEquals("A3", labels[2]);
            assertEquals("i", labels[3]);
            assertEquals("ii", labels[4]);
            assertEquals("iii", labels[5]);
            assertEquals("iv", labels[6]);
            assertEquals("v", labels[7]);
            assertEquals("vi", labels[8]);
            assertEquals("vii", labels[9]);
            assertEquals("Appendix I", labels[10]);
            assertEquals("Appendix II", labels[11]);
        }
    }

    /**
     * Test page labels for malformed PDF.
     * 
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-900"
     *   >PDFBOX-900</a> - Handle malformed PDFs
     *   
     * @throws IOException in case the document can not be parsed.
     */
    @Test
    void retrievePageLabelsOnMalformedPdf() throws IOException
    {
        try (PDDocument doc = Loader
                .loadPDF(TestPDDocumentCatalog.class.getResourceAsStream("badpagelabels.pdf")))
        {
            PDDocumentCatalog cat = doc.getDocumentCatalog();
            // getLabelsByPageIndices() should not throw an exception
            cat.getPageLabels().getLabelsByPageIndices();
        }
    }

    /**
     * Test getNumberOfPages().
     * 
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-911"
     *   >PDFBOX-911</a> - Method PDDocument.getNumberOfPages() returns wrong
     * number of pages
     * 
     * @throws IOException in case the document can not be parsed.
     */
    @Test
    void retrieveNumberOfPages() throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(TestPDDocumentCatalog.class.getResourceAsStream("test.unc.pdf")))
        {
            assertEquals(4, doc.getNumberOfPages());
        }
    }

    /**
     * Test OutputIntents functionality.
     * 
     * Test case for
     * <a https://issues.apache.org/jira/browse/PDFBOX-2687">PDFBOX-2687</a>
     * ClassCastException when trying to get OutputIntents or add to it.
     * 
     * @throws IOException in case the document can not be parsed.
     */
    @Test
    void handleOutputIntents() throws IOException
    {
        try (InputStream colorProfile = TestPDDocumentCatalog.class.getResourceAsStream("sRGB.icc");
             PDDocument doc = Loader.loadPDF(TestPDDocumentCatalog.class.getResourceAsStream("test.unc.pdf")))
        {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            // retrieve OutputIntents
            List<PDOutputIntent> outputIntents = catalog.getOutputIntents();
            assertTrue(outputIntents.isEmpty());

            // create and add output intent
            PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
            oi.setInfo("sRGB IEC61966-2.1"); 
            oi.setOutputCondition("sRGB IEC61966-2.1"); 
            oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
            oi.setRegistryName("http://www.color.org"); 
            doc.getDocumentCatalog().addOutputIntent(oi);

            // retrieve OutputIntents
            outputIntents = catalog.getOutputIntents();
            assertEquals(1,outputIntents.size());

            // set OutputIntents
            catalog.setOutputIntents(outputIntents);
            outputIntents = catalog.getOutputIntents();
            assertEquals(1,outputIntents.size());
        }
    }

    @Test
    void handleBooleanInOpenAction() throws IOException
    {
        //PDFBOX-3772 -- allow for COSBoolean
        try (PDDocument doc = new PDDocument())
        {
            doc.getDocumentCatalog().getCOSObject().setBoolean(COSName.OPEN_ACTION, false);
            assertNull(doc.getDocumentCatalog().getOpenAction());
        }
    }
}
