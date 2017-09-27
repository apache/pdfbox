/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.pdfparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.DateConverter;
import org.junit.Before;
import org.junit.Test;

public class TestPDFParser
{

    private static final String PATH_OF_PDF = "src/test/resources/input/yaddatest.pdf";
    private static final File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));

    private int numberOfTmpFiles = 0;

    /**
     * Initialize the number of tmp file before the test
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        numberOfTmpFiles = getNumberOfTempFile();
    }

    /**
     * Count the number of temporary files
     * 
     * @return
     */
    private int getNumberOfTempFile()
    {
        int result = 0;
        File[] tmpPdfs = tmpDirectory.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.startsWith(COSParser.TMP_FILE_PREFIX)
                        && name.endsWith("pdf");
            }
        });

        if (tmpPdfs != null)
        {
            result = tmpPdfs.length;
        }

        return result;
    }

    @Test
    public void testPDFParserFile() throws IOException
    {
        executeParserTest(new RandomAccessBufferedFileInputStream(new File(PATH_OF_PDF)), MemoryUsageSetting.setupMainMemoryOnly());
    }

    @Test
    public void testPDFParserInputStream() throws IOException
    {
        executeParserTest(new RandomAccessBufferedFileInputStream(new FileInputStream(PATH_OF_PDF)), MemoryUsageSetting.setupMainMemoryOnly());
    }

    @Test
    public void testPDFParserFileScratchFile() throws IOException
    {
        executeParserTest(new RandomAccessBufferedFileInputStream(new File(PATH_OF_PDF)), MemoryUsageSetting.setupTempFileOnly());
    }

    @Test
    public void testPDFParserInputStreamScratchFile() throws IOException
    {
        executeParserTest(new RandomAccessBufferedFileInputStream(new FileInputStream(PATH_OF_PDF)), MemoryUsageSetting.setupTempFileOnly());
    }
    
    @Test
    public void testPDFParserMissingCatalog() throws IOException, URISyntaxException
    {
        // PDFBOX-3060
        PDDocument.load(new File(TestPDFParser.class.getResource("MissingCatalog.pdf").toURI())).close();
    }

    /**
     * Test whether /Info dictionary is retrieved correctly when rebuilding the trailer of a corrupt
     * file. An incorrect algorithm would result in an outline dictionary being mistaken for an
     * /Info.
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    @Test
    public void testPDFBox3208() throws MalformedURLException, IOException
    {
        byte[] byteArray;
        try
        {
            try (InputStream is = new URL("https://issues.apache.org/jira/secure/attachment/12784025/PDFBOX-3208-L33MUTT2SVCWGCS6UIYL5TH3PNPXHIS6.pdf").openStream())
            {
                byteArray = IOUtils.toByteArray(is);
            }
        }
        catch (IOException ex)
        {
            System.err.println("URL loading failed, testPDFBox3208 will be skipped");
            return;
        }

        try (PDDocument doc = PDDocument.load(byteArray))
        {
            PDDocumentInformation di = doc.getDocumentInformation();
            assertEquals("Liquent Enterprise Services", di.getAuthor());
            assertEquals("Liquent services server", di.getCreator());
            assertEquals("Amyuni PDF Converter version 4.0.0.9", di.getProducer());
            assertEquals("", di.getKeywords());
            assertEquals("", di.getSubject());
            assertEquals("892B77DE781B4E71A1BEFB81A51A5ABC_20140326022424.docx", di.getTitle());
            assertEquals(DateConverter.toCalendar("D:20140326142505-02'00'"), di.getCreationDate());
            assertEquals(DateConverter.toCalendar("20140326172513Z"), di.getModificationDate());
        }
    }

    /**
     * Test whether the /Info is retrieved correctly when rebuilding the trailer of a corrupt file,
     * despite the /Info dictionary not having a modification date.
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    @Test
    public void testPDFBox3940() throws MalformedURLException, IOException
    {
        byte[] byteArray;
        try
        {
            try (InputStream is = new URL("https://issues.apache.org/jira/secure/attachment/12888957/079977.pdf").openStream())
            {
                byteArray = IOUtils.toByteArray(is);
            }
        }
        catch (IOException ex)
        {
            System.err.println("URL loading failed, testPDFBox3940 will be skipped");
            return;
        }

        try (PDDocument doc = PDDocument.load(byteArray))
        {
            PDDocumentInformation di = doc.getDocumentInformation();
            assertEquals("Unknown", di.getAuthor());
            assertEquals("C:REGULA~1IREGSFR_EQ_EM.WP", di.getCreator());
            assertEquals("Acrobat PDFWriter 3.02 for Windows", di.getProducer());
            assertEquals("", di.getKeywords());
            assertEquals("", di.getSubject());
            assertEquals("C:REGULA~1IREGSFR_EQ_EM.PDF", di.getTitle());
            assertEquals(DateConverter.toCalendar("Tuesday, July 28, 1998 4:00:09 PM"), di.getCreationDate());
        }
    }

    private void executeParserTest(RandomAccessRead source, MemoryUsageSetting memUsageSetting) throws IOException
    {
        ScratchFile scratchFile = new ScratchFile(memUsageSetting);
        PDFParser pdfParser = new PDFParser(source, scratchFile);
        pdfParser.parse();
        try (COSDocument doc = pdfParser.getDocument())
        {
            assertNotNull(doc);
        }
        source.close();
        // number tmp file must be the same
        assertEquals(numberOfTmpFiles, getNumberOfTempFile());
    }

}
