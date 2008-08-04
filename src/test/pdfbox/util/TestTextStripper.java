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
package test.pdfbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.pdmodel.PDDocument;

import org.pdfbox.util.PDFTextStripper;

/**
 * Test suite for PDFTextStripper.
 *
 * FILE SET VALIDATION
 *
 * This test suite is designed to test PDFTextStripper using a set of PDF
 * files and known good output for each.  The default mode of testAll()
 * is to process each *.pdf file in "test/input".  An output file is
 * created in "test/output" with the same name as the PDF file, plus an
 * additional ".txt" suffix.
 *
 * The output file is then tested against a known good result file from
 * the input directory (again, with the same name as the tested PDF file,
 * but with the additional ".txt" suffix).
 *
 * So for the file "test/input/hello.pdf", an output file will be generated
 * named "test/output/hello.pdf.txt".  Then that file will be compared to
 * the known good file "test/input/hello.pdf.txt", if it exists.
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
 * To further research individual failures, the test.pdfbox.util.TextStripper.file
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
    private boolean bFail = false;
    private PDFTextStripper stripper = null;
    private PrintWriter log = null;

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
        stripper = new PDFTextStripper();
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
        // System.setProperty("test.pdfbox.util.TextStripper.file", "FVS318Ref.pdf");
    }

    /**
     * Determine whether two strings are equal, where two null strings are
     * considered equal.
     *
     * @param expected Excpected string
     * @param actual Actual String
     * @return <code>true</code> is the strings are both null,
     * or if their contents are the same, otherwise <code>false</code>.
     */
    private boolean stringsEqual(PrintWriter log, String expected, String actual)
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
                    log.println("Lines differ at index"
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
                    log.println("Expected line is longer at:" + expectedIndex );
                }
                if( actualIndex != actualArray.length )
                {
                    equals = false;
                    log.println("Actual line is longer at:" + actualIndex );
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
     * @param file The file to validate
     * @param bLogResult Whether to log the extracted text
     * @throws Exception when there is an exception
     */
    public void doTestFile(File file, boolean bLogResult)
        throws Exception
    {
        
        log.println("Preparing to parse " + file.getName());
        
        OutputStream os = null;
        Writer writer = null;
        PDDocument document = null;
        try
        {
            
            document = PDDocument.load(file);
            File outFile = new File(file.getParentFile().getParentFile(), "output/" + file.getName() + ".txt");
            os = new FileOutputStream(outFile);
            os.write( 0xFF );
            os.write( 0xFE );
            writer = new OutputStreamWriter(os,"UTF-16LE");

            stripper.writeText(document, writer);



            if (bLogResult)
            {
                log.println("Text for " + file.getName() + ":\r\n" + stripper.getText(document));
            }

            File expectedFile = new File(file.getParentFile().getParentFile(), "input/" + file.getName() + ".txt");
            File actualFile = new File(file.getParentFile().getParentFile(), "output/" + file.getName() + ".txt");

            if (!expectedFile.exists())
            {
                this.bFail = true;
                log.println(
                    "FAILURE: Input verification file: " + expectedFile.getAbsolutePath() + 
                    " did not exist");
                return;
            }

            LineNumberReader expectedReader =
                new LineNumberReader(new InputStreamReader(new FileInputStream(expectedFile),"UTF-16LE"));
            LineNumberReader actualReader =
                new LineNumberReader(new InputStreamReader(new FileInputStream(actualFile), "UTF-16LE"));

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
                if (!stringsEqual(log, expectedLine, actualLine))
                {
                    this.bFail = true;
                    log.println("FAILURE: Line mismatch for file " + file.getName() +
                              " at expected line: " + expectedReader.getLineNumber() +
                              " at actual line: " + actualReader.getLineNumber() +
                              "\r\n  expected line was: \"" + expectedLine + "\"" +
                              "\r\n  actual line was:   \"" + actualLine + "\"");
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
            if( writer != null )
            {
                writer.close();
            }
            if( os != null )
            {
                os.close();
            }
            if( document != null )
            {
                document.close();
            }
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
        String filename = System.getProperty("test.pdfbox.util.TextStripper.file");
        File testDir = new File("test/input");
        try
        {
            log = new PrintWriter( new FileWriter( "textextract.log" ) );
    
            if ((filename == null) || (filename.length() == 0))
            {
                File[] testFiles = testDir.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return (name.endsWith(".pdf"));
                    }
                });
    
                for (int n = 0; n < testFiles.length; n++)
                {
                    doTestFile(testFiles[n], false);
                }
            }
            else
            {
                doTestFile(new File(testDir, filename), true);
            }
    
            if (this.bFail)
            {
                fail("One or more failures, see test log for details");
            }
        }
        finally
        {
            if( log != null )
            {
                log.close();
            }
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