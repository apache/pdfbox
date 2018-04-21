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

package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Inspired from <a href=
 * "https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/HelloWorldTTF.java?view=markup">PdfBox
 * Example</a>. This attempts to correctly demonstrate to what extent Bengali text rendering is
 * supported. First, we render some text, and then embed an image with the correct text displayed on
 * the next page.
 *
 * @author Palash Ray
 *
 */
public class BengaliPdfGenerationHelloWorld
{

    /**
     * The unicode of this is given below:
     * 
     * <pre>
     * \u0986\u09ae\u09bf  \u0995\u09cb\u09a8 \u09aa\u09a5\u09c7  \u0995\u09cd\u09b7\u09c0\u09b0\u09c7\u09b0 \u09b7\u09a8\u09cd\u09a1  \u09aa\u09c1\u09a4\u09c1\u09b2 \u09b0\u09c1\u09aa\u09cb  \u0997\u0999\u09cd\u0997\u09be \u098b\u09b7\u09bf
     * </pre>
     * 
     */
    private static final String BANGLA_TEXT_1 = "আমি কোন পথে ক্ষীরের লক্ষ্মী ষন্ড পুতুল রুপো গঙ্গা ঋষি";
    private static final String BANGLA_TEXT_2 = "দ্রুত গাঢ় শেয়াল অলস কুকুর জুড়ে জাম্প ধুর্ত  হঠাৎ ভাঙেনি মৌলিক ঐশি দৈ";
    private static final String BANGLA_TEXT_3 = "ঋষি কল্লোল ব্যাস নির্ভয় ";

    static
    {
        if (System.getProperty("java.version").startsWith("1.8"))
        {
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException
    {
        if (args.length != 1)
        {
            System.err.println(
                    "usage: " + BengaliPdfGenerationHelloWorld.class.getName() + " <output-file> ");
            System.exit(1);
        }

        String filename = args[0];

        System.out.println("The generated pdf filename is: " + filename);

        PDDocument doc = new PDDocument();
        try
        {

            PDPage page1 = new PDPage();
            doc.addPage(page1);

            PDFont font = PDType0Font.load(doc, BengaliPdfGenerationHelloWorld.class
                    .getResourceAsStream("/org/apache/pdfbox/resources/ttf/Lohit-Bengali.ttf"),
                    true);

            PDPageContentStream contents = new PDPageContentStream(doc, page1);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(10, 750);
            contents.showText(BANGLA_TEXT_1);
            contents.newLineAtOffset(0, -50);
            contents.showText(BANGLA_TEXT_2);
            contents.newLineAtOffset(0, -30);
            contents.showText(BANGLA_TEXT_3);
            contents.endText();

            PDImageXObject pdImage = PDImageXObject
                    .createFromFile(BengaliPdfGenerationHelloWorld.class
                            .getResource(
                                    "/org/apache/pdfbox/resources/ttf/bengali-correct-text.png")
                            // getFile() doesn't work if there is a space in the path
                            .toURI().getPath(), doc);
            contents.drawImage(pdImage, 0, 300, pdImage.getWidth(), pdImage.getHeight());
            contents.close();

            doc.save(filename);
        }
        finally
        {
            doc.close();
        }
    }

}
