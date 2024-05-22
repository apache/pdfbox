/*
 * Copyright 2024 The Apache Software Foundation.
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.TestCase.assertTrue;
import org.apache.pdfbox.examples.pdmodel.CreatePDFA;
import org.apache.pdfbox.examples.util.PDFMergerExample;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;

/**
 *
 * @author Tilman Hausherr
 */
public class MergePDFATest extends TestCase
{
    private final String outDir = "target/test-output";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        new File(outDir).mkdirs();
    }

    public void testMergePDFA() throws Exception
    {
        System.out.println("testMergePDFA");
        String pdfaFilename = outDir + "/Source_PDFA.pdf";
        String pdfaMergedFilename = outDir + "/Merged_PDFA.pdf";
        String message = "The quick brown fox jumps over the lazy dog äöüÄÖÜß @°^²³ {[]}";
        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        String fontfile = dir + "LiberationSans-Regular.ttf";
        CreatePDFA.main(new String[] { pdfaFilename, message, fontfile });

        List<InputStream> sources = new ArrayList<InputStream>();
        sources.add(new FileInputStream(pdfaFilename));
        sources.add(new FileInputStream(pdfaFilename));
        InputStream is = new PDFMergerExample().merge(sources);
        FileOutputStream os = new FileOutputStream(pdfaMergedFilename);
        IOUtils.copy(is, os);
        os.close();
        sources.get(0).close();
        sources.get(1).close();
        
        // Verify that it is PDF/A-1b
        PreflightParser preflightParser = new PreflightParser(new File(pdfaMergedFilename));
        preflightParser.parse();
        PreflightDocument preflightDocument = preflightParser.getPreflightDocument();
        preflightDocument.validate();
        ValidationResult result = preflightDocument.getResult();
        for (ValidationResult.ValidationError ve : result.getErrorsList())
        {
            System.err.println(ve.getErrorCode() + ": " + ve.getDetails());
        }
        assertTrue("PDF file created with MergePDFA is not valid PDF/A-1b", result.isValid());
        preflightDocument.close();

        // https://docs.verapdf.org/develop/
        VeraGreenfieldFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.fromString("1b");
        PDFAParser parser = Foundries.defaultInstance().createParser(new File(pdfaMergedFilename), flavour);
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
        org.verapdf.pdfa.results.ValidationResult veraResult = validator.validate(parser);
        assertTrue(veraResult.isCompliant());
        parser.close();
    }
}
