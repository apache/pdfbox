/*
 *  Copyright 2011 adam.
 * 
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

package org.apache.pdfbox.pdmodel.font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author adam
 */
public class PDFontTest
{

    /**
     * Test of the error reported in PDFBOX-988
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testPDFBox988() throws IOException, URISyntaxException
    {
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load(new File(PDFontTest.class.getResource("F001u_3_7j.pdf").toURI()));
            PDFRenderer renderer = new PDFRenderer(doc);
            renderer.renderImage(0);
            // the allegation is that renderImage() will crash the JVM or hang
        }
        finally
        {
            if (doc != null)
            {
                doc.close();
            }
        }
    }

    /**
     * PDFBOX-3337: Test ability to reuse a TrueTypeFont for several PDFs to avoid parsing it over
     * and over again.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox3337() throws IOException
    {
        InputStream ttfStream = PDFontTest.class.getClassLoader().getResourceAsStream(
                "org/apache/pdfbox/ttf/LiberationSans-Regular.ttf");
        final TrueTypeFont ttf = new TTFParser ().parse (ttfStream);

        for (int i = 0; i < 2; ++i)
        {
            PDDocument doc = new PDDocument();

            final PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            PDFont font = PDType0Font.load(doc, ttf, true);
            cs.setFont(font, 10);
            cs.beginText();
            cs.showText("PDFBOX");
            cs.endText();
            cs.close();
            doc.save(new ByteArrayOutputStream());
            doc.close();
        }
    }

    /**
     * PDFBOX-3747: Test that using "-" with Calibri in Windows 7 has "-" in text extraction and not
     * \u2010, which was because of a wrong ToUnicode mapping because prior to the bugfix,
     * CmapSubtable#getCharCodes provided values in random order.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox3747() throws IOException
    {
        File file = new File("c:/windows/fonts", "calibri.ttf");
        if (!file.exists())
        {
            System.out.println("testPDFBox3747 skipped");
            return;
        }
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDFont font = PDType0Font.load(doc, file);
        
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        cs.beginText();
        cs.setFont(font, 10);
        cs.showText("PDFBOX-3747");
        cs.endText();
        cs.close();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        
        doc = PDDocument.load(baos.toByteArray());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        Assert.assertEquals("PDFBOX-3747", text.trim());
        doc.close();
    }
}
