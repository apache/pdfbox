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

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import org.apache.pdfbox.examples.pdmodel.CreatePDFA;
import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.DomXmpParser;

/**
 *
 * @author Tilman Hausherr
 */
public class CreatePDFATest extends TestCase
{
    private final String outDir = "target/test-output";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        new File(outDir).mkdirs();
    }

    /**
     * Test of doIt method of class CreatePDFA.
     */
    public void testCreatePDFA() throws Exception
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
        PreflightParser preflightParser = new PreflightParser(new File(signedPdfaFilename));
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        for (ValidationError ve : result.getErrorsList())
        {
            System.err.println(ve.getErrorCode() + ": " + ve.getDetails());
        }
        assertTrue("PDF file created with CreatePDFA is not valid PDF/A-1b", result.isValid());
        preflightDocument.close();
        
        // check the XMP metadata
        PDDocument document = PDDocument.load(new File(pdfaFilename));
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDMetadata meta = catalog.getMetadata();
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata metadata = xmpParser.parse(meta.toByteArray());
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        assertEquals(pdfaFilename, dc.getTitle());
        document.close();
    }
    
}
