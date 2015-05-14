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

package org.apache.pdfbox.pdmodel.font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Tests font embedding.
 *
 * @author John Hewson
 */
public class TestFontEmbedding extends TestCase
{
    private static final File OUT_DIR = new File("target/test-output");

    @Override
    protected void setUp() throws Exception
    {
        OUT_DIR.mkdirs();
    }

    /**
     * Embed a TTF as CIDFontType2.
     */
    public void testCIDFontType2() throws Exception
    {
        validateCIDFontType2(false);
    }

    /**
     * Embed a TTF as CIDFontType2 with subsetting.
     */
    public void testCIDFontType2Subset() throws Exception
    {
        validateCIDFontType2(true);
    }

    private void validateCIDFontType2(boolean useSubset) throws Exception
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        InputStream input = TestFontEmbedding.class.getClassLoader().getResourceAsStream(
                "org/apache/pdfbox/ttf/LiberationSans-Regular.ttf");
        PDType0Font font = PDType0Font.load(document, input, useSubset);

        PDPageContentStream stream = new PDPageContentStream(document, page);

        stream.beginText();
        stream.setFont(font, 12);

        String text = "Unicode русский язык Tiếng Việt";
        stream.newLineAtOffset(50, 600);
        stream.showText(text);

        stream.endText();
        stream.close();
        
        File file = new File(OUT_DIR, "CIDFontType2.pdf");
        document.save(file);
        document.close();

        // check that the extracted text matches what we wrote
        String extracted = getUnicodeText(file);
        assertEquals(text, extracted.trim());
    }

    private String getUnicodeText(File file) throws IOException
    {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    }
}
