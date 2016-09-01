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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.IOException;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Arrays;
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
class PlainText
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
    PlainText(String textValue)
    {
        List<String> parts = Arrays.asList(textValue.replaceAll("\t", " ").split("\\r\\n|\\n|\\r|\\u2028|\\u2029"));
        paragraphs = new ArrayList<Paragraph>();
        for (String part : parts)
        {
        	// Acrobat prints a space for an empty paragraph
        	if (part.length() == 0)
        	{
        		part = " ";
        	}
            paragraphs.add(new Paragraph(part));
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
    PlainText(List<String> listValue)
    {
        paragraphs = new ArrayList<Paragraph>();
        for (String part : listValue)
        {
            paragraphs.add(new Paragraph(part));
        }
    }
    
    /**
     * Get the list of paragraphs.
     * 
     * @return the paragraphs.
     */
    List<Paragraph> getParagraphs()
    {
        return paragraphs;
    }
    
    /**
     * Attribute keys and attribute values used for text handling.
     * 
     * This is similar to {@link java.awt.font.TextAttribute} but
     * handled individually as to avoid a dependency on awt.
     * 
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
     * 
     */
    static class Paragraph
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
        List<Line> getLines(PDFont font, float fontSize, float width) throws IOException
        {
            BreakIterator iterator = BreakIterator.getLineInstance();
            iterator.setText(textContent);
            
            final float scale = fontSize/FONTSCALE;
            
            int start = iterator.first();
            int end = iterator.next();
            float lineWidth = 0;
            
            List<Line> textLines = new ArrayList<Line>();
            Line textLine = new Line();

            while (end != BreakIterator.DONE)
            {
                String word = textContent.substring(start,end);
                float wordWidth = font.getStringWidth(word) * scale;
                
                lineWidth = lineWidth + wordWidth;

                // check if the last word would fit without the whitespace ending it
                if (lineWidth >= width && Character.isWhitespace(word.charAt(word.length()-1)))
                {
                    float whitespaceWidth = font.getStringWidth(word.substring(word.length()-1)) * scale;
                    lineWidth = lineWidth - whitespaceWidth;
                }
                
                if (lineWidth >= width)
                {
                    textLine.setWidth(textLine.calculateWidth(font, fontSize));
                    textLines.add(textLine);
                    textLine = new Line();
                    lineWidth = font.getStringWidth(word) * scale;
                }
                
                AttributedString as = new AttributedString(word);
                as.addAttribute(TextAttribute.WIDTH, wordWidth);
                Word wordInstance = new Word(word);
                wordInstance.setAttributes(as);
                textLine.addWord(wordInstance);
                start = end;
                end = iterator.next();
            }
            textLine.setWidth(textLine.calculateWidth(font, fontSize));
            textLines.add(textLine);
            return textLines;
        }
    }

    /**
     * An individual line of text.
     */
    static class Line
    {
        private final List<Word> words = new ArrayList<Word>();
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
            for (Word word : words)
            {
                calculatedWidth = calculatedWidth + 
                        (Float) word.getAttributes().getIterator().getAttribute(TextAttribute.WIDTH);
                String text = word.getText();
                if (words.indexOf(word) == words.size() -1 && Character.isWhitespace(text.charAt(text.length()-1)))
                {
                    float whitespaceWidth = font.getStringWidth(text.substring(text.length()-1)) * scale;
                    calculatedWidth = calculatedWidth - whitespaceWidth;
                }
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
