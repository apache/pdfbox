/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.annotation.handlers;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;

/**
 *
 * @author Tilman Hausherr
 */
public class PDTextAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDTextAppearanceHandler.class);

    public PDTextAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    @Override
    public void generateAppearanceStreams()
    {
        generateNormalAppearance();
        generateRolloverAppearance();
        generateDownAppearance();
    }

    @Override
    public void generateNormalAppearance()
    {
        PDAnnotationText annotation = (PDAnnotationText) getAnnotation();
        if (!PDAnnotationText.NAME_NOTE.equals(annotation.getName()) &&
                !PDAnnotationText.NAME_INSERT.equals(annotation.getName()) &&
                !PDAnnotationText.NAME_CIRCLE.equals(annotation.getName()))
        {
            //TODO Comment, Key, Help, NewParagraph, Paragraph
            return;
        }

        try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
        {
            PDColor bgColor = getColor();
            if (bgColor == null)
            {
                // White is used by Adobe when /C entry is missing
                contentStream.setNonStrokingColor(1f);
            }
            else
            {
                contentStream.setNonStrokingColor(bgColor);
            }
            // stroking color is always black which is the PDF default

            setOpacity(contentStream, annotation.getConstantOpacity());
            
            PDRectangle rect = getRectangle();
            PDRectangle bbox = rect.createRetranslatedRectangle();
            annotation.getNormalAppearanceStream().setBBox(bbox);

            switch (annotation.getName())
            {
                case PDAnnotationText.NAME_NOTE:
                    drawNote(contentStream, bbox);
                    break;
                case PDAnnotationText.NAME_CIRCLE:
                    drawCircles(contentStream, bbox);
                    break;
                case PDAnnotationText.NAME_INSERT:
                    drawInsert(contentStream, bbox);
                    break;

                default:
                    break;
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    private void drawNote(final PDAppearanceContentStream contentStream, PDRectangle bbox)
            throws IOException
    {
        contentStream.setLineJoinStyle(1); // get round edge the easy way
        contentStream.setLineWidth(0.61f); // value from Adobe
        contentStream.addRect(1, 1, bbox.getWidth() - 2, bbox.getHeight() - 2);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 2);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 2);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 3);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 3);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 4);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 4);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 5);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 5);
        contentStream.fillAndStroke();
    }

    private void drawCircles(final PDAppearanceContentStream contentStream, PDRectangle bbox)
            throws IOException
    {
        // strategy used by Adobe:
        // 1) add small circle in white using /ca /CA 0.6 and width 1
        // 2) fill
        // 3) add small circle in one direction
        // 4) add large circle in other direction
        // 5) stroke + fill
        // with square width 20 small r = 6.36, large r = 9.756

        // should be a square, but who knows...
        float min = Math.min(bbox.getWidth(), bbox.getHeight());
        float smallR = min / 20 * 6.36f;
        float largeR = min / 20 * 9.756f;

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.saveGraphicsState();
        contentStream.setLineWidth(1);
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setAlphaSourceFlag(false);
        gs.setStrokingAlphaConstant(0.6f);
        gs.setNonStrokingAlphaConstant(0.6f);
        gs.setBlendMode(BlendMode.NORMAL);
        contentStream.setGraphicsStateParameters(gs);
        contentStream.setNonStrokingColor(1f);
        drawCircle(contentStream, bbox.getWidth() / 2, bbox.getHeight() / 2, smallR);
        contentStream.fill();
        contentStream.restoreGraphicsState();

        contentStream.setLineWidth(0.59f); // value from Adobe
        drawCircle(contentStream, bbox.getWidth() / 2, bbox.getHeight() / 2, smallR);
        drawCircle2(contentStream, bbox.getWidth() / 2, bbox.getHeight() / 2, largeR);
        contentStream.fillAndStroke();
    }

    private void drawInsert(final PDAppearanceContentStream contentStream, PDRectangle bbox)
            throws IOException
    {
        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(0);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe
        contentStream.moveTo(bbox.getWidth() / 2 - 1, bbox.getHeight() - 2);
        contentStream.lineTo(1, 1);
        contentStream.lineTo(bbox.getWidth() - 2, 1);
        contentStream.closeAndFillAndStroke();
    }

    @Override
    public void generateRolloverAppearance()
    {
        // No rollover appearance generated
    }

    @Override
    public void generateDownAppearance()
    {
        // No down appearance generated
    }
}
