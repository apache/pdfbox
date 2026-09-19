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

package org.apache.pdfbox.pdmodel;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract super class for classes implementing GlyphLayoutProcessorInterface
 *
 * @author Volker Kunert
 */
public abstract class AbstractGlyphLayoutProcessor implements GlyphLayoutProcessorInterface
{
    /**
     *
     */
    private final boolean useActualText;

    /**
     * Options for GlyphLayoutProcessor
     */
    public static class GlyphLayoutProcessorOptions {
        private boolean useActualText;

        /**
         * Turn usage of ActualText on
         * @return this
         */
        public GlyphLayoutProcessorOptions useActualText() {
            useActualText = true;
            return this;
        }

        /**
         * Returns the state of useActualText
         * @return true, if ActualText is used, falls otherwise
         */
        public boolean getUseActualText() {
            return useActualText;
        }
    }

    /**
     * Class for text and Bidi-Level
     */
    protected static class TextAndBidiLevel
    {
        private final String text;
        private final int bidiLevel;

        TextAndBidiLevel(String text, int bidiLevel)
        {
            this.text = text;
            this.bidiLevel = bidiLevel;
        }

        public String getText()
        {
            return text;
        }

        public int getBidiLevel()
        {
            return bidiLevel;
        }
    }

    /**
     * Creates an AbstractGlyphLayoutProcessor with the given options
     * @param options Options for creation
     */
    public AbstractGlyphLayoutProcessor(GlyphLayoutProcessorOptions options) {
        useActualText = options.useActualText;
    }

    /**
     * Creates an AbstractGlyphLayoutProcessor with standard options
     */
    public AbstractGlyphLayoutProcessor() {
        useActualText = false;
    }


    /**
     * Compute the string width for a unidirectional string
     * @param font to be used
     * @param fontSize font size
     * @param text text
     * @param bidiLevel Bidi Level
     * @return string width
     */
    protected abstract float getStringWidthUni(PDType0Font font, float fontSize, String text, int bidiLevel)
            throws IOException;


    /**
     * Compute the width for a text
     * @param font to be used
     * @param fontSize font size
     * @param text text
     * @return string width
     */
    public float getStringWidth(PDType0Font font, float fontSize, String text) throws IOException
    {
        float width = 0f;
        List<TextAndBidiLevel> textAndBidiLevels = doBidiSplittingAndReordering(text);
        for (TextAndBidiLevel textAndBidiLevel:  textAndBidiLevels)
        {
            width += getStringWidthUni(font, fontSize, textAndBidiLevel.getText(), textAndBidiLevel.getBidiLevel());
        }
        return width;
    }


    /**
     * Begin a marked content sequence for ActualText
     *
     * @param contentStream the content stream
     * @param text the text to be written as ActualText
     * @throws IOException If the content stream could not be written
     */
    protected void beginMarkedContentForActualText(ContentStreamForGlyphLayoutInterface contentStream, String text) throws IOException {
        COSDictionary dict = new COSDictionary();
        dict.setItem(COSName.ACTUAL_TEXT, new COSString(text));
        PDPropertyList propertyList = PDPropertyList.create(dict);
        contentStream.beginMarkedContent(COSName.SPAN, propertyList);
    }

    /**
     * End a marked content sequence.
     *
     * @throws IOException If the content stream could not be written
     */
    protected  void endMarkedContent(ContentStreamForGlyphLayoutInterface contentStream) throws IOException {
        contentStream.endMarkedContent();
    }

    /**
     * Shows unidirectional text using glyph positioning (if needed)
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
    protected abstract void showTextUni(ContentStreamForGlyphLayoutInterface contentStream, PDType0Font font, float fontSize,
                               String text, int bidiLevel) throws IOException;

    /**
     * Shows a text using glyph positioning (if needed)
     *
     * @param contentStream the content stream
     * @param font to be used
     * @param fontSize font size
     * @param text text to show
     *
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if glyphs are missing
     */
    public void showText(ContentStreamForGlyphLayoutInterface contentStream, PDType0Font font, float fontSize, String text)
            throws IOException
    {
        if (useActualText) {
            beginMarkedContentForActualText(contentStream, text);
        }
        List<TextAndBidiLevel> textAndBidiLevels = doBidiSplittingAndReordering(text);
        for (TextAndBidiLevel textAndBidiLevel:  textAndBidiLevels)
        {
            showTextUni(contentStream, font, fontSize, textAndBidiLevel.getText(), textAndBidiLevel.getBidiLevel());
        }
        if (useActualText) {
            endMarkedContent(contentStream);
        }
    }

    /**
     * Do Bidi splitting and reordering
     * @param text text
     * @return list of texts and bidi levels
     */
    protected List<TextAndBidiLevel> doBidiSplittingAndReordering(String text)
    {
        ArrayList<TextAndBidiLevel> textAndBidiLevels = new ArrayList<>();

        Objects.requireNonNull(text, "Text must be set");

        if (Bidi.requiresBidi(text.toCharArray(), 0, text.length()))
        {
            Bidi bidi = new Bidi(text, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            if (bidi.isMixed())
            {
                // Split and Reorder
                // See PDFTextStripper.handleDirection
                // collect individual bidi information
                int runCount = bidi.getRunCount();
                byte[] levels = new byte[runCount];
                Integer[] runs = new Integer[runCount];

                for (int i = 0; i < runCount; i++)
                {
                    levels[i] = (byte) bidi.getRunLevel(i);
                    runs[i] = i;
                }
                // reorder individual parts based on their levels
                Bidi.reorderVisually(levels, 0, runs, 0, runCount);

                for (int i = 0; i < runCount; i++)
                {
                    int index = runs[i];
                    int start = bidi.getRunStart(index);
                    int limit = bidi.getRunLimit(index);
                    int bidiLevel = levels[index];
                    String part = text.substring(start, limit);
                    textAndBidiLevels.add(new TextAndBidiLevel(part, bidiLevel));
                }
            }
            else
            {
                textAndBidiLevels.add(new TextAndBidiLevel(text, bidi.getBaseLevel()));
            }
        }
        else
        {
            textAndBidiLevels.add(new TextAndBidiLevel(text, Bidi.DIRECTION_LEFT_TO_RIGHT));
        }
        return Collections.unmodifiableList(textAndBidiLevels);
    }
}
