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
package org.apache.pdfbox.tools;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import junit.framework.TestCase;
import org.apache.pdfbox.text.PDFTextStripper;

public class TestPDFText2HTML extends TestCase
{

    private PDDocument createDocument(String title, PDFont font, String text) throws IOException
    {
        PDDocument doc = new PDDocument();
        doc.getDocumentInformation().setTitle(title);
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
        return doc;
    }

    public void testEscapeTitle() throws IOException
    {
        PDFTextStripper stripper = new PDFText2HTML();
        PDDocument doc = createDocument("<script>\u3042", PDType1Font.HELVETICA, "<foo>");
        String text = stripper.getText(doc);
       
        Matcher m = Pattern.compile("<title>(.*?)</title>").matcher(text);
        assertTrue(m.find());
        assertEquals("&lt;script&gt;&#12354;", m.group(1));
        assertTrue(text.contains("&lt;foo&gt;"));
    }

    public void testStyle() throws IOException
    {
        PDFTextStripper stripper = new PDFText2HTML();
        PDDocument doc = createDocument("t", PDType1Font.HELVETICA_BOLD, "<bold>");
        String text = stripper.getText(doc);

        Matcher bodyMatcher = Pattern.compile("<p>(.*?)</p>").matcher(text);
        assertTrue("body p exists", bodyMatcher.find());
        assertEquals("body p", "<b>&lt;bold&gt;</b>", bodyMatcher.group(1));
    }
}
