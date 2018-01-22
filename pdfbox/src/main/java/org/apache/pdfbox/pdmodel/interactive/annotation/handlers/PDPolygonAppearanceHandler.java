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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPolygon;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Handler to generate the polygon annotations appearance.
 *
 */
public class PDPolygonAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDPolygonAppearanceHandler.class);

    public PDPolygonAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationPolygon annotation = (PDAnnotationPolygon) getAnnotation();
        float lineWidth = getLineWidth();
        PDRectangle rect = annotation.getRectangle();

        // Adjust rectangle even if not empty
        // CTAN-example-Annotations.pdf p2
        //TODO in a class structure this should be overridable
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float[] pathsArray = annotation.getVertices();
        if (pathsArray != null)
        {
            //TODO this adjustment is only for PDF 1.*. 
            //     Similar code should be done for PDF 2.0 (see "Path")
            for (int i = 0; i < pathsArray.length / 2; ++i)
            {
                float x = pathsArray[i * 2];
                float y = pathsArray[i * 2 + 1];
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
            rect.setLowerLeftX(Math.min(minX - lineWidth / 2, rect.getLowerLeftX()));
            rect.setLowerLeftY(Math.min(minY - lineWidth / 2, rect.getLowerLeftY()));
            rect.setUpperRightX(Math.max(maxX + lineWidth, rect.getUpperRightX()));
            rect.setUpperRightY(Math.max(maxY + lineWidth, rect.getUpperRightY()));
            annotation.setRectangle(rect);
        }

        try
        {
            try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
            {
                boolean hasStroke = contentStream.setStrokingColorOnDemand(getColor());

                boolean hasBackground = contentStream
                        .setNonStrokingColorOnDemand(annotation.getInteriorColor());

                handleOpacity(annotation.getConstantOpacity());

                contentStream.setBorderLine(lineWidth, annotation.getBorderStyle());
                
                // the differences rectangle
                // TODO: this only works for border effect solid. Cloudy needs a
                // different approach.
                setRectDifference(lineWidth);
                
                // Acrobat applies a padding to each side of the bbox so the line is
                // completely within
                // the bbox.
                
                // PDF 2.0: Path takes priority over Vertices
                COSBase path = annotation.getCOSObject().getDictionaryObject(COSName.getPDFName("Path"));
                if (path instanceof COSArray)
                {
                    COSArray pathArray = (COSArray) path;
                    for (int i = 0; i < pathArray.size(); i++)
                    {
                        COSBase points = pathArray.get(i);
                        if (points instanceof COSArray)
                        {
                            float[] pointsArray = ((COSArray) points).toFloatArray();
                            // first array shall be of size 2 and specify the moveto
                            // operator
                            if (i == 0 && pointsArray.length == 2)
                            {
                                contentStream.moveTo(pointsArray[0], pointsArray[1]);
                            }
                            else
                            {
                                // entries of length 2 shall be treated as lineto
                                // operator
                                if (pointsArray.length == 2)
                                {
                                    contentStream.lineTo(pointsArray[0], pointsArray[1]);
                                }
                                else if (pointsArray.length == 6)
                                {
                                    contentStream.curveTo(pointsArray[0], pointsArray[1], pointsArray[2], pointsArray[3],
                                            pointsArray[4], pointsArray[5]);
                                }
                            }
                        }
                    }
                }
                else
                {
                    float[] verticesArray = annotation.getVertices();
                    if (verticesArray == null)
                    {
                        return;
                    }

                    int nPoints = verticesArray.length / 2;
                    for (int i = 0; i < nPoints; i++)
                    {
                        float x = verticesArray[i * 2];
                        float y = verticesArray[i * 2 + 1];
                        if (i == 0)
                        {
                            contentStream.moveTo(x, y);
                        }
                        else
                        {
                            contentStream.lineTo(x, y);
                        }
                    }
                }
                contentStream.drawShape(lineWidth, hasStroke, hasBackground);
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    @Override
    public void generateRolloverAppearance()
    {
        // No rollover appearance generated for a polygon annotation
    }

    @Override
    public void generateDownAppearance()
    {
        // No down appearance generated for a polygon annotation
    }

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
        PDAnnotationPolygon annotation = (PDAnnotationPolygon) getAnnotation();

        PDBorderStyleDictionary bs = annotation.getBorderStyle();

        if (bs != null)
        {
            return bs.getWidth();
        }
        else
        {
            COSArray borderCharacteristics = annotation.getBorder();
            if (borderCharacteristics.size() >= 3)
            {
                COSBase base = borderCharacteristics.getObject(2);
                if (base instanceof COSNumber)
                {
                    return ((COSNumber) base).floatValue();
                }
            }
        }

        return 1;
    }
}
