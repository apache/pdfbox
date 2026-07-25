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

import java.io.ByteArrayOutputStream;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

import org.junit.Test;

/**
 * This will test the functionality of Radio Buttons in PDFBox.
 */
public class TestRadioButtons
{
    static final File TESTFILE3656 =
            new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form/PDFBOX-3656-SF1199AEG (Complete).pdf");

    /**
     * This will test the radio button PDModel.
     *
     * @throws IOException If there is an error creating the field.
     */
    @Test
    public void testRadioButtonPDModel() throws IOException
    {
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();
            PDAcroForm form = new PDAcroForm( doc );
            PDRadioButton radioButton = new PDRadioButton(form);
            
            // test that there are no nulls returned for an empty field
            // only specific methods are tested here
            assertNotNull(radioButton.getDefaultValue());
            assertNotNull(radioButton.getSelectedExportValues());
            assertNotNull(radioButton.getExportValues());
            assertNotNull(radioButton.getValue());
            
            // Test setting/getting option values - the dictionaries Opt entry
            List<String> options = new ArrayList<String>();
            options.add("Value01");
            options.add("Value02");
            radioButton.setExportValues(options);

            // Test getSelectedExportValues()
            List<PDAnnotationWidget> widgets = new ArrayList<PDAnnotationWidget>();
            for (int i = 0; i < options.size(); i++)
            {
                PDAnnotationWidget widget = new PDAnnotationWidget();
                COSDictionary apNDict = new COSDictionary();
                apNDict.setItem(COSName.Off, new PDAppearanceStream(doc));
                apNDict.setItem(options.get(i), new PDAppearanceStream(doc));

                PDAppearanceDictionary appearance = new PDAppearanceDictionary();
                PDAppearanceEntry appearanceNEntry = new PDAppearanceEntry(apNDict);
                appearance.setNormalAppearance(appearanceNEntry);
                widget.setAppearance(appearance);
                widget.setAppearanceState("Off");
                widgets.add(widget);
            }
            radioButton.setWidgets(widgets);

            radioButton.setValue("Value01");
            assertEquals("Value01", radioButton.getValue());
            assertEquals(1, radioButton.getSelectedExportValues().size());
            assertEquals("Value01", radioButton.getSelectedExportValues().get(0));
            assertEquals("Value01", widgets.get(0).getAppearanceState().getName());
            assertEquals("Off", widgets.get(1).getAppearanceState().getName());

            radioButton.setValue("Value02");
            assertEquals("Value02", radioButton.getValue());
            assertEquals(1, radioButton.getSelectedExportValues().size());
            assertEquals("Value02", radioButton.getSelectedExportValues().get(0));
            assertEquals("Off", widgets.get(0).getAppearanceState().getName());
            assertEquals("Value02", widgets.get(1).getAppearanceState().getName());

            radioButton.setValue("Off");
            assertEquals("Off", radioButton.getValue());
            assertEquals(0, radioButton.getSelectedExportValues().size());
            assertEquals("Off", widgets.get(0).getAppearanceState().getName());
            assertEquals("Off", widgets.get(1).getAppearanceState().getName());

            COSArray optItem = (COSArray) radioButton.getCOSObject().getItem(COSName.OPT);

            // assert that the values have been correctly set
            assertNotNull(radioButton.getCOSObject().getItem(COSName.OPT));
            assertEquals(2, optItem.size());
            assertEquals(options.get(0), optItem.getString(0));
            
            // assert that the values can be retrieved correctly
            List<String> retrievedOptions = radioButton.getExportValues();
            assertEquals(2, retrievedOptions.size());
            assertEquals(retrievedOptions, options);

            // assert that the Opt entry is removed
            radioButton.setExportValues(null);
            assertNull(radioButton.getCOSObject().getItem(COSName.OPT));
            // if there is no Opt entry an empty List shall be returned
            assertEquals(radioButton.getExportValues(), new ArrayList<String>());
        }
        finally
        {
            {
                IOUtils.closeQuietly(doc);
            }
        }
    }

    /**
     * Test getting/setting the NoToggleToOff flag (PDF spec Ff bit 15).
     *
     * @throws IOException If there is an error creating the field.
     */
    @Test
    public void testNoToggleToOff() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDAcroForm acroForm = new PDAcroForm(doc);
        PDRadioButton radioButton = new PDRadioButton(acroForm);

        // default shall be false/unset
        assertFalse(radioButton.isNoToggleToOff());

        radioButton.setNoToggleToOff(true);
        assertTrue(radioButton.isNoToggleToOff());
        // spec bit 15 -> mask 1 << 14
        assertEquals(1 << 14,
                radioButton.getCOSObject().getInt(COSName.FF) & (1 << 14));

        radioButton.setNoToggleToOff(false);
        assertFalse(radioButton.isNoToggleToOff());
        assertEquals(0, radioButton.getCOSObject().getInt(COSName.FF) & (1 << 14));

        doc.close();
    }

    /**
     * PDFBOX-3656 Radio button field with FLAG_RADIOS_IN_UNISON false
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox3656NotInUnison() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);

            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            assertFalse("the radio buttons can be selected individually although having the same ON value", field.isRadiosInUnison());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-3656 Set by value
     * 
     * There are 6 radio buttons where 3 share the same common values but they are not set in unison
     * Setting by the first export value shall only select the first radio button
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox3656ByValidExportValue() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            // check defaults
            assertFalse("the radio buttons can be selected individually although having the same ON value", field.isRadiosInUnison());
            assertEquals("initially no option shall be selected", "Off", field.getValue());
            // set the field to a valid export value
            field.setValue("Checking");
            assertEquals("setting by the export value should also return that", "Checking", field.getValue());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-3656 Set by invalid export value
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox3656ByInvalidExportValue() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            // check defaults
            assertFalse("the radio buttons can be selected individually although having the same ON value", field.isRadiosInUnison());
            assertEquals("initially no option shall be selected", "Off", field.getValue());

            try
            {
                field.setValue("Invalid");
                fail("Expected an IllegalArgumentException to be thrown");
            }
            catch (IllegalArgumentException ex)
            {
                // compare the messages
                String expectedMessage = "value 'Invalid' is not a valid option for the field Checking/Savings, valid values are: [Checking, Savings] and Off";
                String actualMessage = ex.getMessage();
                assertTrue(actualMessage.contains(expectedMessage));
            }

            assertEquals("no option shall be selected", "Off", field.getValue());
            assertTrue("no export values are selected", field.getSelectedExportValues().isEmpty());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-3656 Set by a valid index
     * 
     * There are 6 radio buttons where 3 share the same common values but they are not set in unison
     * Setting by the index shall only select the corresponding radio button
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox3656ByValidIndex() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            // check defaults
            assertFalse("the radio buttons can be selected individually although having the same ON value", field.isRadiosInUnison());
            assertEquals("initially no option shall be selected", "Off", field.getValue());
            // set the field to a valid index
            field.setValue(4);
            assertEquals("setting by the index value should return the corresponding export", "Checking", field.getValue());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-3656 Set by an invalid index
     * 
     * There are 6 radio buttons where 3 share the same common values but they are not set in unison
     * Setting by the index shall only select the corresponding radio button
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox3656ByInvalidIndex() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            // check defaults
            assertFalse("the radio buttons can be selected individually although having the same ON value", field.isRadiosInUnison());
            assertEquals("initially no option shall be selected", "Off", field.getValue());

            try
            {
                field.setValue(6);
                fail("Expected an IllegalArgumentException to be thrown");
            }
            catch (IllegalArgumentException ex)
            {
                // compare the messages
                String expectedMessage = "index '6' is not a valid index for the field Checking/Savings, valid indices are from 0 to 5";
                String actualMessage = ex.getMessage();
                assertTrue(actualMessage.contains(expectedMessage));
            }

            assertEquals("no option shall be selected", "Off", field.getValue());
            assertTrue("no export values are selected", field.getSelectedExportValues().isEmpty());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-4617 Enable getting selected index
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox4617IndexNoneSelected() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            assertEquals("if there is no value set the index shall be -1", -1, field.getSelectedIndex());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-4617 Enable getting selected index for value being set by option
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox4617IndexForSetByOption() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            field.setValue( "Checking");
            assertEquals("the index shall be equal with the first entry of Checking which is 0", 0, field.getSelectedIndex());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-4617 Enable getting selected index for value being set by index
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox4617IndexForSetByIndex() throws IOException
    {
        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(TESTFILE3656);
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Checking/Savings");
            field.setValue(4);
            assertEquals("setting by the index value should return the corresponding export", "Checking", field.getValue());
            assertEquals("the index shall be equals with the set value of 4", 4, field.getSelectedIndex());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-5831 Numeric value for Opt entry
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testPDFBox5831NumericValueForOpt() throws IOException, URISyntaxException
    {
        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13069137/AU_Erklaerung_final.pdf";

        PDDocument testPdf = null;
        try
        {
            testPdf = PDDocument.load(new URL(sourceUrl).openStream());
            PDAcroForm acroForm = testPdf.getDocumentCatalog().getAcroForm();
            PDRadioButton field = (PDRadioButton) acroForm.getField("Formular1[0].Seite1[0].TF_P[0].Optionsfeldliste[0]");

            field.setValue(0);
            assertEquals("1", field.getValue());
            assertEquals(COSName.getPDFName("0"), field.getCOSObject().getDictionaryObject(COSName.V));
            assertEquals(0, field.getSelectedIndex());

            field.setValue("1");
            assertEquals("1", field.getValue());
            assertEquals(COSName.getPDFName("0"), field.getCOSObject().getDictionaryObject(COSName.V));
            assertEquals(0, field.getSelectedIndex());

            field.setValue(1);
            assertEquals("2", field.getValue());
            assertEquals(COSName.getPDFName("1"), field.getCOSObject().getDictionaryObject(COSName.V));
            assertEquals(1, field.getSelectedIndex());

            field.setValue("2");
            assertEquals("2", field.getValue());
            assertEquals(COSName.getPDFName("1"), field.getCOSObject().getDictionaryObject(COSName.V));
            assertEquals(1, field.getSelectedIndex());
        }
        finally
        {
            IOUtils.closeQuietly(testPdf);
        }
    }

    /**
     * PDFBOX-6178: Ensure that RadioButton values with non-ASCII characters preserve encoding.
     * When setting a RadioButton value to "männlich", both V and AS entries should preserve
     * the original byte encoding from the appearance dictionary (0xE4 for ä in ISO-8859-1,
     * not 0xC3 0xA4 from UTF-8).
     * 
     * @throws IOException
     */
    @Test
    public void testPDFBox6178NonAsciiRadioButtonValue() throws IOException
    {
        PDDocument document = null;
        File pdfFile = new File("target/pdfs/PDFBOX-6178.pdf");
        if (!pdfFile.exists())
        {
            return;  // Skip test if PDF not available
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Load document, set value, and save to memory
        try
        {
            document = PDDocument.load(pdfFile);
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDField field = acroForm.getField("Geschlecht");
            
            field.setValue("männlich");
            
            // Verify V entry preserves encoding - should have 0xE4 byte for ä (ISO-8859-1)
            COSName vEntry = (COSName) field.getCOSObject().getDictionaryObject(COSName.V);
            assertNotNull("V entry should not be null after setValue", vEntry);
            
            // Check that the bytes contain 0xE4 (ISO-8859-1 ä) not 0xC3 0xA4 (UTF-8)
            byte[] vBytes = vEntry.getBytes();
            
            assertFalse("V entry should not contain UTF-8 encoded ä (0xC3 0xA4)",
                    containsSequence(vBytes, new byte[]{(byte) 0xC3, (byte) 0xA4}));
            assertTrue("V entry should contain ISO-8859-1 encoded ä (0xE4)",
                    containsSequence(vBytes, new byte[]{(byte) 0xE4}));
            
            // Verify AS entry preserves encoding
            COSName asEntry = (COSName) field.getWidgets().get(0).getCOSObject()
                    .getDictionaryObject(COSName.AS);
            assertNotNull("AS entry should not be null after setValue", asEntry);
            
            // Check that the AS bytes also preserve ISO-8859-1 encoding
            byte[] asBytes = asEntry.getBytes();
            
            assertFalse("AS entry should not contain UTF-8 encoded ä (0xC3 0xA4)",
                    containsSequence(asBytes, new byte[]{(byte) 0xC3, (byte) 0xA4}));
            assertTrue("AS entry should contain ISO-8859-1 encoded ä (0xE4)",
                    containsSequence(asBytes, new byte[]{(byte) 0xE4}));
            
            document.save(baos);
        }
        finally
        {
            IOUtils.closeQuietly(document);
        }

        // Reload and verify entries are still correct
        try
        {
            document = PDDocument.load(baos.toByteArray()); 
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDField field = acroForm.getField("Geschlecht");
            
            // Verify V entry after reload
            COSName vEntry = (COSName) field.getCOSObject().getDictionaryObject(COSName.V);
            assertNotNull("V entry should not be null after reload", vEntry);
            
            byte[] vBytes = vEntry.getBytes();
            assertFalse("V entry should still not contain UTF-8 ä after reload",
                    containsSequence(vBytes, new byte[]{(byte) 0xC3, (byte) 0xA4}));
            assertTrue("V entry should still contain ISO-8859-1 ä after reload",
                    containsSequence(vBytes, new byte[]{(byte) 0xE4}));
            
            // Verify AS entry after reload
            COSName asEntry = (COSName) field.getWidgets().get(0).getCOSObject()
                    .getDictionaryObject(COSName.AS);
            assertNotNull("AS entry should not be null after reload", asEntry);
            
            byte[] asBytes = asEntry.getBytes();
            assertFalse("AS entry should still not contain UTF-8 ä after reload",
                    containsSequence(asBytes, new byte[]{(byte) 0xC3, (byte) 0xA4}));
            assertTrue("AS entry should still contain ISO-8859-1 ä after reload",
                    containsSequence(asBytes, new byte[]{(byte) 0xE4}));
        }
        finally
        {
            IOUtils.closeQuietly(document);
        }
    }

    /**
     * Helper method to check if a byte sequence contains a particular sub-sequence.
     *
     * @param haystack the bytes to search in
     * @param needle the bytes to search for
     * @return true if needle is found in haystack, false otherwise
     */
    private boolean containsSequence(byte[] haystack, byte[] needle)
    {
        if (needle.length == 0)
        {
            return true;
        }
        if (needle.length > haystack.length)
        {
            return false;
        }
        for (int i = 0; i <= haystack.length - needle.length; i++)
        {
            boolean match = true;
            for (int j = 0; j < needle.length; j++)
            {
                if (haystack[i + j] != needle[j])
                {
                    match = false;
                    break;
                }
            }
            if (match)
            {
                return true;
            }
        }
        return false;
    } 
}