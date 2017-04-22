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
package org.apache.pdfbox.pdmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;

import org.apache.pdfbox.io.IOUtils;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Testcase introduced with PDFBOX-1581.
 * 
 */
public class TestPDDocument extends TestCase
{
    private final File testResultsDir = new File("target/test-output");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    /**
     * Test document save/load using a stream.
     * @throws IOException if something went wrong
     */
    public void testSaveLoadStream() throws IOException
    {
        // Create PDF with one blank page
        PDDocument document = new PDDocument();
        document.addPage(new PDPage());

        // Save
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();

        // Verify content
        byte[] pdf = baos.toByteArray();
        assertTrue(pdf.length > 200);
        assertEquals("%PDF-1.4", new String(Arrays.copyOfRange(pdf, 0, 8), "UTF-8"));
        assertEquals("%%EOF\n", new String(Arrays.copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

        // Load
        PDDocument loadDoc = PDDocument.load(new ByteArrayInputStream(pdf));
        assertEquals(1, loadDoc.getNumberOfPages());
        loadDoc.close();
    }

    /**
     * Test document save/load using a file.
     * @throws IOException if something went wrong
     */
    public void testSaveLoadFile() throws IOException
    {
        // Create PDF with one blank page
        PDDocument document = new PDDocument();
        document.addPage(new PDPage());

        // Save
        File targetFile = new File(testResultsDir, "pddocument-saveloadfile.pdf");
        document.save(targetFile);
        document.close();

        // Verify content
        assertTrue(targetFile.length() > 200);
        InputStream in = new FileInputStream(targetFile);
        byte[] pdf = IOUtils.toByteArray(in);
        in.close();
        assertTrue(pdf.length > 200);
        assertEquals("%PDF-1.4", new String(Arrays.copyOfRange(pdf, 0, 8), "UTF-8"));
        assertEquals("%%EOF\n", new String(Arrays.copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

        // Load
        PDDocument loadDoc = PDDocument.load(targetFile);
        assertEquals(1, loadDoc.getNumberOfPages());
        loadDoc.close();
    }

    /**
     * Test get/setVersion.
     * @throws IOException if something went wrong
     */
    public void testVersions() throws IOException
    {
        PDDocument document = new PDDocument();
        // test default version
        assertEquals(1.4f, document.getVersion());
        assertEquals(1.4f, document.getDocument().getVersion());
        assertEquals("1.4", document.getDocumentCatalog().getVersion());
        // force downgrading version (header)
        document.getDocument().setVersion(1.3f);
        document.getDocumentCatalog().setVersion(null);
        // test new version (header)
        assertEquals(1.3f, document.getVersion());
        assertEquals(1.3f, document.getDocument().getVersion());
        assertNull(document.getDocumentCatalog().getVersion());
        document.close();

        // check if version downgrade is denied
        document = new PDDocument();
        document.setVersion(1.3f);
        // all versions shall have their default value
        assertEquals(1.4f, document.getVersion());
        assertEquals(1.4f, document.getDocument().getVersion());
        assertEquals("1.4", document.getDocumentCatalog().getVersion());
        
        // check version upgrade
        document.setVersion(1.5f);
        // overall version has to be 1.5f
        assertEquals(1.5f, document.getVersion());
        // header version has to be unchanged
        assertEquals(1.4f, document.getDocument().getVersion());
        // catalog version version has to be 1.5
        assertEquals("1.5", document.getDocumentCatalog().getVersion());
        document.close();
    }

    /**
     * Test whether a bad file can be deleted after load() failed.
     *
     * @throws java.io.FileNotFoundException
     */
    public void testDeleteBadFile() throws FileNotFoundException
    {
        File f = new File("test.pdf");
        PrintWriter pw = new PrintWriter(new FileOutputStream(f));
        pw.write("<script language='JavaScript'>");
        pw.close();
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load(f);
            fail("parsing should fail");
        }
        catch (IOException ex)
        {
            // expected
        }
        finally
        {
            assertNull(doc);
        }

        boolean deleted = f.delete();
        assertTrue("delete bad file failed after failed load()", deleted);
    }

    /**
     * Test whether a good file can be deleted after load() and close() succeed.
     *
     * @throws java.io.FileNotFoundException
     */
    public void testDeleteGoodFile() throws IOException
    {
        File f = new File("test.pdf");
        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.save(f);
        doc.close();

        PDDocument.load(f).close();

        boolean deleted = f.delete();
        assertTrue("delete good file failed after successful load() and close()", deleted);
    }

    /**
     * PDFBOX-3481: Test whether XRef generation results in unusable PDFs if Arab numbering is
     * default.
     */
    public void testSaveArabicLocale() throws IOException
    {
        Locale defaultLocale = Locale.getDefault();
        Locale arabicLocale = new Locale.Builder().setLanguageTag("ar-EG-u-nu-arab").build();
        Locale.setDefault(arabicLocale);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        doc.save(baos);
        doc.close();

        PDDocument.load(new ByteArrayInputStream(baos.toByteArray())).close();

        Locale.setDefault(defaultLocale);
    }
}
