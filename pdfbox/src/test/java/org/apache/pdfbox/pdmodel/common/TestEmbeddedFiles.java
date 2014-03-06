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
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.TestPDDocumentCatalog;
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

            PDComplexFileSpecification spec = (PDComplexFileSpecification)
                                            embeddedFiles.getNames().get("non-existent-file.docx");

            if (spec != null)
            {
                embeddedFile = spec.getEmbeddedFile();
                ok = true;
            }
            //now test for actual attachment
            spec = (PDComplexFileSpecification)embeddedFiles.getNames().get("My first attachment");
            assertNotNull("one attachment actually exists", spec);
            assertEquals("existing file length", 17660, spec.getEmbeddedFile().getLength());
            spec = (PDComplexFileSpecification)embeddedFiles
                                                    .getNames().get("non-existent-file.docx");
        }
        catch (NullPointerException e)
        {
            assertNotNull("null pointer exception", null);
        }
        assertTrue("Was able to get file without exception", ok);
        assertNull("EmbeddedFile was correctly null", embeddedFile);
    }

}

