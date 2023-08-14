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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.fixup.AbstractFixup;
import org.apache.pdfbox.pdmodel.fixup.AcroFormDefaultFixup;
import org.apache.pdfbox.pdmodel.fixup.processor.AcroFormOrphanWidgetsProcessor;
import org.apache.pdfbox.pdmodel.font.PDFont;

import org.junit.jupiter.api.Test;

/**
 * Tests for building AcroForm entries form Widget annotations.
 *
 */
class PDAcroFormFromAnnotsTest
{
    /**
     * PDFBOX-4985 AcroForms entry but empty Fields array 
     * 
     * Using the default get AcroForm call with error correction
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots4985DefaultMode() throws IOException, URISyntaxException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13013354/POPPLER-806.pdf";
        String acrobatSourceUrl = "https://issues.apache.org/jira/secure/attachment/13013384/POPPLER-806-acrobat.pdf";

        int numFormFieldsByAcrobat;

        try (PDDocument testPdf = Loader.loadPDF(RandomAccessReadBuffer
                .createBufferFromStream(new URI(acrobatSourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm(null);
            numFormFieldsByAcrobat = acroForm.getFields().size();
        }
                
        try (PDDocument testPdf = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            // need to do a low level cos access as the PDModel access will build the AcroForm 
            COSDictionary cosAcroForm = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.ACRO_FORM);
            COSArray cosFields = (COSArray) cosAcroForm.getDictionaryObject(COSName.FIELDS);
            assertEquals(0, cosFields.size(), "Initially there shall be 0 fields");
            PDAcroForm acroForm = catalog.getAcroForm();
            assertEquals(numFormFieldsByAcrobat, acroForm.getFields().size(), "After rebuild there shall be " + numFormFieldsByAcrobat + " fields");
        }
    }

    /**
     * PDFBOX-4985 AcroForms entry but empty Fields array 
     * 
     * Using the acroform call with error correction
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots4985CorrectionMode() throws IOException, URISyntaxException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13013354/POPPLER-806.pdf";
        String acrobatSourceUrl = "https://issues.apache.org/jira/secure/attachment/13013384/POPPLER-806-acrobat.pdf";

        int numFormFieldsByAcrobat;

        try (PDDocument testPdf = Loader.loadPDF(RandomAccessReadBuffer
                .createBufferFromStream(new URI(acrobatSourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm(null);
            numFormFieldsByAcrobat = acroForm.getFields().size();
        }
                
        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            // need to do a low level cos access as the PDModel access will build the AcroForm 
            COSDictionary cosAcroForm = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.ACRO_FORM);
            COSArray cosFields = (COSArray) cosAcroForm.getDictionaryObject(COSName.FIELDS);
            assertEquals(0, cosFields.size(), "Initially there shall be 0 fields");
            PDAcroForm acroForm = catalog.getAcroForm(new AcroFormDefaultFixup(testPdf));
            assertEquals(numFormFieldsByAcrobat, acroForm.getFields().size(), "After rebuild there shall be " + numFormFieldsByAcrobat + " fields");
        }
    } 

    /**
     * PDFBOX-4985 AcroForms entry but empty Fields array 
     * 
     * Using the acroform call without error correction
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots4985WithoutCorrectionMode() throws IOException, URISyntaxException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13013354/POPPLER-806.pdf";

        int numCosFormFields;
                
        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            // need to do a low level cos access as the PDModel access will build the AcroForm 
            COSDictionary cosAcroForm = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.ACRO_FORM);
            COSArray cosFields = (COSArray) cosAcroForm.getDictionaryObject(COSName.FIELDS);
            numCosFormFields = cosFields.size();
            assertEquals(0, cosFields.size(), "Initially there shall be 0 fields");
            PDAcroForm acroForm = catalog.getAcroForm(null);
            assertEquals(numCosFormFields, acroForm.getFields().size(), "After call without correction there shall be " + numCosFormFields + " fields");
        }
    }

    /**
     * PDFBOX-3891 AcroForm with empty fields entry
     * 
     * With the default correction nothing shall be added
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots3891DontCreateFields() throws IOException, URISyntaxException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12881055/merge-test.pdf";

        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            // need to do a low level cos access as the PDModel access will build the AcroForm
            COSDictionary cosAcroForm = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.ACRO_FORM);
            COSArray cosFields = (COSArray) cosAcroForm.getDictionaryObject(COSName.FIELDS);
            assertEquals(0, cosFields.size(), "Initially there shall be 0 fields");
            PDAcroForm acroForm = catalog.getAcroForm();
            assertEquals(0, acroForm.getFields().size(), "After call with default correction there shall be 0 fields");
        }
    }

    /**
     * PDFBOX-3891 AcroForm with empty fields entry
     * 
     * Special fixup to create fields
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots3891CreateFields() throws IOException, URISyntaxException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12881055/merge-test.pdf";
        String acrobatSourceUrl = "https://issues.apache.org/jira/secure/attachment/13014447/merge-test-na-acrobat.pdf";

        int numFormFieldsByAcrobat;

        // will build the expected fields using the acrobat source document
        Map<String, PDField> fieldsByName = new HashMap<>();

        try (PDDocument testPdf = Loader.loadPDF(RandomAccessReadBuffer
                .createBufferFromStream(new URI(acrobatSourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm(null);
            numFormFieldsByAcrobat = acroForm.getFields().size();
            for (PDField field : acroForm.getFieldTree())
            {
                fieldsByName.put(field.getFullyQualifiedName(), field);
            }
        }

        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            // need to do a low level cos access as the PDModel access will build the AcroForm
            COSDictionary cosAcroForm = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.ACRO_FORM);
            COSArray cosFields = (COSArray) cosAcroForm.getDictionaryObject(COSName.FIELDS);
            assertEquals(0, cosFields.size(), "Initially there shall be 0 fields");
            PDAcroForm acroForm = catalog.getAcroForm(new CreateFieldsFixup(testPdf));
            assertEquals(numFormFieldsByAcrobat, acroForm.getFields().size(), "After rebuild there shall be " + numFormFieldsByAcrobat + " fields");

            // the the fields found are contained in the map
            for (PDField field : acroForm.getFieldTree())
            {
                assertNotNull(fieldsByName.get(field.getFullyQualifiedName()));
            }

            // test all fields in the map are also found in the AcroForm
            fieldsByName.keySet().forEach(fieldName -> assertNotNull(acroForm.getField(fieldName)));
        }
    }

    /**
     * PDFBOX-3891 AcroForm with empty fields entry
     * 
     * Check if the font resources added by PDFBox matches these by Acrobat
     * which are taken from the widget normal appearance resources
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots3891ValidateFont() throws IOException, URISyntaxException
    {

        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12881055/merge-test.pdf";
        String acrobatSourceUrl = "https://issues.apache.org/jira/secure/attachment/13014447/merge-test-na-acrobat.pdf";

        // will build the expected font respurce names and font decriptor names using the acrobat source document
        Map<String, String> fontNames = new HashMap<>();

        try (PDDocument testPdf = Loader.loadPDF(RandomAccessReadBuffer
                .createBufferFromStream(new URI(acrobatSourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm(null);
            PDResources acroFormResources = acroForm.getDefaultResources();
            if (acroFormResources != null)
            {
                acroFormResources.getFontNames().forEach(fontName -> {
                    try
                    {
                        PDFont font = acroFormResources.getFont(fontName);
                        font.getFontDescriptor().getFontName();
                        fontNames.put(fontName.getName(), font.getName());
                    }
                    catch (IOException ioe)
                    {
                        //ignoring
                    }
                });
            }
        }

        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm(new CreateFieldsFixup(testPdf));
            PDResources acroFormResources = acroForm.getDefaultResources();
            if (acroFormResources != null)
            {
                acroFormResources.getFontNames().forEach(fontName -> {
                    try
                    {
                        PDFont font = acroFormResources.getFont(fontName);
                        String pdfBoxFontName = font.getFontDescriptor().getFontName();
                        assertEquals(fontNames.get(fontName.getName()), pdfBoxFontName, "font resource added by Acrobat shall match font resource added by PDFBox");
                    }
                    catch (IOException ioe)
                    {
                        //ignoring
                    }
                });
            }
        }
    }

    /**
     * PDFBOX-3891 null PDFieldFactory.createField 
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testFromAnnots3891NullField() throws IOException, URISyntaxException
    {
        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13016993/poppler-14433-0.pdf";

        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();
            assertDoesNotThrow(() -> catalog.getAcroForm(new CreateFieldsFixup(testPdf)), "Getting the AcroForm shall not throw an exception");
        }
    }



    /*
     * Create fields from widget annotations
     */
    class CreateFieldsFixup extends AbstractFixup
    {
        CreateFieldsFixup(PDDocument document)
        { 
            super(document); 
        }

        @Override
        public void apply() {
            new AcroFormOrphanWidgetsProcessor(document).process();

        }        
    }
}
