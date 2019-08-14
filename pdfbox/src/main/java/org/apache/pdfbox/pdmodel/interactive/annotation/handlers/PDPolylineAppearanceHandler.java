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

package org.apache.pdfbox.pdmodel.interactive.annotation.handlers;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine.LE_NONE;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.util.Matrix;

/**
 * Handler to generate the polyline annotations appearance.
 *
 */
public class PDPolylineAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDPolylineAppearanceHandler.class);

    public PDPolylineAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDPolylineAppearanceHandler(PDAnnotation annotation, PDDocument document)
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
        PDAnnotationMarkup annotation = (PDAnnotationMarkup) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getVertices();
        if (pathsArray == null || pathsArray.length < 4)
        {
            return;
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        PDColor color = annotation.getColor();
        if (color == null || color.getComponents().length == 0 || Float.compare(ab.width, 0) == 0)
        {
            return;
        }

        // Adjust rectangle even if not empty
        // CTAN-example-Annotations.pdf and pdf_commenting_new.pdf p11
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
        // arrow length is 9 * width at about 30° => 10 * width seems to be enough
        rect.setLowerLeftX(Math.min(minX - ab.width * 10, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width * 10, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width * 10, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width * 10, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        PDAppearanceContentStream cs = null;

        try
        {
            cs = getNormalAppearanceAsContentStream();

            boolean hasBackground = cs.setNonStrokingColorOnDemand(annotation.getInteriorColor());
            setOpacity(cs, annotation.getConstantOpacity());
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
                    if (SHORT_STYLES.contains(annotation.getStartPointEndingStyle()))
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
                    if (i == pathsArray.length / 2 - 1 &&
                        SHORT_STYLES.contains(annotation.getEndPointEndingStyle()))
                    {
                        // modify coordinate to shorten the segment
                        // https://stackoverflow.com/questions/7740507/extend-a-line-segment-a-specific-distance
                        float x0 = pathsArray[pathsArray.length - 4];
                        float y0 = pathsArray[pathsArray.length - 3];
                        float len = (float) (Math.sqrt(Math.pow(x0 - x, 2) + Math.pow(y0 - y, 2)));
                        if (Float.compare(len, 0) != 0)
                        {
                            x -= (x - x0) / len * ab.width;
                            y -= (y - y0) / len * ab.width;
                        }
                    }
                    cs.lineTo(x, y);
                }
            }
            cs.stroke();

            // do a transform so that first and last "arms" are imagined flat, like in line handler
            // the alternative would be to apply the transform to the LE shapes directly,
            // which would be more work and produce code difficult to understand

            // paint the styles here and after polyline draw, to avoid line crossing a filled shape
            if (!LE_NONE.equals(annotation.getStartPointEndingStyle()))
            {
                // check only needed to avoid q cm Q if LE_NONE
                float x2 = pathsArray[2];
                float y2 = pathsArray[3];
                float x1 = pathsArray[0];
                float y1 = pathsArray[1];
                cs.saveGraphicsState();
                if (ANGLED_STYLES.contains(annotation.getStartPointEndingStyle()))
                {
                    double angle = Math.atan2(y2 - y1, x2 - x1);
                    cs.transform(Matrix.getRotateInstance(angle, x1, y1));
                }
                else
                {
                    cs.transform(Matrix.getTranslateInstance(x1, y1));
                }
                drawStyle(annotation.getStartPointEndingStyle(), cs, 0, 0, ab.width, hasStroke, hasBackground, false);
                cs.restoreGraphicsState();
            }

            if (!LE_NONE.equals(annotation.getEndPointEndingStyle()))
            {
                // check only needed to avoid q cm Q if LE_NONE
                float x1 = pathsArray[pathsArray.length - 4];
                float y1 = pathsArray[pathsArray.length - 3];
                float x2 = pathsArray[pathsArray.length - 2];
                float y2 = pathsArray[pathsArray.length - 1];
                // save / restore not needed because it's the last one
                if (ANGLED_STYLES.contains(annotation.getEndPointEndingStyle()))
                {
                    double angle = Math.atan2(y2 - y1, x2 - x1);
                    cs.transform(Matrix.getRotateInstance(angle, x2, y2));
                }
                else
                {
                    cs.transform(Matrix.getTranslateInstance(x2, y2));
                }
                drawStyle(annotation.getEndPointEndingStyle(), cs, 0, 0, ab.width, hasStroke, hasBackground, true);
            }
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
        // No rollover appearance generated for a polyline annotation
    }

    @Override
    public void generateDownAppearance()
    {
        // No down appearance generated for a polyline annotation
    }

    //TODO DRY, this code is from polygonAppearanceHandler so it's double
    
    /**
     * Get the line with of the border.
     * 
     * Get the width of the line used to draw a border around the annotation.
     * This may either be specified by the annotation dictionaries Border
     * setting or by the W entry in the BS border style dictionary. If both are
     * missing the default width is 1.
     * 
     * @return the line width
     */
    // TODO: according to the PDF spec the use of the BS entry is annotation
    // specific
    // so we will leave that to be implemented by individual handlers.
    // If at the end all annotations support the BS entry this can be handled
    // here and removed from the individual handlers.
    float getLineWidth()
    {
        PDAnnotationMarkup annotation = (PDAnnotationMarkup) getAnnotation();

        PDBorderStyleDictionary bs = annotation.getBorderStyle();

        if (bs != null)
        {
            return bs.getWidth();
        }

        COSArray borderCharacteristics = annotation.getBorder();
        if (borderCharacteristics.size() >= 3)
        {
            COSBase base = borderCharacteristics.getObject(2);
            if (base instanceof COSNumber)
            {
                return ((COSNumber) base).floatValue();
            }
        }

        return 1;
    }
}
