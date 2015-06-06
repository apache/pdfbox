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
import org.apache.pdfbox.examples.pdmodel.CreatePDFA;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;

/**
 *
 * @author Tilman Hausherr
 */
public class CreatePDFATest extends TestCase
{
    private final String outDir = "target/test-output/";

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
        String message = "The quick brown fox jumps over the lazy dog äöüÄÖÜß @°^²³ {[]}";
        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        String fontfile = dir + "LiberationSans-Regular.ttf";
        CreatePDFA.main(new String[] { pdfaFilename, message, fontfile });
        
        PreflightParser preflightParser = new PreflightParser(new File(pdfaFilename));
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
    }
    
}
