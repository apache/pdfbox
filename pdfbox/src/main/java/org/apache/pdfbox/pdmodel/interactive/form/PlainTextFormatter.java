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
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.Line;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.Paragraph;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.TextAttribute;
import org.apache.pdfbox.pdmodel.interactive.form.PlainText.Word;
import org.apache.pdfbox.util.Charsets;

/**
 * TextFormatter to handle plain text formatting.
 * 
 * The text formatter will take a single value or an array of values which
 * are treated as paragraphs.
 */

class PlainTextFormatter
{
    private AppearanceStyle appearanceStyle;
    private final boolean wrapLines;
    private final float width;
    private final OutputStream outputstream;
    private final PlainText textContent;
    
    // number format
    private final NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);
    
    static class Builder
    {

        // required parameters
        private OutputStream outputstream;

        // optional parameters
        private AppearanceStyle appearanceStyle;
        private boolean wrapLines = false;
        private float width = 0f;
        private PlainText textContent;
        
        public Builder(OutputStream outputstream)
        {
            this.outputstream = outputstream;
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

        Builder text(PlainText textContent)
        {
            this.textContent  = textContent;
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
        outputstream = builder.outputstream;
        textContent = builder.textContent;
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
            for (Paragraph paragraph : textContent.getParagraphs())
            {
                if (wrapLines)
                {
                    List<Line> lines = paragraph.getLines(
                                appearanceStyle.getFont(), 
                                appearanceStyle.getFontSize(), 
                                width
                            );
                    processLines(lines);
                }
                else
                {
                    showText(paragraph.getText(), appearanceStyle.getFont());
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
    private void processLines(List<Line> lines) throws IOException
    {
        PDFont font = appearanceStyle.getFont();
        float wordWidth = 0f;
        
        for (Line line : lines)
        {
           float offset = 0f;
           List<Word> words = line.getWords();
           for (Word word : words)
           {
               showText(word.getText(), font);
               wordWidth = (Float) word.getAttributes().getIterator().getAttribute(TextAttribute.WIDTH);
               
               if (words.indexOf(word) != words.size() -1)
               {
                   newLineAtOffset(wordWidth, 0f);
                   offset = offset + wordWidth;
               }
           }
           if (lines.indexOf(line) != lines.size()-1)
           {
               newLineAtOffset(-offset, -appearanceStyle.getLeading());
           }
        }
    }
    
    /**
     * Shows the given text at the location specified by the current text matrix.
     *
     * @param text The Unicode text to show.
     * @throws IOException if there is an error writing to the stream.
     */
    private void showText(String text, PDFont font) throws IOException
    {
        COSWriter.writeString(font.encode(text), outputstream);
        write(" ");
        writeOperator("Tj");
    }
    
    /**
     * Writes a string to the content stream as ASCII.
     */
    private void write(String text) throws IOException
    {
        outputstream.write(text.getBytes(Charsets.US_ASCII));
    }
    
    /**
     * Writes a string to the content stream as ASCII.
     */
    private void writeOperator(String text) throws IOException
    {
        outputstream.write(text.getBytes(Charsets.US_ASCII));
        outputstream.write('\n');
    }
    
    /**
     * The Td operator.
     * Move to the start of the next line, offset from the start of the current line by (tx, ty).
     *
     * @param tx The x translation.
     * @param ty The y translation.
     * @throws IOException if there is an error writing to the stream.
     */
    public void newLineAtOffset(float tx, float ty) throws IOException
    {
        writeOperand(tx);
        writeOperand(ty);
        writeOperator("Td");
    }
    
    /**
     * Writes a real real to the content stream.
     */
    private void writeOperand(float real) throws IOException
    {
        write(formatDecimal.format(real));
        outputstream.write(' ');
    }
}
