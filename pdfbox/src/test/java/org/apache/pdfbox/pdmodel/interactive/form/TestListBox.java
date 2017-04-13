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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

/**
 * This will test the functionality of choice fields in PDFBox.
 */
public class TestListBox extends TestCase
{
    
    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestListBox( String name )
    {
        super( name );
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite( TestListBox.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestListBox.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test the radio button PDModel.
     *
     * @throws IOException If there is an error creating the field.
     */
    public void testChoicePDModel() throws IOException
    {

        /*
         * Set up two data list which will be used for the tests
         */

        // export values
        List<String> exportValues = new ArrayList<>();
        exportValues.add("export01");
        exportValues.add("export02");
        exportValues.add("export03");

        // display values, not sorted on purpose as this
        // will be used to test the sort option of the list box
        List<String> displayValues = new ArrayList<>();
        displayValues.add("display02");
        displayValues.add("display01");
        displayValues.add("display03");

        PDDocument doc = null;
        try
        {
            doc = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDAcroForm form = new PDAcroForm( doc );
            
            // Adobe Acrobat uses Helvetica as a default font and 
            // stores that under the name '/Helv' in the resources dictionary
            PDFont font = PDType1Font.HELVETICA;
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), font);
            
            // Add and set the resources and default appearance at the form level
            form.setDefaultResources(resources);
            
            // Acrobat sets the font size on the form level to be
            // auto sized as default. This is done by setting the font size to '0'
            String defaultAppearanceString = "/Helv 0 Tf 0 g";
            form.setDefaultAppearance(defaultAppearanceString);
            
            PDChoice choice = new PDListBox(form);
            
            choice.setDefaultAppearance("/Helv 12 Tf 0g");
            
            
            // Specify the annotation associated with the field
            PDAnnotationWidget widget = choice.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 750, 200, 50);
            widget.setRectangle(rect);
            widget.setPage(page);
            
            // Add the annotation to the page
            page.getAnnotations().add(widget);
            
            
            // test that there are no nulls returned for an empty field
            // only specific methods are tested here
            assertNotNull(choice.getOptions());
            assertNotNull(choice.getValue());
            
            /*
             * Tests for setting the export values
             */

            // setting/getting option values - the dictionaries Opt entry
            choice.setOptions(exportValues);
            assertEquals(exportValues,choice.getOptionsDisplayValues());
            assertEquals(exportValues,choice.getOptionsExportValues());

            // assert that the option values have been correctly set
            COSArray optItem = (COSArray) choice.getCOSObject().getItem(COSName.OPT);
            assertNotNull(choice.getCOSObject().getItem(COSName.OPT));
            assertEquals(optItem.size(),exportValues.size());
            assertEquals(exportValues.get(0), optItem.getString(0));
            
            // assert that the option values can be retrieved correctly
            List<String> retrievedOptions = choice.getOptions();
            assertEquals(retrievedOptions.size(),exportValues.size());
            assertEquals(retrievedOptions, exportValues);

            /*
             * Tests for setting the field values
             */

            // assert that the field value can be set
            choice.setValue("export01");
            assertEquals(choice.getValue().get(0),"export01");
            
            // ensure that the choice field doesn't allow multiple selections
            choice.setMultiSelect(false);
            
            // without multiselect setting multiple items shall fail
            try
            {
                choice.setValue(exportValues);
                fail( "Missing IllegalArgumentException" );
            }
            catch( IllegalArgumentException e )
            {
                assertEquals("The list box does not allow multiple selections.",e.getMessage() );
            }            
            
            // ensure that the choice field does allow multiple selections
            choice.setMultiSelect(true);
            // now this call must suceed
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

            // assert that the Opt entry is removed
            choice.setOptions(null);
            assertNull(choice.getCOSObject().getItem(COSName.OPT));
            // if there is no Opt entry an empty List shall be returned
            assertEquals(choice.getOptions(), Collections.<String>emptyList());
            
            /*
             * Test for setting export and display values
             */
            
            // setting display and export value
            choice.setOptions(exportValues, displayValues);
            assertEquals(displayValues,choice.getOptionsDisplayValues());
            assertEquals(exportValues,choice.getOptionsExportValues());
            
            /*
             * Testing the sort option
             */
            assertEquals(choice.getOptionsDisplayValues().get(0),"display02");
            choice.setSort(true);
            choice.setOptions(exportValues, displayValues);
            assertEquals(choice.getOptionsDisplayValues().get(0),"display01");
            
            /*
             * Setting options with an empty list
             */
            // assert that the Opt entry is removed
            choice.setOptions(null, displayValues);
            assertNull(choice.getCOSObject().getItem(COSName.OPT));
            
            // if there is no Opt entry an empty list shall be returned
            assertEquals(choice.getOptions(), Collections.<String>emptyList());
            assertEquals(choice.getOptionsDisplayValues(), Collections.<String>emptyList());
            assertEquals(choice.getOptionsExportValues(), Collections.<String>emptyList());
            
            // test that an IllegalArgumentException is thrown when export and display 
            // value lists have different sizes
            exportValues.remove(1);
            
            try
            {
                choice.setOptions(exportValues, displayValues);
                fail( "Missing exception" );
            }
            catch( IllegalArgumentException e )
            {
                assertEquals( 
                        "The number of entries for exportValue and displayValue shall be the same.",
                        e.getMessage() );
            }
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }
}