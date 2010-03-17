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
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;


/**
 * Test the performance of the PDF text stripper utility.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class TestTextStripperPerformance extends TestCase
{
    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     */
    public TestTextStripperPerformance( String name )
    {
        super( name );
    }

    /**
     * Test suite setup.
     */
    public void setUp()
    {
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

        PDFTextStripper stripper = new PDFTextStripper();
        OutputStream os = null;
        Writer writer = null;
        PDDocument document = null;
        try
        {
            document = PDDocument.load(file);

            File outFile = new File(file.getParentFile().getParentFile(), "output/" + file.getName() + ".txt");
            os = new FileOutputStream(outFile);
            writer = new OutputStreamWriter(os);

            stripper.writeText(document, writer);
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
        String filename = System.getProperty("org.apache.pdfbox.util.TextStripper.file");
        File testDir = new File("src/test/resources/input");

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
            //doTestFile(new File(testDir, filename), true);
        }
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite( TestTextStripperPerformance.class );
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestTextStripperPerformance.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
