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
package org.apache.pdfbox.pdmodel.edit;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Matrix;

/**
 * Provides the ability to write to a page content stream.
 *
 * @author Ben Litchfield
 */
public final class PDPageContentStream implements Closeable
{
    private static final Log LOG = LogFactory.getLog(PDPageContentStream.class);
    private static final String SPACE = " ";

    private OutputStream output;
    private PDResources resources;

    private boolean inTextMode = false;
    private final Stack<PDFont> fontStack = new Stack<PDFont>();
    private final List<PDFont> fontsToSubset = new ArrayList<PDFont>();
    private final Map<PDFont, Set<Integer>> subsetCodePoints = new HashMap<PDFont, Set<Integer>>();

    private PDColorSpace currentStrokingColorSpace = PDDeviceGray.INSTANCE;
    private PDColorSpace currentNonStrokingColorSpace = PDDeviceGray.INSTANCE;

    // cached storage component for getting color values
    private final float[] colorComponents = new float[4];

    // number format
    private final NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage) throws IOException
    {
        this(document, sourcePage, false, true);
    }

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten. If false all previous content is deleted.
     * @param compress Tell if the content stream should compress the page contents.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage, boolean appendContent, boolean compress)
            throws IOException
    {
        this(document, sourcePage, appendContent, compress, false);
    }

    /**
     * Create a new PDPage content stream.
     *
     * @param document The document the page is part of.
     * @param sourcePage The page to write the contents to.
     * @param appendContent Indicates whether content will be overwritten. If false all previous content is deleted.
     * @param compress Tell if the content stream should compress the page contents.
     * @param resetContext Tell if the graphic context should be reseted.
     * @throws IOException If there is an error writing to the page contents.
     */
    public PDPageContentStream(PDDocument document, PDPage sourcePage, boolean appendContent, boolean compress,
            boolean resetContext) throws IOException
    {
        // Get the pdstream from the source page instead of creating a new one
        PDStream contents = sourcePage.getStream();
        boolean hasContent = contents != null;

        // If request specifies the need to append to the document
        if (appendContent && hasContent)
        {

            // Create a pdstream to append new content
            PDStream contentsToAppend = new PDStream(document);

            // This will be the resulting COSStreamArray after existing and new streams are merged
            COSStreamArray compoundStream = null;

            // If contents is already an array, a new stream is simply appended to it
            if (contents.getStream() instanceof COSStreamArray)
            {
                compoundStream = (COSStreamArray) contents.getStream();
                compoundStream.appendStream(contentsToAppend.getStream());
            }
            else
            {
                // Creates the COSStreamArray and adds the current stream plus a new one to it
                COSArray newArray = new COSArray();
                newArray.add(contents.getCOSObject());
                newArray.add(contentsToAppend.getCOSObject());
                compoundStream = new COSStreamArray(newArray);
            }

            if (compress)
            {
                List<COSName> filters = new ArrayList<COSName>();
                filters.add(COSName.FLATE_DECODE);
                contentsToAppend.setFilters(filters);
            }

            if (resetContext)
            {
                // create a new stream to encapsulate the existing stream
                PDStream saveGraphics = new PDStream(document);
                output = saveGraphics.createOutputStream();
                // save the initial/unmodified graphics context
                saveGraphicsState();
                close();
                if (compress)
                {
                    List<COSName> filters = new ArrayList<COSName>();
                    filters.add(COSName.FLATE_DECODE);
                    saveGraphics.setFilters(filters);
                }
                // insert the new stream at the beginning
                compoundStream.insertCOSStream(saveGraphics);
            }

            // Sets the compoundStream as page contents
            sourcePage.setContents(new PDStream(compoundStream));
            output = contentsToAppend.createOutputStream();
            if (resetContext)
            {
                // restore the initial/unmodified graphics context
                restoreGraphicsState();
            }
        }
        else
        {
            if (hasContent)
            {
                LOG.warn("You are overwriting an existing content, you should use the append mode");
            }
            contents = new PDStream(document);
            if (compress)
            {
                List<COSName> filters = new ArrayList<COSName>();
                filters.add(COSName.FLATE_DECODE);
                contents.setFilters(filters);
            }
            sourcePage.setContents(contents);
            output = contents.createOutputStream();
        }
        formatDecimal.setMaximumFractionDigits(10);
        formatDecimal.setGroupingUsed(false);
        // this has to be done here, as the resources will be set to null when reseting the content stream
        resources = sourcePage.getResources();
        if (resources == null)
        {
            resources = new PDResources();
            sourcePage.setResources(resources);
        }
    }

    /**
     * Begin some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest beginText calls.
     */
    public void beginText() throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: Nested beginText() calls are not allowed.");
        }
        writeLine("BT");
        inTextMode = true;
    }

    /**
     * End some text operations.
     *
     * @throws IOException If there is an error writing to the stream or if you attempt to
     *         nest endText calls.
     */
    public void endText() throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: You must call beginText() before calling endText.");
        }
        writeLine("ET");
        inTextMode = false;
    }

    /**
     * Set the font to draw text with.
     *
     * @param font The font to use.
     * @param fontSize The font size to draw the text.
     * @throws IOException If there is an error writing the font information.
     */
    public void setFont(PDFont font, float fontSize) throws IOException
    {
        setFont(font, fontSize, true);
    }

    /**
     * Set the font and font size to draw text with.
     *
     * @param font The font to use.
     * @param fontSize The font size to draw the text.
     * @param embedSubset True to subset this font when embedding it. Affects all uses of this font.
     * @throws IOException If there is an error writing the font information.
     */
    public void setFont(PDFont font, float fontSize, boolean embedSubset) throws IOException
    {
        if (fontStack.isEmpty())
        {
            fontStack.add(font);
        }
        else
        {
            fontStack.setElementAt(font, fontStack.size() - 1);
        }

        if (embedSubset)
        {
            fontsToSubset.add(font);
            subsetCodePoints.put(font, new HashSet<Integer>());
        }
        else
        {
            fontsToSubset.remove(font);
        }

        write(resources.add(font));
        write(SPACE);
        write(fontSize);
        write(SPACE);
        writeLine("Tf");
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
    public void showText(String text) throws IOException
    {
        if (!inTextMode)
        {
            throw new IllegalStateException("Must call beginText() before showText()");
        }

        if (fontStack.isEmpty())
        {
            throw new IllegalStateException("Must call setFont() before showText()");
        }

        PDFont font = fontStack.peek();

        // Unicode code points to keep when subsetting
        Set<Integer> codePoints = subsetCodePoints.get(font);
        if (codePoints != null)
        {
            for (int offset = 0; offset < text.length(); )
            {
                int codePoint = text.codePointAt(offset);
                codePoints.add(codePoint);
                offset += Character.charCount(codePoint);
            }

        }

        COSWriter.writeString(font.encode(text), output);
        write(SPACE);
        writeLine("Tj");
    }

    /**
     * Sets the text leading.
     *
     * @param leading The leading in unscaled text units.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setLeading(double leading) throws IOException
    {
        write((float)leading);
        write(SPACE);
        writeLine("TL");
    }

    /**
     * Move to the start of the next line of text. Requires the leading to have been set.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void newLine() throws IOException
    {
        if (!inTextMode)
        {
            throw new IllegalStateException("Must call beginText() before newLine()");
        }
        writeLine("T*");
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
     * The Td operator.
     * Move to the start of the next line, offset from the start of the current line by (tx, ty).
     *
     * @param tx The x translation.
     * @param ty The y translation.
     * @throws IOException If there is an error writing to the stream.
     */
    public void newLineAtOffset(float tx, float ty) throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: must call beginText() before newLineAtOffset()");
        }
        write(tx);
        write(SPACE);
        write(ty);
        write(SPACE);
        writeLine("Td");
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
     * The Tm operator. Sets the text matrix to the given values.
     * A current text matrix will be replaced with the new one.
     *
     * @param matrix the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextMatrix(Matrix matrix) throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: must call beginText() before setTextMatrix");
        }
        writeAffineTransform(matrix.createAffineTransform());
        writeLine("Tm");
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
     * Draw an image at the x,y coordinates, with the default size of the image.
     *
     * @param image The image to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawImage(PDImageXObject image, float x, float y) throws IOException
    {
        drawImage(image, x, y, image.getWidth(), image.getHeight());
    }

    /**
     * Draw an image at the x,y coordinates, with the given size.
     *
     * @param image The image to draw.
     * @param x The x-coordinate to draw the image.
     * @param y The y-coordinate to draw the image.
     * @param width The width to draw the image.
     * @param height The height to draw the image.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawImage(PDImageXObject image, float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawImage is not allowed within a text block.");
        }

        saveGraphicsState();
        write(SPACE);

        AffineTransform transform = new AffineTransform(width, 0, 0, height, x, y);
        transform(new Matrix(transform));
        write(SPACE);

        write(resources.add(image));
        write(SPACE);
        writeLine("Do");

        restoreGraphicsState();
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
     * Draw an inline image at the x,y coordinates, with the default size of the image.
     *
     * @param inlineImage The inline image to draw.
     * @param x The x-coordinate to draw the inline image.
     * @param y The y-coordinate to draw the inline image.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawImage(PDInlineImage inlineImage, float x, float y) throws IOException
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
     * Draw an inline image at the x,y coordinates and a certain width and height.
     *
     * @param inlineImage The inline image to draw.
     * @param x The x-coordinate to draw the inline image.
     * @param y The y-coordinate to draw the inline image.
     * @param width The width of the inline image to draw.
     * @param height The height of the inline image to draw.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawImage(PDInlineImage inlineImage, float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawImage is not allowed within a text block.");
        }

        saveGraphicsState();
        transform(new Matrix(width, 0, 0, height, x, y));

        write("BI\n");
        write("/W");
        write(SPACE);
        write(Integer.toString(inlineImage.getWidth()));
        write(SPACE);
        write("/H");
        write(SPACE);
        write(Integer.toString(inlineImage.getHeight()));
        write(SPACE);
        write("/CS");
        write(SPACE);
        write("/");
        write(inlineImage.getColorSpace().getName());
        writeLine();
        if (inlineImage.getDecode() != null && inlineImage.getDecode().size() > 0)
        {
            write("/D");
            write(SPACE);
            write("[");
            write(SPACE);
            for (COSBase cosBase : inlineImage.getDecode())
            {
                COSInteger cosInt = (COSInteger) cosBase;
                write(Integer.toString(cosInt.intValue()));
                write(SPACE);
            }
            write("]");
            write("\n");
        }
        if (inlineImage.isStencil())
        {
            write("/IM true\n");
        }
        write("/BPC");
        write(SPACE);
        write(Integer.toString(inlineImage.getBitsPerComponent()));
        writeLine();
        write("ID\n");
        writeBytes(inlineImage.getStream().getByteArray());
        writeLine();
        write("EI\n");

        restoreGraphicsState();
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
     * @deprecated Use {@link #drawImage} or {@link #drawForm} instead.
     */
    @Deprecated
    public void drawXObject(PDXObject xobject, AffineTransform transform) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawXObject is not allowed within a text block.");
        }

        String xObjectPrefix = null;
        if (xobject instanceof PDImageXObject)
        {
            xObjectPrefix = "Im";
        }
        else
        {
            xObjectPrefix = "Form";
        }
        COSName objMapping = resources.add(xobject, xObjectPrefix);
        saveGraphicsState();
        write(SPACE);
        transform(new Matrix(transform));
        write(SPACE);
        write(objMapping);
        write(SPACE);
        writeLine("Do");
        restoreGraphicsState();
    }

    /**
     * Draws the given Form XObject at the current location.
     *
     * @param form Form XObject
     * @throws IOException if the content stream could not be written
     */
    public void drawForm(PDFormXObject form) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawForm is not allowed within a text block.");
        }

        write(resources.add(form));
        write(SPACE);
        writeLine("Do");
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
        transform(new Matrix((float)a, (float)b, (float)c, (float)d, (float)e, (float)f));
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
     * The cm operator. Concatenates the given matrix with the CTM.
     *
     * @param matrix the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     */
    public void transform(Matrix matrix) throws IOException
    {
        writeAffineTransform(matrix.createAffineTransform());
        writeLine("cm");
    }

    /**
     * q operator. Saves the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void saveGraphicsState() throws IOException
    {
        if (!fontStack.isEmpty())
        {
            fontStack.push(fontStack.peek());
        }
        writeLine("q");
    }

    /**
     * Q operator. Restores the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void restoreGraphicsState() throws IOException
    {
        if (!fontStack.isEmpty())
        {
            fontStack.pop();
        }
        writeLine("Q");
    }

    /**
     * Set the stroking color space.  This will add the colorspace to the PDResources
     * if necessary.
     *
     * @param colorSpace The colorspace to write.
     * @throws IOException If there is an error writing the colorspace.
     */
    public void setStrokingColorSpace(PDColorSpace colorSpace) throws IOException
    {
        currentStrokingColorSpace = colorSpace;
        writeColorSpace(colorSpace);
        writeLine("CS");
    }

    /**
     * Set the stroking color space.  This will add the colorspace to the PDResources
     * if necessary.
     *
     * @param colorSpace The colorspace to write.
     * @throws IOException If there is an error writing the colorspace.
     */
    public void setNonStrokingColorSpace(PDColorSpace colorSpace) throws IOException
    {
        currentNonStrokingColorSpace = colorSpace;
        writeColorSpace(colorSpace);
        writeLine("cs");
    }

    private void writeColorSpace(PDColorSpace colorSpace) throws IOException
    {
        COSName key = null;
        if (colorSpace instanceof PDDeviceGray || colorSpace instanceof PDDeviceRGB
                || colorSpace instanceof PDDeviceCMYK)
        {
            key = COSName.getPDFName(colorSpace.getName());
        }
        else
        {
            COSDictionary colorSpaces = (COSDictionary) resources.getCOSObject().getDictionaryObject(
                    COSName.COLORSPACE);
            if (colorSpaces == null)
            {
                colorSpaces = new COSDictionary();
                resources.getCOSObject().setItem(COSName.COLORSPACE, colorSpaces);
            }
            key = colorSpaces.getKeyForValue(colorSpace.getCOSObject());

            if (key == null)
            {
                int counter = 0;
                String csName = "CS";
                while (colorSpaces.containsValue(csName + counter))
                {
                    counter++;
                }
                key = COSName.getPDFName(csName + counter);
                colorSpaces.setItem(key, colorSpace);
            }
        }
        key.writePDF(output);
        write(SPACE);
    }

    /**
     * Set the color components of current stroking colorspace.
     *
     * @param components The components to set for the current color.
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setStrokingColor(float[] components) throws IOException
    {
        for (int i = 0; i < components.length; i++)
        {
            write(components[i]);
            write(SPACE);
        }
        if (currentStrokingColorSpace instanceof PDSeparation ||
            currentStrokingColorSpace instanceof PDPattern ||
            currentStrokingColorSpace instanceof PDICCBased)
        {
            writeLine("SCN");
        }
        else
        {
            writeLine("SC");
        }
    }

    /**
     * Set the stroking color, specified as RGB or Gray.
     *
     * @param color The color to set.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(Color color) throws IOException
    {
        ColorSpace colorSpace = color.getColorSpace();
        if (colorSpace.getType() == ColorSpace.TYPE_RGB)
        {
            setStrokingColor(color.getRed(), color.getGreen(), color.getBlue());
        }
        else if (colorSpace.getType() == ColorSpace.TYPE_GRAY)
        {
            color.getColorComponents(colorComponents);
            setStrokingColor(colorComponents[0]);
        }
        else
        {
            throw new IOException("Error: unknown color space:" + colorSpace);
        }
    }

    /**
     * Set the non stroking color, specified as RGB or Gray.
     *
     * @param color The color to set.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(Color color) throws IOException
    {
        ColorSpace colorSpace = color.getColorSpace();
        if (colorSpace.getType() == ColorSpace.TYPE_RGB)
        {
            setNonStrokingColor(color.getRed(), color.getGreen(), color.getBlue());
        }
        else if (colorSpace.getType() == ColorSpace.TYPE_GRAY)
        {
            color.getColorComponents(colorComponents);
            setNonStrokingColor(colorComponents[0]);
        }
        else
        {
            throw new IOException("Error: unknown color space:" + colorSpace);
        }
    }

    /**
     * Set the stroking color, specified as RGB, 0-255.
     *
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(int r, int g, int b) throws IOException
    {
        write(r / 255f);
        write(SPACE);
        write(g / 255f);
        write(SPACE);
        write(b / 255f);
        write(SPACE);
        writeLine("RG");
    }

    /**
     * Set the stroking color, specified as CMYK, 0-255.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(int c, int m, int y, int k) throws IOException
    {
        setStrokingColor(c / 255f, m / 255f, y / 255f, k / 255f);
    }

    /**
     * Set the stroking color, specified as CMYK, 0.0-1.0.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(double c, double m, double y, double k) throws IOException
    {
        write((float) c);
        write(SPACE);
        write((float) m);
        write(SPACE);
        write((float) y);
        write(SPACE);
        write((float) k);
        write(SPACE);
        writeLine("K");
    }

    /**
     * Set the stroking color, specified as grayscale, 0-255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(int g) throws IOException
    {
        setStrokingColor(g / 255f);
    }

    /**
     * Set the stroking color, specified as Grayscale 0.0-1.0.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(double g) throws IOException
    {
        write((float) g);
        write(SPACE);
        writeLine("G");
    }

    /**
     * Set the color components of current non stroking colorspace.
     *
     * @param components The components to set for the current color.
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setNonStrokingColor(float[] components) throws IOException
    {
        for (int i = 0; i < components.length; i++)
        {
            write(components[i]);
            write(SPACE);
        }

        if (currentNonStrokingColorSpace instanceof PDSeparation ||
            currentNonStrokingColorSpace instanceof PDPattern ||
            currentNonStrokingColorSpace instanceof PDICCBased)
        {
            writeLine("scn");
        }
        else
        {
            writeLine("sc");
        }
    }

    /**
     * Set the non stroking color, specified as RGB, 0-255.
     *
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(int r, int g, int b) throws IOException
    {
        write(r / 255f);
        write(SPACE);
        write(g / 255f);
        write(SPACE);
        write(b / 255f);
        write(SPACE);
        writeLine("rg");
    }

    /**
     * Set the non stroking color, specified as CMYK, 0-255.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(int c, int m, int y, int k) throws IOException
    {
        setNonStrokingColor(c / 255f, m / 255f, y / 255f, k / 255f);
    }

    /**
     * Set the non stroking color, specified as CMYK, 0.0-1.0.
     *
     * @param c The cyan value.
     * @param m The magenta value.
     * @param y The yellow value.
     * @param k The black value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(double c, double m, double y, double k) throws IOException
    {
        write((float)c);
        write(SPACE);
        write((float)m);
        write(SPACE);
        write((float)y);
        write(SPACE);
        write((float)k);
        write(SPACE);
        writeLine("k");
    }

    /**
     * Set the non stroking color, specified as grayscale, 0-255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(int g) throws IOException
    {
        setNonStrokingColor(g / 255f);
    }

    /**
     * Set the non stroking color, specified as Grayscale 0.0-1.0.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(double g) throws IOException
    {
        write((float)g);
        write(SPACE);
        writeLine("g");
    }

    /**
     * Add a rectangle to the current path.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void addRect(float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addRect is not allowed within a text block.");
        }
        write(x);
        write(SPACE);
        write(y);
        write(SPACE);
        write(width);
        write(SPACE);
        write(height);
        write(SPACE);
        writeLine("re");
    }

    /**
     * Draw a rectangle on the page using the current non stroking color.
     *
     * @param x The lower left x coordinate.
     * @param y The lower left y coordinate.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void fillRect(float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: fillRect is not allowed within a text block.");
        }
        addRect(x, y, width, height);
        fill(PathIterator.WIND_NON_ZERO);
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
     */
    public void addBezier312(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addBezier312 is not allowed within a text block.");
        }
        write(x1);
        write(SPACE);
        write(y1);
        write(SPACE);
        write(x2);
        write(SPACE);
        write(y2);
        write(SPACE);
        write(x3);
        write(SPACE);
        write(y3);
        write(SPACE);
        writeLine("c");
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using the current point and (x2 , y2 ) as the Bézier control points
     * @param x2 x coordinate of the point 2
     * @param y2 y coordinate of the point 2
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     */
    public void addBezier32(float x2, float y2, float x3, float y3) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addBezier32 is not allowed within a text block.");
        }
        write(x2);
        write(SPACE);
        write(y2);
        write(SPACE);
        write(x3);
        write(SPACE);
        write(y3);
        write(SPACE);
        writeLine("v");
    }

    /**
     * Append a cubic Bézier curve to the current path. The curve extends from the current
     * point to the point (x3 , y3 ), using (x1 , y1 ) and (x3 , y3 ) as the Bézier control points
     * @param x1 x coordinate of the point 1
     * @param y1 y coordinate of the point 1
     * @param x3 x coordinate of the point 3
     * @param y3 y coordinate of the point 3
     * @throws IOException If there is an error while adding the .
     */
    public void addBezier31(float x1, float y1, float x3, float y3) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addBezier31 is not allowed within a text block.");
        }
        write(x1);
        write(SPACE);
        write(y1);
        write(SPACE);
        write(x3);
        write(SPACE);
        write(y3);
        write(SPACE);
        writeLine("y");
    }

    /**
     * Add a line to the given coordinate.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If there is an error while adding the line.
     */
    public void moveTo(float x, float y) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: moveTo is not allowed within a text block.");
        }
        write(x);
        write(SPACE);
        write(y);
        write(SPACE);
        writeLine("m");
    }

    /**
     * Add a move to the given coordinate.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If there is an error while adding the line.
     */
    public void lineTo(float x, float y) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: lineTo is not allowed within a text block.");
        }
        write(x);
        write(SPACE);
        write(y);
        write(SPACE);
        writeLine("l");
    }

    /**
     * add a line to the current path.
     *
     * @param xStart The start x coordinate.
     * @param yStart The start y coordinate.
     * @param xEnd The end x coordinate.
     * @param yEnd The end y coordinate.
     * @throws IOException If there is an error while adding the line.
     */
    public void addLine(float xStart, float yStart, float xEnd, float yEnd) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addLine is not allowed within a text block.");
        }
        // moveTo
        moveTo(xStart, yStart);
        // lineTo
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
     */
    public void drawLine(float xStart, float yStart, float xEnd, float yEnd) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawLine is not allowed within a text block.");
        }
        addLine(xStart, yStart, xEnd, yEnd);
        // stroke
        stroke();
    }

    /**
     * Add a polygon to the current path.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void addPolygon(float[] x, float[] y) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: addPolygon is not allowed within a text block.");
        }
        if (x.length != y.length)
        {
            throw new IOException("Error: some points are missing coordinate");
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
     */
    public void drawPolygon(float[] x, float[] y) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawPolygon is not allowed within a text block.");
        }
        addPolygon(x, y);
        stroke();
    }

    /**
     * Draw and fill a polygon on the page using the current non stroking color.
     * @param x x coordinate of each points
     * @param y y coordinate of each points
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void fillPolygon(float[] x, float[] y) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: fillPolygon is not allowed within a text block.");
        }
        addPolygon(x, y);
        fill(PathIterator.WIND_NON_ZERO);
    }

    /**
     * Stroke the path.
     * 
     * @throws IOException If there is an error while stroking the path.
     */
    public void stroke() throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: stroke is not allowed within a text block.");
        }
        writeLine("S");
    }

    /**
     * Close and stroke the path.
     * 
     * @throws IOException If there is an error while closing and stroking the path.
     */
    public void closeAndStroke() throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: closeAndStroke is not allowed within a text block.");
        }
        writeLine("s");
    }

    /**
     * Fill the path.
     * 
     * @param windingRule the winding rule to be used for filling 
     * 
     * @throws IOException If there is an error while filling the path.
     */
    public void fill(int windingRule) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: fill is not allowed within a text block.");
        }
        if (windingRule == PathIterator.WIND_NON_ZERO)
        {
            writeLine("f");
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            writeLine("f*");
        }
        else
        {
            throw new IOException("Error: unknown value for winding rule");
        }

    }

    /**
     * Fills the clipping area with the given shading.
     *
     * @param shading Shading resource
     * @throws IOException If the content stream could not be written
     */
    public void shadingFill(PDShading shading) throws IOException
    {
        write(resources.add(shading));
        write(SPACE);
        writeLine("sh");
    }

    /**
     * Close subpath.
     * 
     * @throws IOException If there is an error while closing the subpath.
     */
    public void closeSubPath() throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: closeSubPath is not allowed within a text block.");
        }
        writeLine("h");
    }

    /**
     * Clip path.
     * 
     * @param windingRule the winding rule to be used for clipping
     *  
     * @throws IOException If there is an error while clipping the path.
     */
    public void clipPath(int windingRule) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: clipPath is not allowed within a text block.");
        }
        if (windingRule == PathIterator.WIND_NON_ZERO)
        {
            writeLine("W");
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            writeLine("W*");
        }
        else
        {
            throw new IOException("Error: unknown value for winding rule");
        }
        writeLine("n");
    }

    /**
     * Set linewidth to the given value.
     *
     * @param lineWidth The width which is used for drwaing.
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void setLineWidth(float lineWidth) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: setLineWidth is not allowed within a text block.");
        }
        write(lineWidth);
        write(SPACE);
        writeLine("w");
    }

    /**
     * Set the line join style.
     * @param lineJoinStyle 0 for miter join, 1 for round join, and 2 for bevel join.
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setLineJoinStyle(int lineJoinStyle) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: setLineJoinStyle is not allowed within a text block.");
        }
        if (lineJoinStyle >= 0 && lineJoinStyle <= 2)
        {
            write(lineJoinStyle);
            write(SPACE);
            writeLine("j");
        }
        else
        {
            throw new IOException("Error: unknown value for line join style");
        }
    }

    /**
     * Set the line cap style.
     * @param lineCapStyle 0 for butt cap, 1 for round cap, and 2 for projecting square cap.
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setLineCapStyle(int lineCapStyle) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: setLineCapStyle is not allowed within a text block.");
        }
        if (lineCapStyle >= 0 && lineCapStyle <= 2)
        {
            write(lineCapStyle);
            write(SPACE);
            writeLine("J");
        }
        else
        {
            throw new IOException("Error: unknown value for line cap style");
        }
    }

    /**
     * Set the line dash pattern.
     * @param pattern The pattern array
     * @param phase The phase of the pattern
     * @throws IOException If there is an error while writing to the stream.
     */
    public void setLineDashPattern(float[] pattern, float phase) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: setLineDashPattern is not allowed within a text block.");
        }
        write("[");
        for (float value : pattern)
        {
            write(value);
            write(SPACE);
        }
        write("]");
        write(SPACE);
        write(phase);
        write(SPACE);
        writeLine("d");
    }

    /**
     * Begin a marked content sequence.
     * @param tag the tag
     * @throws IOException if an I/O error occurs
     */
    public void beginMarkedContentSequence(COSName tag) throws IOException
    {
        write(tag);
        write(SPACE);
        writeLine("BMC");
    }

    /**
     * Begin a marked content sequence with a reference to an entry in the page resources'
     * Properties dictionary.
     * @param tag the tag
     * @param propsName the properties reference
     * @throws IOException if an I/O error occurs
     */
    public void beginMarkedContentSequence(COSName tag, COSName propsName) throws IOException
    {
        write(tag);
        write(SPACE);
        write(propsName);
        write(SPACE);
        writeLine("BDC");
    }

    /**
     * End a marked content sequence.
     * @throws IOException if an I/O error occurs
     */
    public void endMarkedContentSequence() throws IOException
    {
        writeLine("EMC");
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
        output.write(commands.getBytes(Charsets.US_ASCII));
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
        output.write(commands);
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
        output.write(data);
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
        output.write(formatDecimal.format(data).getBytes(Charsets.US_ASCII));
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
        output.write(formatDecimal.format(data).getBytes(Charsets.US_ASCII));
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
        name.writePDF(output);
    }

    /**
     * Writes a real real to the content stream.
     */
    private void write(float real) throws IOException
    {
        write(formatDecimal.format(real));
    }

    /**
     * Writes a real number to the content stream.
     */
    private void write(int integer) throws IOException
    {
        write(formatDecimal.format(integer));
    }

    /**
     * Writes a string to the content stream as ASCII.
     */
    private void write(String text) throws IOException
    {
        output.write(text.getBytes(Charsets.US_ASCII));
    }

    /**
     * Writes a string to the content stream as ASCII.
     */
    private void writeLine(String text) throws IOException
    {
        output.write(text.getBytes(Charsets.US_ASCII));
        output.write('\n');
    }

    /**
     * Writes an empty line to the content stream as ASCII.
     */
    private void writeLine() throws IOException
    {
        output.write('\n');
    }

    /**
     * Writes binary data to the content stream.
     */
    private void writeBytes(byte[] data) throws IOException
    {
        output.write(data);
    }

    /**
     * Writes a COSName to the content stream.
     */
    private void write(COSName name) throws IOException
    {
        name.writePDF(output);
    }

    /**
     * Writes an AffineTransform to the content stream as an array.
     */
    private void writeAffineTransform(AffineTransform transform) throws IOException
    {
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            write((float)v);
            write(SPACE);
        }
    }

    /**
     * Close the content stream.  This must be called when you are done with this object.
     *
     * @throws IOException If the underlying stream has a problem being written to.
     */
    public void close() throws IOException
    {
        for (PDFont font : fontsToSubset)
        {
            // currently we only support subsetting Type0/CIDFontType2 fonts
            if (font instanceof PDType0Font)
            {
                if (((PDType0Font)font).getDescendantFont() instanceof PDCIDFontType2)
                {
                    font.subset(subsetCodePoints.get(font));
                }
            }
        }
        output.close();
        currentNonStrokingColorSpace = null;
        currentStrokingColorSpace = null;
        resources = null;
    }
}
