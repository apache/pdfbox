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
package org.apache.pdfbox.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;

/**
 * Wrap stripped text in simple HTML, trying to form HTML paragraphs. Paragraphs
 * broken by pages, columns, or figures are not mended.
 *
 * @author jjb - http://www.johnjbarton.com
 * 
 */
public class PDFText2HTML extends PDFTextStripper
{
    private static final int INITIAL_PDF_TO_HTML_BYTES = 8192;

    private boolean onFirstPage = true;
    private FontState fontState = new FontState();

    /**
     * Constructor.
     * @param encoding The encoding to be used
     * @throws IOException If there is an error during initialization.
     */
    public PDFText2HTML(String encoding) throws IOException
    {
        super(encoding);
        setLineSeparator(systemLineSeparator);
        setParagraphStart("<p>");
        setParagraphEnd("</p>"+systemLineSeparator);
        setPageStart("<div style=\"page-break-before:always; page-break-after:always\">");
        setPageEnd("</div>"+systemLineSeparator);
        setArticleStart(systemLineSeparator);
        setArticleEnd(systemLineSeparator);
    }

    /**
     * Write the header to the output document. Now also writes the tag defining
     * the character encoding.
     *
     * @throws IOException
     *             If there is a problem writing out the header to the document.
     */
    protected void writeHeader() throws IOException
    {
        StringBuffer buf = new StringBuffer(INITIAL_PDF_TO_HTML_BYTES);
        buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" + "\n"
                + "\"http://www.w3.org/TR/html4/loose.dtd\">\n");
        buf.append("<html><head>");
        buf.append("<title>" + escape(getTitle()) + "</title>\n");
        if(outputEncoding != null)
        {
            buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset="
                    + this.outputEncoding + "\">\n");
        }
        buf.append("</head>\n");
        buf.append("<body>\n");
        super.writeString(buf.toString());
    }

    /**
     * {@inheritDoc}
     */
    protected void writePage() throws IOException
    {
        if (onFirstPage)
        {
            writeHeader();
            onFirstPage = false;
        }
        super.writePage();
    }

    /**
     * {@inheritDoc}
     */
    public void endDocument(PDDocument pdf) throws IOException
    {
        super.writeString("</body></html>");
    }

    /**
     * This method will attempt to guess the title of the document using
     * either the document properties or the first lines of text.
     *
     * @return returns the title.
     */
    protected String getTitle()
    {
        String titleGuess = document.getDocumentInformation().getTitle();
        if(titleGuess != null && titleGuess.length() > 0)
        {
            return titleGuess;
        }
        else
        {
            Iterator<List<TextPosition>> textIter = getCharactersByArticle().iterator();
            float lastFontSize = -1.0f;

            StringBuffer titleText = new StringBuffer();
            while (textIter.hasNext())
            {
                Iterator<TextPosition> textByArticle = textIter.next().iterator();
                while (textByArticle.hasNext())
                {
                    TextPosition position = textByArticle.next();

                    float currentFontSize = position.getFontSize();
                    //If we're past 64 chars we will assume that we're past the title
                    //64 is arbitrary
                    if (currentFontSize != lastFontSize || titleText.length() > 64)
                    {
                        if (titleText.length() > 0)
                        {
                            return titleText.toString();
                        }
                        lastFontSize = currentFontSize;
                    }
                    if (currentFontSize > 13.0f)
                    { // most body text is 12pt
                        titleText.append(position.getCharacter());
                    }
                }
            }
        }
        return "";
    }


    /**
     * Write out the article separator (div tag) with proper text direction
     * information.
     *
     * @param isltr true if direction of text is left to right
     * @throws IOException
     *             If there is an error writing to the stream.
     */
    protected void startArticle(boolean isltr) throws IOException
    {
        if (isltr)
        {
            super.writeString("<div>");
        }
        else
        {
            super.writeString("<div dir=\"RTL\">");
        }
    }

    /**
     * Write out the article separator.
     *
     * @throws IOException
     *             If there is an error writing to the stream.
     */
    protected void endArticle() throws IOException
    {
        super.endArticle();
        super.writeString("</div>");
    }

    /**
     * Write a string to the output stream, maintain font state, and escape some HTML characters.
     * The font state is only preserved per word.
     *
     * @param text The text to write to the stream.
     * @param textPositions the corresponding text positions
     * @throws IOException If there is an error writing to the stream.
     */
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        super.writeString(fontState.push(text, textPositions));
    }

    /**
     * Write a string to the output stream and escape some HTML characters.
     *
     * @param chars String to be written to the stream
     * @throws IOException
     *             If there is an error writing to the stream.
     */
    protected void writeString(String chars) throws IOException
    {
        super.writeString(escape(chars));
    }

    /**
     * Writes the paragraph end "</p>" to the output. Furthermore, it will also clear the font state.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void writeParagraphEnd() throws IOException
    {
        writeString(fontState.clear());
        super.writeParagraphEnd();
    }

    /**
     * Escape some HTML characters.
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
        // write non-ASCII as named entities
        if ((character < 32) || (character > 126))
        {
            int charAsInt = character;
            builder.append("&#").append(charAsInt).append(";");
        }
        else
        {
            switch (character)
            {
            case 34:
                builder.append("&quot;");
                break;
            case 38:
                builder.append("&amp;");
                break;
            case 60:
                builder.append("&lt;");
                break;
            case 62:
                builder.append("&gt;");
                break;
            default:
                builder.append(String.valueOf(character));
            }
        }
    }

    /**
     * A helper class to maintain the current font state. It's public methods will emit opening and
     * closing tags as needed, and in the correct order.
     *
     * @author Axel Dörfler
     */
    private static class FontState
    {
        protected List<String> stateList = new ArrayList<String>();
        protected Set<String> stateSet = new HashSet<String>();

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
         * Closes all open states.
         * @return A string that contains the closing tags of all currently open states.
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
            
            buffer.append(bold ? open("b") : close("b"));
            buffer.append(italics ? open("i") : close("i"));
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
            return "<" + tag + ">";
        }

        private String closeTag(String tag)
        {
            return "</" + tag + ">";
        }

        private boolean isBold(PDFontDescriptor descriptor)
        {
            if (descriptor.isForceBold())
            {
                return true;
            }
            return descriptor.getFontName().contains("Bold");
        }

        private boolean isItalic(PDFontDescriptor descriptor)
        {
            if (descriptor.isItalic())
            {
                return true;
            }
            return descriptor.getFontName().contains("Italic");
        }
    }
}
