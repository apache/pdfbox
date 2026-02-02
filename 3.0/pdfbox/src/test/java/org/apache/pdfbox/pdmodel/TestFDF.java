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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * This will test the FDF algorithms in PDFBox.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 * 
 */
class TestFDF
{
    /**
     * Test load two simple fdf files with two fields. One of the files does not have a
     * /Type/Catalog entry, which isn't required anyway (PDFBOX-3639).
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    void testLoad2() throws URISyntaxException, IOException
    {
        checkFields("/org/apache/pdfbox/pdfparser/withcatalog.fdf");
        checkFields("/org/apache/pdfbox/pdfparser/nocatalog.fdf");
    }

    /**
     * PDFBOX-5894: check that premature file close bug is fixed.
     *
     * @throws IOException 
     */
    @Test
    void testPDFBox5894() throws IOException
    {
        try (FDFDocument fdf = Loader.loadFDF(new File("target/pdfs/PDFBOX-5894.fdf")))
        {
            List<COSObject> objectsByType = fdf.getDocument().getObjectsByType(COSName.ANNOT);
            assertEquals(4, objectsByType.size());
            for (COSObject obj : objectsByType)
            {
                COSBase base = obj.getObject();
                assertEquals(COSName.ANNOT, ((COSDictionary) base).getDictionaryObject(COSName.TYPE));
            }
        }
    }

    private void checkFields(String name) throws IOException, URISyntaxException
    {
        try (FDFDocument fdf = Loader.loadFDF(new File(TestFDF.class.getResource(name).toURI())))
        {
            fdf.saveXFDF(new PrintWriter(new ByteArrayOutputStream()));
            
            List<FDFField> fields = fdf.getCatalog().getFDF().getFields();
            
            assertEquals(2, fields.size());
            assertEquals("Field1", fields.get(0).getPartialFieldName());
            assertEquals("Field2", fields.get(1).getPartialFieldName());
            assertEquals("Test1", fields.get(0).getValue());
            assertEquals("Test2", fields.get(1).getValue());
            
            try (PDDocument pdf = Loader.loadPDF(new File(TestFDF.class
                    .getResource("/org/apache/pdfbox/pdfparser/SimpleForm2Fields.pdf").toURI())))
            {
                PDAcroForm acroForm = pdf.getDocumentCatalog().getAcroForm();
                acroForm.importFDF(fdf);
                assertEquals("Test1", acroForm.getField("Field1").getValueAsString());
                assertEquals("Test2", acroForm.getField("Field2").getValueAsString());
            }
        }
    }
}
