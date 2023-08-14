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
package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This test attempts to save different documents compressed, without causing errors, it also checks, whether the PDF is
 * readable after compression and whether some central contents are still contained after compression. Output files are
 * created in "target/test-output/compression/" source files are placed in "src/test/resources/input/compression/".
 *
 * @author Christian Appl
 */
class COSDocumentCompressionTest
{

    static final File inDir = new File("src/test/resources/input/compression/");
    static final File outDir = new File("target/test-output/compression/");

    public COSDocumentCompressionTest()
    {
        outDir.mkdirs();
    }

    /**
     * Compress a document, that contains acroform fields and touch the expected fields.
     *
     * @throws Exception Shall be thrown, when compressing the document failed.
     */
    @Test
    void testCompressAcroformDoc() throws Exception
    {
        File source = new File(inDir, "acroform.pdf");
        File target = new File(outDir, "acroform.pdf");

        PDDocument document = Loader.loadPDF(source);
        try
        {
            document.save(target);
        }
        finally
        {
            document.close();
        }

        document = Loader.loadPDF(target);
        try
        {
            assertEquals(1, document.getNumberOfPages(),
                    "The number of pages should not have changed, during compression.");
            PDPage page = document.getPage(0);
            List<PDAnnotation> annotations = page.getAnnotations();
            assertEquals(13, annotations.size(),
                    "The number of annotations should not have changed");
            assertEquals("TextField", annotations.get(0).getCOSObject().getNameAsString(COSName.T),
                    "The 1. annotation should have been a text field.");
            assertEquals("Button", annotations.get(1).getCOSObject().getNameAsString(COSName.T),
                    "The 2. annotation should have been a button.");
            assertEquals("CheckBox1", annotations.get(2).getCOSObject().getNameAsString(COSName.T),
                    "The 3. annotation should have been a checkbox.");
            assertEquals("CheckBox2", annotations.get(3).getCOSObject().getNameAsString(COSName.T),
                    "The 4. annotation should have been a checkbox.");
            assertEquals("TextFieldMultiLine",
                    annotations.get(4).getCOSObject().getNameAsString(COSName.T),
                    "The 5. annotation should have been a multiline textfield.");
            assertEquals("TextFieldMultiLineRT",
                    annotations.get(5).getCOSObject().getNameAsString(COSName.T),
                    "The 6. annotation should have been a multiline textfield.");
            assertNotNull(annotations.get(6).getCOSObject().getItem(COSName.PARENT),
                    "The 7. annotation should have had a parent entry.");
            assertEquals("GroupOption",
                    annotations.get(6).getCOSObject().getCOSDictionary(COSName.PARENT)
                            .getNameAsString(COSName.T),
                    "The 7. annotation's parent should have been a GroupOption.");
            assertNotNull(annotations.get(7).getCOSObject().getItem(COSName.PARENT),
                    "The 8. annotation should have had a parent entry.");
            assertEquals("GroupOption",
                    annotations.get(7).getCOSObject().getCOSDictionary(COSName.PARENT)
                            .getNameAsString(COSName.T),
                    "The 8. annotation's parent should have been a GroupOption.");
            assertEquals("ListBox", annotations.get(8).getCOSObject().getNameAsString(COSName.T),
                    "The 9. annotation should have been a ListBox.");
            assertEquals("ListBoxMultiSelect",
                    annotations.get(9).getCOSObject().getNameAsString(COSName.T),
                    "The 10. annotation should have been a ListBox Multiselect.");
            assertEquals("ComboBox", annotations.get(10).getCOSObject().getNameAsString(COSName.T),
                    "The 11. annotation should have been a ComboBox.");
            assertEquals("ComboBoxEditable",
                    annotations.get(11).getCOSObject().getNameAsString(COSName.T),
                    "The 12. annotation should have been a EditableComboBox.");
            assertEquals("Signature", annotations.get(12).getCOSObject().getNameAsString(COSName.T),
                    "The 13. annotation should have been a Signature.");
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Compress a document, that contains an attachment and touch the expected attachment.
     *
     * @throws Exception Shall be thrown, when compressing the document failed.
     */
    @Test
    void testCompressAttachmentsDoc() throws Exception
    {
        File source = new File(inDir, "attachment.pdf");
        File target = new File(outDir, "attachment.pdf");

        PDDocument document = Loader.loadPDF(source);
        try
        {
            document.save(target);
        }
        finally
        {
            document.close();
        }

        document = Loader.loadPDF(target);
        try
        {
            assertEquals(2, document.getNumberOfPages(),
                    "The number of pages should not have changed, during compression.");
            Map<String, PDComplexFileSpecification> embeddedFiles = document.getDocumentCatalog()
                    .getNames().getEmbeddedFiles().getNames();
            assertEquals(1, embeddedFiles.size(),
                    "The document should have contained an attachment");
            PDComplexFileSpecification attachment;
            assertNotNull((attachment = embeddedFiles.get("A4Unicode.pdf")),
                    "The document should have contained 'A4Unicode.pdf'.");
            assertEquals(14997, attachment.getEmbeddedFile().getLength(),
                    "The attachments length is not as expected.");
        }
        finally
        {
            document.close();
        }
    }

    /**
     * Compress and encrypt the given document, without causing an exception to be thrown.
     *
     * @throws Exception Shall be thrown, when compressing/encrypting the document failed.
     */
    @Test
    void testCompressEncryptedDoc() throws Exception
    {
        File source = new File(inDir, "unencrypted.pdf");
        File target = new File(outDir, "encrypted.pdf");

        PDDocument document = Loader.loadPDF(source, "user");
        try
        {
            document.protect(
                    new StandardProtectionPolicy("owner", "user", new AccessPermission(0)));
            document.save(target);
        }
        finally
        {
            document.close();
        }

        document = Loader.loadPDF(target, "user");
        // If this didn't fail, the encryption dictionary should be present and working.
        assertEquals(2, document.getNumberOfPages());
        document.close();
    }

    /**
     * Adds a page to an existing document, compresses it and touches the resulting page content stream.
     *
     * @throws Exception Shall be thrown, if compressing the document failed.
     */
    @Test
    void testAlteredDoc() throws Exception
    {
        File source = new File(inDir, "unencrypted.pdf");
        File target = new File(outDir, "altered.pdf");

        PDDocument document = Loader.loadPDF(source);
        try
        {
            PDPage page = new PDPage(new PDRectangle(100, 100));
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try
            {
                contentStream.beginText();
                contentStream.newLineAtOffset(20, 80);
                contentStream.setFont(new PDType1Font(FontName.HELVETICA), 12);
                contentStream.showText("Test");
                contentStream.endText();
            }
            finally
            {
                contentStream.close();
            }

            document.save(target);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            document.close();
        }

        document = Loader.loadPDF(target);
        try
        {
            assertEquals(3, document.getNumberOfPages(),
                    "The number of pages should not have changed, during compression.");
            PDPage page = document.getPage(2);
            assertEquals(43, page.getContentStreams().next().getLength(),
                    "The stream length of the new page is not as expected.");
        }
        finally
        {
            document.close();
        }
    }

}
