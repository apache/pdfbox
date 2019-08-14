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
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Handler to generate the link annotations appearance.
 *
 */
public class PDLinkAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDLinkAppearanceHandler.class);
    
    public PDLinkAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDLinkAppearanceHandler(PDAnnotation annotation, PDDocument document)
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
        PDAnnotationLink annotation = (PDAnnotationLink) getAnnotation();
        if (annotation.getRectangle() == null)
        {
            // 660402-p1-AnnotationEmptyRect.pdf has /Rect entry with 0 elements
            return;
        }

        // Adobe doesn't generate an appearance for a link annotation
        float lineWidth = getLineWidth();

        PDAppearanceContentStream contentStream  = null;

        try
        {
            contentStream = getNormalAppearanceAsContentStream();

            PDColor color = annotation.getColor();
            if (color == null)
            {
                // spec is unclear, but black is what Adobe does
                color = new PDColor(new float[] { 0 }, PDDeviceGray.INSTANCE);
            }
            boolean hasStroke = contentStream.setStrokingColorOnDemand(color);

            contentStream.setBorderLine(lineWidth, annotation.getBorderStyle(), annotation.getBorder());

            // Acrobat applies a padding to each side of the bbox so the line is completely within
            // the bbox.
            PDRectangle borderEdge = getPaddedRectangle(getRectangle(),lineWidth/2);

            float[] pathsArray = annotation.getQuadPoints();

            if (pathsArray != null)
            {
                // QuadPoints shall be ignored if any coordinate in the array lies outside
                // the region specified by Rect.
                PDRectangle rect = annotation.getRectangle();
                for (int i = 0; i < pathsArray.length / 2; ++i)
                {
                    if (!rect.contains(pathsArray[i * 2], pathsArray[i * 2 + 1]))
                    {
                        LOG.warn("At least one /QuadPoints entry (" + 
                                pathsArray[i * 2] + ";" + pathsArray[i * 2 + 1] + 
                                ") is outside of rectangle, " + rect + 
                                ", /QuadPoints are ignored and /Rect is used instead");
                        pathsArray = null;
                        break;
                    }
                }
            }

            if (pathsArray == null)
            {
                // Convert rectangle coordinates as if it was a /QuadPoints entry
                pathsArray = new float[8];
                pathsArray[0] = borderEdge.getLowerLeftX();
                pathsArray[1] = borderEdge.getLowerLeftY();
                pathsArray[2] = borderEdge.getUpperRightX();
                pathsArray[3] = borderEdge.getLowerLeftY();
                pathsArray[4] = borderEdge.getUpperRightX();
                pathsArray[5] = borderEdge.getUpperRightY();
                pathsArray[6] = borderEdge.getLowerLeftX();
                pathsArray[7] = borderEdge.getUpperRightY();
            }

            int of = 0;
            while (of + 7 < pathsArray.length)
            {
                if (annotation.getBorderStyle() != null &&
                    annotation.getBorderStyle().getStyle().equals(PDBorderStyleDictionary.STYLE_UNDERLINE))
                {
                    contentStream.moveTo(pathsArray[of], pathsArray[of + 1]);
                    contentStream.lineTo(pathsArray[of + 2], pathsArray[of + 3]);
                }
                else
                {
                    contentStream.moveTo(pathsArray[of], pathsArray[of + 1]);
                    contentStream.lineTo(pathsArray[of + 2], pathsArray[of + 3]);
                    contentStream.lineTo(pathsArray[of + 4], pathsArray[of + 5]);
                    contentStream.lineTo(pathsArray[of + 6], pathsArray[of + 7]);
                    contentStream.closePath();
                }
                of += 8;
            }

            contentStream.drawShape(lineWidth, hasStroke, false);
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
        finally{
            IOUtils.closeQuietly(contentStream);
        }
    }

    @Override
    public void generateRolloverAppearance()
    {
        // No rollover appearance generated for a link annotation
    }

    @Override
    public void generateDownAppearance()
    {
     // No down appearance generated for a link annotation
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
