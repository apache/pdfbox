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
 *
 * Volker Kunert 2026
 */

package org.apache.pdfbox.examples.pdmodel.glyphposition;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;


import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedString;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Character positioning
 * (No glyph reordering or substitution)
 */
public class GlyphPositioner {
    private final Map<PDType0Font, Font> awtFontMap = new ConcurrentHashMap<>();
    private final PDPageContentStream contentStream;
    private PDType0Font font;
    private float fontSize;

    /**
     * Constructs a GlyphPositioner
     *
     */
    public GlyphPositioner(PDPageContentStream contentStream) {
        this.contentStream = contentStream;
    }

    /**
     * Checks if the glyphVector contains adjustments
     * that make advanced glyph layout necessary
     *
     * @param glyphVector glyph vector containing the positions
     * @return true if the glyphVector contains adjustments
     */
    private static boolean hasAdjustments(GlyphVector glyphVector) {
        boolean retVal = false;
        float lastX = 0f;
        float lastY = 0f;

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);
            float dx = (float) p.getX() - lastX;
            float dy = (float) p.getY() - lastY;

            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float ay = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceY();

            if (dx != ax || dy != ay) {
                retVal = true;
                break;
            }
            lastX = (float) p.getX();
            lastY = (float) p.getY();
        }
        return retVal;
    }

    /**
     * Loads the AWT font needed
     *
     * @param pdType0Font PDFBOX font
     * @param inputStream of the font file
     * @throws RuntimeException if font can not be loaded
     */
    public void loadAwtFont(PDType0Font pdType0Font, InputStream inputStream) {
        Font awtFont = null;
        try {
            if (!awtFontMap.containsKey(pdType0Font)) {
                awtFont = Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream);
                if (awtFont == null) {
                    throw new RuntimeException("Font is null");
                }
                awtFontMap.put(pdType0Font, awtFont);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("AWT Font creation failed for %s.", pdType0Font.getName()), e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    /**
     * Computes glyph positioning
     *
     * @param text input text
     * @return glyph vector containing reordered text, width and positioning info
     */
    public GlyphVector computeGlyphVector(String text) {
        char[] chars = text.toCharArray();

        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), false, true);
        // use fractional metrics
        AttributedString as = new AttributedString(text);
        Bidi bidi = new Bidi(as.getIterator());
        int localFlags = bidi.isLeftToRight() ? java.awt.Font.LAYOUT_LEFT_TO_RIGHT : java.awt.Font.LAYOUT_RIGHT_TO_LEFT;

        java.awt.Font awtFont = awtFontMap.get(font).deriveFont(fontSize);

        return awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length, localFlags);
    }


    /**
     * Sets the font and fontSize
     *
     * @param font     to be set
     * @param fontSize font size
     */
    public void setFontAndSize(PDType0Font font, float fontSize) {
        this.font = font;
        this.fontSize = fontSize;
    }

    /**
     * Shows a text using glyph positioning (if needed)
     *
     * @param text text to show
     * @throws IOException in case of IO-error
     */
    public void showText(String text) throws IOException {
        GlyphVector glyphVector = computeGlyphVector(text);
        if (!hasAdjustments(glyphVector)) {
            contentStream.showText(text);
            return;
        }

        final float delta = 1e-5f;
        final float factorX = 1000f / fontSize;
        float lastX = 0f;
        char[] characters = text.toCharArray();

        CharactersAndPositions ga = new CharactersAndPositions();

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);
            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float dx = (float) p.getX() - lastX - ax;
            float py = (float) p.getY();

            if (Math.abs(py) >= delta) {
                if (!ga.isEmpty()) {
                    showCharactersWithPositioning(contentStream, ga);
                    ga.clear();
                }
                contentStream.setTextRise(-py);
            }
            if (Math.abs(dx) >= delta) {
                ga.add(-dx * factorX);
            }

            // There is no method in PDPageContentStream to position
            // glyphs using the glyph id, so we have to use the
            // characters.
            ga.add(characters[i]);
            if (Math.abs(py) >= delta) {
                showCharactersWithPositioning(contentStream, ga);
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
        if (Math.abs(dx) >= delta) {
            ga.add(-dx * factorX);
        }
        showCharactersWithPositioning(contentStream, ga);
        ga.clear();
    }

    /**
     * Show the given characters at the given positions
     * @param contentStream to show the text
     * @param charactersAndPositions List of characters and positions
     * @throws IOException if an IO error occurs
     */
    public void showCharactersWithPositioning(PDPageContentStream contentStream, CharactersAndPositions charactersAndPositions) throws IOException {
        ArrayList<Object> textWithPositioning = new ArrayList<>();
        for (Object obj : charactersAndPositions.toArray()) {
            if (obj instanceof CharactersAndPositions.GlyphSubList) {
                CharactersAndPositions.GlyphSubList glyphSubList = (CharactersAndPositions.GlyphSubList) obj;
                StringBuilder sb = new StringBuilder();
                for (Object o : glyphSubList.toArray()) {
                    sb.append(o);
                }
                textWithPositioning.add(sb.toString());
            } else if (obj instanceof Float) {
                textWithPositioning.add(obj);
            } else {
                if (obj == null) {
                    throw new NullPointerException("Argument contains null entry");
                }
                throw new IllegalArgumentException("Argument must consist of array of Float and CharactersAndPositions.GlyphSubList types, not " + obj.getClass().getName());
            }
        }
        contentStream.showTextWithPositioning(textWithPositioning.toArray());
    }
}
