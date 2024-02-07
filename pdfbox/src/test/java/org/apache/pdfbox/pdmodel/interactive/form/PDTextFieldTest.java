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

import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for the PDSignatureField class.
 *
 */
public class PDTextFieldTest
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
    public void createDefaultTextField()
    {
        PDField textField = new PDTextField(acroForm);
        
        assertEquals(textField.getFieldType(), textField.getCOSObject().getNameAsString(COSName.FT));
        assertEquals("Tx", textField.getFieldType());
    }

    @Test
    public void createWidgetForGet()
    {
        PDTextField textField = new PDTextField(acroForm);

        assertNull(textField.getCOSObject().getItem(COSName.TYPE));
        assertNull(textField.getCOSObject().getNameAsString(COSName.SUBTYPE));
        
        PDAnnotationWidget widget = textField.getWidgets().get(0);
        
        assertEquals(COSName.ANNOT, textField.getCOSObject().getItem(COSName.TYPE));
        assertEquals(PDAnnotationWidget.SUB_TYPE, textField.getCOSObject().getNameAsString(COSName.SUBTYPE));
        
        assertEquals(widget.getCOSObject(), textField.getCOSObject());
    }

    /**
     * PDFBOX-5763: check that -Infinity is avoided. The test is based on a slightly modified
     * CreateSimpleForm example where the field is tiny and has a 0 (variable) font size and no
     * content.
     *
     * @throws IOException
     */
    @Test
    public void testTinyHorizontalFieldWith0FontSize() throws IOException
    {
        // Create a new document with an empty page.
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.HELV, font);
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);
        acroForm.setDefaultResources(resources);

        String defaultAppearanceString = "/Helv 0 Tf 0 g";
        acroForm.setDefaultAppearance(defaultAppearanceString);
        PDTextField textBox = new PDTextField(acroForm);
        textBox.setPartialName("SampleField");
        textBox.setDefaultAppearance(defaultAppearanceString);
        acroForm.getFields().add(textBox);

        PDAnnotationWidget widget = textBox.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(50, 750, 1, 50);
        widget.setRectangle(rect);
        widget.setPage(page);
        page.getAnnotations().add(widget);

        textBox.setValue(""); // mayhem happened here

        doc.close();
    }
}
