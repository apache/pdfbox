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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;


/**
 * This class will take a pdf document and strip out all of the text and ignore the
 * formatting and such.  Please note; it is up to clients of this class to verify that
 * a specific user has the correct permissions to extract text from the
 * PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.70 $
 */
public class PDFTextStripper extends PDFStreamEngine
{
    private int currentPageNo = 0;
    private int startPage = 1;
    private int endPage = Integer.MAX_VALUE;
    private PDOutlineItem startBookmark = null;
    private int startBookmarkPageNumber = -1;
    private PDOutlineItem endBookmark = null;
    private int endBookmarkPageNumber = -1;
    private PDDocument document;
    private boolean suppressDuplicateOverlappingText = true;
    private boolean shouldSeparateByBeads = true;
    private boolean sortByPosition = false;

    private List pageArticles = null;
    /**
     * The charactersByArticle is used to extract text by article divisions.  For example
     * a PDF that has two columns like a newspaper, we want to extract the first column and
     * then the second column.  In this example the PDF would have 2 beads(or articles), one for
     * each column.  The size of the charactersByArticle would be 5, because not all text on the
     * screen will fall into one of the articles.  The five divisions are shown below
     *
     * Text before first article
     * first article text
     * text between first article and second article
     * second article text
     * text after second article
     *
     * Most PDFs won't have any beads, so charactersByArticle will contain a single entry.
     */
    protected Vector charactersByArticle = new Vector();

    private Map characterListMapping = new HashMap();

    private String lineSeparator = System.getProperty("line.separator");
    private String pageSeparator = System.getProperty("line.separator");
    private String wordSeparator = " ";

    /**
     * The stream to write the output to.
     */
    protected Writer output;

    /**
     * Instantiate a new PDFTextStripper object.  This object will load properties from
     * Resources/PDFTextStripper.properties.
     * @throws IOException If there is an error loading the properties.
     */
    public PDFTextStripper() throws IOException
    {
        super( ResourceLoader.loadProperties( "Resources/PDFTextStripper.properties", true ) );
    }

    /**
     * Instantiate a new PDFTextStripper object.  Loading all of the operator mappings
     * from the properties object that is passed in.
     *
     * @param props The properties containing the mapping of operators to PDFOperator
     * classes.
     *
     * @throws IOException If there is an error reading the properties.
     */
    public PDFTextStripper( Properties props ) throws IOException
    {
        super( props );
    }

    /**
     * This will return the text of a document.  See writeText. <br />
     * NOTE: The document must not be encrypted when coming into this method.
     *
     * @param doc The document to get the text from.
     *
     * @return The text of the PDF document.
     *
     * @throws IOException if the doc state is invalid or it is encrypted.
     */
    public String getText( PDDocument doc ) throws IOException
    {
        StringWriter outputStream = new StringWriter();
        writeText( doc, outputStream );
        return outputStream.toString();
    }

    /**
     * @deprecated
     * @see PDFTextStripper#getText( PDDocument )
     * @param doc The document to extract the text from.
     * @return The document text.
     * @throws IOException If there is an error extracting the text.
     */
    public String getText( COSDocument doc ) throws IOException
    {
        return getText( new PDDocument( doc ) );
    }

    /**
     * @deprecated
     * @see PDFTextStripper#writeText( PDDocument, Writer )
     * @param doc The document to extract the text.
     * @param outputStream The stream to write the text to.
     * @throws IOException If there is an error extracting the text.
     */
    public void writeText( COSDocument doc, Writer outputStream ) throws IOException
    {
        writeText( new PDDocument( doc ), outputStream );
    }

    /**
     * This will take a PDDocument and write the text of that document to the print writer.
     *
     * @param doc The document to get the data from.
     * @param outputStream The location to put the text.
     *
     * @throws IOException If the doc is in an invalid state.
     */
    public void writeText( PDDocument doc, Writer outputStream ) throws IOException
    {
        resetEngine();

        currentPageNo = 0;
        document = doc;
        output = outputStream;
        startDocument(document);

        if( document.isEncrypted() )
        {
            // We are expecting non-encrypted documents here, but it is common
            // for users to pass in a document that is encrypted with an empty
            // password (such a document appears to not be encrypted by
            // someone viewing the document, thus the confusion).  We will
            // attempt to decrypt with the empty password to handle this case.
            //
            try
            {
                document.decrypt("");
            }
            catch (CryptographyException e)
            {
                throw new WrappedIOException("Error decrypting document, details: ", e);
            }
            catch (InvalidPasswordException e)
            {
                throw new WrappedIOException("Error: document is encrypted", e);
            }
        }

        processPages( document.getDocumentCatalog().getAllPages() );
        endDocument(document);
    }

    /**
     * This will process all of the pages and the text that is in them.
     *
     * @param pages The pages object in the document.
     *
     * @throws IOException If there is an error parsing the text.
     */
    protected void processPages( List pages ) throws IOException
    {
        if( startBookmark != null )
        {
            startBookmarkPageNumber = getPageNumber( startBookmark, pages );
        }

        if( endBookmark != null )
        {
            endBookmarkPageNumber = getPageNumber( endBookmark, pages );
        }

        if( startBookmarkPageNumber == -1 && startBookmark != null &&
            endBookmarkPageNumber == -1 && endBookmark != null &&
            startBookmark.getCOSObject() == endBookmark.getCOSObject() )
        {
            //this is a special case where both the start and end bookmark
            //are the same but point to nothing.  In this case
            //we will not extract any text.
            startBookmarkPageNumber = 0;
            endBookmarkPageNumber = 0;
        }


        Iterator pageIter = pages.iterator();
        while( pageIter.hasNext() )
        {
            PDPage nextPage = (PDPage)pageIter.next();
            PDStream contentStream = nextPage.getContents();
            currentPageNo++;
            if( contentStream != null )
            {
                COSStream contents = contentStream.getStream();
                processPage( nextPage, contents );
            }
        }
    }

    private int getPageNumber( PDOutlineItem bookmark, List allPages ) throws IOException
    {
        int pageNumber = -1;
        PDPage page = bookmark.findDestinationPage( document );
        if( page != null )
        {
            pageNumber = allPages.indexOf( page )+1;//use one based indexing
        }
        return pageNumber;
    }

    /**
     * This method is available for subclasses of this class.  It will be called before processing
     * of the document start.
     *
     * @param pdf The PDF document that is being processed.
     * @throws IOException If an IO error occurs.
     */
    protected void startDocument(PDDocument pdf) throws IOException
    {
        // no default implementation, but available for subclasses
    }

    /**
     * This method is available for subclasses of this class.  It will be called after processing
     * of the document finishes.
     *
     * @param pdf The PDF document that is being processed.
     * @throws IOException If an IO error occurs.
     */
    protected void endDocument(PDDocument pdf ) throws IOException
    {
        // no default implementation, but available for subclasses
    }

    /**
     * This will process the contents of a page.
     *
     * @param page The page to process.
     * @param content The contents of the page.
     *
     * @throws IOException If there is an error processing the page.
     */
    protected void processPage( PDPage page, COSStream content ) throws IOException
    {
        if( currentPageNo >= startPage && currentPageNo <= endPage &&
            (startBookmarkPageNumber == -1 || currentPageNo >= startBookmarkPageNumber ) &&
            (endBookmarkPageNumber == -1 || currentPageNo <= endBookmarkPageNumber ))
        {
            startPage( page );
            pageArticles = page.getThreadBeads();
            int numberOfArticleSections = 1 + pageArticles.size() * 2;
            if( !shouldSeparateByBeads )
            {
                numberOfArticleSections = 1;
            }
            int originalSize = charactersByArticle.size();
            charactersByArticle.setSize( numberOfArticleSections );
            for( int i=0; i<numberOfArticleSections; i++ )
            {
                if( numberOfArticleSections < originalSize )
                {
                    ((List)charactersByArticle.get( i )).clear();
                }
                else
                {
                    charactersByArticle.set( i, new ArrayList() );
                }
            }

            characterListMapping.clear();
            processStream( page, page.findResources(), content );
            flushText();
            endPage( page );
        }

    }

    /**
     * Start a new paragraph.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startParagraph() throws IOException
    {
        //default is to do nothing.
    }

    /**
     * End a paragraph.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endParagraph() throws IOException
    {
        //default is to do nothing
    }

    /**
     * Start a new page.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @param page The page we are about to process.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startPage( PDPage page ) throws IOException
    {
        //default is to do nothing.
    }

    /**
     * End a page.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @param page The page we are about to process.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endPage( PDPage page ) throws IOException
    {
        //default is to do nothing
    }

    /**
     * This will print the text to the output stream.
     *
     * @throws IOException If there is an error writing the text.
     */
    protected void flushText() throws IOException
    {
        float maxYForLine = -1;
        float minYTopForLine = Float.MAX_VALUE;
        //float lastBaselineFontSize = -1;
        float endOfLastTextX = -1;
        //float endOfLastTextY = -1;
        float expectedStartOfNextWordX = -1;
        float lastWordSpacing = -1;
        float maxHeightForLine = -1;
        //float lastHeightForLine = -1;
        TextPosition lastPosition = null;
        for( int i = 0; i < charactersByArticle.size(); i++)
        {
            startParagraph();
            List textList = (List)charactersByArticle.get( i );
            if( sortByPosition )
            {
                TextPositionComparator comparator = new TextPositionComparator();
                Collections.sort( textList, comparator );
            }

            Iterator textIter = textList.iterator();
            while( textIter.hasNext() )
            {
                TextPosition position = (TextPosition)textIter.next();
                String characterValue = position.getCharacter();

                float positionX;
                float positionY;
                float positionWidth;
                float positionHeight;

                /* If we are sorting, then we need to use the text direction
                 * adjusted coordinates, because they were used in the sorting. */
                if (sortByPosition) {
                	positionX = position.getXDirAdj();
                	positionY = position.getYDirAdj();
                	positionWidth = position.getWidthDirAdj();
                	positionHeight = position.getHeightDir();
                }
                else {
                	positionX = position.getX();
                	positionY = position.getY();
                	positionWidth = position.getWidth();
                	positionHeight = position.getHeight();
                }


                float wordSpacing = 0;
                /* float wordSpacing = position.getWordSpacing();	BC: When I re-enabled this for a a test, lots of extra spaces were added
                if( wordSpacing == 0 )
                {
                */
                    //try to get width of a space character
                    wordSpacing = position.getWidthOfSpace();
                    //if still zero fall back to getting the width of the current
                    //character
                    if( wordSpacing == 0 )
                    {
                      wordSpacing = positionWidth;
                    }
                //}


                // RDD - We add a conservative approximation for space determination.
                // basically if there is a blank area between two characters that is
                //equal to some percentage of the word spacing then that will be the
                //start of the next word
                if( lastWordSpacing <= 0 )
                {
                    expectedStartOfNextWordX = endOfLastTextX + (wordSpacing* 0.50f);
                }
                else
                {
                    expectedStartOfNextWordX = endOfLastTextX + (((wordSpacing+lastWordSpacing)/2f)* 0.50f);
                }

                // RDD - We will suppress text that is very close to the current line
                // and which overwrites previously rendered text on this line.
                // This is done specifically to handle a reasonably common situation
                // where an application (MS Word, in the case of my examples) renders
                // text four times at small (1 point) offsets in order to accomplish
                // bold printing.  You would not want to do this step if you were
                // going to render the TextPosition objects graphically.
                //
                /*if ((endOfLastTextX != -1 && position.getX() < endOfLastTextX) &&
                    (currentY != -1 && Math.abs(position.getY() - currentY) < 1))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Suppressing text overwrite" +
                                  " x: " + position.getX() +
                                  " endOfLastTextX: " + endOfLastTextX +
                                  " string: " + position.getCharacter());
                    }
                    continue;
                }*/

                // RDD - Here we determine whether this text object is on the current
                // line.  We use the lastBaselineFontSize to handle the superscript
                // case, and the size of the current font to handle the subscript case.
                // Text must overlap with the last rendered baseline text by at least
                // a small amount in order to be considered as being on the same line.
                //

                //int verticalScaling = 1;
                //if( lastBaselineFontSize < 0 || position.getFontSize() < 0 )
                //{
                //    verticalScaling = -1;
                //}

                if( lastPosition != null )
                {
                    //if (currentY != -1 &&
                    //    ((position.getY() < (currentY - (lastBaselineFontSize * 0.9f * verticalScaling))) ||
                    //     (position.getY() > (currentY + (position.getFontSize() * 0.9f * verticalScaling)))))
                    //{
                    /* XXX BC: In theory, this check should really check if the next char is in full range
                     * seen in this line. This is what I tried to do with minYTopForLine, but this caused a lot
                     * of regression test failures.  So, I'm leaving it be for now. */
                    if( ( !overlap( positionY, positionHeight, maxYForLine, maxHeightForLine ) ))
                    		//maxYForLine - minYTopForLine)))
                    {
                        processLineSeparator( position );
                        endOfLastTextX = -1;
                        expectedStartOfNextWordX = -1;
                        maxYForLine = -1;
                        maxHeightForLine = -1;
                        //lastBaselineFontSize = -1;
                        minYTopForLine = Float.MAX_VALUE;
                        //lastHeightForLine = -1;
                    }


	                if (expectedStartOfNextWordX != -1 && expectedStartOfNextWordX < positionX &&
	                   //only bother adding a space if the last character was not a space
	                   lastPosition.getCharacter() != null &&
	                   !lastPosition.getCharacter().endsWith( " " ) )
	                {
	                    processWordSeparator( lastPosition, position );
	                }
	                else
	                {
	                    //System.out.println( "Not a word separator " + position.getCharacter() +  " start=" + startOfNextWordX + " x=" + position.getX() );
	                }
                }

                if (positionY >= maxYForLine) {
                	maxYForLine = positionY;
                    //lastBaselineFontSize = position.getFontSize();
                }

                // RDD - endX is what PDF considers to be the x coordinate of the
                // end position of the text.  We use it in computing our metrics below.
                endOfLastTextX = positionX + positionWidth;
                //endOfLastTextY = positionY;

                if (characterValue != null)
                {
                    writeCharacters( position );
                }
                else
                {
                    //Position.getString() is null so not writing anything
                }
                maxHeightForLine = Math.max( maxHeightForLine, positionHeight );
                minYTopForLine = Math.min(minYTopForLine, positionY - positionHeight);
                lastPosition = position;
                //lastHeightForLine = position.getHeight();
                lastWordSpacing = wordSpacing;
            }
            endParagraph();
        }


        // RDD - newline at end of flush - required for end of page (so that the top
        // of the next page starts on its own line.
        //
        output.write(getPageSeparator());

        output.flush();
    }

    private boolean overlap( float y1, float height1, float y2, float height2 )
    {
        return within( y1, y2, .1f) || (y2 <= y1 && y2 >= y1-height1) ||
               (y1 <= y2 && y1 >= y2-height2);
    }

    protected void processLineSeparator( TextPosition currentText ) throws IOException
    {
        output.write(getLineSeparator());
    }

    protected void processWordSeparator( TextPosition lastText, TextPosition currentText ) throws IOException
    {
        output.write(getWordSeparator());
    }

    /**
     * Write the string to the output stream.
     *
     * @param text The text to write to the stream.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeCharacters( TextPosition text ) throws IOException
    {
        output.write( text.getCharacter() );
    }

    /**
     * This will determine of two floating point numbers are within a specified variance.
     *
     * @param first The first number to compare to.
     * @param second The second number to compare to.
     * @param variance The allowed variance.
     */
    private boolean within( float first, float second, float variance )
    {
        return second > first - variance && second < first + variance;
    }

    /**
     * This will show add a character to the list of characters to be printed to
     * the text file.
     *
     * @param text The description of the character to display.
     */
    protected void showCharacter( TextPosition text )
    {
        boolean showCharacter = true;
        if( suppressDuplicateOverlappingText )
        {
            showCharacter = false;
            String textCharacter = text.getCharacter();
            float textX = text.getX();
            float textY = text.getY();
            List sameTextCharacters = (List)characterListMapping.get( textCharacter );
            if( sameTextCharacters == null )
            {
                sameTextCharacters = new ArrayList();
                characterListMapping.put( textCharacter, sameTextCharacters );
            }

            // RDD - Here we compute the value that represents the end of the rendered
            // text.  This value is used to determine whether subsequent text rendered
            // on the same line overwrites the current text.
            //
            // We subtract any positive padding to handle cases where extreme amounts
            // of padding are applied, then backed off (not sure why this is done, but there
            // are cases where the padding is on the order of 10x the character width, and
            // the TJ just backs up to compensate after each character).  Also, we subtract
            // an amount to allow for kerning (a percentage of the width of the last
            // character).
            //
            boolean suppressCharacter = false;
            float tolerance = (text.getWidth()/textCharacter.length())/3.0f;
            for( int i=0; i<sameTextCharacters.size() && textCharacter != null; i++ )
            {
                TextPosition character = (TextPosition)sameTextCharacters.get( i );
                String charCharacter = character.getCharacter();
                float charX = character.getX();
                float charY = character.getY();
                //only want to suppress

                if( charCharacter != null &&
                    //charCharacter.equals( textCharacter ) &&
                    within( charX, textX, tolerance ) &&
                    within( charY,
                    		textY,
                            tolerance ) )
                {
                    suppressCharacter = true;
                }
            }
            if( !suppressCharacter )
            {
                sameTextCharacters.add( text );
                showCharacter = true;
            }
        }

        if( showCharacter )
        {
            //if we are showing the character then we need to determine which
            //article it belongs to.
            int foundArticleDivisionIndex = -1;
            int notFoundButFirstLeftAndAboveArticleDivisionIndex = -1;
            int notFoundButFirstLeftArticleDivisionIndex = -1;
            int notFoundButFirstAboveArticleDivisionIndex = -1;
            float x = text.getX();
            float y = text.getY();
            if( shouldSeparateByBeads )
            {
                for( int i=0; i<pageArticles.size() && foundArticleDivisionIndex == -1; i++ )
                {
                    PDThreadBead bead = (PDThreadBead)pageArticles.get( i );
                    if( bead != null )
                    {
                        PDRectangle rect = bead.getRectangle();
                        if( rect.contains( x, y ) )
                        {
                            foundArticleDivisionIndex = i*2+1;
                        }
                        else if( (x < rect.getLowerLeftX() ||
                                  y < rect.getUpperRightY()) &&
                            notFoundButFirstLeftAndAboveArticleDivisionIndex == -1)
                        {
                            notFoundButFirstLeftAndAboveArticleDivisionIndex = i*2;
                        }
                        else if( x < rect.getLowerLeftX() &&
                                notFoundButFirstLeftArticleDivisionIndex == -1)
                        {
                            notFoundButFirstLeftArticleDivisionIndex = i*2;
                        }
                        else if( y < rect.getUpperRightY() &&
                                notFoundButFirstAboveArticleDivisionIndex == -1)
                        {
                            notFoundButFirstAboveArticleDivisionIndex = i*2;
                        }
                    }
                    else
                    {
                        foundArticleDivisionIndex = 0;
                    }
                }
            }
            else
            {
                foundArticleDivisionIndex = 0;
            }
            int articleDivisionIndex = -1;
            if( foundArticleDivisionIndex != -1 )
            {
                articleDivisionIndex = foundArticleDivisionIndex;
            }
            else if( notFoundButFirstLeftAndAboveArticleDivisionIndex != -1 )
            {
                articleDivisionIndex = notFoundButFirstLeftAndAboveArticleDivisionIndex;
            }
            else if( notFoundButFirstLeftArticleDivisionIndex != -1 )
            {
                articleDivisionIndex = notFoundButFirstLeftArticleDivisionIndex;
            }
            else if( notFoundButFirstAboveArticleDivisionIndex != -1 )
            {
                articleDivisionIndex = notFoundButFirstAboveArticleDivisionIndex;
            }
            else
            {
                articleDivisionIndex = charactersByArticle.size()-1;
            }
            List textList = (List) charactersByArticle.get( articleDivisionIndex );
            textList.add( text );
        }
    }

    /**
     * This is the page that the text extraction will start on.  The pages start
     * at page 1.  For example in a 5 page PDF document, if the start page is 1
     * then all pages will be extracted.  If the start page is 4 then pages 4 and 5
     * will be extracted.  The default value is 1.
     *
     * @return Value of property startPage.
     */
    public int getStartPage()
    {
        return startPage;
    }

    /**
     * This will set the first page to be extracted by this class.
     *
     * @param startPageValue New value of property startPage.
     */
    public void setStartPage(int startPageValue)
    {
        startPage = startPageValue;
    }

    /**
     * This will get the last page that will be extracted.  This is inclusive,
     * for example if a 5 page PDF an endPage value of 5 would extract the
     * entire document, an end page of 2 would extract pages 1 and 2.  This defaults
     * to Integer.MAX_VALUE such that all pages of the pdf will be extracted.
     *
     * @return Value of property endPage.
     */
    public int getEndPage()
    {
        return endPage;
    }

    /**
     * This will set the last page to be extracted by this class.
     *
     * @param endPageValue New value of property endPage.
     */
    public void setEndPage(int endPageValue)
    {
        endPage = endPageValue;
    }

    /**
     * Set the desired line separator for output text.  The line.separator
     * system property is used if the line separator preference is not set
     * explicitly using this method.
     *
     * @param separator The desired line separator string.
     */
    public void setLineSeparator(String separator)
    {
        lineSeparator = separator;
    }

    /**
     * This will get the line separator.
     *
     * @return The desired line separator string.
     */
    public String getLineSeparator()
    {
        return lineSeparator;
    }

    /**
     * Set the desired page separator for output text.  The line.separator
     * system property is used if the page separator preference is not set
     * explicitly using this method.
     *
     * @param separator The desired page separator string.
     */
    public void setPageSeparator(String separator)
    {
        pageSeparator = separator;
    }

    /**
     * This will get the word separator.
     *
     * @return The desired word separator string.
     */
    public String getWordSeparator()
    {
        return wordSeparator;
    }

    /**
     * Set the desired word separator for output text.  The PDFBox text extraction
     * algorithm will output a space character if there is enough space between
     * two words.  By default a space character is used.  If you need and accurate
     * count of characters that are found in a PDF document then you might want to
     * set the word separator to the empty string.
     *
     * @param separator The desired page separator string.
     */
    public void setWordSeparator(String separator)
    {
        wordSeparator = separator;
    }

    /**
     * This will get the page separator.
     *
     * @return The page separator string.
     */
    public String getPageSeparator()
    {
        return pageSeparator;
    }
    /**
     * @return Returns the suppressDuplicateOverlappingText.
     */
    public boolean shouldSuppressDuplicateOverlappingText()
    {
        return suppressDuplicateOverlappingText;
    }

    /**
     * Get the current page number that is being processed.
     *
     * @return A 1 based number representing the current page.
     */
    protected int getCurrentPageNo()
    {
        return currentPageNo;
    }

    /**
     * The output stream that is being written to.
     *
     * @return The stream that output is being written to.
     */
    protected Writer getOutput()
    {
        return output;
    }

    /**
     * Character strings are grouped by articles.  It is quite common that there
     * will only be a single article.  This returns a List that contains List objects,
     * the inner lists will contain TextPosition objects.
     *
     * @return A double List of TextPositions for all text strings on the page.
     */
    protected List getCharactersByArticle()
    {
        return charactersByArticle;
    }

    /**
     * By default the text stripper will attempt to remove text that overlapps each other.
     * Word paints the same character several times in order to make it look bold.  By setting
     * this to false all text will be extracted, which means that certain sections will be
     * duplicated, but better performance will be noticed.
     *
     * @param suppressDuplicateOverlappingTextValue The suppressDuplicateOverlappingText to set.
     */
    public void setSuppressDuplicateOverlappingText(
            boolean suppressDuplicateOverlappingTextValue)
    {
        this.suppressDuplicateOverlappingText = suppressDuplicateOverlappingTextValue;
    }

    /**
     * This will tell if the text stripper should separate by beads.
     *
     * @return If the text will be grouped by beads.
     */
    public boolean shouldSeparateByBeads()
    {
        return shouldSeparateByBeads;
    }

    /**
     * Set if the text stripper should group the text output by a list of beads.  The default value is true!
     *
     * @param aShouldSeparateByBeads The new grouping of beads.
     */
    public void setShouldSeparateByBeads(boolean aShouldSeparateByBeads)
    {
        this.shouldSeparateByBeads = aShouldSeparateByBeads;
    }

    /**
     * Get the bookmark where text extraction should end, inclusive.  Default is null.
     *
     * @return The ending bookmark.
     */
    public PDOutlineItem getEndBookmark()
    {
        return endBookmark;
    }

    /**
     * Set the bookmark where the text extraction should stop.
     *
     * @param aEndBookmark The ending bookmark.
     */
    public void setEndBookmark(PDOutlineItem aEndBookmark)
    {
        endBookmark = aEndBookmark;
    }

    /**
     * Get the bookmark where text extraction should start, inclusive.  Default is null.
     *
     * @return The starting bookmark.
     */
    public PDOutlineItem getStartBookmark()
    {
        return startBookmark;
    }

    /**
     * Set the bookmark where text extraction should start, inclusive.
     *
     * @param aStartBookmark The starting bookmark.
     */
    public void setStartBookmark(PDOutlineItem aStartBookmark)
    {
        startBookmark = aStartBookmark;
    }

    /**
     * This will tell if the text stripper should sort the text tokens
     * before writing to the stream.
     *
     * @return true If the text tokens will be sorted before being written.
     */
    public boolean shouldSortByPosition()
    {
        return sortByPosition;
    }

    /**
     * The order of the text tokens in a PDF file may not be in the same
     * as they appear visually on the screen.  For example, a PDF writer may
     * write out all text by font, so all bold or larger text, then make a second
     * pass and write out the normal text.<br/>
     * The default is to <b>not</b> sort by position.<br/>
     * <br/>
     * A PDF writer could choose to write each character in a different order.  By
     * default PDFBox does <b>not</b> sort the text tokens before processing them due to
     * performance reasons.
     *
     * @param newSortByPosition Tell PDFBox to sort the text positions.
     */
    public void setSortByPosition(boolean newSortByPosition)
    {
        sortByPosition = newSortByPosition;
    }
}
