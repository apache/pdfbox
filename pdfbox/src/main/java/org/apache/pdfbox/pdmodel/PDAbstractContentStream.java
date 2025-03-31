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

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.gsub.GsubWorker;
import org.apache.fontbox.ttf.gsub.GsubWorkerFactory;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.NumberFormatUtil;
import org.apache.pdfbox.util.StringUtil;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Provides the ability to write to a content stream.
 *
 * @author Ben Litchfield
 */
abstract class PDAbstractContentStream implements PDContentOutputStream
{
    private static final Logger LOG = LogManager.getLogger(PDAbstractContentStream.class);

    protected final PDDocument document; // may be null

    protected final OutputStream outputStream;
    protected final PDResources resources;

    protected boolean inTextMode = false;
    protected final Deque<PDFont> fontStack = new ArrayDeque<>();

    protected final Deque<PDColorSpace> nonStrokingColorSpaceStack = new ArrayDeque<>();
    protected final Deque<PDColorSpace> strokingColorSpaceStack = new ArrayDeque<>();

    // number format
    private final NumberFormat formatDecimal = NumberFormat.getNumberInstance(Locale.US);
    private final byte[] formatBuffer = new byte[32];

    private final Map<PDType0Font, GsubWorker> gsubWorkers = new HashMap<>();
    private final GsubWorkerFactory gsubWorkerFactory = new GsubWorkerFactory();

    /**
     * Create a new appearance stream.
     *
     * @param document may be null
     * @param outputStream The appearances output stream to write to.
     * @param resources The resources to use
     */
    PDAbstractContentStream(PDDocument document, OutputStream outputStream, PDResources resources)
    {
        this.document = document;
        this.outputStream = outputStream;
        this.resources = resources;

        formatDecimal.setMaximumFractionDigits(4);
        formatDecimal.setGroupingUsed(false);
    }

    /**
     * Sets the maximum number of digits allowed for fractional numbers.
     *
     * @see NumberFormat#setMaximumFractionDigits(int)
     * @param fractionDigitsNumber the maximum number of digits allowed for fractional numbers
     */
    protected void setMaximumFractionDigits(int fractionDigitsNumber)
    {
        formatDecimal.setMaximumFractionDigits(fractionDigitsNumber);
    }

    /**
     * Implement {@link PDContentOutputStream#beginText()}
     */
    @Override
    public void beginText() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: Nested beginText() calls are not allowed.");
        }
        writeOperator(OperatorName.BEGIN_TEXT);
        inTextMode = true;
    }

    /**
     * Implement {@link PDContentOutputStream#endText()}
     */
    @Override
    public void endText() throws IOException
    {
        if (!inTextMode)
        {
            throw new IllegalStateException("Error: You must call beginText() before calling endText.");
        }
        writeOperator(OperatorName.END_TEXT);
        inTextMode = false;
    }

    /**
     * Implement {@link PDContentOutputStream#setFont(PDFont, float)}
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
            fontStack.pop();
            fontStack.push(font);
        }

        // keep track of fonts which are configured for subsetting
        if (font.willBeSubset())
        {
            if (document != null)
            {
                document.getFontsToSubset().add(font);
            }
            else
            {
                LOG.warn(
                        "Using the subsetted font '{}' without a PDDocument context; call subset() before saving",
                        font.getName());
            }
        }
        else if (!font.isEmbedded() && !font.isStandard14())
        {
            LOG.warn("attempting to use font '{}' that isn't embedded", font.getName());
        }

        // complex text layout
        if (font instanceof PDType0Font)
        {
            PDType0Font type0Font = (PDType0Font) font;
            GsubData gsubData = type0Font.getGsubData();
            if (gsubData != GsubData.NO_DATA_FOUND)
            {
                GsubWorker gsubWorker = gsubWorkerFactory.getGsubWorker(type0Font.getCmapLookup(), gsubData);
                gsubWorkers.put(type0Font, gsubWorker);
            }
            else
            {
                LOG.info("No GSUB data found in font {}", font.getName());
            }
        }

        writeOperand(resources.add(font));
        writeOperand(fontSize);
        writeOperator(OperatorName.SET_FONT_AND_SIZE);
    }

    /**
     * Implement {@link PDContentOutputStream#showTextWithPositioning(Object[])}
     */
    @Override
    public void showTextWithPositioning(Object[] textWithPositioningArray) throws IOException
    {
        write("[");
        for (Object obj : textWithPositioningArray)
        {
            if (obj instanceof String)
            {
                showTextInternal((String) obj);
            }
            else if (obj instanceof Float)
            {
                writeOperand((Float) obj);
            }
            else
            {
                throw new IllegalArgumentException("Argument must consist of array of Float and String types");
            }
        }
        write("] ");
        writeOperator(OperatorName.SHOW_TEXT_ADJUSTED);
    }

    /**
     * Implement {@link PDContentOutputStream#showText(String)}
     */
    @Override
    public void showText(String text) throws IOException
    {
        showTextInternal(text);
        write(" ");
        writeOperator(OperatorName.SHOW_TEXT);
    }

    /**
     * Outputs a string using the correct encoding and subsetting as required.
     *
     * @param text The Unicode text to show.
     *
     * @throws IOException If an io exception occurs.
     */
    protected void showTextInternal(String text) throws IOException
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

        // complex text layout
        byte[] encodedText = null;
        if (font instanceof PDType0Font)
        {
            GsubWorker gsubWorker = gsubWorkers.get(font);
            if (gsubWorker != null)
            {
                PDType0Font type0Font = (PDType0Font) font;
                Set<Integer> glyphIds = new HashSet<>();
                encodedText = encodeForGsub(gsubWorker, glyphIds, type0Font, text);
                if (type0Font.willBeSubset())
                {
                    type0Font.addGlyphsToSubset(glyphIds);
                }
            }
        }

        if (encodedText == null)
        {
            encodedText = font.encode(text);
        }

        // Unicode code points to keep when subsetting
        if (font.willBeSubset())
        {
            int offset = 0;
            while (offset < text.length())
            {
                int codePoint = text.codePointAt(offset);
                font.addToSubset(codePoint);
                offset += Character.charCount(codePoint);
            }
        }

        COSWriter.writeString(encodedText, outputStream);
    }

    /**
     * Implement {@link PDContentOutputStream#setLeading(float)}
     */
    @Override
    public void setLeading(float leading) throws IOException
    {
        writeOperand(leading);
        writeOperator(OperatorName.SET_TEXT_LEADING);
    }

    /**
     * Implement {@link PDContentOutputStream#newLine()}
     */
    @Override
    public void newLine() throws IOException
    {
        if (!inTextMode)
        {
            throw new IllegalStateException("Must call beginText() before newLine()");
        }
        writeOperator(OperatorName.NEXT_LINE);
    }

    /**
     * Implement {@link PDContentOutputStream#newLineAtOffset(float, float)}
     */
    @Override
    public void newLineAtOffset(float tx, float ty) throws IOException
    {
        if (!inTextMode)
        {
            throw new IllegalStateException("Error: must call beginText() before newLineAtOffset()");
        }
        writeOperand(tx);
        writeOperand(ty);
        writeOperator(OperatorName.MOVE_TEXT);
    }

    /**
     * Implement {@link PDContentOutputStream#setTextMatrix(Matrix)}
     */
    @Override
    public void setTextMatrix(Matrix matrix) throws IOException
    {
        if (!inTextMode)
        {
            throw new IllegalStateException("Error: must call beginText() before setTextMatrix");
        }
        writeAffineTransform(matrix.createAffineTransform());
        writeOperator(OperatorName.SET_MATRIX);
    }

    /**
     * Implement {@link PDContentOutputStream#drawImage(PDImageXObject, float, float)}
     */
    @Override
    public void drawImage(PDImageXObject image, float x, float y) throws IOException
    {
        drawImage(image, x, y, image.getWidth(), image.getHeight());
    }

    /**
     * Implement {@link PDContentOutputStream#drawImage(PDImageXObject, float, float, float, float)}
     */
    @Override
    public void drawImage(PDImageXObject image, float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawImage is not allowed within a text block.");
        }

        saveGraphicsState();

        AffineTransform transform = new AffineTransform(width, 0, 0, height, x, y);
        transform(new Matrix(transform));

        writeOperand(resources.add(image));
        writeOperator(OperatorName.DRAW_OBJECT);

        restoreGraphicsState();
    }

    /**
     * Implement {@link PDContentOutputStream#drawImage(PDImageXObject, Matrix)}
     */
    @Override
    public void drawImage(PDImageXObject image, Matrix matrix) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawImage is not allowed within a text block.");
        }

        saveGraphicsState();

        AffineTransform transform = matrix.createAffineTransform();
        transform(new Matrix(transform));

        writeOperand(resources.add(image));
        writeOperator(OperatorName.DRAW_OBJECT);

        restoreGraphicsState();
    }

    /**
     * Implement {@link PDContentOutputStream#drawImage(PDInlineImage, float, float)}
     */
    @Override
    public void drawImage(PDInlineImage inlineImage, float x, float y) throws IOException
    {
        drawImage(inlineImage, x, y, inlineImage.getWidth(), inlineImage.getHeight());
    }

    /**
     * Implement {@link PDContentOutputStream#drawImage(PDInlineImage, float, float, float, float)}
     */
    @Override
    public void drawImage(PDInlineImage inlineImage, float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawImage is not allowed within a text block.");
        }

        saveGraphicsState();
        transform(new Matrix(width, 0, 0, height, x, y));

        // create the image dictionary
        StringBuilder sb = new StringBuilder();
        sb.append(OperatorName.BEGIN_INLINE_IMAGE);

        sb.append("\n /W ");
        sb.append(inlineImage.getWidth());

        sb.append("\n /H ");
        sb.append(inlineImage.getHeight());

        sb.append("\n /CS ");
        sb.append('/');
        sb.append(inlineImage.getColorSpace().getName());

        COSArray decodeArray = inlineImage.getDecode();
        if (decodeArray != null && !decodeArray.isEmpty())
        {
            sb.append("\n /D ");
            sb.append('[');
            for (COSBase base : decodeArray)
            {
                sb.append(((COSNumber) base).intValue());
                sb.append(' ');
            }
            sb.append(']');
        }

        if (inlineImage.isStencil())
        {
            sb.append("\n /IM true");
        }

        sb.append("\n /BPC ");
        sb.append(inlineImage.getBitsPerComponent());

        // image dictionary
        write(sb.toString());
        writeLine();

        // binary data
        writeOperator(OperatorName.BEGIN_INLINE_IMAGE_DATA);
        writeBytes(inlineImage.getData());
        writeLine();
        writeOperator(OperatorName.END_INLINE_IMAGE);

        restoreGraphicsState();
    }

    /**
     * Implement {@link PDContentOutputStream#drawForm(PDFormXObject)}
     */
    @Override
    public void drawForm(PDFormXObject form) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: drawForm is not allowed within a text block.");
        }

        writeOperand(resources.add(form));
        writeOperator(OperatorName.DRAW_OBJECT);
    }

    /**
     * Implement {@link PDContentOutputStream#transform(Matrix)}
     */
    @Override
    public void transform(Matrix matrix) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: Modifying the current transformation matrix is not allowed within text objects.");
        }

        writeAffineTransform(matrix.createAffineTransform());
        writeOperator(OperatorName.CONCAT);
    }

    /**
     * Implement {@link PDContentOutputStream#saveGraphicsState()}
     */
    @Override
    public void saveGraphicsState() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: Saving the graphics state is not allowed within text objects.");
        }

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
        writeOperator(OperatorName.SAVE);
    }

    /**
     * Implement {@link PDContentOutputStream#restoreGraphicsState()}
     */
    @Override
    public void restoreGraphicsState() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: Restoring the graphics state is not allowed within text objects.");
        }

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
        writeOperator(OperatorName.RESTORE);
    }

    protected COSName getName(PDColorSpace colorSpace)
    {
        if (colorSpace instanceof PDDeviceGray ||
            colorSpace instanceof PDDeviceRGB ||
            colorSpace instanceof PDDeviceCMYK)
        {
            return COSName.getPDFName(colorSpace.getName());
        }
        else
        {
            return resources.add(colorSpace);
        }
    }

    /**
     * Implement {@link PDContentOutputStream#setStrokingColor(PDColor color)}
     */
    @Override
    public void setStrokingColor(PDColor color) throws IOException
    {
        if (strokingColorSpaceStack.isEmpty() ||
            strokingColorSpaceStack.peek() != color.getColorSpace())
        {
            writeOperand(getName(color.getColorSpace()));
            writeOperator(OperatorName.STROKING_COLORSPACE);
            setStrokingColorSpaceStack(color.getColorSpace());
        }

        for (float value : color.getComponents())
        {
            writeOperand(value);
        }

        if (color.getColorSpace() instanceof PDPattern)
        {
            writeOperand(color.getPatternName());
        }

        if (color.getColorSpace() instanceof PDPattern ||
            color.getColorSpace() instanceof PDSeparation ||
            color.getColorSpace() instanceof PDDeviceN ||
            color.getColorSpace() instanceof PDICCBased)
        {
            writeOperator(OperatorName.STROKING_COLOR_N);
        }
        else
        {
            writeOperator(OperatorName.STROKING_COLOR);
        }
    }

    /**
     * Implement {@link PDContentOutputStream#setStrokingColor(Color)}
     */
    @Override
    public void setStrokingColor(Color color) throws IOException
    {
        float[] components = {
                color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f };
        PDColor pdColor = new PDColor(components, PDDeviceRGB.INSTANCE);
        setStrokingColor(pdColor);
    }

    /**
     * Implement {@link PDContentOutputStream#setStrokingColor(float, float, float)}
     */
    @Override
    public void setStrokingColor(float r, float g, float b) throws IOException
    {
        if (isOutsideOneInterval(r) || isOutsideOneInterval(g) || isOutsideOneInterval(b))
        {
            throw new IllegalArgumentException("Parameters must be within 0..1, but are "
                    + String.format("(%.2f,%.2f,%.2f)", r, g, b));
        }
        writeOperand(r);
        writeOperand(g);
        writeOperand(b);
        writeOperator(OperatorName.STROKING_COLOR_RGB);
        setStrokingColorSpaceStack(PDDeviceRGB.INSTANCE);
    }

    /**
     * Implement {@link PDContentOutputStream#setStrokingColor(float, float, float, float)}
     */
    @Override
    public void setStrokingColor(float c, float m, float y, float k) throws IOException
    {
        if (isOutsideOneInterval(c) || isOutsideOneInterval(m) || isOutsideOneInterval(y) || isOutsideOneInterval(k))
        {
            throw new IllegalArgumentException("Parameters must be within 0..1, but are "
                    + String.format("(%.2f,%.2f,%.2f,%.2f)", c, m, y, k));
        }
        writeOperand(c);
        writeOperand(m);
        writeOperand(y);
        writeOperand(k);
        writeOperator(OperatorName.STROKING_COLOR_CMYK);
        setStrokingColorSpaceStack(PDDeviceCMYK.INSTANCE);
    }

    /**
     * Implement {@link PDContentOutputStream#setStrokingColor(float)}
     */
    @Override
    public void setStrokingColor(float g) throws IOException
    {
        if (isOutsideOneInterval(g))
        {
            throw new IllegalArgumentException("Parameter must be within 0..1, but is " + g);
        }
        writeOperand(g);
        writeOperator(OperatorName.STROKING_COLOR_GRAY);
        setStrokingColorSpaceStack(PDDeviceGray.INSTANCE);
    }

    /**
     * Implement {@link PDContentOutputStream#setNonStrokingColor(PDColor)}
     */
    @Override
    public void setNonStrokingColor(PDColor color) throws IOException
    {
        if (nonStrokingColorSpaceStack.isEmpty() ||
            nonStrokingColorSpaceStack.peek() != color.getColorSpace())
        {
            writeOperand(getName(color.getColorSpace()));
            writeOperator(OperatorName.NON_STROKING_COLORSPACE);
            setNonStrokingColorSpaceStack(color.getColorSpace());
        }

        for (float value : color.getComponents())
        {
            writeOperand(value);
        }

        if (color.getColorSpace() instanceof PDPattern)
        {
            writeOperand(color.getPatternName());
        }

        if (color.getColorSpace() instanceof PDPattern ||
            color.getColorSpace() instanceof PDSeparation ||
            color.getColorSpace() instanceof PDDeviceN ||
            color.getColorSpace() instanceof PDICCBased)
        {
            writeOperator(OperatorName.NON_STROKING_COLOR_N);
        }
        else
        {
            writeOperator(OperatorName.NON_STROKING_COLOR);
        }
    }

    /**
     * Implement {@link PDContentOutputStream#setNonStrokingColor(Color)}
     */
    @Override
    public void setNonStrokingColor(Color color) throws IOException
    {
        float[] components = {
                color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f };
        PDColor pdColor = new PDColor(components, PDDeviceRGB.INSTANCE);
        setNonStrokingColor(pdColor);
    }

    /**
     * Implement {@link PDContentOutputStream#setNonStrokingColor(float, float, float)}
     */
    @Override
    public void setNonStrokingColor(float r, float g, float b) throws IOException
    {
        if (isOutsideOneInterval(r) || isOutsideOneInterval(g) || isOutsideOneInterval(b))
        {
            throw new IllegalArgumentException("Parameters must be within 0..1, but are "
                    + String.format("(%.2f,%.2f,%.2f)", r, g, b));
        }
        writeOperand(r);
        writeOperand(g);
        writeOperand(b);
        writeOperator(OperatorName.NON_STROKING_RGB);
        setNonStrokingColorSpaceStack(PDDeviceRGB.INSTANCE);
    }

    /**
     * Implement {@link PDContentOutputStream#setNonStrokingColor(float, float, float, float)}
     */
    @Override
    public void setNonStrokingColor(float c, float m, float y, float k) throws IOException
    {
        if (isOutsideOneInterval(c) || isOutsideOneInterval(m) || isOutsideOneInterval(y) || isOutsideOneInterval(k))
        {
            throw new IllegalArgumentException("Parameters must be within 0..1, but are "
                    + String.format("(%.2f,%.2f,%.2f,%.2f)", c, m, y, k));
        }
        writeOperand(c);
        writeOperand(m);
        writeOperand(y);
        writeOperand(k);
        writeOperator(OperatorName.NON_STROKING_CMYK);
        setNonStrokingColorSpaceStack(PDDeviceCMYK.INSTANCE);
    }

    /**
     * Implement {@link PDContentOutputStream#setNonStrokingColor(float)}
     */
    @Override
    public void setNonStrokingColor(float g) throws IOException
    {
        if (isOutsideOneInterval(g))
        {
            throw new IllegalArgumentException("Parameter must be within 0..1, but is " + g);
        }
        writeOperand(g);
        writeOperator(OperatorName.NON_STROKING_GRAY);
        setNonStrokingColorSpaceStack(PDDeviceGray.INSTANCE);
    }

    /**
     * Implement {@link PDContentOutputStream#addRect(float, float, float, float)}
     */
    @Override
    public void addRect(float x, float y, float width, float height) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: addRect is not allowed within a text block.");
        }
        writeOperand(x);
        writeOperand(y);
        writeOperand(width);
        writeOperand(height);
        writeOperator(OperatorName.APPEND_RECT);
    }

    /**
     * Implement {@link PDContentOutputStream#curveTo(float, float, float, float, float, float)}
     */
    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: curveTo is not allowed within a text block.");
        }
        writeOperand(x1);
        writeOperand(y1);
        writeOperand(x2);
        writeOperand(y2);
        writeOperand(x3);
        writeOperand(y3);
        writeOperator(OperatorName.CURVE_TO);
    }

    /**
     * Implement {@link PDContentOutputStream#curveTo2(float, float, float, float)}
     */
    @Override
    public void curveTo2(float x2, float y2, float x3, float y3) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: curveTo2 is not allowed within a text block.");
        }
        writeOperand(x2);
        writeOperand(y2);
        writeOperand(x3);
        writeOperand(y3);
        writeOperator(OperatorName.CURVE_TO_REPLICATE_INITIAL_POINT);
    }

    /**
     * Implement {@link PDContentOutputStream#curveTo1(float, float, float, float)}
     */
    @Override
    public void curveTo1(float x1, float y1, float x3, float y3) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: curveTo1 is not allowed within a text block.");
        }
        writeOperand(x1);
        writeOperand(y1);
        writeOperand(x3);
        writeOperand(y3);
        writeOperator(OperatorName.CURVE_TO_REPLICATE_FINAL_POINT);
    }

    /**
     * Implement {@link PDContentOutputStream#moveTo(float, float)}
     */
    @Override
    public void moveTo(float x, float y) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: moveTo is not allowed within a text block.");
        }
        writeOperand(x);
        writeOperand(y);
        writeOperator(OperatorName.MOVE_TO);
    }

    /**
     * Implement {@link PDContentOutputStream#lineTo(float, float)}
     */
    @Override
    public void lineTo(float x, float y) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: lineTo is not allowed within a text block.");
        }
        writeOperand(x);
        writeOperand(y);
        writeOperator(OperatorName.LINE_TO);
    }

    /**
     * Implement {@link PDContentOutputStream#stroke()}
     */
    @Override
    public void stroke() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: stroke is not allowed within a text block.");
        }
        writeOperator(OperatorName.STROKE_PATH);
    }

    /**
     * Implement {@link PDContentOutputStream#closeAndStroke()}
     */
    @Override
    public void closeAndStroke() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: closeAndStroke is not allowed within a text block.");
        }
        writeOperator(OperatorName.CLOSE_AND_STROKE);
    }

    /**
     * Implement {@link PDContentOutputStream#fill()}
     */
    @Override
    public void fill() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: fill is not allowed within a text block.");
        }
        writeOperator(OperatorName.FILL_NON_ZERO);
    }

    /**
     * Implement {@link PDContentOutputStream#fillEvenOdd()}
     */
    @Override
    public void fillEvenOdd() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: fillEvenOdd is not allowed within a text block.");
        }
        writeOperator(OperatorName.FILL_EVEN_ODD);
    }

    /**
     * Implement {@link PDContentOutputStream#fillAndStroke()}
     */
    @Override
    public void fillAndStroke() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: fillAndStroke is not allowed within a text block.");
        }
        writeOperator(OperatorName.FILL_NON_ZERO_AND_STROKE);
    }

    /**
     * Implement {@link PDContentOutputStream#fillAndStrokeEvenOdd()}
     */
    @Override
    public void fillAndStrokeEvenOdd() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: fillAndStrokeEvenOdd is not allowed within a text block.");
        }
        writeOperator(OperatorName.FILL_EVEN_ODD_AND_STROKE);
    }

    /**
     * Implement {@link PDContentOutputStream#closeAndFillAndStroke()}
     */
    @Override
    public void closeAndFillAndStroke() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: closeAndFillAndStroke is not allowed within a text block.");
        }
        writeOperator(OperatorName.CLOSE_FILL_NON_ZERO_AND_STROKE);
    }

    /**
     * Implement {@link PDContentOutputStream#closeAndFillAndStrokeEvenOdd()}
     */
    @Override
    public void closeAndFillAndStrokeEvenOdd() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: closeAndFillAndStrokeEvenOdd is not allowed within a text block.");
        }
        writeOperator(OperatorName.CLOSE_FILL_EVEN_ODD_AND_STROKE);
    }

    /**
     * Implement {@link PDContentOutputStream#shadingFill(PDShading)}
     */
    @Override
    public void shadingFill(PDShading shading) throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: shadingFill is not allowed within a text block.");
        }

        writeOperand(resources.add(shading));
        writeOperator(OperatorName.SHADING_FILL);
    }

    /**
     * Implement {@link PDContentOutputStream#closePath()}
     */
    @Override
    public void closePath() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: closePath is not allowed within a text block.");
        }
        writeOperator(OperatorName.CLOSE_PATH);
    }

    /**
     * Implement {@link PDContentOutputStream#clip()}
     */
    @Override
    public void clip() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: clip is not allowed within a text block.");
        }
        writeOperator(OperatorName.CLIP_NON_ZERO);

        // end path without filling or stroking
        writeOperator(OperatorName.ENDPATH);
    }

    /**
     * Implement {@link PDContentOutputStream#clipEvenOdd()}
     */
    @Override
    public void clipEvenOdd() throws IOException
    {
        if (inTextMode)
        {
            throw new IllegalStateException("Error: clipEvenOdd is not allowed within a text block.");
        }
        writeOperator(OperatorName.CLIP_EVEN_ODD);

        // end path without filling or stroking
        writeOperator(OperatorName.ENDPATH);
    }

    /**
     * Implement {@link PDContentOutputStream#setLineWidth(float)}}
     */
    @Override
    public void setLineWidth(float lineWidth) throws IOException
    {
        writeOperand(lineWidth);
        writeOperator(OperatorName.SET_LINE_WIDTH);
    }

    /**
     * Implement {@link PDContentOutputStream#setLineJoinStyle(int)}
     */
    @Override
    public void setLineJoinStyle(int lineJoinStyle) throws IOException
    {
        if (lineJoinStyle >= 0 && lineJoinStyle <= 2)
        {
            writeOperand(lineJoinStyle);
            writeOperator(OperatorName.SET_LINE_JOINSTYLE);
        }
        else
        {
            throw new IllegalArgumentException("Error: unknown value for line join style");
        }
    }

    /**
     * Implement {@link PDContentOutputStream#setLineCapStyle(int)}
     */
    @Override
    public void setLineCapStyle(int lineCapStyle) throws IOException
    {
        if (lineCapStyle >= 0 && lineCapStyle <= 2)
        {
            writeOperand(lineCapStyle);
            writeOperator(OperatorName.SET_LINE_CAPSTYLE);
        }
        else
        {
            throw new IllegalArgumentException("Error: unknown value for line cap style");
        }
    }

    /**
     * Implement {@link PDContentOutputStream#setLineDashPattern(float[], float)}
     */
    @Override
    public void setLineDashPattern(float[] pattern, float phase) throws IOException
    {
        write("[");
        for (float value : pattern)
        {
            writeOperand(value);
        }
        write("] ");
        writeOperand(phase);
        writeOperator(OperatorName.SET_LINE_DASHPATTERN);
    }

    /**
     * Implement {@link PDContentOutputStream#setMiterLimit(float)}
     */
    @Override
    public void setMiterLimit(float miterLimit) throws IOException
    {
        if (miterLimit <= 0.0)
        {
            throw new IllegalArgumentException("A miter limit <= 0 is invalid and will not render in Acrobat Reader");
        }
        writeOperand(miterLimit);
        writeOperator(OperatorName.SET_LINE_MITERLIMIT);
    }

    /**
     * Implement {@link PDContentOutputStream#beginMarkedContent(COSName)}
     */
    @Override
    public void beginMarkedContent(COSName tag) throws IOException
    {
        writeOperand(tag);
        writeOperator(OperatorName.BEGIN_MARKED_CONTENT);
    }

    /**
     * Implement {@link PDContentOutputStream#beginMarkedContent(COSName, int)}
     */
    @Override
    public void beginMarkedContent(COSName tag, int mcid) throws IOException
    {
        if (mcid < 0)
        {
            throw new IllegalArgumentException("mcid should not be negative");
        }
        writeOperand(tag);
        write("<</MCID " + mcid + ">> ");
        writeOperator(OperatorName.BEGIN_MARKED_CONTENT_SEQ);
    }

    /**
     * Implement {@link PDContentOutputStream#beginMarkedContent(COSName, PDPropertyList)}
     */
    @Override
    public void beginMarkedContent(COSName tag, PDPropertyList propertyList) throws IOException
    {
        writeOperand(tag);

        COSDictionary dict = propertyList.getCOSObject();
        if (dict.getInt(COSName.MCID) > -1 && dict.size() == 1)
        {
            // PDFBOX-5890: use simplified notation if there's only an MCID
            write("<</MCID " + dict.getInt(COSName.MCID) + ">> ");
        }
        else
        {
            writeOperand(resources.add(propertyList));
        }

        writeOperator(OperatorName.BEGIN_MARKED_CONTENT_SEQ);
    }

    /**
     * Implement {@link PDContentOutputStream#beginMarkedContent(COSName, PDPropertyList)}
     */
    @Override
    public void endMarkedContent() throws IOException
    {
        writeOperator(OperatorName.END_MARKED_CONTENT);
    }

    /**
     * Implement {@link PDContentOutputStream#setGraphicsStateParameters(PDExtendedGraphicsState)}
     */
    @Override
    public void setGraphicsStateParameters(PDExtendedGraphicsState state) throws IOException
    {
        writeOperand(resources.add(state));
        writeOperator(OperatorName.SET_GRAPHICS_STATE_PARAMS);
    }

    /**
     * Implement {@link PDContentOutputStream#addComment(String)}
     */
    @Override
    public void addComment(String comment) throws IOException
    {
        if (comment.indexOf('\n') >= 0 || comment.indexOf('\r') >= 0)
        {
            throw new IllegalArgumentException("comment should not include a newline");
        }
        outputStream.write('%');
        outputStream.write(comment.getBytes(StandardCharsets.US_ASCII));
        outputStream.write('\n');
    }

    /**
     * Writes a real number to the content stream.
     *
     * @param real the real number to be added to the content stream
     *
     * @throws IOException If the underlying stream has a problem being written to.
     * @throws IllegalArgumentException if the parameter is not a finite number
     */
    protected void writeOperand(float real) throws IOException
    {
        if (!Float.isFinite(real))
        {
            throw new IllegalArgumentException(real + " is not a finite number");
        }
        int byteCount = NumberFormatUtil.formatFloatFast(real, formatDecimal.getMaximumFractionDigits(), formatBuffer);

        if (byteCount == -1)
        {
            //Fast formatting failed
            write(formatDecimal.format(real));
        }
        else
        {
            outputStream.write(formatBuffer, 0, byteCount);
        }
        outputStream.write(' ');
    }

    /**
     * Writes an integer number to the content stream.
     *
     * @param integer the integer to be added to the content stream
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void writeOperand(int integer) throws IOException
    {
        write(formatDecimal.format(integer));
        outputStream.write(' ');
    }

    /**
     * Writes a COSName to the content stream.
     *
     * @param name the name to be added to the content stream
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void writeOperand(COSName name) throws IOException
    {
        name.writePDF(outputStream);
        outputStream.write(' ');
    }

    /**
     * Writes a string to the content stream as ASCII.
     *
     * @param text the text to be added to the content stream followed by a newline
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void writeOperator(String text) throws IOException
    {
        write(text);
        writeLine();
    }

    /**
     * Writes a string to the content stream as ASCII.
     *
     * @param text the text to be added to the content stream
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void write(String text) throws IOException
    {
        writeBytes(text.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Writes a newline to the content stream as ASCII.
     *
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void writeLine() throws IOException
    {
        outputStream.write('\n');
    }

    /**
     * Writes binary data to the content stream.
     *
     * @param data as byte formatted to be added to the content stream
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void writeBytes(byte[] data) throws IOException
    {
        outputStream.write(data);
    }

    /**
     * Writes an AffineTransform to the content stream as an array.
     *
     * @param transform AffineTransfrom to be added to the content stream
     * @throws IOException If the underlying stream has a problem being written to.
     */
    private void writeAffineTransform(AffineTransform transform) throws IOException
    {
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            writeOperand((float) v);
        }
    }

    /**
     * Implement {@link PDContentOutputStream#close()}
     */
    @Override
    public void close() throws IOException
    {
        if (inTextMode)
        {
            LOG.warn("You did not call endText(), some viewers won't display your text");
        }
        outputStream.close();
    }

    protected boolean isOutside255Interval(int val)
    {
        return val < 0 || val > 255;
    }

    private boolean isOutsideOneInterval(double val)
    {
        return val < 0 || val > 1;
    }

    protected void setStrokingColorSpaceStack(PDColorSpace colorSpace)
    {
        if (strokingColorSpaceStack.isEmpty())
        {
            strokingColorSpaceStack.add(colorSpace);
        }
        else
        {
            strokingColorSpaceStack.pop();
            strokingColorSpaceStack.push(colorSpace);
        }
    }

    protected void setNonStrokingColorSpaceStack(PDColorSpace colorSpace)
    {
        if (nonStrokingColorSpaceStack.isEmpty())
        {
            nonStrokingColorSpaceStack.add(colorSpace);
        }
        else
        {
            nonStrokingColorSpaceStack.pop();
            nonStrokingColorSpaceStack.push(colorSpace);
        }
    }

    /**
     * Implement {@link PDContentOutputStream#setCharacterSpacing(float)}
     */
    @Override
    public void setCharacterSpacing(float spacing) throws IOException
    {
        writeOperand(spacing);
        writeOperator(OperatorName.SET_CHAR_SPACING);
    }

    /**
     * Implement {@link PDContentOutputStream#setWordSpacing(float)}
     */
    @Override
    public void setWordSpacing(float spacing) throws IOException
    {
        writeOperand(spacing);
        writeOperator(OperatorName.SET_WORD_SPACING);
    }

    /**
     * Implement {@link PDContentOutputStream#setHorizontalScaling(float)}
     */
    @Override
    public void setHorizontalScaling(float scale) throws IOException
    {
        writeOperand(scale);
        writeOperator(OperatorName.SET_TEXT_HORIZONTAL_SCALING);
    }

    /**
     * Implement {@link PDContentOutputStream#setRenderingMode(RenderingMode)}
     */
    @Override
    public void setRenderingMode(RenderingMode rm) throws IOException
    {
        writeOperand(rm.intValue());
        writeOperator(OperatorName.SET_TEXT_RENDERINGMODE);
    }

    /**
     * Implement {@link PDContentOutputStream#setTextRise(float)}
     */
    @Override
    public void setTextRise(float rise) throws IOException
    {
        writeOperand(rise);
        writeOperator(OperatorName.SET_TEXT_RISE);
    }

    /**
     * Retrieve the encoded glyph IDs for the characters in the specified text, after applying any
     * relevant GSUB rules. The glyph IDs used are also added to the specified glyph ID set.
     *
     * @param gsubWorker The GSUB worker which defines the GSUB transformations to apply.
     * @param glyphIds The set of glyph IDs which is to be populated with the glyph IDs found in the
     * text.
     * @param font The font whose cmap table will be used to map characters to glyph IDs.
     * @param text The text which is being converted from characters to glyph IDs.
     * @return The encoded glyph IDs for the characters in the specified text, after applying any
     * relevant GSUB rules.
     * @throws IOException If there is an error during encoding.
     * @throws IllegalStateException If we cannot find a glyph ID for any characters in the
     * specified text.
     */
    private byte[] encodeForGsub(GsubWorker gsubWorker,
            Set<Integer> glyphIds, PDType0Font font, String text) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2 * text.length());
        String[] words = StringUtil.tokenizeOnSpace(text);
        for (String word : words)
        {
            if (word.length() == 1 && word.isBlank()) // PDFBOX-5823: optimization
            {
                out.writeBytes(font.encode(word));
            }
            else
            {
                glyphIds.addAll(applyGSUBRules(gsubWorker, out, font, word));
            }
        }
        return out.toByteArray();
    }

    /**
     * Retrieve the glyph IDs for the characters in the specified word, after applying any relevant
     * GSUB rules. The encoded glyph IDs are also written to the specified output stream.
     *
     * @param gsubWorker The GSUB worker which defines the GSUB transformations to apply.
     * @param out The output stream to write the glyph IDs to.
     * @param font The font whose cmap table will be used to map characters to glyph IDs.
     * @param word The word which is being converted from characters to glyph IDs.
     * @return The glyph IDs for the characters in the specified word, after applying any relevant
     * GSUB rules.
     * @throws IllegalStateException If we cannot find a glyph ID for any characters in the
     * specified word.
     */
    private List<Integer> applyGSUBRules(GsubWorker gsubWorker, ByteArrayOutputStream out, PDType0Font font, String word)
    {
        int[] codePoints = word.codePoints().toArray();
        List<Integer> originalGlyphIds = new ArrayList<>(codePoints.length);
        CmapLookup cmapLookup = font.getCmapLookup();

        // convert characters into glyph IDs
        for (int codePoint : codePoints)
        {
            int glyphId = cmapLookup.getGlyphId(codePoint);
            if (glyphId <= 0)
            {
                String source;
                if (Character.isBmpCodePoint(codePoint))
                {
                    source = String.valueOf((char) codePoint);
                }
                else if (Character.isValidCodePoint(codePoint))
                {
                    source = new String(new int[] {codePoint},0,1);
                }
                else
                {
                    source = "?";
                }
                throw new IllegalStateException("could not find the glyphId for the character: " +
                        source + ", codePoint: " + codePoint +
                        " (0x" + Integer.toHexString(codePoint).toUpperCase() + ")");
            }
            originalGlyphIds.add(glyphId);
        }

        // transform glyph IDs, write them to the output stream
        List<Integer> glyphIdsAfterGsub = gsubWorker.applyTransforms(originalGlyphIds);
        for (Integer glyphId : glyphIdsAfterGsub)
        {
            out.writeBytes(font.encodeGlyphId(glyphId));
        }

        return glyphIdsAfterGsub;
    }
}
