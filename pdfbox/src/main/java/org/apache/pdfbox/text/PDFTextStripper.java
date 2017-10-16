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
package org.apache.pdfbox.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Bidi;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.util.QuickSort;

/**
 * This class will take a pdf document and strip out all of the text and ignore the formatting and such. Please note; it
 * is up to clients of this class to verify that a specific user has the correct permissions to extract text from the
 * PDF document.
 * 
 * The basic flow of this process is that we get a document and use a series of processXXX() functions that work on
 * smaller and smaller chunks of the page. Eventually, we fully process each page and then print it.
 *
 * @author Ben Litchfield
 */
public class PDFTextStripper extends LegacyPDFStreamEngine
{
    private static float defaultIndentThreshold = 2.0f;
    private static float defaultDropThreshold = 2.5f;

    private static final Log LOG = LogFactory.getLog(PDFTextStripper.class);

    // enable the ability to set the default indent/drop thresholds
    // with -D system properties:
    // pdftextstripper.indent
    // pdftextstripper.drop
    static
    {
        String strDrop = null, strIndent = null;
        try
        {
            String className = PDFTextStripper.class.getSimpleName().toLowerCase();
            String prop = className + ".indent";
            strIndent = System.getProperty(prop);
            prop = className + ".drop";
            strDrop = System.getProperty(prop);
        }
        catch (SecurityException e)
        {
            // PDFBOX-1946 when run in an applet
            // ignore and use default
        }
        if (strIndent != null && strIndent.length() > 0)
        {
            try
            {
                defaultIndentThreshold = Float.parseFloat(strIndent);
            }
            catch (NumberFormatException nfe)
            {
                // ignore and use default
            }
        }
        if (strDrop != null && strDrop.length() > 0)
        {
            try
            {
                defaultDropThreshold = Float.parseFloat(strDrop);
            }
            catch (NumberFormatException nfe)
            {
                // ignore and use default
            }
        }
    }

    /**
     * The platform's line separator.
     */
    protected final String LINE_SEPARATOR = System.getProperty("line.separator");

    private String lineSeparator = LINE_SEPARATOR;
    private String wordSeparator = " ";
    private String paragraphStart = "";
    private String paragraphEnd = "";
    private String pageStart = "";
    private String pageEnd = LINE_SEPARATOR;
    private String articleStart = "";
    private String articleEnd = "";

    private int currentPageNo = 0;
    private int startPage = 1;
    private int endPage = Integer.MAX_VALUE;
    private PDOutlineItem startBookmark = null;

    // 1-based bookmark pages
    private int startBookmarkPageNumber = -1;
    private int endBookmarkPageNumber = -1;

    private PDOutlineItem endBookmark = null;
    private boolean suppressDuplicateOverlappingText = true;
    private boolean shouldSeparateByBeads = true;
    private boolean sortByPosition = false;
    private boolean addMoreFormatting = false;

    private float indentThreshold = defaultIndentThreshold;
    private float dropThreshold = defaultDropThreshold;

    // we will need to estimate where to add spaces, these are used to help guess
    private float spacingTolerance = .5f;
    private float averageCharTolerance = .3f;

    private List<PDRectangle> beadRectangles = null;

    /**
     * The charactersByArticle is used to extract text by article divisions. For example a PDF that has two columns like
     * a newspaper, we want to extract the first column and then the second column. In this example the PDF would have 2
     * beads(or articles), one for each column. The size of the charactersByArticle would be 5, because not all text on
     * the screen will fall into one of the articles. The five divisions are shown below
     *
     * Text before first article
     * first article text
     * text between first article and second article
     * second article text
     * text after second article
     *
     * Most PDFs won't have any beads, so charactersByArticle will contain a single entry.
     */
    protected ArrayList<List<TextPosition>> charactersByArticle = new ArrayList<>();

    private Map<String, TreeMap<Float, TreeSet<Float>>> characterListMapping = new HashMap<>();

    protected PDDocument document;
    protected Writer output;

    /**
     * True if we started a paragraph but haven't ended it yet.
     */
    private boolean inParagraph;

    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public PDFTextStripper() throws IOException
    {
    }

    /**
     * This will return the text of a document. See writeText. <br>
     * NOTE: The document must not be encrypted when coming into this method.
     *
     * @param doc The document to get the text from.
     * @return The text of the PDF document.
     * @throws IOException if the doc state is invalid or it is encrypted.
     */
    public String getText(PDDocument doc) throws IOException
    {
        StringWriter outputStream = new StringWriter();
        writeText(doc, outputStream);
        return outputStream.toString();
    }

    private void resetEngine()
    {
        currentPageNo = 0;
        document = null;
        if (charactersByArticle != null)
        {
            charactersByArticle.clear();
        }
        if (characterListMapping != null)
        {
            characterListMapping.clear();
        }
    }

    /**
     * This will take a PDDocument and write the text of that document to the print writer.
     *
     * @param doc The document to get the data from.
     * @param outputStream The location to put the text.
     *
     * @throws IOException If the doc is in an invalid state.
     */
    public void writeText(PDDocument doc, Writer outputStream) throws IOException
    {
        resetEngine();
        document = doc;
        output = outputStream;
        if (getAddMoreFormatting())
        {
            paragraphEnd = lineSeparator;
            pageStart = lineSeparator;
            articleStart = lineSeparator;
            articleEnd = lineSeparator;
        }
        startDocument(document);
        processPages(document.getPages());
        endDocument(document);
    }

    /**
     * This will process all of the pages and the text that is in them.
     *
     * @param pages The pages object in the document.
     *
     * @throws IOException If there is an error parsing the text.
     */
    protected void processPages(PDPageTree pages) throws IOException
    {
        PDPage startBookmarkPage = startBookmark == null ? null
                : startBookmark.findDestinationPage(document);
        if (startBookmarkPage != null)
        {
            startBookmarkPageNumber = pages.indexOf(startBookmarkPage) + 1;
        }
        else
        {
            // -1 = undefined
            startBookmarkPageNumber = -1;
        }

        PDPage endBookmarkPage = endBookmark == null ? null
                : endBookmark.findDestinationPage(document);
        if (endBookmarkPage != null)
        {
            endBookmarkPageNumber = pages.indexOf(endBookmarkPage) + 1;
        }
        else
        {
            // -1 = undefined
            endBookmarkPageNumber = -1;
        }

        if (startBookmarkPageNumber == -1 && startBookmark != null && endBookmarkPageNumber == -1
                && endBookmark != null
                && startBookmark.getCOSObject() == endBookmark.getCOSObject())
        {
            // this is a special case where both the start and end bookmark
            // are the same but point to nothing. In this case
            // we will not extract any text.
            startBookmarkPageNumber = 0;
            endBookmarkPageNumber = 0;
        }

        for (PDPage page : pages)
        {
            currentPageNo++;
            if (page.hasContents())
            {
                processPage(page);
            }
        }
    }

    /**
     * This method is available for subclasses of this class. It will be called before processing of the document start.
     *
     * @param document The PDF document that is being processed.
     * @throws IOException If an IO error occurs.
     */
    protected void startDocument(PDDocument document) throws IOException
    {
        // no default implementation, but available for subclasses
    }

    /**
     * This method is available for subclasses of this class. It will be called after processing of the document
     * finishes.
     *
     * @param document The PDF document that is being processed.
     * @throws IOException If an IO error occurs.
     */
    protected void endDocument(PDDocument document) throws IOException
    {
        // no default implementation, but available for subclasses
    }

    /**
     * This will process the contents of a page.
     *
     * @param page The page to process.
     *
     * @throws IOException If there is an error processing the page.
     */
    @Override
    public void processPage(PDPage page) throws IOException
    {
        if (currentPageNo >= startPage && currentPageNo <= endPage
                && (startBookmarkPageNumber == -1 || currentPageNo >= startBookmarkPageNumber)
                && (endBookmarkPageNumber == -1 || currentPageNo <= endBookmarkPageNumber))
        {
            startPage(page);

            int numberOfArticleSections = 1;
            if (shouldSeparateByBeads)
            {
                fillBeadRectangles(page);
                numberOfArticleSections += beadRectangles.size() * 2;
            }
            int originalSize = charactersByArticle.size();
            charactersByArticle.ensureCapacity(numberOfArticleSections);
            int lastIndex = Math.max(numberOfArticleSections, originalSize);
            for (int i = 0; i < lastIndex; i++)
            {
                if (i < originalSize)
                {
                    charactersByArticle.get(i).clear();
                }
                else
                {
                    if (numberOfArticleSections < originalSize)
                    {
                        charactersByArticle.remove(i);
                    }
                    else
                    {
                        charactersByArticle.add(new ArrayList<TextPosition>());
                    }
                }
            }
            characterListMapping.clear();
            super.processPage(page);
            writePage();
            endPage(page);
        }
    }

    private void fillBeadRectangles(PDPage page)
    {
        beadRectangles = new ArrayList<>();
        for (PDThreadBead bead : page.getThreadBeads())
        {
            if (bead == null)
            {
                // can't skip, because of null entry handling in processTextPosition()
                beadRectangles.add(null);
                continue;
            }
            
            PDRectangle rect = bead.getRectangle();
            
            // bead rectangle is in PDF coordinates (y=0 is bottom),
            // glyphs are in image coordinates (y=0 is top),
            // so we must flip
            PDRectangle mediaBox = page.getMediaBox();
            float upperRightY = mediaBox.getUpperRightY() - rect.getLowerLeftY();
            float lowerLeftY = mediaBox.getUpperRightY() - rect.getUpperRightY();
            rect.setLowerLeftY(lowerLeftY);
            rect.setUpperRightY(upperRightY);
            
            // adjust for cropbox
            PDRectangle cropBox = page.getCropBox();
            if (cropBox.getLowerLeftX() != 0 || cropBox.getLowerLeftY() != 0)
            {
                rect.setLowerLeftX(rect.getLowerLeftX() - cropBox.getLowerLeftX());
                rect.setLowerLeftY(rect.getLowerLeftY() - cropBox.getLowerLeftY());
                rect.setUpperRightX(rect.getUpperRightX() - cropBox.getLowerLeftX());
                rect.setUpperRightY(rect.getUpperRightY() - cropBox.getLowerLeftY());
            }
            
            beadRectangles.add(rect);
        }
    }

    /**
     * Start a new article, which is typically defined as a column on a single page (also referred to as a bead). This
     * assumes that the primary direction of text is left to right. Default implementation is to do nothing. Subclasses
     * may provide additional information.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startArticle() throws IOException
    {
        startArticle(true);
    }

    /**
     * Start a new article, which is typically defined as a column on a single page (also referred to as a bead).
     * Default implementation is to do nothing. Subclasses may provide additional information.
     *
     * @param isLTR true if primary direction of text is left to right.
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startArticle(boolean isLTR) throws IOException
    {
        output.write(getArticleStart());
    }

    /**
     * End an article. Default implementation is to do nothing. Subclasses may provide additional information.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endArticle() throws IOException
    {
        output.write(getArticleEnd());
    }

    /**
     * Start a new page. Default implementation is to do nothing. Subclasses may provide additional information.
     *
     * @param page The page we are about to process.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startPage(PDPage page) throws IOException
    {
        // default is to do nothing
    }

    /**
     * End a page. Default implementation is to do nothing. Subclasses may provide additional information.
     *
     * @param page The page we are about to process.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endPage(PDPage page) throws IOException
    {
        // default is to do nothing
    }

    private static final float END_OF_LAST_TEXT_X_RESET_VALUE = -1;
    private static final float MAX_Y_FOR_LINE_RESET_VALUE = -Float.MAX_VALUE;
    private static final float EXPECTED_START_OF_NEXT_WORD_X_RESET_VALUE = -Float.MAX_VALUE;
    private static final float MAX_HEIGHT_FOR_LINE_RESET_VALUE = -1;
    private static final float MIN_Y_TOP_FOR_LINE_RESET_VALUE = Float.MAX_VALUE;
    private static final float LAST_WORD_SPACING_RESET_VALUE = -1;

    /**
     * This will print the text of the processed page to "output". It will estimate, based on the coordinates of the
     * text, where newlines and word spacings should be placed. The text will be sorted only if that feature was
     * enabled.
     *
     * @throws IOException If there is an error writing the text.
     */
    protected void writePage() throws IOException
    {
        float maxYForLine = MAX_Y_FOR_LINE_RESET_VALUE;
        float minYTopForLine = MIN_Y_TOP_FOR_LINE_RESET_VALUE;
        float endOfLastTextX = END_OF_LAST_TEXT_X_RESET_VALUE;
        float lastWordSpacing = LAST_WORD_SPACING_RESET_VALUE;
        float maxHeightForLine = MAX_HEIGHT_FOR_LINE_RESET_VALUE;
        PositionWrapper lastPosition = null;
        PositionWrapper lastLineStartPosition = null;

        boolean startOfPage = true; // flag to indicate start of page
        boolean startOfArticle;
        if (charactersByArticle.size() > 0)
        {
            writePageStart();
        }

        for (List<TextPosition> textList : charactersByArticle)
        {
            if (getSortByPosition())
            {
                TextPositionComparator comparator = new TextPositionComparator();

                // because the TextPositionComparator is not transitive, but
                // JDK7+ enforces transitivity on comparators, we need to use
                // a custom quicksort implementation (which is slower, unfortunately).
                QuickSort.sort(textList, comparator);
            }

            startArticle();
            startOfArticle = true;

            // Now cycle through to print the text.
            // We queue up a line at a time before we print so that we can convert
            // the line from presentation form to logical form (if needed).
            List<LineItem> line = new ArrayList<>();

            Iterator<TextPosition> textIter = textList.iterator();
            // PDF files don't always store spaces. We will need to guess where we should add
            // spaces based on the distances between TextPositions. Historically, this was done
            // based on the size of the space character provided by the font. In general, this
            // worked but there were cases where it did not work. Calculating the average character
            // width and using that as a metric works better in some cases but fails in some cases
            // where the spacing worked. So we use both. NOTE: Adobe reader also fails on some of
            // these examples.

            // Keeps track of the previous average character width
            float previousAveCharWidth = -1;
            while (textIter.hasNext())
            {
                TextPosition position = textIter.next();
                PositionWrapper current = new PositionWrapper(position);
                String characterValue = position.getUnicode();

                // Resets the average character width when we see a change in font
                // or a change in the font size
                if (lastPosition != null && (position.getFont() != lastPosition.getTextPosition()
                        .getFont()
                        || position.getFontSize() != lastPosition.getTextPosition().getFontSize()))
                {
                    previousAveCharWidth = -1;
                }

                float positionX;
                float positionY;
                float positionWidth;
                float positionHeight;

                // If we are sorting, then we need to use the text direction
                // adjusted coordinates, because they were used in the sorting.
                if (getSortByPosition())
                {
                    positionX = position.getXDirAdj();
                    positionY = position.getYDirAdj();
                    positionWidth = position.getWidthDirAdj();
                    positionHeight = position.getHeightDir();
                }
                else
                {
                    positionX = position.getX();
                    positionY = position.getY();
                    positionWidth = position.getWidth();
                    positionHeight = position.getHeight();
                }

                // The current amount of characters in a word
                int wordCharCount = position.getIndividualWidths().length;

                // Estimate the expected width of the space based on the
                // space character with some margin.
                float wordSpacing = position.getWidthOfSpace();
                float deltaSpace;
                if (wordSpacing == 0 || Float.isNaN(wordSpacing))
                {
                    deltaSpace = Float.MAX_VALUE;
                }
                else
                {
                    if (lastWordSpacing < 0)
                    {
                        deltaSpace = wordSpacing * getSpacingTolerance();
                    }
                    else
                    {
                        deltaSpace = (wordSpacing + lastWordSpacing) / 2f * getSpacingTolerance();
                    }
                }

                // Estimate the expected width of the space based on the average character width
                // with some margin. This calculation does not make a true average (average of
                // averages) but we found that it gave the best results after numerous experiments.
                // Based on experiments we also found that .3 worked well.
                float averageCharWidth;
                if (previousAveCharWidth < 0)
                {
                    averageCharWidth = positionWidth / wordCharCount;
                }
                else
                {
                    averageCharWidth = (previousAveCharWidth + positionWidth / wordCharCount) / 2f;
                }
                float deltaCharWidth = averageCharWidth * getAverageCharTolerance();

                // Compares the values obtained by the average method and the wordSpacing method
                // and picks the smaller number.
                float expectedStartOfNextWordX = EXPECTED_START_OF_NEXT_WORD_X_RESET_VALUE;
                if (endOfLastTextX != END_OF_LAST_TEXT_X_RESET_VALUE)
                {
                    if (deltaCharWidth > deltaSpace)
                    {
                        expectedStartOfNextWordX = endOfLastTextX + deltaSpace;
                    }
                    else
                    {
                        expectedStartOfNextWordX = endOfLastTextX + deltaCharWidth;
                    }
                }

                if (lastPosition != null)
                {
                    if (startOfArticle)
                    {
                        lastPosition.setArticleStart();
                        startOfArticle = false;
                    }
                    // RDD - Here we determine whether this text object is on the current
                    // line. We use the lastBaselineFontSize to handle the superscript
                    // case, and the size of the current font to handle the subscript case.
                    // Text must overlap with the last rendered baseline text by at least
                    // a small amount in order to be considered as being on the same line.

                    // XXX BC: In theory, this check should really check if the next char is in
                    // full range seen in this line. This is what I tried to do with minYTopForLine,
                    // but this caused a lot of regression test failures. So, I'm leaving it be for
                    // now
                    if (!overlap(positionY, positionHeight, maxYForLine, maxHeightForLine))
                    {
                        writeLine(normalize(line));
                        line.clear();
                        lastLineStartPosition = handleLineSeparation(current, lastPosition,
                                lastLineStartPosition, maxHeightForLine);
                        expectedStartOfNextWordX = EXPECTED_START_OF_NEXT_WORD_X_RESET_VALUE;
                        maxYForLine = MAX_Y_FOR_LINE_RESET_VALUE;
                        maxHeightForLine = MAX_HEIGHT_FOR_LINE_RESET_VALUE;
                        minYTopForLine = MIN_Y_TOP_FOR_LINE_RESET_VALUE;
                    }
                    // test if our TextPosition starts after a new word would be expected to start
                    if (expectedStartOfNextWordX != EXPECTED_START_OF_NEXT_WORD_X_RESET_VALUE
                            && expectedStartOfNextWordX < positionX &&
                            // only bother adding a space if the last character was not a space
                            lastPosition.getTextPosition().getUnicode() != null
                            && !lastPosition.getTextPosition().getUnicode().endsWith(" "))
                    {
                        line.add(LineItem.getWordSeparator());
                    }
                }
                if (positionY >= maxYForLine)
                {
                    maxYForLine = positionY;
                }
                // RDD - endX is what PDF considers to be the x coordinate of the
                // end position of the text. We use it in computing our metrics below.
                endOfLastTextX = positionX + positionWidth;

                // add it to the list
                if (characterValue != null)
                {
                    if (startOfPage && lastPosition == null)
                    {
                        writeParagraphStart();// not sure this is correct for RTL?
                    }
                    line.add(new LineItem(position));
                }
                maxHeightForLine = Math.max(maxHeightForLine, positionHeight);
                minYTopForLine = Math.min(minYTopForLine, positionY - positionHeight);
                lastPosition = current;
                if (startOfPage)
                {
                    lastPosition.setParagraphStart();
                    lastPosition.setLineStart();
                    lastLineStartPosition = lastPosition;
                    startOfPage = false;
                }
                lastWordSpacing = wordSpacing;
                previousAveCharWidth = averageCharWidth;
            }
            // print the final line
            if (line.size() > 0)
            {
                writeLine(normalize(line));
                writeParagraphEnd();
            }
            endArticle();
        }
        writePageEnd();
    }

    private boolean overlap(float y1, float height1, float y2, float height2)
    {
        return within(y1, y2, .1f) || y2 <= y1 && y2 >= y1 - height1
                || y1 <= y2 && y1 >= y2 - height2;
    }

    /**
     * Write the line separator value to the output stream.
     * 
     * @throws IOException If there is a problem writing out the lineseparator to the document.
     */
    protected void writeLineSeparator() throws IOException
    {
        output.write(getLineSeparator());
    }

    /**
     * Write the word separator value to the output stream.
     * 
     * @throws IOException If there is a problem writing out the wordseparator to the document.
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
    protected void writeCharacters(TextPosition text) throws IOException
    {
        output.write(text.getUnicode());
    }

    /**
     * Write a Java string to the output stream. The default implementation will ignore the <code>textPositions</code>
     * and just calls {@link #writeString(String)}.
     *
     * @param text The text to write to the stream.
     * @param textPositions The TextPositions belonging to the text.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        writeString(text);
    }

    /**
     * Write a Java string to the output stream.
     *
     * @param text The text to write to the stream.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeString(String text) throws IOException
    {
        output.write(text);
    }

    /**
     * This will determine of two floating point numbers are within a specified variance.
     *
     * @param first The first number to compare to.
     * @param second The second number to compare to.
     * @param variance The allowed variance.
     */
    private boolean within(float first, float second, float variance)
    {
        return second < first + variance && second > first - variance;
    }

    /**
     * This will process a TextPosition object and add the text to the list of characters on a page. It takes care of
     * overlapping text.
     *
     * @param text The text to process.
     */
    @Override
    protected void processTextPosition(TextPosition text)
    {
        boolean showCharacter = true;
        if (suppressDuplicateOverlappingText)
        {
            showCharacter = false;
            String textCharacter = text.getUnicode();
            float textX = text.getX();
            float textY = text.getY();
            TreeMap<Float, TreeSet<Float>> sameTextCharacters = characterListMapping
                    .get(textCharacter);
            if (sameTextCharacters == null)
            {
                sameTextCharacters = new TreeMap<>();
                characterListMapping.put(textCharacter, sameTextCharacters);
            }
            // RDD - Here we compute the value that represents the end of the rendered
            // text. This value is used to determine whether subsequent text rendered
            // on the same line overwrites the current text.
            //
            // We subtract any positive padding to handle cases where extreme amounts
            // of padding are applied, then backed off (not sure why this is done, but there
            // are cases where the padding is on the order of 10x the character width, and
            // the TJ just backs up to compensate after each character). Also, we subtract
            // an amount to allow for kerning (a percentage of the width of the last
            // character).
            boolean suppressCharacter = false;
            float tolerance = text.getWidth() / textCharacter.length() / 3.0f;

            SortedMap<Float, TreeSet<Float>> xMatches = sameTextCharacters.subMap(textX - tolerance,
                    textX + tolerance);
            for (TreeSet<Float> xMatch : xMatches.values())
            {
                SortedSet<Float> yMatches = xMatch.subSet(textY - tolerance, textY + tolerance);
                if (!yMatches.isEmpty())
                {
                    suppressCharacter = true;
                    break;
                }
            }
            if (!suppressCharacter)
            {
                TreeSet<Float> ySet = sameTextCharacters.get(textX);
                if (ySet == null)
                {
                    ySet = new TreeSet<>();
                    sameTextCharacters.put(textX, ySet);
                }
                ySet.add(textY);
                showCharacter = true;
            }
        }
        if (showCharacter)
        {
            // if we are showing the character then we need to determine which article it belongs to
            int foundArticleDivisionIndex = -1;
            int notFoundButFirstLeftAndAboveArticleDivisionIndex = -1;
            int notFoundButFirstLeftArticleDivisionIndex = -1;
            int notFoundButFirstAboveArticleDivisionIndex = -1;
            float x = text.getX();
            float y = text.getY();
            if (shouldSeparateByBeads)
            {
                for (int i = 0; i < beadRectangles.size() && foundArticleDivisionIndex == -1; i++)
                {
                    PDRectangle rect = beadRectangles.get(i);
                    if (rect != null)
                    {
                        if (rect.contains(x, y))
                        {
                            foundArticleDivisionIndex = i * 2 + 1;
                        }
                        else if ((x < rect.getLowerLeftX() || y < rect.getUpperRightY())
                                && notFoundButFirstLeftAndAboveArticleDivisionIndex == -1)
                        {
                            notFoundButFirstLeftAndAboveArticleDivisionIndex = i * 2;
                        }
                        else if (x < rect.getLowerLeftX()
                                && notFoundButFirstLeftArticleDivisionIndex == -1)
                        {
                            notFoundButFirstLeftArticleDivisionIndex = i * 2;
                        }
                        else if (y < rect.getUpperRightY()
                                && notFoundButFirstAboveArticleDivisionIndex == -1)
                        {
                            notFoundButFirstAboveArticleDivisionIndex = i * 2;
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
            int articleDivisionIndex;
            if (foundArticleDivisionIndex != -1)
            {
                articleDivisionIndex = foundArticleDivisionIndex;
            }
            else if (notFoundButFirstLeftAndAboveArticleDivisionIndex != -1)
            {
                articleDivisionIndex = notFoundButFirstLeftAndAboveArticleDivisionIndex;
            }
            else if (notFoundButFirstLeftArticleDivisionIndex != -1)
            {
                articleDivisionIndex = notFoundButFirstLeftArticleDivisionIndex;
            }
            else if (notFoundButFirstAboveArticleDivisionIndex != -1)
            {
                articleDivisionIndex = notFoundButFirstAboveArticleDivisionIndex;
            }
            else
            {
                articleDivisionIndex = charactersByArticle.size() - 1;
            }

            List<TextPosition> textList = charactersByArticle.get(articleDivisionIndex);

            // In the wild, some PDF encoded documents put diacritics (accents on
            // top of characters) into a separate Tj element. When displaying them
            // graphically, the two chunks get overlayed. With text output though,
            // we need to do the overlay. This code recombines the diacritic with
            // its associated character if the two are consecutive.
            if (textList.isEmpty())
            {
                textList.add(text);
            }
            else
            {
                // test if we overlap the previous entry.
                // Note that we are making an assumption that we need to only look back
                // one TextPosition to find what we are overlapping.
                // This may not always be true. */
                TextPosition previousTextPosition = textList.get(textList.size() - 1);
                if (text.isDiacritic() && previousTextPosition.contains(text))
                {
                    previousTextPosition.mergeDiacritic(text);
                }
                // If the previous TextPosition was the diacritic, merge it into this
                // one and remove it from the list.
                else if (previousTextPosition.isDiacritic() && text.contains(previousTextPosition))
                {
                    text.mergeDiacritic(previousTextPosition);
                    textList.remove(textList.size() - 1);
                    textList.add(text);
                }
                else
                {
                    textList.add(text);
                }
            }
        }
    }

    /**
     * This is the page that the text extraction will start on. The pages start at page 1. For example in a 5 page PDF
     * document, if the start page is 1 then all pages will be extracted. If the start page is 4 then pages 4 and 5 will
     * be extracted. The default value is 1.
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
     * @param startPageValue New value of 1-based startPage property.
     */
    public void setStartPage(int startPageValue)
    {
        startPage = startPageValue;
    }

    /**
     * This will get the last page that will be extracted. This is inclusive, for example if a 5 page PDF an endPage
     * value of 5 would extract the entire document, an end page of 2 would extract pages 1 and 2. This defaults to
     * Integer.MAX_VALUE such that all pages of the pdf will be extracted.
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
     * @param endPageValue New value of 1-based endPage property.
     */
    public void setEndPage(int endPageValue)
    {
        endPage = endPageValue;
    }

    /**
     * Set the desired line separator for output text. The line.separator system property is used if the line separator
     * preference is not set explicitly using this method.
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
     * This will get the word separator.
     *
     * @return The desired word separator string.
     */
    public String getWordSeparator()
    {
        return wordSeparator;
    }

    /**
     * Set the desired word separator for output text. The PDFBox text extraction algorithm will output a space
     * character if there is enough space between two words. By default a space character is used. If you need and
     * accurate count of characters that are found in a PDF document then you might want to set the word separator to
     * the empty string.
     *
     * @param separator The desired page separator string.
     */
    public void setWordSeparator(String separator)
    {
        wordSeparator = separator;
    }

    /**
     * @return Returns the suppressDuplicateOverlappingText.
     */
    public boolean getSuppressDuplicateOverlappingText()
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
     * Character strings are grouped by articles. It is quite common that there will only be a single article. This
     * returns a List that contains List objects, the inner lists will contain TextPosition objects.
     *
     * @return A double List of TextPositions for all text strings on the page.
     */
    protected List<List<TextPosition>> getCharactersByArticle()
    {
        return charactersByArticle;
    }

    /**
     * By default the text stripper will attempt to remove text that overlapps each other. Word paints the same
     * character several times in order to make it look bold. By setting this to false all text will be extracted, which
     * means that certain sections will be duplicated, but better performance will be noticed.
     *
     * @param suppressDuplicateOverlappingTextValue The suppressDuplicateOverlappingText to set.
     */
    public void setSuppressDuplicateOverlappingText(boolean suppressDuplicateOverlappingTextValue)
    {
        suppressDuplicateOverlappingText = suppressDuplicateOverlappingTextValue;
    }

    /**
     * This will tell if the text stripper should separate by beads.
     *
     * @return If the text will be grouped by beads.
     */
    public boolean getSeparateByBeads()
    {
        return shouldSeparateByBeads;
    }

    /**
     * Set if the text stripper should group the text output by a list of beads. The default value is true!
     *
     * @param aShouldSeparateByBeads The new grouping of beads.
     */
    public void setShouldSeparateByBeads(boolean aShouldSeparateByBeads)
    {
        shouldSeparateByBeads = aShouldSeparateByBeads;
    }

    /**
     * Get the bookmark where text extraction should end, inclusive. Default is null.
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
     * Get the bookmark where text extraction should start, inclusive. Default is null.
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
     * This will tell if the text stripper should add some more text formatting.
     * 
     * @return true if some more text formatting will be added
     */
    public boolean getAddMoreFormatting()
    {
        return addMoreFormatting;
    }

    /**
     * There will some additional text formatting be added if addMoreFormatting is set to true. Default is false.
     * 
     * @param newAddMoreFormatting Tell PDFBox to add some more text formatting
     */
    public void setAddMoreFormatting(boolean newAddMoreFormatting)
    {
        addMoreFormatting = newAddMoreFormatting;
    }

    /**
     * This will tell if the text stripper should sort the text tokens before writing to the stream.
     *
     * @return true If the text tokens will be sorted before being written.
     */
    public boolean getSortByPosition()
    {
        return sortByPosition;
    }

    /**
     * The order of the text tokens in a PDF file may not be in the same as they appear visually on the screen. For
     * example, a PDF writer may write out all text by font, so all bold or larger text, then make a second pass and
     * write out the normal text.<br>
     * The default is to <b>not</b> sort by position.<br>
     * <br>
     * A PDF writer could choose to write each character in a different order. By default PDFBox does <b>not</b> sort
     * the text tokens before processing them due to performance reasons.
     *
     * @param newSortByPosition Tell PDFBox to sort the text positions.
     */
    public void setSortByPosition(boolean newSortByPosition)
    {
        sortByPosition = newSortByPosition;
    }

    /**
     * Get the current space width-based tolerance value that is being used to estimate where spaces in text should be
     * added. Note that the default value for this has been determined from trial and error.
     * 
     * @return The current tolerance / scaling factor
     */
    public float getSpacingTolerance()
    {
        return spacingTolerance;
    }

    /**
     * Set the space width-based tolerance value that is used to estimate where spaces in text should be added. Note
     * that the default value for this has been determined from trial and error. Setting this value larger will reduce
     * the number of spaces added.
     * 
     * @param spacingToleranceValue tolerance / scaling factor to use
     */
    public void setSpacingTolerance(float spacingToleranceValue)
    {
        spacingTolerance = spacingToleranceValue;
    }

    /**
     * Get the current character width-based tolerance value that is being used to estimate where spaces in text should
     * be added. Note that the default value for this has been determined from trial and error.
     * 
     * @return The current tolerance / scaling factor
     */
    public float getAverageCharTolerance()
    {
        return averageCharTolerance;
    }

    /**
     * Set the character width-based tolerance value that is used to estimate where spaces in text should be added. Note
     * that the default value for this has been determined from trial and error. Setting this value larger will reduce
     * the number of spaces added.
     * 
     * @param averageCharToleranceValue average tolerance / scaling factor to use
     */
    public void setAverageCharTolerance(float averageCharToleranceValue)
    {
        averageCharTolerance = averageCharToleranceValue;
    }

    /**
     * returns the multiple of whitespace character widths for the current text which the current line start can be
     * indented from the previous line start beyond which the current line start is considered to be a paragraph start.
     * 
     * @return the number of whitespace character widths to use when detecting paragraph indents.
     */
    public float getIndentThreshold()
    {
        return indentThreshold;
    }

    /**
     * sets the multiple of whitespace character widths for the current text which the current line start can be
     * indented from the previous line start beyond which the current line start is considered to be a paragraph start.
     * The default value is 2.0.
     *
     * @param indentThresholdValue the number of whitespace character widths to use when detecting paragraph indents.
     */
    public void setIndentThreshold(float indentThresholdValue)
    {
        indentThreshold = indentThresholdValue;
    }

    /**
     * the minimum whitespace, as a multiple of the max height of the current characters beyond which the current line
     * start is considered to be a paragraph start.
     * 
     * @return the character height multiple for max allowed whitespace between lines in the same paragraph.
     */
    public float getDropThreshold()
    {
        return dropThreshold;
    }

    /**
     * sets the minimum whitespace, as a multiple of the max height of the current characters beyond which the current
     * line start is considered to be a paragraph start. The default value is 2.5.
     *
     * @param dropThresholdValue the character height multiple for max allowed whitespace between lines in the same
     * paragraph.
     */
    public void setDropThreshold(float dropThresholdValue)
    {
        dropThreshold = dropThresholdValue;
    }

    /**
     * Returns the string which will be used at the beginning of a paragraph.
     * 
     * @return the paragraph start string
     */
    public String getParagraphStart()
    {
        return paragraphStart;
    }

    /**
     * Sets the string which will be used at the beginning of a paragraph.
     * 
     * @param s the paragraph start string
     */
    public void setParagraphStart(String s)
    {
        paragraphStart = s;
    }

    /**
     * Returns the string which will be used at the end of a paragraph.
     * 
     * @return the paragraph end string
     */
    public String getParagraphEnd()
    {
        return paragraphEnd;
    }

    /**
     * Sets the string which will be used at the end of a paragraph.
     * 
     * @param s the paragraph end string
     */
    public void setParagraphEnd(String s)
    {
        paragraphEnd = s;
    }

    /**
     * Returns the string which will be used at the beginning of a page.
     * 
     * @return the page start string
     */
    public String getPageStart()
    {
        return pageStart;
    }

    /**
     * Sets the string which will be used at the beginning of a page.
     * 
     * @param pageStartValue the page start string
     */
    public void setPageStart(String pageStartValue)
    {
        pageStart = pageStartValue;
    }

    /**
     * Returns the string which will be used at the end of a page.
     * 
     * @return the page end string
     */
    public String getPageEnd()
    {
        return pageEnd;
    }

    /**
     * Sets the string which will be used at the end of a page.
     * 
     * @param pageEndValue the page end string
     */
    public void setPageEnd(String pageEndValue)
    {
        pageEnd = pageEndValue;
    }

    /**
     * Returns the string which will be used at the beginning of an article.
     * 
     * @return the article start string
     */
    public String getArticleStart()
    {
        return articleStart;
    }

    /**
     * Sets the string which will be used at the beginning of an article.
     * 
     * @param articleStartValue the article start string
     */
    public void setArticleStart(String articleStartValue)
    {
        articleStart = articleStartValue;
    }

    /**
     * Returns the string which will be used at the end of an article.
     * 
     * @return the article end string
     */
    public String getArticleEnd()
    {
        return articleEnd;
    }

    /**
     * Sets the string which will be used at the end of an article.
     * 
     * @param articleEndValue the article end string
     */
    public void setArticleEnd(String articleEndValue)
    {
        articleEnd = articleEndValue;
    }

    /**
     * handles the line separator for a new line given the specified current and previous TextPositions.
     * 
     * @param current the current text position
     * @param lastPosition the previous text position
     * @param lastLineStartPosition the last text position that followed a line separator.
     * @param maxHeightForLine max height for positions since lastLineStartPosition
     * @return start position of the last line
     * @throws IOException if something went wrong
     */
    private PositionWrapper handleLineSeparation(PositionWrapper current,
            PositionWrapper lastPosition, PositionWrapper lastLineStartPosition,
            float maxHeightForLine) throws IOException
    {
        current.setLineStart();
        isParagraphSeparation(current, lastPosition, lastLineStartPosition, maxHeightForLine);
        lastLineStartPosition = current;
        if (current.isParagraphStart())
        {
            if (lastPosition.isArticleStart())
            {
                if (lastPosition.isLineStart())
                {
                    writeLineSeparator();
                }
                writeParagraphStart();
            }
            else
            {
                writeLineSeparator();
                writeParagraphSeparator();
            }
        }
        else
        {
            writeLineSeparator();
        }
        return lastLineStartPosition;
    }

    /**
     * tests the relationship between the last text position, the current text position and the last text position that
     * followed a line separator to decide if the gap represents a paragraph separation. This should <i>only</i> be
     * called for consecutive text positions that first pass the line separation test.
     * <p>
     * This base implementation tests to see if the lastLineStartPosition is null OR if the current vertical position
     * has dropped below the last text vertical position by at least 2.5 times the current text height OR if the current
     * horizontal position is indented by at least 2 times the current width of a space character.
     * </p>
     * <p>
     * This also attempts to identify text that is indented under a hanging indent.
     * </p>
     * <p>
     * This method sets the isParagraphStart and isHangingIndent flags on the current position object.
     * </p>
     *
     * @param position the current text position. This may have its isParagraphStart or isHangingIndent flags set upon
     * return.
     * @param lastPosition the previous text position (should not be null).
     * @param lastLineStartPosition the last text position that followed a line separator, or null.
     * @param maxHeightForLine max height for text positions since lasLineStartPosition.
     */
    private void isParagraphSeparation(PositionWrapper position, PositionWrapper lastPosition,
            PositionWrapper lastLineStartPosition, float maxHeightForLine)
    {
        boolean result = false;
        if (lastLineStartPosition == null)
        {
            result = true;
        }
        else
        {
            float yGap = Math.abs(position.getTextPosition().getYDirAdj()
                    - lastPosition.getTextPosition().getYDirAdj());
            float newYVal = multiplyFloat(getDropThreshold(), maxHeightForLine);
            // do we need to flip this for rtl?
            float xGap = position.getTextPosition().getXDirAdj()
                    - lastLineStartPosition.getTextPosition().getXDirAdj();
            float newXVal = multiplyFloat(getIndentThreshold(),
                    position.getTextPosition().getWidthOfSpace());
            float positionWidth = multiplyFloat(0.25f, position.getTextPosition().getWidth());

            if (yGap > newYVal)
            {
                result = true;
            }
            else if (xGap > newXVal)
            {
                // text is indented, but try to screen for hanging indent
                if (!lastLineStartPosition.isParagraphStart())
                {
                    result = true;
                }
                else
                {
                    position.setHangingIndent();
                }
            }
            else if (xGap < -position.getTextPosition().getWidthOfSpace())
            {
                // text is left of previous line. Was it a hanging indent?
                if (!lastLineStartPosition.isParagraphStart())
                {
                    result = true;
                }
            }
            else if (Math.abs(xGap) < positionWidth)
            {
                // current horizontal position is within 1/4 a char of the last
                // linestart. We'll treat them as lined up.
                if (lastLineStartPosition.isHangingIndent())
                {
                    position.setHangingIndent();
                }
                else if (lastLineStartPosition.isParagraphStart())
                {
                    // check to see if the previous line looks like
                    // any of a number of standard list item formats
                    Pattern liPattern = matchListItemPattern(lastLineStartPosition);
                    if (liPattern != null)
                    {
                        Pattern currentPattern = matchListItemPattern(position);
                        if (liPattern == currentPattern)
                        {
                            result = true;
                        }
                    }
                }
            }
        }
        if (result)
        {
            position.setParagraphStart();
        }
    }

    private float multiplyFloat(float value1, float value2)
    {
        // multiply 2 floats and truncate the resulting value to 3 decimal places
        // to avoid wrong results when comparing with another float
        return Math.round(value1 * value2 * 1000) / 1000f;
    }

    /**
     * writes the paragraph separator string to the output.
     * 
     * @throws IOException if something went wrong
     */
    protected void writeParagraphSeparator() throws IOException
    {
        writeParagraphEnd();
        writeParagraphStart();
    }

    /**
     * Write something (if defined) at the start of a paragraph.
     * 
     * @throws IOException if something went wrong
     */
    protected void writeParagraphStart() throws IOException
    {
        if (inParagraph)
        {
            writeParagraphEnd();
            inParagraph = false;
        }
        output.write(getParagraphStart());
        inParagraph = true;
    }

    /**
     * Write something (if defined) at the end of a paragraph.
     * 
     * @throws IOException if something went wrong
     */
    protected void writeParagraphEnd() throws IOException
    {
        if (!inParagraph)
        {
            writeParagraphStart();
        }
        output.write(getParagraphEnd());
        inParagraph = false;
    }

    /**
     * Write something (if defined) at the start of a page.
     * 
     * @throws IOException if something went wrong
     */
    protected void writePageStart() throws IOException
    {
        output.write(getPageStart());
    }

    /**
     * Write something (if defined) at the end of a page.
     * 
     * @throws IOException if something went wrong
     */
    protected void writePageEnd() throws IOException
    {
        output.write(getPageEnd());
    }

    /**
     * returns the list item Pattern object that matches the text at the specified PositionWrapper or null if the text
     * does not match such a pattern. The list of Patterns tested against is given by the {@link #getListItemPatterns()}
     * method. To add to the list, simply override that method (if sub-classing) or explicitly supply your own list
     * using {@link #setListItemPatterns(List)}.
     * 
     * @param pw position
     * @return the matching pattern
     */
    private Pattern matchListItemPattern(PositionWrapper pw)
    {
        TextPosition tp = pw.getTextPosition();
        String txt = tp.getUnicode();
        return matchPattern(txt, getListItemPatterns());
    }

    /**
     * a list of regular expressions that match commonly used list item formats, i.e. bullets, numbers, letters, Roman
     * numerals, etc. Not meant to be comprehensive.
     */
    private static final String[] LIST_ITEM_EXPRESSIONS = { "\\.", "\\d+\\.", "\\[\\d+\\]",
            "\\d+\\)", "[A-Z]\\.", "[a-z]\\.", "[A-Z]\\)", "[a-z]\\)", "[IVXL]+\\.",
            "[ivxl]+\\.", };

    private List<Pattern> listOfPatterns = null;

    /**
     * use to supply a different set of regular expression patterns for matching list item starts.
     *
     * @param patterns list of patterns
     */
    protected void setListItemPatterns(List<Pattern> patterns)
    {
        listOfPatterns = patterns;
    }

    /**
     * returns a list of regular expression Patterns representing different common list item formats. For example
     * numbered items of form:
     * <ol>
     * <li>some text</li>
     * <li>more text</li>
     * </ol>
     * or
     * <ul>
     * <li>some text</li>
     * <li>more text</li>
     * </ul>
     * etc., all begin with some character pattern. The pattern "\\d+\." (matches "1.", "2.", ...) or "\[\\d+\]"
     * (matches "[1]", "[2]", ...).
     * <p>
     * This method returns a list of such regular expression Patterns.
     * 
     * @return a list of Pattern objects.
     */
    protected List<Pattern> getListItemPatterns()
    {
        if (listOfPatterns == null)
        {
            listOfPatterns = new ArrayList<>();
            for (String expression : LIST_ITEM_EXPRESSIONS)
            {
                Pattern p = Pattern.compile(expression);
                listOfPatterns.add(p);
            }
        }
        return listOfPatterns;
    }

    /**
     * iterates over the specified list of Patterns until it finds one that matches the specified string. Then returns
     * the Pattern.
     * <p>
     * Order of the supplied list of patterns is important as most common patterns should come first. Patterns should be
     * strict in general, and all will be used with case sensitivity on.
     * </p>
     * 
     * @param string the string to be searched
     * @param patterns list of patterns
     * @return matching pattern
     */
    protected static Pattern matchPattern(String string, List<Pattern> patterns)
    {
        for (Pattern p : patterns)
        {
            if (p.matcher(string).matches())
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Write a list of string containing a whole line of a document.
     * 
     * @param line a list with the words of the given line
     * @throws IOException if something went wrong
     */
    private void writeLine(List<WordWithTextPositions> line)
            throws IOException
    {
        int numberOfStrings = line.size();
        for (int i = 0; i < numberOfStrings; i++)
        {
            WordWithTextPositions word = line.get(i);
            writeString(word.getText(), word.getTextPositions());
            if (i < numberOfStrings - 1)
            {
                writeWordSeparator();
            }
        }
    }

    /**
     * Normalize the given list of TextPositions.
     * 
     * @param line list of TextPositions
     * @return a list of strings, one string for every word
     */
    private List<WordWithTextPositions> normalize(List<LineItem> line)
    {
        List<WordWithTextPositions> normalized = new LinkedList<>();
        StringBuilder lineBuilder = new StringBuilder();
        List<TextPosition> wordPositions = new ArrayList<>();

        for (LineItem item : line)
        {
            lineBuilder = normalizeAdd(normalized, lineBuilder, wordPositions, item);
        }

        if (lineBuilder.length() > 0)
        {
            normalized.add(createWord(lineBuilder.toString(), wordPositions));
        }
        return normalized;
    }

    /**
     * Handles the LTR and RTL direction of the given words. The whole implementation stands and falls with the given
     * word. If the word is a full line, the results will be the best. If the word contains of single words or
     * characters, the order of the characters in a word or words in a line may wrong, due to RTL and LTR marks and
     * characters!
     * 
     * Based on http://www.nesterovsky-bros.com/weblog/2013/07/28/VisualToLogicalConversionInJava.aspx
     * 
     * @param word The word that shall be processed
     * @return new word with the correct direction of the containing characters
     */
    private String handleDirection(String word)
    {
        Bidi bidi = new Bidi(word, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);

        // if there is pure LTR text no need to process further
        if (!bidi.isMixed() && bidi.getBaseLevel() == Bidi.DIRECTION_LEFT_TO_RIGHT)
        {
            return word;
        }
        
        // collect individual bidi information
        int runCount = bidi.getRunCount();
        byte[] levels = new byte[runCount];
        Integer[] runs = new Integer[runCount];
      
        for (int i = 0; i < runCount; i++)
        {
           levels[i] = (byte)bidi.getRunLevel(i);
           runs[i] = i;
        }

        // reorder individual parts based on their levels
        Bidi.reorderVisually(levels, 0, runs, 0, runCount);
        
        // collect the parts based on the direction within the run
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < runCount; i++)
        {
           int index = runs[i];
           int start = bidi.getRunStart(index);
           int end = bidi.getRunLimit(index);

            int level = levels[index];

            if ((level & 1) != 0)
            {
                while (--end >= start)
                {
                    char character = word.charAt(end);
                    if (Character.isMirrored(word.codePointAt(end)))
                    {
                        if (MIRRORING_CHAR_MAP.containsKey(character))
                        {
                            result.append(MIRRORING_CHAR_MAP.get(character));
                        }
                        else
                        {
                            result.append(character);
                        }
                    }
                    else
                    {
                        result.append(character);
                    }
                }
            }
            else
            {
                result.append(word, start, end);
            }
        }
        
        return result.toString();
    }

    private static final Map<Character, Character> MIRRORING_CHAR_MAP = new HashMap<>();

    static
    {
        String path = "org/apache/pdfbox/resources/text/BidiMirroring.txt";
        try (InputStream input = PDFTextStripper.class.getClassLoader().getResourceAsStream(path))
        {
            if (input != null)
            {
                parseBidiFile(input);
            }
            else
            {
                LOG.warn("Could not find '" + path + "', mirroring char map will be empty: ");
            }
            
        }
        catch (IOException e)
        {
            LOG.warn("Could not parse BidiMirroring.txt, mirroring char map will be empty: "
                    + e.getMessage());
        }
    }

    /**
     * This method parses the bidi file provided as inputstream.
     * 
     * @param inputStream - The bidi file as inputstream
     * @throws IOException if any line could not be read by the LineNumberReader
     */
    private static void parseBidiFile(InputStream inputStream) throws IOException
    {
        LineNumberReader rd = new LineNumberReader(new InputStreamReader(inputStream));

        do
        {
            String s = rd.readLine();
            if (s == null)
            {
                break;
            }

            int comment = s.indexOf('#'); // ignore comments
            if (comment != -1)
            {
                s = s.substring(0, comment);
            }

            if (s.length() < 2)
            {
                continue;
            }

            StringTokenizer st = new StringTokenizer(s, ";");
            int nFields = st.countTokens();
            Character[] fields = new Character[nFields];
            for (int i = 0; i < nFields; i++)
            {
                fields[i] = (char) Integer.parseInt(st.nextToken().trim(), 16);
            }

            if (fields.length == 2)
            {
                // initialize the MIRRORING_CHAR_MAP
                MIRRORING_CHAR_MAP.put(fields[0], fields[1]);
            }
        }
        while (true);
    }

    /**
     * Used within {@link #normalize(List)} to create a single {@link WordWithTextPositions} entry.
     */
    private WordWithTextPositions createWord(String word, List<TextPosition> wordPositions)
    {
        return new WordWithTextPositions(normalizeWord(word), wordPositions);
    }

    /**
     * Normalize certain Unicode characters. For example, convert the single "fi" ligature to "f" and "i". Also
     * normalises Arabic and Hebrew presentation forms.
     *
     * @param word Word to normalize
     * @return Normalized word
     */
    private String normalizeWord(String word)
    {
        StringBuilder builder = null;
        int p = 0;
        int q = 0;
        int strLength = word.length();
        for (; q < strLength; q++)
        {
            // We only normalize if the codepoint is in a given range.
            // Otherwise, NFKC converts too many things that would cause
            // confusion. For example, it converts the micro symbol in
            // extended Latin to the value in the Greek script. We normalize
            // the Unicode Alphabetic and Arabic A&B Presentation forms.
            char c = word.charAt(q);
            if (0xFB00 <= c && c <= 0xFDFF || 0xFE70 <= c && c <= 0xFEFF)
            {
                if (builder == null)
                {
                    builder = new StringBuilder(strLength * 2);
                }
                builder.append(word.substring(p, q));
                // Some fonts map U+FDF2 differently than the Unicode spec.
                // They add an extra U+0627 character to compensate.
                // This removes the extra character for those fonts.
                if (c == 0xFDF2 && q > 0
                        && (word.charAt(q - 1) == 0x0627 || word.charAt(q - 1) == 0xFE8D))
                {
                    builder.append("\u0644\u0644\u0647");
                }
                else
                {
                    // Trim because some decompositions have an extra space, such as U+FC5E
                    builder.append(Normalizer
                            .normalize(word.substring(q, q + 1), Normalizer.Form.NFKC).trim());
                }
                p = q + 1;
            }
        }
        if (builder == null)
        {
            return handleDirection(word);
        }
        else
        {
            builder.append(word.substring(p, q));
            return handleDirection(builder.toString());
        }
    }

    /**
     * Used within {@link #normalize(List)} to handle a {@link TextPosition}.
     * 
     * @return The StringBuilder that must be used when calling this method.
     */
    private StringBuilder normalizeAdd(List<WordWithTextPositions> normalized,
            StringBuilder lineBuilder, List<TextPosition> wordPositions, LineItem item)
    {
        if (item.isWordSeparator())
        {
            normalized.add(
                    createWord(lineBuilder.toString(), new ArrayList<>(wordPositions)));
            lineBuilder = new StringBuilder();
            wordPositions.clear();
        }
        else
        {
            TextPosition text = item.getTextPosition();
            lineBuilder.append(text.getUnicode());
            wordPositions.add(text);
        }
        return lineBuilder;
    }

    /**
     * internal marker class. Used as a place holder in a line of TextPositions.
     */
    private static final class LineItem
    {
        public static LineItem WORD_SEPARATOR = new LineItem();

        public static LineItem getWordSeparator()
        {
            return WORD_SEPARATOR;
        }

        private final TextPosition textPosition;

        private LineItem()
        {
            textPosition = null;
        }

        LineItem(TextPosition textPosition)
        {
            this.textPosition = textPosition;
        }

        public TextPosition getTextPosition()
        {
            return textPosition;
        }

        public boolean isWordSeparator()
        {
            return textPosition == null;
        }
    }

    /**
     * Internal class that maps strings to lists of {@link TextPosition} arrays. Note that the number of entries in that
     * list may differ from the number of characters in the string due to normalization.
     *
     * @author Axel Dörfler
     */
    private static final class WordWithTextPositions
    {
        String text;
        List<TextPosition> textPositions;

        WordWithTextPositions(String word, List<TextPosition> positions)
        {
            text = word;
            textPositions = positions;
        }

        public String getText()
        {
            return text;
        }

        public List<TextPosition> getTextPositions()
        {
            return textPositions;
        }
    }

    /**
     * wrapper of TextPosition that adds flags to track status as linestart and paragraph start positions.
     * <p>
     * This is implemented as a wrapper since the TextPosition class doesn't provide complete access to its state fields
     * to subclasses. Also, conceptually TextPosition is immutable while these flags need to be set post-creation so it
     * makes sense to put these flags in this separate class.
     * </p>
     * 
     * @author m.martinez@ll.mit.edu
     */
    private static final class PositionWrapper
    {
        private boolean isLineStart = false;
        private boolean isParagraphStart = false;
        private boolean isPageBreak = false;
        private boolean isHangingIndent = false;
        private boolean isArticleStart = false;

        private TextPosition position = null;

        /**
         * Constructs a PositionWrapper around the specified TextPosition object.
         *
         * @param position the text position.
         */
        PositionWrapper(TextPosition position)
        {
            this.position = position;
        }

        /**
         * Returns the underlying TextPosition object.
         * 
         * @return the text position
         */
        public TextPosition getTextPosition()
        {
            return position;
        }

        public boolean isLineStart()
        {
            return isLineStart;
        }

        /**
         * Sets the isLineStart() flag to true.
         */
        public void setLineStart()
        {
            this.isLineStart = true;
        }

        public boolean isParagraphStart()
        {
            return isParagraphStart;
        }

        /**
         * sets the isParagraphStart() flag to true.
         */
        public void setParagraphStart()
        {
            this.isParagraphStart = true;
        }

        public boolean isArticleStart()
        {
            return isArticleStart;
        }

        /**
         * Sets the isArticleStart() flag to true.
         */
        public void setArticleStart()
        {
            this.isArticleStart = true;
        }

        public boolean isPageBreak()
        {
            return isPageBreak;
        }

        /**
         * Sets the isPageBreak() flag to true.
         */
        public void setPageBreak()
        {
            this.isPageBreak = true;
        }

        public boolean isHangingIndent()
        {
            return isHangingIndent;
        }

        /**
         * Sets the isHangingIndent() flag to true.
         */
        public void setHangingIndent()
        {
            this.isHangingIndent = true;
        }
    }
}
