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
package org.apache.pdfbox.glyphlayout.fop;

import java.io.IOException;
import java.io.InputStream;
import java.text.Bidi;
import java.util.Arrays;
import java.util.Objects;
import org.apache.fop.fonts.Font;

import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.traits.MinOptMax;

import org.apache.pdfbox.pdmodel.AbstractGlyphLayoutProcessor;
import org.apache.pdfbox.pdmodel.ContentStreamForGlyphLayoutInterface;
import org.apache.pdfbox.pdmodel.GlyphLayoutProcessorInterface;
import org.apache.pdfbox.pdmodel.GlyphsAndPositions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;


/**
 * Processor for glyph layout using Apache FOP
 * <p>
 * Use an object of this class only in one thread.
 *
 * @author Volker Kunert
 */
public class GlyphLayoutProcessorFop extends AbstractGlyphLayoutProcessor implements GlyphLayoutProcessorInterface
{

    private final GlyphLayoutFontLoaderFop glyphLayoutFontLoaderFop;

    /*
        Before you call GlyphMapping.doGlyphMapping to position the glyphs,
        the font size must be multiplied by this factor. Otherwise, the
        positioning is wrong.
     */
    private static final float FOP_FONTSIZE_FACTOR = 1000f;

    /**
     * Constructs a GlyphLayoutProcessorFop
     *
     */
    public GlyphLayoutProcessorFop()
    {
        this.glyphLayoutFontLoaderFop = new GlyphLayoutFontLoaderFop();
    }

    /**
     * Checks if glyphs needed for text are missing in t
     *
     * @param text text to be checked
     * @param mbf font to be checked
     * @throws IllegalArgumentException if glyphs are missing
     */
    public static void checkMissingGlyphs(String text, MultiByteFont mbf)
    {
        int[] codePoints = text.codePoints().toArray();
        for (int cp : codePoints)
        {
            if (mbf.findGlyphIndex(cp) == 0)
            {
                String s = new String(new int[]{cp}, 0, 1);
                throw new IllegalArgumentException(
                        String.format("Missing glyph in font '%s' for the character '%s', codePoint: %d (U+%04x).",
                                mbf.getFontName(), s, cp, cp));
            }
        }
    }

    /**
     * Checks if the font is supported
     * <p>
     * This class supports OpenType fonts with description of glyphs as TrueType outlines, i.e.
     * *.ttf-files. *.otf-files using CFF outlines are not supported by PDFBox
     *
     * @param font to be checked
     * @return true if glyph layout is supported for this font and this font is a PDType0Font
     */
    @Override
    public boolean supportsFont(PDFont font)
    {
        return glyphLayoutFontLoaderFop.supportsFont(font);
    }

    /**
     * Loads the font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param embedSubset must be false for PDF forms
     * @throws IOException if font can not be loaded
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset) throws IOException
    {
        return glyphLayoutFontLoaderFop.loadFont(pdDocument, inputStream, embedSubset);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @throws IOException if font can not be loaded
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream) throws IOException
    {
        return glyphLayoutFontLoaderFop.loadFont(pdDocument, inputStream);
    }


    /**
     * Compute the string width for a unidirectional string
     * @param font to be used
     * @param fontSize font size
     * @param text text
     * @param bidiLevel Bidi Level
     * @return string width
     */
    @Override
    protected float getStringWidthUni(PDType0Font font, float fontSize, String text, int bidiLevel) throws IOException {
        TextAndGpa textAndGpa = computeGlyphsAndPositions(font, fontSize, text, bidiLevel);
        return font.getStringWidth(textAndGpa.getText());
    }


    /**
     * Internal class for text, positioning and GPA
     */
    protected static class TextAndGpa {
        private final String text;
        private final boolean positioning;
        private final int[][] gpa;

        public TextAndGpa(String text, boolean positioning, int[][] gpa) {
            this.text = text;
            this.positioning = positioning;
            this.gpa = gpa;
        }

        public String getText() {
            return text;
        }

        public boolean hasPositioning() {
            return positioning;
        }

        public int[][] getGpa() {
            return gpa;
        }
    }

    /**
     * Computes glyph positioning
     *
     * @param font to be used
     * @param fontSize font size
     * @param text text to show
     * @param bidiLevel Bidi level
     */
    protected TextAndGpa computeGlyphsAndPositions(PDType0Font font, float fontSize, String text, int bidiLevel)
    {
        Objects.requireNonNull(font, "Font must be set");
        Objects.requireNonNull(text, "Text must be set");

        MultiByteFont mbf = glyphLayoutFontLoaderFop.getFopFont(font);

        checkMissingGlyphs(text, mbf);

        MinOptMax letterSpaceIPD = MinOptMax.getInstance(0);
        MinOptMax[] letterSpaceAdjustArray = new MinOptMax[text.length()];

        org.apache.fop.fonts.Font fopFont = new Font(mbf.getFontName(), null, mbf, (int) (fontSize * FOP_FONTSIZE_FACTOR));

        GlyphMapping glyphMapping = GlyphMapping.doGlyphMapping(
                new FopStringTextFragment(text), 0, text.length() - 1, fopFont,
                letterSpaceIPD, letterSpaceAdjustArray, ' ',
                ' ', //?
                false, bidiLevel, false, true, false);

        text = glyphMapping.mapping != null ? glyphMapping.mapping : text;
        boolean positioning = glyphMapping.gposAdjustments != null;
        int[][] gpa = positioning ? glyphMapping.gposAdjustments : createZeroGpa(text.length());

        if (bidiLevel % 2 == Bidi.DIRECTION_RIGHT_TO_LEFT)
        {
            gpa = reverseGpa(gpa);
            text = new StringBuilder(text).reverse().toString();
        }
        return new TextAndGpa(text, positioning, gpa);
    }

    /**
     * Converts the codepoints of the text to glyph ids
     *
     * @param font the font to be used
     * @param text the text to be converted
     * @return glyph ids with respect to font
     */
    protected int[] convertCharsToGlyphIds(PDType0Font font, String text)
    {
        MultiByteFont mbf = glyphLayoutFontLoaderFop.getFopFont(font);
        return text.codePoints().map(mbf::findGlyphIndex).toArray();
    }

    /**
     * Shows unidirectional text using positioning
     *
     * @param contentStream the content stream
     * @param font to be used
     * @param fontSize font size
     * @param text text to show
     * @param bidiLevel Bidi level*
     *
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if glyphs are missing
     */
    @Override
    protected void showTextUni(ContentStreamForGlyphLayoutInterface contentStream, PDType0Font font, float fontSize,
            String text, int bidiLevel) throws IOException
    {

        Objects.requireNonNull(text, "Text must be set");
        Objects.requireNonNull(contentStream, "contentStream must be set");

        TextAndGpa textAndGpa = computeGlyphsAndPositions(font, fontSize, text, bidiLevel);
        text = textAndGpa.getText();
        int[][] gpa = textAndGpa.getGpa();
        boolean hasPositioning = textAndGpa.hasPositioning();

        int[] glyphIds = convertCharsToGlyphIds(font, text);

        if (glyphIds.length != text.length() && hasPositioning)
        {
            // This case:
            // letters from the supplementary multilingual plane AND position adjustments
            // is not implemented
            throw new IllegalStateException("glyphIds.length != text.length() and gposAdjustments!=null"
                    + glyphIds.length + " " + text.length() + " " + text);
        }

        final float delta = Math.ulp(fontSize); // do only adjustments bigger than or equal to one ulp of the font size
        GlyphsAndPositions ga = new GlyphsAndPositions();

        for (int i = 0; i < glyphIds.length; i++)
        {
            // 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
            float px = gpa[i][0] / fontSize * 1000f / FOP_FONTSIZE_FACTOR;
            float py = gpa[i][1] / FOP_FONTSIZE_FACTOR;
            float ax = gpa[i][2] / fontSize * 1000f / FOP_FONTSIZE_FACTOR;
            // float ay = gpa[i][3] / FOP_FONTSIZE_FACTOR; // ignored in horizontal typesetting

            if (Math.abs(py) >= delta)
            {
                if (!ga.isEmpty())
                {
                    contentStream.showGlyphsWithPositioning(ga);
                    ga.clear();
                }
                contentStream.setTextRise(py);
            }
            if (Math.abs(px) >= delta)
            {
                ga.add(-px);
            }
            ga.add(glyphIds[i]);

            if (Math.abs(px) >= delta)
            {
                ga.add(px);
            }
            if (Math.abs(ax) >= delta)
            {
                ga.add(-ax);
            }
            if (Math.abs(py) >= delta)
            {
                contentStream.showGlyphsWithPositioning(ga);
                ga.clear();
                contentStream.setTextRise(0.0f);
            }
            if (Math.abs(ax) >= delta)
            {
                ga.add(ax);
            }
        }
        if (!ga.isEmpty())
        {
            contentStream.showGlyphsWithPositioning(ga);
            ga.clear();
        }
    }

    /**
     * Create a gpa with no corrections
     *
     * @param length of the text
     * @return gpa filled with zeroes
     */
    protected int[][] createZeroGpa(int length)
    {
        int[][] gpa = new int[length][];
        int[] z4 = new int[] { 0, 0, 0, 0 };
        Arrays.fill(gpa, z4);
        return gpa;
    }

    /**
     * Reverse gpa
     *
     * @param gpa positioning parameters
     * @return reversed gpa
     */
    protected int[][] reverseGpa(int[][] gpa)
    {
        int[][] reversed = new int[gpa.length][];

        for (int i = 0; i < gpa.length; i++)
        {
            int r = gpa.length - i - 1;
            reversed[r] = gpa[i];
        }
        return reversed;
    }
}
