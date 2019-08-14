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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderEffectDictionary;

/**
 * Handler to generate the circle annotations appearance.
 *
 */
public class PDCircleAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDCircleAppearanceHandler.class);
    
    public PDCircleAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDCircleAppearanceHandler(PDAnnotation annotation, PDDocument document)
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
        float lineWidth = getLineWidth();
        PDAnnotationSquareCircle annotation = (PDAnnotationSquareCircle) getAnnotation();
        PDAppearanceContentStream contentStream  = null;

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
                cloudyBorder.createCloudyEllipse(annotation.getRectDifference());
                annotation.setRectangle(cloudyBorder.getRectangle());
                annotation.setRectDifference(cloudyBorder.getRectDifference());
                PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                appearanceStream.setBBox(cloudyBorder.getBBox());
                appearanceStream.setMatrix(cloudyBorder.getMatrix());
            }
            else
            {
                // Acrobat applies a padding to each side of the bbox so the line is completely within
                // the bbox.

                PDRectangle borderBox = handleBorderBox(annotation, lineWidth);

                // lower left corner
                float x0 = borderBox.getLowerLeftX();
                float y0 = borderBox.getLowerLeftY();
                // upper right corner
                float x1 = borderBox.getUpperRightX();
                float y1 = borderBox.getUpperRightY();
                // mid points
                float xm = x0 + borderBox.getWidth() / 2;
                float ym = y0 + borderBox.getHeight() / 2;
                // see http://spencermortensen.com/articles/bezier-circle/
                // the below number was calculated from sampling content streams
                // generated using Adobe Reader
                float magic = 0.55555417f;
                // control point offsets
                float vOffset = borderBox.getHeight() / 2 * magic;
                float hOffset = borderBox.getWidth() / 2 * magic;

                contentStream.moveTo(xm, y1);
                contentStream.curveTo((xm + hOffset), y1, x1, (ym + vOffset), x1, ym);
                contentStream.curveTo(x1, (ym - vOffset), (xm + hOffset), y0, xm, y0);
                contentStream.curveTo((xm - hOffset), y0, x0, (ym - vOffset), x0, ym);
                contentStream.curveTo(x0, (ym + vOffset), (xm - hOffset), y1, xm, y1);
                contentStream.closePath();
            }

            contentStream.drawShape(lineWidth, hasStroke, hasBackground);
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
