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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test for the PDButton class.
 *
 */
class PDButtonTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "AcroFormsBasicFields.pdf";
    private static final File TARGET_PDF_DIR = new File("target/pdfs");

    private PDDocument document;
    private PDAcroForm acroForm;

    private PDDocument acrobatDocument;
    private PDAcroForm acrobatAcroForm;
    
    
    @BeforeEach
    public void setUp() throws IOException
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
        
        acrobatDocument = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF));
        acrobatAcroForm = acrobatDocument.getDocumentCatalog().getAcroForm();
    }

    @Test
    void createCheckBox()
    {
        PDButton buttonField = new PDCheckBox(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Btn", buttonField.getFieldType());
        assertFalse(buttonField.isPushButton());
        assertFalse(buttonField.isRadioButton());
    }

    @Test
    void createPushButton()
    {
        PDButton buttonField = new PDPushButton(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Btn", buttonField.getFieldType());
        assertTrue(buttonField.isPushButton());
        assertFalse(buttonField.isRadioButton());
    }

    @Test
    void createRadioButton()
    {
        PDButton buttonField = new PDRadioButton(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Btn", buttonField.getFieldType());
        assertTrue(buttonField.isRadioButton());
        assertFalse(buttonField.isPushButton());
    }
    
    /**
     * PDFBOX-3656
     * 
     * Test a radio button with options.
     * This was causing an ArrayIndexOutOfBoundsException when trying to set to "Off", as this
     * wasn't treated to be a valid option.
     */
    @Test
    void testRadioButtonWithOptions()
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-3656.pdf");
        
        try (InputStream is = new FileInputStream(file);
                PDDocument pdfDocument = Loader.loadPDF(is))
        {   
            PDRadioButton radioButton = (PDRadioButton) pdfDocument.getDocumentCatalog().getAcroForm().getField("Checking/Savings");
            radioButton.setValue("Off");
            radioButton.getWidgets().forEach(widget ->
                assertEquals(COSName.Off, widget.getCOSObject().getItem(COSName.AS),
                        "The widget should be set to Off"));
        }
        catch (IOException e)
        {
            fail("Unexpected IOException " + e.getMessage());
        }
    }
    
    /**
     * PDFBOX-3682
     * 
     * Test a radio button with options.
     * Special handling for a radio button with /Opt and the On state not being named
     * after the index.
     * 
     */
    @Test
    void testOptionsAndNamesNotNumbers()
    {
        File file = new File(TARGET_PDF_DIR, "PDFBOX-3682.pdf");
        try (InputStream is = new FileInputStream(file);
                PDDocument pdfDocument = Loader.loadPDF(is))
        {            
            pdfDocument.getDocumentCatalog().getAcroForm().getField("RadioButton").setValue("c");
            PDRadioButton radioButton = (PDRadioButton) pdfDocument.getDocumentCatalog().getAcroForm().getField("RadioButton");
            radioButton.setValue("c");

            // test that the old behavior is now invalid
            assertNotEquals("2", radioButton.getValueAsString(), "This shall no longer be 2");
            assertNotEquals("2",
                    radioButton.getWidgets().get(2).getCOSObject().getNameAsString(COSName.AS),
                    "This shall no longer be 2");
            
            // test for the correct behavior
            assertEquals("c", radioButton.getValueAsString(), "This shall be c");
            assertEquals("c",
                    radioButton.getWidgets().get(2).getCOSObject().getNameAsString(COSName.AS),
                    "This shall be c");
        }
        catch (IOException e)
        {
            fail("Unexpected IOException " + e.getMessage());
        }
    }
        
    @Test
    void retrieveAcrobatCheckBoxProperties()
    {
        PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox");
        assertNotNull(checkbox);
        assertEquals("Yes", checkbox.getOnValue());
        assertEquals(1, checkbox.getOnValues().size());
        assertTrue(checkbox.getOnValues().contains("Yes"));
    }
    
    @Test
    void testAcrobatCheckBoxProperties() throws IOException
    {
        PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox");
        assertEquals("Off", checkbox.getValue());
        assertEquals(false, checkbox.isChecked());

        checkbox.check();
        assertEquals(checkbox.getValue(), checkbox.getOnValue());
        assertEquals(true, checkbox.isChecked());

        checkbox.setValue("Yes");
        assertEquals(checkbox.getValue(), checkbox.getOnValue());
        assertEquals(true, checkbox.isChecked());
        assertEquals(COSName.YES, checkbox.getCOSObject().getDictionaryObject(COSName.AS));

        checkbox.setValue("Off");
        assertEquals(COSName.Off.getName(), checkbox.getValue());
        assertEquals(false, checkbox.isChecked());
        assertEquals(COSName.Off, checkbox.getCOSObject().getDictionaryObject(COSName.AS));

        checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox-DefaultValue");
        assertEquals(checkbox.getDefaultValue(), checkbox.getOnValue());
        
        checkbox.setDefaultValue("Off");
        assertEquals(COSName.Off.getName(), checkbox.getDefaultValue());
    }
    
    @Test
    void setValueForAbstractedAcrobatCheckBox() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("Checkbox");

        checkbox.setValue("Yes");
        assertEquals(checkbox.getValueAsString(), ((PDCheckBox) checkbox).getOnValue());
        assertEquals(true, ((PDCheckBox) checkbox).isChecked());
        assertEquals(COSName.YES, checkbox.getCOSObject().getDictionaryObject(COSName.AS));

        checkbox.setValue("Off");
        assertEquals(COSName.Off.getName(), checkbox.getValueAsString());
        assertEquals(false, ((PDCheckBox) checkbox).isChecked());
        assertEquals(COSName.Off, checkbox.getCOSObject().getDictionaryObject(COSName.AS));
    }
    
    @Test
    void testAcrobatCheckBoxGroupProperties() throws IOException
    {
        PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("CheckboxGroup");
        assertEquals("Off", checkbox.getValue());
        assertEquals(false, checkbox.isChecked());

        checkbox.check();
        assertEquals(checkbox.getValue(), checkbox.getOnValue());
        assertEquals(true, checkbox.isChecked());

        assertEquals(3, checkbox.getOnValues().size());
        assertTrue(checkbox.getOnValues().contains("Option1"));
        assertTrue(checkbox.getOnValues().contains("Option2"));
        assertTrue(checkbox.getOnValues().contains("Option3"));

        // test a value which sets one of the individual checkboxes within the group
        checkbox.setValue("Option1");
        assertEquals("Option1", checkbox.getValue());
        assertEquals("Option1", checkbox.getValueAsString());

        // ensure that for the widgets representing the individual checkboxes
        // the AS entry has been set
        assertEquals("Option1", checkbox.getWidgets().get(0).getAppearanceState().getName());
        assertEquals("Off", checkbox.getWidgets().get(1).getAppearanceState().getName());
        assertEquals("Off", checkbox.getWidgets().get(2).getAppearanceState().getName());
        assertEquals("Off", checkbox.getWidgets().get(3).getAppearanceState().getName());

        // test a value which sets two of the individual chekboxes within the group
        // as the have the same name entry for being checked
        checkbox.setValue("Option3");
        assertEquals("Option3", checkbox.getValue());
        assertEquals("Option3", checkbox.getValueAsString());

        // ensure that for both widgets representing the individual checkboxes
        // the AS entry has been set
        assertEquals("Off", checkbox.getWidgets().get(0).getAppearanceState().getName());
        assertEquals("Off", checkbox.getWidgets().get(1).getAppearanceState().getName());
        assertEquals("Option3", checkbox.getWidgets().get(2).getAppearanceState().getName());
        assertEquals("Option3", checkbox.getWidgets().get(3).getAppearanceState().getName());
    }
    
    @Test
    void setValueForAbstractedCheckBoxGroup() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("CheckboxGroup");

        // test a value which sets one of the individual checkboxes within the group
        checkbox.setValue("Option1");
        assertEquals("Option1",checkbox.getValueAsString());

        // ensure that for the widgets representing the individual checkboxes
        // the AS entry has been set
        assertEquals("Option1",checkbox.getWidgets().get(0).getAppearanceState().getName());
        assertEquals("Off",checkbox.getWidgets().get(1).getAppearanceState().getName());
        assertEquals("Off",checkbox.getWidgets().get(2).getAppearanceState().getName());
        assertEquals("Off",checkbox.getWidgets().get(3).getAppearanceState().getName());
        
        // test a value which sets two of the individual chekboxes within the group
        // as the have the same name entry for being checked
        checkbox.setValue("Option3");
        assertEquals("Option3",checkbox.getValueAsString());
        
        // ensure that for both widgets representing the individual checkboxes
        // the AS entry has been set
        assertEquals("Off",checkbox.getWidgets().get(0).getAppearanceState().getName());
        assertEquals("Off",checkbox.getWidgets().get(1).getAppearanceState().getName());
        assertEquals("Option3",checkbox.getWidgets().get(2).getAppearanceState().getName());
        assertEquals("Option3",checkbox.getWidgets().get(3).getAppearanceState().getName());
    }
    
    @Test
    void setCheckboxInvalidValue() throws IOException
    {
        PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("Checkbox");
        // Set a value which doesn't match the radio button list 
        assertThrows(IllegalArgumentException.class, () -> checkbox.setValue("InvalidValue"));
    }    

    @Test
    void setCheckboxGroupInvalidValue() throws IOException
    {
        PDCheckBox checkbox = (PDCheckBox) acrobatAcroForm.getField("CheckboxGroup");
        // Set a value which doesn't match the radio button list 
        assertThrows(IllegalArgumentException.class, () -> checkbox.setValue("InvalidValue"));
    }    

    @Test
    void setAbstractedCheckboxInvalidValue() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("Checkbox");
        // Set a value which doesn't match the radio button list 
        assertThrows(IllegalArgumentException.class, () -> checkbox.setValue("InvalidValue"));
    }    

    @Test
    void setAbstractedCheckboxGroupInvalidValue() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("CheckboxGroup");
        // Set a value which doesn't match the radio button list
        assertThrows(IllegalArgumentException.class, () -> checkbox.setValue("InvalidValue"));
    }    

    @Test
    void retrieveAcrobatRadioButtonProperties()
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
        assertNotNull(radioButton);
        assertEquals(2, radioButton.getOnValues().size());
        assertTrue(radioButton.getOnValues().contains("RadioButton01"));
        assertTrue(radioButton.getOnValues().contains("RadioButton02"));
    }
    
    @Test
    void testAcrobatRadioButtonProperties() throws IOException
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");

        // Set value so that first radio button option is selected
        radioButton.setValue("RadioButton01");
        assertEquals("RadioButton01", radioButton.getValue());
        // First option shall have /RadioButton01, second shall have /Off
        assertEquals(COSName.getPDFName("RadioButton01"),
                radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS));
        assertEquals(COSName.Off,
                radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS));

        // Set value so that second radio button option is selected
        radioButton.setValue("RadioButton02");
        assertEquals("RadioButton02", radioButton.getValue());
        // First option shall have /Off, second shall have /RadioButton02
        assertEquals(COSName.Off,
                radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS));
        assertEquals(COSName.getPDFName("RadioButton02"),
                radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS));
    }
    
    @Test
    void setValueForAbstractedAcrobatRadioButton() throws IOException
    {
        PDField radioButton = acrobatAcroForm.getField("RadioButtonGroup");

        // Set value so that first radio button option is selected
        radioButton.setValue("RadioButton01");
        assertEquals("RadioButton01", radioButton.getValueAsString());
        // First option shall have /RadioButton01, second shall have /Off
        assertEquals(COSName.getPDFName("RadioButton01"),
                radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS));
        assertEquals(COSName.Off,
                radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS));

        // Set value so that second radio button option is selected
        radioButton.setValue("RadioButton02");
        assertEquals("RadioButton02", radioButton.getValueAsString());
        // First option shall have /Off, second shall have /RadioButton02
        assertEquals(COSName.Off,
                radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS));
        assertEquals(COSName.getPDFName("RadioButton02"),
                radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS));
    }
    
    @Test
    void setRadioButtonInvalidValue() throws IOException
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
        // Set a value which doesn't match the radio button list
        assertThrows(IllegalArgumentException.class, () -> radioButton.setValue("InvalidValue"));
    }

    @Test
    void setAbstractedRadioButtonInvalidValue() throws IOException
    {
        PDField radioButton = acrobatAcroForm.getField("RadioButtonGroup");
        // Set a value which doesn't match the radio button list
        assertThrows(IllegalArgumentException.class, () -> radioButton.setValue("InvalidValue"));
    }
    
    @AfterEach
    public void tearDown() throws IOException
    {
        document.close();
        acrobatDocument.close();
    }
    
}

