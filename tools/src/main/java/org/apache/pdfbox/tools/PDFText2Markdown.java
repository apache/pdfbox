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
package org.apache.pdfbox.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Convert PDF text to Markdown format. Each line in the PDF is converted to a corresponding
 * Markdown paragraph. Bold and italic formatting is also applied based on font properties.
 *
 * @author Saurav Rawat
 */
public class PDFText2Markdown extends PDFTextStripper
{
    private final FontState fontState = new FontState();

    /**
     * Constructor.
     */
    public PDFText2Markdown()
    {
        setLineSeparator(LINE_SEPARATOR);
        setParagraphStart(LINE_SEPARATOR);
        setParagraphEnd(LINE_SEPARATOR);
        setPageStart(LINE_SEPARATOR);
        setPageEnd(LINE_SEPARATOR);
        setArticleStart(LINE_SEPARATOR);
        setArticleEnd(LINE_SEPARATOR);
    }

    /**
     * Escape some Markdown characters.
     *
     * @param chars String to be escaped
     * @return returns escaped String.
     */
    private static String escape(String chars)
    {
        StringBuilder builder = new StringBuilder(chars.length());
        for (int i = 0; i < chars.length(); i++)
        {
            appendEscaped(builder, chars.charAt(i));
        }
        return builder.toString();
    }

    private static void appendEscaped(StringBuilder builder, char character)
    {
        switch (character)
        {
            case '*':
            case '+':
            case '-':
            case '#':
                builder.append('\\').append(character);
                break;
            case 178:
                builder.append("<sup>2</sup>");
                break;
            case 179:
                builder.append("<sup>3</sup>");
                break;
            default:
                builder.append(character);
                break;
        }
    }

    /**
     * Write out the article separator with proper text direction information.
     *
     * @param isLTR true if direction of text is left to right
     * @throws IOException If there is an error writing to the stream.
     */
    @Override
    protected void startArticle(boolean isLTR) throws IOException
    {
        super.writeString(LINE_SEPARATOR);
    }

    /**
     * Write out the article separator.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    @Override
    protected void endArticle() throws IOException
    {
        super.endArticle();
        super.writeString(LINE_SEPARATOR);
    }

    /**
     * Write a string to the output stream, maintain font state, and escape some Markdown
     * characters. The font state is only preserved per word.
     *
     * @param text The text to write to the stream.
     * @param textPositions The corresponding text positions.
     * @throws IOException If there is an error writing to the stream.
     */
    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        super.writeString(fontState.push(text, textPositions));
    }

    /**
     * Write a string to the output stream and escape some Markdown characters.
     *
     * @param chars String to be written to the stream.
     * @throws IOException If there is an error writing to the stream.
     */
    @Override
    protected void writeString(String chars) throws IOException
    {
        super.writeString(escape(chars));
    }

    /**
     * Writes the Markdown paragraph end to the output. Furthermore, it will also clear the font
     * state.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected void writeParagraphEnd() throws IOException
    {
        // do not escape HTML
        super.writeString(fontState.clear());

        super.writeParagraphEnd();
    }

    /**
     * A helper class to maintain the current font state. Its public methods will emit opening and
     * closing tags as needed and in the correct order.
     * <p>
     * Responsible for applying Markdown formatting based on font properties. Supports bold and
     * italic text based on font descriptors.
     *
     * @author Axel Dörfler
     * @author Saurav Rawat
     */
    private static class FontState
    {
        private final List<String> stateList = new ArrayList<>();
        private final Set<String> stateSet = new HashSet<>();

        /**
         * Pushes new {@link TextPosition TextPositions} into the font state. The state is only
         * preserved correctly for each letter if the number of letters in <code>text</code> matches
         * the number of {@link TextPosition} objects. Otherwise, it's done once for the complete
         * array (just by looking at its first entry).
         *
         * @return A string that contains the text including tag changes caused by its font state.
         */
        public String push(String text, List<TextPosition> textPositions)
        {
            StringBuilder buffer = new StringBuilder();

            if (text.length() == textPositions.size())
            {
                // There is a 1:1 mapping, and we can use the TextPositions directly
                for (int i = 0; i < text.length(); i++)
                {
                    push(buffer, text.charAt(i), textPositions.get(i));
                }
            }
            else if (!text.isEmpty())
            {
                // The normalized text does not match the number of TextPositions, so we'll just
                // have a look at its first entry.
                // TODO change PDFTextStripper.normalize() such that it maintains the 1:1 relation
                if (textPositions.isEmpty())
                {
                    return text;
                }
                push(buffer, text.charAt(0), textPositions.get(0));
                buffer.append(escape(text.substring(1)));
            }
            return buffer.toString();
        }

        /**
         * Closes all open Markdown formatting.
         *
         * @return A string that contains the closing tags of all currently open Markdown
         * formatting.
         */
        public String clear()
        {
            StringBuilder buffer = new StringBuilder();
            closeUntil(buffer, null);
            stateList.clear();
            stateSet.clear();
            return buffer.toString();
        }

        protected String push(StringBuilder buffer, char character, TextPosition textPosition)
        {
            boolean bold = false;
            boolean italics = false;

            PDFontDescriptor descriptor = textPosition.getFont().getFontDescriptor();
            if (descriptor != null)
            {
                bold = isBold(descriptor);
                italics = isItalic(descriptor);
            }

            buffer.append(bold ? open("**") : close("**"));
            buffer.append(italics ? open("*") : close("*"));

            appendEscaped(buffer, character);

            return buffer.toString();
        }

        private String open(String tag)
        {
            if (stateSet.contains(tag))
            {
                return "";
            }
            stateList.add(tag);
            stateSet.add(tag);

            return openTag(tag);
        }

        private String close(String tag)
        {
            if (!stateSet.contains(tag))
            {
                return "";
            }
            // Close all tags until (but including) the one we should close
            StringBuilder tagsBuilder = new StringBuilder();
            int index = closeUntil(tagsBuilder, tag);

            // Remove from state
            stateList.remove(index);
            stateSet.remove(tag);

            // Now open the states that were closed but should remain open again
            for (; index < stateList.size(); index++)
            {
                tagsBuilder.append(openTag(stateList.get(index)));
            }
            return tagsBuilder.toString();
        }

        private int closeUntil(StringBuilder tagsBuilder, String endTag)
        {
            for (int i = stateList.size(); i-- > 0;)
            {
                String tag = stateList.get(i);
                tagsBuilder.append(closeTag(tag));
                if (endTag != null && tag.equals(endTag))
                {
                    return i;
                }
            }
            return -1;
        }

        private String openTag(String tag)
        {
            return tag;
        }

        private String closeTag(String tag)
        {
            return tag;
        }

        private boolean isBold(PDFontDescriptor descriptor)
        {
            if (descriptor.isForceBold())
            {
                return true;
            }
            return descriptor.getFontName().toLowerCase().contains("bold");
        }

        private boolean isItalic(PDFontDescriptor descriptor)
        {
            if (descriptor.isItalic())
            {
                return true;
            }
            String fontName = descriptor.getFontName().toLowerCase();
            return fontName.contains("italic") || fontName.contains("oblique");
        }
    }
}
