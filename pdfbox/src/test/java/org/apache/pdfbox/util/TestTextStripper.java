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
package org.apache.pdfbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;


/**
 * Test suite for PDFTextStripper.
 *
 * FILE SET VALIDATION
 *
 * This test suite is designed to test PDFTextStripper using a set of PDF
 * files and known good output for each.  The default mode of testAll()
 * is to process each *.pdf file in "src/test/resources/input".  An output
 * file is created in "target/test-output" with the same name as the PDF file,
 * plus an additional ".txt" suffix.  
 *
 * The output file is then tested against a known good result file from
 * the input directory (again, with the same name as the tested PDF file,
 * but with the additional ".txt" suffix).  The process is performed both
 * with and without sorting enabled.  The sorted files have a "-sorted.txt" 
 * suffix. 
 *
 * So for the file "src/test/resources/input/hello.pdf", an output file will
 * be generated named "target/test-output/hello.pdf.txt".  Then that file
 * will be compared to the known good file
 * "src/test/resources/input/hello.pdf.txt", if it exists.
 * 
 * To support testing with files that are not officially distributed 
 * with PDFBox, this test will also look in the "target/test-input-ext"
 * directory.
 *
 * Any errors are logged, and at the end of processing all *.pdf files, if
 * there were any errors, the test fails.  The logging is at INFO, as the
 * general goal is overall validation, and on failure, the indication of
 * which file or files failed.
 *
 * When processing new PDF files, you may use testAll() to generate output,
 * verify the output manually, then move the output file to the test input
 * directory to use as the basis for future validations.
 *
 * SINGLE FILE VALIDATION
 *
 * To further research individual failures, the org.apache.pdfbox.util.TextStripper.file
 * system property may be set with the name of a single file in the "test/input"
 * directory.  In this mode, testAll() will evaluate only that file, and will
 * do so with DEBUG level logging.  You can set this property from ant by
 * defining "file", as in:
 *
 *    ant testextract -Dfile=hello.pdf
 *
 * @author Robert Dickinson (bob@brutesquadlabs.com)
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.19 $
 */
public class TestTextStripper extends TestCase
{

    /**
     * Logger instance.
     */
    private static final Log log = LogFactory.getLog(TestTextStripper.class);

    private boolean bFail = false;
    private PDFTextStripper stripper = null;
    private final String encoding = "UTF-16LE";

    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     *
     * @throws IOException If there is an error creating the test.
     */
    public TestTextStripper( String name ) throws IOException
    {
        super( name );
        stripper = new PDFTextStripper(encoding);
        stripper.setLineSeparator("\n");
    }

    /**
     * Test suite setup.
     */
    public void setUp()
    {
        // If you want to test a single file using DEBUG logging, from an IDE,
        // you can do something like this:
        //
        // System.setProperty("org.apache.pdfbox.util.TextStripper.file", "FVS318Ref.pdf");
    }

    /**
     * Determine whether two strings are equal, where two null strings are
     * considered equal.
     *
     * @param expected Expected string
     * @param actual Actual String
     * @return <code>true</code> is the strings are both null,
     * or if their contents are the same, otherwise <code>false</code>.
     */
    private boolean stringsEqual(String expected, String actual)
    {
        boolean equals = true;
        if( (expected == null) && (actual == null) )
        {
            return true;
        }
        else if( expected != null && actual != null )
        {
            expected = expected.trim();
            actual = actual.trim();
            char[] expectedArray = expected.toCharArray();
            char[] actualArray = actual.toCharArray();
            int expectedIndex = 0;
            int actualIndex = 0;
            while( expectedIndex<expectedArray.length && actualIndex<actualArray.length )
            {
                if( expectedArray[expectedIndex] != actualArray[actualIndex] )
                {
                    equals = false;
                    log.warn("Lines differ at index"
                     + " expected:" + expectedIndex + "-" + (int)expectedArray[expectedIndex]
                     + " actual:" + actualIndex + "-" + (int)actualArray[actualIndex] );
                    break;
                }
                expectedIndex = skipWhitespace( expectedArray, expectedIndex );
                actualIndex = skipWhitespace( actualArray, actualIndex );
                expectedIndex++;
                actualIndex++;
            }
            if( equals )
            {
                if( expectedIndex != expectedArray.length )
                {
                    equals = false;
                    log.warn("Expected line is longer at:" + expectedIndex );
                }
                if( actualIndex != actualArray.length )
                {
                    equals = false;
                    log.warn("Actual line is longer at:" + actualIndex );
                }
            }
        }
        else if( ( expected == null && actual != null && actual.trim().equals( "" ) ) ||
            ( actual == null && expected != null && expected.trim().equals( "" ) ) )
        {
            //basically there are some cases where pdfbox will put an extra line
            //at the end of the file, who cares, this is not enough to report
            // a failure
            equals = true;
        }
        else
        {
            equals = false;
        }
        return equals;
    }

    /**
     * If the current index is whitespace then skip any subsequent whitespace.
     */
    private int skipWhitespace( char[] array, int index )
    {
        //if we are at a space character then skip all space
        //characters, but when all done rollback 1 because stringsEqual
        //will roll forward 1
        if( array[index] == ' ' || array[index] > 256 )
        {
            while( index < array.length && (array[index] == ' ' || array[index] > 256))
            {
                index++;
            }
            index--;
        }
        return index;
    }

    /**
     * Validate text extraction on a single file.
     *
     * @param inFile The PDF file to validate
     * @param outDir The directory to store the output in
     * @param bLogResult Whether to log the extracted text
     * @param bSort Whether or not the extracted text is sorted
     * @throws Exception when there is an exception
     */
    public void doTestFile(File inFile, File outDir, boolean bLogResult, boolean bSort)
    throws Exception
    {
        if(bSort)
        {
            log.info("Preparing to parse " + inFile.getName() + " for sorted test");
        }
        else
        {
            log.info("Preparing to parse " + inFile.getName() + " for standard test");
        }

        if (!outDir.exists()) 
        {
            if (!outDir.mkdirs()) 
            {
                throw (new Exception("Error creating " + outDir.getAbsolutePath() + " directory"));
            }
        }

        PDDocument document = PDDocument.load(inFile);
        try
        {
            
            File outFile = null;
            File expectedFile = null;

            if(bSort)
            {
                outFile = new File(outDir,  inFile.getName() + "-sorted.txt");
                expectedFile = new File(inFile.getParentFile(), inFile.getName() + "-sorted.txt");
            }
            else
            {
                outFile = new File(outDir, inFile.getName() + ".txt");
                expectedFile = new File(inFile.getParentFile(), inFile.getName() + ".txt");
            }

            OutputStream os = new FileOutputStream(outFile);
            try {
                os.write( 0xFF );
                os.write( 0xFE );

                Writer writer = new OutputStreamWriter(os, encoding);
                try {
                    //Allows for sorted tests 
                    stripper.setSortByPosition(bSort);
                    stripper.writeText(document, writer);
                } finally {
                    // close the written file before reading it again
                    writer.close();
                }
            } finally {
                os.close();
            }

            if (bLogResult)
            {
                log.info("Text for " + inFile.getName() + ":");
                log.info(stripper.getText(document));
            }

            if (!expectedFile.exists())
            {
                this.bFail = true;
                log.error(
                        "FAILURE: Input verification file: " + expectedFile.getAbsolutePath() +
                " did not exist");
                return;
            }

            LineNumberReader expectedReader =
                new LineNumberReader(new InputStreamReader(new FileInputStream(expectedFile), encoding));
            LineNumberReader actualReader =
                new LineNumberReader(new InputStreamReader(new FileInputStream(outFile), encoding));

            while (true)
            {
                String expectedLine = expectedReader.readLine();
                while( expectedLine != null && expectedLine.trim().length() == 0 )
                {
                    expectedLine = expectedReader.readLine();
                }
                String actualLine = actualReader.readLine();
                while( actualLine != null && actualLine.trim().length() == 0 )
                {
                    actualLine = actualReader.readLine();
                }
                if (!stringsEqual(expectedLine, actualLine))
                {
                    this.bFail = true;
                    log.error("FAILURE: Line mismatch for file " + inFile.getName() +
                            " ( sort = "+bSort+")" +
                            " at expected line: " + expectedReader.getLineNumber() +
                            " at actual line: " + actualReader.getLineNumber());
                    log.error("  expected line was: \"" + expectedLine + "\"");
                    log.error("  actual line was:   \"" + actualLine + "\"" + "\n");

                    //lets report all lines, even though this might produce some verbose logging
                    //break;
                }

                if( expectedLine == null || actualLine==null)
                {
                    break;
                }
            }
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Process each file in the specified directory.
     * @param inDir Input directory search for PDF files in.
     * @param outDir Output directory where the temp files will be created.
     */
    private void doTestDir(File inDir, File outDir) throws Exception 
    {
        File[] testFiles = inDir.listFiles(new FilenameFilter() 
        {
            public boolean accept(File dir, String name) 
            {
                return (name.endsWith(".pdf"));
            }
        });

        for (int n = 0; n < testFiles.length; n++) 
        {
            //Test without sorting
            doTestFile(testFiles[n], outDir, false, false);
            //Test with sorting
            doTestFile(testFiles[n], outDir, false, true);
        }
    }
    
    /**
     * Test to validate text extraction of file set.
     *
     * @throws Exception when there is an exception
     */
    public void testExtract()
    throws Exception
    {
        String filename = System.getProperty("org.apache.pdfbox.util.TextStripper.file");
        File inDir = new File("src/test/resources/input");
        File outDir = new File("target/test-output");
        File inDirExt = new File("target/test-input-ext");
        File outDirExt = new File("target/test-output-ext");

            if ((filename == null) || (filename.length() == 0)) 
            {
                doTestDir(inDir, outDir);
                if (inDirExt.exists())
                {
                    doTestDir(inDirExt, outDirExt);
                }
            }
            else 
            {
                //Test without sorting
                doTestFile(new File(inDir, filename), outDir, true, false);
                //Test with sorting
                doTestFile(new File(inDir, filename), outDir, true, true);
            }

            if (this.bFail)
            {
                fail("One or more failures, see test log for details");
            }
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite( TestTextStripper.class );
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestTextStripper.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
