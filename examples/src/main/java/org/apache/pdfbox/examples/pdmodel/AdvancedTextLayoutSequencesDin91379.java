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

import org.apache.fontbox.ttf.GlyphVector;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OpenTypeFont;
import org.apache.fontbox.ttf.advanced.GlyphVectorAdvanced;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

/**
 * An example of using an embedded OpenType font with advanced glyph layout for
 * the sequences of DIN91379
 *
 *
 * @author Volker Kunert
 * @author Daniel Fickling
 * @see AdvancedTextLayout
 */
public final class AdvancedTextLayoutSequencesDin91379
{
    public static String sequencesDin91379 = "Sequences\n"
            + "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H n"
            + "K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄ \n"
            + "T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀ \n"
            + "k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥̄ \n"
            + "s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ Ī́ ī́ Ž̦ \n"
            + "Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈ \n";

    private AdvancedTextLayoutSequencesDin91379()
    {
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Usage AdvancedTextLayoutSequencesDin91379 directory");
        }
        try {
            String dir = args[0];

            //String fontFileName = "/NotoSans-Regular.ttf";
            //String fontFileName = "/LiberationSans-Regular.ttf";
            String fontFileName = "/DejaVuSans.ttf";

            testJava2D(dir, fontFileName, sequencesDin91379);
            testAdvancedLayout(dir, fontFileName, sequencesDin91379);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testJava2D(String dir, String fontFileName, String s) {

        try {
            PDDocument pdDocument = new PDDocument();
            PDPage blankPage = new PDPage();
            pdDocument.addPage(blankPage);
            PDPageContentStream cs = new PDPageContentStream(pdDocument, pdDocument.getPage(0),
                    PDPageContentStream.AppendMode.APPEND, true);

            File fontFile = new File(dir + "/" + fontFileName );
            PDType0Font font = PDType0Font.load(pdDocument, fontFile);
            float fontSize = 12;
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(fontSize);
            float x = blankPage.getBBox().getLowerLeftX();
            float y = blankPage.getBBox().getUpperRightY();
            testSequences2D(cs, font, fontSize, awtFont, x, y, s);
            cs.close();
            pdDocument.save(dir + "/TestDin91379Java2D.pdf");
            pdDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testSequences2D(PDPageContentStream cs, PDType0Font font,
                                      float fontSize, Font awtFont, float x, float y, String s) throws Exception {

        s = s.replaceAll("[ \t]", " ");
        String[] lines = s.split("\n");

        for (String line : lines) {
            if (line.length() > 0) {
                testSequencesLine2D(cs, font, fontSize, awtFont, line, x, y);
            }
            y -= awtFont.getSize2D() * 0.65;
        }
    }

    public static void printAdjustmentsAdvancedLayout(String line, java.awt.font.GlyphVector awtGlyphVector) {
        int[] gids = awtGlyphVector.getGlyphCodes(0, awtGlyphVector.getNumGlyphs(), null);
        System.out.println("--Java2D--");
        System.out.println(line);
        float lastX = 0f;
        float lastY = 0f;
        for (int i=0; i<gids.length; i++) {
            System.out.printf("%d %d ", i, gids[i]);
            Point2D p = awtGlyphVector.getGlyphPosition(i);
            float dx = (float) p.getX() - lastX;
            float dy = (float) p.getY() - lastY;
            float ax = (i == 0) ? 0.0f : awtGlyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float ay = (i == 0) ? 0.0f : awtGlyphVector.getGlyphMetrics(i - 1).getAdvanceY();
            System.out.printf("%f %f %f ", dx, ax, dx-ax);
            System.out.printf("%f %f %f\n", dy, ay, dy-ay);
            System.out.println();
            lastX = (float) p.getX();
            lastY = (float) p.getY();
        }
    }


    public static void testSequencesLine2D(PDPageContentStream cs, PDType0Font font,
                                          float fontSize, Font awtFont, String line, float x, float y) throws IOException {
        char[] chars = line.toCharArray();
        // Use Java2D to compute positioning of glyphs
        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), false, true);
        java.awt.font.GlyphVector glyphVector = awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length,
                Font.LAYOUT_LEFT_TO_RIGHT);
        printAdjustmentsAdvancedLayout(line, glyphVector);

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);

        float lastX = 0f;
        float lastY = 0f;
        for (int i = 0; i < line.length(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);

            float dx = (float) p.getX() - lastX;
            float dy = (float) p.getY() - lastY;

            cs.newLineAtOffset(dx, -dy);
            String text = line.substring(i, i + 1);

            cs.showText(text);

            lastX = (float) p.getX();
            lastY = (float) p.getY();
        }
        cs.endText();
    }

    private static Matrix createMatrix(float translateX, float translateY) {
        return Matrix.getTranslateInstance(translateX, translateY);
    }

    public static void printAdjustmentsAdvancedLayout(String line, GlyphVector vector) {
        GlyphVectorAdvanced vec = (GlyphVectorAdvanced) vector;
        int[] gids = vec.getGlyphArray();
        int[][] adjustments = vec.getAdjustments();
        System.out.println("--Advanced--");
        System.out.println(line);
        for (int i=0; i<gids.length; i++) {
            System.out.printf("%d %d ", i, gids[i]);
            if(adjustments!=null && adjustments[i]!=null) {
                for (int j = 0; j < adjustments[i].length; j++) {
                    System.out.printf("%d ", adjustments[i][j]);
                }
            } else {
                System.out.printf("no adjustments ");
            }
            System.out.println();
        }
    }

    public static void testAdvancedLayout(String dir, String fontFileName, String s) throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            String fontFile = dir + "/" + fontFileName;

            OTFParser fontParser = new OTFParser(true, false, true);
            OpenTypeFont otFont = fontParser.parse(fontFile);
            PDFont font = PDType0Font.load(document, otFont, true);

            GlyphVector vector;
            float x = 10;

            try (PDPageContentStream stream = new PDPageContentStream(document, page))
            {
                float fontSize = 20;
                stream.beginText();
                stream.setFont(font, fontSize);

                s = s.replaceAll("[ \t]", " ");
                String[] lines = s.split("\n");

                for (String line : lines) {
                    if (line.length() > 0) {
                        vector = otFont.createGlyphVector(line);
                        printAdjustmentsAdvancedLayout(line, vector);
                        stream.showGlyphVector(vector, createMatrix(x, 200));
                    }
                }
                stream.endText();
            }
            document.save("TestDin91379AdvancedLayout.pdf");
            document.close();
        }
    }
}
