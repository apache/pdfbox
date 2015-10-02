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

package org.apache.pdfbox.text;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the PDButton class.
 *
 */
public class BidiTest
{
    /**
     * Logger instance.
     */
    private static final Log log = LogFactory.getLog(TestTextStripper.class);
    
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/text/");
    private static final File outDir = new File("target/test-output");
    private static final String NAME_OF_PDF = "BidiSample.pdf";
    
    private static final String ENCODING = "UTF-8";

    private PDDocument document;
    private PDFTextStripper stripper;
    
    private boolean bFail = false;

    @Before
    public void setUp() throws IOException
    {
        if (!outDir.exists()) 
        {
            if (!outDir.mkdirs()) 
            {
                throw (new IOException("Error creating " + outDir.getAbsolutePath() + " directory"));
            }
        }
        
        document = PDDocument.load(new File(IN_DIR, NAME_OF_PDF));
        stripper = new PDFTextStripper();
        stripper.setLineSeparator("\n");
    }

    @Test
    public void testSorted() throws IOException
    {
        File testFile = new File(IN_DIR, NAME_OF_PDF);
        doTestFile(testFile, outDir, false, true);
    }

    @Test
    public void testNotSorted() throws IOException
    {
        File testFile = new File(IN_DIR, NAME_OF_PDF);
        doTestFile(testFile, outDir, false, false);
    }

    @After
    public void tearDown() throws IOException
    {
        document.close();
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
    throws IOException
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
                throw (new IOException("Error creating " + outDir.getAbsolutePath() + " directory"));
            }
        }

        PDDocument document = PDDocument.load(inFile);
        try
        {            
            File outFile;
            File expectedFile;

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
            try
            {
                Writer writer = new OutputStreamWriter(os, ENCODING);
                try
                {
                    //Allows for sorted tests 
                    stripper.setSortByPosition(bSort);
                    stripper.writeText(document, writer);
                }
                finally
                {
                    // close the written file before reading it again
                    writer.close();
                }
            }
            finally
            {
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
                fail("FAILURE: Input verification file: " + expectedFile.getAbsolutePath() +
                        " did not exist");
                return;
            }

            LineNumberReader expectedReader =
                new LineNumberReader(new InputStreamReader(new FileInputStream(expectedFile), ENCODING));
            LineNumberReader actualReader =
                new LineNumberReader(new InputStreamReader(new FileInputStream(outFile), ENCODING));

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
                    fail("FAILURE: Line mismatch for file " + inFile.getName() +
                            " (sort = "+bSort+")" +
                            " at expected line: " + expectedReader.getLineNumber() +
                            " at actual line: " + actualReader.getLineNumber() +
                            "\nexpected line was: \"" + expectedLine + "\"" +
                            "\nactual line was:   \"" + actualLine + "\"" + "\n");

                    //lets report all lines, even though this might produce some verbose logging
                    //break;
                }

                if( expectedLine == null || actualLine==null)
                {
                    break;
                }
            }
            expectedReader.close();
            actualReader.close();
        }
        finally
        {
            document.close();
        }
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
        else
        {
            equals = (expected == null && actual != null && actual.trim().isEmpty())
                    || (actual == null && expected != null && expected.trim().isEmpty());
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

}
