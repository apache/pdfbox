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
package org.apache.pdfbox.pdmodel;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;

/**
 * Provides the ability to write to a page content stream.
 *
 * @author Ben Litchfield
 */
public final class PDPageContentStream extends PDAbstractContentStream implements Closeable
{
    /**
     * This is to choose what to do with the stream: overwrite, append or prepend.
     */
    public enum AppendMode
    {
        /**
         * Overwrite the existing page content streams.
         */
        OVERWRITE, 
        /**
         * Append the content stream after all existing page content streams.
         */
        APPEND, 
        /**
         * Insert before all other page content streams.
         */
        PREPEND;

        public boolean isOverwrite()
        {
            return this == OVERWRITE;
        }

        public boolean isPrepend()
        {
            return this == PREPEND;
        }
    }
  
    private static final Log LOG = LogFactory.getLog(PDPageContentStream.class);

    private boolean sourcePageHadContents = false;

    /**
     * Create a new PDPage content stream. This constructor overwrites all existing content streams
     * of this page.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(final PDDocument document, final PDPage sourcePage) throws IOException
    {
        this(document, sourcePage, AppendMode.OVERWRITE, true, false);
        if (sourcePageHadContents)
        {
            LOG.warn("You are overwriting an existing content, you should use the append mode");
        }
    }

    /**
     * Create a new PDPage content stream. If the appendContent parameter is set to
     * {@link AppendMode#APPEND}, you may want to use
     * {@link #PDPageContentStream(PDDocument, PDPage, PDPageContentStream.AppendMode, boolean, boolean)}
     * instead, with the fifth parameter set to true.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten, appended or prepended.
     * @param compress Tell if the content stream should compress the page contents.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(final PDDocument document, final PDPage sourcePage, final AppendMode appendContent,
                               final boolean compress) throws IOException
    {
        this(document, sourcePage, appendContent, compress, false);
    }
    
    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten, appended or prepended.
     * @param compress Tell if the content stream should compress the page contents.
     * @param resetContext Tell if the graphic context should be reset. This is only relevant when
     * the appendContent parameter is set to {@link AppendMode#APPEND}. You should use this when
     * appending to an existing stream, because the existing stream may have changed graphic
     * properties (e.g. scaling, rotation).
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(final PDDocument document, final PDPage sourcePage, final AppendMode appendContent,
                               final boolean compress, final boolean resetContext) throws IOException
    {
        this(document, sourcePage, appendContent, compress, resetContext, new PDStream(document),
                sourcePage.getResources() != null ? sourcePage.getResources() : new PDResources());
    }

    private PDPageContentStream(final PDDocument document, final PDPage sourcePage, final AppendMode appendContent,
                                final boolean compress, final boolean resetContext, final PDStream stream,
                                final PDResources resources) throws IOException
    {
        super(document, stream.createOutputStream(compress ? COSName.FLATE_DECODE : null), resources);

        // propagate resources to the page
        if (sourcePage.getResources() == null)
        {
            sourcePage.setResources(resources);
        }

        // If request specifies the need to append/prepend to the document
        if (!appendContent.isOverwrite() && sourcePage.hasContents())
        {
            // Add new stream to contents array
            final COSBase contents = sourcePage.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            final COSArray array;
            if (contents instanceof COSArray)
            {
                // If contents is already an array, a new stream is simply appended to it
                array = (COSArray) contents;
            }
            else
            {
                // Creates a new array and adds the current stream plus a new one to it
                array = new COSArray();
                array.add(contents);
            }

            if (appendContent.isPrepend())
            {
                array.add(0, stream.getCOSObject());
            }
            else
            {
                array.add(stream);
            }

            // save the initial/unmodified graphics context
            if (resetContext)
            {
                // create a new stream to prefix existing stream
                final PDStream prefixStream = new PDStream(document);

                // save the pre-append graphics state
                try (OutputStream prefixOut = prefixStream.createOutputStream())
                {
                    prefixOut.write("q".getBytes(StandardCharsets.US_ASCII));
                    prefixOut.write('\n');
                }

                // insert the new stream at the beginning
                array.add(0, prefixStream.getCOSObject());
            }

            // Sets the compoundStream as page contents
            sourcePage.getCOSObject().setItem(COSName.CONTENTS, array);

            // restore the pre-append graphics state
            if (resetContext)
            {
                restoreGraphicsState();
            }
        }
        else
        {
            sourcePageHadContents = sourcePage.hasContents();
            sourcePage.setContents(stream);
        }

        // configure NumberFormat
        setMaximumFractionDigits(5);
    }

    /**
     * Create a new appearance stream. Note that this is not actually a "page" content stream.
     *
     * @param doc The document the page is part of.
     * @param appearance The appearance stream to write to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(final PDDocument doc, final PDAppearanceStream appearance) throws IOException
    {
        this (doc, appearance, appearance.getStream().createOutputStream()); 
    }
    
    /**
     * Create a new appearance stream. Note that this is not actually a "page" content stream.
     *
     * @param doc The document the appearance is part of.
     * @param appearance The appearance stream to add to.
     * @param outputStream The appearances output stream to write to.
     */
    public PDPageContentStream(final PDDocument doc, final PDAppearanceStream appearance, final OutputStream outputStream)
    {
        super(doc, outputStream, appearance.getResources());
    }

    /**
     * This will draw a string at the current location on the screen.
     *
     * @param text The text to draw.
     * @throws IOException If an io exception occurs.
     * @deprecated Use {@link #showText} instead.
     */
    @Deprecated
    public void drawString(final String text) throws IOException
    {
        showText(text);
    }

    /**
     * The Td operator.
     * A current text matrix will be replaced with a new one (1 0 0 1 x y).
     * @param tx The x translation.
     * @param ty The y translation.
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #newLineAtOffset} instead.
     */
    @Deprecated
    public void moveTextPositionByAmount(final float tx, final float ty) throws IOException
    {
        newLineAtOffset(tx, ty);
    }

    /**
     * The Tm operator. Sets the text matrix to the given values.
     * A current text matrix will be replaced with the new one.
     * @param a The a value of the matrix.
     * @param b The b value of the matrix.
     * @param c The c value of the matrix.
     * @param d The d value of the matrix.
     * @param e The e value of the matrix.
     * @param f The f value of the matrix.
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #setTextMatrix(Matrix)} instead.
     */
    @Deprecated
    public void setTextMatrix(final double a, final double b, final double c, final double d, final double e, final double f) throws IOException
    {
        setTextMatrix(new Matrix((float)a, (float)b, (float)c, (float)d, (float)e, (float)f));
    }

    /**
     * The Tm operator. Sets the text matrix to the given values.
     * A current text matrix will be replaced with the new one.
     * @param matrix the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #setTextMatrix(Matrix)} instead.
     */
    @Deprecated
    public void setTextMatrix(final AffineTransform matrix) throws IOException
    {
        setTextMatrix(new Matrix(matrix));
    }

    /**
     * The Tm operator. Sets the text matrix to the given scaling and translation values.
     * A current text matrix will be replaced with the new one.
     * @param sx The scaling factor in x-direction.
     * @param sy The scaling factor in y-direction.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #setTextMatrix(Matrix)} instead.
     */
    @Deprecated
    public void setTextScaling(final double sx, final double sy, final double tx, final double ty) throws IOException
    {
        setTextMatrix(new Matrix((float) sx, 0f, 0f, (float) sy, (float) tx, (float) ty));
    }

    /**
     * The Tm operator. Sets the text matrix to the given translation values.
     * A current text matrix will be replaced with the new one.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #setTextMatrix(Matrix)} instead.
     */
    @Deprecated
    public void setTextTranslation(final double tx, final double ty) throws IOException
    {
        setTextMatrix(Matrix.getTranslateInstance((float) tx, (float) ty));
    }

    /**
     * The Tm operator. Sets the text matrix to the given rotation and translation values.
     * A current text matrix will be replaced with the new one.
     * @param angle The angle used for the counterclockwise rotation in radians.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #setTextMatrix(Matrix)} instead.
     */
    @Deprecated
    public void setTextRotation(final double angle, final double tx, final double ty) throws IOException
    {
        setTextMatrix(Matrix.getRotateInstance(angle, (float) tx, (float) ty));
    }

    /**
     * Draw an inline image at the x,y coordinates, with the default size of the image.
     *
     * @param inlineImage The inline image to draw.
     * @param x The x-coordinate to draw the inline image.
     * @param y The y-coordinate to draw the inline image.
     *
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #drawImage(PDInlineImage, float, float)} instead.
     */
    @Deprecated
    public void drawInlineImage(final PDInlineImage inlineImage, final float x, final float y) throws IOException
    {
        drawImage(inlineImage, x, y, inlineImage.getWidth(), inlineImage.getHeight());
    }

    /**
     * Draw an inline image at the x,y coordinates and a certain width and height.
     *
     * @param inlineImage The inline image to draw.
     * @param x The x-coordinate to draw the inline image.
     * @param y The y-coordinate to draw the inline image.
     * @param width The width of the inline image to draw.
     * @param height The height of the inline image to draw.
     *
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #drawImage(PDInlineImage, float, float, float, float)} instead.
     */
    @Deprecated
    public void drawInlineImage(final PDInlineImage inlineImage, final float x, final float y, final float width, final float height) throws IOException
    {
        drawImage(inlineImage, x, y, width, height);
    }

    /**
     * Draw an xobject(form or image) at the x,y coordinates and a certain width and height.
     *
     * @param xobject The xobject to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     * @param width The width of the image to draw.
     * @param height The height of the image to draw.
     *
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #drawImage} instead.
     */
    @Deprecated
    public void drawXObject(final PDXObject xobject, final float x, final float y, final float width, final float height) throws IOException
    {
        final AffineTransform transform = new AffineTransform(width, 0, 0, height, x, y);
        drawXObject(xobject, transform);
    }

    /**
     * Draw an xobject(form or image) using the given {@link AffineTransform} to position
     * the xobject.
     *
     * @param xobject The xobject to draw.
     * @param transform the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #drawImage(PDImageXObject, Matrix) drawImage(PDImageXObject, Matrix)}
     * or {@link #drawForm(PDFormXObject) drawForm(PDFormXObject)} with
     * {@link #transform(Matrix) transform(Matrix)} instead.
     */
    @Deprecated
    public void drawXObject(final PDXObject xobject, final AffineTransform transform) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawXObject is not allowed within a text block.");
        }

        final String xObjectPrefix;
        if (xobject instanceof PDImageXObject)
        {
            xObjectPrefix = "Im";
        }
        else
        {
            xObjectPrefix = "Form";
        }
        final COSName objMapping = resources.add(xobject, xObjectPrefix);

        saveGraphicsState();
        transform(new Matrix(transform));

        writeOperand(objMapping);
        writeOperator(OperatorName.DRAW_OBJECT);

        restoreGraphicsState();
    }

    /**
     * The cm operator. Concatenates the current transformation matrix with the given values.
     * @param a The a value of the matrix.
     * @param b The b value of the matrix.
     * @param c The c value of the matrix.
     * @param d The d value of the matrix.
     * @param e The e value of the matrix.
     * @param f The f value of the matrix.
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #transform} instead.
     */
    @Deprecated
    public void concatenate2CTM(final double a, final double b, final double c, final double d, final double e, final double f) throws IOException
    {
        transform(new Matrix((float) a, (float) b, (float) c, (float) d, (float) e, (float) f));
    }

    /**
     * The cm operator. Concatenates the current transformation matrix with the given
     * {@link AffineTransform}.
     * @param at the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     * @deprecated Use {@link #transform} instead.
     */
    @Deprecated
    public void concatenate2CTM(final AffineTransform at) throws IOException
    {
        transform(new Matrix(at));
    }

    /**
     * Fill a rectangle on the page using the current non stroking color.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #addRect} followed by {@link #fill()} instead.
     */
    @Deprecated
    public void fillRect(final float x, final float y, final float width, final float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: fillRect is not allowed within a text block.");
        }
        addRect(x, y, width, height);
        fill();
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using (x1 , y1 ) and (x2 , y2 ) as the Bézier control points
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     * @deprecated Use {@link #curveTo} instead.
     */
    @Deprecated
    public void addBezier312(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) throws IOException
    {
        curveTo(x1, y1, x2, y2, x3, y3);
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using the current point and (x2 , y2 ) as the Bézier control points/
     *
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     * @deprecated Use {@link #curveTo2} instead.
     */
    @Deprecated
    public void addBezier32(final float x2, final float y2, final float x3, final float y3) throws IOException
    {
        curveTo2(x2, y2, x3, y3);
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using (x1 , y1 ) and (x3 , y3 ) as the Bézier control points/
     *
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     * @deprecated Use {@link #curveTo1} instead.
     */
    @Deprecated
    public void addBezier31(final float x1, final float y1, final float x3, final float y3) throws IOException
    {
        curveTo1(x1, y1, x3, y3);
    }

    /**
     * add a line to the current path.
     *
     * @param xStart The start x coordinate.
     * @param yStart The start y coordinate.
     * @param xEnd The end x coordinate.
     * @param yEnd The end y coordinate.
     * @throws IOException If there is an error while adding the line.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #moveTo moveto(xStart,yStart)} followed by
     * {@link #lineTo lineTo(xEnd,yEnd)}.
     */
    @Deprecated
    public void addLine(final float xStart, final float yStart, final float xEnd, final float yEnd) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: addLine is not allowed within a text block.");
        }
        moveTo(xStart, yStart);
        lineTo(xEnd, yEnd);
    }

    /**
     * Draw a line on the page using the current stroking color and the current line width.
     *
     * @param xStart The start x coordinate.
     * @param yStart The start y coordinate.
     * @param xEnd The end x coordinate.
     * @param yEnd The end y coordinate.
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #moveTo moveto(xStart,yStart)} followed by
     * {@link #lineTo lineTo(xEnd,yEnd)} followed by {@link #stroke stroke()}.
     */
    @Deprecated
    public void drawLine(final float xStart, final float yStart, final float xEnd, final float yEnd) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawLine is not allowed within a text block.");
        }
        moveTo(xStart, yStart);
        lineTo(xEnd, yEnd);
        stroke();
    }

    /**
     * Add a polygon to the current path.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @throws IllegalArgumentException If the two arrays have different lengths.
     * @deprecated Use {@link #moveTo} and {@link #lineTo} methods instead.
     */
    @Deprecated
    public void addPolygon(final float[] x, final float[] y) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: addPolygon is not allowed within a text block.");
        }
        if (x.length != y.length)
        {
            throw new IllegalArgumentException("Error: some points are missing coordinate");
        }
        for (int i = 0; i < x.length; i++)
        {
            if (i == 0)
            {
                moveTo(x[i], y[i]);
            }
            else
            {
                lineTo(x[i], y[i]);
            }
        }
        closeSubPath();
    }

    /**
     * Draw a polygon on the page using the current stroking color.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #moveTo} and {@link #lineTo} methods instead.
     */
    @Deprecated
    public void drawPolygon(final float[] x, final float[] y) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawPolygon is not allowed within a text block.");
        }
        addPolygon(x, y);
        stroke();
    }

    /**
     * Draw and fill a polygon on the page using the current stroking / non stroking colors.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #moveTo} and {@link #lineTo} methods instead.
     */
    @Deprecated
    public void fillPolygon(final float[] x, final float[] y) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: fillPolygon is not allowed within a text block.");
        }
        addPolygon(x, y);
        fill();
    }

    /**
     * Fill the path.
     * 
     * @param windingRule the winding rule to be used for filling
     * @throws IOException If the content stream could not be written
     * @throws IllegalArgumentException If the parameter is not a valid winding rule.
     * @deprecated Use {@link #fill()} or {@link #fillEvenOdd} instead.
     */
    @Deprecated
    public void fill(final int windingRule) throws IOException
    {
        switch (windingRule)
        {
            case PathIterator.WIND_NON_ZERO:
                fill();
                break;
            case PathIterator.WIND_EVEN_ODD:
                fillEvenOdd();
                break;
            default:
                throw new IllegalArgumentException("Error: unknown value for winding rule");
        }
    }

    /**
     * Closes the current subpath.
     * 
     * @throws IOException If the content stream could not be written
     * @deprecated Use {@link #closePath()} instead.
     */
    @Deprecated
    public void closeSubPath() throws IOException
    {
        closePath();
    }

    /**
     * Clip path.
     * 
     * @param windingRule the winding rule to be used for clipping
     * @throws IOException If there is an error while clipping the path.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #clip()} or {@link #clipEvenOdd} instead.
     */
    @Deprecated
    public void clipPath(final int windingRule) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: clipPath is not allowed within a text block.");
        }
        switch (windingRule)
        {
            case PathIterator.WIND_NON_ZERO:
            writeOperator(OperatorName.CLIP_NON_ZERO);
                break;
            case PathIterator.WIND_EVEN_ODD:
            writeOperator(OperatorName.CLIP_EVEN_ODD);
                break;
            default:
                throw new IllegalArgumentException("Error: unknown value for winding rule");
        }
        writeOperator(OperatorName.ENDPATH);
    }

    /**
     * Begin a marked content sequence.
     *
     * @param tag the tag
     * @throws IOException if an I/O error occurs
     * @deprecated Use {@link #beginMarkedContent} instead.
     */
    @Deprecated
    public void beginMarkedContentSequence(final COSName tag) throws IOException
    {
        beginMarkedContent(tag);
    }

    /**
     * Begin a marked content sequence with a reference to an entry in the page resources'
     * Properties dictionary.
     *
     * @param tag the tag
     * @param propsName the properties reference
     * @throws IOException if an I/O error occurs
     * @deprecated Use {@link #beginMarkedContent(COSName, PDPropertyList)} instead.
     */
    @Deprecated
    public void beginMarkedContentSequence(final COSName tag, final COSName propsName) throws IOException
    {
        writeOperand(tag);
        writeOperand(propsName);
        writeOperator(OperatorName.BEGIN_MARKED_CONTENT_SEQ);
    }

    /**
     * End a marked content sequence.
     *
     * @throws IOException If the content stream could not be written
     * @deprecated Use {@link #endMarkedContent} instead.
     */
    @Deprecated
    public void endMarkedContentSequence() throws IOException
    {
        endMarkedContent();
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(final String commands) throws IOException
    {
        write(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(final byte[] commands) throws IOException
    {
        write(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a raw byte to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(final int data) throws IOException
    {
        writeOperand(data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted double value to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(final double data) throws IOException
    {
        writeOperand((float) data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted float value to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendRawCommands(final float data) throws IOException
    {
        writeOperand(data);
    }

    /**
     * This will append a {@link COSName} to the content stream.
     *
     * @param name the name
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated Usage of this method is discouraged.
     */
    @Deprecated
    public void appendCOSName(final COSName name) throws IOException
    {
        writeOperand(name);
    }
}
