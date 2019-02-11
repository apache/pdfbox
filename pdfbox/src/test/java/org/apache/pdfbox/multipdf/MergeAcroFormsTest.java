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
package org.apache.pdfbox.multipdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility.AcroFormMergeMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.Before;
import org.junit.Test;

/**
 * Test merging different PDFs with AcroForms.
 *
 *
 */
public class MergeAcroFormsTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/multipdf");
    private static final File OUT_DIR = new File("target/test-output/merge/");
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    @Before
    public void setUp()
    {
        OUT_DIR.mkdirs();
    }

    /*
     * Test LegacyMode merge
     */
    @Test
    public void testLegacyModeMerge() throws IOException
    {
        PDFMergerUtility merger = new PDFMergerUtility();
        File toBeMerged = new File(IN_DIR,"AcroFormForMerge.pdf");
        File pdfOutput = new File(OUT_DIR,"PDFBoxLegacyMerge-SameMerged.pdf");
        merger.setDestinationFileName(pdfOutput.getAbsolutePath());
        merger.addSource(toBeMerged);
        merger.addSource(toBeMerged);
        merger.mergeDocuments(null);
        merger.setAcroFormMergeMode(AcroFormMergeMode.PDFBOX_LEGACY_MODE);

        PDDocument compliantDocument = null;
        PDDocument toBeCompared = null;


        try
        {
            compliantDocument = PDDocument.load(new File(IN_DIR,"PDFBoxLegacyMerge-SameMerged.pdf"));
            toBeCompared = PDDocument.load(new File(OUT_DIR,"PDFBoxLegacyMerge-SameMerged.pdf"));


            PDAcroForm compliantAcroForm = compliantDocument.getDocumentCatalog().getAcroForm();
            PDAcroForm toBeComparedAcroForm = toBeCompared.getDocumentCatalog().getAcroForm();

            assertEquals("There shall be the same number of root fields",
                    compliantAcroForm.getFields().size(),
                    toBeComparedAcroForm.getFields().size());

            for (PDField compliantField : compliantAcroForm.getFieldTree())
            {
                assertNotNull("There shall be a field with the same FQN", toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName()));
                PDField toBeComparedField = toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName());
                compareFieldProperties(compliantField, toBeComparedField);
            }

            for (PDField toBeComparedField : toBeComparedAcroForm.getFieldTree())
            {
                assertNotNull("There shall be a field with the same FQN", compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName()));
                PDField compliantField = compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName());
                compareFieldProperties(toBeComparedField, compliantField);
            }
        }
        finally
        {
            IOUtils.closeQuietly(compliantDocument);
            IOUtils.closeQuietly(toBeCompared);
        }
    }

    /*
     * Test Join Field Merge for text fields only and same source documents
     */
    @Test
    public void testJoinFieldsMerge_TextFieldsOnly_SameMerged() throws IOException
    {
        PDFMergerUtility merger = new PDFMergerUtility();
        File toBeMerged = new File(IN_DIR,"AcroFormForMerge-TextFieldsOnly.pdf");
        File pdfOutput = new File(OUT_DIR,"PDFBoxJoinFieldsMerge-TextFieldsOnly-SameMerged.pdf");
        merger.setDestinationFileName(pdfOutput.getAbsolutePath());
        merger.addSource(toBeMerged);
        merger.addSource(toBeMerged);
        merger.setAcroFormMergeMode(AcroFormMergeMode.JOIN_FORM_FIELDS_MODE);
        merger.mergeDocuments(null);

        PDDocument compliantDocument = null;
        PDDocument toBeCompared = null;


        try
        {
            compliantDocument = PDDocument.load(new File(IN_DIR,"AcrobatMerge-TextFieldsOnly-SameMerged.pdf"));
            toBeCompared = PDDocument.load(new File(OUT_DIR,"PDFBoxJoinFieldsMerge-TextFieldsOnly-SameMerged.pdf"));


            PDAcroForm compliantAcroForm = compliantDocument.getDocumentCatalog().getAcroForm();
            PDAcroForm toBeComparedAcroForm = toBeCompared.getDocumentCatalog().getAcroForm();

            assertEquals("There shall be the same number of root fields",
                    compliantAcroForm.getFields().size(),
                    toBeComparedAcroForm.getFields().size());

            for (PDField compliantField : compliantAcroForm.getFieldTree())
            {
                assertNotNull("There shall be a field with the same FQN", toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName()));
                PDField toBeComparedField = toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName());
                compareFieldProperties(compliantField, toBeComparedField);
            }

            for (PDField toBeComparedField : toBeComparedAcroForm.getFieldTree())
            {
                assertNotNull("There shall be a field with the same FQN", compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName()));
                PDField compliantField = compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName());
                compareFieldProperties(toBeComparedField, compliantField);
            }
        }
        finally
        {
            IOUtils.closeQuietly(compliantDocument);
            IOUtils.closeQuietly(toBeCompared);
        }
    }


    private void compareFieldProperties(PDField sourceField, PDField toBeComapredField)
    {
        // List of keys for comparison
        // Don't include too complex properties such as AP as this will fail the test because
        // of a stack overflow when
        final String[] keys = {"FT", "T", "TU", "TM", "Ff", "V", "DV", "Opts", "TI", "I", "Rect", "DA", };

        COSDictionary sourceFieldCos = sourceField.getCOSObject();
        COSDictionary toBeComparedCos = toBeComapredField.getCOSObject();

        for (String key : keys)
        {
            COSBase sourceBase = sourceFieldCos.getDictionaryObject(key);
            COSBase toBeComparedBase = toBeComparedCos.getDictionaryObject(key);

            if (sourceBase != null)
            {
                assertEquals("The content of the field properties shall be the same",sourceBase.toString(), toBeComparedBase.toString());
            }
            else
            {
                assertNull("If the source property is null the compared property shall be null too", toBeComparedBase);
            }
        }
    }    

    /*
     * PDFBOX-1031 Ensure that after merging the PDFs there is an Annots entry per page.
     */
    @Test
    public void testAnnotsEntry() throws IOException {

        InputStream s1 = null;
        InputStream s2 = null;
        // Merge the PDFs form PDFBOX-1031
        PDFMergerUtility merger = new PDFMergerUtility();
        try {
            File f1 = new File(TARGET_PDF_DIR, "PDFBOX-1031-1.pdf");
            s1 = new FileInputStream(f1);

            File f2 = new File(TARGET_PDF_DIR, "PDFBOX-1031-2.pdf");
            s2 = new FileInputStream(f2);

            File pdfOutput = new File(OUT_DIR, "PDFBOX-1031.pdf");
            merger.setDestinationFileName(pdfOutput.getAbsolutePath());
            merger.addSource(s1);
            merger.addSource(s2);
            merger.mergeDocuments(null);

            // Test merge result
            PDDocument mergedPDF = PDDocument.load(pdfOutput);
            assertEquals("There shall be 2 pages", 2, mergedPDF.getNumberOfPages());

            assertNotNull("There shall be an /Annots entry for the first page", mergedPDF.getPage(0).getCOSObject().getDictionaryObject(COSName.ANNOTS));
            assertEquals("There shall be 1 annotation for the first page", 1, mergedPDF.getPage(0).getAnnotations().size());

            assertNotNull("There shall be an /Annots entry for the second page", mergedPDF.getPage(1).getCOSObject().getDictionaryObject(COSName.ANNOTS));
            assertEquals("There shall be 1 annotation for the second page", 1, mergedPDF.getPage(0).getAnnotations().size());

            mergedPDF.close();
        } finally {
            IOUtils.closeQuietly(s1);
            IOUtils.closeQuietly(s2);
        }
    }

    /*
     * PDFBOX-1100 Ensure that after merging the PDFs there is an AP and V entry.
     */
    @Test
    public void testAPEntry() throws IOException {

        InputStream is1 = null;
        InputStream is2 = null;
        // Merge the PDFs form PDFBOX-1100
        PDFMergerUtility merger = new PDFMergerUtility();

        try {
            File file1 = new File(TARGET_PDF_DIR, "PDFBOX-1100-1.pdf");
            is1 = new FileInputStream(file1);

            File file2 = new File(TARGET_PDF_DIR, "PDFBOX-1100-2.pdf");
            is2 = new FileInputStream(file2);
            File pdfOutput = new File(OUT_DIR, "PDFBOX-1100.pdf");
            merger.setDestinationFileName(pdfOutput.getAbsolutePath());
            merger.addSource(is1);
            merger.addSource(is2);
            merger.mergeDocuments(null);

            // Test merge result
            PDDocument mergedPDF = PDDocument.load(pdfOutput);
            assertEquals("There shall be 2 pages", 2, mergedPDF.getNumberOfPages());

            PDAcroForm acroForm = mergedPDF.getDocumentCatalog().getAcroForm();

            PDField formField = acroForm.getField("Testfeld");
            assertNotNull("There shall be an /AP entry for the field", formField.getCOSObject().getDictionaryObject(COSName.AP));
            assertNotNull("There shall be a /V entry for the field", formField.getCOSObject().getDictionaryObject(COSName.V));

            formField = acroForm.getField("Testfeld2");
            assertNotNull("There shall be an /AP entry for the field", formField.getCOSObject().getDictionaryObject(COSName.AP));
            assertNotNull("There shall be a /V entry for the field", formField.getCOSObject().getDictionaryObject(COSName.V));

            mergedPDF.close();
        } finally {
            IOUtils.closeQuietly(is1);
            IOUtils.closeQuietly(is2);
        }
    }
}
