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
 * The basic flow of this process is that we get a document and use a series of 
 * processXXX() functions that work on smaller and smaller chunks of the page.  
 * Eventually, we fully process each page and then print it. 
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
    protected PDDocument document;
    private boolean suppressDuplicateOverlappingText = true;
    private boolean shouldSeparateByBeads = true;
    private boolean sortByPosition = false;
    
    // We will need to estimate where to add spaces.  
    // These are used to help guess. 
    private float spacingTolerance = .5f;
    private float averageCharTolerance = .3f;

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

    protected String lineSeparator = System.getProperty("line.separator");
    private String pageSeparator = System.getProperty("line.separator");
    private String wordSeparator = " ";

    /**
     * The stream to write the output to.
     */
    protected Writer output;

    /**
     * The normalizer is used to remove text ligatures/presentation forms
     * and to correct the direction of right to left text, such as Arabic and Hebrew.
     */
    private TextNormalize normalize = null;

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
            writePage();
            endPage( page );
        }

    }

    /**
     * Start a new article, which is typically defined as a column
     * on a single page (also referred to as a bead).  This assumes
     * that the primary direction of text is left to right.  
     * Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startArticle() throws IOException
    {
        startArticle(true);
    }

    /**
     * Start a new article, which is typically defined as a column
     * on a single page (also referred to as a bead).  
     * Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @param true if primary direction of text is left to right
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startArticle(boolean isltr) throws IOException
    {
        //default is to do nothing.
    }

    /**
     * End an article.  Default implementation is to do nothing.  Subclasses
     * may provide additional information.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endArticle() throws IOException
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
     * This will print the text of the processed page to "output".
     * It will estimate, based on the coordinates of the text, where
     * newlines and word spacings should be placed. The text will be
     * sorted only if that feature was enabled. 
     *
     * @throws IOException If there is an error writing the text.
     */
    protected void writePage() throws IOException    
    {
        float maxYForLine = -1;
        float minYTopForLine = Float.MAX_VALUE;
        float endOfLastTextX = -1;
        float lastWordSpacing = -1;
        float maxHeightForLine = -1;
        TextPosition lastPosition = null;

        if (normalize == null) {
            normalize = new TextNormalize();
        }

        for( int i = 0; i < charactersByArticle.size(); i++)
        {
            List textList = (List)charactersByArticle.get( i );
            if( sortByPosition )
            {
                TextPositionComparator comparator = new TextPositionComparator();
                Collections.sort( textList, comparator );
            }

            Iterator textIter = textList.iterator();

            /* Before we can display the text, we need to do some normalizing.
             * Arabic and Hebrew text is right to left and is typically stored
             * in its logical format, which means that the rightmost character is 
             * stored first, followed by the second character from the right etc.
             * However, PDF stores the text in presentation form, which is left to 
             * right.  We need to do some normalization to convert the PDF data to
             * the proper logical output format. 
             * 
             * Note that if we did not sort the text, then the output of reversing the
             * text is undefined and can sometimes produce worse output then not trying
             * to reverse the order.  Sorting should be done for these languages.
             * */

            /* First step is to determine if we have any right to left text, and 
             * if so, is it dominant. */ 
            int ltrCnt = 0;
            int rtlCnt = 0;

            while( textIter.hasNext() )
            {
                TextPosition position = (TextPosition)textIter.next();
                String stringValue = position.getCharacter();

                for (int a = 0; a < stringValue.length(); a++) {
                    byte dir = Character.getDirectionality(stringValue.charAt(a));
                    if ((dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT ) || 
                            (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING) ||
                            (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE )) {
                        ltrCnt++;
                    }
                    else if ((dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT ) ||
                            (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) ||
                            (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING) ||
                            (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE )) {
                        rtlCnt++;
                    }
                }
            }

            // choose the dominant direction
            boolean isRtlDominant = false; 
            if (rtlCnt > ltrCnt) {
                isRtlDominant = true;
            }

            startArticle(!isRtlDominant);

            // we will later use this to skip reordering
            boolean hasRtl = false;
            if (rtlCnt > 0)
                hasRtl = true;

            /* Now cycle through to print the text.  
             * We queue up a line at a time before we print so that we can convert
             * the line from presentation form to logical form (if needed). */
            String lineStr = "";

            textIter = textList.iterator();    // start from the beginning again

            /* PDF files don't always store spaces. We will need to guess where we should add
             * spaces based on the distances between TextPositions. Historically, this was done
             * based on the size of the space character provided by the font. In general, this worked
             * but there were cases where it did not work. Calculating the average character width
             * and using that as a metric works better in some cases but fails in some cases where the 
             * spacing worked. So we use both. NOTE: Adobe reader also fails on some of these examples. 
             */

            //Keeps track of the previous average character width
            float previousAveCharWidth = -1;
            while( textIter.hasNext() )
            {
                TextPosition position = (TextPosition)textIter.next();
                String characterValue = position.getCharacter();

                //Resets the average character width when we see a change in font 
                // or a change in the font size
                if(lastPosition != null && ((position.getFont() != lastPosition.getFont()) 
                        || (position.getFontSize() != lastPosition.getFontSize()))){
                    previousAveCharWidth = -1;
                }

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

                //The current amount of characters in a word
                int wordCharCount = position.getIndividualWidths().length;

                /* Estimate the expected width of the space based on the 
                 * space character with some margin. */
                float wordSpacing = position.getWidthOfSpace();
                float deltaSpace = 0;
                if ((wordSpacing == 0) || (wordSpacing == Float.NaN)) {
                    deltaSpace = Float.MAX_VALUE;
                }
                else {
                    if( lastWordSpacing < 0 ){
                        deltaSpace = (wordSpacing * spacingTolerance);
                    }
                    else{
                        deltaSpace = (((wordSpacing+lastWordSpacing)/2f)* spacingTolerance);
                    }
                }

                /* Estimate the expected width of the space based on the 
                 * average character width with some margin. This calculation does not
                 * make a true average (average of averages) but we found that it gave the
                 * best results after numerous experiments. Based on experiments we also found that
                 * .3 worked well. */                       
                float averageCharWidth = -1;
                if(previousAveCharWidth < 0){
                    averageCharWidth = (positionWidth/wordCharCount);
                }
                else{
                    averageCharWidth = (previousAveCharWidth + (positionWidth/wordCharCount))/2f;
                }
                float deltaCharWidth = (averageCharWidth * averageCharTolerance);
                
                //Compares the values obtained by the average method and the wordSpacing method and picks
                //the smaller number. 
                float expectedStartOfNextWordX = -1;
                if(endOfLastTextX != -1){
                    if(deltaCharWidth > deltaSpace){
                        expectedStartOfNextWordX = endOfLastTextX + deltaSpace;
                    }
                    else{
                        expectedStartOfNextWordX = endOfLastTextX + deltaCharWidth;
                    }
                }   

                if( lastPosition != null ){  
                    // RDD - Here we determine whether this text object is on the current
                    // line.  We use the lastBaselineFontSize to handle the superscript
                    // case, and the size of the current font to handle the subscript case.
                    // Text must overlap with the last rendered baseline text by at least
                    // a small amount in order to be considered as being on the same line.

                    /* XXX BC: In theory, this check should really check if the next char is in full range
                     * seen in this line. This is what I tried to do with minYTopForLine, but this caused a lot
                     * of regression test failures.  So, I'm leaving it be for now. */
                    if(!overlap(positionY, positionHeight, maxYForLine, maxHeightForLine)){
                        // If we have RTL text on the page, change the direction
                        if (hasRtl)
                            lineStr = normalize.makeLineLogicalOrder(lineStr, isRtlDominant);

                        /* normalize string to remove presentation forms.
                         * Note that this must come after the line direction 
                         * conversion because the process looks ahead to the next
                         * logical character. 
                         */
                        lineStr = normalize.normalizePres(lineStr);

                        writeString(lineStr);
                        lineStr = "";

                        writeLineSeparator( );

                        endOfLastTextX = -1;
                        expectedStartOfNextWordX = -1;
                        maxYForLine = -1;
                        maxHeightForLine = -1;
                        minYTopForLine = Float.MAX_VALUE;
                    }

                    //Test if our TextPosition starts after a new word would be expected to start. 
                    if (expectedStartOfNextWordX != -1 && expectedStartOfNextWordX < positionX &&
                            //only bother adding a space if the last character was not a space
                            lastPosition.getCharacter() != null &&
                            !lastPosition.getCharacter().endsWith( " " ) ) {
                        lineStr += getWordSeparator();
                    }
                }

                if (positionY >= maxYForLine) {
                    maxYForLine = positionY;
                }

                // RDD - endX is what PDF considers to be the x coordinate of the
                // end position of the text.  We use it in computing our metrics below.
                endOfLastTextX = positionX + positionWidth;

                // add it to the list
                if (characterValue != null) {
                    lineStr += characterValue;
                }
                maxHeightForLine = Math.max( maxHeightForLine, positionHeight );
                minYTopForLine = Math.min(minYTopForLine, positionY - positionHeight);
                lastPosition = position;
                lastWordSpacing = wordSpacing;
                previousAveCharWidth = averageCharWidth;
            }

            // print the final line
            if (lineStr.length() > 0) {
                if (hasRtl)
                    lineStr = normalize.makeLineLogicalOrder(lineStr, isRtlDominant);

                // normalize string to remove presentation forms
                lineStr = normalize.normalizePres(lineStr);

                writeString(lineStr);
            }

            endArticle();
        }

        writePageSeperator();;
    }

    private boolean overlap( float y1, float height1, float y2, float height2 )
    {
        return within( y1, y2, .1f) || (y2 <= y1 && y2 >= y1-height1) ||
        (y1 <= y2 && y1 >= y2-height2);
    }

    /**
     * Write the page separator value to the output stream
     * @throws IOException
     */
    protected void writePageSeperator() throws IOException
    {
        // RDD - newline at end of flush - required for end of page (so that the top
        // of the next page starts on its own line.
        //
        output.write(getPageSeparator());
        output.flush();
    }

    /**
     * Write the line separator value to the output stream
     * @throws IOException
     */
    protected void writeLineSeparator( ) throws IOException
    {
        output.write(getLineSeparator());
    }


    /**
     * Write the word separator value to the output stream
     * @throws IOException
     */
    protected void writeWordSeparator() throws IOException
    {
        output.write(getWordSeparator());
    }

    /**
     * Write the string in TextPosition to the output stream.
     *
     * @param text The text to write to the stream.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeCharacters( TextPosition text ) throws IOException
    {
        output.write( text.getCharacter() );
    }

    /**
     * Write a Java string to the output stream.
     *
     * @param text The text to write to the stream.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeString( String text ) throws IOException
    {
        output.write( text );
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
     * This will process a TextPosition object and add the
     * text to the list of characters on a page.  It takes care of
     * overlapping text.
     *
     * @param text The text to process.
     */
    protected void processTextPosition( TextPosition text )
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

            /* In the wild, some PDF encoded documents put diacritics (accents on
             * top of characters) into a separate Tj element.  When displaying them
             * graphically, the two chunks get overlayed.  With text output though,
             * we need to do the overlay. This code recombines the diacritic with
             * its associated character if the two are consecutive.
             */ 
            if(textList.isEmpty()){
                textList.add(text);
            }
            else{
                /* test if we overlap the previous entry.  
                 * Note that we are making an assumption that we need to only look back
                 * one TextPosition to find what we are overlapping.  
                 * This may not always be true. */
                TextPosition previousTextPosition = (TextPosition)textList.get(textList.size()-1);
                if(text.isDiacritic() && previousTextPosition.contains(text)){
                    previousTextPosition.mergeDiacritic(text);
                }
                /* If the previous TextPosition was the diacritic, merge it into this
                 * one and remove it from the list. */
                else if(previousTextPosition.isDiacritic() && text.contains(previousTextPosition)){
                    text.mergeDiacritic(previousTextPosition);
                    textList.remove(textList.size()-1);
                    textList.add(text);
                }
                else{
                    textList.add(text);
                }
            }
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

    /**
     * Get the current space width-based tolerance value that is being used
     * to estimate where spaces in text should be added.  Note that the
     * default value for this has been determined from trial and error. 
     * 
     * @return The current tolerance / scaling factor
     */
    public float getSpacingTolerance() {
        return spacingTolerance;
    }

    /**
     * Set the space width-based tolerance value that is used
     * to estimate where spaces in text should be added.  Note that the
     * default value for this has been determined from trial and error.
     * Setting this value larger will reduce the number of spaces added. 
     * 
     * @param spacingTolerance tolerance / scaling factor to use
     */
    public void setSpacingTolerance(float spacingTolerance) {
        this.spacingTolerance = spacingTolerance;
    }

    /**
     * Get the current character width-based tolerance value that is being used
     * to estimate where spaces in text should be added.  Note that the
     * default value for this has been determined from trial and error.
     * 
     * @return The current tolerance / scaling factor
     */
    public float getAverageCharTolerance() {
        return averageCharTolerance;
    }

    /**
     * Set the character width-based tolerance value that is used
     * to estimate where spaces in text should be added.  Note that the
     * default value for this has been determined from trial and error.
     * Setting this value larger will reduce the number of spaces added. 
     * 
     * @param spacingTolerance tolerance / scaling factor to use
     */
    public void setAverageCharTolerance(float averageCharTolerance) {
        this.averageCharTolerance = averageCharTolerance;
    }
}
