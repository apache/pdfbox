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

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDChoice class.
 *
 */
public class PDChoiceTest
{
    private PDDocument document;
    private PDAcroForm acroForm;

    @Before
    public void setUp()
    {
        document = new PDDocument();
        acroForm = new PDAcroForm(document);
    }

    @Test
    public void createListBox()
    {
        PDChoice choiceField = new PDListBox(acroForm);
        
        assertEquals(choiceField.getFieldType(), choiceField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals(choiceField.getFieldType(), "Ch");
        assertFalse(choiceField.isCombo());
    }

    @Test
    public void createComboBox()
    {
        PDChoice choiceField = new PDComboBox(acroForm);
        
        assertEquals(choiceField.getFieldType(), choiceField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals(choiceField.getFieldType(), "Ch");
        assertTrue(choiceField.isCombo());
    }

}

