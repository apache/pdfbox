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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.examples.pdmodel.CreatePDFA;
import org.apache.pdfbox.examples.util.PDFMergerExample;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.verapdf.core.VeraPDFException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;

/**
 *
 * @author Tilman Hausherr
 */
class MergePDFATest
{
    private final String outDir = "target/test-output";

    @BeforeEach
    protected void setUp()
    {
        new File(outDir).mkdirs();
    }

    @Test
    void testMergePDFA() throws IOException, TransformerException, VeraPDFException
    {
        System.out.println("testMergePDFA");
        String pdfaFilename = outDir + "/Source_PDFA.pdf";
        String pdfaMergedFilename = outDir + "/Merged_PDFA.pdf";
        String message = "The quick brown fox jumps over the lazy dog äöüÄÖÜß @°^²³ {[]}";
        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        String fontfile = dir + "LiberationSans-Regular.ttf";
        CreatePDFA.main(new String[] { pdfaFilename, message, fontfile });

        List<RandomAccessRead> sources = new ArrayList<>();
        sources.add(new RandomAccessReadBufferedFile(pdfaFilename));
        sources.add(new RandomAccessReadBufferedFile(pdfaFilename));
        InputStream is = new PDFMergerExample().merge(sources);
        try (FileOutputStream os = new FileOutputStream(pdfaMergedFilename))
        {
            is.transferTo(os);
        }
        sources.get(0).close();
        sources.get(1).close();

        // https://docs.verapdf.org/develop/
        VeraGreenfieldFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.fromString("1b");
        try (PDFAParser parser = Foundries.defaultInstance().createParser(new File(pdfaMergedFilename), flavour))
        {
            PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
            ValidationResult veraResult = validator.validate(parser);
            assertTrue(veraResult.isCompliant());
        }
    }
}
