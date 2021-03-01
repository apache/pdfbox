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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

/**
 * This class tests the extraction of document-level metadata.
 * @author Neil McErlean
 * @since 1.3.0
 */
class TestPDDocumentInformation
{

    @Test
    void testMetadataExtraction() throws Exception
    {
        // This document has been selected for this test as it contains custom metadata.
        try (PDDocument doc = Loader.loadPDF(new File("src/test/resources/input/hello3.pdf")))
        {
           PDDocumentInformation info = doc.getDocumentInformation();
           
           assertEquals("Brian Carrier", info.getAuthor(), "Wrong author");
           assertNotNull(info.getCreationDate(), "Wrong creationDate");
           assertEquals("Acrobat PDFMaker 8.1 for Word", info.getCreator(), "Wrong creator");
           assertNull(info.getKeywords(), "Wrong keywords");
           assertNotNull(info.getModificationDate(), "Wrong modificationDate");
           assertEquals("Acrobat Distiller 8.1.0 (Windows)", info.getProducer(), "Wrong producer");
           assertNull(info.getSubject(), "Wrong subject");
           assertNull(info.getTrapped(), "Wrong trapped");

           List<String> expectedMetadataKeys = Arrays.asList("CreationDate", "Author", "Creator",
                                                             "Producer", "ModDate", "Company",
                                                             "SourceModified", "Title");
           assertEquals(expectedMetadataKeys.size(), info.getMetadataKeys().size(),
                   "Wrong metadata key count");
           expectedMetadataKeys.forEach(key ->
                   assertTrue(info.getMetadataKeys().contains(key), "Missing metadata key:" + key));
           
           // Custom metadata fields.
           assertEquals("Basis Technology Corp.", info.getCustomMetadataValue("Company"),
                   "Wrong company");
           assertEquals("D:20080819181502", info.getCustomMetadataValue("SourceModified"),
                   "Wrong sourceModified");
        }
    }
    
    /**
     * PDFBOX-3068: test that indirect /Title element of /Info entry can be found.
     * 
     * @throws Exception 
     */
    @Test
    void testPDFBox3068() throws Exception
    {
        try (PDDocument doc = Loader
                .loadPDF(TestPDDocumentInformation.class.getResourceAsStream("PDFBOX-3068.pdf")))
        {
            PDDocumentInformation documentInformation = doc.getDocumentInformation();
            assertEquals("Title", documentInformation.getTitle());
        }
    }
    
}
