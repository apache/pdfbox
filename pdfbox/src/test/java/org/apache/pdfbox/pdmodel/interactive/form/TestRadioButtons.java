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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 * This will test the functionality of Radio Buttons in PDFBox.
 */
public class TestRadioButtons extends TestCase
{
    
    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestRadioButtons( String name )
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
        return new TestSuite( TestRadioButtons.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestRadioButtons.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test the radio button PDModel.
     *
     * @throws IOException If there is an error creating the field.
     */
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
            assertEquals(optItem.size(),2);
            assertEquals(options.get(0), optItem.getString(0));
            
            // assert that the values can be retrieved correctly
            List<String> retrievedOptions = radioButton.getExportValues();
            assertEquals(retrievedOptions.size(),2);
            assertEquals(retrievedOptions, options);

            // assert that the Opt entry is removed
            radioButton.setExportValues(null);
            assertNull(radioButton.getCOSObject().getItem(COSName.OPT));
            // if there is no Opt entry an empty List shall be returned
            assertEquals(radioButton.getExportValues(), new ArrayList<String>());
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