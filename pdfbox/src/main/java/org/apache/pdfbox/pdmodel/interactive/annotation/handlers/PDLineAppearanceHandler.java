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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.util.Matrix;

/**
 *
 */
public class PDLineAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDLineAppearanceHandler.class);

    public PDLineAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationLine annotation = (PDAnnotationLine) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getLine();
        if (pathsArray == null)
        {
            return;
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        if (ab.color.getComponents().length == 0)
        {
            return;
        }

        // Adjust rectangle even if not empty, see PLPDF.com-MarkupAnnotations.pdf
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (int i = 0; i < pathsArray.length / 2; ++i)
        {
            float x = pathsArray[i * 2];
            float y = pathsArray[i * 2 + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        // add/substract with and font height
        //TODO also consider arrow and other stuff
        rect.setLowerLeftX(Math.min(minX - ab.width - 12, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width - 12, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width + 12, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width + 12, rect.getUpperRightY()));

        annotation.setRectangle(rect);

        try
        {
            try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream())
            {
                // Tested with Adobe Reader:
                // text is written first (TODO)
                // width 0 is used by Adobe as such (but results in a visible line in rendering)
                // empty color array results in an invisible line ("n" operator) but the rest is visible
                // empty content is like no caption

                boolean hasStroke = cs.setStrokingColorOnDemand(getColor());

                if (ab.dashArray != null)
                {
                    cs.setLineDashPattern(ab.dashArray, 0);
                }
                cs.setLineWidth(ab.width);

                float x1 = pathsArray[0] - rect.getLowerLeftX();
                float y1 = pathsArray[1] - rect.getLowerLeftY();
                float x2 = pathsArray[2] - rect.getLowerLeftX();
                float y2 = pathsArray[3] - rect.getLowerLeftY();

                String contents = annotation.getContents();
                if (contents == null)
                {
                    contents = "";
                }

                double angle = Math.atan2(y2 - y1, x2 - x1);
                cs.transform(Matrix.getRotateInstance(angle, x1, y1));
                float lineLength = (float) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
                if (annotation.getCaption() && !contents.isEmpty())
                {
                    PDType1Font font = PDType1Font.HELVETICA;
                    // TODO: support newlines!!!!!
                    // see https://www.pdfill.com/example/pdf_commenting_new.pdf
                    float contentLength = 0;
                    try
                    {
                        contentLength = font.getStringWidth(annotation.getContents()) / 1000 * 12;
                    }
                    catch (IllegalArgumentException ex)
                    {
                        //TODO test with "illegal" char to see what Adobe does
                    }
                    float xOffset = (lineLength - contentLength) / 2 - 1;
                    float yOffset;

                    String captionPositioning = annotation.getCaptionPositioning();
                    // draw the line horizontally, using the rotation CTM to get to correct final position
                    // that's the easiest way to calculate the positions for the line before and after inline caption
                    cs.moveTo(0, 0);
                    if ("Top".equals(captionPositioning))
                    {
                        // Add 1/2 of size
                        yOffset = 6;
                    }
                    else
                    {
                        // Inline
                        yOffset = -3;

                        // chitgoks: 
                        // "for the 1st half of the line i set is to xOffset - 2. that looks evened out"
                        cs.lineTo(0 + xOffset - 2, 0);
                        cs.moveTo(lineLength - xOffset, 0);
                    }
                    cs.lineTo(lineLength, 0);
                    cs.drawShape(ab.width, hasStroke, false);

                    if (contentLength > 0)
                    {
                        // don't show if there was trouble before
                        cs.beginText();
                        //TODO reduce font? How to decide the size?
                        cs.setFont(font, 12);
                        cs.setTextMatrix(Matrix.getTranslateInstance(xOffset, yOffset));
                        cs.showText(annotation.getContents());
                        cs.endText();
                    }
                }
                else
                {
                    cs.moveTo(0, 0);
                    cs.lineTo(lineLength, 0);
                    cs.drawShape(ab.width, hasStroke, false);
                }

                // there can be many, many more styles...
                //TODO numbers for arrow size are arbitrary and likely wrong
                if (PDAnnotationLine.LE_OPEN_ARROW.equals(annotation.getStartPointEndingStyle()))
                {
                    cs.moveTo(6, 3);
                    cs.lineTo(0, 0);
                    cs.lineTo(6, -3);
                    cs.stroke();
                }
                if (PDAnnotationLine.LE_OPEN_ARROW.equals(annotation.getEndPointEndingStyle()))
                {
                    cs.moveTo(lineLength - 6, 3);
                    cs.lineTo(lineLength, 0);
                    cs.lineTo(lineLength - 6, -3);
                    cs.stroke();
                }
            }
        }
        catch (IOException ex)
        {
            LOG.error(ex);
        }
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
