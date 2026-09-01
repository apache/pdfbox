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

package org.apache.pdfbox.glyphlayout.awt;


import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.pdfbox.pdmodel.AbstractGlyphLayoutProcessor;
import org.apache.pdfbox.pdmodel.ContentStreamForGlyphLayoutInterface;
import org.apache.pdfbox.pdmodel.GlyphLayoutProcessorInterface;
import org.apache.pdfbox.pdmodel.GlyphsAndPositions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;


/**
 * Processor for glyph layout
 * <p>
 * Use an object of this class only in one thread.
 *
 * @author Volker Kunert
 */
public class GlyphLayoutProcessorAwt extends AbstractGlyphLayoutProcessor implements GlyphLayoutProcessorInterface
{
    private final GlyphLayoutFontLoaderAwt glyphLayoutFontLoaderAwt;

    /**
     * Constructs a GlyphLayoutProcessorAwt
     *
     */
    public GlyphLayoutProcessorAwt()
    {
        this.glyphLayoutFontLoaderAwt = new GlyphLayoutFontLoaderAwt(this);
    }

    /**
     * Checks if glyphs needed for text are missing in awtFont
     *
     * @param text text to be checked
     * @param awtFont font to be checked
     * @throws IllegalArgumentException if glyphs are missing
     */
    public static void checkMissingGlyphs(String text, Font awtFont)
    {
        int firstMissingCharacter = awtFont.canDisplayUpTo(text);
        if (firstMissingCharacter != -1)
        {
            char c = text.charAt(firstMissingCharacter);
            int codepoint = text.codePointAt(firstMissingCharacter);

            throw new IllegalArgumentException(
                    String.format("Missing glyph in font '%s' for the character '%c', codePoint: %d (U+%04x).",
                            awtFont.getName(), c, codepoint, codepoint));
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
        return glyphLayoutFontLoaderAwt.supportsFont(font);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     *
     * @return a PDType0Font font.
     *
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream) throws IOException, FontFormatException
    {
        return glyphLayoutFontLoaderAwt.loadFont(pdDocument, inputStream);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param embedSubset must be false for PDF forms
     *
     * @return a PDType0Font font.
     *
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset)
            throws IOException, FontFormatException
    {
        return glyphLayoutFontLoaderAwt.loadFont(pdDocument, inputStream, embedSubset);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param fontOptions options for font loading
     *
     * @return a PDType0Font font.
     *
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream,
            GlyphLayoutFontLoaderAwt.FontOptions fontOptions) throws IOException, FontFormatException
    {
        return glyphLayoutFontLoaderAwt.loadFont(pdDocument, inputStream, fontOptions);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument document
     * @param inputStream of the font
     * @param embedSubset must be false for PDF forms
     * @param fontOptions options for font
     *
     * @return a PDType0Font font.
     *
     * @throws IOException if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset,
            GlyphLayoutFontLoaderAwt.FontOptions fontOptions) throws IOException, FontFormatException
    {
        return glyphLayoutFontLoaderAwt.loadFont(pdDocument, inputStream, embedSubset, fontOptions);
    }

    /**
     * Computes glyph positioning
     *
     * @param font to be used
     * @param fontSize font size
     * @param text text to show
     * @param bidiLevel as computed by Bidi class, even LTR, odd RTL
     *
     * @return an awt GlyphVector
     */
    protected GlyphVector computeGlyphVector(PDType0Font font, float fontSize, String text, int bidiLevel)
    {
        Objects.requireNonNull(font, "Font must be set");
        Objects.requireNonNull(text, "Text must be set");

        char[] chars = text.toCharArray();

        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), false, true);
        // use fractional metrics

        int localFlags = bidiLevel % 2 == 0 ? Font.LAYOUT_LEFT_TO_RIGHT : Font.LAYOUT_RIGHT_TO_LEFT;

        Font awtFont = glyphLayoutFontLoaderAwt.getAwtFont(font).deriveFont(fontSize);

        checkMissingGlyphs(text, awtFont);

        return awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length, localFlags);
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
    protected float getStringWidthUni(PDType0Font font, float fontSize, String text, int bidiLevel)
    {
        GlyphVector glyphVector = computeGlyphVector(font, fontSize, text, bidiLevel);
        Rectangle2D rect = glyphVector.getLogicalBounds();
        return (float) rect.getWidth();
    }


    /**
     * Shows a text using glyph positioning (if needed) This text must have a uniform run direction.
     *
     * @param contentStream the content stream
     * @param font to be used
     * @param fontSize font size
     * @param text text to show
     * @param bidiLevel as computed by Bidi class, even LTR, odd RTL
     * @throws IOException if an IO-exception occurs
     * @throws IllegalArgumentException if glyphs are missing
     */
    @Override
    protected void showTextUni(ContentStreamForGlyphLayoutInterface contentStream, PDType0Font font, float fontSize, String text, int bidiLevel) throws IOException
    {
        Objects.requireNonNull(text, "Text must be set");
        Objects.requireNonNull(contentStream, "contentStream must be set");

        GlyphVector glyphVector = computeGlyphVector(font, fontSize, text, bidiLevel);

        // check for adjustment not needed:
        // glyphVector.getLayoutFlags() & FLAG_HAS_POSITION_ADJUSTMENTS is always true
        // because of horizontal adjustments in every string except one character string

        final float delta = 1e-5f;
        final float factorX = 1000f / fontSize;
        float lastX = 0f;

        GlyphsAndPositions ga = new GlyphsAndPositions();

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++)
        {
            Point2D p = glyphVector.getGlyphPosition(i);
            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float dx = (float) p.getX() - lastX - ax;
            float py = (float) p.getY();

            if (Math.abs(py) >= delta)
            {
                if (!ga.isEmpty())
                {
                    contentStream.showGlyphsWithPositioning(ga);
                    ga.clear();
                }
                contentStream.setTextRise(-py);
            }
            if (Math.abs(dx) >= delta)
            {
                ga.add(-dx * factorX);
            }
            ga.add(glyphVector.getGlyphCode(i));
            if (Math.abs(py) >= delta)
            {
                contentStream.showGlyphsWithPositioning(ga);
                ga.clear();
                contentStream.setTextRise(0.0f);
            }
            lastX = (float) p.getX();
        }
        // adjust the end position
        Point2D p = glyphVector.getGlyphPosition(glyphVector.getNumGlyphs());
        float ax = (glyphVector.getNumGlyphs() == 0) ? 0.0f
                : glyphVector.getGlyphMetrics(glyphVector.getNumGlyphs() - 1).getAdvanceX();
        float dx = (float) p.getX() - lastX - ax;
        if (Math.abs(dx) >= delta)
        {
            ga.add(-dx * factorX);
        }
        contentStream.showGlyphsWithPositioning(ga);
        ga.clear();
    }
}
