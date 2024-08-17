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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This will test the functionality of choice fields in PDFBox.
 */
class TestListBox
{

    private List<String> exportValues;
    private List<String> displayValues;
    private PDDocument doc;
    PDListBox choice;

    @BeforeEach
    void setUp() throws IOException
    {
        // export values
        exportValues = new ArrayList<>();
        exportValues.add("export01");
        exportValues.add("export02");
        exportValues.add("export03");

        // display values, not sorted on purpose as this
        // will be used to test the sort option of the list box
        displayValues = new ArrayList<>();
        displayValues.add("display02");
        displayValues.add("display01");
        displayValues.add("display03");
        
        doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDAcroForm form = new PDAcroForm( doc );
        
        // Adobe Acrobat uses Helvetica as a default font and 
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = new PDType1Font(FontName.HELVETICA);
        PDResources resources = new PDResources();
        resources.put(COSName.HELV, font);
        
        // Add and set the resources and default appearance at the form level
        form.setDefaultResources(resources);
        
        // Acrobat sets the font size on the form level to be
        // auto sized as default. This is done by setting the font size to '0'
        String defaultAppearanceString = "/Helv 0 Tf 0 g";
        form.setDefaultAppearance(defaultAppearanceString);
        
        // the choice field for testing
        choice = new PDListBox(form);
        
        choice.setDefaultAppearance("/Helv 12 Tf 0g");
        
        
        // Specify the annotation associated with the field
        PDAnnotationWidget widget = choice.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(50, 750, 200, 50);
        widget.setRectangle(rect);
        widget.setPage(page);
        
        // Add the annotation to the page
        page.getAnnotations().add(widget);
    }

    @Test
    void testNoNullsReturned()
    {
        // test that there are no nulls returned for an empty field
        // only specific methods are tested here
        assertNotNull(choice.getOptions());
        assertNotNull(choice.getValue());
    }

    /*
     * Tests for setting the export values
     */
    @Test
    void testExportValuesGetterSetter() throws IOException
    {
        // setting/getting option values - the dictionaries Opt entry
        choice.setOptions(exportValues);
        assertEquals(exportValues,choice.getOptionsDisplayValues());
        assertEquals(exportValues,choice.getOptionsExportValues());

        // Test bug 1 of PDFBOX-4252 when top index is not null
        choice.setTopIndex(1);
        choice.setValue(exportValues.get(2));
        assertEquals(exportValues.get(2), choice.getValue().get(0));
        choice.setTopIndex(null); // reset

        // assert that the option values have been correctly set
        COSArray optItem = (COSArray) choice.getCOSObject().getItem(COSName.OPT);
        assertNotNull(choice.getCOSObject().getItem(COSName.OPT));
        assertEquals(optItem.size(),exportValues.size());
        assertEquals(exportValues.get(0), optItem.getString(0));
            
        // assert that the option values can be retrieved correctly
        List<String> retrievedOptions = choice.getOptions();
        assertEquals(retrievedOptions.size(),exportValues.size());
        assertEquals(retrievedOptions, exportValues);
        // assert that the field value can be set
    }

    /*
     * Test for setting the field value
     */
    @Test
    void testFieldValueSetterGetter() throws IOException
    {
        // add test data
        choice.setOptions(exportValues);
        choice.setMultiSelect(true);
        choice.setValue(exportValues);

        // assert that the option values have been correctly set
        COSArray valueItems = (COSArray) choice.getCOSObject().getItem(COSName.V);
        assertNotNull(valueItems);
        assertEquals(valueItems.size(),exportValues.size());
        assertEquals(exportValues.get(0), valueItems.getString(0));
            
        // assert that the index values have been correctly set
        COSArray indexItems = (COSArray) choice.getCOSObject().getItem(COSName.I);
        assertNotNull(indexItems);
        assertEquals(indexItems.size(),exportValues.size());
            
        // setting a single value shall remove the indices
        choice.setValue("export01");
        indexItems = (COSArray) choice.getCOSObject().getItem(COSName.I);
        assertNull(indexItems);        
    }

    @Test
    void testMultiselect() throws IOException
    {
        // add test data
        choice.setOptions(exportValues);

        // ensure that the choice field doesn't allow multiple selections
        choice.setMultiSelect(false);

        // without multiselect setting multiple items shall fail
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            choice.setValue(exportValues);
        });

        assertEquals("The list box does not allow multiple selections.", exception.getMessage());      
            
        // ensure that the choice field does allow multiple selections
        choice.setMultiSelect(true);
        // now this call must succeed
        choice.setValue(exportValues);
    }

    @Test
    void testOptIsRemovedForNull()
    {
        // add test data
        choice.setOptions(exportValues);
        assertNotNull(choice.getCOSObject().getItem(COSName.OPT));
        // assert that the Opt entry is removed
        choice.setOptions(null);
        assertNull(choice.getCOSObject().getItem(COSName.OPT));
        // if there is no Opt entry an empty List shall be returned
        assertEquals(Collections.emptyList(), choice.getOptions());
    }

    @Test
    void testSetExportAndDisplay()
    {
        // setting display and export value
        choice.setOptions(exportValues, displayValues);
        assertEquals(displayValues,choice.getOptionsDisplayValues());
        assertEquals(exportValues,choice.getOptionsExportValues());
    }

    @Test
    void testSortOption()
    {
        // add test data
        choice.setOptions(exportValues, displayValues);
        assertEquals("display02", choice.getOptionsDisplayValues().get(0));

        // test the sort option
        choice.setSort(true);
        choice.setOptions(exportValues, displayValues);
        assertEquals("display01", choice.getOptionsDisplayValues().get(0));
        assertEquals("display02", choice.getOptionsDisplayValues().get(1));
        assertEquals("display03", choice.getOptionsDisplayValues().get(2));
    }

    @Test
    void testEmptyOptionsNotNull()
    {
        // assert that the Opt entry is removed
        choice.setOptions(null, displayValues);
        assertNull(choice.getCOSObject().getItem(COSName.OPT));
            
        // if there is no Opt entry an empty list shall be returned
        assertEquals(Collections.emptyList(), choice.getOptions());
        assertEquals(Collections.emptyList(), choice.getOptionsDisplayValues());
        assertEquals(Collections.emptyList(), choice.getOptionsExportValues());
    }

    @Test
    void testExceptionForDifferentNumberOfEntries()
    {
        // test that an IllegalArgumentException is thrown when export and display 
        // value lists have different sizes
        exportValues.remove(1);

        // without multiselect setting multiple items shall fail
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            choice.setOptions(exportValues, displayValues);
        });
        
        assertEquals("The number of entries for exportValue and displayValue shall be the same.", exception.getMessage());  
    }

    @AfterEach
    void tearDown()
    {
        IOUtils.closeQuietly(doc);
    }
}