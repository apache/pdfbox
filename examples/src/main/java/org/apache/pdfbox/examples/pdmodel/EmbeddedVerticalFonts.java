/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class EmbeddedVerticalFonts
{
    private EmbeddedVerticalFonts()
    {
    }

    public static void main(String[] args) throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // The actual font file
        // Download: https://ipafont.ipa.go.jp/old/ipafont/ipag00303.php
        // (free license: https://www.gnu.org/licenses/license-list.html#IPAFONT)
        File ipafont = new File("ipag.ttf");

        // You can also use a Windows 7 TrueType font collection, e.g. MingLiU:
        // TrueTypeFont ttf = new TrueTypeCollection(new File("C:/windows/fonts/mingliu.ttc")).getFontByName("MingLiU")
        // PDType0Font.loadVertical(document, ttf, true)

        // Load as horizontal
        PDType0Font hfont = PDType0Font.load(document, ipafont);

        // Load as vertical
        PDType0Font vfont = PDType0Font.loadVertical(document, ipafont);

        // Load as vertical, but disable vertical glyph substitution
        // (You will usually not want this because it doesn't look good!)
        TrueTypeFont ttf = new TTFParser().parse(ipafont);
        PDType0Font vfont2 = PDType0Font.loadVertical(document, ttf, true);
        ttf.disableGsubFeature("vrt2");
        ttf.disableGsubFeature("vert");

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(hfont, 20);
        contentStream.setLeading(25);
        contentStream.newLineAtOffset(20, 300);
        contentStream.showText("Key:");
        contentStream.newLine();
        contentStream.showText("① Horizontal");
        contentStream.newLine();
        contentStream.showText("② Vertical with substitution");
        contentStream.newLine();
        contentStream.showText("③ Vertical without substitution");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(hfont, 20);
        contentStream.newLineAtOffset(20, 650);
        contentStream.showText("①「あーだこーだ」");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(vfont, 20);
        contentStream.newLineAtOffset(50, 600);
        contentStream.showText("②「あーだこーだ」");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(vfont2, 20);
        contentStream.newLineAtOffset(100, 600);
        contentStream.showText("③「あーだこーだ」");
        contentStream.endText();
        contentStream.close();
        // result file should look like the one attached to JIRA issue PDFBOX-4106
        document.save("vertical.pdf");
    }
}
