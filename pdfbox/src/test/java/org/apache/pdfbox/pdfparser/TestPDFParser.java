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
package org.apache.pdfbox.pdfparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;


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
           doc = PDDocument.load( "src/test/resources/pdfparser/genko_oc_shiryo1.pdf");
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
            super( input, false );
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
