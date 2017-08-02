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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Handler to generate the square annotations appearance.
 *
 */
public class PDSquareAppearanceHandler extends PDAbstractAppearanceHandler
{
    
    public PDSquareAppearanceHandler(PDAnnotation annotation)
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
        float lineWidth = getLineWidth();
        try
        {
            PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream();
            contentStream.setStrokingColorOnDemand(getColor());
            boolean hasBackground = contentStream
                    .setNonStrokingColorOnDemand(((PDAnnotationSquareCircle) getAnnotation()).getInteriorColor());

            handleOpacity(((PDAnnotationSquareCircle) getAnnotation()).getConstantOpacity());
            
            contentStream.setBorderLine(lineWidth, ((PDAnnotationSquareCircle) getAnnotation()).getBorderStyle());
            
            // the differences rectangle
            // TODO: this only works for border effect solid. Cloudy needs a different approach.
            setRectDifference(lineWidth);
            
            // Acrobat applies a padding to each side of the bbox so the line is completely within
            // the bbox.
            PDRectangle borderEdge = getPaddedRectangle(getRectangle(),lineWidth/2);
            contentStream.addRect(borderEdge.getLowerLeftX() , borderEdge.getLowerLeftY(),
                    borderEdge.getWidth(), borderEdge.getHeight());
            
            contentStream.drawShape(lineWidth, hasBackground);
            
            contentStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
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
        PDAnnotationSquareCircle annotation = (PDAnnotationSquareCircle) getAnnotation();

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
