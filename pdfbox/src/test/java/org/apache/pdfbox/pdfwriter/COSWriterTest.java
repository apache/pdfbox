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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
            doc.save(new BufferedOutputStream(new ByteArrayOutputStream(1024)
            {
                @Override
                public void close() throws IOException
                {
                    throw new IOException("Stream was closed");
                }
            }));
        }
    }

    @Test
    void testPDFBox5485() throws Exception
    {
        File pdfFile = Paths.get("src", "test", "resources", "input", "PDFBOX-3110-poems-beads.pdf")
                .toFile();
        try (PDDocument pdfDocument = Loader.loadPDF(pdfFile))
        {
            PageExtractor pageExtractor = new PageExtractor(pdfDocument, 2, 2);
            try (PDDocument pdfPages = pageExtractor.extract())
            {
                pdfPages.save(new ByteArrayOutputStream());
            }
        }
    }

    @Test
    void testPDFBox5945() throws Exception
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

}
