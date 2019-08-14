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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderEffectDictionary;
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

    public PDPolygonAppearanceHandler(PDAnnotation annotation, PDDocument document)
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
        float lineWidth = getLineWidth();
        PDRectangle rect = annotation.getRectangle();

        // Adjust rectangle even if not empty
        // CTAN-example-Annotations.pdf p2
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        float[][] pathArray = getPathArray(annotation);
        if (pathArray == null)
        {
            return;
        }
        for (int i = 0; i < pathArray.length; ++i)
        {
            for (int j = 0; j < pathArray[i].length / 2; ++j)
            {
                float x = pathArray[i][j * 2];
                float y = pathArray[i][j * 2 + 1];
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        rect.setLowerLeftX(Math.min(minX - lineWidth, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - lineWidth, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + lineWidth, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + lineWidth, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        PDAppearanceContentStream contentStream = null;

        try
        {
            contentStream = getNormalAppearanceAsContentStream();

            boolean hasStroke = contentStream.setStrokingColorOnDemand(getColor());

            boolean hasBackground = contentStream
                    .setNonStrokingColorOnDemand(annotation.getInteriorColor());

            setOpacity(contentStream, annotation.getConstantOpacity());

            contentStream.setBorderLine(lineWidth, annotation.getBorderStyle(), annotation.getBorder());

            PDBorderEffectDictionary borderEffect = annotation.getBorderEffect();
            if (borderEffect != null && borderEffect.getStyle().equals(PDBorderEffectDictionary.STYLE_CLOUDY))
            {
                CloudyBorder cloudyBorder = new CloudyBorder(contentStream,
                    borderEffect.getIntensity(), lineWidth, getRectangle());
                cloudyBorder.createCloudyPolygon(pathArray);
                annotation.setRectangle(cloudyBorder.getRectangle());
                PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                appearanceStream.setBBox(cloudyBorder.getBBox());
                appearanceStream.setMatrix(cloudyBorder.getMatrix());
            }
            else
            {
                // Acrobat applies a padding to each side of the bbox so the line is
                // completely within the bbox.

                for (int i = 0; i < pathArray.length; i++)
                {
                    float[] pointsArray = pathArray[i];
                    // first array shall be of size 2 and specify the moveto operator
                    if (i == 0 && pointsArray.length == 2)
                    {
                        contentStream.moveTo(pointsArray[0], pointsArray[1]);
                    }
                    else
                    {
                        // entries of length 2 shall be treated as lineto operator
                        if (pointsArray.length == 2)
                        {
                            contentStream.lineTo(pointsArray[0], pointsArray[1]);
                        }
                        else if (pointsArray.length == 6)
                        {
                            contentStream.curveTo(pointsArray[0], pointsArray[1],
                                    pointsArray[2], pointsArray[3],
                                    pointsArray[4], pointsArray[5]);
                        }
                    }
                }
                contentStream.closePath();
            }
            contentStream.drawShape(lineWidth, hasStroke, hasBackground);
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
        finally
        {
            IOUtils.closeQuietly(contentStream);
        }
    }

    private float[][] getPathArray(PDAnnotationMarkup annotation)
    {
        // PDF 2.0: Path takes priority over Vertices
        float[][] pathArray = annotation.getPath();
        if (pathArray == null)
        {
            // convert PDF 1.* array to PDF 2.0 array
            float[] verticesArray = annotation.getVertices();
            if (verticesArray == null)
            {
                return null;
            }
            int points = verticesArray.length / 2;
            pathArray = new float[points][2];
            for (int i = 0; i < points; ++i)
            {
                pathArray[i][0] = verticesArray[i * 2];
                pathArray[i][1] = verticesArray[i * 2 + 1];
            }
        }
        return pathArray;
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
