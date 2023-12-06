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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;

/**
 *
 * @author Tilman Hausherr
 */
public class PDFileAttachmentAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Logger LOG = LogManager.getLogger(PDFileAttachmentAppearanceHandler.class);

    public PDFileAttachmentAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDFileAttachmentAppearanceHandler(PDAnnotation annotation, PDDocument document)
    {
        super(annotation, document);
    }

    @Override
    public void generateNormalAppearance()
    {
        PDAnnotationFileAttachment annotation = (PDAnnotationFileAttachment) getAnnotation();
        PDRectangle rect = getRectangle();
        if (rect == null)
        {
            return;
        }
        try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
        {
            setOpacity(contentStream, annotation.getConstantOpacity());

            // minimum code of PDTextAppearanceHandler.adjustRectAndBBox() 
            int size = 18;
            rect.setUpperRightX(rect.getLowerLeftX() + size);
            rect.setLowerLeftY(rect.getUpperRightY() - size);
            annotation.setRectangle(rect);
            annotation.getNormalAppearanceStream().setBBox(new PDRectangle(size, size));

            //TODO support Graph, PushPin, Paperclip, Tag
            drawPaperclip(contentStream);
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    /**
     * Draw a paperclip. Shape is from
     * <a href="https://raw.githubusercontent.com/Iconscout/unicons/master/svg/line/paperclip.svg">Iconscout</a>
     * (Apache licensed).
     *
     * @param contentStream
     * @throws IOException
     */
    private void drawPaperclip(final PDAppearanceContentStream contentStream) throws IOException
    {
        contentStream.moveTo(13.574f, 9.301f);
        contentStream.lineTo(8.926f, 13.949f);
        contentStream.curveTo(7.648f, 15.227f, 5.625f, 15.227f, 4.426f, 13.949f);
        contentStream.curveTo(3.148f, 12.676f, 3.148f, 10.648f, 4.426f, 9.449f);
        contentStream.lineTo(10.426f, 3.449f);
        contentStream.curveTo(11.176f, 2.773f, 12.301f, 2.773f, 13.051f, 3.449f);
        contentStream.curveTo(13.801f, 4.199f, 13.801f, 5.398f, 13.051f, 6.074f);
        contentStream.lineTo(7.875f, 11.25f);
        contentStream.curveTo(7.648f, 11.477f, 7.273f, 11.477f, 7.051f, 11.25f);
        contentStream.curveTo(6.824f, 11.023f, 6.824f, 10.648f, 7.051f, 10.426f);
        contentStream.lineTo(10.875f, 6.602f);
        contentStream.curveTo(11.176f, 6.301f, 11.176f, 5.852f, 10.875f, 5.551f);
        contentStream.curveTo(10.574f, 5.25f, 10.125f, 5.25f, 9.824f, 5.551f);
        contentStream.lineTo(6f, 9.449f);
        contentStream.curveTo(5.176f, 10.273f, 5.176f, 11.551f, 6f, 12.375f);
        contentStream.curveTo(6.824f, 13.125f, 8.102f, 13.125f, 8.926f, 12.375f);
        contentStream.lineTo(14.102f, 7.199f);
        contentStream.curveTo(15.449f, 5.852f, 15.449f, 3.75f, 14.102f, 2.398f);
        contentStream.curveTo(12.75f, 1.051f, 10.648f, 1.051f, 9.301f, 2.398f);
        contentStream.lineTo(3.301f, 8.398f);
        contentStream.curveTo(2.398f, 9.301f, 1.949f, 10.5f, 1.949f, 11.699f);
        contentStream.curveTo(1.949f, 14.324f, 4.051f, 16.352f, 6.676f, 16.352f);
        contentStream.curveTo(7.949f, 16.352f, 9.074f, 15.824f, 9.977f, 15f);
        contentStream.lineTo(14.625f, 10.352f);
        contentStream.curveTo(14.926f, 10.051f, 14.926f, 9.602f, 14.625f, 9.301f);
        contentStream.curveTo(14.324f, 9f, 13.875f, 9f, 13.574f, 9.301f);
        contentStream.closePath();
        contentStream.fill();
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
