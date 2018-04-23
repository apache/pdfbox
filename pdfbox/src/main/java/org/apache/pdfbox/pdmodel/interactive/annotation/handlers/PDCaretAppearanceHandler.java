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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationCaret;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;

/**
 * Handler to generate the caret annotations appearance.
 *
 * @author Tilman Hausherr
 */
public class PDCaretAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDCaretAppearanceHandler.class);

    public PDCaretAppearanceHandler(PDAnnotation annotation)
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
        float lineWidth = 1f;
        //TODO Adobe creates the /RD entry, but it is unclear how it
        // gets the (identical) numbers. The numbers from there are then substracted/added from /BBox
        // and used in the translation in the matrix and also for the line width.

        try
        {
            PDAnnotationCaret annotation = (PDAnnotationCaret) getAnnotation();
            try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
            {
                contentStream.setStrokingColor(getColor());
                contentStream.setLineWidth(lineWidth);
                contentStream.setNonStrokingColor(getColor());
                
                handleOpacity(annotation.getConstantOpacity());

                PDRectangle rect = getRectangle();
                PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
                annotation.getNormalAppearanceStream().setBBox(bbox);

                float halfX = rect.getWidth() / 2;
                float halfY = rect.getHeight() / 2;
                contentStream.moveTo(0, 0);
                contentStream.curveTo(halfX, 0,
                                      halfX, halfY, 
                                      halfX, rect.getHeight());
                contentStream.curveTo(halfX, halfY, 
                                      halfX, 0,
                                      rect.getWidth(), 0);
                // closeAndFillAndStroke() would bring a thicker "thin top" shape.
                contentStream.closePath();
                contentStream.fill();
                contentStream.stroke();
                //TODO test whether the stroke() and setLineWidth() calls have any effect at all.
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
        // TODO to be implemented
    }

    @Override
    public void generateDownAppearance()
    {
        // TODO to be implemented
    }
}