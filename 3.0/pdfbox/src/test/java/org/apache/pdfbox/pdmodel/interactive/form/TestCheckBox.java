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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.junit.jupiter.api.Test;

/**
 * This will test the functionality of checkboxes in PDFBox.
 */
class TestCheckBox
{
    
    /**
     * This will test the checkbox PDModel.
     *
     * @throws IOException If there is an error creating the field.
     */
    @Test
    void testCheckboxPDModel() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDAcroForm form = new PDAcroForm( doc );
            PDCheckBox checkBox = new PDCheckBox(form);
            
            // test that there are no nulls returned for an empty field
            // only specific methods are tested here
            assertNotNull(checkBox.getExportValues());
            assertNotNull(checkBox.getValue());
            
            // Test setting/getting option values - the dictionaries Opt entry
            List<String> options = new ArrayList<>();
            options.add("Value01");
            options.add("Value02");
            checkBox.setExportValues(options);

            COSArray optItem = (COSArray) checkBox.getCOSObject().getItem(COSName.OPT);

            // assert that the values have been correctly set
            assertNotNull(checkBox.getCOSObject().getItem(COSName.OPT));
            assertEquals(2, optItem.size());
            assertEquals(options.get(0), optItem.getString(0));
            
            // assert that the values can be retrieved correctly
            List<String> retrievedOptions = checkBox.getExportValues();
            assertEquals(2, retrievedOptions.size());
            assertEquals(retrievedOptions, options);

            // assert that the Opt entry is removed
            checkBox.setExportValues(null);
            assertNull(checkBox.getCOSObject().getItem(COSName.OPT));
            // if there is no Opt entry an empty List shall be returned
            assertTrue(checkBox.getExportValues().isEmpty());
        }
    }

    /**
     * PDFBOX-4366: Create and test a checkbox with no /AP. The created file works with Adobe Reader!
     * 
     * @throws IOException 
     */
    @Test
    void testCheckBoxNoAppearance() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDAcroForm acroForm = new PDAcroForm(doc);
            acroForm.setNeedAppearances(true); // need this or it won't appear on Adobe Reader
            doc.getDocumentCatalog().setAcroForm(acroForm);
            List<PDField> fields = new ArrayList<>();
            PDCheckBox checkBox = new PDCheckBox(acroForm);
            checkBox.setPartialName("checkbox");
            PDAnnotationWidget widget = checkBox.getWidgets().get(0);
            widget.setRectangle(new PDRectangle(50, 600, 100, 100));
            PDBorderStyleDictionary bs = new PDBorderStyleDictionary();
            bs.setStyle(PDBorderStyleDictionary.STYLE_SOLID);
            bs.setWidth(1);
            COSDictionary acd = new COSDictionary();
            PDAppearanceCharacteristicsDictionary ac = new PDAppearanceCharacteristicsDictionary(acd);
            ac.setBackground(new PDColor(new float[] { 1, 1, 0 }, PDDeviceRGB.INSTANCE));
            ac.setBorderColour(new PDColor(new float[] { 1, 0, 0 }, PDDeviceRGB.INSTANCE));
            ac.setNormalCaption("4"); // 4 is checkmark, 8 is cross
            widget.setAppearanceCharacteristics(ac);
            widget.setBorderStyle(bs);
            checkBox.setValue("Off");
            fields.add(checkBox);
            page.getAnnotations().add(widget);
            acroForm.setFields(fields);
            assertEquals("Off", checkBox.getValue());
        }
    }
}