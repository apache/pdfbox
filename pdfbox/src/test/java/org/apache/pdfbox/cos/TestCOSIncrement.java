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
package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;

import org.junit.Test;

public class TestCOSIncrement
{
    @BeforeClass
    public static void init() throws Exception
    {
        new File("target/test-output").mkdirs();
    }

    /**
     * Check that subsetting takes place in incremental saving.
     *
     */
    @Test
    public void testSubsetting() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        document.save(baos);
        document.close();

        document = PDDocument.load(baos.toByteArray());

        page = document.getPage(0);

        PDFont font = PDType0Font.load(document, TestCOSIncrement.class.getResourceAsStream(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"));

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(75, 750);
        contentStream.showText("Apache PDFBox");
        contentStream.endText();
        contentStream.close();

        COSDictionary catalog = document.getDocumentCatalog().getCOSObject();
        catalog.setNeedToBeUpdated(true);
        COSDictionary pages = catalog.getCOSDictionary(COSName.PAGES);
        pages.setNeedToBeUpdated(true);
        page.getCOSObject().setNeedToBeUpdated(true);

        document.saveIncremental(new FileOutputStream("target/test-output/PDFBOX-5627.pdf"));
        document.close();

        document = PDDocument.load(new File("target/test-output/PDFBOX-5627.pdf"));
        page = document.getPage(0);
        COSName fontName = page.getResources().getFontNames().iterator().next();
        font = page.getResources().getFont(fontName);
        assertTrue(font.isEmbedded());
        document.close();
    }
}

