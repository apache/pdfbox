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
package org.apache.pdfbox.contentstream;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;

/**
 * Processes a PDF content stream and executes certain operations.
 * Provides a callback interface for clients that want to do things with the stream.
 * 
 * @author Ben Litchfield
 */
public class PDFStreamEngine
{
    private static final Log LOG = LogFactory.getLog(PDFStreamEngine.class);

    private final Map<String, OperatorProcessor> operators = new HashMap<String, OperatorProcessor>();

    private Matrix textMatrix;
    private Matrix textLineMatrix;
    protected Matrix subStreamMatrix = new Matrix();

    private final Stack<PDGraphicsState> graphicsStack = new Stack<PDGraphicsState>();

    private PDResources resources;
    private PDPage currentPage;
    private boolean isProcessingPage;

    // skip malformed or otherwise unparseable input where possible
    private boolean forceParsing;

    /**
     * Creates a new PDFStreamEngine.
     */
    public PDFStreamEngine()
    {
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
     * @deprecated Use {@link #addOperator(OperatorProcessor)} instead
     */
    @Deprecated
    public void registerOperatorProcessor(String operator, OperatorProcessor op)
    {
        op.setContext(this);
        operators.put(operator, op);
    }

    /**
     * Adds an operator processor to the engine.
     *
     * @param op operator processor
     */
    public final void addOperator(OperatorProcessor op)
    {
        op.setContext(this);
        operators.put(op.getName(), op);
    }

    /**
     * Initialises the stream engine for the given page.
     */
    private void initPage(PDPage page)
    {
        if (page == null)
        {
            throw new IllegalArgumentException("Page cannot be null");
        }
        currentPage = page;
        graphicsStack.clear();
        graphicsStack.push(new PDGraphicsState(page.getCropBox()));
        textMatrix = null;
        textLineMatrix = null;
        resources = null;
    }

    /**
     * This will initialise and process the contents of the stream.
     *
     * @param page the page to process
     * @throws IOException if there is an error accessing the stream
     */
    public void processPage(PDPage page) throws IOException
    {
        initPage(page);
        if (page.getStream() != null)
        {
            isProcessingPage = true;
            processStream(page);
            isProcessingPage = false;
        }
    }

    /**
     * Shows a transparency group from the content stream.
     *
     * @param form transparency group (form) XObject
     * @throws IOException if the transparency group cannot be processed
     */
    public void showTransparencyGroup(PDFormXObject form) throws IOException
    {
        processTransparencyGroup(form);
    }

    /**
     * Shows a form from the content stream.
     *
     * @param form form XObject
     * @throws IOException if the form cannot be processed
     */
    public void showForm(PDFormXObject form) throws IOException
    {
        processChildStream(form);
    }

    /**
     * Process a child stream of the current page. For use with #processPage(PDPage).
     *
     * @param contentStream the child content stream
     * @throws IOException if there is an exception while processing the stream
     */
    public void processChildStream(PDContentStream contentStream) throws IOException
    {
        if (currentPage == null)
        {
            throw new IllegalStateException("No current page, call " +
                    "#processChildStream(PDContentStream, PDPage) instead");
        }
        processStream(contentStream);
    }

    /**
     * Processes a transparency group stream.
     */
    protected void processTransparencyGroup(PDFormXObject group)
            throws IOException
    {
        if (currentPage == null)
        {
            throw new IllegalStateException("No current page, call " +
                    "#processChildStream(PDContentStream, PDPage) instead");
        }

        PDResources parent = pushResources(group);
        saveGraphicsState();

        // transform the CTM using the stream's matrix
        getGraphicsState().getCurrentTransformationMatrix().concatenate(group.getMatrix());

        // clip to bounding box
        clipToRect(group.getBBox());

        processStreamOperators(group);

        restoreGraphicsState();
        popResources(parent);
    }

    /**
     * Processes a Type 3 character stream.
     *
     * @param charProc Type 3 character procedure
     * @param textRenderingMatrix the Text Rendering Matrix
     */
    protected void processType3Stream(PDType3CharProc charProc, Matrix textRenderingMatrix)
            throws IOException
    {
        if (currentPage == null)
        {
            throw new IllegalStateException("No current page, call " +
                    "#processChildStream(PDContentStream, PDPage) instead");
        }

        PDResources parent = pushResources(charProc);
        saveGraphicsState();

        // replace the CTM with the TRM
        getGraphicsState().setCurrentTransformationMatrix(textRenderingMatrix);

        // transform the CTM using the stream's matrix (this is the FontMatrix)
        getGraphicsState().getCurrentTransformationMatrix().concatenate(charProc.getMatrix());

        // note: we don't clip to the BBox as it is often wrong, see PDFBOX-1917

        // save text matrices (Type 3 stream may contain BT/ET, see PDFBOX-2137)
        Matrix textMatrixOld = textMatrix;
        textMatrix = new Matrix();
        Matrix textLineMatrixOld = textLineMatrix;
        textLineMatrix = new Matrix();

        processStreamOperators(charProc);

        // restore text matrices
        textMatrix = textMatrixOld;
        textLineMatrix = textLineMatrixOld;

        restoreGraphicsState();
        popResources(parent);
    }

    /**
     * Process the given annotation with the specified appearance stream.
     *
     * @param annotation The annotation containing the appearance stream to process.
     * @param appearance The appearance stream to process.
     */
    protected void processAnnotation(PDAnnotation annotation, PDAppearanceStream appearance)
            throws IOException
    {
        PDResources parent = pushResources(appearance);
        saveGraphicsState();

        PDRectangle bbox = appearance.getBBox();
        PDRectangle rect = annotation.getRectangle();
        Matrix matrix = appearance.getMatrix();

        // transformed appearance box
        PDRectangle transformedBox = bbox.transform(matrix);

        // compute a matrix which scales and translates the transformed appearance box to align
        // with the edges of the annotation's rectangle
        Matrix a = Matrix.getTranslatingInstance(rect.getLowerLeftX(), rect.getLowerLeftY());
        a.concatenate(Matrix.getScaleInstance(rect.getWidth() / transformedBox.getWidth(),
                                              rect.getHeight() / transformedBox.getHeight()));
        a.concatenate(Matrix.getTranslatingInstance(-transformedBox.getLowerLeftX(),
                                                    -transformedBox.getLowerLeftY()));

        // Matrix shall be concatenated with A to form a matrix AA that maps from the appearance’s
        // coordinate system to the annotation’s rectangle in default user space
        Matrix aa = Matrix.concatenate(matrix, a);

        // make matrix AA the CTM
        getGraphicsState().setCurrentTransformationMatrix(aa);

        // clip to bounding box
        clipToRect(bbox);

        processStreamOperators(appearance);

        restoreGraphicsState();
        popResources(parent);
    }

    /**
     * Processes the given tiling pattern.
     *
     * @param tilingPattern tiling patten
     */
    protected final void processTilingPattern(PDTilingPattern tilingPattern) throws IOException
    {
        PDResources parent = pushResources(tilingPattern);
        saveGraphicsState();

        // note: we don't transform the CTM using the stream's matrix, as TilingPaint handles this

        // clip to bounding box
        PDRectangle bbox = tilingPattern.getBBox();
        clipToRect(bbox);

        processStreamOperators(tilingPattern);

        restoreGraphicsState();
        popResources(parent);
    }

    /**
     * Shows the given annotation.
     *
     * @param annotation An annotation on the current page.
     * @throws IOException If an error occurred reading the annotation
     */
    public void showAnnotation(PDAnnotation annotation) throws IOException
    {
        PDAppearanceStream appearanceStream = getAppearance(annotation);
        if (appearanceStream != null)
        {
            processAnnotation(annotation, appearanceStream);
        }
    }

    /**
     * Returns the appearance stream to process for the given annotation. May be used to render
     * a specific appearance such as "hover".
     *
     * @param annotation The current annotation.
     * @return The stream to process.
     */
    public PDAppearanceStream getAppearance(PDAnnotation annotation)
    {
        return annotation.getNormalAppearanceStream();
    }

    /**
     * Process a child stream of the given page. Cannot be used with #processPage(PDPage).
     *
     * @param contentStream the child content stream
     * @throws IOException if there is an exception while processing the stream
     */
    protected void processChildStream(PDContentStream contentStream, PDPage page) throws IOException
    {
        if (isProcessingPage)
        {
            throw new IllegalStateException("Current page has already been set via " +
                    " #processPage(PDPage) call #processChildStream(PDContentStream) instead");
        }
        initPage(page);
        processStream(contentStream);
        currentPage = null;
    }

    /**
     * Process a content stream.
     *
     * @param contentStream the content stream
     * @throws IOException if there is an exception while processing the stream
     */
    private void processStream(PDContentStream contentStream) throws IOException
    {
        processStream(contentStream, null);
    }

    /**
     * Process a content stream.
     *
     * @param contentStream the content stream
     * @param patternBBox fixme: temporary workaround for tiling patterns
     * @throws IOException if there is an exception while processing the stream
     */
    private void processStream(PDContentStream contentStream, PDRectangle patternBBox)
            throws IOException
    {
        PDResources parent = pushResources(contentStream);
        saveGraphicsState();

        // transform the CTM using the stream's matrix
        getGraphicsState().getCurrentTransformationMatrix().concatenate(contentStream.getMatrix());

        // clip to bounding box
        PDRectangle bbox = contentStream.getBBox();
        if (patternBBox != null)
        {
            bbox = patternBBox;
        }
        clipToRect(bbox);

        processStreamOperators(contentStream);

        restoreGraphicsState();
        popResources(parent);
    }

    /**
     * Processes the operators of the given content stream.
     */
    private void processStreamOperators(PDContentStream contentStream) throws IOException
    {
        // fixme: stream matrix
        Matrix oldSubStreamMatrix = subStreamMatrix;
        subStreamMatrix = getGraphicsState().getCurrentTransformationMatrix();

        List<COSBase> arguments = new ArrayList<COSBase>();
        PDFStreamParser parser = new PDFStreamParser(contentStream.getContentStream(), forceParsing);
        try
        {
            Iterator<Object> iter = parser.getTokenIterator();
            while (iter.hasNext())
            {
                Object token = iter.next();
                if (token instanceof COSObject)
                {
                    arguments.add(((COSObject) token).getObject());
                }
                else if (token instanceof Operator)
                {
                    processOperator((Operator) token, arguments);
                    arguments = new ArrayList<COSBase>();
                }
                else
                {
                    arguments.add((COSBase) token);
                }
            }
        }
        finally
        {
            parser.close();
        }

        // fixme: stream matrix
        subStreamMatrix = oldSubStreamMatrix;
    }

    /**
     * Pushes the given stream's resources, returning the previous resources.
     */
    private PDResources pushResources(PDContentStream contentStream)
    {
        // resource lookup: first look for stream resources, then fallback to the current page
        PDResources parentResources = resources;
        PDResources streamResources = contentStream.getResources();
        if (streamResources != null)
        {
            resources = streamResources;
        }
        else
        {
            resources = currentPage.getResources();
        }

        // resources are required in PDF
        if (resources == null)
        {
            resources = new PDResources();
        }
        return parentResources;
    }

    /**
     * Pops the current resources, replacing them with the given resources.
     */
    private void popResources(PDResources parentResources)
    {
        resources = parentResources;
    }

    /**
     * Transforms the given rectangle using the CTM and then intersects it with the current
     * clipping area.
     */
    private void clipToRect(PDRectangle rectangle)
    {
        if (rectangle != null)
        {
            PDRectangle clip = rectangle.transform(getGraphicsState().getCurrentTransformationMatrix());
            getGraphicsState().intersectClippingPath(new Area(clip.toGeneralPath()));
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
    public void showTextString(byte[] string) throws IOException
    {
        showText(string);
    }

    /**
     * Called when a string of text with spacing adjustments is to be shown.
     *
     * @param array array of encoded text strings and adjustments
     * @throws IOException if there was an error showing the text
     */
    public void showTextStrings(COSArray array) throws IOException
    {
        PDTextState textState = getGraphicsState().getTextState();
        float fontSize = textState.getFontSize();
        float horizontalScaling = textState.getHorizontalScaling() / 100f;
        boolean isVertical = textState.getFont().isVertical();

        for (COSBase obj : array)
        {
            if (obj instanceof COSNumber)
            {
                float tj = ((COSNumber)obj).floatValue();

                // calculate the combined displacements
                float tx, ty;
                if (isVertical)
                {
                    tx = 0;
                    ty = -tj / 1000 * fontSize;
                }
                else
                {
                    tx = -tj / 1000 * fontSize * horizontalScaling;
                    ty = 0;
                }

                applyTextAdjustment(tx, ty);
            }
            else if(obj instanceof COSString)
            {
                byte[] string = ((COSString)obj).getBytes();
                showText(string);
            }
            else
            {
                throw new IOException("Unknown type in array for TJ operation:" + obj);
            }
        }
    }

    /**
     * Applies a text position adjustment from the TJ operator. May be overridden in subclasses.
     *
     * @param tx x-translation
     * @param ty y-translation
     */
    protected void applyTextAdjustment(float tx, float ty) throws IOException
    {
        // update the text matrix
        textMatrix.concatenate(Matrix.getTranslatingInstance(tx, ty));
    }

    /**
     * Process text from the PDF Stream. You should override this method if you want to
     * perform an action when encoded text is being processed.
     *
     * @param string the encoded text
     * @throws IOException if there is an error processing the string
     */
    protected void showText(byte[] string) throws IOException
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
            if (codeLength == 1 && code == 32)
            {
                wordSpacing += textState.getWordSpacing();
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
            saveGraphicsState();
            showGlyph(textRenderingMatrix, font, code, unicode, w);
            restoreGraphicsState();

            // calculate the combined displacements
            float tx, ty;
            if (font.isVertical())
            {
                tx = 0;
                ty = w.getY() * fontSize + charSpacing + wordSpacing;
            }
            else
            {
                tx = (w.getX() * fontSize + charSpacing + wordSpacing) * horizontalScaling;
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
        if (font instanceof PDType3Font)
        {
            showType3Glyph(textRenderingMatrix, (PDType3Font)font, code, unicode, displacement);
        }
        else
        {
            showFontGlyph(textRenderingMatrix, font, code, unicode, displacement);
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
    protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException
    {
        // overridden in subclasses
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
    protected void showType3Glyph(Matrix textRenderingMatrix, PDType3Font font, int code,
                                  String unicode, Vector displacement) throws IOException
    {
        PDType3CharProc charProc = font.getCharProc(code);
        if (charProc != null)
        {
            processType3Stream(charProc, textRenderingMatrix);
        }
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
        Operator operator = Operator.getOperator(operation);
        processOperator(operator, arguments);
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
        String name = operator.getName();
        OperatorProcessor processor = operators.get(name);
        if (processor != null)
        {
            processor.setContext(this);
            processor.process(operator, arguments);
        }
        else
        {
            unsupportedOperator(operator, arguments);
        }
    }

    /**
     * Called when an unsupported operator is encountered.
     *
     * @param operator The unknown operator.
     * @param arguments The list of arguments.
     */
    protected void unsupportedOperator(Operator operator, List<COSBase> arguments) throws IOException
    {
        // overridden in subclasses
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
     * Returns the subStreamMatrix.
     */
    protected Matrix getSubStreamMatrix()
    {
        return subStreamMatrix;
    }
    
    /**
     * Returns the stream' resources.
     */
    public PDResources getResources()
    {
        return resources;
    }

    /**
     * Returns the current page.
     */
    public PDPage getCurrentPage()
    {
        return currentPage;
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
    
    // transforms a width using the CTM
    protected float transformWidth(float width)
    {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        float x = ctm.getValue(0, 0) + ctm.getValue(1, 0);
        float y = ctm.getValue(0, 1) + ctm.getValue(1, 1);
        return width * (float)Math.sqrt((x * x + y * y) * 0.5);
    }
}
