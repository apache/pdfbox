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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This will test the form fields in PDFBox.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class TestFields extends TestCase
{
    //private static Logger log = Logger.getLogger(TestFDF.class);

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
            PDTextbox textBox = new PDTextbox(form);

            //assert that default is false.
            assertFalse( textBox.shouldComb() );

            //try setting and clearing a single field
            textBox.setComb( true );
            assertTrue( textBox.shouldComb() );
            textBox.setComb( false );
            assertFalse( textBox.shouldComb() );

            //try setting and clearing multiple fields
            textBox.setComb( true );
            textBox.setDoNotScroll( true );
            assertTrue( textBox.shouldComb() );
            assertTrue( textBox.doNotScroll() );

            textBox.setComb( false );
            textBox.setDoNotScroll( false );
            assertFalse( textBox.shouldComb() );
            assertFalse( textBox.doNotScroll() );

            //assert that setting a field to false multiple times works
            textBox.setComb( false );
            assertFalse( textBox.shouldComb() );
            textBox.setComb( false );
            assertFalse( textBox.shouldComb() );

            //assert that setting a field to true multiple times works
            textBox.setComb( true );
            assertTrue( textBox.shouldComb() );
            textBox.setComb( true );
            assertTrue( textBox.shouldComb() );





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
