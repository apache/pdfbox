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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.rendering.TestPDFToImage;
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
    
    private static final File OUT_DIR = new File("target/test-output");
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    
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
        PDDocument testPdf = PDDocument.load(new File(IN_DIR, "AlignmentTests.pdf"));
        testPdf.getDocumentCatalog().getAcroForm().flatten();
        assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
        File file = new File(OUT_DIR, "AlignmentTests-flattened.pdf");
        testPdf.save(file);
        // compare rendering
        TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
        if (!testPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
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
    public void testFlattenWidgetNoRef() throws IOException
    {
        PDDocument testPdf = PDDocument.load(new File(IN_DIR, "AlignmentTests.pdf"));
        PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
        for (PDField field : acroForm.getFieldTree()) {
        	for (PDAnnotationWidget widget : field.getWidgets()) {
        		widget.getCOSObject().removeItem(COSName.P);
        	}
        }
        testPdf.getDocumentCatalog().getAcroForm().flatten();

        // 36 non widget annotations shall not be flattened
        assertEquals(36, testPdf.getPage(0).getAnnotations().size());

        assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
        File file = new File(OUT_DIR, "AlignmentTests-flattened-noRef.pdf");
        testPdf.save(file);
        // compare rendering
        TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
        if (!testPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
        {
            // don't fail, rendering is different on different systems, result must be viewed manually
            System.out.println("Rendering of " + file + " failed or is not identical to expected rendering in " + IN_DIR + " directory");
        }
    }

    @Test
    public void testFlattenSpecificFieldsOnly() throws IOException
    {
        File file = new File(OUT_DIR, "AlignmentTests-flattened-specificFields.pdf");
        
        List<PDField> fieldsToFlatten = new ArrayList<PDField>();
        
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(new File(IN_DIR, "AlignmentTests.pdf"));
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
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    } 
    
    /*
     * Test that we do not modify an AcroForm with missing resource information
     * when loading the document only.
     * (PDFBOX-3752)
     */
    @Test
    public void testDontAddMissingInformationOnDocumentLoad()
    {
        try
        {
            byte[] pdfBytes =  createAcroFormWithMissingResourceInformation();
            PDDocument pdfDocument = PDDocument.load(pdfBytes);
            
            // do a low level access to the AcroForm to avoid the generation of missing entries
            PDDocumentCatalog documentCatalog = pdfDocument.getDocumentCatalog();
            COSDictionary catalogDictionary = documentCatalog.getCOSObject();
            COSDictionary acroFormDictionary = (COSDictionary) catalogDictionary.getDictionaryObject(COSName.ACRO_FORM);

            // ensure that the missing information has not been generated
            assertNull(acroFormDictionary.getDictionaryObject(COSName.DA));
            assertNull(acroFormDictionary.getDictionaryObject(COSName.RESOURCES));
            
            pdfDocument.close();
        }
        catch (IOException e)
        {
            System.err.println("Couldn't create test document, test skipped");
            return;
        }
    } 
    
    /*
     * Test that we add missing ressouce information to an AcroForm 
     * when accessing the AcroForm on the PD level
     * (PDFBOX-3752)
     */
    @Test
    public void testAddMissingInformationOnAcroFormAccess()
    {
        try
        {
            byte[] pdfBytes =  createAcroFormWithMissingResourceInformation();
            PDDocument pdfDocument = PDDocument.load(pdfBytes);
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

            pdfDocument.close();
        }
        catch (IOException e)
        {
            System.err.println("Couldn't create test document, test skipped");
            return;
        }
    }

    /**
     * PDFBOX-4235: a bad /DA string should not result in an NPE.
     * 
     * @throws IOException 
     */
    @Test
    public void testBadDA() throws IOException
    {
        PDDocument doc = new PDDocument();

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

        try
        {
            textBox.setValue("huhu");
        }
        catch (IllegalArgumentException ex)
        {
            return;
        }
        finally
        {
            doc.close();
        }
        fail("IllegalArgumentException should have been thrown");
    }

    /**
     * PDFBOX-3732, PDFBOX-4303, PDFBOX-4393: Test whether /Helv and /ZaDb get added, but only if
     * they don't exist.
     */
    @Test
    public void testAcroFormDefaultFonts() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument doc = new PDDocument();

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
        doc.close();

        doc = PDDocument.load(baos.toByteArray());
        acroForm2 = doc.getDocumentCatalog().getAcroForm();
        defaultResources = acroForm2.getDefaultResources();
        helv = defaultResources.getFont(COSName.HELV);
        zadb = defaultResources.getFont(COSName.ZA_DB);
        assertNotNull(helv);
        assertNotNull(zadb);
        // make sure that font wasn't overwritten
        assertNotEquals(PDType1Font.HELVETICA, helv);
        assertNotEquals(PDType1Font.ZAPF_DINGBATS, zadb);
        doc.close();
    }

    @After
    public void tearDown() throws IOException
    {
        document.close();
    }

    private byte[] createAcroFormWithMissingResourceInformation() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDAcroForm newAcroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(newAcroForm);

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos); // this is a working PDF
        document.close();
        return baos.toByteArray();
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

