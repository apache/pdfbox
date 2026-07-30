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
package org.apache.pdfbox.glyphlayout.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.glyphlayout.fop.GlyphLayoutProcessorFop;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Creates a simple document with a TrueType font using GlyphLayoutProcessorFop
 * adapted from org.apache.pdfbox.examples.pdmodel.HelloWorldTTF
 * <p>
 * This subproject is an alternative to the AWT subproject; DIN 91379 characters will work, but
 * complex scripts won't. For most people, the AWT project is the better choice.
 */
public class GlyphLayoutHelloWorldFOP
{

    public static void main(String[] args) throws IOException
    {
        new GlyphLayoutHelloWorldFOP().test(args);
    }

    public void test(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            System.err.println("Usage: " + this.getClass().getName() + " <output-file> <Message> <ttf-file>");
            System.exit(1);
        }

        String pdfPath = args[0];
        String message = args[1];
        String ttfPath = args[2];

        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            GlyphLayoutProcessorFop glyphLayoutProcessorFop = new GlyphLayoutProcessorFop();
            PDType0Font font;
            try (InputStream is = new FileInputStream(ttfPath))
            {
                font = glyphLayoutProcessorFop.loadFont(doc, is);
            }

            try (PDPageContentStream contents = new PDPageContentStream(doc, page))
            {
                contents.setGlyphLayoutProcessor(glyphLayoutProcessorFop);
                contents.beginText();
                contents.setFont(font, 20);
                contents.newLineAtOffset(100, 700);
                contents.showText(message);
                contents.endText();
            }
            doc.save(pdfPath);
            System.out.println(pdfPath + " created!");
        }
    }
}
