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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class CombAlignmentTest
{
    private static final File OUT_DIR = new File("target/test-output");
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "CombTest.pdf";
    private static final String TEST_VALUE = "1234567";

    @BeforeEach
    void setUp() throws IOException
    {
        OUT_DIR.mkdirs();
    }
    
    // PDFBOX-5256
    @Test
    void testCombFields() throws IOException
    {
        try (PDDocument document = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF)))
        {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDField field = acroForm.getField("PDFBoxCombLeft");
            field.setValue(TEST_VALUE);
            field = acroForm.getField("PDFBoxCombMiddle");
            field.setValue(TEST_VALUE);
            field = acroForm.getField("PDFBoxCombRight");
            field.setValue(TEST_VALUE);
            // compare rendering
            File file = new File(OUT_DIR, NAME_OF_PDF);
            document.save(file);
            if (!TestPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
            {
                // don't fail, rendering is different on different systems, result must be viewed manually
                System.err.println("Rendering of " + file +
                        " failed or is not identical to expected rendering in " + IN_DIR + " directory");
            }
        }
    }

    // PDFBOX-5784
    @Test
    void testPDFBOX5784() throws IOException
    {

        final String NAME_OF_PDF = "PDFBOX-5784.pdf";

        try (PDDocument document = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF)))
        {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            for (PDField field : acroForm.getFieldTree()) {
                if (!field.getPartialName().contains("acrobat")) {
                    field.setValue("WIaqg");
                }
            }
            // compare rendering
            File file = new File(OUT_DIR, NAME_OF_PDF);
            document.save(file);
            if (!TestPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
            {
                // don't fail, rendering is different on different systems, result must be viewed manually
                System.err.println("Rendering of " + file +
                        " failed or is not identical to expected rendering in " + IN_DIR + " directory");
            }
        }
    }
}
