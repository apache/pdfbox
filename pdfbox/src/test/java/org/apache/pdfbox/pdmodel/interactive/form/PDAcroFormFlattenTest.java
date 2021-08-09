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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test flatten different forms and compare with rendering.
 *
 * Some of the tests are currently disabled to not run within the CI environment
 * as the test results need manual inspection. Enable as needed.
 *
 */
@Execution(ExecutionMode.CONCURRENT)
class PDAcroFormFlattenTest
{

    private static final File IN_DIR = new File("target/test-output/flatten/in");
    private static final File OUT_DIR = new File("target/test-output/flatten/out");

    @BeforeAll
    static void setUp()
    {
        IN_DIR.mkdirs();
        OUT_DIR.mkdirs();
    }

    @ParameterizedTest
    @CsvSource({
        // PDFBOX-142 Filled template.
        // disabled as there is a small difference which can not be seen visually
        // "https://issues.apache.org/jira/secure/attachment/12742551/Testformular1.pdf,Testformular1.pdf",
        
        // PDFBOX-563 Filled template.
        // Disabled as there is a minimal difference which can not be seen visually on ci-builds
        // "https://issues.apache.org/jira/secure/attachment/12425859/TestFax_56972.pdf,TestFax_56972.pdf",
        
        // PDFBOX-2469 Empty template.
        "https://issues.apache.org/jira/secure/attachment/12682897/FormI-9-English.pdf,FormI-9-English.pdf",
        
        // PDFBOX-2469 Filled template.
        // Disabled as there is a minimal difference which can not be seen visually, see PDFBOX-5133
        // "https://issues.apache.org/jira/secure/attachment/12678455/testPDF_acroForm.pdf,testPDF_acroForm.pdf",
        
        //PDFBOX-2586 Empty template.
        "https://issues.apache.org/jira/secure/attachment/12689788/test.pdf,test-2586.pdf",
        
        // PDFBOX-3083 Filled template rotated.
        // disabled as there is a small difference which can not be seen visually
        // "https://issues.apache.org/jira/secure/attachment/12770263/mypdf.pdf,mypdf.pdf",

        // PDFBOX-3262 Hidden fields.
        "https://issues.apache.org/jira/secure/attachment/12792007/hidden_fields.pdf,hidden_fields.pdf",
        
        // PDFBOX-3396 Signed Document 1.
        "https://issues.apache.org/jira/secure/attachment/12816014/Signed-Document-1.pdf,Signed-Document-1.pdf",
        
        // PDFBOX-3396 Signed Document 2.
        "https://issues.apache.org/jira/secure/attachment/12816016/Signed-Document-2.pdf,Signed-Document-2.pdf",

        // PDFBOX-3396 Signed Document 3.
        "https://issues.apache.org/jira/secure/attachment/12821307/Signed-Document-3.pdf,Signed-Document-3.pdf",
        
        // PDFBOX-3396 Signed Document 4.
        "https://issues.apache.org/jira/secure/attachment/12821308/Signed-Document-4.pdf,Signed-Document-4.pdf",

        // PDFBOX-3587 Filled template.
        // disabled as there is a small difference which can not be seen visually
        // "https://issues.apache.org/jira/secure/attachment/12840280/OpenOfficeForm_filled.pdf,OpenOfficeForm_filled.pdf",

        // PDFBOX-4157 Filled template.
        // disabled as there is a small difference which can not be seen visually
        // "https://issues.apache.org/jira/secure/attachment/12976553/PDFBOX-4157-filled.pdf,PDFBOX-4157-filled.pdf",

        // PDFBOX-4172 Filled template.
        // disabled as there is a minimal difference which can not be seen visually
        // "https://issues.apache.org/jira/secure/attachment/12976552/PDFBOX-4172-filled.pdf,PDFBOX-4172-filled.pdf",

        // PDFBOX-4615 Filled template.
        // disabled as there is a minimal difference which can not be seen visually on ci-builds
        // "https://issues.apache.org/jira/secure/attachment/12976452/resetboundingbox-filled.pdf,PDFBOX-4615-filled.pdf",

        // PDFBOX-4693: page is not rotated, but the appearance stream is.
        "https://issues.apache.org/jira/secure/attachment/12986337/stenotypeTest-3_rotate_no_flatten.pdf,PDFBOX-4693-filled.pdf",
        
        // PDFBOX-4788: non-widget annotations are not to be removed on a page that has no widget
        // annotations.
        "https://issues.apache.org/jira/secure/attachment/12994791/flatten.pdf,PDFBOX-4788.pdf",
    
        // PDFBOX-4889: appearance streams with empty /BBox.
        "https://issues.apache.org/jira/secure/attachment/13005793/f1040sb%20test.pdf,PDFBOX-4889.pdf",

        // PDFBOX-4955: appearance streams with forms that are not used.
        "https://issues.apache.org/jira/secure/attachment/13011410/PDFBOX-4955.pdf,PDFBOX-4955.pdf",

        // PDFBOX-4958 text and button with image.
        // disabled as there is a minimal difference which can not be seen visually on ci-builds
        // "https://issues.apache.org/jira/secure/attachment/13012242/PDFBOX-4958.pdf,PDFBOX-4958-flattened.pdf"
    })
    void testFlatten(String sourceUrl, String targetFileName) throws IOException {
        flattenAndCompare(sourceUrl, targetFileName);
    }

    @Test
    void flattenSingleField() throws IOException
    {
        final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
        final String NAME_OF_PDF = "MultilineFields.pdf";

        PDDocument document = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF));
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        int numFieldsBefore = acroForm.getFields().size();
    
        List<PDField> toBeFlattened = new ArrayList<>();
        PDTextField field = (PDTextField) acroForm.getField("AlignLeft-Filled");
        toBeFlattened.add(field);
        acroForm.flatten(toBeFlattened,false);

        assertEquals(numFieldsBefore, acroForm.getFields().size() + 1, "the number of form fields shall be reduced by one");
        assertNull(acroForm.getField("AlignLeft-Filled"), "the flattened field shall no longer exist");

        // Store for manual comparison if needed
        // final File OUT_DIR = new File("target/test-output");
        // File file = new File(OUT_DIR, "MultilineFields-SingleFieldFlattened.pdf");
        // document.save(file);
    }

    @Test
    void flattenTestPDFBOX5254() throws IOException
    {
        String sourceUrl = "https://issues.apache.org/jira/secure/attachment/13005793/f1040sb%20test.pdf";
        String targetFileName = "PDFBOX-4889-5254.pdf";
        generateSamples(sourceUrl, targetFileName);

        File inputFile = new File(IN_DIR, targetFileName);
        File outputFile = new File(OUT_DIR, targetFileName);

        try (PDDocument testPdf = Loader.loadPDF(inputFile))
        {
            testPdf.getDocumentCatalog().getAcroForm().flatten();
            testPdf.setAllSecurityToBeRemoved(true);
            assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
            testPdf.save(outputFile, CompressParameters.NO_COMPRESSION);
        }

        // compare rendering
        if (!TestPDFToImage.doTestFile(outputFile, IN_DIR.getAbsolutePath(),
                OUT_DIR.getAbsolutePath()))
        {
            fail("Rendering of " + outputFile
                    + " failed or is not identical to expected rendering in " + IN_DIR
                    + " directory");
        }
        else
        {
            // cleanup input and output directory for matching files.
            removeAllRenditions(inputFile);
            inputFile.delete();
            outputFile.delete();
        }
    }

    /*
     * Flatten and compare with generated image samples.
     *
     * @throws IOException
     */
    private static void flattenAndCompare(String sourceUrl, String targetFileName) throws IOException
    {
        generateSamples(sourceUrl,targetFileName);

        File inputFile = new File(IN_DIR, targetFileName);
        File outputFile = new File(OUT_DIR, targetFileName);

        try (PDDocument testPdf = Loader.loadPDF(inputFile))
        {
            testPdf.getDocumentCatalog().getAcroForm().flatten();
            testPdf.setAllSecurityToBeRemoved(true);
            assertTrue(testPdf.getDocumentCatalog().getAcroForm().getFields().isEmpty());
            testPdf.save(outputFile);
        }

        // compare rendering
        if (!TestPDFToImage.doTestFile(outputFile, IN_DIR.getAbsolutePath(),
                OUT_DIR.getAbsolutePath()))
        {
            fail("Rendering of " + outputFile + " failed or is not identical to expected rendering in " + IN_DIR + " directory");
        }
        else
        {
            // cleanup input and output directory for matching files.
            removeAllRenditions(inputFile);
            inputFile.delete();
            outputFile.delete();
        }
    }

    /*
     * Generate the sample images to which the PDF will be compared after flatten.
     *
     * @throws IOException
     */
    private static void generateSamples(String sourceUrl, String targetFile) throws IOException
    {
        getFromUrl(sourceUrl, targetFile);

        File file = new File(IN_DIR,targetFile);

        try (PDDocument document = Loader.loadPDF(file, (String) null))
        {
            String outputPrefix = IN_DIR.getAbsolutePath() + '/' + file.getName() + "-";
            int numPages = document.getNumberOfPages();

            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < numPages; i++)
            {
                String fileName = outputPrefix + (i + 1) + ".png";
                BufferedImage image = renderer.renderImageWithDPI(i, 96); // Windows native DPI
                ImageIO.write(image, "PNG", new File(fileName));
            }
        }
    }

    /*
     * Get a PDF from URL and copy to file for processing.
     *
     * @throws IOException
     */
    private static void getFromUrl(String sourceUrl, String targetFile) throws IOException
    {
        try (InputStream is = new URL(sourceUrl).openStream())
        {
            Files.copy(is, new File(IN_DIR, targetFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /*
     * Remove renditions for the PDF from the input directory.
     * The output directory will have been cleaned by the TestPDFToImage utility.
     */
    private static void removeAllRenditions(final File inputFile)
    {
        File[] testFiles = inputFile.getParentFile().listFiles(
                (File dir, String name) -> 
                    (name.startsWith(inputFile.getName()) && name.toLowerCase().endsWith(".png")));

        Stream.of(testFiles).forEach(File::delete);
    }
}
