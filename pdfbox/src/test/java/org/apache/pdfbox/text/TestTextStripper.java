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

import difflib.ChangeDelta;
import difflib.DeleteDelta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.TestPDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;


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
 * do so with DEBUG level logging.
 *
 * @author Robert Dickinson
 * @author Ben Litchfield
 */
public class TestTextStripper extends TestCase
{

    /**
     * Logger instance.
     */
    private static final Log log = LogFactory.getLog(TestTextStripper.class);

    private boolean bFail = false;
    private PDFTextStripper stripper = null;
    private static final String ENCODING = "UTF-8";

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
    @Override
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

        //System.out.println("  " + inFile + (bSort ? " (sorted)" : ""));
        try (PDDocument document = PDDocument.load(inFile))
        {
            File outFile;
            File diffFile;
            File expectedFile;

            if(bSort)
            {
                outFile = new File(outDir,  inFile.getName() + "-sorted.txt");
                diffFile = new File(outDir, inFile.getName() + "-sorted-diff.txt");
                expectedFile = new File(inFile.getParentFile(), inFile.getName() + "-sorted.txt");
            }
            else
            {
                outFile = new File(outDir, inFile.getName() + ".txt");
                diffFile = new File(outDir, inFile.getName() + "-diff.txt");
                expectedFile = new File(inFile.getParentFile(), inFile.getName() + ".txt");
            }
            
            // delete possible leftover
            diffFile.delete();

            try (OutputStream os = new FileOutputStream(outFile))
            {
                os.write (0xEF);
                os.write (0xBB);
                os.write (0xBF);

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(os, ENCODING)))
                {
                    //Allows for sorted tests 
                    stripper.setSortByPosition(bSort);
                    stripper.writeText(document, writer);
                    // close the written file before reading it again
                }
            }

            if (bLogResult)
            {
                log.info("Text for " + inFile.getName() + ":");
                log.info(stripper.getText(document));
            }

            if (!expectedFile.exists())
            {
                this.bFail = true;
                log.error("FAILURE: Input verification file: " + expectedFile.getAbsolutePath() +
                        " did not exist");
                return;
            }
            
            boolean localFail = false;

            try (LineNumberReader expectedReader = 
                    new LineNumberReader(new InputStreamReader(new FileInputStream(expectedFile), ENCODING));
                 LineNumberReader actualReader = 
                    new LineNumberReader(new InputStreamReader(new FileInputStream(outFile), ENCODING)))
            {    
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
                        localFail = true;
                        log.error("FAILURE: Line mismatch for file " + inFile.getName() +
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
            }
            if (!localFail)
            {
                outFile.delete();
            }
            else
            {
                // https://code.google.com/p/java-diff-utils/wiki/SampleUsage
                List<String> original = fileToLines(expectedFile);
                List<String> revised = fileToLines(outFile);

                // Compute diff. Get the Patch object. Patch is the container for computed deltas.
                Patch patch = DiffUtils.diff(original, revised);

                try (PrintStream diffPS = new PrintStream(diffFile, ENCODING))
                {
                    for (Object delta : patch.getDeltas())
                    {
                        if (delta instanceof ChangeDelta)
                        {
                            ChangeDelta cdelta = (ChangeDelta) delta;
                            diffPS.println("Org: " + cdelta.getOriginal());
                            diffPS.println("New: " + cdelta.getRevised());
                            diffPS.println();
                        }
                        else if (delta instanceof DeleteDelta)
                        {
                            DeleteDelta ddelta = (DeleteDelta) delta;
                            diffPS.println("Org: " + ddelta.getOriginal());
                            diffPS.println("New: " + ddelta.getRevised());
                            diffPS.println();
                        }
                        else if (delta instanceof InsertDelta)
                        {
                            InsertDelta idelta = (InsertDelta) delta;
                            diffPS.println("Org: " + idelta.getOriginal());
                            diffPS.println("New: " + idelta.getRevised());
                            diffPS.println();
                        }
                        else
                        {
                            diffPS.println(delta);
                        }
                    }
                }
            }
        }
    }
    
    // Helper method for get the file content
    private static List<String> fileToLines(File file)
    {
        List<String> lines = new LinkedList<>();
        String line;
        try
        {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), ENCODING)))
            {
                while ((line = in.readLine()) != null)
                {
                    lines.add(line);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return lines;
    }

    private int findOutlineItemDestPageNum(PDDocument doc, PDOutlineItem oi) throws IOException
    {
        PDPageDestination pageDest = (PDPageDestination) oi.getDestination();
        
        // two methods to get the page index, the result should be identical!
        int indexOfPage = doc.getPages().indexOf(oi.findDestinationPage(doc));
        int pageNum = pageDest.retrievePageNumber();
        assertEquals(indexOfPage, pageNum);
                
        return pageNum;
    }

    /**
     * Test whether stripping controlled by outline items works properly. The test file has 4
     * outline items at the top level, that point to 0-based pages 0, 2, 3 and 4. We are testing
     * text stripping by outlines pointing to 0-based pages 2 and 3, and also text stripping of the
     * 0-based page 2. The test makes sure that the output is different to a complete strip, not
     * empty, different to each other when different bookmark intervals are used, but identical from
     * bookmark intervals to strips with page intervals. When fed with orphan bookmarks, stripping
     * must be empty.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    public void testStripByOutlineItems() throws IOException, URISyntaxException
    {
        PDDocument doc = PDDocument.load(new File(TestPDPageTree.class.getResource("with_outline.pdf").toURI()));
        PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
        Iterable<PDOutlineItem> children = outline.children();
        Iterator<PDOutlineItem> it = children.iterator();
        PDOutlineItem oi0 = it.next();
        PDOutlineItem oi2 = it.next();
        PDOutlineItem oi3 = it.next();
        PDOutlineItem oi4 = it.next();

        assertEquals(0, findOutlineItemDestPageNum(doc, oi0));
        assertEquals(2, findOutlineItemDestPageNum(doc, oi2));
        assertEquals(3, findOutlineItemDestPageNum(doc, oi3));
        assertEquals(4, findOutlineItemDestPageNum(doc, oi4));

        String textFull = stripper.getText(doc);
        assertFalse(textFull.isEmpty());
        
        String expectedTextFull = 
                "First level 1\n"
                + "First level 2\n"
                + "Fist level 3\n"
                + "Some content\n"
                + "Some other content\n"
                + "Second at level 1\n"
                + "Second level 2\n"
                + "Content\n"
                + "Third level 1\n"
                + "Third level 2\n"
                + "Third level 3\n"
                + "Content\n"
                + "Fourth level 1\n"
                + "Content\n"
                + "Content\n";
        assertEquals(expectedTextFull, textFull.replaceAll("\r", ""));
        
        // this should grab 0-based pages 2 and 3, i.e. 1-based pages 3 and 4
        // by their bookmarks
        stripper.setStartBookmark(oi2);
        stripper.setEndBookmark(oi3);
        String textoi23 = stripper.getText(doc);
        assertFalse(textoi23.isEmpty());
        assertFalse(textoi23.equals(textFull));
        
        String expectedTextoi23 = 
                "Second at level 1\n"
                + "Second level 2\n"
                + "Content\n"
                + "Third level 1\n"
                + "Third level 2\n"
                + "Third level 3\n"
                + "Content\n";
        assertEquals(expectedTextoi23, textoi23.replaceAll("\r", ""));
        
        // this should grab 0-based pages 2 and 3, i.e. 1-based pages 3 and 4
        // by their page numbers
        stripper.setStartBookmark(null);
        stripper.setEndBookmark(null);
        stripper.setStartPage(3);
        stripper.setEndPage(4);
        String textp34 = stripper.getText(doc);
        assertFalse(textp34.isEmpty());
        assertFalse(textoi23.equals(textFull));
        assertTrue(textoi23.equals(textp34));
        
        
        // this should grab 0-based page 2, i.e. 1-based page 3
        // by the bookmark
        stripper.setStartBookmark(oi2);
        stripper.setEndBookmark(oi2);
        String textoi2 = stripper.getText(doc);
        assertFalse(textoi2.isEmpty());
        assertFalse(textoi2.equals(textoi23));
        assertFalse(textoi23.equals(textFull));
        
        String expectedTextoi2 = 
                "Second at level 1\n"
                + "Second level 2\n"
                + "Content\n";        
        assertEquals(expectedTextoi2, textoi2.replaceAll("\r", ""));
        
         
        // this should grab 0-based page 2, i.e. 1-based page 3
        // by the page number
        stripper.setStartBookmark(null);
        stripper.setEndBookmark(null);
        stripper.setStartPage(3);
        stripper.setEndPage(3);
        String textp3 = stripper.getText(doc);
        assertFalse(textp3.isEmpty());
        assertFalse(textp3.equals(textp34));
        assertFalse(textoi23.equals(textFull));
        assertTrue(textoi2.equals(textp3));
        
        // Test with orphan bookmark
        PDOutlineItem oiOrphan = new PDOutlineItem();
        stripper.setStartBookmark(oiOrphan);
        stripper.setEndBookmark(oiOrphan);
        String textOiOrphan = stripper.getText(doc);
        assertTrue(textOiOrphan.isEmpty());
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
            @Override
            public boolean accept(File dir, String name) 
            {
                return (name.endsWith(".pdf"));
            }
        });
        for (File testFile : testFiles) 
        {
            //Test without sorting
            doTestFile(testFile, outDir, false, false);
            //Test with sorting
            doTestFile(testFile, outDir, false, true);
        }
    }
    
    /**
     * Test to validate text extraction of file set.
     *
     * @throws Exception when there is an exception
     */
    public void testExtract() throws Exception
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
