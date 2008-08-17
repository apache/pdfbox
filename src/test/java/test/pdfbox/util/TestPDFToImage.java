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

import java.io.*; //for BufferedReader
/*import java.io.File;
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
*/

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFImageWriter;

/**
 * Test suite for PDFTextStripper.
 *
 * FILE SET VALIDATION
 *
 * This test suite is designed to test PDFToImage using a set of PDF
 * files and known good output for each.  The default mode of testAll()
 * is to process each *.pdf file in "test/input".  An output file is
 * created in "test/output" with the same name as the PDF file, plus an
 * additional ".jpg" suffix.
 *
 * The output file is then tested against a known good result file from
 * the input directory (again, with the same name as the tested PDF file,
 * but with the additional ".jpg" suffix).
 *
 * Currently, testing against known output is simply a byte-for-byte comparison
 *
 *In the future, testing against the known output may be accomplished using PerceptualDiff
 *  http://sourceforge.net/projects/pdiff
 *
 *
 * @author <a href="mailto:DanielWilson@Users.Sourceforge.net">Daniel Wilson</a>
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.1 $
 */
public class TestPDFToImage extends TestCase
{
    private boolean bFail = false;
    private PDFImageWriter writer = null;
    private PrintWriter log = null;
    private File mdirTest = null;
    private File mcurFile = null;

    /**
     * Test class constructor.
     *
     * @param name The name of the test class.
     *
     * @throws IOException If there is an error creating the test.
     */
    public TestPDFToImage( String name ) throws IOException
    {
        super( name );
        writer = new PDFImageWriter();
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
     * Validate text extraction on a single file.
     *
     * @param file The file to validate
     * @param bLogResult Whether to log the extracted text
     * @throws Exception when there is an exception
     */
    public void doTestFile(File file, boolean bLogResult)
        throws Exception
    {
	String sCmd;
        PDDocument document = null;

        log.println("\nPreparing to convert " + file.getName());
        log.flush();

	try
      {
        document =  PDDocument.load(file);
        writer.WriteImage(document,"png","",1,Integer.MAX_VALUE, "test/output/" + file.getName() + "-");
      }
      catch(Exception e)
      {
          this.bFail=true;
         System.err.println("Error converting file " + file.getName());
         e.printStackTrace();

          log.println("Error converting file " + file.getName() + "\n" + e);

      }
      finally{
          document.close();
      }

      //Now check the resulting files ... did we get identical PNG(s)?
      try
      {
          mcurFile = file;

      File[] outFiles = testDir().listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return (name.endsWith(".png") && name.startsWith(mcurFile.getName(),0));
                }
            });
            for (int n = 0; n < outFiles.length; n++)
                {
                    File outFile = new File("test/output/" + outFiles[n].getName());
                    if (!outFile.exists() ||
                        !filesAreIdentical(outFiles[n], outFile)){
                            this.bFail=true;
                            log.println("Input and output not identical for file: " + outFile.getName());
                        }
                }
        }
    catch(Exception e)
      {
          this.bFail=true;
         System.err.println("Error comparing file output for " + file.getName());
         e.printStackTrace();
      }

    }

    /**
     * Test to validate image rendering of file set.
     *
     * @throws Exception when there is an exception
     */
    public void testRenderImage()
        throws Exception
    {
        String filename = System.getProperty("test.pdfbox.util.TextStripper.file");

        try
        {
            log = new PrintWriter( new FileWriter( "RenderImage.log" ) );

            if ((filename == null) || (filename.length() == 0))
            {
                File[] testFiles = testDir().listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return (name.endsWith(".pdf") || name.endsWith(".ai"));
                    }
                });

                for (int n = 0; n < testFiles.length; n++)
                {
                    doTestFile(testFiles[n], false);
                }
            }
            else
            {
                doTestFile(new File(testDir(), filename), true);
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
        return new TestSuite( TestPDFToImage.class );
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestPDFToImage.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    public static boolean filesAreIdentical(File left, File right) throws IOException
    {
	    //http://forum.java.sun.com/thread.jspa?threadID=688105&messageID=4003259

	    /* -- I reworked ASSERT's into IF statement -- dwilson
        assert left != null;
        assert right != null;
        assert left.exists();
        assert right.exists();
        */
	if(left != null && right != null && left.exists() && right.exists()){
		if (left.length() != right.length())
		    return false;

		FileInputStream lin = new FileInputStream(left);
		FileInputStream rin = new FileInputStream(right);
		try
		{
		    byte[] lbuffer = new byte[4096];
		    byte[] rbuffer = new byte[lbuffer.length];
		    for (int lcount = 0; (lcount = lin.read(lbuffer)) > 0;)
		    {
			int bytesRead = 0;
			for (int rcount = 0; (rcount = rin.read(rbuffer, bytesRead, lcount - bytesRead)) > 0;)
			{
			    bytesRead += rcount;
			}
			for (int byteIndex = 0; byteIndex < lcount; byteIndex++)
			{
			    if (lbuffer[byteIndex] != rbuffer[byteIndex])
				return false;
			}
		    }
		}
		finally
		{
		    lin.close();
		    rin.close();
		}
		return true;
	}else{
		return false;
	}
    }

    private File testDir(){
        if (null==mdirTest)
            mdirTest = new File("test/input/rendering");

        return mdirTest;
    }
}
