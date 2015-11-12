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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDButton class.
 *
 */
public class PDAcroFormTest
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

    @Test
    public void testFieldsEntry()
    {
        // the /Fields entry has been created with the AcroForm
        // as this is a required entry
        assertNotNull(acroForm.getFields());
        assertEquals(acroForm.getFields().size(),0);
        
        // there shouldn't be an exception if there is no such field
        assertNull(acroForm.getField("foo"));
        
        // remove the required entry which is the case for some
        // PDFs (see PDFBOX-2965)
        acroForm.getCOSObject().removeItem(COSName.FIELDS);
        
        // ensure there is always an empty collection returned
        assertNotNull(acroForm.getFields());
        assertEquals(acroForm.getFields().size(),0);

        // there shouldn't be an exception if there is no such field
        assertNull(acroForm.getField("foo"));
    }
    
    @Test
    public void testAcroFormProperties()
    {
        assertTrue(acroForm.getDefaultAppearance().isEmpty());
        acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");
        assertEquals(acroForm.getDefaultAppearance(),"/Helv 0 Tf 0 g");
    }
    
    @Test
    public void testFlatten() throws IOException
    {
        PDDocument testPdf = PDDocument.load(new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form/AlignmentTests.pdf"));
        testPdf.getDocumentCatalog().getAcroForm().flatten();
        testPdf.save("target/test-output/AlignmentTests-flattened.pdf"); 
    }

    @After
    public void tearDown() throws IOException
    {
        document.close();
    }

}

