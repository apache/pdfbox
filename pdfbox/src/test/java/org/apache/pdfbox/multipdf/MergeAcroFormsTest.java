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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility.AcroFormMergeMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test merging different PDFs with AcroForms.
 * 
 * 
 */
@Execution(ExecutionMode.CONCURRENT)
class MergeAcroFormsTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/multipdf");
    private static final File OUT_DIR = new File("target/test-output/merge/");
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    @BeforeEach
    public void setUp()
    {
        OUT_DIR.mkdirs();
    }
    
    /*
     * Test LegacyMode merge
     */
    @Test
    void testLegacyModeMerge() throws IOException
    {
        PDFMergerUtility merger = new PDFMergerUtility();
        File toBeMerged = new File(IN_DIR,"AcroFormForMerge.pdf");
        File pdfOutput = new File(OUT_DIR,"PDFBoxLegacyMerge-SameMerged.pdf");
        merger.setDestinationFileName(pdfOutput.getAbsolutePath());
        merger.addSource(toBeMerged);
        merger.addSource(toBeMerged);
        merger.mergeDocuments(null);
        merger.setAcroFormMergeMode(AcroFormMergeMode.PDFBOX_LEGACY_MODE);
        
        try (PDDocument compliantDocument = Loader
                .loadPDF(new File(IN_DIR, "PDFBoxLegacyMerge-SameMerged.pdf"));
                PDDocument toBeCompared = Loader
                        .loadPDF(new File(OUT_DIR, "PDFBoxLegacyMerge-SameMerged.pdf")))
        {
            PDAcroForm compliantAcroForm = compliantDocument.getDocumentCatalog().getAcroForm();
            PDAcroForm toBeComparedAcroForm = toBeCompared.getDocumentCatalog().getAcroForm();
            
            assertEquals(compliantAcroForm.getFields().size(),
                    toBeComparedAcroForm.getFields().size(),
                    "There shall be the same number of root fields");
            
            for (PDField compliantField : compliantAcroForm.getFieldTree())
            {
                assertNotNull(toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName()),
                        "There shall be a field with the same FQN");
                PDField toBeComparedField = toBeComparedAcroForm.getField(compliantField.getFullyQualifiedName());
                compareFieldProperties(compliantField, toBeComparedField);
            }

            for (PDField toBeComparedField : toBeComparedAcroForm.getFieldTree())
            {
                assertNotNull(compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName()),
                        "There shall be a field with the same FQN");
                PDField compliantField = compliantAcroForm.getField(toBeComparedField.getFullyQualifiedName());
                compareFieldProperties(toBeComparedField, compliantField);
            }       
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
                assertEquals(sourceBase.toString(), toBeComparedBase.toString(),
                        "The content of the field properties shall be the same");
            }
            else
            {
                assertNull(toBeComparedBase,
                        "If the source property is null the compared property shall be null too");
            }
        }
    }
    
    
    /*
     * PDFBOX-1031 Ensure that after merging the PDFs there is an Annots entry per page.
     */
    @Test
    void testAnnotsEntry() throws IOException
    {

        // Merge the PDFs form PDFBOX-1031
        PDFMergerUtility merger = new PDFMergerUtility();

        File f1 = new File(TARGET_PDF_DIR, "PDFBOX-1031-1.pdf");
        File f2 = new File(TARGET_PDF_DIR, "PDFBOX-1031-2.pdf");
        File pdfOutput = new File(OUT_DIR,"PDFBOX-1031.pdf");

        try (InputStream is1 = new FileInputStream(f1);
             InputStream is2 = new FileInputStream(f2))
        {
            
            merger.setDestinationFileName(pdfOutput.getAbsolutePath());
            merger.addSource(is1);
            merger.addSource(is2);
            merger.mergeDocuments(null);
        }
        
        // Test merge result
        try (PDDocument mergedPDF = Loader.loadPDF(pdfOutput))
        {
            assertEquals(2, mergedPDF.getNumberOfPages(), "There shall be 2 pages");
            assertNotNull(mergedPDF.getPage(0).getCOSObject().getDictionaryObject(COSName.ANNOTS),
                    "There shall be an /Annots entry for the first page");
            assertEquals(1, mergedPDF.getPage(0).getAnnotations().size(),
                    "There shall be 1 annotation for the first page");
            
            assertNotNull(mergedPDF.getPage(1).getCOSObject().getDictionaryObject(COSName.ANNOTS),
                    "There shall be an /Annots entry for the second page");
            assertEquals(1, mergedPDF.getPage(0).getAnnotations().size(),
                    "There shall be 1 annotation for the second page");
        }
    }    
    
    /*
     * PDFBOX-1100 Ensure that after merging the PDFs there is an AP and V entry.
     */
    @Test
    void testAPEntry() throws IOException
    {

        File file1 = new File(TARGET_PDF_DIR, "PDFBOX-1100-1.pdf");
        File file2 = new File(TARGET_PDF_DIR, "PDFBOX-1100-2.pdf");
        // Merge the PDFs form PDFBOX-1100
        PDFMergerUtility merger = new PDFMergerUtility();
        
        File pdfOutput = new File(OUT_DIR,"PDFBOX-1100.pdf");

        try (InputStream is1 = new FileInputStream(file1);
                InputStream is2 = new FileInputStream(file2))
        {
            merger.setDestinationFileName(pdfOutput.getAbsolutePath());
            merger.addSource(is1);
            merger.addSource(is2);
            merger.mergeDocuments(null);
        }
        
        // Test merge result
        try (PDDocument mergedPDF = Loader.loadPDF(pdfOutput))
        {
            assertEquals(2, mergedPDF.getNumberOfPages(), "There shall be 2 pages");
            
            PDAcroForm acroForm = mergedPDF.getDocumentCatalog().getAcroForm();
            
            PDField formField = acroForm.getField("Testfeld");
            assertNotNull(formField.getCOSObject().getDictionaryObject(COSName.AP),
                    "There shall be an /AP entry for the field");
            assertNotNull(formField.getCOSObject().getDictionaryObject(COSName.V),
                    "There shall be a /V entry for the field");
    
            formField = acroForm.getField("Testfeld2");
            assertNotNull(formField.getCOSObject().getDictionaryObject(COSName.AP),
                    "There shall be an /AP entry for the field");
            assertNotNull(formField.getCOSObject().getDictionaryObject(COSName.V),
                    "There shall be a /V entry for the field");
        }
    }
    
}
