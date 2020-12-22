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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.junit.jupiter.api.Test;

class TestEmbeddedFiles
{
    @Test
    void testNullEmbeddedFile() throws IOException
    {
        PDEmbeddedFile embeddedFile = null;
        boolean ok = false;
        try
        {
            final PDDocument doc = Loader.loadPDF(TestEmbeddedFiles.class
                    .getResourceAsStream(
                "null_PDComplexFileSpecification.pdf"));

            final PDDocumentCatalog catalog = doc.getDocumentCatalog();
            final PDDocumentNameDictionary names = catalog.getNames();
            assertEquals(2, names.getEmbeddedFiles().getNames().size(), "expected two files");
            final PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();

            PDComplexFileSpecification spec = embeddedFiles.getNames().get("non-existent-file.docx");

            if (spec != null)
            {
                embeddedFile = spec.getEmbeddedFile();
                ok = true;
            }
            //now test for actual attachment
            spec = embeddedFiles.getNames().get("My first attachment");
            assertNotNull(spec, "one attachment actually exists");
            assertEquals(17660, spec.getEmbeddedFile().getLength(), "existing file length");
            spec = embeddedFiles.getNames().get("non-existent-file.docx");
        }
        catch (NullPointerException e)
        {
            fail("null pointer exception");
        }
        assertTrue(ok, "Was able to get file without exception");
        assertNull(embeddedFile, "EmbeddedFile was correctly null");
    }

    @Test
    void testOSSpecificAttachments() throws IOException
    {
        PDEmbeddedFile nonOSFile = null;
        PDEmbeddedFile macFile = null;
        PDEmbeddedFile dosFile = null;
        PDEmbeddedFile unixFile = null;

        final PDDocument doc = Loader.loadPDF(
                TestEmbeddedFiles.class
                .getResourceAsStream("testPDF_multiFormatEmbFiles.pdf"));

        final PDDocumentCatalog catalog = doc.getDocumentCatalog();
        final PDDocumentNameDictionary names = catalog.getNames();
        final PDEmbeddedFilesNameTreeNode treeNode = names.getEmbeddedFiles();
        final List<PDNameTreeNode<PDComplexFileSpecification>> kids = treeNode.getKids();
        for (final PDNameTreeNode<PDComplexFileSpecification> kid : kids)
        {
            final Map<String, PDComplexFileSpecification> tmpNames = kid.getNames();
            final COSObjectable obj = tmpNames.get("My first attachment");
            
            final PDComplexFileSpecification spec = (PDComplexFileSpecification) obj;
            nonOSFile = spec.getEmbeddedFile();
            macFile = spec.getEmbeddedFileMac();
            dosFile = spec.getEmbeddedFileDos();
            unixFile = spec.getEmbeddedFileUnix();
        }

        assertTrue(byteArrayContainsLC("non os specific", nonOSFile.toByteArray(), "ISO-8859-1"),
                "non os specific");
        assertTrue(byteArrayContainsLC("mac embedded", macFile.toByteArray(), "ISO-8859-1"), "mac");

        assertTrue(byteArrayContainsLC("dos embedded", dosFile.toByteArray(), "ISO-8859-1"), "dos");

        assertTrue(byteArrayContainsLC("unix embedded", unixFile.toByteArray(), "ISO-8859-1"),
                "unix");

    }

    private boolean byteArrayContainsLC(final String target, final byte[] bytes, final String encoding)
            throws UnsupportedEncodingException
    {
        final String s = new String(bytes, encoding);
        return s.toLowerCase().contains(target);
    }
}
