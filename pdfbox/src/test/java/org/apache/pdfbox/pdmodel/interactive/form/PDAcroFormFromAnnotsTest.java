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
package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import java.io.IOException;
import java.net.URL;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDButton class.
 *
 */
public class PDAcroFormFromAnnotsTest
{
    
    private PDDocument document;
    private PDAcroForm acroForm;
        
    @Before
    public void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);
    }

    /**
     * PDFBOX-4985 AcroForms entry but empty Fields array
     *
     * @throws IOException
     */

    @Test
    public void testFromAnnots4985() throws IOException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13013354/POPPLER-806.pdf";
                
        try (PDDocument testPdf = Loader.loadPDF(new URL(sourceUrl).openStream()))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            // need to do a low level cos access as the PDModel access will build the AcroForm 
            COSDictionary cosAcroForm = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.ACRO_FORM);
            COSArray cosFields = (COSArray) cosAcroForm.getDictionaryObject(COSName.FIELDS);
            assertEquals("Initially there shall be 0 fields", cosFields.size(), 0);
            PDAcroForm acroForm = catalog.getAcroForm();
            assertTrue("After rebuild there shall be > 0 fields", acroForm.getFields().size() > 0);
        }
    }    


    @After
    public void tearDown() throws IOException
    {
        document.close();
    }
}
