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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
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
                if (LOG.isTraceEnabled())
                {
                    LOG.trace("processing substream token: " + next);
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
        showText(string, 0);
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
        for (int i = 0, len = strings.size(); i < len; i++)
        {
            showText(strings.get(i), adjustments.get(i));
        }
    }

    /**
     * Process text from the PDF Stream. You should override this method if you want to
     * perform an action when encoded text is being processed.
     *
     * @param string the encoded text
     * @param adjustment a position adjustment from a TJ array to be applied after the glyph
     * @throws IOException if there is an error processing the string
     */
    protected void showText(byte[] string, float adjustment) throws IOException
    {
        PDGraphicsState state = getGraphicsState();
        PDTextState textState = state.getTextState();

        // get the current font
        PDFont font = textState.getFont();
        if (font == null)
        {
            LOG.warn("No current font, will use default");
            font = PDFontFactory.createDefaultFont();
        }

        float fontSize = textState.getFontSize();
        float horizontalScaling = textState.getHorizontalScaling() / 100f;
        float charSpacing = textState.getCharacterSpacing();

        // put the text state parameters into matrix form
        Matrix parameters = new Matrix(
                fontSize * horizontalScaling, 0, // 0
                0, fontSize,                     // 0
                0, textState.getRise());         // 1

        // read the stream until it is empty
        InputStream in = new ByteArrayInputStream(string);
        while (in.available() > 0)
        {
            // decode a character
            int before = in.available();
            int code = font.readCode(in);
            int codeLength = before - in.available();
            String unicode = font.toUnicode(code);

            // Word spacing shall be applied to every occurrence of the single-byte character code
            // 32 in a string when using a simple font or a composite font that defines code 32 as
            // a single-byte code.
            float wordSpacing = 0;
            if (codeLength == 1)
            {
                if (code == 32)
                {
                    wordSpacing += textState.getWordSpacing();
                }
            }

            // text rendering matrix (text space -> device space)
            Matrix ctm = state.getCurrentTransformationMatrix();
            Matrix textRenderingMatrix = parameters.multiply(textMatrix).multiply(ctm);

            // get glyph's position vector if this is vertical text
            // changes to vertical text should be tested with PDFBOX-2294 and PDFBOX-1422
            if (font.isVertical())
            {
                // position vector, in text space
                Vector v = font.getPositionVector(code);

                // apply the position vector to the horizontal origin to get the vertical origin
                textRenderingMatrix.translate(v);
            }

            // get glyph's horizontal and vertical displacements, in text space
            Vector w = font.getDisplacement(code);

            // process the decoded glyph
            showGlyph(textRenderingMatrix, font, code, unicode, w);

            // TJ adjustment after final glyph
            float tj = 0;
            if (in.available() == 0)
            {
                tj = adjustment;
            }

            // calculate the combined displacements
            float tx, ty;
            if (font.isVertical())
            {
                tx = 0;
                ty = (w.getY() - tj / 1000) * fontSize + charSpacing + wordSpacing;
            }
            else
            {
                tx = ((w.getX() - tj / 1000) * fontSize + charSpacing + wordSpacing) *
                        horizontalScaling;
                ty = 0;
            }

            // update the text matrix
            textMatrix.concatenate(Matrix.getTranslatingInstance(tx, ty));
        }
    }

    /**
     * Called when a glyph is to be processed.This method is intended for overriding in subclasses,
     * the default implementation does nothing.
     *
     * @param textRenderingMatrix the current text rendering matrix, T<sub>rm</sub>
     * @param font the current font
     * @param code internal PDF character code for the glyph
     * @param unicode the Unicode text for this glyph, or null if the PDF does provide it
     * @param displacement the displacement (i.e. advance) of the glyph in text space
     * @throws IOException if the glyph cannot be processed
     */
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                             Vector displacement) throws IOException
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
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("unsupported/disabled operation: " + operation);
                }
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
     * use the current transformation matrix to transformPoint a single point.
     *
     * @param x x-coordinate of the point to be transformPoint
     * @param y y-coordinate of the point to be transformPoint
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
     * use the current transformation matrix to transformPoint a PDRectangle.
     * 
     * @param rect the PDRectangle to transformPoint
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
