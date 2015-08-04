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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.junit.Test;

import junit.framework.TestCase;

public class TestEmbeddedFiles extends TestCase
{
    @Test
    public void testNullEmbeddedFile() throws IOException
    {
        PDEmbeddedFile embeddedFile = null;
        boolean ok = false;
        try
        {
            PDDocument doc = PDDocument.load(TestEmbeddedFiles.class.getResourceAsStream(
                "null_PDComplexFileSpecification.pdf"));

            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDDocumentNameDictionary names = catalog.getNames();
            assertEquals("expected two files", 2, names.getEmbeddedFiles().getNames().size());
            PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();

            PDComplexFileSpecification spec = embeddedFiles.getNames().get("non-existent-file.docx");

            if (spec != null)
            {
                embeddedFile = spec.getEmbeddedFile();
                ok = true;
            }
            //now test for actual attachment
            spec = embeddedFiles.getNames().get("My first attachment");
            assertNotNull("one attachment actually exists", spec);
            assertEquals("existing file length", 17660, spec.getEmbeddedFile().getLength());
            spec = embeddedFiles.getNames().get("non-existent-file.docx");
        }
        catch (NullPointerException e)
        {
            assertNotNull("null pointer exception", null);
        }
        assertTrue("Was able to get file without exception", ok);
        assertNull("EmbeddedFile was correctly null", embeddedFile);
    }

    @Test
    public void testOSSpecificAttachments() throws IOException
    {
        PDEmbeddedFile nonOSFile = null;
        PDEmbeddedFile macFile = null;
        PDEmbeddedFile dosFile = null;
        PDEmbeddedFile unixFile = null;

        PDDocument doc = PDDocument.load(TestEmbeddedFiles.class
                .getResourceAsStream("testPDF_multiFormatEmbFiles.pdf"));

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        PDEmbeddedFilesNameTreeNode treeNode = names.getEmbeddedFiles();
        List<PDNameTreeNode<PDComplexFileSpecification>> kids = treeNode.getKids();
        for (PDNameTreeNode kid : kids)
        {
            Map<String, PDComplexFileSpecification> tmpNames = kid.getNames();
            COSObjectable obj = tmpNames.get("My first attachment");
            
            PDComplexFileSpecification spec = (PDComplexFileSpecification) obj;
            nonOSFile = spec.getEmbeddedFile();
            macFile = spec.getEmbeddedFileMac();
            dosFile = spec.getEmbeddedFileDos();
            unixFile = spec.getEmbeddedFileUnix();
        }

        assertTrue("non os specific",
                byteArrayContainsLC("non os specific", nonOSFile.toByteArray(), "ISO-8859-1"));

        assertTrue("mac", byteArrayContainsLC("mac embedded", macFile.toByteArray(), "ISO-8859-1"));

        assertTrue("dos", byteArrayContainsLC("dos embedded", dosFile.toByteArray(), "ISO-8859-1"));

        assertTrue("unix",
                byteArrayContainsLC("unix embedded", unixFile.toByteArray(), "ISO-8859-1"));

    }

    private boolean byteArrayContainsLC(String target, byte[] bytes, String encoding)
            throws UnsupportedEncodingException
    {
        String s = new String(bytes, encoding);
        return s.toLowerCase().contains(target);
    }
}
