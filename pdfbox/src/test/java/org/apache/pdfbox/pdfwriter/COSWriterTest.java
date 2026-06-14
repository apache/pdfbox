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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.multipdf.PageExtractor;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import org.junit.jupiter.api.Test;

class COSWriterTest
{
    /**
     * PDFBOX-4321: check whether the output stream is closed after saving.
     * 
     * @throws IOException
     */
    @Test
    void testPDFBox4321() throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            assertDoesNotThrow(() ->
            {
                doc.save(new BufferedOutputStream(new ByteArrayOutputStream(1024)
                {
                    @Override
                    public void close() throws IOException
                    {
                        throw new IOException("Stream was closed");
                    }
                }));
            });
        }
    }

    @Test
    void testPDFBox5485() throws IOException
    {
        File pdfFile = Paths.get("src", "test", "resources", "input", "PDFBOX-3110-poems-beads.pdf")
                .toFile();
        try (PDDocument pdfDocument = Loader.loadPDF(pdfFile))
        {
            PageExtractor pageExtractor = new PageExtractor(pdfDocument, 2, 2);
            try (PDDocument pdfPages = pageExtractor.extract())
            {
                assertDoesNotThrow(() -> pdfPages.save(OutputStream.nullOutputStream()));
            }
        }
    }

    @Test
    void testPDFBox5945() throws IOException
    {
        byte[] input = create();
        checkTrailerSize(input);

        byte[] output = edit(input);
        checkTrailerSize(output);
    }

    private static void checkTrailerSize(byte[] docData) throws IOException
    {
        try (PDDocument pdDocument = Loader.loadPDF(docData))
        {
            COSDocument cosDocument = pdDocument.getDocument();
            long maxObjNumber = cosDocument.getXrefTable().keySet().stream() //
                    .mapToLong(COSObjectKey::getNumber).max().getAsLong();
            long sizeFromTrailer = cosDocument.getTrailer().getLong(COSName.SIZE);
            assertEquals(maxObjNumber + 1, sizeFromTrailer);
        }
    }

    private static byte[] create() throws IOException
    {
        try (PDDocument pdDocument = new PDDocument())
        {
            PDAcroForm acroForm = new PDAcroForm(pdDocument);
            pdDocument.getDocumentCatalog().setAcroForm(acroForm);
            PDFont font1 = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont font2 = new PDType1Font(Standard14Fonts.FontName.ZAPF_DINGBATS);
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), font1);
            resources.put(COSName.getPDFName("ZaDb"), font2);
            acroForm.setDefaultResources(resources);
            PDPage page = new PDPage(PDRectangle.A4);
            pdDocument.addPage(page);
            PDTextField textField = new PDTextField(acroForm);
            textField.setPartialName("textFieldName");
            acroForm.getFields().add(textField);
            PDAnnotationWidget widget = textField.getWidgets().get(0);
            widget.setPage(page);
            page.getAnnotations().add(widget);
            PDRectangle rectangle = new PDRectangle(10, 200, 200, 15);
            widget.setRectangle(rectangle);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdDocument.save(out, CompressParameters.NO_COMPRESSION);
            return out.toByteArray();
        }
    }

    private static byte[] edit(byte[] input) throws IOException
    {
        try (PDDocument pdDocument = Loader.loadPDF(input))
        {
            PDTextField textField = (PDTextField) pdDocument.getDocumentCatalog().getAcroForm()
                    .getField("textFieldName");
            assertNotNull(textField);
            textField.setMultiline(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdDocument.saveIncremental(out);
            return out.toByteArray();
        }
    }

    /**
     * Test if overlapping object numbers are eliminated when merging pdfs.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testPDFBox6036() throws IOException, URISyntaxException
    {
        URL emptyURL = new URI(
                "https://issues.apache.org/jira/secure/attachment/13066015/empty.pdf").toURL();
        URL roboURL = new URI(
                "https://issues.apache.org/jira/secure/attachment/13066016/roboto-14.pdf").toURL();
        byte[] emptyPDF = null;
        byte[] roboPDF = null;
        try (InputStream isEmpty = emptyURL.openStream(); InputStream isRobo = roboURL.openStream())
        {
            emptyPDF = isEmpty.readAllBytes();
            roboPDF = isRobo.readAllBytes();
        }
        // write merge result using compressed streams
        ByteArrayOutputStream baosCompressed = new ByteArrayOutputStream();
        try (PDDocument targetDoc = Loader.loadPDF(emptyPDF);
                PDDocument doc2 = Loader.loadPDF(roboPDF))
        {
            PDPage sourcePage = doc2.getPage(0);
            targetDoc.importPage(sourcePage);
            targetDoc.save(baosCompressed);
        }
        try (PDDocument targetDoc = Loader.loadPDF(baosCompressed.toByteArray()))
        {
            assertNotNull(targetDoc.getDocumentCatalog().getStructureTreeRoot());
            PDResources res = targetDoc.getPage(1).getResources();
            assertEquals("BCDEEE+Roboto-Regular", res.getFont(COSName.getPDFName("F1")).getName());
            assertEquals("BCDFEE+Roboto-Regular", res.getFont(COSName.getPDFName("F2")).getName());
        }
        // write merge result without compressed streams
        ByteArrayOutputStream baosUncompressed = new ByteArrayOutputStream();
        try (PDDocument targetDoc = Loader.loadPDF(emptyPDF);
                PDDocument doc2 = Loader.loadPDF(roboPDF))
        {
            PDPage sourcePage = doc2.getPage(0);
            targetDoc.importPage(sourcePage);
            targetDoc.save(baosUncompressed, CompressParameters.NO_COMPRESSION);
        }
        try (PDDocument targetDoc = Loader.loadPDF(baosUncompressed.toByteArray()))
        {
            assertNotNull(targetDoc.getDocumentCatalog().getStructureTreeRoot());
            PDResources res = targetDoc.getPage(1).getResources();
            assertEquals("BCDEEE+Roboto-Regular", res.getFont(COSName.getPDFName("F1")).getName());
            assertEquals("BCDFEE+Roboto-Regular", res.getFont(COSName.getPDFName("F2")).getName());
        }

    }

}
