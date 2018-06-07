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
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 *
 * @author Tilman Hausherr
 */
public class PDTextAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDTextAppearanceHandler.class);

    public PDTextAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationText annotation = (PDAnnotationText) getAnnotation();
        if (!PDAnnotationText.NAME_NOTE.equals(annotation.getName()))
        {
            //TODO Comment, Key, Help, NewParagraph, Paragraph, Insert
            return;
        }

        try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
        {
            boolean hasBackground = contentStream.setNonStrokingColorOnDemand(getColor());
            setOpacity(contentStream, annotation.getConstantOpacity());
            
            //TODO find out what Adobe chooses if color is missing

            PDRectangle rect = getRectangle();
            PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
            PDRectangle bbox = rect.createRetranslatedRectangle();
            appearanceStream.setBBox(bbox);

            switch (annotation.getName())
            {
                case PDAnnotationText.NAME_NOTE:
                    drawNote(contentStream, bbox, hasBackground);
                    break;

                default:
                    break;
            }

        }
        catch (IOException e)
        {
            LOG.error(e);
        }

    }

    private void drawNote(final PDAppearanceContentStream contentStream, PDRectangle bbox, boolean hasBackground)
            throws IOException
    {
        contentStream.setLineJoinStyle(1); // round edge
        contentStream.addRect(1, 1, bbox.getWidth() - 2,  bbox.getHeight() - 2);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 2);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 2);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 3);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 3);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 4);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 4);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 5);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 5);
        contentStream.drawShape(1, true, hasBackground);
    }

    @Override
    public void generateRolloverAppearance()
    {
        // No rollover appearance generated
    }

    @Override
    public void generateDownAppearance()
    {
        // No down appearance generated
    }
}
