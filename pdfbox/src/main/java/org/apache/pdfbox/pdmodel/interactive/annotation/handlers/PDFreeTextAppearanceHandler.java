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
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFreeText;
import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine.LE_NONE;
import static org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAbstractAppearanceHandler.SHORT_STYLES;
import org.apache.pdfbox.util.Matrix;

public class PDFreeTextAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDFreeTextAppearanceHandler.class);

    public PDFreeTextAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationFreeText annotation = (PDAnnotationFreeText) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = new float[0];
        if ("FreeTextCallout".equals(annotation.getIntent()))
        {
            pathsArray = annotation.getCallout();
            if (pathsArray == null || pathsArray.length != 4 && pathsArray.length != 6)
            {
                pathsArray = new float[0];
            }
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        PDColor color = annotation.getColor();
        if (color == null || color.getComponents().length == 0 || Float.compare(ab.width, 0) == 0)
        {
            return;
        }

        if (pathsArray.length > 0)
        {
            // Adjust rectangle even if not empty
            // CTAN-example-Annotations.pdf p1
            //TODO in a class structure this should be overridable
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
            // arrow length is 9 * width at about 30Â° => 10 * width seems to be enough
            rect.setLowerLeftX(Math.min(minX - ab.width * 10, rect.getLowerLeftX()));
            rect.setLowerLeftY(Math.min(minY - ab.width * 10, rect.getLowerLeftY()));
            rect.setUpperRightX(Math.max(maxX + ab.width * 10, rect.getUpperRightX()));
            rect.setUpperRightY(Math.max(maxY + ab.width * 10, rect.getUpperRightY()));
            annotation.setRectangle(rect);
        }

        try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream())
        {
            // The fill color is the /C entry, there is no /IC entry defined
            boolean hasBackground = cs.setNonStrokingColorOnDemand(annotation.getColor());
            setOpacity(cs, annotation.getConstantOpacity());

            // in reality, Adobe uses the non stroking color from /DA as stroking color. WTF?
            boolean hasStroke = cs.setStrokingColorOnDemand(color);
            if (ab.dashArray != null)
            {
                cs.setLineDashPattern(ab.dashArray, 0);
            }
            cs.setLineWidth(ab.width);

            for (int i = 0; i < pathsArray.length / 2; ++i)
            {
                float x = pathsArray[i * 2];
                float y = pathsArray[i * 2 + 1];
                if (i == 0)
                {
                    if (SHORT_STYLES.contains(annotation.getLineEndingStyle()))
                    {
                        // modify coordinate to shorten the segment
                        // https://stackoverflow.com/questions/7740507/extend-a-line-segment-a-specific-distance
                        float x1 = pathsArray[2];
                        float y1 = pathsArray[3];
                        float len = (float) (Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2)));
                        if (Float.compare(len, 0) != 0)
                        {
                            x += (x1 - x) / len * ab.width;
                            y += (y1 - y) / len * ab.width;
                        }
                    }
                    cs.moveTo(x, y);
                }
                else
                {
                    cs.lineTo(x, y);
                }
            }
            cs.stroke();

            // do a transform so that first "arm" is imagined flat, like in line handler
            // the alternative would be to apply the transform to the LE shapes directly,
            // which would be more work and produce code difficult to understand
            // paint the styles here and after line(s) draw, to avoid line crossing a filled shape
            if ("FreeTextCallout".equals(annotation.getIntent())
                    && !LE_NONE.equals(annotation.getLineEndingStyle())
                    && pathsArray.length >= 4)
            {
                // check only needed to avoid q cm Q if LE_NONE
                float x2 = pathsArray[2];
                float y2 = pathsArray[3];
                float x1 = pathsArray[0];
                float y1 = pathsArray[1];
                double angle = Math.atan2(y2 - y1, x2 - x1);
                cs.transform(Matrix.getRotateInstance(angle, x1, y1));
                drawStyle(annotation.getLineEndingStyle(), cs, 0, 0, ab.width, hasStroke, hasBackground);
            }
            
            //TODO display border and text
            // how to set the text color? Apparently red is the default.
        }
        catch (IOException ex)
        {
            LOG.error(ex);
        }
    }

    @Override
    public void generateRolloverAppearance()
    {
        // TODO to be implemented
    }

    @Override
    public void generateDownAppearance()
    {
        // TODO to be implemented
    }
}
