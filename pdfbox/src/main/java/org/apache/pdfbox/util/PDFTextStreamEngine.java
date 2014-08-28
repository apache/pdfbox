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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Properties;

/**
 * PDFStreamEngine subclass for advanced processing of text via TextPosition.
 *
 * @see org.apache.pdfbox.text.TextPosition
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDFTextStreamEngine extends PDFStreamEngine
{
    private static final Log log = LogFactory.getLog(PDFStreamEngine.class);

    private int pageRotation;
    private PDRectangle pageSize;

    private PDFTextStreamEngine()
    {
    }

    /**
     * Constructor with engine properties. The property keys are all PDF operators, the values are
     * class names used to execute those operators. An empty value means that the operator will be
     * silently ignored.
     *
     * @param properties The engine properties.
     */
    public PDFTextStreamEngine(Properties properties)
    {
        super(properties);
    }

    /**
     * This will initialise and process the contents of the stream.
     *
     * @param resources The location to retrieve resources.
     * @param cosStream the Stream to execute.
     * @param pageSize the size of the page
     * @param rotation the page rotation
     * @throws java.io.IOException if there is an error accessing the stream.
     */
    public void processStream(PDResources resources, COSStream cosStream, PDRectangle pageSize,
                              int rotation) throws IOException
    {
        this.pageRotation = rotation;
        this.pageSize = pageSize;
        super.processStream(resources, cosStream, pageSize);
    }

    /**
     * This method was originally written by Ben Litchfield for PDFStreamEngine.
     */
    @Override
    protected void processGlyph(Matrix textRenderingMatrix, float dx, float dy, int code,
                                String unicode, PDFont font) throws IOException
    {
        //
        // legacy calculations which were previously in PDFStreamEngine
        //

        PDGraphicsState state = getGraphicsState();
        Matrix ctm = state.getCurrentTransformationMatrix();
        float fontSize = state.getTextState().getFontSize();
        float horizontalScaling = state.getTextState().getHorizontalScaling() / 100f;
        Matrix textMatrix = getTextMatrix();

        // 1/2 the bbox is used as the height todo: why?
        float glyphHeight = font.getBoundingBox().getHeight() / 2;

        // transform from glyph space -> text space
        float height = (float)font.getFontMatrix().transform(0, glyphHeight).getY();

        // (modified) combined displacement, this is calculated *without* taking the character
        // spacing and word spacing into account, due to legacy code in TextStripper
        float tx = dx * fontSize * horizontalScaling;
        float ty = 0; // todo: support vertical writing mode

        // (modified) combined displacement matrix
        Matrix td = Matrix.getTranslatingInstance(tx, ty);

        // (modified) text rendering matrix
        Matrix nextTextRenderingMatrix = td.multiply(textMatrix).multiply(ctm); // text space -> device space
        float nextX = nextTextRenderingMatrix.getXPosition();
        float nextY = nextTextRenderingMatrix.getYPosition();

        // (modified) width and height calculations
        float dxDisplay = nextX - textRenderingMatrix.getXPosition();
        float dyDisplay = height * textRenderingMatrix.getYScale();

        //
        // start of the original method
        //

        // Note on variable names. There are three different units being used in this code.
        // Character sizes are given in glyph units, text locations are initially given in text
        // units, and we want to save the data in display units. The variable names should end with
        // Text or Disp to represent if the values are in text or disp units (no glyph units are
        // saved).

        float fontSizeText = getGraphicsState().getTextState().getFontSize();
        float horizontalScalingText = getGraphicsState().getTextState().getHorizontalScaling()/100f;
        //Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

        float glyphSpaceToTextSpaceFactor = 1 / 1000f;
        if (font instanceof PDType3Font)
        {
            // This will typically be 1000 but in the case of a type3 font
            // this might be a different number
            glyphSpaceToTextSpaceFactor = 1f / font.getFontMatrix().getValue(0, 0);
        }

        float spaceWidthText = 0;
        try
        {
            // to avoid crash as described in PDFBOX-614, see what the space displacement should be
            spaceWidthText = font.getSpaceWidth() * glyphSpaceToTextSpaceFactor;
        }
        catch (Throwable exception)
        {
            log.warn(exception, exception);
        }

        if (spaceWidthText == 0)
        {
            spaceWidthText = font.getAverageFontWidth() * glyphSpaceToTextSpaceFactor;
            // the average space width appears to be higher than necessary so make it smaller
            spaceWidthText *= .80f;
        }
        if (spaceWidthText == 0)
        {
            spaceWidthText = 1.0f; // if could not find font, use a generic value
        }

        // the space width has to be transformed into display units
        float spaceWidthDisplay = spaceWidthText * fontSizeText * horizontalScalingText *
                textRenderingMatrix.getXScale()  * ctm.getXScale();

        // when there is no Unicode mapping available, Acrobat simply coerces the character code
        // into Unicode, so we do the same. Subclasses of PDFStreamEngine don't necessarily want
        // this, which is why we leave it until this point in PDFTextStreamEngine.
        if (unicode == null)
        {
            if (font instanceof PDSimpleFont)
            {
                char c = (char) code;
                unicode = new String(new char[] { c });
            }
            else
            {
                // Acrobat doesn't seem to coerce composite font's character codes, instead it
                // skips them. See the "allah2.pdf" TestTextStripper file.
                return;
            }
        }

        processTextPosition(new TextPosition(pageRotation, pageSize.getWidth(),
                pageSize.getHeight(), textRenderingMatrix, nextX, nextY,
                dyDisplay, dxDisplay,
                spaceWidthDisplay, unicode, new int[] { code } , font, fontSize,
                (int)(fontSize * textRenderingMatrix.getXScale())));
    }

    /**
     * A method provided as an event interface to allow a subclass to perform some specific
     * functionality when text needs to be processed.
     *
     * @param text The text to be processed.
     */
    protected void processTextPosition(TextPosition text)
    {
        // subclasses can override to provide specific functionality
    }
}
