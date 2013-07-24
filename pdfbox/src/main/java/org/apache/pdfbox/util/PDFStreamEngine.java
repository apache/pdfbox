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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * This class will run through a PDF content stream and execute certain operations and provide a callback interface for
 * clients that want to do things with the stream. See the PDFTextStripper class for an example of how to use this
 * class.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.38 $
 */
public class PDFStreamEngine
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFStreamEngine.class);

    /**
     * The PDF operators that are ignored by this engine.
     */
    private final Set<String> unsupportedOperators = new HashSet<String>();

    private PDGraphicsState graphicsState = null;

    private Matrix textMatrix = null;
    private Matrix textLineMatrix = null;
    private Stack<PDGraphicsState> graphicsStack = new Stack<PDGraphicsState>();

    private Map<String, OperatorProcessor> operators = new HashMap<String, OperatorProcessor>();

    private Stack<PDResources> streamResourcesStack = new Stack<PDResources>();

    private PDPage page;

    private int validCharCnt;
    private int totalCharCnt;

    /**
     * Flag to skip malformed or otherwise unparseable input where possible.
     */
    private boolean forceParsing = false;

    /**
     * Constructor.
     */
    public PDFStreamEngine()
    {
        // default constructor
        validCharCnt = 0;
        totalCharCnt = 0;

    }

    /**
     * Constructor with engine properties. The property keys are all PDF operators, the values are class names used to
     * execute those operators. An empty value means that the operator will be silently ignored.
     * 
     * @param properties The engine properties.
     * 
     * @throws IOException If there is an error setting the engine properties.
     */
    public PDFStreamEngine(Properties properties) throws IOException
    {
        if (properties == null)
        {
            throw new NullPointerException("properties cannot be null");
        }
        Enumeration<?> names = properties.propertyNames();
        for (Object name : Collections.list(names))
        {
            String operator = name.toString();
            String processorClassName = properties.getProperty(operator);
            if ("".equals(processorClassName))
            {
                unsupportedOperators.add(operator);
            }
            else
            {
                try
                {
                    Class<?> klass = Class.forName(processorClassName);
                    OperatorProcessor processor = (OperatorProcessor) klass.newInstance();
                    registerOperatorProcessor(operator, processor);
                }
                catch (Exception e)
                {
                    throw new WrappedIOException("OperatorProcessor class " + processorClassName
                            + " could not be instantiated", e);
                }
            }
        }
        validCharCnt = 0;
        totalCharCnt = 0;
    }

    /**
     * Indicates if force parsing is activated.
     * 
     * @return true if force parsing is active
     */
    public boolean isForceParsing()
    {
        return forceParsing;
    }

    /**
     * Enable/Disable force parsing.
     * 
     * @param forceParsingValue true activates force parsing
     */
    public void setForceParsing(boolean forceParsingValue)
    {
        forceParsing = forceParsingValue;
    }

    /**
     * Register a custom operator processor with the engine.
     * 
     * @param operator The operator as a string.
     * @param op Processor instance.
     */
    public void registerOperatorProcessor(String operator, OperatorProcessor op)
    {
        op.setContext(this);
        operators.put(operator, op);
    }

    /**
     * This method must be called between processing documents. The PDFStreamEngine caches information for the document
     * between pages and this will release the cached information. This only needs to be called if processing a new
     * document.
     * 
     */
    public void resetEngine()
    {
        validCharCnt = 0;
        totalCharCnt = 0;
    }

    /**
     * This will process the contents of the stream.
     * 
     * @param aPage The page.
     * @param resources The location to retrieve resources.
     * @param cosStream the Stream to execute.
     * 
     * 
     * @throws IOException if there is an error accessing the stream.
     */
    public void processStream(PDPage aPage, PDResources resources, COSStream cosStream) throws IOException
    {
        graphicsState = new PDGraphicsState(aPage.findCropBox());
        textMatrix = null;
        textLineMatrix = null;
        graphicsStack.clear();
        streamResourcesStack.clear();
        processSubStream(aPage, resources, cosStream);
    }

    /**
     * Process a sub stream of the current stream.
     * 
     * @param aPage The page used for drawing.
     * @param resources The resources used when processing the stream.
     * @param cosStream The stream to process.
     * 
     * @throws IOException If there is an exception while processing the stream.
     */
    public void processSubStream(PDPage aPage, PDResources resources, COSStream cosStream) throws IOException
    {
        page = aPage;
        if (resources != null)
        {
            streamResourcesStack.push(resources);
            try
            {
                processSubStream(cosStream);
            }
            finally
            {
                streamResourcesStack.pop().clear();
            }
        }
        else
        {
            processSubStream(cosStream);
        }
    }

    private void processSubStream(COSStream cosStream) throws IOException
    {
        List<COSBase> arguments = new ArrayList<COSBase>();
        PDFStreamParser parser = new PDFStreamParser(cosStream, forceParsing);
        try
        {
            Iterator<Object> iter = parser.getTokenIterator();
            while (iter.hasNext())
            {
                Object next = iter.next();
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("processing substream token: " + next);
                }
                if (next instanceof COSObject)
                {
                    arguments.add(((COSObject) next).getObject());
                }
                else if (next instanceof PDFOperator)
                {
                    processOperator((PDFOperator) next, arguments);
                    arguments = new ArrayList<COSBase>();
                }
                else
                {
                    arguments.add((COSBase) next);
                }
            }
        }
        finally
        {
            parser.close();
        }
    }

    /**
     * A method provided as an event interface to allow a subclass to perform some specific functionality when text
     * needs to be processed.
     * 
     * @param text The text to be processed.
     */
    protected void processTextPosition(TextPosition text)
    {
        // subclasses can override to provide specific functionality.
    }

    /**
     * A method provided as an event interface to allow a subclass to perform some specific functionality on the string
     * encoded by a glyph.
     * 
     * @param str The string to be processed.
     */
    protected String inspectFontEncoding(String str)
    {
        return str;
    }

    /**
     * Process encoded text from the PDF Stream. You should override this method if you want to perform an action when
     * encoded text is being processed.
     * 
     * @param string The encoded text
     * 
     * @throws IOException If there is an error processing the string
     */
    public void processEncodedText(byte[] string) throws IOException
    {
        /*
         * Note on variable names. There are three different units being used in this code. Character sizes are given in
         * glyph units, text locations are initially given in text units, and we want to save the data in display units.
         * The variable names should end with Text or Disp to represent if the values are in text or disp units (no
         * glyph units are saved).
         */
        final float fontSizeText = graphicsState.getTextState().getFontSize();
        final float horizontalScalingText = graphicsState.getTextState().getHorizontalScalingPercent() / 100f;
        // float verticalScalingText = horizontalScaling;//not sure if this is right but what else to do???
        final float riseText = graphicsState.getTextState().getRise();
        final float wordSpacingText = graphicsState.getTextState().getWordSpacing();
        final float characterSpacingText = graphicsState.getTextState().getCharacterSpacing();

        // We won't know the actual number of characters until
        // we process the byte data(could be two bytes each) but
        // it won't ever be more than string.length*2(there are some cases
        // were a single byte will result in two output characters "fi"

        final PDFont font = graphicsState.getTextState().getFont();
        // all fonts are providing the width/height of a character in thousandths of a unit of text space
        float fontMatrixXScaling = 1 / 1000f;
        float fontMatrixYScaling = 1 / 1000f;
        float glyphSpaceToTextSpaceFactor = 1 / 1000f;
        // expect Type3 fonts, those are providing the width of a character in glyph space units
        if (font instanceof PDType3Font)
        {
            PDMatrix fontMatrix = font.getFontMatrix();
            fontMatrixXScaling = fontMatrix.getValue(0, 0);
            fontMatrixYScaling = fontMatrix.getValue(1, 1);
            // This will typically be 1000 but in the case of a type3 font
            // this might be a different number
            glyphSpaceToTextSpaceFactor = 1f / fontMatrix.getValue(0, 0);
        }
        float spaceWidthText = 0;
        try
        {
            // to avoid crash as described in PDFBOX-614
            // lets see what the space displacement should be
            spaceWidthText = (font.getSpaceWidth() * glyphSpaceToTextSpaceFactor);
        }
        catch (Throwable exception)
        {
            LOG.warn(exception, exception);
        }

        if (spaceWidthText == 0)
        {
            spaceWidthText = (font.getAverageFontWidth() * glyphSpaceToTextSpaceFactor);
            // The average space width appears to be higher than necessary
            // so lets make it a little bit smaller.
            spaceWidthText *= .80f;
        }

        float maxVerticalDisplacementText = 0;

        Matrix textStateParameters = new Matrix();
        textStateParameters.setValue(0, 0, fontSizeText * horizontalScalingText);
        textStateParameters.setValue(1, 1, fontSizeText);
        textStateParameters.setValue(2, 1, riseText);

        int pageRotation = page.findRotation();
        float pageHeight = page.findCropBox().getHeight();
        float pageWidth = page.findCropBox().getWidth();

        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        Matrix textXctm = new Matrix();
        Matrix textMatrixEnd = new Matrix();
        Matrix td = new Matrix();
        Matrix tempMatrix = new Matrix();

        int codeLength = 1;
        for (int i = 0; i < string.length; i += codeLength)
        {
            // Decode the value to a Unicode character
            codeLength = 1;
            String c = font.encode(string, i, codeLength);
            int[] codePoints = null;
            if (c == null && i + 1 < string.length)
            {
                // maybe a multibyte encoding
                codeLength++;
                c = font.encode(string, i, codeLength);
                codePoints = new int[] { font.getCodeFromArray(string, i, codeLength) };
            }
            else
            {
                codePoints = new int[] { font.getCodeFromArray(string, i, codeLength) };
            }

            // the space width has to be transformed into display units
            float spaceWidthDisp = spaceWidthText * fontSizeText * horizontalScalingText * textMatrix.getValue(0, 0)
                    * ctm.getValue(0, 0);

            // todo, handle horizontal displacement
            // get the width and height of this character in text units
            float characterHorizontalDisplacementText = font.getFontWidth(string, i, codeLength);
            float characterVerticalDisplacementText = font.getFontHeight(string, i, codeLength);

            // multiply the width/height with the scaling factor
            characterHorizontalDisplacementText = characterHorizontalDisplacementText * fontMatrixXScaling;
            characterVerticalDisplacementText = characterVerticalDisplacementText * fontMatrixYScaling;

            maxVerticalDisplacementText = Math.max(maxVerticalDisplacementText, characterVerticalDisplacementText);

            // PDF Spec - 5.5.2 Word Spacing
            //
            // Word spacing works the same was as character spacing, but applies
            // only to the space character, code 32.
            //
            // Note: Word spacing is applied to every occurrence of the single-byte
            // character code 32 in a string. This can occur when using a simple
            // font or a composite font that defines code 32 as a single-byte code.
            // It does not apply to occurrences of the byte value 32 in multiple-byte
            // codes.
            //
            // RDD - My interpretation of this is that only character code 32's that
            // encode to spaces should have word spacing applied. Cases have been
            // observed where a font has a space character with a character code
            // other than 32, and where word spacing (Tw) was used. In these cases,
            // applying word spacing to either the non-32 space or to the character
            // code 32 non-space resulted in errors consistent with this interpretation.
            //
            float spacingText = 0;
            if ((string[i] == 0x20) && codeLength == 1)
            {
                spacingText += wordSpacingText;
            }
            textXctm = textMatrix.multiply(ctm, textXctm);
            // Convert textMatrix to display units
            // We need to instantiate a new Matrix instance here as it is passed to the TextPosition constructor below.
            Matrix textMatrixStart = textStateParameters.multiply(textXctm);

            // TODO : tx should be set for horizontal text and ty for vertical text
            // which seems to be specified in the font (not the direction in the matrix).
            float tx = ((characterHorizontalDisplacementText) * fontSizeText) * horizontalScalingText;
            float ty = 0;
            // reset the matrix instead of creating a new one
            td.reset();
            td.setValue(2, 0, tx);
            td.setValue(2, 1, ty);

            // The text matrix gets updated after each glyph is placed. The updated
            // version will have the X and Y coordinates for the next glyph.
            // textMatrixEnd contains the coordinates of the end of the last glyph without
            // taking characterSpacingText and spacintText into account, otherwise it'll be
            // impossible to detect new words within text extraction
            tempMatrix = textStateParameters.multiply(td, tempMatrix);
            textMatrixEnd = tempMatrix.multiply(textXctm, textMatrixEnd);
            final float endXPosition = textMatrixEnd.getXPosition();
            final float endYPosition = textMatrixEnd.getYPosition();

            // add some spacing to the text matrix (see comment above)
            tx = ((characterHorizontalDisplacementText) * fontSizeText + characterSpacingText + spacingText)
                    * horizontalScalingText;
            td.setValue(2, 0, tx);
            textMatrix = td.multiply(textMatrix, textMatrix);

            // determine the width of this character
            // XXX: Note that if we handled vertical text, we should be using Y here
            float startXPosition = textMatrixStart.getXPosition();
            float widthText = endXPosition - startXPosition;

            // there are several cases where one character code will
            // output multiple characters. For example "fi" or a
            // glyphname that has no mapping like "visiblespace"
            if (c != null)
            {
                validCharCnt++;
            }
            else
            {
                // PDFBOX-373: Replace a null entry with "?" so it is
                // not printed as "(null)"
                c = "?";
            }
            totalCharCnt++;

            float totalVerticalDisplacementDisp = maxVerticalDisplacementText * fontSizeText * textXctm.getYScale();

            // process the decoded text
            processTextPosition(new TextPosition(pageRotation, pageWidth, pageHeight, textMatrixStart, endXPosition,
                    endYPosition, totalVerticalDisplacementDisp, widthText, spaceWidthDisp, c, codePoints, font,
                    fontSizeText, (int) (fontSizeText * textMatrix.getXScale())));
        }
    }

    /**
     * This is used to handle an operation.
     * 
     * @param operation The operation to perform.
     * @param arguments The list of arguments.
     * 
     * @throws IOException If there is an error processing the operation.
     */
    public void processOperator(String operation, List<COSBase> arguments) throws IOException
    {
        try
        {
            PDFOperator oper = PDFOperator.getOperator(operation);
            processOperator(oper, arguments);
        }
        catch (IOException e)
        {
            LOG.warn(e, e);
        }
    }

    /**
     * This is used to handle an operation.
     * 
     * @param operator The operation to perform.
     * @param arguments The list of arguments.
     * 
     * @throws IOException If there is an error processing the operation.
     */
    protected void processOperator(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        try
        {
            String operation = operator.getOperation();
            OperatorProcessor processor = (OperatorProcessor) operators.get(operation);
            if (processor != null)
            {
                processor.setContext(this);
                processor.process(operator, arguments);
            }
            else
            {
                if (!unsupportedOperators.contains(operation))
                {
                    LOG.info("unsupported/disabled operation: " + operation);
                    unsupportedOperators.add(operation);
                }
            }
        }
        catch (Exception e)
        {
            LOG.warn(e, e);
        }
    }

    /**
     * @return Returns the colorSpaces.
     */
    public Map<String, PDColorSpace> getColorSpaces()
    {
        return streamResourcesStack.peek().getColorSpaces();
    }

    /**
     * @return Returns the colorSpaces.
     */
    public Map<String, PDXObject> getXObjects()
    {
        return streamResourcesStack.peek().getXObjects();
    }

    /**
     * @param value The colorSpaces to set.
     */
    public void setColorSpaces(Map<String, PDColorSpace> value)
    {
        streamResourcesStack.peek().setColorSpaces(value);
    }

    /**
     * @return Returns the fonts.
     */
    public Map<String, PDFont> getFonts()
    {
        if (streamResourcesStack.isEmpty())
        {
            return Collections.emptyMap();
        }

        return streamResourcesStack.peek().getFonts();
    }

    /**
     * @param value The fonts to set.
     */
    public void setFonts(Map<String, PDFont> value)
    {
        streamResourcesStack.peek().setFonts(value);
    }

    /**
     * @return Returns the graphicsStack.
     */
    public Stack<PDGraphicsState> getGraphicsStack()
    {
        return graphicsStack;
    }

    /**
     * @param value The graphicsStack to set.
     */
    public void setGraphicsStack(Stack<PDGraphicsState> value)
    {
        graphicsStack = value;
    }

    /**
     * @return Returns the graphicsState.
     */
    public PDGraphicsState getGraphicsState()
    {
        return graphicsState;
    }

    /**
     * @param value The graphicsState to set.
     */
    public void setGraphicsState(PDGraphicsState value)
    {
        graphicsState = value;
    }

    /**
     * @return Returns the graphicsStates.
     */
    public Map<String, PDExtendedGraphicsState> getGraphicsStates()
    {
        return streamResourcesStack.peek().getGraphicsStates();
    }

    /**
     * @param value The graphicsStates to set.
     */
    public void setGraphicsStates(Map<String, PDExtendedGraphicsState> value)
    {
        streamResourcesStack.peek().setGraphicsStates(value);
    }

    /**
     * @return Returns the textLineMatrix.
     */
    public Matrix getTextLineMatrix()
    {
        return textLineMatrix;
    }

    /**
     * @param value The textLineMatrix to set.
     */
    public void setTextLineMatrix(Matrix value)
    {
        textLineMatrix = value;
    }

    /**
     * @return Returns the textMatrix.
     */
    public Matrix getTextMatrix()
    {
        return textMatrix;
    }

    /**
     * @param value The textMatrix to set.
     */
    public void setTextMatrix(Matrix value)
    {
        textMatrix = value;
    }

    /**
     * @return Returns the resources.
     */
    public PDResources getResources()
    {
        return streamResourcesStack.peek();
    }

    /**
     * Get the current page that is being processed.
     * 
     * @return The page being processed.
     */
    public PDPage getCurrentPage()
    {
        return page;
    }

    /**
     * Get the total number of valid characters in the doc that could be decoded in processEncodedText().
     * 
     * @return The number of valid characters.
     */
    public int getValidCharCnt()
    {
        return validCharCnt;
    }

    /**
     * Get the total number of characters in the doc (including ones that could not be mapped).
     * 
     * @return The number of characters.
     */
    public int getTotalCharCnt()
    {
        return totalCharCnt;
    }

}
