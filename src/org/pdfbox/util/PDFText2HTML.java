/**
 * Copyright (c) 2003-2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.util;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import org.pdfbox.pdmodel.PDDocument;

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
        getOutput().write(buf.toString());
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
    protected void flushText() throws IOException 
    {
        Iterator textIter = getCharactersByArticle().iterator();
      
        if (onFirstPage) 
        {
            guessTitle(textIter);
            writeHeader();
            onFirstPage = false;
        }
        super.flushText();
    }
    
    /**
     * {@inheritDoc}
     */
    public void endDocument(PDDocument pdf) throws IOException 
    {
        output.write("</body></html>");      
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
            getOutput().write("<p>");
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
            getOutput().write("</p>");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void writeCharacters(TextPosition position ) throws IOException 
    {
        if (position == beginTitle) 
        {
            output.write("<H1>");
            suppressParagraphs = true;
        } 
        if (position == afterEndTitle) 
        {
            output.write("</H1>");  // end title and start first paragraph
            suppressParagraphs = false;
        }
      
        String chars = position.getCharacter();

        for (int i = 0; i < chars.length(); i++) 
        {
            char c = chars.charAt(i);
            if ((c < 32) || (c > 126)) 
            {
                int charAsInt = c;
                output.write("&#" + charAsInt + ";");
            } 
            else 
            {
                switch (c) 
                {
                    case 34:
                        output.write("&quot;");
                        break;
                    case 38:
                        output.write("&amp;");
                        break;
                    case 60:
                        output.write("&lt;");
                        break;
                    case 62:
                        output.write("&gt;");
                        break;
                    default:
                        output.write(c);
                }
            }
        }
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