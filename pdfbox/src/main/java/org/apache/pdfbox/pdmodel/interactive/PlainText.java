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
package org.apache.pdfbox.pdmodel.interactive;

import java.io.IOException;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * A block of text.
 * <p>
 * A block of text can contain multiple paragraphs which will
 * be treated individually within the block placement.
 * </p>
 * 
 */
public class PlainText
{
    private static final float FONTSCALE = 1000f;
    
    private final List<Paragraph> paragraphs;
    
    /**
     * Construct the text block from a single value.
     * 
     * Constructs the text block from a single value splitting
     * into individual {@link Paragraph} when a new line character is 
     * encountered.
     * 
     * @param textValue the text block string.
     */
    public PlainText(String textValue)
    {
        if (textValue.isEmpty())
        {
            paragraphs = new ArrayList<>(1);
            paragraphs.add(new Paragraph(""));
        }
        else
        {
            String[] parts = textValue.replace('\t', ' ').split("\\R");
            paragraphs = new ArrayList<>(parts.length);
            for (String part : parts)
            {
                // Acrobat prints a space for an empty paragraph
                if (part.isEmpty())
                {
                    part = " ";
                }
                paragraphs.add(new Paragraph(part));
            }
        }
    }
    
    /**
     * Construct the text block from a list of values.
     * 
     * Constructs the text block from a list of values treating each
     * entry as an individual {@link Paragraph}.
     * 
     * @param listValue the text block string.
     */
    public PlainText(List<String> listValue)
    {
        paragraphs = new ArrayList<>(listValue.size());
        listValue.forEach(part -> paragraphs.add(new Paragraph(part)));
    }
    
    /**
     * Get the list of paragraphs.
     * 
     * @return the paragraphs.
     */
    public List<Paragraph> getParagraphs()
    {
        return paragraphs;
    }
    
    /**
     * Attribute keys and attribute values used for text handling.
     * 
     * This is similar to {@link java.awt.font.TextAttribute} but
     * handled individually as to avoid a dependency on awt.
     */
    static class TextAttribute extends Attribute
    {
        /**
         * UID for serializing.
         */
        private static final long serialVersionUID = -3138885145941283005L;

        /**
         * Attribute width of the text.
         */
        public static final Attribute WIDTH = new TextAttribute("width");
        
        protected TextAttribute(String name)
        {
            super(name);
        }
        

    }

    /**
     * A block of text to be formatted as a whole.
     * <p>
     * A block of text can contain multiple paragraphs which will
     * be treated individually within the block placement.
     * </p>
     */
    public static class Paragraph
    {
        private final String textContent;

        Paragraph(String text)
        {
            textContent = text;
        }

        /**
         * Get the paragraph text.
         *
         * @return the text.
         */
        String getText()
        {
            return textContent;
        }

        /**
         * Break the paragraph into individual lines.
         *
         * @param font the font used for rendering the text.
         * @param fontSize the fontSize used for rendering the text.
         * @param width the width of the box holding the content.
         * @return the individual lines.
         * @throws IOException
         */
        public List<Line> getLines(PDFont font, float fontSize, float width) throws IOException
        {
            if (width <= 0)
            {
                return Collections.emptyList();
            }
            BreakIterator iterator = BreakIterator.getLineInstance();
            iterator.setText(textContent);

            final float scale = fontSize/FONTSCALE;

            int start = iterator.first();
            int end = iterator.next();
            float lineWidth = 0;

            List<Line> textLines = new ArrayList<>();
            Line textLine = new Line();

            while (end != BreakIterator.DONE)
            {
                String word = textContent.substring(start,end);
                float wordWidth = font.getStringWidth(word) * scale;

                boolean wordNeedsSplit = false;
                int splitOffset = end - start;

                lineWidth = lineWidth + wordWidth;

                // check if the last word would fit without the whitespace ending it
                if (lineWidth >= width && Character.isWhitespace(word.charAt(word.length()-1)))
                {
                    float whitespaceWidth = font.getStringWidth(word.substring(word.length()-1)) * scale;
                    lineWidth = lineWidth - whitespaceWidth;
                }

                if (lineWidth >= width && !textLine.getWords().isEmpty())
                {
                    textLine.setWidth(textLine.calculateWidth(font, fontSize));
                    textLines.add(textLine);
                    textLine = new Line();
                    lineWidth = font.getStringWidth(word) * scale;
                }

                if (word.length() > 1 && wordWidth > width && textLine.getWords().isEmpty())
                {
                    // Single word does not fit into the available width.
                    // PDFBOX-6082: at least 1 character must be placed per line.
                    wordNeedsSplit = true;

                    // PDFBOX-5049:  The original approach was to decrement splitOffset
                    // until the substring fits, but this can be very expensive for long words and 
                    // narrow widths (e.g. a long URL in a narrow column).
                    // 
                    // Optimization: instead of decrementing splitOffset one step at a time and
                    // calling getStringWidth on progressively shorter substrings:
                    //   - compute the scaled width of every individual character once
                    //   - build a prefix-sum array
                    //   - binary-search for the largest prefix that fits
                    // 
                    // TODO: The special case in PDFBOX-5049 should be handled by not generating an appearance
                    // stream at all as the the height of the text box is only 1pt and the text is not visible.

                    float[] prefixWidth = buildPrefixWidths(word, font, scale);
                    splitOffset = findMaxFittingChars(prefixWidth, width);

                    word      = word.substring(0, splitOffset);
                    wordWidth = prefixWidth[splitOffset];
                    lineWidth  = wordWidth;
                }

                AttributedString as = new AttributedString(word);
                as.addAttribute(TextAttribute.WIDTH, wordWidth);
                Word wordInstance = new Word(word);
                wordInstance.setAttributes(as);
                textLine.addWord(wordInstance);

                if (wordNeedsSplit)
                {
                    start = start + splitOffset;
                }
                else
                {
                    start = end;
                    end = iterator.next();
                }
            }
            textLine.setWidth(textLine.calculateWidth(font, fontSize));
            textLines.add(textLine);
            return textLines;
        }


        /**
         * Build the prefix-sum array of scaled character widths for the given word.
         *
         * @param word  the word to measure.
         * @param font  the font used to obtain glyph advance widths.
         * @param scale {@code fontSize / FONTSCALE}, pre-computed by the caller.
         * @return the {@code float[word.length() + 1]} prefix-sum array.
         * @throws IOException if the font cannot provide a glyph width.
         */
        private static float[] buildPrefixWidths(String word, PDFont font, float scale)
                throws IOException
        {
            int wordLen = word.length();
            float[] prefixWidth = new float[wordLen + 1];
            int i = 0;
            while (i < wordLen)
            {
                int codePoint = word.codePointAt(i);
                int charCount = Character.charCount(codePoint);
                // Measure this code point as a single string (handles surrogate pairs).
                float cpWidth = font.getStringWidth(word.substring(i, i + charCount)) * scale;
                // Propagate the cumulative width across all Java chars of this code point.
                for (int j = 0; j < charCount; j++)
                {
                    prefixWidth[i + j + 1] = prefixWidth[i + j] + (j == 0 ? cpWidth : 0f);
                }
                i += charCount;
            }
            return prefixWidth;
        }

        /**
         * Find the maximum number of Java chars from a prefix-width array that fit
         * within the given available width.
         * <p>
         * Binary search over the pre-computed {@code prefixWidth} array.
         * The result is always at least {@code 1} so that the caller is guaranteed to
         * make forward progress (PDFBOX-6082).
         * </p>
         *
         * @param prefixWidth array as returned by {@link #buildPrefixWidths}; length is
         *                    {@code wordLength + 1}.
         * @param width       the available line width in the same unit as the widths stored
         *                    in {@code prefixWidth}.
         * @return the largest index {@code k >= 1} such that {@code prefixWidth[k] < width},
         *         or {@code 1} if even a single character exceeds the available width.
         */
        private static int findMaxFittingChars(float[] prefixWidth, float width)
        {
            int lo = 1;
            int hi = prefixWidth.length - 1;
            while (lo < hi)
            {
                int mid = (lo + hi + 1) >>> 1; // upper-mid to avoid infinite loop
                if (prefixWidth[mid] < width)
                {
                    lo = mid;
                }
                else
                {
                    hi = mid - 1;
                }
            }
            return lo;
        }
    }
    
    /**
     * An individual line of text.
     */
    static class Line
    {
        private final List<Word> words = new ArrayList<>();
        private float lineWidth;

        float getWidth()
        {
            return lineWidth;
        }
        
        void setWidth(float width)
        {
            lineWidth = width;
        }
        
        float calculateWidth(PDFont font, float fontSize) throws IOException
        {
            final float scale = fontSize/FONTSCALE;
            float calculatedWidth = 0f;
            int indexOfWord = 0;
            for (Word word : words)
            {
                calculatedWidth = calculatedWidth + 
                        (Float) word.getAttributes().getIterator().getAttribute(TextAttribute.WIDTH);
                String text = word.getText();
                if (indexOfWord == words.size() -1 && Character.isWhitespace(text.charAt(text.length()-1)))
                {
                    float whitespaceWidth = font.getStringWidth(text.substring(text.length()-1)) * scale;
                    calculatedWidth = calculatedWidth - whitespaceWidth;
                }
                ++indexOfWord;
            }
            return calculatedWidth;
        }

        List<Word> getWords()
        {
            return words;
        }
        
        float getInterWordSpacing(float width)
        {
            return (width - lineWidth)/(words.size()-1);
        }

        void addWord(Word word)
        {
            words.add(word);
        }
    }
    
    /**
     * An individual word.
     * 
     * A word is defined as a string which must be kept
     * on the same line.
     */
    static class Word
    {
        private AttributedString attributedString;
        private final String textContent;
        
        Word(String text)
        {
            textContent = text;
        }
        
        String getText()
        {
            return textContent;
        }
        
        AttributedString getAttributes()
        {
            return attributedString;
        }
        
        void setAttributes(AttributedString as)
        {
            this.attributedString = as;
        }
    }
}
