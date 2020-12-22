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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Inspired from <a href=
 * "https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/HelloWorldTTF.java?view=markup">PdfBox
 * Example</a>. This attempts to correctly demonstrate to what extent Bengali text rendering is
 * supported. We read large amount of text from a file and try to render it properly.
 *
 * @author Palash Ray
 *
 */
public class BengaliPdfGenerationHelloWorld
{
    private static final int LINE_GAP = 5;
    private static final String LOHIT_BENGALI_TTF = "/org/apache/pdfbox/resources/ttf/Lohit-Bengali.ttf";
    private static final String TEXT_SOURCE_FILE = "/org/apache/pdfbox/resources/ttf/bengali-samples.txt";
    private static final int FONT_SIZE = 20;
    private static final int MARGIN = 20;

    private BengaliPdfGenerationHelloWorld()
    {
    }

    public static void main(final String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println(
                    "usage: " + BengaliPdfGenerationHelloWorld.class.getName() + " <output-file> ");
            System.exit(1);
        }

        final String filename = args[0];

        System.out.println("The generated pdf filename is: " + filename);

        try (PDDocument doc = new PDDocument())
        {
            final PDFont font = PDType0Font.load(doc,
                    BengaliPdfGenerationHelloWorld.class.getResourceAsStream(LOHIT_BENGALI_TTF),
                    true);
            final PDRectangle rectangle = getPageSize();
            final float workablePageWidth = rectangle.getWidth() - 2 * MARGIN;
            final float workablePageHeight = rectangle.getHeight() - 2 * MARGIN;

            final List<List<String>> pagedTexts = getReAlignedTextBasedOnPageHeight(
                    getReAlignedTextBasedOnPageWidth(getBengaliTextFromFile(), font,
                            workablePageWidth),
                    font, workablePageHeight);

            for (final List<String> linesForPage : pagedTexts)
            {
                final PDPage page = new PDPage(getPageSize());
                doc.addPage(page);

                try (PDPageContentStream contents = new PDPageContentStream(doc, page))
                {
                    contents.beginText();
                    contents.setFont(font, FONT_SIZE);
                    contents.newLineAtOffset(rectangle.getLowerLeftX() + MARGIN,
                            rectangle.getUpperRightY() - MARGIN);

                    for (final String line : linesForPage)
                    {
                        contents.showText(line);
                        contents.newLineAtOffset(0, -(FONT_SIZE + LINE_GAP));
                    }

                    contents.endText();

                }
            }

            doc.save(filename);
        }
    }

    private static List<List<String>> getReAlignedTextBasedOnPageHeight(final List<String> originalLines,
                                                                        final PDFont font, final float workablePageHeight)
    {
        final float newLineHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000
                * FONT_SIZE + LINE_GAP;
        final List<List<String>> realignedTexts = new ArrayList<>();
        float consumedHeight = 0;
        List<String> linesInAPage = new ArrayList<>();
        for (final String line : originalLines)
        {
            if (newLineHeight + consumedHeight < workablePageHeight)
            {
                consumedHeight += newLineHeight;
            }
            else
            {
                consumedHeight = newLineHeight;
                realignedTexts.add(linesInAPage);
                linesInAPage = new ArrayList<>();
            }

            linesInAPage.add(line);
        }
        realignedTexts.add(linesInAPage);
        return realignedTexts;
    }

    private static List<String> getReAlignedTextBasedOnPageWidth(final List<String> originalLines,
                                                                 final PDFont font, final float workablePageWidth) throws IOException
    {
        final List<String> uniformlyWideTexts = new ArrayList<>();
        float consumedWidth = 0;
        StringBuilder sb = new StringBuilder();
        for (final String line : originalLines)
        {
            float newTokenWidth = 0;
            final StringTokenizer st = new StringTokenizer(line, " ", true);
            while (st.hasMoreElements())
            {
                final String token = st.nextToken();
                newTokenWidth = font.getStringWidth(token) / 1000 * FONT_SIZE;
                if (newTokenWidth + consumedWidth < workablePageWidth)
                {
                    consumedWidth += newTokenWidth;
                }
                else
                {
                    // add a new text chunk
                    uniformlyWideTexts.add(sb.toString());
                    consumedWidth = newTokenWidth;
                    sb = new StringBuilder();
                }

                sb.append(token);
            }

            // add a new text chunk
            uniformlyWideTexts.add(sb.toString());
            consumedWidth = newTokenWidth;
            sb = new StringBuilder();
        }

        return uniformlyWideTexts;
    }

    private static PDRectangle getPageSize()
    {
        return PDRectangle.A4;
    }

    private static List<String> getBengaliTextFromFile() throws IOException
    {
        final List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                BengaliPdfGenerationHelloWorld.class.getResourceAsStream(TEXT_SOURCE_FILE), StandardCharsets.UTF_8)))
        {
            while (true)
            {
                final String line = br.readLine();

                if (line == null)
                {
                    break;
                }

                if (!line.startsWith("#"))
                {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

}
