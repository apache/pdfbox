/**
 * Copyright (c) 2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package test.pdfbox.pdmodel.interactive.form;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.pdfbox.pdmodel.interactive.form.PDTextbox;

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