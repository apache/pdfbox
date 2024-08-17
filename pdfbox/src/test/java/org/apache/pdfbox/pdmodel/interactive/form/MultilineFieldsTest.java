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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MultilineFieldsTest
{
    private static final File OUT_DIR = new File("target/test-output");
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "MultilineFields.pdf";
    private static final String TEST_VALUE = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
            "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam";

    
    private PDDocument document;
    private PDAcroForm acroForm;

    @BeforeEach
    public void setUp() throws IOException
    {
        document = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF));
        acroForm = document.getDocumentCatalog().getAcroForm();
        OUT_DIR.mkdirs();
    }

    @Test
    void fillFields() throws IOException
    {
        PDTextField field = (PDTextField) acroForm.getField("AlignLeft");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignMiddle");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignRight");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignLeft-Border_Small");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignMiddle-Border_Small");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignRight-Border_Small");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignLeft-Border_Medium");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignMiddle-Border_Medium");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignRight-Border_Medium");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignLeft-Border_Wide");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignMiddle-Border_Wide");
        field.setValue(TEST_VALUE);

        field = (PDTextField) acroForm.getField("AlignRight-Border_Wide");
        field.setValue(TEST_VALUE);
        
        // compare rendering
        File file = new File(OUT_DIR, NAME_OF_PDF);
        document.save(file);
        if (!TestPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
        {
            // don't fail, rendering is different on different systems, result must be viewed manually
            System.err.println ("Rendering of " + file + " failed or is not identical to expected rendering in " + IN_DIR + " directory");
        }       
    }

    // Test for PDFBOX-3812
    @Test
    void testMultilineAuto() throws IOException
    {
        PDDocument document = Loader.loadPDF(new File(IN_DIR, "PDFBOX3812-acrobat-multiline-auto.pdf"));
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

        // Get and store the field sizes in the original PDF
        PDTextField fieldMultiline = (PDTextField) acroForm.getField("Multiline");
        float fontSizeMultiline = getFontSizeFromAppearanceStream(fieldMultiline);

        PDTextField fieldSingleline = (PDTextField) acroForm.getField("Singleline");
        float fontSizeSingleline = getFontSizeFromAppearanceStream(fieldSingleline);

        PDTextField fieldMultilineAutoscale = (PDTextField) acroForm.getField("MultilineAutoscale");
        float fontSizeMultilineAutoscale = getFontSizeFromAppearanceStream(fieldMultilineAutoscale);

        PDTextField fieldSinglelineAutoscale = (PDTextField) acroForm.getField("SinglelineAutoscale");
        float fontSizeSinglelineAutoscale = getFontSizeFromAppearanceStream(fieldSinglelineAutoscale);

        fieldMultiline.setValue("Multiline - Fixed");
        fieldSingleline.setValue("Singleline - Fixed");
        fieldMultilineAutoscale.setValue("Multiline - auto");
        fieldSinglelineAutoscale.setValue("Singleline - auto");

        assertEquals(fontSizeMultiline, getFontSizeFromAppearanceStream(fieldMultiline), 0.001f);
        assertEquals(fontSizeSingleline, getFontSizeFromAppearanceStream(fieldSingleline), 0.001f);
        assertEquals(fontSizeMultilineAutoscale, getFontSizeFromAppearanceStream(fieldMultilineAutoscale), 0.001f);
        assertEquals(fontSizeSinglelineAutoscale, getFontSizeFromAppearanceStream(fieldSinglelineAutoscale), 0.025f);
    }

    // Test for PDFBOX-3835
    @Test
    void testMultilineBreak() throws IOException
    {
        final String TEST_PDF = "PDFBOX-3835-input-acrobat-wrap.pdf";
        try (PDDocument document = Loader.loadPDF(new File(IN_DIR, TEST_PDF)))
        {
            PDAcroForm localAcroForm = document.getDocumentCatalog().getAcroForm();
            
            // Get and store the field sizes in the original PDF
            PDTextField fieldInput = (PDTextField) localAcroForm.getField("filled");
            String fieldValue = fieldInput.getValue();
            List<String> acrobatLines = getTextLinesFromAppearanceStream(fieldInput);
            fieldInput.setValue(fieldValue);
            List<String> pdfboxLines = getTextLinesFromAppearanceStream(fieldInput);
            assertEquals(acrobatLines.size(),pdfboxLines.size(), "Number of lines generated by PDFBox shall match Acrobat");
            for (int i = 0; i < acrobatLines.size(); i++)
            {
                assertEquals(acrobatLines.get(i).length(), pdfboxLines.get(i).length(), "Number of characters per lines generated by PDFBox shall match Acrobat");
            }
        }
    }


    private float getFontSizeFromAppearanceStream(PDField field) throws IOException
    {
        PDAnnotationWidget widget = field.getWidgets().get(0);
        PDFStreamParser parser = new PDFStreamParser(widget.getNormalAppearanceStream());
        
        Object token = parser.parseNextToken();
                
        while (token != null)
        {
            if (token instanceof COSName && ((COSName) token).getName().equals("Helv"))
            {
                token = parser.parseNextToken();
                if (token instanceof COSNumber)
                {
                    return ((COSNumber) token).floatValue();
                }
            }
            token = parser.parseNextToken();
        }
        return 0;
    }

    private List<String> getTextLinesFromAppearanceStream(PDField field) throws IOException
    {
        PDAnnotationWidget widget = field.getWidgets().get(0);
        PDFStreamParser parser = new PDFStreamParser(widget.getNormalAppearanceStream());
        
        Object token = parser.parseNextToken();
        
        List<String> lines = new ArrayList<>();
                
        while (token != null)
        {
            if (token instanceof COSString)
            {
                lines.add(((COSString) token).getString());
            }
            token = parser.parseNextToken();
        }
        return lines;
    }

    
    @AfterEach
    public void tearDown() throws IOException
    {
        document.close();
    }
    
}
