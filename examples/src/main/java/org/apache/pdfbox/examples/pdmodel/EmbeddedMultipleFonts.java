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
import java.util.ArrayList;
import java.util.List;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;

/**
 * Output a text without knowing which font is the right one. One use case is a worldwide address
 * list. Only LTR languages are supported, RTL (e.g. Hebrew, Arabic) are not supported so they would
 * appear in the wrong direction. Complex scripts (Thai, Arabic, some Indian languages) are also not
 * supported, any output will look weird. There is an (unfinished) effort here:
 * https://issues.apache.org/jira/browse/PDFBOX-4189
 *
 * @author Tilman Hausherr
 */
public class EmbeddedMultipleFonts
{
    private EmbeddedMultipleFonts()
    {
    }

    public static void main(String[] args) throws IOException
    {
        PDDocument document = new PDDocument();

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDFont font1 = PDType1Font.HELVETICA; // always have a simple font as first one
        TrueTypeCollection ttc2 = new TrueTypeCollection(new File("c:/windows/fonts/batang.ttc"));
        PDType0Font font2 = PDType0Font.load(document, ttc2.getFontByName("Batang"), true); // Korean
        TrueTypeCollection ttc3 = new TrueTypeCollection(new File("c:/windows/fonts/mingliu.ttc"));
        PDType0Font font3 = PDType0Font.load(document, ttc3.getFontByName("MingLiU"), true); // Chinese
        PDType0Font font4 = PDType0Font.load(document, new File("c:/windows/fonts/mangal.ttf")); // Indian
        PDType0Font font5 = PDType0Font.load(document, new File("c:/windows/fonts/ArialUni.ttf")); // Fallback

        PDPageContentStream cs = new PDPageContentStream(document, page);

        cs.beginText();
        List<PDFont> fonts = new ArrayList<PDFont>();
        fonts.add(font1);
        fonts.add(font2);
        fonts.add(font3);
        fonts.add(font4);
        fonts.add(font5);
        cs.newLineAtOffset(20, 700);
        showTextMultiple(cs, "abc 한국 中国 भारत 日本 abc", fonts, 20);
        cs.endText();
        cs.close();

        document.save("example.pdf");
        document.close();
        
        ttc2.close();
        ttc3.close();
    }

    static void showTextMultiple(PDPageContentStream cs, String text, List<PDFont> fonts, float size)
            throws IOException
    {
        try
        {
            // first try all at once
            fonts.get(0).encode(text);
            cs.setFont(fonts.get(0), size);
            cs.showText(text);
            return;
        }
        catch (IllegalArgumentException ex)
        {
            // do nothing
        }
        // now try separately
        int i = 0;
        while (i < text.length())
        {
            boolean found = false;
            for (PDFont font : fonts)
            {
                try
                {
                    String s = text.substring(i, i + 1);
                    font.encode(s);
                    // it works! Try more with this font
                    int j = i + 1;
                    for (; j < text.length(); ++j)
                    {
                        String s2 = text.substring(j, j + 1);

                        if (isWinAnsiEncoding(s2.codePointAt(0)) && font != fonts.get(0))
                        {
                            // Without this segment, the example would have a flaw:
                            // This code tries to keep the current font, so
                            // the second "abc" would appear in a different font
                            // than the first one, which would be weird.
                            // This segment assumes that the first font has WinAnsiEncoding.
                            // (all static PDType1Font Times / Helvetica / Courier fonts)
                            break;
                        }
                        try
                        {
                            font.encode(s2);
                        }
                        catch (IllegalArgumentException ex)
                        {
                            // it's over
                            break;
                        }
                    }
                    s = text.substring(i, j);
                    cs.setFont(font, size);
                    cs.showText(s);
                    i = j;
                    found = true;
                    break;
                }
                catch (IllegalArgumentException ex)
                {
                    // didn't work, will try next font
                }
            }
            if (!found)
            {
                throw new IllegalArgumentException("Could not show '" + text.substring(i, i + 1)
                        + "' with the fonts provided");
            }
        }
    }

    static boolean isWinAnsiEncoding(int unicode)
    {
        String name = GlyphList.getAdobeGlyphList().codePointToName(unicode);
        if (".notdef".equals(name))
        {
            return false;
        }
        return WinAnsiEncoding.INSTANCE.contains(name);
    }
}
