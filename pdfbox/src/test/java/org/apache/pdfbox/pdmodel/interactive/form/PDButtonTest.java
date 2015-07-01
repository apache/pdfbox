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
        assertEquals(checkbox.getOnValue(), "Yes");
        assertEquals(checkbox.getOnValues().size(),1);
        assertEquals(checkbox.getOnValues().get(0), "Yes");
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
    public void testAcrobatradioButtonProperties() throws IOException
    {
        PDRadioButton radioButton = (PDRadioButton) acrobatAcroForm.getField("RadioButtonGroup");
        assertEquals(radioButton.getOnValues().size(),2);
        assertEquals(radioButton.getOnValues().get(0),"RadioButton01");
        assertEquals(radioButton.getOnValues().get(1),"RadioButton02");
    }
    
    
    @After
    public void tearDown() throws IOException
    {
        document.close();
        acrobatDocument.close();
    }
    
}

