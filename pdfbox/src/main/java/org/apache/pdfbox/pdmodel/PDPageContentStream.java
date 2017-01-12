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
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDAbstractContentStream;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
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

    private final PDDocument document;

    private final Stack<PDFont> fontStack = new Stack<PDFont>();

    private final Stack<PDColorSpace> nonStrokingColorSpaceStack = new Stack<PDColorSpace>();
    private final Stack<PDColorSpace> strokingColorSpaceStack = new Stack<PDColorSpace>();

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage) throws IOException
    {
        this(document, sourcePage, AppendMode.OVERWRITE, true, false);
    }

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten. If false all previous
     *                      content is deleted.
     * @param compress Tell if the content stream should compress the page contents.
     * @throws IOException If there is an error writing to the page contents.
     * @deprecated use {@link #PDPageContentStream(PDDocument, PDPage, PDPageContentStream.AppendMode, boolean)}
     */
    @Deprecated
    public PDPageContentStream(PDDocument document, PDPage sourcePage, boolean appendContent,
                               boolean compress) throws IOException
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
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage, AppendMode appendContent,
                               boolean compress) throws IOException
    {
        this(document, sourcePage, appendContent, compress, false);
    }

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten. If false all previous
     *                      content is deleted.
     * @param compress Tell if the content stream should compress the page contents.
     * @param resetContext Tell if the graphic context should be reseted. You should use this when
     * appending to an existing stream, because the existing stream may have changed graphic
     * properties (e.g. scaling, rotation).
     * @throws IOException If there is an error writing to the page contents.
     * @deprecated use {@link #PDPageContentStream(PDDocument, PDPage, PDPageContentStream.AppendMode, boolean, boolean) }
     */
    @Deprecated
    public PDPageContentStream(PDDocument document, PDPage sourcePage, boolean appendContent,
                               boolean compress, boolean resetContext) throws IOException
    {
      this (document, sourcePage, appendContent ? AppendMode.APPEND : AppendMode.OVERWRITE, compress, resetContext);
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
    public PDPageContentStream(PDDocument document, PDPage sourcePage, AppendMode appendContent,
                               boolean compress, boolean resetContext) throws IOException
    {
        super();
        this.document = document;
        COSName filter = compress ? COSName.FLATE_DECODE : null;
        
        // If request specifies the need to append/prepend to the document
        if (!appendContent.isOverwrite() && sourcePage.hasContents())
        {
            // Create a stream to append new content
            PDStream contentsToAppend = new PDStream(document);
            
            // Add new stream to contents array
            COSBase contents = sourcePage.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            COSArray array;
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
                array.add(0, contentsToAppend.getCOSObject());
            }
            else
            {
                array.add(contentsToAppend);
            }

            // save the initial/unmodified graphics context
            if (resetContext)
            {
                // create a new stream to encapsulate the existing stream
                PDStream saveGraphics = new PDStream(document);
                setOutput(saveGraphics.createOutputStream(filter));
                
                // save the initial/unmodified graphics context
                saveGraphicsState();
                close();
                
                // insert the new stream at the beginning
                array.add(0, saveGraphics.getCOSObject());
            }

            // Sets the compoundStream as page contents
            sourcePage.getCOSObject().setItem(COSName.CONTENTS, array);
            setOutput(contentsToAppend.createOutputStream(filter));

            // restore the initial/unmodified graphics context
            if (resetContext)
            {
                restoreGraphicsState();
            }
        }
        else
        {
            if (sourcePage.hasContents())
            {
                LOG.warn("You are overwriting an existing content, you should use the append mode");
            }
            PDStream contents = new PDStream(document);
            sourcePage.setContents(contents);
            setOutput(contents.createOutputStream(filter));
        }
        
        // this has to be done here, as the resources will be set to null when resetting the content
        // stream
        PDResources resources = sourcePage.getResources();
        if (resources == null)
        {
            resources = new PDResources();
            sourcePage.setResources(resources);
        }
        setResources(resources);
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
    public PDPageContentStream(PDDocument doc, PDAppearanceStream appearance) throws IOException
    {
        this (doc, appearance, appearance.getStream().createOutputStream()); 
    }
    
    /**
     * Create a new appearance stream. Note that this is not actually a "page" content stream.
     *
     * @param doc The document the appearance is part of.
     * @param appearance The appearance stream to add to.
     * @param outputStream The appearances output stream to write to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument doc, PDAppearanceStream appearance, OutputStream outputStream)
            throws IOException
    {
        super(appearance, outputStream);
        this.document = doc;
        
        setResources(appearance.getResources());
    }

    /**
     * Set the font and font size to draw text with.
     *
     * @param font The font to use.
     * @param fontSize The font size to draw the text.
     * @throws IOException If there is an error writing the font information.
     */
    @Override
    public void setFont(PDFont font, float fontSize) throws IOException
    {
        if (fontStack.isEmpty())
        {
            fontStack.add(font);
        }
        else
        {
            fontStack.setElementAt(font, fontStack.size() - 1);
        }
        
        if (font.willBeSubset())
        {
            document.getFontsToSubset().add(font);
        }
        
        writeOperand(getResources().add(font));
        writeOperand(fontSize);
        writeOperator("Tf");
    }

    /**
     * This will draw a string at the current location on the screen.
     *
     * @param text The text to draw.
     * @throws IOException If an io exception occurs.
     * @deprecated Use {@link #showText} instead.
     */
    @Deprecated
    public void drawString(String text) throws IOException
    {
        showText(text);
    }

    /**
     * Shows the given text at the location specified by the current text matrix.
     *
     * @param text The Unicode text to show.
     * @throws IOException If an io exception occurs.
     */
    @Override
    public void showText(String text) throws IOException
    {
        if (!isInTextMode())
        {
            throw new IllegalStateException("Must call beginText() before showText()");
        }

        if (fontStack.isEmpty())
        {
            throw new IllegalStateException("Must call setFont() before showText()");
        }

        PDFont font = fontStack.peek();

        // Unicode code points to keep when subsetting
        if (font.willBeSubset())
        {
            for (int offset = 0; offset < text.length(); )
            {
                int codePoint = text.codePointAt(offset);
                font.addToSubset(codePoint);
                offset += Character.charCount(codePoint);
            }
        }

        
        COSWriter.writeString(font.encode(text), getOutput());
        write(" ");

        writeOperator("Tj");
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
    public void moveTextPositionByAmount(float tx, float ty) throws IOException
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
    public void setTextMatrix(double a, double b, double c, double d, double e, double f) throws IOException
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
    public void setTextMatrix(AffineTransform matrix) throws IOException
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
    public void setTextScaling(double sx, double sy, double tx, double ty) throws IOException
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
    public void setTextTranslation(double tx, double ty) throws IOException
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
    public void setTextRotation(double angle, double tx, double ty) throws IOException
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
    public void drawInlineImage(PDInlineImage inlineImage, float x, float y) throws IOException
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
    public void drawInlineImage(PDInlineImage inlineImage, float x, float y, float width, float height) throws IOException
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
    public void drawXObject(PDXObject xobject, float x, float y, float width, float height) throws IOException
    {
        AffineTransform transform = new AffineTransform(width, 0, 0, height, x, y);
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
    public void drawXObject(PDXObject xobject, AffineTransform transform) throws IOException
    {
        if (isInTextMode())
        {
            throw new IllegalStateException("Error: drawXObject is not allowed within a text block.");
        }

        String xObjectPrefix;
        if (xobject instanceof PDImageXObject)
        {
            xObjectPrefix = "Im";
        }
        else
        {
            xObjectPrefix = "Form";
        }
        COSName objMapping = getResources().add(xobject, xObjectPrefix);

        saveGraphicsState();
        transform(new Matrix(transform));

        writeOperand(objMapping);
        writeOperator("Do");

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
    public void concatenate2CTM(double a, double b, double c, double d, double e, double f) throws IOException
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
    public void concatenate2CTM(AffineTransform at) throws IOException
    {
        transform(new Matrix(at));
    }

    /**
     * q operator. Saves the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    @Override
    public void saveGraphicsState() throws IOException
    {
        if (!fontStack.isEmpty())
        {
            fontStack.push(fontStack.peek());
        }
        if (!strokingColorSpaceStack.isEmpty())
        {
            strokingColorSpaceStack.push(strokingColorSpaceStack.peek());
        }
        if (!nonStrokingColorSpaceStack.isEmpty())
        {
            nonStrokingColorSpaceStack.push(nonStrokingColorSpaceStack.peek());
        }
        writeOperator("q");
    }

    /**
     * Q operator. Restores the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    @Override
    public void restoreGraphicsState() throws IOException
    {
        if (!fontStack.isEmpty())
        {
            fontStack.pop();
        }
        if (!strokingColorSpaceStack.isEmpty())
        {
            strokingColorSpaceStack.pop();
        }
        if (!nonStrokingColorSpaceStack.isEmpty())
        {
            nonStrokingColorSpaceStack.pop();
        }
        writeOperator("Q");
    }

    /**
     * Set the stroking color space.  This will add the colorspace to the PDResources
     * if necessary.
     *
     * @param colorSpace The colorspace to write.
     * @throws IOException If there is an error writing the colorspace.
     * @deprecated Use {@link #setStrokingColor} instead.
     */
    @Deprecated
    public void setStrokingColorSpace(PDColorSpace colorSpace) throws IOException
    {
        setStrokingColorSpaceStack(colorSpace);
        writeOperand(getName(colorSpace));
        writeOperator("CS");
    }

    /**
     * Set the stroking color space.  This will add the colorspace to the PDResources
     * if necessary.
     *
     * @param colorSpace The colorspace to write.
     * @throws IOException If there is an error writing the colorspace.
     * @deprecated Use {@link #setNonStrokingColor} instead.
     */
    @Deprecated
    public void setNonStrokingColorSpace(PDColorSpace colorSpace) throws IOException
    {
        setNonStrokingColorSpaceStack(colorSpace);
        writeOperand(getName(colorSpace));
        writeOperator("cs");
    }

    /**
     * Set the color components of current stroking color space.
     *
     * @param components The components to set for the current color.
     * @throws IOException If there is an error while writing to the stream.
     * @deprecated Use {@link #setStrokingColor(PDColor)} instead.
     */
    @Deprecated
    public void setStrokingColor(float[] components) throws IOException
    {
        if (strokingColorSpaceStack.isEmpty())
        {
            throw new IllegalStateException("The color space must be set before setting a color");
        }

        for (float component : components)
        {
            writeOperand(component);
        }

        PDColorSpace currentStrokingColorSpace = strokingColorSpaceStack.peek();

        if (currentStrokingColorSpace instanceof PDSeparation ||
            currentStrokingColorSpace instanceof PDPattern ||
            currentStrokingColorSpace instanceof PDICCBased)
        {
            writeOperator("SCN");
        }
        else
        {
            writeOperator("SC");
        }
    }

    /**
     * Set the stroking color in the DeviceCMYK color space. Range is 0..255.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameters are invalid.
     * @deprecated Use {@link #setStrokingColor(float, float, float, float)} instead.
     */
    @Deprecated
    public void setStrokingColor(int c, int m, int y, int k) throws IOException
    {
        if (isOutside255Interval(c) || isOutside255Interval(m) || isOutside255Interval(y) || isOutside255Interval(k))
        {
            throw new IllegalArgumentException("Parameters must be within 0..255, but are "
                    + String.format("(%d,%d,%d,%d)", c, m, y, k));
        }
        setStrokingColor(c / 255f, m / 255f, y / 255f, k / 255f);
    }

    /**
     * Set the stroking color in the DeviceGray color space. Range is 0..255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     * @throws IllegalArgumentException If the parameter is invalid.
     * @deprecated Use {@link #setStrokingColor(double)} instead.
     */
    @Deprecated
    public void setStrokingColor(int g) throws IOException
    {
        if (isOutside255Interval(g))
        {
            throw new IllegalArgumentException("Parameter must be within 0..255, but is " + g);
        }
        setStrokingColor(g / 255f);
    }

    /**
     * Set the color components of current non-stroking color space.
     *
     * @param components The components to set for the current color.
     * @throws IOException If there is an error while writing to the stream.
     * @deprecated Use {@link #setNonStrokingColor(PDColor)} instead.
     */
    @Deprecated
    public void setNonStrokingColor(float[] components) throws IOException
    {
        if (nonStrokingColorSpaceStack.isEmpty())
        {
            throw new IllegalStateException("The color space must be set before setting a color");
        }

        for (float component : components)
        {
            writeOperand(component);
        }

        PDColorSpace currentNonStrokingColorSpace = nonStrokingColorSpaceStack.peek();

        if (currentNonStrokingColorSpace instanceof PDSeparation ||
            currentNonStrokingColorSpace instanceof PDPattern ||
            currentNonStrokingColorSpace instanceof PDICCBased)
        {
            writeOperator("scn");
        }
        else
        {
            writeOperator("sc");
        }
    }

    /**
     * Draw a rectangle on the page using the current non stroking color.
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
    public void fillRect(float x, float y, float width, float height) throws IOException
    {
        if (isInTextMode())
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
    public void addBezier312(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
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
    public void addBezier32(float x2, float y2, float x3, float y3) throws IOException
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
    public void addBezier31(float x1, float y1, float x3, float y3) throws IOException
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
    public void addLine(float xStart, float yStart, float xEnd, float yEnd) throws IOException
    {
        if (isInTextMode())
        {
            throw new IllegalStateException("Error: addLine is not allowed within a text block.");
        }
        moveTo(xStart, yStart);
        lineTo(xEnd, yEnd);
    }

    /**
     * Draw a line on the page using the current non stroking color and the current line width.
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
    public void drawLine(float xStart, float yStart, float xEnd, float yEnd) throws IOException
    {
        if (isInTextMode())
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
    public void addPolygon(float[] x, float[] y) throws IOException
    {
        if (isInTextMode())
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
     * Draw a polygon on the page using the current non stroking color.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #moveTo} and {@link #lineTo} methods instead.
     */
    @Deprecated
    public void drawPolygon(float[] x, float[] y) throws IOException
    {
        if (isInTextMode())
        {
            throw new IllegalStateException("Error: drawPolygon is not allowed within a text block.");
        }
        addPolygon(x, y);
        stroke();
    }

    /**
     * Draw and fill a polygon on the page using the current non stroking color.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     * @throws IllegalStateException If the method was called within a text block.
     * @deprecated Use {@link #moveTo} and {@link #lineTo} methods instead.
     */
    @Deprecated
    public void fillPolygon(float[] x, float[] y) throws IOException
    {
        if (isInTextMode())
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
    public void fill(int windingRule) throws IOException
    {
        if (windingRule == PathIterator.WIND_NON_ZERO)
        {
            fill();
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            fillEvenOdd();
        }
        else
        {
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
    public void clipPath(int windingRule) throws IOException
    {
        if (isInTextMode())
        {
            throw new IllegalStateException("Error: clipPath is not allowed within a text block.");
        }
        if (windingRule == PathIterator.WIND_NON_ZERO)
        {
            writeOperator("W");
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            writeOperator("W*");
        }
        else
        {
            throw new IllegalArgumentException("Error: unknown value for winding rule");
        }
        writeOperator("n");
    }

    /**
     * Begin a marked content sequence.
     *
     * @param tag the tag
     * @throws IOException if an I/O error occurs
     * @deprecated Use {@link #beginMarkedContent} instead.
     */
    @Deprecated
    public void beginMarkedContentSequence(COSName tag) throws IOException
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
    public void beginMarkedContentSequence(COSName tag, COSName propsName) throws IOException
    {
        writeOperand(tag);
        writeOperand(propsName);
        writeOperator("BDC");
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
     * @deprecated This method will be removed in a future release.
     */
    @Deprecated
    public void appendRawCommands(String commands) throws IOException
    {
        write(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated This method will be removed in a future release.
     */
    @Deprecated
    public void appendRawCommands(byte[] commands) throws IOException
    {
        write(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a raw byte to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated This method will be removed in a future release.
     */
    @Deprecated
    public void appendRawCommands(int data) throws IOException
    {
        writeOperand(data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted double value to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated This method will be removed in a future release.
     */
    @Deprecated
    public void appendRawCommands(double data) throws IOException
    {
        writeOperand(data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted float value to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated This method will be removed in a future release.
     */
    @Deprecated
    public void appendRawCommands(float data) throws IOException
    {
        writeOperand(data);
    }

    /**
     * This will append a {@link COSName} to the content stream.
     *
     * @param name the name
     * @throws IOException If an error occurs while writing to the stream.
     * @deprecated This method will be removed in a future release.
     */
    @Deprecated
    public void appendCOSName(COSName name) throws IOException
    {
        writeOperand(name);
    }
    
    /**
     * Set an extended graphics state.
     * 
     * @param state The extended graphics state.
     * @throws IOException If the content stream could not be written.
     */
    @Override
    public void setGraphicsStateParameters(PDExtendedGraphicsState state) throws IOException
    {
        writeOperand(getResources().add(state));
        writeOperator("gs");
    }

    /**
     * Set the text rendering mode. This determines whether showing text shall cause glyph outlines
     * to be stroked, filled, used as a clipping boundary, or some combination of the three.
     *
     * @param rm The text rendering mode.
     * @throws IOException If the content stream could not be written.
     */
    public void setRenderingMode(RenderingMode rm) throws IOException
    {
        writeOperand(rm.intValue());
        writeOperator("Tr");
    }
}
