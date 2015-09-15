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
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Creates a simple document with a TrueType font.
 */
public final class HelloWorldTTF
{
    private HelloWorldTTF()
    {
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            System.err.println("usage: " + HelloWorldTTF.class.getName() +
                    " <output-file> <Message> <ttf-file>");
            System.exit(1);
        }

        String pdfPath = args[0];
        String message = args[1];
        String ttfPath = args[2];
        
        PDDocument doc = new PDDocument();
        try
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            
            PDFont font = PDType0Font.load(doc, new File(ttfPath));

            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(message);
            contents.endText();
            contents.close();
            
            doc.save(pdfPath);
            System.out.println(pdfPath + " created!");
        }
        finally
        {
            doc.close();
        }
    }
}
