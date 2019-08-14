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
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 */
public class PDUnderlineAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDUnderlineAppearanceHandler.class);

    public PDUnderlineAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDUnderlineAppearanceHandler(PDAnnotation annotation, PDDocument document)
    {
        super(annotation, document);
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
        PDAnnotationTextMarkup annotation = (PDAnnotationTextMarkup) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getQuadPoints();
        if (pathsArray == null)
        {
            return;
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        PDColor color = annotation.getColor();
        if (color == null || color.getComponents().length == 0)
        {
            return;
        }
        if (Float.compare(ab.width, 0) == 0)
        {
            // value found in adobe reader
            ab.width = 1.5f;
        }

        // Adjust rectangle even if not empty, see PLPDF.com-MarkupAnnotations.pdf
        //TODO in a class structure this should be overridable
        // this is similar to polyline but different data type
        // all coordinates (unlike painting) are used because I'm lazy
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
        rect.setLowerLeftX(Math.min(minX - ab.width / 2, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width / 2, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width / 2, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width / 2, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        PDAppearanceContentStream cs = null;

        try
        {
            cs = getNormalAppearanceAsContentStream();

            setOpacity(cs, annotation.getConstantOpacity());

            cs.setStrokingColor(color);
            if (ab.dashArray != null)
            {
                cs.setLineDashPattern(ab.dashArray, 0);
            }
            cs.setLineWidth(ab.width);

            // spec is incorrect
            // https://stackoverflow.com/questions/9855814/pdf-spec-vs-acrobat-creation-quadpoints
            for (int i = 0; i < pathsArray.length / 8; ++i)
            {
                // Adobe doesn't use the lower coordinate for the line, it uses lower + delta / 7.
                // do the math for diagonal annotations with this weird old trick:
                // https://stackoverflow.com/questions/7740507/extend-a-line-segment-a-specific-distance
                float len0 = (float) (Math.sqrt(Math.pow(pathsArray[i * 8] - pathsArray[i * 8 + 4], 2) + 
                                      Math.pow(pathsArray[i * 8 + 1] - pathsArray[i * 8 + 5], 2)));
                float x0 = pathsArray[i * 8 + 4];
                float y0 = pathsArray[i * 8 + 5];
                if (Float.compare(len0, 0) != 0)
                {
                    // only if both coordinates are not identical to avoid divide by zero
                    x0 += (pathsArray[i * 8] - pathsArray[i * 8 + 4]) / len0 * len0 / 7;
                    y0 += (pathsArray[i * 8 + 1] - pathsArray[i * 8 + 5]) / len0 * (len0 / 7);
                }
                float len1 = (float) (Math.sqrt(Math.pow(pathsArray[i * 8 + 2] - pathsArray[i * 8 + 6], 2) + 
                                      Math.pow(pathsArray[i * 8 + 3] - pathsArray[i * 8 + 7], 2)));
                float x1 = pathsArray[i * 8 + 6];
                float y1 = pathsArray[i * 8 + 7];
                if (Float.compare(len1, 0) != 0)
                {
                    // only if both coordinates are not identical to avoid divide by zero
                    x1 += (pathsArray[i * 8 + 2] - pathsArray[i * 8 + 6]) / len1 * len1 / 7;
                    y1 += (pathsArray[i * 8 + 3] - pathsArray[i * 8 + 7]) / len1 * len1 / 7;
                }
                cs.moveTo(x0, y0);
                cs.lineTo(x1, y1);
            }
            cs.stroke();
        }
        catch (IOException ex)
        {
            LOG.error(ex);
        }
        finally
        {
            IOUtils.closeQuietly(cs);
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
