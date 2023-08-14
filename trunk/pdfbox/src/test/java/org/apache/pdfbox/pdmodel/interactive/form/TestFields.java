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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.jupiter.api.Test;

/**
 * This will test the form fields in PDFBox.
 *
 * @author Ben Litchfield
 */
class TestFields
{
    //private static Logger log = Logger.getLogger(TestFDF.class);

    private static final String PATH_OF_PDF = "src/test/resources/org/apache/pdfbox/pdmodel/interactive/form/AcroFormsBasicFields.pdf";

    
    /**
     * This will test setting field flags on the PDField.
     *
     * @throws IOException If there is an error creating the field.
     */
    @Test
    void testFlags() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDAcroForm form = new PDAcroForm( doc );
            PDTextField textBox = new PDTextField(form);

            //assert that default is false.
            assertFalse( textBox.isComb() );

            //try setting and clearing a single field
            textBox.setComb( true );
            assertTrue( textBox.isComb() );
            textBox.setComb( false );
            assertFalse( textBox.isComb() );

            //try setting and clearing multiple fields
            textBox.setComb( true );
            textBox.setDoNotScroll( true );
            assertTrue( textBox.isComb() );
            assertTrue( textBox.doNotScroll() );

            textBox.setComb( false );
            textBox.setDoNotScroll( false );
            assertFalse( textBox.isComb() );
            assertFalse( textBox.doNotScroll() );

            //assert that setting a field to false multiple times works
            textBox.setComb( false );
            assertFalse( textBox.isComb() );
            textBox.setComb( false );
            assertFalse( textBox.isComb() );

            //assert that setting a field to true multiple times works
            textBox.setComb( true );
            assertTrue( textBox.isComb() );
            textBox.setComb( true );
            assertTrue( textBox.isComb() );
        }
    }
    
    /**
     * This will test some form fields functionality based with 
     * a sample form.
     *
     * @throws IOException If there is an error creating the field.
     */
    @Test
    void testAcroFormsBasicFields() throws IOException
    {       
        try (PDDocument doc = Loader.loadPDF(new File(PATH_OF_PDF)))
        {            
            // get and assert that there is a form
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            assertNotNull(form);
            
            // assert that there is no value, set the field value and
            // ensure it has been set 
            PDTextField textField = (PDTextField)form.getField("TextField");
            assertNull(textField.getCOSObject().getItem(COSName.V));
            textField.setValue("field value");
            assertNotNull(textField.getCOSObject().getItem(COSName.V));
            assertEquals("field value", textField.getValue());
            
            // assert when setting to null the key has also been removed
            assertNotNull(textField.getCOSObject().getItem(COSName.V));
            textField.setValue(null);
            assertNull(textField.getCOSObject().getItem(COSName.V));
            
            // get the TextField with a DV entry
            textField = (PDTextField)form.getField("TextField-DefaultValue");
            assertNotNull(textField);
            assertEquals("DefaultValue", textField.getDefaultValue());
            assertEquals(textField.getDefaultValue(),
                    ((COSString)textField.getCOSObject().getDictionaryObject(COSName.DV)).getString());
            assertEquals("/Helv 12 Tf 0 g", textField.getDefaultAppearance());

            // get a rich text field with a  DV entry
            textField = (PDTextField)form.getField("RichTextField-DefaultValue");
            assertNotNull(textField);
            assertEquals("DefaultValue", textField.getDefaultValue());
            assertEquals(textField.getDefaultValue(),
                    ((COSString)textField.getCOSObject().getDictionaryObject(COSName.DV)).getString());
            assertEquals("DefaultValue", textField.getValue());
            assertEquals("/Helv 12 Tf 0 g", textField.getDefaultAppearance());
            assertEquals("font: Helvetica,sans-serif 12.0pt; text-align:left; color:#000000 ",
                    textField.getDefaultStyleString());
            // do not test for the full content as this is a rather long xml string
            assertEquals(338, textField.getRichTextValue().length());
            
            // get a rich text field with a text stream for the value
            textField = (PDTextField)form.getField("LongRichTextField");
            assertNotNull(textField);
            assertEquals("org.apache.pdfbox.cos.COSStream",
                    textField.getCOSObject().getDictionaryObject(COSName.V).getClass().getName());
            assertEquals(145396, textField.getValue().length());
            
        }
    }
    
    
    /**
     * This will test the handling of a widget with a missing (required) /Rect entry.
     *
     * @throws IOException If there is an error loading the form or the field.
     */
    @Test
    void testWidgetMissingRect() throws IOException
    {        
        try (PDDocument doc = Loader.loadPDF(new File(PATH_OF_PDF)))
        {            
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            
            PDTextField textField = (PDTextField)form.getField("TextField-DefaultValue");
            PDAnnotationWidget widget = textField.getWidgets().get(0);

            // initially there is an Appearance Entry in the form
            assertNotNull(widget.getCOSObject().getDictionaryObject(COSName.AP));
            widget.getCOSObject().removeItem(COSName.RECT);
            textField.setValue("field value");
            
            // There shall be no appearance entry if there is no /Rect to
            // behave as Adobe Acrobat does
            assertNull(widget.getCOSObject().getDictionaryObject(COSName.AP));
             
        }
    }
}
