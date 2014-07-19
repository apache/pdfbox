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

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
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
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.util.operator.Operator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * Processes a PDF content stream and executes certain operations.
 * Provides a callback interface for clients that want to do things with the stream.
 * 
 * @author Ben Litchfield
 */
public class PDFStreamEngine
{
    private static final Log LOG = LogFactory.getLog(PDFStreamEngine.class);

    private final Set<String> unsupportedOperators = new HashSet<String>();
    private final Map<String, OperatorProcessor> operators = new HashMap<String, OperatorProcessor>();

    private Matrix textMatrix;
    private Matrix textLineMatrix;

    private final Stack<PDGraphicsState> graphicsStack = new Stack<PDGraphicsState>();
    private final Stack<PDResources> streamResourcesStack = new Stack<PDResources>();

    // skip malformed or otherwise unparseable input where possible
    private boolean forceParsing;

    /**
     * Creates a new PDFStreamEngine.
     */
    public PDFStreamEngine()
    {
    }

    /**
     * Constructor with engine properties. The property keys are all PDF operators, the values are
     * class names used to execute those operators. An empty value means that the operator will be
     * silently ignored.
     * 
     * @param properties The engine properties.
     */
    public PDFStreamEngine(Properties properties)
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
                    Class<?> cls = Class.forName(processorClassName);
                    OperatorProcessor processor = (OperatorProcessor) cls.newInstance();
                    registerOperatorProcessor(operator, processor);
                }
                catch (ClassNotFoundException e)
                {
                    // should not happen
                    throw new RuntimeException(e);
                }
                catch (InstantiationException e)
                {
                  // should not happen
                  throw new RuntimeException(e);
                }
                catch (IllegalAccessException e)
                {
                  // should not happen
                  throw new RuntimeException(e);
                }
            }
        }
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
     * Initialises a stream for processing.
     *
     * @param drawingSize the size of the page
     */
    protected void initStream(PDRectangle drawingSize)
    {
        graphicsStack.clear();
        graphicsStack.push(new PDGraphicsState(drawingSize));
        textMatrix = null;
        textLineMatrix = null;
        streamResourcesStack.clear();
    }

    /**
     * This will initialise and process the contents of the stream.
     * 
     * @param resources the location to retrieve resources
     * @param cosStream the Stream to execute
     * @param drawingSize the size of the page
     * @throws IOException if there is an error accessing the stream
     */
    public void processStream(PDResources resources, COSStream cosStream, PDRectangle drawingSize)
            throws IOException
    {
        initStream(drawingSize);
        processSubStream(resources, cosStream);
    }

    /**
     * Shows a form from the content stream.
     *
     * @param form form XObject
     * @throws IOException if the form cannot be processed
     */
    public void showForm(PDFormXObject form) throws IOException
    {
        processSubStream(form.getResources(), form.getCOSStream());
    }

    /**
     * Shows a transparency group from the content stream.
     *
     * @param form transparency group (form) XObject
     * @throws IOException if the transparency group cannot be processed
     */
    public void showTransparencyGroup(PDFormXObject form) throws IOException
    {
        showForm(form);
    }

    /**
     * Process a sub stream of the current stream.
     * 
     * @param resources the resources used when processing the stream
     * @param cosStream the stream to process
     * @throws IOException if there is an exception while processing the stream
     */
    public void processSubStream(PDResources resources, COSStream cosStream) throws IOException
    {
        // sanity check
        if (graphicsStack.isEmpty())
        {
            throw new IllegalStateException("Call to processSubStream() before processStream() " +
                                            "or initStream()");
        }

        if (resources != null)
        {
            streamResourcesStack.push(resources);
            try
            {
                processSubStream(cosStream);
            }
            finally
            {
                streamResourcesStack.pop().clearCache();
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
                else if (next instanceof Operator)
                {
                    processOperator((Operator) next, arguments);
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
     * Called when the BT operator is encountered. This method is for overriding in subclasses, the
     * default implementation does nothing.
     *
     * @throws IOException if there was an error processing the text
     */
    public void beginText() throws IOException
    {
        // overridden in subclasses
    }

    /**
     * Called when the ET operator is encountered. This method is for overriding in subclasses, the
     * default implementation does nothing.
     *
     * @throws IOException if there was an error processing the text
     */
    public void endText() throws IOException
    {
        // overridden in subclasses
    }

    /**
     * Called when a string of text is to be shown.
     *
     * @param string the encoded text
     * @throws IOException if there was an error showing the text
     */
    public void showText(byte[] string) throws IOException
    {
        processText(string);
    }

    /**
     * Called when a string of text with spacing adjustments is to be shown.
     *
     * @param strings list of the encoded text
     * @param adjustments spacing adjustment for each string
     * @throws IOException if there was an error showing the text
     */
    public void showAdjustedText(List<byte[]> strings, List<Float> adjustments) throws IOException
    {
        float fontsize = getGraphicsState().getTextState().getFontSize();
        float horizontalScaling = getGraphicsState().getTextState().getHorizontalScaling() / 100;
        for (int i = 0, len = strings.size(); i < len; i++)
        {
            float adjustment = adjustments.get(i);
            Matrix adjMatrix = new Matrix();
            adjustment =- (adjustment / 1000) * horizontalScaling * fontsize;
            // TODO vertical writing mode
            adjMatrix.setValue( 2, 0, adjustment );
            showAdjustedTextRun(strings.get(i), adjMatrix);
        }
    }

    /**
     * Called when a single run of text with a spacing adjustment is to be shown.
     *
     * @param string the encoded text
     * @param adjustment spacing adjustment to apply before showing the string
     * @throws IOException if there was an error showing the text
     */
    protected void showAdjustedTextRun(byte[] string, Matrix adjustment) throws IOException
    {
        setTextMatrix(adjustment.multiply(getTextMatrix(), adjustment));
        processText(string);
    }

    /**
     * Process text from the PDF Stream. You should override this method if you want to
     * perform an action when encoded text is being processed.
     * 
     * @param string the encoded text
     * @throws IOException if there is an error processing the string
     */
    protected void processText(byte[] string) throws IOException
    {
        // Note on variable names. There are three different units being used in this code.
        // Character sizes are given in glyph units, text locations are initially given in text
        // units, and we want to save the data in display units. The variable names should end with
        // Text or Disp to represent if the values are in text or disp units (no glyph units are
        // saved).

        PDGraphicsState graphicsState = getGraphicsState();

        final float fontSizeText = graphicsState.getTextState().getFontSize();
        final float horizontalScalingText = graphicsState.getTextState().getHorizontalScaling() / 100f;
        final float riseText = graphicsState.getTextState().getRise();
        final float wordSpacingText = graphicsState.getTextState().getWordSpacing();
        final float characterSpacingText = graphicsState.getTextState().getCharacterSpacing();

        // We won't know the actual number of characters until
        // we process the byte data(could be two bytes each) but
        // it won't ever be more than string.length*2(there are some cases
        // were a single byte will result in two output characters "fi"

        PDFont font = graphicsState.getTextState().getFont();
        if (font == null)
        {
            LOG.warn("font is undefined, creating default font");
            font = PDFontFactory.createDefaultFont();
        }
        // all fonts have the width/height of a character in thousandths of a unit of text space
        float fontMatrixXScaling = 1 / 1000f;
        float fontMatrixYScaling = 1 / 1000f;
        // expect Type3 fonts, those are providing the width of a character in glyph space units
        if (font instanceof PDType3Font)
        {
            PDMatrix fontMatrix = font.getFontMatrix();
            fontMatrixXScaling = fontMatrix.getValue(0, 0);
            fontMatrixYScaling = fontMatrix.getValue(1, 1);
        }

        float maxVerticalDisplacementText = 0;

        Matrix textStateParameters = new Matrix();
        textStateParameters.setValue(0, 0, fontSizeText * horizontalScalingText);
        textStateParameters.setValue(1, 1, fontSizeText);
        textStateParameters.setValue(2, 1, riseText);

        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        Matrix textXctm = new Matrix();
        Matrix textMatrixEnd = new Matrix();
        Matrix td = new Matrix();
        Matrix tempMatrix = new Matrix();

        int codeLength;
        for (int i = 0; i < string.length; i += codeLength)
        {
            // Decode the value to a Unicode character
            codeLength = 1;
            String unicode = font.encode(string, i, codeLength);
            int[] charCodes;
            if (unicode == null && i + 1 < string.length)
            {
                // maybe a multibyte encoding
                codeLength++;
                unicode = font.encode(string, i, codeLength);
                charCodes = new int[] { font.getCodeFromArray(string, i, codeLength) };
            }
            else
            {
                charCodes = new int[] { font.getCodeFromArray(string, i, codeLength) };
            }

            // TODO: handle horizontal displacement
            // get the width and height of this character in text units
            float charHorizontalDisplacementText = font.getFontWidth(string, i, codeLength);
            float charVerticalDisplacementText = font.getFontHeight(string, i, codeLength);

            // multiply the width/height with the scaling factor
            charHorizontalDisplacementText = charHorizontalDisplacementText * fontMatrixXScaling;
            charVerticalDisplacementText = charVerticalDisplacementText * fontMatrixYScaling;

            maxVerticalDisplacementText = Math.max(maxVerticalDisplacementText,
                    charVerticalDisplacementText);

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
            if (string[i] == 0x20 && codeLength == 1)
            {
                spacingText += wordSpacingText;
            }
            textMatrix.multiply(ctm, textXctm);
            // Convert textMatrix to display units
            // We need to instantiate a new Matrix instance here as it is passed to the TextPosition
            // constructor below
            Matrix textMatrixStart = textStateParameters.multiply(textXctm);

            // TODO: tx should be set for horizontal text and ty for vertical text
            // which seems to be specified in the font (not the direction in the matrix).
            float tx = charHorizontalDisplacementText * fontSizeText * horizontalScalingText;
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
            textStateParameters.multiply(td, tempMatrix);
            tempMatrix.multiply(textXctm, textMatrixEnd);
            final float endXPosition = textMatrixEnd.getXPosition();
            final float endYPosition = textMatrixEnd.getYPosition();

            // add some spacing to the text matrix (see comment above)
            tx = (charHorizontalDisplacementText * fontSizeText + characterSpacingText +
                    spacingText) * horizontalScalingText;
            td.setValue(2, 0, tx);
            td.multiply(textMatrix, textMatrix);

            // determine the width of this character
            // XXX: Note that if we handled vertical text, we should be using Y here
            float startXPosition = textMatrixStart.getXPosition();
            float widthText = endXPosition - startXPosition;

            float totalVerticalDisplacementDisp = maxVerticalDisplacementText * fontSizeText *
                    textXctm.getYScale();

            // process the decoded glyph
            processGlyph(textMatrixStart, new Point2D.Float(endXPosition, endYPosition),
                    totalVerticalDisplacementDisp, widthText, unicode, charCodes,
                    font, fontSizeText);
        }
    }

    /**
     * Called when a glyph is to be processed.This method is intended for overriding in subclasses,
     * the default implementation does nothing.
     *
     * @param textMatrix the text matrix at the start of the glyph
     * @param end the end position of the glyph in text space
     * @param maxHeight the height of the glyph in device space
     * @param widthText the width of the glyph in text space
     * @param unicode the Unicode text for this glyph, or null. May be meaningless.
     * @param charCodes array of internal PDF character codes for the glyph todo: should be 1 code?
     * @param font the current font
     * @param fontSize font size in text space
     * @throws IOException if the glyph cannot be processed
     */
    protected void processGlyph(Matrix textMatrix, Point2D.Float end, float maxHeight,
                                float widthText, String unicode, int[] charCodes, PDFont font,
                                float fontSize) throws IOException
    {
        // overridden in subclasses
    }

    /**
     * This is used to handle an operation.
     * 
     * @param operation The operation to perform.
     * @param arguments The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    public void processOperator(String operation, List<COSBase> arguments) throws IOException
    {
        try
        {
            Operator operator = Operator.getOperator(operation);
            processOperator(operator, arguments);
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
     * @throws IOException If there is an error processing the operation.
     */
    protected void processOperator(Operator operator, List<COSBase> arguments) throws IOException
    {
        String operation = operator.getOperation();
        OperatorProcessor processor = operators.get(operation);
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

    /**
     * @return Returns the XObjects.
     */
    public Map<String, PDXObject> getXObjects()
    {
        return streamResourcesStack.peek().getXObjects();
    }

    /**
     * @return Returns the fonts.
     */
    public Map<String, PDFont> getFonts() throws IOException
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
     * Pushes the current graphics state to the stack.
     */
    public void saveGraphicsState()
    {
        graphicsStack.push(graphicsStack.peek().clone());
    }

    /**
     * Pops the current graphics state from the stack.
     */
    public void restoreGraphicsState()
    {
        graphicsStack.pop();
    }

    /**
     * @return Returns the size of the graphicsStack.
     */
    public int getGraphicsStackSize()
    {
        return graphicsStack.size();
    }

    /**
     * @return Returns the graphicsState.
     */
    public PDGraphicsState getGraphicsState()
    {
        return graphicsStack.peek();
    }

    /**
     * @return Returns the graphicsStates.
     */
    public Map<String, PDExtendedGraphicsState> getGraphicsStates()
    {
        return streamResourcesStack.peek().getGraphicsStates();
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
     * use the current transformation matrix to transform a single point.
     *
     * @param x x-coordinate of the point to be transform
     * @param y y-coordinate of the point to be transform
     * @return the transformed coordinates as Point2D.Double
     */
    public Point2D.Double transformedPoint(double x, double y)
    {
        double[] position = { x, y };
        getGraphicsState().getCurrentTransformationMatrix().createAffineTransform()
                .transform(position, 0, position, 0, 1);
        return new Point2D.Double(position[0], position[1]);
    }

    /**
     * use the current transformation matrix to transform a PDRectangle.
     * 
     * @param rect the PDRectangle to transform
     * @return the transformed coordinates as a GeneralPath
     */
    public GeneralPath transformedPDRectanglePath(PDRectangle rect)
    {
        float x1 = rect.getLowerLeftX();
        float y1 = rect.getLowerLeftY();
        float x2 = rect.getUpperRightX();
        float y2 = rect.getUpperRightY();
        Point2D p0 = transformedPoint(x1, y1);
        Point2D p1 = transformedPoint(x2, y1);
        Point2D p2 = transformedPoint(x2, y2);
        Point2D p3 = transformedPoint(x1, y2);
        GeneralPath path = new GeneralPath();
        path.moveTo((float) p0.getX(), (float) p0.getY());
        path.lineTo((float) p1.getX(), (float) p1.getY());
        path.lineTo((float) p2.getX(), (float) p2.getY());
        path.lineTo((float) p3.getX(), (float) p3.getY());
        path.closePath();
        return path;
    }
    
    // transforms a width using the CTM
    protected float transformWidth(float width)
    {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        float x = ctm.getValue(0, 0) + ctm.getValue(1, 0);
        float y = ctm.getValue(0, 1) + ctm.getValue(1, 1);
        return width * (float)Math.sqrt((x * x + y * y) * 0.5);
    }
}
