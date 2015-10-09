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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
public class PDButtonTest
{
    
    
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "AcroFormsBasicFields.pdf";
    
    private PDDocument document;
    private PDAcroForm acroForm;

    private PDDocument acrobatDocument;
    private PDAcroForm acrobatAcroForm;
    
    
    @Before
    public void setUp() throws IOException
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
        
        acrobatDocument = PDDocument.load(new File(IN_DIR, NAME_OF_PDF));
        acrobatAcroForm = acrobatDocument.getDocumentCatalog().getAcroForm();
    }

    @Test
    public void createCheckBox()
    {
        PDButton buttonField = new PDCheckbox(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals(buttonField.getFieldType(), "Btn");
        assertFalse(buttonField.isPushButton());
        assertFalse(buttonField.isRadioButton());
    }

    @Test
    public void createPushButton()
    {
        PDButton buttonField = new PDPushButton(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals(buttonField.getFieldType(), "Btn");
        assertTrue(buttonField.isPushButton());
        assertFalse(buttonField.isRadioButton());
    }

    @Test
    public void createRadioButton()
    {
        PDButton buttonField = new PDRadioButton(acroForm);
        
        assertEquals(buttonField.getFieldType(), buttonField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals(buttonField.getFieldType(), "Btn");
        assertTrue(buttonField.isRadioButton());
        assertFalse(buttonField.isPushButton());
    }
        
    @Test
    public void retrieveAcrobatCheckBoxProperties() throws IOException
    {
        PDCheckbox checkbox = (PDCheckbox) acrobatAcroForm.getField("Checkbox");
        assertNotNull(checkbox);
        assertEquals(checkbox.getOnValue(), "Yes");
        assertEquals(checkbox.getOnValues().size(), 1);
        assertTrue(checkbox.getOnValues().contains("Yes"));
    }
    
    @Test
    public void testAcrobatCheckBoxProperties() throws IOException
    {
        PDCheckbox checkbox = (PDCheckbox) acrobatAcroForm.getField("Checkbox");
        assertEquals(checkbox.getValue(), "");
        assertEquals(checkbox.isChecked(), false);

        checkbox.check();
        assertEquals(checkbox.getValue(), checkbox.getOnValue());
        assertEquals(checkbox.isChecked(), true);

        checkbox.setValue("Yes");
        assertEquals(checkbox.getValue(), checkbox.getOnValue());
        assertEquals(checkbox.isChecked(), true);
        assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.YES);

        checkbox.setValue("Off");
        assertEquals(checkbox.getValue(), COSName.Off.getName());
        assertEquals(checkbox.isChecked(), false);
        assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);

        checkbox = (PDCheckbox) acrobatAcroForm.getField("Checkbox-DefaultValue");
        assertEquals(checkbox.getDefaultValue(), checkbox.getOnValue());
        
        checkbox.setDefaultValue("Off");
        assertEquals(checkbox.getDefaultValue(), COSName.Off.getName());
    }
    
    @Test
    public void setValueForAbstractedAcrobatCheckBox() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("Checkbox");

        checkbox.setValue("Yes");
        assertEquals(checkbox.getValueAsString(), ((PDCheckbox) checkbox).getOnValue());
        assertEquals(((PDCheckbox) checkbox).isChecked(), true);
        assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.YES);

        checkbox.setValue("Off");
        assertEquals(checkbox.getValueAsString(), COSName.Off.getName());
        assertEquals(((PDCheckbox) checkbox).isChecked(), false);
        assertEquals(checkbox.getCOSObject().getDictionaryObject(COSName.AS), COSName.Off);
    }
    
    @Test
    public void testAcrobatCheckBoxGroupProperties() throws IOException
    {
        PDCheckbox checkbox = (PDCheckbox) acrobatAcroForm.getField("CheckboxGroup");
        assertEquals(checkbox.getValue(), "");
        assertEquals(checkbox.isChecked(), false);

        checkbox.check();
        assertEquals(checkbox.getValue(), checkbox.getOnValue());
        assertEquals(checkbox.isChecked(), true);
        
        assertEquals(checkbox.getOnValues().size(), 3);
        assertTrue(checkbox.getOnValues().contains("Option1"));
        assertTrue(checkbox.getOnValues().contains("Option2"));
        assertTrue(checkbox.getOnValues().contains("Option3"));
        
        // test a value which sets one of the individual checkboxes within the group
        checkbox.setValue("Option1");
        assertEquals("Option1",checkbox.getValue());
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
        assertEquals("Option3",checkbox.getValue());
        assertEquals("Option3",checkbox.getValueAsString());
        
        // ensure that for both widgets representing the individual checkboxes
        // the AS entry has been set
        assertEquals("Off",checkbox.getWidgets().get(0).getAppearanceState().getName());
        assertEquals("Off",checkbox.getWidgets().get(1).getAppearanceState().getName());
        assertEquals("Option3",checkbox.getWidgets().get(2).getAppearanceState().getName());
        assertEquals("Option3",checkbox.getWidgets().get(3).getAppearanceState().getName());
    }
    
    @Test
    public void setValueForAbstractedCheckBoxGroup() throws IOException
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
    
    @Test(expected=IllegalArgumentException.class)
    public void setCheckboxInvalidValue() throws IOException
    {
        PDCheckbox checkbox = (PDCheckbox) acrobatAcroForm.getField("Checkbox");
        // Set a value which doesn't match the radio button list 
        checkbox.setValue("InvalidValue");
    }    

    @Test(expected=IllegalArgumentException.class)
    public void setCheckboxGroupInvalidValue() throws IOException
    {
        PDCheckbox checkbox = (PDCheckbox) acrobatAcroForm.getField("CheckboxGroup");
        // Set a value which doesn't match the radio button list 
        checkbox.setValue("InvalidValue");
    }    

    @Test(expected=IllegalArgumentException.class)
    public void setAbstractedCheckboxInvalidValue() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("Checkbox");
        // Set a value which doesn't match the radio button list 
        checkbox.setValue("InvalidValue");
    }    

    @Test(expected=IllegalArgumentException.class)
    public void setAbstractedCheckboxGroupInvalidValue() throws IOException
    {
        PDField checkbox = acrobatAcroForm.getField("CheckboxGroup");
        // Set a value which doesn't match the radio button list 
        checkbox.setValue("InvalidValue");
    }    

    @Test
    public void retrieveAcrobatRadioButtonProperties() throws IOException
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
        assertNotNull(radioButton);
        assertEquals(radioButton.getOnValues().size(), 2);
        assertTrue(radioButton.getOnValues().contains("RadioButton01"));
        assertTrue(radioButton.getOnValues().contains("RadioButton02"));
    }
    
    @Test
    public void testAcrobatRadioButtonProperties() throws IOException
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");

        // Set value so that first radio button option is selected
        radioButton.setValue("RadioButton01");
        assertEquals(radioButton.getValue(), "RadioButton01");
        // First option shall have /RadioButton01, second shall have /Off
        assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.getPDFName("RadioButton01"));
        assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.Off);

        // Set value so that second radio button option is selected
        radioButton.setValue("RadioButton02");
        assertEquals(radioButton.getValue(), "RadioButton02");
        // First option shall have /Off, second shall have /RadioButton02
        assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.Off);
        assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.getPDFName("RadioButton02"));
    }
    
    @Test
    public void setValueForAbstractedAcrobatRadioButton() throws IOException
    {
        PDField radioButton = acrobatAcroForm.getField("RadioButtonGroup");

        // Set value so that first radio button option is selected
        radioButton.setValue("RadioButton01");
        assertEquals(radioButton.getValueAsString(), "RadioButton01");
        // First option shall have /RadioButton01, second shall have /Off
        assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.getPDFName("RadioButton01"));
        assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.Off);

        // Set value so that second radio button option is selected
        radioButton.setValue("RadioButton02");
        assertEquals(radioButton.getValueAsString(), "RadioButton02");
        // First option shall have /Off, second shall have /RadioButton02
        assertEquals(radioButton.getWidgets().get(0).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.Off);
        assertEquals(radioButton.getWidgets().get(1).getCOSObject().getDictionaryObject(COSName.AS),
                COSName.getPDFName("RadioButton02"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void setRadioButtonInvalidValue() throws IOException
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
        // Set a value which doesn't match the radio button list 
        radioButton.setValue("InvalidValue");
    }

    @Test(expected=IllegalArgumentException.class)
    public void setAbstractedRadioButtonInvalidValue() throws IOException
    {
        PDField radioButton = acrobatAcroForm.getField("RadioButtonGroup");
        // Set a value which doesn't match the radio button list 
        radioButton.setValue("InvalidValue");
    }
    
    @After
    public void tearDown() throws IOException
    {
        document.close();
        acrobatDocument.close();
    }
    
}

