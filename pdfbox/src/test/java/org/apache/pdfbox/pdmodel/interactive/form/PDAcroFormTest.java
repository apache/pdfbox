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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Test for the PDButton class.
 *
 */
class PDAcroFormTest
{
    
    private PDDocument document;
    private PDAcroForm acroForm;
    
    private static final File OUT_DIR = new File("target/test-output");
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    
    @BeforeEach
    public void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);
    }

    @Test
    void testFieldsEntry()
    {
        // the /Fields entry has been created with the AcroForm
        // as this is a required entry
        assertNotNull(acroForm.getFields());
        assertEquals(0, acroForm.getFields().size());
        
        // there shouldn't be an exception if there is no such field
        assertNull(acroForm.getField("foo"));
        
        // remove the required entry which is the case for some
        // PDFs (see PDFBOX-2965)
        acroForm.getCOSObject().removeItem(COSName.FIELDS);
        
        // ensure there is always an empty collection returned
        assertNotNull(acroForm.getFields());
        assertEquals(0, acroForm.getFields().size());

        // there shouldn't be an exception if there is no such field
        assertNull(acroForm.getField("foo"));
    }
    
    @Test
    void testAcroFormProperties()
    {
        assertTrue(acroForm.getDefaultAppearance().isEmpty());
        acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");
        assertEquals("/Helv 0 Tf 0 g", acroForm.getDefaultAppearance());
    }
    
    @Test
    void testFlatten() throws IOException
    {
        File file = new File(OUT_DIR, "AlignmentTests-flattened.pdf");
        try (PDDocument testPdf = Loader.loadPDF(new File(IN_DIR, "AlignmentTests.pdf")))
        {
            testPdf.getDocumentCatalog().getAcroForm().flatten();
            assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
            testPdf.save(file);
        }
        // compare rendering
        if (!TestPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
        {
            // don't fail, rendering is different on different systems, result must be viewed manually
            System.out.println("Rendering of " + file + " failed or is not identical to expected rendering in " + IN_DIR + " directory");
        }
        
    }

    /*
     * Same as above but remove the page reference from the widget annotation
     * before doing the flatten() to ensure that the widgets page reference is properly looked up
     * (PDFBOX-3301)
     */
    @Test
    void testFlattenWidgetNoRef() throws IOException
    {
        File file = new File(OUT_DIR, "AlignmentTests-flattened-noRef.pdf");

        try (PDDocument testPdf = Loader.loadPDF(new File(IN_DIR, "AlignmentTests.pdf")))
        {
            PDAcroForm acroFormToTest = testPdf.getDocumentCatalog().getAcroForm();
            for (PDField field : acroFormToTest.getFieldTree())
            {
                field.getWidgets().forEach(widget -> widget.getCOSObject().removeItem(COSName.P));
            }
            acroFormToTest.flatten();

            // 36 non widget annotations shall not be flattened
            assertEquals(36, testPdf.getPage(0).getAnnotations().size());

            assertTrue(acroFormToTest.getFields().isEmpty());
            testPdf.save(file);
        }
        // compare rendering
        if (!TestPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
        {
            // don't fail, rendering is different on different systems, result must be viewed manually
            System.out.println("Rendering of " + file + " failed or is not identical to expected rendering in " + IN_DIR + " directory");
        }
    }
    
    @Test
    void testFlattenSpecificFieldsOnly() throws IOException
    {
        File file = new File(OUT_DIR, "AlignmentTests-flattened-specificFields.pdf");
        
        List<PDField> fieldsToFlatten = new ArrayList<>();
                
        try (PDDocument testPdf = Loader.loadPDF(new File(IN_DIR, "AlignmentTests.pdf")))
        {
            PDAcroForm acroFormToFlatten = testPdf.getDocumentCatalog().getAcroForm();
            int numFieldsBeforeFlatten = acroFormToFlatten.getFields().size();
            int numWidgetsBeforeFlatten = countWidgets(testPdf);
            
            fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Small-Filled"));
            fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Medium-Filled"));
            fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Wide-Filled"));
            fieldsToFlatten.add(acroFormToFlatten.getField("AlignLeft-Border_Wide_Clipped-Filled"));
            
            acroFormToFlatten.flatten(fieldsToFlatten, true);
            int numFieldsAfterFlatten = acroFormToFlatten.getFields().size();
            int numWidgetsAfterFlatten = countWidgets(testPdf);

            assertEquals(numFieldsBeforeFlatten, numFieldsAfterFlatten + fieldsToFlatten.size());
            assertEquals(numWidgetsBeforeFlatten, numWidgetsAfterFlatten + fieldsToFlatten.size());
            
            testPdf.save(file);
        }
    }    
    
    /*
     * Test that we do not modify an AcroForm with missing resource information
     * when loading the document only.
     * (PDFBOX-3752)
     */
    @Test
    void testDontAddMissingInformationOnDocumentLoad()
    {
        try
        {
            byte[] pdfBytes =  createAcroFormWithMissingResourceInformation();
            
            try (PDDocument pdfDocument = Loader.loadPDF(pdfBytes))
            {
                // do a low level access to the AcroForm to avoid the generation of missing entries
                PDDocumentCatalog documentCatalog = pdfDocument.getDocumentCatalog();
                COSDictionary catalogDictionary = documentCatalog.getCOSObject();
                COSDictionary acroFormDictionary = (COSDictionary) catalogDictionary.getDictionaryObject(COSName.ACRO_FORM);

                // ensure that the missing information has not been generated
                assertNull(acroFormDictionary.getDictionaryObject(COSName.DA));
                assertNull(acroFormDictionary.getDictionaryObject(COSName.RESOURCES));
            }
        }
        catch (IOException e)
        {
            System.err.println("Couldn't create test document, test skipped");
        }
    }
    
    
    /*
     * Test that we add missing ressouce information to an AcroForm 
     * when accessing the AcroForm on the PD level
     * (PDFBOX-3752)
     */
    @Test
    void testAddMissingInformationOnAcroFormAccess()
    {
        try
        {
            byte[] pdfBytes =  createAcroFormWithMissingResourceInformation();

            try (PDDocument pdfDocument = Loader.loadPDF(pdfBytes))
            {
                PDDocumentCatalog documentCatalog = pdfDocument.getDocumentCatalog();
                
                // this call shall trigger the generation of missing information
                PDAcroForm theAcroForm = documentCatalog.getAcroForm();
                
                // ensure that the missing information has been generated
                // DA entry
                assertEquals("/Helv 0 Tf 0 g ", theAcroForm.getDefaultAppearance());
                assertNotNull(theAcroForm.getDefaultResources());
                
                // DR entry
                PDResources acroFormResources = theAcroForm.getDefaultResources();
                assertNotNull(acroFormResources.getFont(COSName.getPDFName("Helv")));
                assertEquals("Helvetica", acroFormResources.getFont(COSName.getPDFName("Helv")).getName());
                assertNotNull(acroFormResources.getFont(COSName.getPDFName("ZaDb")));
                assertEquals("ZapfDingbats", acroFormResources.getFont(COSName.getPDFName("ZaDb")).getName());
            }
        }
        catch (IOException e)
        {
            System.err.println("Couldn't create test document, test skipped");
        }
    }

    /**
     * PDFBOX-4235: a bad /DA string should not result in an NPE.
     * 
     * @throws IOException 
     */
    @Test
    void testBadDA() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDAcroForm acroForm = new PDAcroForm(document);
            doc.getDocumentCatalog().setAcroForm(acroForm);
            acroForm.setDefaultResources(new PDResources());

            PDTextField textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleField");

            // https://stackoverflow.com/questions/50609478/
            // "tf" is a typo, should have been "Tf" and this results that no font is chosen
            textBox.setDefaultAppearance("/Helv 0 tf 0 g");
            acroForm.getFields().add(textBox);

            PDAnnotationWidget widget = textBox.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 750, 200, 20);
            widget.setRectangle(rect);
            widget.setPage(page);

            page.getAnnotations().add(widget);
            
            Assertions.assertThrows(IllegalArgumentException.class, () -> textBox.setValue("huhu"),
                "IllegalArgumentException should have been thrown");
        }
    }

    /**
     * PDFBOX-3732, PDFBOX-4303, PDFBOX-4393: Test whether /Helv and /ZaDb get added, but only if
     * they don't exist.
     */
    @Test
    void testAcroFormDefaultFonts() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDAcroForm acroForm2 = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm2);
            PDResources defaultResources = acroForm2.getDefaultResources();
            assertNull(defaultResources);
            defaultResources = new PDResources();
            acroForm2.setDefaultResources(defaultResources);
            assertNull(defaultResources.getFont(COSName.HELV));
            assertNull(defaultResources.getFont(COSName.ZA_DB));

            // getting AcroForm sets the two fonts
            acroForm2 = doc.getDocumentCatalog().getAcroForm();
            defaultResources = acroForm2.getDefaultResources();
            assertNotNull(defaultResources.getFont(COSName.HELV));
            assertNotNull(defaultResources.getFont(COSName.ZA_DB));

            // repeat with a new AcroForm (to delete AcroForm cache) and thus missing /DR
            doc.getDocumentCatalog().setAcroForm(new PDAcroForm(doc));
            acroForm2 = doc.getDocumentCatalog().getAcroForm();
            defaultResources = acroForm2.getDefaultResources();
            
            PDFont helv = defaultResources.getFont(COSName.HELV);
            PDFont zadb = defaultResources.getFont(COSName.ZA_DB);
            assertNotNull(helv);
            assertNotNull(zadb);
            doc.save(baos);
        }
        try (PDDocument doc = Loader.loadPDF(baos.toByteArray()))
        {
            PDAcroForm acroForm2 = doc.getDocumentCatalog().getAcroForm();
            PDResources defaultResources = acroForm2.getDefaultResources();
            PDFont helv = defaultResources.getFont(COSName.HELV);
            PDFont zadb = defaultResources.getFont(COSName.ZA_DB);
            assertNotNull(helv);
            assertNotNull(zadb);
            // make sure that font wasn't overwritten
            assertTrue(helv instanceof PDType1Font);
            assertTrue(zadb instanceof PDType1Font);
            PDType1Font helvType1 = (PDType1Font) helv;
            PDType1Font zadbType1 = (PDType1Font) zadb;
            assertEquals(FontName.HELVETICA.getName(), helv.getName());
            assertEquals(FontName.ZAPF_DINGBATS.getName(), zadb.getName());
            assertNull(helvType1.getType1Font());
            assertNull(zadbType1.getType1Font());
        }
    }

    /**
     * PDFBOX-3777 Illegal Fields definition COSDictionary instead of Array
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testIllegalFieldsDefinition() throws IOException, URISyntaxException
    {
        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12866226/D1790B.PDF";

        try (PDDocument testPdf = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDDocumentCatalog catalog = testPdf.getDocumentCatalog();

            assertDoesNotThrow(() -> catalog.getAcroForm(), "Getting the AcroForm shall not throw an exception");
        }
    }

    /**
     * Test for names with invalid UTF-8.
     * 
     * @throws IOException
     * @throws URISyntaxException 
     */
    @Test
    void testPDFBox3347() throws IOException, URISyntaxException
    {
        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/12968302/KYF%20211%20Best%C3%A4llning%202014.pdf";

        try (PDDocument doc = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(new URI(sourceUrl).toURL().openStream())))
        {
            PDField field = doc.getDocumentCatalog().getAcroForm().getField("Krematorier");
            List<PDAnnotationWidget> widgets = field.getWidgets();
            Set<String> set = new TreeSet<>();
            for (PDAnnotationWidget annot : widgets)
            {
                PDAppearanceDictionary ap = annot.getAppearance();
                PDAppearanceEntry normalAppearance = ap.getNormalAppearance();
                Set<COSName> nameSet = normalAppearance.getSubDictionary().keySet();
                assertTrue(nameSet.contains(COSName.Off));
                for (COSName name : nameSet)
                {
                    if (!name.equals(COSName.Off))
                    {
                        set.add(name.getName());
                    }
                }
            }
            assertEquals("[Nynäshamn, Råcksta, Silverdal, Skogskrem, St Botvid, Storkällan]",
                    set.toString());
        }
    }

    @AfterEach
    public void tearDown() throws IOException
    {
        document.close();
    }

    
    private byte[] createAcroFormWithMissingResourceInformation() throws IOException
    {
        try (PDDocument tmpDocument = new PDDocument();
                ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            PDPage page = new PDPage();
            tmpDocument.addPage(page);

            PDAcroForm newAcroForm = new PDAcroForm(document);
            tmpDocument.getDocumentCatalog().setAcroForm(newAcroForm);

            PDTextField textBox = new PDTextField(newAcroForm);
            textBox.setPartialName("SampleField");
            newAcroForm.getFields().add(textBox);

            PDAnnotationWidget widget = textBox.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 750, 200, 20);
            widget.setRectangle(rect);
            widget.setPage(page);

            page.getAnnotations().add(widget);

            // acroForm.setNeedAppearances(true);
            // acroForm.getField("SampleField").getCOSObject().setString(COSName.V, "content");

            tmpDocument.save(baos); // this is a working PDF
            return baos.toByteArray();
        }
    }
    
    private int countWidgets(PDDocument documentToTest)
    {
        int count = 0;
        for (PDPage page : documentToTest.getPages())
        {
            try
            {
                for (PDAnnotation annotation : page.getAnnotations())
                {
                    if (annotation instanceof PDAnnotationWidget)
                    {
                        count ++;
                    }
                }
            }
            catch (IOException e)
            {
                // ignoring
            }
        }
        return count;
    } 
}
