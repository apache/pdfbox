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

import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Wrap stripped text in simple HTML, trying to form HTML paragraphs. Paragraphs
 * broken by pages, columns, or figures are not mended.
 *
 *
 * @author jjb - http://www.johnjbarton.com
 * @version $Revision: 1.3 $
 */
public class PDFText2HTML extends PDFTextStripper
{
    private static final int INITIAL_PDF_TO_HTML_BYTES = 8192;

    private boolean onFirstPage = true;

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
     * Escape some HTML characters.
     *
     * @param chars String to be escaped
     * @return returns escaped String.
     */
    private String escape(String chars)
    {
        StringBuilder builder = new StringBuilder(chars.length());
        for (int i = 0; i < chars.length(); i++)
        {
            char c = chars.charAt(i);
            // write non-ASCII as named entities
            if ((c < 32) || (c > 126))
            {
                int charAsInt = c;
                builder.append("&#").append(charAsInt).append(";");
            }
            else
            {
                switch (c)
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
                    builder.append(String.valueOf(c));
                }
            }
        }
        return builder.toString();
    }
}
