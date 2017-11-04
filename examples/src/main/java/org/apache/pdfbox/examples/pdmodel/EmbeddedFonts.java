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

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * An example of using an embedded TrueType font with Unicode text.
 *
 * @author Keiji Suzuki
 * @author John Hewson
 */
public final class EmbeddedFonts
{

    private EmbeddedFonts()
    {
    }
    
    public static void main(String[] args) throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
        PDType0Font font = PDType0Font.load(document, new File(dir + "LiberationSans-Regular.ttf"));

        PDPageContentStream stream = new PDPageContentStream(document, page);

        stream.beginText();
        stream.setFont(font, 12);
        stream.setLeading(12 * 1.2f);

        stream.newLineAtOffset(50, 600);
        stream.showText("PDFBox's Unicode with Embedded TrueType Font");
        stream.newLine();

        stream.showText("Supports full Unicode text ☺");
        stream.newLine();

        stream.showText("English русский язык Tiếng Việt");
        stream.newLine();

        // ligature
        stream.showText("Ligatures: \uFB01lm \uFB02ood");

        stream.endText();
        stream.close();

        document.save("example.pdf");
        document.close();
    }
}
