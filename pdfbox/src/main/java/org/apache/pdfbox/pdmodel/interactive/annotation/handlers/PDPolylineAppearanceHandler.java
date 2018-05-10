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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPolyline;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

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
        PDAnnotationPolyline annotation = (PDAnnotationPolyline) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getVertices();
        if (pathsArray == null)
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
        rect.setLowerLeftX(Math.min(minX - ab.width / 2, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width / 2, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        try
        {
            try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream())
            {
                setOpacity(cs, annotation.getConstantOpacity());

                cs.setStrokingColor(color);
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
                        cs.moveTo(x, y);
                    }
                    else
                    {
                        cs.lineTo(x, y);
                    }
                }
                cs.stroke();
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
        PDAnnotationPolyline annotation = (PDAnnotationPolyline) getAnnotation();

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
