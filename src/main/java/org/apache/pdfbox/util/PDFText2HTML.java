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
 * Wrap stripped text in simple HTML, trying to form HTML paragraphs.
 * Paragraphs broken by pages, columns, or figures are not mended.
 *
 *
 * @author jjb - http://www.johnjbarton.com
 * @version  $Revision: 1.3 $
 */
public class PDFText2HTML extends PDFTextStripper
{
    private static final int INITIAL_PDF_TO_HTML_BYTES = 8192;

    private TextPosition beginTitle;
    private TextPosition afterEndTitle;
    private String titleGuess;
    private boolean suppressParagraphs;
    private boolean onFirstPage = true;

    /**
     * Constructor.
     *
     * @throws IOException If there is an error during initialization.
     */
    public PDFText2HTML() throws IOException
    {
        titleGuess = "";
        beginTitle = null;
        afterEndTitle = null;
        suppressParagraphs = false;
    }

    /**
     * Write the header to the output document.
     *
     * @throws IOException If there is a problem writing out the header to the document.
     */
    protected void writeHeader() throws IOException
    {
        StringBuffer buf = new StringBuffer(INITIAL_PDF_TO_HTML_BYTES);
        buf.append("<html><head>");
        buf.append("<title>");
        buf.append(getTitleGuess());
        buf.append("</title>");
        buf.append("</head>");
        buf.append("<body>\n");
        super.writeString(buf.toString());
    }

    /**
     * The guess to the document title.
     *
     * @return A string that is the title of this document.
     */
    protected String getTitleGuess()
    {
        return titleGuess;
    }


    /**
     * {@inheritDoc}
     */
    protected void writePage() throws IOException
    {
        Iterator textIter = getCharactersByArticle().iterator();

        if (onFirstPage)
        {
            guessTitle(textIter);
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
     * This method will attempt to guess the title of the document.
     *
     * @param textIter The characters on the first page.
     * @return The text position that is guessed to be the title.
     */
    protected TextPosition guessTitle(Iterator textIter)
    {
        float lastFontSize = -1.0f;
        int stringsInFont = 0;
        StringBuffer titleText = new StringBuffer();
        while (textIter.hasNext())
        {
            Iterator textByArticle = ((List)textIter.next()).iterator();
            while( textByArticle.hasNext() )
            {
                TextPosition position = (TextPosition) textByArticle.next();
                float currentFontSize = position.getFontSize();
                if (currentFontSize != lastFontSize)
                {
                    if (beginTitle != null)
                    { // font change in candidate title.
                        if (stringsInFont == 0)
                        {
                            beginTitle = null; // false alarm
                            titleText.setLength(0);
                        }
                        else
                        {
                            // had a significant font with some words: call it a title
                            titleGuess = titleText.toString();
                            afterEndTitle = position;
                            return beginTitle;
                        }
                    }
                    else
                    { // font change and begin == null
                        if (currentFontSize > 13.0f)
                        { // most body text is 12pt max I guess
                            beginTitle = position;
                        }
                    }

                    lastFontSize = currentFontSize;
                    stringsInFont = 0;
                }
                stringsInFont++;
                if (beginTitle != null)
                {
                    titleText.append(position.getCharacter()+" ");
                }
            }
        }
        return beginTitle; // null
    }

    /**
     * Write out the paragraph separator.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    protected void startParagraph() throws IOException
    {
        if (! suppressParagraphs)
        {
            super.writeString("<p>");
        }
    }
    /**
     * Write out the paragraph separator.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    protected void endParagraph() throws IOException
    {
        if (! suppressParagraphs)
        {
            super.writeString("</p>");
        }
    }

    /**
     * Write a string to the output stream and escape some HTML characters
     */
    protected void writeString(String chars) throws IOException
    {
        for (int i = 0; i < chars.length(); i++)
        {
            char c = chars.charAt(i);
            if ((c < 32) || (c > 126))
            {
                int charAsInt = c;
                super.writeString("&#" + charAsInt + ";");
            }
            else
            {
                switch (c)
                {
                case 34:
                    super.writeString("&quot;");
                    break;
                case 38:
                    super.writeString("&amp;");
                    break;
                case 60:
                    super.writeString("&lt;");
                    break;
                case 62:
                    super.writeString("&gt;");
                    break;
                default:
                    super.writeString(String.valueOf(c));
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void writeCharacters(TextPosition position ) throws IOException
    {
        if (position == beginTitle)
        {
            super.writeString("<H1>");
            suppressParagraphs = true;
        }
        if (position == afterEndTitle)
        {
            super.writeString("</H1>");  // end title and start first paragraph
            suppressParagraphs = false;
        }

        writeString(position.getCharacter());
    }
    

    /**
     * @return Returns the suppressParagraphs.
     */
    public boolean isSuppressParagraphs()
    {
        return suppressParagraphs;
    }
    /**
     * @param shouldSuppressParagraphs The suppressParagraphs to set.
     */
    public void setSuppressParagraphs(boolean shouldSuppressParagraphs)
    {
        this.suppressParagraphs = shouldSuppressParagraphs;
    }
}
