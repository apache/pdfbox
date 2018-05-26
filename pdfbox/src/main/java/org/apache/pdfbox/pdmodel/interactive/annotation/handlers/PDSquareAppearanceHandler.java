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

import java.awt.geom.AffineTransform;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquare;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderEffectDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Handler to generate the square annotations appearance.
 *
 */
public class PDSquareAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDSquareAppearanceHandler.class);
        
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
        PDAnnotationSquare annotation = (PDAnnotationSquare) getAnnotation();
        try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
        {
            boolean hasStroke = contentStream.setStrokingColorOnDemand(getColor());
            boolean hasBackground = contentStream
                    .setNonStrokingColorOnDemand(annotation.getInteriorColor());

            setOpacity(contentStream, annotation.getConstantOpacity());

            contentStream.setBorderLine(lineWidth, annotation.getBorderStyle());                
            PDBorderEffectDictionary borderEffect = annotation.getBorderEffect();

            if (borderEffect != null && borderEffect.getStyle().equals(PDBorderEffectDictionary.STYLE_CLOUDY))
            {
                CloudyBorder cloudyBorder = new CloudyBorder(contentStream,
                    borderEffect.getIntensity(), lineWidth, getRectangle());
                cloudyBorder.createCloudyRectangle(annotation.getRectDifference());
                annotation.setRectangle(cloudyBorder.getRectangle());
                annotation.setRectDifference(cloudyBorder.getRectDifference());
                PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                appearanceStream.setBBox(cloudyBorder.getBBox());
                appearanceStream.setMatrix(cloudyBorder.getMatrix());
            }
            else
            {
                // handle the border box
                //
                // There are two options. The handling is not part of the PDF specification but
                // implementation specific to Adobe Reader
                // - if /RD is set the border box is the /Rect entry inset by the respective
                //   border difference.
                // - if /RD is not set the border box is defined by the /Rect entry. The /RD entry will
                //   be set to be the line width and the /Rect is enlarged by the /RD amount

                PDRectangle borderBox;
                float[] rectDifferences = annotation.getRectDifferences();

                if (rectDifferences.length == 0)
                {
                    borderBox = getPaddedRectangle(getRectangle(), lineWidth/2);
                    // the differences rectangle
                    annotation.setRectDifferences(lineWidth/2);
                    annotation.setRectangle(addRectDifferences(getRectangle(), annotation.getRectDifferences()));

                    // when the normal appearance stream was generated BBox and Matrix have been set to the
                    // values of the original /Rect. As the /Rect was changed that needs to be adjusted too.
                    annotation.getNormalAppearanceStream().setBBox(getRectangle());
                    AffineTransform transform = AffineTransform.getTranslateInstance(-getRectangle().getLowerLeftX(),
                            -getRectangle().getLowerLeftY());
                    annotation.getNormalAppearanceStream().setMatrix(transform);
                }
                else
                {
                    borderBox = applyRectDifferences(getRectangle(), rectDifferences);
                    borderBox = getPaddedRectangle(borderBox, lineWidth/2);
                }

                contentStream.addRect(borderBox.getLowerLeftX(), borderBox.getLowerLeftY(),
                        borderBox.getWidth(), borderBox.getHeight());
            }

            contentStream.drawShape(lineWidth, hasStroke, hasBackground);
        }
        catch (IOException e)
        {
            LOG.error(e);
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
        PDAnnotationSquare annotation = (PDAnnotationSquare) getAnnotation();

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
