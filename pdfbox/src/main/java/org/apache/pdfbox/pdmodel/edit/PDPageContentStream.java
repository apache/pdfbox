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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 * This class is a convenience for creating page content streams.  You MUST
 * call close() when you are finished with this object.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDPageContentStream
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDPageContentStream.class);

    private OutputStream output;
    private boolean inTextMode = false;
    private PDResources resources;

    private PDColorSpace currentStrokingColorSpace = new PDDeviceGray();
    private PDColorSpace currentNonStrokingColorSpace = new PDDeviceGray();

    // cached storage component for getting color values
    private float[] colorComponents = new float[4];

    private NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);

    private static final String ISO8859 = "ISO-8859-1";

    private static byte[] getISOBytes(final String s)
    {
        try
        {
            return s.getBytes(ISO8859);
        }
        catch (final UnsupportedEncodingException ex)
        {
            throw new IllegalStateException(ex);
        }
    }

    private static final byte[] BEGIN_TEXT = getISOBytes("BT\n");
    private static final byte[] END_TEXT = getISOBytes("ET\n");
    private static final byte[] SET_FONT = getISOBytes("Tf\n");
    private static final byte[] MOVE_TEXT_POSITION = getISOBytes("Td\n");
    private static final byte[] SET_TEXT_MATRIX = getISOBytes("Tm\n");
    private static final byte[] SHOW_TEXT = getISOBytes("Tj\n");

    private static final byte[] SAVE_GRAPHICS_STATE = getISOBytes("q\n");
    private static final byte[] RESTORE_GRAPHICS_STATE = getISOBytes("Q\n");
    private static final byte[] CONCATENATE_MATRIX = getISOBytes("cm\n");
    private static final byte[] XOBJECT_DO = getISOBytes("Do\n");
    private static final byte[] RG_STROKING = getISOBytes("RG\n");
    private static final byte[] RG_NON_STROKING = getISOBytes("rg\n");
    private static final byte[] K_STROKING = getISOBytes("K\n");
    private static final byte[] K_NON_STROKING = getISOBytes("k\n");
    private static final byte[] G_STROKING = getISOBytes("G\n");
    private static final byte[] G_NON_STROKING = getISOBytes("g\n");
    private static final byte[] RECTANGLE = getISOBytes("re\n");
    private static final byte[] FILL_NON_ZERO = getISOBytes("f\n");
    private static final byte[] FILL_EVEN_ODD = getISOBytes("f*\n");
    private static final byte[] LINE_TO = getISOBytes("l\n");
    private static final byte[] MOVE_TO = getISOBytes("m\n");
    private static final byte[] CLOSE_STROKE = getISOBytes("s\n");
    private static final byte[] STROKE = getISOBytes("S\n");
    private static final byte[] LINE_WIDTH = getISOBytes("w\n");
    private static final byte[] LINE_JOIN_STYLE = getISOBytes("j\n");
    private static final byte[] LINE_CAP_STYLE = getISOBytes("J\n");
    private static final byte[] LINE_DASH_PATTERN = getISOBytes("d\n");
    private static final byte[] CLOSE_SUBPATH = getISOBytes("h\n");
    private static final byte[] CLIP_PATH_NON_ZERO = getISOBytes("W\n");
    private static final byte[] CLIP_PATH_EVEN_ODD = getISOBytes("W*\n");
    private static final byte[] NOP = getISOBytes("n\n");
    private static final byte[] BEZIER_312 = getISOBytes("c\n");
    private static final byte[] BEZIER_32 = getISOBytes("v\n");
    private static final byte[] BEZIER_313 = getISOBytes("y\n");

    private static final byte[] BMC = getISOBytes("BMC\n");
    private static final byte[] BDC = getISOBytes("BDC\n");
    private static final byte[] EMC = getISOBytes("EMC\n");

    private static final byte[] SET_STROKING_COLORSPACE = getISOBytes("CS\n");
    private static final byte[] SET_NON_STROKING_COLORSPACE = getISOBytes("cs\n");

    private static final byte[] SET_STROKING_COLOR_SIMPLE = getISOBytes("SC\n");
    private static final byte[] SET_STROKING_COLOR_COMPLEX = getISOBytes("SCN\n");
    private static final byte[] SET_NON_STROKING_COLOR_SIMPLE = getISOBytes("sc\n");
    private static final byte[] SET_NON_STROKING_COLOR_COMPLEX = getISOBytes("scn\n");

    private static final byte[] OPENING_BRACKET = getISOBytes("[");
    private static final byte[] CLOSING_BRACKET = getISOBytes("]");

    private static final int SPACE = 32;

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
        PDStream contents = sourcePage.getContents();
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
        appendRawCommands(BEGIN_TEXT);
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
        appendRawCommands(END_TEXT);
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
        String fontMapping = resources.addFont(font);
        appendRawCommands("/");
        appendRawCommands(fontMapping);
        appendRawCommands(SPACE);
        appendRawCommands(fontSize);
        appendRawCommands(SPACE);
        appendRawCommands(SET_FONT);
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
    public void drawImage(PDXObjectImage image, float x, float y) throws IOException
    {
        drawXObject(image, x, y, image.getWidth(), image.getHeight());
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
     */
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
     */
    public void drawXObject(PDXObject xobject, AffineTransform transform) throws IOException
    {
        if (inTextMode)
        {
            throw new IOException("Error: drawXObject is not allowed within a text block.");
        }
        String xObjectPrefix = null;
        if (xobject instanceof PDXObjectImage)
        {
            xObjectPrefix = "Im";
        }
        else
        {
            xObjectPrefix = "Form";
        }
        String objMapping = resources.addXObject(xobject, xObjectPrefix);
        saveGraphicsState();
        appendRawCommands(SPACE);
        concatenate2CTM(transform);
        appendRawCommands(SPACE);
        appendRawCommands("/");
        appendRawCommands(objMapping);
        appendRawCommands(SPACE);
        appendRawCommands(XOBJECT_DO);
        restoreGraphicsState();
    }

    /**
     * The Td operator.
     * A current text matrix will be replaced with a new one (1 0 0 1 x y).
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @throws IOException If there is an error writing to the stream.
     */
    public void moveTextPositionByAmount(float x, float y) throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: must call beginText() before moveTextPositionByAmount");
        }
        appendRawCommands(x);
        appendRawCommands(SPACE);
        appendRawCommands(y);
        appendRawCommands(SPACE);
        appendRawCommands(MOVE_TEXT_POSITION);
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
     */
    public void setTextMatrix(double a, double b, double c, double d, double e, double f) throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: must call beginText() before setTextMatrix");
        }
        appendRawCommands(a);
        appendRawCommands(SPACE);
        appendRawCommands(b);
        appendRawCommands(SPACE);
        appendRawCommands(c);
        appendRawCommands(SPACE);
        appendRawCommands(d);
        appendRawCommands(SPACE);
        appendRawCommands(e);
        appendRawCommands(SPACE);
        appendRawCommands(f);
        appendRawCommands(SPACE);
        appendRawCommands(SET_TEXT_MATRIX);
    }

    /**
    * The Tm operator. Sets the text matrix to the given values.
    * A current text matrix will be replaced with the new one.
    * @param matrix the transformation matrix
    * @throws IOException If there is an error writing to the stream.
    */
    public void setTextMatrix(AffineTransform matrix) throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: must call beginText() before setTextMatrix");
        }
        appendMatrix(matrix);
        appendRawCommands(SET_TEXT_MATRIX);
    }

    /**
     * The Tm operator. Sets the text matrix to the given scaling and translation values.
     * A current text matrix will be replaced with the new one.
     * @param sx The scaling factor in x-direction.
     * @param sy The scaling factor in y-direction.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextScaling(double sx, double sy, double tx, double ty) throws IOException
    {
        setTextMatrix(sx, 0, 0, sy, tx, ty);
    }

    /**
     * The Tm operator. Sets the text matrix to the given translation values.
     * A current text matrix will be replaced with the new one.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextTranslation(double tx, double ty) throws IOException
    {
        setTextMatrix(1, 0, 0, 1, tx, ty);
    }

    /**
     * The Tm operator. Sets the text matrix to the given rotation and translation values.
     * A current text matrix will be replaced with the new one.
     * @param angle The angle used for the counterclockwise rotation in radians.
     * @param tx The translation value in x-direction.
     * @param ty The translation value in y-direction.
     * @throws IOException If there is an error writing to the stream.
     */
    public void setTextRotation(double angle, double tx, double ty) throws IOException
    {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        setTextMatrix(angleCos, angleSin, -angleSin, angleCos, tx, ty);
    }

    /**
     * The Cm operator. Concatenates the current transformation matrix with the given values.
     * @param a The a value of the matrix.
     * @param b The b value of the matrix.
     * @param c The c value of the matrix.
     * @param d The d value of the matrix.
     * @param e The e value of the matrix.
     * @param f The f value of the matrix.
     * @throws IOException If there is an error writing to the stream.
     */
    public void concatenate2CTM(double a, double b, double c, double d, double e, double f) throws IOException
    {
        appendRawCommands(a);
        appendRawCommands(SPACE);
        appendRawCommands(b);
        appendRawCommands(SPACE);
        appendRawCommands(c);
        appendRawCommands(SPACE);
        appendRawCommands(d);
        appendRawCommands(SPACE);
        appendRawCommands(e);
        appendRawCommands(SPACE);
        appendRawCommands(f);
        appendRawCommands(SPACE);
        appendRawCommands(CONCATENATE_MATRIX);
    }

    /**
     * The Cm operator. Concatenates the current transformation matrix with the given
     * {@link AffineTransform}.
     * @param at the transformation matrix
     * @throws IOException If there is an error writing to the stream.
     */
    public void concatenate2CTM(AffineTransform at) throws IOException
    {
        appendMatrix(at);
        appendRawCommands(CONCATENATE_MATRIX);
    }

    /**
     * This will draw a string at the current location on the screen.
     *
     * @param text The text to draw.
     * @throws IOException If an io exception occurs.
     */
    public void drawString(String text) throws IOException
    {
        if (!inTextMode)
        {
            throw new IOException("Error: must call beginText() before drawString");
        }
        COSString string = new COSString(text);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        string.writePDF(buffer);
        appendRawCommands(buffer.toByteArray());
        appendRawCommands(SPACE);
        appendRawCommands(SHOW_TEXT);
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
        appendRawCommands(SET_STROKING_COLORSPACE);
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
        appendRawCommands(SET_NON_STROKING_COLORSPACE);
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
            COSDictionary colorSpaces = (COSDictionary) resources.getCOSDictionary().getDictionaryObject(
                    COSName.COLORSPACE);
            if (colorSpaces == null)
            {
                colorSpaces = new COSDictionary();
                resources.getCOSDictionary().setItem(COSName.COLORSPACE, colorSpaces);
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
        appendRawCommands(SPACE);
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
            appendRawCommands(components[i]);
            appendRawCommands(SPACE);
        }
        if (currentStrokingColorSpace instanceof PDSeparation || currentStrokingColorSpace instanceof PDPattern
                || currentStrokingColorSpace instanceof PDDeviceN || currentStrokingColorSpace instanceof PDICCBased)
        {
            appendRawCommands(SET_STROKING_COLOR_COMPLEX);
        }
        else
        {
            appendRawCommands(SET_STROKING_COLOR_SIMPLE);
        }
    }

    /**
     * Set the stroking color, specified as RGB.
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
        else if (colorSpace.getType() == ColorSpace.TYPE_CMYK)
        {
            color.getColorComponents(colorComponents);
            setStrokingColor(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
        }
        else
        {
            throw new IOException("Error: unknown colorspace:" + colorSpace);
        }
    }

    /**
     * Set the non stroking color, specified as RGB.
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
        else if (colorSpace.getType() == ColorSpace.TYPE_CMYK)
        {
            color.getColorComponents(colorComponents);
            setNonStrokingColor(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
        }
        else
        {
            throw new IOException("Error: unknown colorspace:" + colorSpace);
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
        appendRawCommands(r / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(g / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(b / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(RG_STROKING);
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
        appendRawCommands(c / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(m / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(y / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(k / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(K_STROKING);
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
        appendRawCommands(c);
        appendRawCommands(SPACE);
        appendRawCommands(m);
        appendRawCommands(SPACE);
        appendRawCommands(y);
        appendRawCommands(SPACE);
        appendRawCommands(k);
        appendRawCommands(SPACE);
        appendRawCommands(K_STROKING);
    }

    /**
     * Set the stroking color, specified as grayscale, 0-255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(int g) throws IOException
    {
        appendRawCommands(g / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(G_STROKING);
    }

    /**
     * Set the stroking color, specified as Grayscale 0.0-1.0.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setStrokingColor(double g) throws IOException
    {
        appendRawCommands(g);
        appendRawCommands(SPACE);
        appendRawCommands(G_STROKING);
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
            appendRawCommands(components[i]);
            appendRawCommands(SPACE);
        }
        if (currentNonStrokingColorSpace instanceof PDSeparation || currentNonStrokingColorSpace instanceof PDPattern
                || currentNonStrokingColorSpace instanceof PDDeviceN
                || currentNonStrokingColorSpace instanceof PDICCBased)
        {
            appendRawCommands(SET_NON_STROKING_COLOR_COMPLEX);
        }
        else
        {
            appendRawCommands(SET_NON_STROKING_COLOR_SIMPLE);
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
        appendRawCommands(r / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(g / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(b / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(RG_NON_STROKING);
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
        appendRawCommands(c / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(m / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(y / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(k / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(K_NON_STROKING);
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
        appendRawCommands(c);
        appendRawCommands(SPACE);
        appendRawCommands(m);
        appendRawCommands(SPACE);
        appendRawCommands(y);
        appendRawCommands(SPACE);
        appendRawCommands(k);
        appendRawCommands(SPACE);
        appendRawCommands(K_NON_STROKING);
    }

    /**
     * Set the non stroking color, specified as grayscale, 0-255.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(int g) throws IOException
    {
        appendRawCommands(g / 255d);
        appendRawCommands(SPACE);
        appendRawCommands(G_NON_STROKING);
    }

    /**
     * Set the non stroking color, specified as Grayscale 0.0-1.0.
     *
     * @param g The gray value.
     * @throws IOException If an IO error occurs while writing to the stream.
     */
    public void setNonStrokingColor(double g) throws IOException
    {
        appendRawCommands(g);
        appendRawCommands(SPACE);
        appendRawCommands(G_NON_STROKING);
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
        appendRawCommands(x);
        appendRawCommands(SPACE);
        appendRawCommands(y);
        appendRawCommands(SPACE);
        appendRawCommands(width);
        appendRawCommands(SPACE);
        appendRawCommands(height);
        appendRawCommands(SPACE);
        appendRawCommands(RECTANGLE);
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
        appendRawCommands(x1);
        appendRawCommands(SPACE);
        appendRawCommands(y1);
        appendRawCommands(SPACE);
        appendRawCommands(x2);
        appendRawCommands(SPACE);
        appendRawCommands(y2);
        appendRawCommands(SPACE);
        appendRawCommands(x3);
        appendRawCommands(SPACE);
        appendRawCommands(y3);
        appendRawCommands(SPACE);
        appendRawCommands(BEZIER_312);
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
        appendRawCommands(x2);
        appendRawCommands(SPACE);
        appendRawCommands(y2);
        appendRawCommands(SPACE);
        appendRawCommands(x3);
        appendRawCommands(SPACE);
        appendRawCommands(y3);
        appendRawCommands(SPACE);
        appendRawCommands(BEZIER_32);
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
        appendRawCommands(x1);
        appendRawCommands(SPACE);
        appendRawCommands(y1);
        appendRawCommands(SPACE);
        appendRawCommands(x3);
        appendRawCommands(SPACE);
        appendRawCommands(y3);
        appendRawCommands(SPACE);
        appendRawCommands(BEZIER_313);
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
        appendRawCommands(x);
        appendRawCommands(SPACE);
        appendRawCommands(y);
        appendRawCommands(SPACE);
        appendRawCommands(MOVE_TO);
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
        appendRawCommands(x);
        appendRawCommands(SPACE);
        appendRawCommands(y);
        appendRawCommands(SPACE);
        appendRawCommands(LINE_TO);
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
        appendRawCommands(STROKE);
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
        appendRawCommands(CLOSE_STROKE);
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
            appendRawCommands(FILL_NON_ZERO);
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            appendRawCommands(FILL_EVEN_ODD);
        }
        else
        {
            throw new IOException("Error: unknown value for winding rule");
        }

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
        appendRawCommands(CLOSE_SUBPATH);
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
            appendRawCommands(CLIP_PATH_NON_ZERO);
            appendRawCommands(NOP);
        }
        else if (windingRule == PathIterator.WIND_EVEN_ODD)
        {
            appendRawCommands(CLIP_PATH_EVEN_ODD);
            appendRawCommands(NOP);
        }
        else
        {
            throw new IOException("Error: unknown value for winding rule");
        }
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
        appendRawCommands(lineWidth);
        appendRawCommands(SPACE);
        appendRawCommands(LINE_WIDTH);
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
            appendRawCommands(Integer.toString(lineJoinStyle));
            appendRawCommands(SPACE);
            appendRawCommands(LINE_JOIN_STYLE);
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
            appendRawCommands(Integer.toString(lineCapStyle));
            appendRawCommands(SPACE);
            appendRawCommands(LINE_CAP_STYLE);
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
        appendRawCommands(OPENING_BRACKET);
        for (float value : pattern)
        {
            appendRawCommands(value);
            appendRawCommands(SPACE);
        }
        appendRawCommands(CLOSING_BRACKET);
        appendRawCommands(SPACE);
        appendRawCommands(phase);
        appendRawCommands(SPACE);
        appendRawCommands(LINE_DASH_PATTERN);
    }

    /**
     * Begin a marked content sequence.
     * @param tag the tag
     * @throws IOException if an I/O error occurs
     */
    public void beginMarkedContentSequence(COSName tag) throws IOException
    {
        appendCOSName(tag);
        appendRawCommands(SPACE);
        appendRawCommands(BMC);
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
        appendCOSName(tag);
        appendRawCommands(SPACE);
        appendCOSName(propsName);
        appendRawCommands(SPACE);
        appendRawCommands(BDC);
    }

    /**
     * End a marked content sequence.
     * @throws IOException if an I/O error occurs
     */
    public void endMarkedContentSequence() throws IOException
    {
        appendRawCommands(EMC);
    }

    /**
     * q operator. Saves the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void saveGraphicsState() throws IOException
    {
        appendRawCommands(SAVE_GRAPHICS_STATE);
    }

    /**
     * Q operator. Restores the current graphics state.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void restoreGraphicsState() throws IOException
    {
        appendRawCommands(RESTORE_GRAPHICS_STATE);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands(String commands) throws IOException
    {
        appendRawCommands(commands.getBytes("ISO-8859-1"));
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param commands The commands to append to the stream.
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands(byte[] commands) throws IOException
    {
        output.write(commands);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a raw byte to the stream.
     *
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands(int data) throws IOException
    {
        output.write(data);
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted double value to the stream.
     *
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands(double data) throws IOException
    {
        appendRawCommands(formatDecimal.format(data));
    }

    /**
     * This will append raw commands to the content stream.
     *
     * @param data Append a formatted float value to the stream.
     *
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendRawCommands(float data) throws IOException
    {
        appendRawCommands(formatDecimal.format(data));
    }

    /**
     * This will append a {@link COSName} to the content stream.
     * @param name the name
     * @throws IOException If an error occurs while writing to the stream.
     */
    public void appendCOSName(COSName name) throws IOException
    {
        name.writePDF(output);
    }

    private void appendMatrix(AffineTransform transform) throws IOException
    {
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            appendRawCommands(v);
            appendRawCommands(SPACE);
        }
    }

    /**
     * Close the content stream.  This must be called when you are done with this
     * object.
     * @throws IOException If the underlying stream has a problem being written to.
     */
    public void close() throws IOException
    {
        output.close();
        currentNonStrokingColorSpace = null;
        currentStrokingColorSpace = null;
        resources = null;
    }
}
