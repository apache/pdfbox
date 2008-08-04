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
package test.pdfbox.pdfparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.cos.COSName;

import org.pdfbox.pdfparser.BaseParser;
import org.pdfbox.pdmodel.PDDocument;


/**
 * This will test the PDF parsing in PDFBox.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class TestPDFParser extends TestCase
{
    //private static Logger log = Logger.getLogger(TestFDF.class);

    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestPDFParser( String name )
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
        return new TestSuite( TestPDFParser.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestPDFParser.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test some cos name parsing.
     *
     * @throws Exception If there is an exception while parsing.
     */
    public void testCOSName() throws Exception
    {
        TestParser parser = new TestParser(new ByteArrayInputStream( "/PANTONE#20116#20CV".getBytes() ) );
        COSName name = parser.parseCOSName();
        assertTrue("Failed to parse COSName",name.getName().equals( "PANTONE 116 CV" ));
        
    }
    
    /**
     * Test some trouble PDFs, these should all parse without an issue.
     * 
     * @throws Exception If there is an error parsing the PDF.
     */
    public void testParsingTroublePDFs() throws Exception
    {
        PDDocument doc = null;
        try
        {
           doc = PDDocument.load( "test/pdfparser/genko_oc_shiryo1.pdf"); 
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
     * A simple class used to test parsing of the cos name.
     */
    private class TestParser extends BaseParser
    {
        /**
         * Constructor.
         * @param input The input stream.
         * @throws IOException If there is an error during parsing.
         */
        public TestParser( InputStream input) throws IOException
        {
            super( input );
        }
        
        /**
         * Expose the parseCOSName as public.
         * 
         * @return The parsed cos name.
         * @throws IOException If there is an error parsing the COSName.
         */
        public COSName parseCOSName() throws IOException
        {
            return super.parseCOSName();
        }
    }
}