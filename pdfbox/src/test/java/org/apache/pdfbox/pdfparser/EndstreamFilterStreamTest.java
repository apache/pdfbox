/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdfparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class EndstreamFilterStreamTest
{
    @Test
    void testEndstreamFilterStream() throws IOException
    {
        EndstreamFilterStream feos = new EndstreamFilterStream();
        byte[] tab1 = { 1, 2, 3, 4 };
        byte[] tab2 = { 5, 6, 7, '\r', '\n' };
        byte[] tab3 = { 8, 9, '\r', '\n' };
        feos.filter(tab1, 0, tab1.length);
        feos.filter(tab2, 0, tab2.length);
        feos.filter(tab3, 0, tab3.length);
        byte[] expectedResult1 = { 1, 2, 3, 4, 5, 6, 7, '\r', '\n', 8, 9 };
        assertEquals(expectedResult1.length, feos.calculateLength());

        feos = new EndstreamFilterStream();
        byte[] tab4 = { 1, 2, 3, 4 };
        byte[] tab5 = { 5, 6, 7, '\r' };
        byte[] tab6 = { 8, 9, '\n' };
        feos.filter(tab4, 0, tab4.length);
        feos.filter(tab5, 0, tab5.length);
        feos.filter(tab6, 0, tab6.length);
        byte[] expectedResult2 = { 1, 2, 3, 4, 5, 6, 7, '\r', 8, 9 };
        assertEquals(expectedResult2.length, feos.calculateLength());

        feos = new EndstreamFilterStream();
        byte[] tab7 = { 1, 2, 3, 4, '\r' };
        byte[] tab8 = { '\n', 5, 6, 7, '\n' };
        byte[] tab9 = { 8, 9, '\r' }; // final CR is not to be discarded
        feos.filter(tab7, 0, tab7.length);
        feos.filter(tab8, 0, tab8.length);
        feos.filter(tab9, 0, tab9.length);
        byte[] expectedResult3 = { 1, 2, 3, 4, '\r', '\n', 5, 6, 7, '\n', 8, 9, '\r' };
        assertEquals(expectedResult3.length, feos.calculateLength());

        feos = new EndstreamFilterStream();
        byte[] tab10 = { 1, 2, 3, 4, '\r' };
        byte[] tab11 = { '\n', 5, 6, 7, '\r' };
        byte[] tab12 = { 8, 9, '\r' };
        byte[] tab13 = { '\n' }; // final CR LF across buffers
        feos.filter(tab10, 0, tab10.length);
        feos.filter(tab11, 0, tab11.length);
        feos.filter(tab12, 0, tab12.length);
        feos.filter(tab13, 0, tab13.length);
        byte[] expectedResult4 = { 1, 2, 3, 4, '\r', '\n', 5, 6, 7, '\r', 8, 9 };
        assertEquals(expectedResult4.length, feos.calculateLength());

        feos = new EndstreamFilterStream();
        byte[] tab14 = { 1, 2, 3, 4, '\r' };
        byte[] tab15 = { '\n', 5, 6, 7, '\r' };
        byte[] tab16 = { 8, 9, '\n' };
        byte[] tab17 = { '\r' }; // final CR is not to be discarded
        feos.filter(tab14, 0, tab14.length);
        feos.filter(tab15, 0, tab15.length);
        feos.filter(tab16, 0, tab16.length);
        feos.filter(tab17, 0, tab17.length);
        byte[] expectedResult5 = { 1, 2, 3, 4, '\r', '\n', 5, 6, 7, '\r', 8, 9, '\n', '\r' };
        assertEquals(expectedResult5.length, feos.calculateLength());
    }

    @Test
    void testPDFBox2079EmbeddedFile() throws IOException
    {
        // there should be 17660 bytes in the zip file.
        // in PDFBox 1.8.5, windows newline is appended to the byte stream
        // yielding 17662 bytes, which causes a problem for ZipFile in Java 1.6

        // Modification of embedded_zip.pdf for 2.0:
        // /Length entry removed to force usage of EndstreamOutputStream
        try (PDDocument doc = Loader.loadPDF(
                new File("src/test/resources/org/apache/pdfbox/pdfparser", "embedded_zip.pdf")))
        {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDDocumentNameDictionary names = catalog.getNames();
            PDEmbeddedFilesNameTreeNode node = names.getEmbeddedFiles();
            Map<String, PDComplexFileSpecification> map = node.getNames();
            assertEquals(1, map.size());
            PDComplexFileSpecification spec = map.get("My first attachment");
            PDEmbeddedFile file = spec.getEmbeddedFile();
            InputStream input = file.createInputStream();
            File d = new File("target/test-output");
            d.mkdirs();
            File f = new File(d, spec.getFile());
            try (OutputStream os = new FileOutputStream(f))
            {
                input.transferTo(os);
            }
            assertEquals(17660, f.length());
        }
    }
}
