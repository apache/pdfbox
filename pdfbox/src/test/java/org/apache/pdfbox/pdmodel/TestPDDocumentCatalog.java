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

import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;

import junit.framework.TestCase;

public class TestPDDocumentCatalog extends TestCase {

    /**
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-90"
     *   >PDFBOX-90</a> - Support explicit retrieval of page labels.
     */
    public void testPageLabels() throws Exception {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(TestPDDocumentCatalog.class.getResourceAsStream("test_pagelabels.pdf"));
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
        } finally {
            if(doc != null)
                doc.close();
        }
    }

    /**
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-911"
     *   >PDFBOX-911</a> - Method PDDocument.getNumberOfPages() returns wrong
     * number of pages
     */
    public void testGetNumberOfPages() throws Exception {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(TestPDDocumentCatalog.class.getResource("test.unc.pdf"));
            assertEquals(4, doc.getNumberOfPages());
        } finally {
            if(doc != null)
                doc.close();
        }
    }

    /**
     * Test case for
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-1088"
     *   >PDFBOX-1088</a> - Class PDDocument Method getPageMap returns a ClassCastException
     */
    public void testPageMap() throws Exception {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(TestPDDocumentCatalog.class.getResource("test.unc.pdf"));
            assertEquals(4, doc.getPageMap().size());
            int pageNum = doc.getPageMap().get("14,0");
            assertEquals(1, pageNum);
        } finally {
            if(doc != null)
                doc.close();
        }
    }
    
    /**
     * Test case for
     * <a https://issues.apache.org/jira/browse/PDFBOX-2687">PDFBOX-2687</a>
     * ClassCastException when trying to get OutputIntents or add to it
     */
    public void testOutputIntents() throws Exception {
        PDDocument doc = null;
        InputStream colorProfile = null;
        try {
            
            doc = PDDocument.load(TestPDDocumentCatalog.class.getResource("test.unc.pdf"));
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            // retrieve OutputIntents
            List<PDOutputIntent> outputIntents = catalog.getOutputIntent();
            assertTrue(outputIntents.isEmpty());
            
            // add an OutputIntent
            colorProfile = TestPDDocumentCatalog.class.getResourceAsStream("sRGB.icc");
            // create output intent
            PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
            oi.setInfo("sRGB IEC61966-2.1"); 
            oi.setOutputCondition("sRGB IEC61966-2.1"); 
            oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
            oi.setRegistryName("http://www.color.org"); 
            doc.getDocumentCatalog().addOutputIntent(oi);
            
            // retrieve OutputIntents
            outputIntents = catalog.getOutputIntent();
            assertEquals(1,outputIntents.size());
            
            // set OutputIntents
            catalog.setOutputIntents(outputIntents);
            outputIntents = catalog.getOutputIntent();
            assertEquals(1,outputIntents.size());            
            
        } finally {
            if(doc != null)
            {
                doc.close();
            }
            
            if (colorProfile != null)
            {
                colorProfile.close();
            }
        }
    }
}
