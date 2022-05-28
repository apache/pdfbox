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

/*XXX
Comaring Java AWT layout vector and AdvancedLayout: factor 50 = 1000/(20=fontSize), y different sign
Small diffenences, awt.Glyphlayout has data type float, AdvancedLayout integer

Comparison of glyph vector to hb-shape, see HarfBUzz documentation:
  hb_position_t x_offset;  how much the glyph moves on the X-axis before drawing it, this should not affect how much the line advances.
  hb_position_t y_offset;  how much the glyph moves on the Y-axis before drawing it, this should not affect how much the line advances.
  hb_position_t x_advance; how much the line advances after drawing this glyph when setting text in horizontal direction.
  hb_position_t y_advance; how much the line advances after drawing this glyph when setting text in vertical direction.

2022-05-28
  Error positioning double accents, e.g. "C̨̆" GlyphVectorAdvanced does not contain positioning information for the second accent.
*/
package org.apache.pdfbox.examples.pdmodel;

import org.apache.fontbox.ttf.advanced.api.GlyphVector;
import org.apache.fontbox.ttf.advanced.GlyphVectorAdvanced;
import org.apache.fontbox.ttf.advanced.api.AdvancedOTFParser;
import org.apache.fontbox.ttf.advanced.api.AdvancedOpenTypeFont;
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
import java.text.AttributedString;
import java.text.Bidi;

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
    public static String sequencesDin91379 = /*"xC̆C̨̆x"; */
             "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H \n"
            + "K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄ \n"
            + "T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀ \n"
            + "k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥̄ \n"
            + "s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ Ī́ ī́ Ž̦ \n"
            + "Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈ \n";
/**/

    private AdvancedTextLayoutSequencesDin91379()
    {
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Usage AdvancedTextLayoutSequencesDin91379 directory");
        }
        String dir = args[0];
        String[] fontFileNames = new String[] {
                "NotoSans-Regular.ttf",
           /*     "LiberationSans-Regular.ttf",
                "DejaVuSans.ttf",
                "IBMPlexSans-Regular.ttf",
                "SourceSans3-Regular.ttf",

            */
        };
        float[] fontSizes = new float[]{20f};

        for (float fontSize: fontSizes) {
            for (String fontFileName : fontFileNames) {
                try {
                    System.out.printf("--font:%s%n", fontFileName);
                    testJava2D(dir, fontFileName, fontSize, sequencesDin91379);
                    testAdvancedLayout(dir, fontFileName, fontSize, sequencesDin91379);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    public static void testJava2D(String dir, String fontFileName, float fontSize, String s) {

        try {
            PDDocument pdDocument = new PDDocument();
            PDPage page = new PDPage();
            pdDocument.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(pdDocument, pdDocument.getPage(0),
                    PDPageContentStream.AppendMode.APPEND, true);

            File fontFile = new File(dir + "/" + fontFileName );
            PDType0Font font = PDType0Font.load(pdDocument, fontFile);
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(fontSize);
            float x = page.getBBox().getLowerLeftX();
            float y = page.getBBox().getUpperRightY() - awtFont.getSize2D();
            System.out.printf("testJava2D ur=%f h=%f %n", page.getBBox().getUpperRightY(), awtFont.getSize2D());
            System.out.printf("testJava2D (x,y)=(%f, %f)%n", x, y);


            testSequences2D(cs, font, fontSize, awtFont, x, y, s);
            cs.close();
            pdDocument.save(String.format("%s/TestDin91379Java2D-%s-%s.pdf", dir, fontFileName, fontSize));
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
            y -= awtFont.getSize2D()*1.2f;
        }
    }

    public static void printAdjustments2D(String line, java.awt.font.GlyphVector awtGlyphVector, float fontSize) {
        int[] gids = awtGlyphVector.getGlyphCodes(0, awtGlyphVector.getNumGlyphs(), null);
        System.out.println("--Java2D--");
        System.out.println(line);
        float lastX = 0f;
        float lastY = 0f;
        float lastAx = 0f;
        float lastAy = 0f;
        for (int i=0; i<gids.length; i++) {
            System.out.printf("%d %d ", i, gids[i]);
            Point2D p = awtGlyphVector.getGlyphPosition(i);
            float ax =  awtGlyphVector.getGlyphMetrics(i).getAdvanceX();
            float ay =  awtGlyphVector.getGlyphMetrics(i).getAdvanceY();
            float factor = 1000f/fontSize; //XXX oder 72 ??
            System.out.printf("px=%f py=%f ", p.getX()*factor, p.getY()*factor);
            System.out.printf("ax=%f ay=%f ", ax*factor, ay*factor);
            System.out.printf("dx=%f dy=%f %n", (p.getX() - lastX -lastAx)*factor, (p.getY() - lastY -lastAy)*factor);
            System.out.println();
            lastX = (float) p.getX();
            lastY = (float) p.getY();
            lastAx = ax;
            lastAy = ay;
        }
    }


    /**
     * Positioning using an adapted version of the AWT layout vector
     * @param cs
     * @param font
     * @param fontSize
     * @param awtFont
     * @param line
     * @param x
     * @param y
     * @throws IOException
     */
    public static void testSequencesLine2D(PDPageContentStream cs, PDType0Font font,
                                          float fontSize, Font awtFont, String line, float x, float y) throws IOException {
        char[] chars = line.toCharArray();
        // Use Java2D to compute positioning of glyphs

        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), false, true);
        // specify fractional metrics to compute accurate positions
        AttributedString as = new AttributedString(line);
        Bidi bidi = new Bidi(as.getIterator());
        int localFlags = bidi.isLeftToRight() ? java.awt.Font.LAYOUT_LEFT_TO_RIGHT : java.awt.Font.LAYOUT_RIGHT_TO_LEFT;
        java.awt.font.GlyphVector awtGlyphVector = awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length, localFlags);
        printAdjustments2D(line, awtGlyphVector, fontSize);

        int[] glyphs = awtGlyphVector.getGlyphCodes(0, awtGlyphVector.getNumGlyphs(), null);
        int width = 0;
        int[][] adjustments = new int[awtGlyphVector.getNumGlyphs()][4];
        int lastX = 0;
        int lastAdvanceX = 0;
        for( int i=0; i<awtGlyphVector.getNumGlyphs(); i+=1) {
            int px = (int) (awtGlyphVector.getGlyphPosition(i).getX()*1000/fontSize);
            adjustments[i][0] = lastX = px - lastX - lastAdvanceX;
            adjustments[i][1] = -(int)Math.round(awtGlyphVector.getGlyphPosition(i).getY()*1000/fontSize);
            adjustments[i][2] = lastAdvanceX = (int) Math.round(awtGlyphVector.getGlyphMetrics(i).getAdvanceX()*1000/fontSize);
            adjustments[i][3] = (int) Math.round(awtGlyphVector.getGlyphMetrics(i).getAdvanceY()*1000/fontSize);
            lastX = px;
            lastAdvanceX = adjustments[i][2];
            width += adjustments[i][2];
        }
        GlyphVectorAdvanced vector = new GlyphVectorAdvanced(glyphs, width, adjustments);
        System.out.printf("2d GlyphVectorAdvanced=%s%n", vector);

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showGlyphVector(vector);
        cs.endText();
/* Alternate implementation using newLIneAtOffset for positioning
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
  */
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
                System.out.printf("px=%d py=%d ax=%d ay=%d%n",
                        adjustments[i][0], adjustments[i][1],
                        adjustments[i][2], adjustments[i][3]);
            } else {
                System.out.print("no adjustments ");
            }
            System.out.println();
        }
    }

    public static void testAdvancedLayout(String dir, String fontFileName, float fontSize, String s) throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            String fontFile = dir + "/" + fontFileName;

            AdvancedOTFParser fontParser = new AdvancedOTFParser();
            AdvancedOpenTypeFont otFont = fontParser.parse(fontFile);
            PDFont font = PDType0Font.load(document, otFont, true);

            GlyphVector vector;
            float x = page.getBBox().getLowerLeftX();
            float y = page.getBBox().getUpperRightY() - otFont.getFontBBox().getHeight()/72;
            System.out.printf("ur=%f h=%f %n", page.getBBox().getUpperRightY(), otFont.getFontBBox().getHeight());
            System.out.printf("(x,y)=(%f, %f)%n", x, y);


            try (PDPageContentStream stream = new PDPageContentStream(document, page))
            {
                stream.beginText();
                stream.setFont(font, fontSize);
                stream.newLineAtOffset(x, y);


                s = s.replaceAll("[ \t]", " ");
                String[] lines = s.split("\n");

                for (String line : lines) {
                    if (line.length() > 0) {
                        vector = otFont.createGlyphVector(line, (int)fontSize);
                        System.out.printf("fontbox vector=%s%n",vector.toString());
                        printAdjustmentsAdvancedLayout(line, vector);
                        stream.showGlyphVector(vector);
                    }
                    stream.newLineAtOffset(0, -24);
                    System.out.printf("(x,y)=(%f, %f) l=%s%n", x, y, line);
                }
                stream.endText();
            }
            document.save(String.format("%s/TestDin91379AdvancedLayout-%s-%s.pdf", dir, fontFileName, fontSize));
        }
    }
}
