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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test case introduced with PDFBOX-1581.
 * 
 */
class TestPDDocument
{
    private static final File TESTRESULTSDIR = new File("target/test-output");

    @BeforeAll
    public static void setUp() throws Exception
    {
        TESTRESULTSDIR.mkdirs();
    }

    /**
     * Test document save/load using a stream.
     * @throws IOException if something went wrong
     */
    @Test
    void testSaveLoadStream() throws IOException
    {
        ByteArrayOutputStream baos;
        // Create PDF with one blank page
        try (PDDocument document = new PDDocument())
        {
            document.addPage(new PDPage());
            // Save
            baos = new ByteArrayOutputStream();
            document.save(baos, CompressParameters.NO_COMPRESSION);
        }

        // Verify content
        byte[] pdf = baos.toByteArray();
        assertTrue(pdf.length > 200);
        assertEquals("%PDF-1.4", new String(Arrays.copyOfRange(pdf, 0, 8), StandardCharsets.UTF_8));
        assertEquals("%%EOF\n", new String(Arrays.copyOfRange(pdf, pdf.length - 6, pdf.length), StandardCharsets.UTF_8));

        // reload
        try (PDDocument loadDoc = Loader.loadPDF(pdf))
        {
            assertEquals(1, loadDoc.getNumberOfPages());
        }
    }

    /**
     * Test document save/load using a file.
     * @throws IOException if something went wrong
     */
    @Test
    void testSaveLoadFile() throws IOException
    {
        File targetFile = new File(TESTRESULTSDIR, "pddocument-saveloadfile.pdf");

        // Create PDF with one blank page
        try (PDDocument document = new PDDocument())
        {
            document.addPage(new PDPage());
            document.save(targetFile, CompressParameters.NO_COMPRESSION);
        }

        // Verify content
        assertTrue(targetFile.length() > 200);

        byte[] pdf = Files.readAllBytes(targetFile.toPath());

        assertTrue(pdf.length > 200);
        assertEquals("%PDF-1.4", new String(Arrays.copyOfRange(pdf, 0, 8), StandardCharsets.UTF_8));
        assertEquals("%%EOF\n", new String(Arrays.copyOfRange(pdf, pdf.length - 6, pdf.length), StandardCharsets.UTF_8));

        // reload
        try (PDDocument loadDoc = Loader.loadPDF(targetFile))
        {
            assertEquals(1, loadDoc.getNumberOfPages());
        }
    }

    /**
     * Test get/setVersion.
     * @throws IOException if something went wrong
     */
    @Test
    void testVersions() throws IOException
    {
        // test default version
        try (PDDocument document = new PDDocument())
        {
            // test default version
            assertEquals(1.4f, document.getVersion(), 0);
            assertEquals(1.4f, document.getDocument().getVersion(), 0);
            assertEquals("1.4", document.getDocumentCatalog().getVersion());
            // force downgrading version (header)
            document.getDocument().setVersion(1.3f);
            document.getDocumentCatalog().setVersion(null);
            // test new version (header)
            assertEquals(1.3f, document.getVersion(), 0);
            assertEquals(1.3f, document.getDocument().getVersion(), 0);
            assertNull(document.getDocumentCatalog().getVersion());
        }

        // check if version downgrade is denied
        try (PDDocument document = new PDDocument())
        {
            document.setVersion(1.3f);
            // all versions shall have their default value
            assertEquals(1.4f, document.getVersion(), 0);
            assertEquals(1.4f, document.getDocument().getVersion(), 0);
            assertEquals("1.4", document.getDocumentCatalog().getVersion());

            // check version upgrade
            document.setVersion(1.5f);
            // overall version has to be 1.5f
            assertEquals(1.5f, document.getVersion(), 0);
            // header version has to be unchanged
            assertEquals(1.4f, document.getDocument().getVersion(), 0);
            // catalog version version has to be 1.5
            assertEquals("1.5", document.getDocumentCatalog().getVersion());
        }

        // PDFBOX-5265: check that all versions are 1.6 when compression is used (default)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument())
        {
            document.addPage(new PDPage());
            document.save(baos);
        }
        try (PDDocument document = Loader.loadPDF(baos.toByteArray()))
        {
            assertEquals("1.6", document.getDocumentCatalog().getVersion());
            assertEquals(1.6f, document.getDocument().getVersion());
            assertEquals(1.6f, document.getVersion());
        }
        assertEquals("%PDF-1.6", new String(baos.toByteArray(), 0, 8));
    }

    /**
     * Test whether a bad file can be deleted after load() failed.
     *
     * @throws java.io.IOException
     */
    @Test
    void testDeleteBadFile() throws IOException
    {
        File f = new File(TESTRESULTSDIR, "testDeleteBadFile.pdf");
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(f)))
        {
            pw.write("<script language='JavaScript'>");
        }
        assertThrows(IOException.class, () -> Loader.loadPDF(f), "parsing should fail");
        try
        {
            Files.delete(f.toPath());
        }
        catch (IOException ex)
        {
            fail("delete bad file failed after failed load");
        }
    }

    /**
     * Test whether a good file can be deleted after loadPDF() and close() succeed.
     *
     * @throws java.io.IOException
     */
    @Test
    void testDeleteGoodFile() throws IOException
    {
        File f = new File(TESTRESULTSDIR, "testDeleteGoodFile.pdf");
        try (PDDocument doc = new PDDocument())
        {
            doc.addPage(new PDPage());
            doc.save(f);
        }

        Loader.loadPDF(f).close();
        
        try
        {
            Files.delete(f.toPath());
        }
        catch (IOException ex)
        {
            fail("delete good file failed after successful load() and close()");
        }
    }

    /**
     * PDFBOX-3481: Test whether XRef generation results in unusable PDFs if Arab numbering is
     * default.
     *
     * @throws java.io.IOException
     */
    @Test
    void testSaveArabicLocale() throws IOException
    {
        Locale defaultLocale = Locale.getDefault();
        Locale arabicLocale = new Locale.Builder().setLanguageTag("ar-EG-u-nu-arab").build();
        Locale.setDefault(arabicLocale);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PDDocument doc = new PDDocument())
        {
            doc.addPage(new PDPage());
            doc.save(baos);
        }

        Loader.loadPDF(baos.toByteArray()).close();

        Locale.setDefault(defaultLocale);
    }
}
