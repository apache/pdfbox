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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Handler to generate the polygon annotations appearance.
 *
 */
public class PDPolygonAppearanceHandler extends PDAbstractAppearanceHandler
{

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
        // Adobe doesn't generate an appearance for a link annotation
        float lineWidth = getLineWidth();
        try
        {
            PDAnnotation annotation = getAnnotation();
            PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream();
            contentStream.setStrokingColorOnDemand(getColor());

            // TODO: handle opacity settings

            contentStream.setBorderLine(lineWidth, ((PDAnnotationMarkup) annotation).getBorderStyle());

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
                            } else if (pointsArray.length == 6)
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
                COSBase vertices = annotation.getCOSObject().getDictionaryObject(COSName.VERTICES);
                if (!(vertices instanceof COSArray))
                {
                    return;
                }

                COSArray verticesArray = (COSArray) vertices;
                int nPoints = verticesArray.size() / 2;
                for (int i = 0; i < nPoints; i++)
                {
                    COSBase bx = verticesArray.getObject(i * 2);
                    COSBase by = verticesArray.getObject(i * 2 + 1);
                    if (bx instanceof COSNumber && by instanceof COSNumber)
                    {
                        float x = ((COSNumber) bx).floatValue();
                        float y = ((COSNumber) by).floatValue();
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
                contentStream.stroke();
            }

            contentStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
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
        PDAnnotationLink annotation = (PDAnnotationLink) getAnnotation();

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
                return borderCharacteristics.getInt(3);
            }
        }

        return 1;
    }
}
