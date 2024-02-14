/*
 * Copyright 2015 The Apache Software Foundation.
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
package org.apache.pdfbox.examples.pdfa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.examples.pdmodel.CreatePDFA;
import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class CreatePDFATest
{
    private final String outDir = "target/test-output";

    @BeforeEach
    protected void setUp() throws Exception
    {
        new File(outDir).mkdirs();
    }

    /**
     * Test of doIt method of class CreatePDFA.
     */
    @Test
    void testCreatePDFA() throws Exception
    {
        System.out.println("testCreatePDFA");
        String pdfaFilename = outDir + "/PDFA.pdf";
        String signedPdfaFilename = outDir + "/PDFA_signed.pdf";
        String keystorePath = "src/test/resources/org/apache/pdfbox/examples/signature/keystore.p12";
        String message = "The quick brown fox jumps over the lazy dog äöüÄÖÜß @°^²³ {[]}";
        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        String fontfile = dir + "LiberationSans-Regular.ttf";
        CreatePDFA.main(new String[] { pdfaFilename, message, fontfile });

        // sign PDF - because we want to make sure that the signed PDF is also PDF/A-1b
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), "123456".toCharArray());
        CreateSignature signing = new CreateSignature(keystore, "123456".toCharArray());
        signing.signDetached(new File(pdfaFilename), new File(signedPdfaFilename));

        // Verify that it is PDF/A-1b
        ValidationResult result = PreflightParser.validate(new File(signedPdfaFilename));
        for (ValidationError ve : result.getErrorsList())
        {
            System.err.println(ve.getErrorCode() + ": " + ve.getDetails());
        }
        assertTrue(result.isValid(), "PDF file created with CreatePDFA is not valid PDF/A-1b");

        // check the XMP metadata
        try (PDDocument document = Loader.loadPDF(new File(pdfaFilename)))
        {
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata meta = catalog.getMetadata();
            DomXmpParser xmpParser = new DomXmpParser();
            XMPMetadata metadata = xmpParser.parse(meta.toByteArray());
            DublinCoreSchema dc = metadata.getDublinCoreSchema();
            assertEquals(pdfaFilename, dc.getTitle());
        }

        File signedFile = new File(signedPdfaFilename);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(signedFile)));
        String line;
        boolean isIncrementalArea = false;
        Set<String> set = new HashSet<>();
        int linePos = 0;
        while ((line = br.readLine()) != null)
        {
            ++linePos;
            if (line.equals("%%EOF"))
            {
                isIncrementalArea = true;
                set.clear(); // for cases with several revisions
            }
            if (!isIncrementalArea)
            {
                continue;
            }
            if (line.matches("\\d+ 0 obj"))
            {
                int pos = line.indexOf(" 0 obj");
                line = line.substring(0, pos);
                assertFalse(set.contains(line), "object '" + line
                        + " 0 obj' twice in incremental part of PDF at line " + linePos);
                set.add(line);
            }
        }
        br.close();

    }
}
