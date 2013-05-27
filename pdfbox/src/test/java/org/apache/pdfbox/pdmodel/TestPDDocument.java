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
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessBuffer;

import junit.framework.TestCase;

/**
 * Testcase introduced with PDFBOX-1581.
 * 
 */
public class TestPDDocument extends TestCase
{
    private File testResultsDir = new File("target/test-output");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testResultsDir.mkdirs();
    }

    private byte[] copyOfRange(byte[] array, int from, int to)
    {
    	// java5 backport of java6-only Arrays.copyOfRange
    	int length = to-from;
    	byte[] subArray = new byte[length];
    	System.arraycopy(array, from, subArray, 0, length);
    	return subArray;
    }
    /**
     * Test document save/load using a stream.
     * @throws IOException if something went wrong
     * @throws COSVisitorException  if something went wrong
     */
    public void testSaveLoadStream() throws IOException, COSVisitorException
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
        assertEquals("%PDF-1.4", new String(copyOfRange(pdf, 0, 8), "UTF-8"));
        assertEquals("%%EOF\n", new String(copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

        // Load
        PDDocument loadDoc = PDDocument.load(new ByteArrayInputStream(pdf), new RandomAccessBuffer());
        assertEquals(1, loadDoc.getNumberOfPages());
        loadDoc.close();
    }

    /**
     * Test document save/load using a file.
     * @throws IOException if something went wrong
     * @throws COSVisitorException  if something went wrong
     */
    public void testSaveLoadFile() throws IOException, COSVisitorException
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
        assertEquals("%PDF-1.4", new String(copyOfRange(pdf, 0, 8), "UTF-8"));
        assertEquals("%%EOF\n", new String(copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

        // Load
        PDDocument loadDoc = PDDocument.load(targetFile, new RandomAccessBuffer());
        assertEquals(1, loadDoc.getNumberOfPages());
        loadDoc.close();
    }

    /**
     * Test document save/loadNonSeq using a stream.
     * @throws IOException if something went wrong
     * @throws COSVisitorException  if something went wrong
     */
public void testSaveLoadNonSeqStream() throws IOException, COSVisitorException
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
        assertEquals("%PDF-1.4", new String(copyOfRange(pdf, 0, 8), "UTF-8"));
        assertEquals("%%EOF\n", new String(copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

        // Load
        PDDocument loadDoc = PDDocument.loadNonSeq(new ByteArrayInputStream(pdf), new RandomAccessBuffer());
        assertEquals(1, loadDoc.getNumberOfPages());
        loadDoc.close();
    }

    /**
     * Test document save/loadNonSeq using a file.
     * @throws IOException if something went wrong
     * @throws COSVisitorException  if something went wrong
     */
    public void testSaveLoadNonSeqFile() throws IOException, COSVisitorException
    {
        // Create PDF with one blank page
        PDDocument document = new PDDocument();
        document.addPage(new PDPage());

        // Save
        File targetFile = new File(testResultsDir, "pddocument-saveloadnonseqfile.pdf");
        document.save(targetFile);
        document.close();

        // Verify content
        assertTrue(targetFile.length() > 200);
        InputStream in = new FileInputStream(targetFile);
        byte[] pdf = IOUtils.toByteArray(in);
        in.close();
        assertTrue(pdf.length > 200);
        assertEquals("%PDF-1.4", new String(copyOfRange(pdf, 0, 8), "UTF-8"));
        assertEquals("%%EOF\n", new String(copyOfRange(pdf, pdf.length - 6, pdf.length), "UTF-8"));

        // Load
        PDDocument loadDoc = PDDocument.loadNonSeq(targetFile, new RandomAccessBuffer());
        assertEquals(1, loadDoc.getNumberOfPages());
        loadDoc.close();
    }
}
