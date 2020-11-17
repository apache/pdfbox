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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the PDChoice class.
 *
 */
class PDChoiceTest
{
    private PDDocument document;
    private PDAcroForm acroForm;
    private List<String> options;

    @BeforeEach
    public void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
        options = new ArrayList<>();
        options.add(" ");
        options.add("A");
        options.add("B");
    }

    @Test
    void createListBox()
    {
        PDChoice choiceField = new PDListBox(acroForm);
        
        assertEquals(choiceField.getFieldType(), choiceField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Ch", choiceField.getFieldType());
        assertFalse(choiceField.isCombo());
    }

    @Test
    void createComboBox()
    {
        PDChoice choiceField = new PDComboBox(acroForm);
        
        assertEquals(choiceField.getFieldType(), choiceField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Ch", choiceField.getFieldType());
        assertTrue(choiceField.isCombo());
    }
    
    @Test
    void getOptionsFromStrings()
    {
        PDChoice choiceField = new PDComboBox(acroForm);
        COSArray choiceFieldOptions = new COSArray();
        choiceFieldOptions.add(new COSString(" "));
        choiceFieldOptions.add(new COSString("A"));
        choiceFieldOptions.add(new COSString("B"));
        
        // add the options using the low level COS model as the PD model will
        // abstract the COSArray
        choiceField.getCOSObject().setItem(COSName.OPT, choiceFieldOptions);

        assertEquals(options, choiceField.getOptions());
    }

    @Test
    void getOptionsFromCOSArray()
    {
        PDChoice choiceField = new PDComboBox(acroForm);
        COSArray choiceFieldOptions = new COSArray();

        // add entry to options
        COSArray entry = new COSArray();
        entry.add(new COSString(" "));
        choiceFieldOptions.add(entry);

        // add entry to options
        entry = new COSArray();
        entry.add(new COSString("A"));
        choiceFieldOptions.add(entry);

        // add entry to options
        entry = new COSArray();
        entry.add(new COSString("B"));
        choiceFieldOptions.add(entry);
                
        // add the options using the low level COS model as the PD model will
        // abstract the COSArray
        choiceField.getCOSObject().setItem(COSName.OPT, choiceFieldOptions);

        assertEquals(options, choiceField.getOptions());
    }
    
    /*
     * Get the entries form a moxed values array. See PDFBOX-4185
     */
    @Test
    void getOptionsFromMixed()
    {
        PDChoice choiceField = new PDComboBox(acroForm);
        COSArray choiceFieldOptions = new COSArray();

        // add string entry to options
        choiceFieldOptions.add(new COSString(" "));

        // add array entry to options
        COSArray entry = new COSArray();
        entry.add(new COSString("A"));
        choiceFieldOptions.add(entry);

        // add array entry to options
        entry = new COSArray();
        entry.add(new COSString("B"));
        choiceFieldOptions.add(entry);
                
        // add the options using the low level COS model as the PD model will
        // abstract the COSArray
        choiceField.getCOSObject().setItem(COSName.OPT, choiceFieldOptions);

        assertEquals(options, choiceField.getOptions());
    }
}

