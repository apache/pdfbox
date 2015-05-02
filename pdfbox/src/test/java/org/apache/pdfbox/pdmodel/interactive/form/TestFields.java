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

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This will test the form fields in PDFBox.
 *
 * @author Ben Litchfield
 */
public class TestFields extends TestCase
{
    //private static Logger log = Logger.getLogger(TestFDF.class);

    private static final String PATH_OF_PDF = "src/test/resources/org/apache/pdfbox/pdmodel/interactive/form/AcroFormsBasicFields.pdf";

    
    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestFields( String name )
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
        return new TestSuite( TestFields.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestFields.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test setting field flags on the PDField.
     *
     * @throws IOException If there is an error creating the field.
     */
    public void testFlags() throws IOException
    {
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();
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
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }
    
    /**
     * This will test some form fields functionality based with 
     * a sample form.
     *
     * @throws IOException If there is an error creating the field.
     */
    public void testAcroFormsBasicFields() throws IOException
    {
        PDDocument doc = null;
        
        try
        {
            doc = PDDocument.load(new File(PATH_OF_PDF));
            
            // get and assert that there is a form
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            assertNotNull(form);
            
            // assert that there is no value, set the field value and
            // ensure it has been set 
            PDFieldTreeNode field = form.getField("TextField");
            assertNull(field.getCOSObject().getItem(COSName.V));
            field.setValue("field value");
            assertNotNull(field.getCOSObject().getItem(COSName.V));
            assertEquals(field.getValue(),"field value");
            
            // assert when setting to null the key has also been removed
            assertNotNull(field.getCOSObject().getItem(COSName.V));
            field.setValue(null);
            assertNull(field.getCOSObject().getItem(COSName.V));
            
            // get the RadioButton with a DV entry
            field = form.getField("RadioButtonGroup-DefaultValue");
            assertNotNull(field);
            assertEquals(field.getDefaultValue(),"RadioButton01");
            assertEquals(COSName.getPDFName((String)field.getDefaultValue()),
                    field.getCOSObject().getDictionaryObject(COSName.DV));

            // get the Checkbox with a DV entry
            field = form.getField("Checkbox-DefaultValue");
            assertNotNull(field);
            assertEquals(field.getDefaultValue(),"Yes");
            assertEquals(field.getDefaultValue(),
                    ((COSName) field.getCOSObject().getDictionaryObject(COSName.DV)).getName());
            
            // get the TextField with a DV entry
            field = form.getField("TextField-DefaultValue");
            assertNotNull(field);
            assertEquals(field.getDefaultValue(),"DefaultValue");
            assertEquals(field.getDefaultValue(),
                    ((COSString)field.getCOSObject().getDictionaryObject(COSName.DV)).getString());
            assertEquals(((PDVariableText)field).getDefaultAppearance(),"/Helv 12 Tf 0 g");

            // get a rich text field with a  DV entry
            field = form.getField("RichTextField-DefaultValue");
            assertNotNull(field);
            assertEquals(field.getDefaultValue(),"DefaultValue");
            assertEquals(field.getDefaultValue(),
                    ((COSString)field.getCOSObject().getDictionaryObject(COSName.DV)).getString());
            assertEquals(field.getValue(), "DefaultValue");
            assertEquals(((PDVariableText)field).getDefaultAppearance(), "/Helv 12 Tf 0 g");
            assertEquals(((PDVariableText)field).getDefaultStyleString(),
                    "font: Helvetica,sans-serif 12.0pt; text-align:left; color:#000000 ");
            // do not test for the full content as this is a rather long xml string
            assertEquals(((PDVariableText)field).getRichTextValue().length(),338);
            
            // get a rich text field with a text stream for the value
            field = form.getField("LongRichTextField");
            assertNotNull(field);
            assertEquals(field.getCOSObject().getDictionaryObject(
                    COSName.V).getClass().getName(),
                    "org.apache.pdfbox.cos.COSStream");
            assertEquals(((PDTextField)field).getValue().length(),145396);
            
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