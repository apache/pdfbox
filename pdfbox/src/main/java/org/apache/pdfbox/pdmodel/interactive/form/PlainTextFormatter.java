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
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.Line;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.Paragraph;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.TextAttribute;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.Word;

/**
 * TextFormatter to handle plain text formatting.
 * 
 * The text formatter will take a single value or an array of values which
 * are treated as paragraphs.
 */

class PlainTextFormatter
{
    
    enum TextAlign
    {
        LEFT(0), CENTER(1), RIGHT(2), JUSTIFY(4);
        
        private final int alignment;
        
        private TextAlign(int alignment)
        {
            this.alignment = alignment;
        }
        
        int getTextAlign()
        {
            return alignment;
        }
        
        public static TextAlign valueOf(int alignment)
        {
            for (TextAlign textAlignment : TextAlign.values())
            {
                if (textAlignment.getTextAlign() == alignment)
                {
                    return textAlignment;
                }
            }
            return TextAlign.LEFT;
        }
    }

    /**
     * The scaling factor for font units to PDF units
     */
    private static final int FONTSCALE = 1000;
    
    private final AppearanceStyle appearanceStyle;
    private final boolean wrapLines;
    private final float width;
    
    private final PDAppearanceContentStream contents;
    private final PlainText textContent;
    private final TextAlign textAlignment;
    
    private float horizontalOffset;
    private float verticalOffset;
    
    static class Builder
    {

        // required parameters
        private PDAppearanceContentStream contents;

        // optional parameters
        private AppearanceStyle appearanceStyle;
        private boolean wrapLines = false;
        private float width = 0f;
        private PlainText textContent;
        private TextAlign textAlignment = TextAlign.LEFT;
        
       
        // initial offset from where to start the position of the first line
        private float horizontalOffset = 0f;
        private float verticalOffset = 0f;
        
        Builder(PDAppearanceContentStream contents)
        {
            this.contents = contents;
        }

        Builder style(AppearanceStyle appearanceStyle)
        {
            this.appearanceStyle = appearanceStyle;
            return this;
        }
        
        Builder wrapLines(boolean wrapLines)
        {
            this.wrapLines = wrapLines;
            return this;
        }

        Builder width(float width)
        {
            this.width = width;
            return this;
        }

        Builder textAlign(int alignment)
        {
            this.textAlignment  = TextAlign.valueOf(alignment);
            return this;
        }
        
        Builder textAlign(TextAlign alignment)
        {
            this.textAlignment  = alignment;
            return this;
        }
        
        
        Builder text(PlainText textContent)
        {
            this.textContent  = textContent;
            return this;
        }
        
        Builder initialOffset(float horizontalOffset, float verticalOffset)
        {
            this.horizontalOffset = horizontalOffset;
            this.verticalOffset = verticalOffset;
            return this;
        }
        
        PlainTextFormatter build()
        {
            return new PlainTextFormatter(this);
        }
    }
    
    private PlainTextFormatter(Builder builder)
    {
        appearanceStyle = builder.appearanceStyle;
        wrapLines = builder.wrapLines;
        width = builder.width;
        contents = builder.contents;
        textContent = builder.textContent;
        textAlignment = builder.textAlignment;
        horizontalOffset = builder.horizontalOffset;
        verticalOffset = builder.verticalOffset;
    }
    
    /**
     * Format the text block.
     * 
     * @throws IOException if there is an error writing to the stream.
     */
    public void format() throws IOException
    {
        if (textContent != null && !textContent.getParagraphs().isEmpty())
        {
            boolean isFirstParagraph = true;
        	for (Paragraph paragraph : textContent.getParagraphs())
            {
                if (wrapLines)
                {
                    List<Line> lines = paragraph.getLines(
                                appearanceStyle.getFont(), 
                                appearanceStyle.getFontSize(), 
                                width
                            );
                    processLines(lines, isFirstParagraph);
                    isFirstParagraph = false;
                }
                else
                {
                    float startOffset = 0f;
                    
                    
                    float lineWidth = appearanceStyle.getFont().getStringWidth(paragraph.getText()) *
                            appearanceStyle.getFontSize() / FONTSCALE;
                    
                    if (lineWidth < width) 
                    {
                        switch (textAlignment)
                        {
                        case CENTER:
                            startOffset = (width - lineWidth)/2;
                            break;
                        case RIGHT:
                            startOffset = width - lineWidth;
                            break;
                        case JUSTIFY:
                        default:
                            startOffset = 0f;
                        }
                    }
                    
                    contents.newLineAtOffset(horizontalOffset + startOffset, verticalOffset);
                    contents.showText(paragraph.getText());
                }
            }
        }
    }

    /**
     * Process lines for output. 
     *
     * Process lines for an individual paragraph and generate the 
     * commands for the content stream to show the text.
     * 
     * @param lines the lines to process.
     * @throws IOException if there is an error writing to the stream.
     */
    private void processLines(List<Line> lines, boolean isFirstParagraph) throws IOException
    {
        float wordWidth = 0f;

        float lastPos = 0f;
        float startOffset = 0f;
        float interWordSpacing = 0f;
        
        for (Line line : lines)
        {
            switch (textAlignment)
            {
            case CENTER:
                startOffset = (width - line.getWidth())/2;
                break;
            case RIGHT:
                startOffset = width - line.getWidth();
                break;
            case JUSTIFY:
                if (lines.indexOf(line) != lines.size() -1)
                {
                    interWordSpacing = line.getInterWordSpacing(width);
                }
                break;
            default:
                startOffset = 0f;
            }
            
            float offset = -lastPos + startOffset + horizontalOffset;
            
            if (lines.indexOf(line) == 0 && isFirstParagraph)
            {
                contents.newLineAtOffset(offset, verticalOffset);
            }
            else
            {
                // keep the last position
                verticalOffset = verticalOffset - appearanceStyle.getLeading();
                contents.newLineAtOffset(offset, - appearanceStyle.getLeading());
            }

            lastPos += offset; 

            List<Word> words = line.getWords();
            for (Word word : words)
            {
                contents.showText(word.getText());
                wordWidth = (Float) word.getAttributes().getIterator().getAttribute(TextAttribute.WIDTH);
                if (words.indexOf(word) != words.size() -1)
                {
                    contents.newLineAtOffset(wordWidth + interWordSpacing, 0f);
                    lastPos = lastPos + wordWidth + interWordSpacing;
                }
            }
        }
        horizontalOffset = horizontalOffset - lastPos;
    }
}
