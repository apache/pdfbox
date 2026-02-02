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
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.util.Matrix;

/**
 *
 * @author Tilman Hausherr
 */
public class PDFileAttachmentAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDFileAttachmentAppearanceHandler.class);

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

            // test case: pdf_commenting_new.pdf page 7
            String attachmentName = annotation.getAttachmentName();
            switch (attachmentName)
            {
                case "Paperclip":
                    drawPaperclip(contentStream);
                    break;
                case "Graph":
                    drawGraph(contentStream);
                    break;
                case "Tag":
                    drawTag(contentStream);
                    break;
                default:
                    drawPushPin(contentStream);
                    break;
            }
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

    private void drawPushPin(final PDAppearanceContentStream contentStream) throws IOException
    {
        // ty 18 is from the caller, scale 0.022 is by trial and error
        contentStream.transform(new Matrix(0.022f, 0, 0, -0.022f, 0f, 18f));

        // Source: https://www.svgrepo.com/svg/269187/push-pin
        // License: CC0
        contentStream.transform(Matrix.getTranslateInstance(586.47f, 178.97f));
        contentStream.moveTo(0, 0);
        contentStream.curveTo(13f, 0f, 23.43f, -10.58f, 23.43f, -23.57f);
        contentStream.lineTo(23.43f, -70.53f);
        contentStream.curveTo(23.43f, -109.32f, -8.19f, -141.06f, -47.03f, -141.06f);
        contentStream.lineTo(-329.17f, -141.06f);
        contentStream.curveTo(-368.17f, -141.06f, -399.79f, -109.32f, -399.79f, -70.53f);
        contentStream.lineTo(-399.79f, -23.57f);
        contentStream.curveTo(-399.79f, -10.58f, -389.19f, 0.0f, -376.19f, 0f);
        contentStream.lineTo(-305.74f, 0f);
        contentStream.lineTo(-305.74f, 129.52f);
        contentStream.curveTo(-364.0f, 168.47f, -399.79f, 234.67f, -399.79f, 305.36f);
        contentStream.curveTo(-399.79f, 318.34f, -389.19f, 328.76f, -376.19f, 328.76f);
        contentStream.lineTo(-211.69f, 328.76f);
        contentStream.lineTo(-211.69f, 555.9f);
        contentStream.curveTo(-211.69f, 568.88f, -201.1f, 579.3f, -188.1f, 579.3f);
        contentStream.curveTo(-175.1f, 579.3f, -164.67f, 568.88f, -164.67f, 555.9f);
        contentStream.lineTo(-164.67f, 328.76f);
        contentStream.lineTo(0f, 328.76f);
        contentStream.curveTo(13.0f, 328.76f, 23.43f, 318.34f, 23.43f, 305.36f);
        contentStream.curveTo(23.43f, 234.67f, -12.2f, 168.47f, -70.62f, 129.52f);
        contentStream.lineTo(-70.62f, 0f);
        contentStream.lineTo(0f, 0f);
        contentStream.closePath();
        contentStream.moveTo(-25.2f, 281.79f);
        contentStream.lineTo(-351.0f, 281.79f);
        contentStream.curveTo(-343.77f, 232.42f, -314.24f, 188.18f, -270.43f, 162.86f);
        contentStream.curveTo(-263.21f, 158.69f, -258.71f, 150.99f, -258.71f, 142.5f);
        contentStream.lineTo(-258.71f, 0f);
        contentStream.lineTo(-117.64f, 0f);
        contentStream.lineTo(-117.64f, 142.5f);
        contentStream.curveTo(-117.64f, 150.99f, -113.15f, 158.69f, -105.77f, 162.86f);
        contentStream.curveTo(-61.95f, 188.18f, -32.42f, 232.42f, -25.2f, 281.79f);
        contentStream.closePath();
        contentStream.moveTo(-352.76f, -46.97f);
        contentStream.lineTo(-352.76f, -70.53f);
        contentStream.curveTo(-352.76f, -83.52f, -342.17f, -93.93f, -329.17f, -93.93f);
        contentStream.lineTo(-47.03f, -93.93f);
        contentStream.curveTo(-34.03f, -93.93f, -23.59f, -83.52f, -23.59f, -70.53f);
        contentStream.lineTo(-23.59f, -46.97f);
        contentStream.lineTo(-352.76f, -46.97f);
        contentStream.lineTo(-352.76f, -46.97f);
        contentStream.closePath();
        contentStream.fill();
    }

    private void drawGraph(final PDAppearanceContentStream contentStream) throws IOException
    {
        // ty 18 is from the caller, scale 0.022 is by trial and error
        contentStream.transform(new Matrix(0.022f, 0, 0, -0.022f, 0f, 18f));

        // Source: https://www.svgrepo.com/svg/339018/chart-histogram
        // Author: Carbon Design https://github.com/carbon-design-system/carbon
        // License: Apache
        contentStream.transform(Matrix.getTranslateInstance(736.04f, 907.89f));
        contentStream.moveTo(0f, 0f);
        contentStream.lineTo(-675.23f, 0f);
        contentStream.curveTo(-679.72f, 0f, -683.41f, -3.53f, -683.41f, -8.01f);
        contentStream.lineTo(-683.41f, -683.37f);
        contentStream.lineTo(-667.22f, -683.37f);
        contentStream.lineTo(-667.22f, -353.95f);
        contentStream.curveTo(-583.85f, -357.8f, -541.53f, -419.99f, -500.49f, -480.27f);
        contentStream.curveTo(-459.93f, -539.74f, -418.09f, -601.46f, -337.61f, -601.46f);
        contentStream.curveTo(-257.14f, -601.46f, -215.3f, -539.74f, -174.74f, -480.27f);
        contentStream.curveTo(-132.58f, -418.07f, -88.81f, -353.79f, 0f, -353.79f);
        contentStream.lineTo(0f, -337.6f);
        contentStream.curveTo(-97.31f, -337.6f, -143.48f, -405.41f, -188.2f, -471.13f);
        contentStream.curveTo(-228.12f, -529.8f, -265.8f, -585.27f, -337.61f, -585.27f);
        contentStream.curveTo(-409.43f, -585.27f, -447.11f, -529.8f, -487.03f, -471.13f);
        contentStream.curveTo(-530.47f, -407.33f, -575.36f, -341.45f, -667.22f, -337.76f);
        contentStream.lineTo(-667.22f, -16.19f);
        contentStream.lineTo(-615.76f, -16.19f);
        contentStream.lineTo(-615.76f, -255.68f);
        contentStream.curveTo(-615.76f, -260.17f, -612.23f, -263.7f, -607.74f, -263.7f);
        contentStream.lineTo(-525.82f, -263.7f);
        contentStream.lineTo(-525.82f, -345.77f);
        contentStream.curveTo(-525.82f, -350.26f, -522.13f, -353.79f, -517.64f, -353.79f);
        contentStream.lineTo(-435.73f, -353.79f);
        contentStream.lineTo(-435.73f, -458.31f);
        contentStream.curveTo(-435.73f, -462.8f, -432.2f, -466.32f, -427.71f, -466.32f);
        contentStream.lineTo(-337.61f, -466.32f);
        contentStream.curveTo(-333.13f, -466.32f, -329.6f, -462.8f, -329.6f, -458.31f);
        contentStream.lineTo(-329.6f, -421.28f);
        contentStream.lineTo(-247.68f, -421.28f);
        contentStream.curveTo(-243.19f, -421.28f, -239.5f, -417.75f, -239.5f, -413.26f);
        contentStream.lineTo(-239.5f, -331.35f);
        contentStream.lineTo(-157.58f, -331.35f);
        contentStream.curveTo(-153.1f, -331.35f, -149.41f, -327.66f, -149.41f, -323.17f);
        contentStream.lineTo(-149.41f, -218.81f);
        contentStream.lineTo(-67.49f, -218.81f);
        contentStream.curveTo(-63.0f, -218.81f, -59.47f, -215.13f, -59.47f, -210.64f);
        contentStream.lineTo(-59.47f, -16.19f);
        contentStream.lineTo(0f, -16.19f);
        contentStream.lineTo(0f, 0f);
        contentStream.closePath();
        contentStream.moveTo(-149.41f, -16.19f);
        contentStream.lineTo(-75.67f, -16.19f);
        contentStream.lineTo(-75.67f, -202.62f);
        contentStream.lineTo(-149.41f, -202.62f);
        contentStream.lineTo(-149.41f, -16.19f);
        contentStream.closePath();
        contentStream.moveTo(-239.5f, -16.19f);
        contentStream.lineTo(-165.76f, -16.19f);
        contentStream.lineTo(-165.76f, -315.16f);
        contentStream.lineTo(-239.5f, -315.16f);
        contentStream.lineTo(-239.5f, -16.19f);
        contentStream.closePath();
        contentStream.moveTo(-329.6f, -16.19f);
        contentStream.lineTo(-255.7f, -16.19f);
        contentStream.lineTo(-255.7f, -405.09f);
        contentStream.lineTo(-329.6f, -405.09f);
        contentStream.lineTo(-329.6f, -16.19f);
        contentStream.closePath();
        contentStream.moveTo(-419.53f, -16.19f);
        contentStream.lineTo(-345.79f, -16.19f);
        contentStream.lineTo(-345.79f, -450.13f);
        contentStream.lineTo(-419.53f, -450.13f);
        contentStream.lineTo(-419.53f, -16.19f);
        contentStream.closePath();
        contentStream.moveTo(-509.63f, -16.19f);
        contentStream.lineTo(-435.73f, -16.19f);
        contentStream.lineTo(-435.73f, -337.6f);
        contentStream.lineTo(-509.63f, -337.6f);
        contentStream.lineTo(-509.63f, -16.19f);
        contentStream.closePath();
        contentStream.moveTo(-599.56f, -16.19f);
        contentStream.lineTo(-525.82f, -16.19f);
        contentStream.lineTo(-525.82f, -247.51f);
        contentStream.lineTo(-599.56f, -247.51f);
        contentStream.lineTo(-599.56f, -16.19f);
        contentStream.closePath();
        contentStream.fill();
    }

    private void drawTag(final PDAppearanceContentStream contentStream) throws IOException
    {
        // ty 18 is from the caller, scale 0.022 is by trial and error
        contentStream.transform(new Matrix(0.022f, 0, 0, -0.022f, 0f, 18f));

        // Source: https://www.svgrepo.com/svg/29652/tag
        // License: CC0
        contentStream.saveGraphicsState();
        contentStream.transform(Matrix.getTranslateInstance(209.26f, 128.32f));
        contentStream.moveTo(0f, 0f);
        contentStream.curveTo(-44.73f, 0f, -80.64f, 36.23f, -80.64f, 80.64f);
        contentStream.curveTo(-80.64f, 125.2f, -44.57f, 161.27f, 0f, 161.27f);
        contentStream.curveTo(44.56f, 161.27f, 80.47f, 125.04f, 80.47f, 80.64f);
        contentStream.curveTo(80.63f, 36.07f, 44.56f, 0f, 0f, 0f);
        contentStream.closePath();
        contentStream.moveTo(0f, 132.74f);
        contentStream.curveTo(-28.7f, 132.74f, -52.1f, 109.33f, -52.1f, 80.64f);
        contentStream.curveTo(-52.1f, 51.94f, -28.7f, 28.54f, 0f, 28.54f);
        contentStream.curveTo(28.69f, 28.54f, 51.93f, 51.94f, 51.93f, 80.64f);
        contentStream.curveTo(51.93f, 109.33f, 28.85f, 132.74f, 0f, 132.74f);
        contentStream.closePath();
        contentStream.fill();
        contentStream.restoreGraphicsState();
        contentStream.saveGraphicsState();
        contentStream.transform(Matrix.getTranslateInstance(382.22f, 79.91f));
        contentStream.moveTo(0f, 0f);
        contentStream.curveTo(-14.58f, -16.19f, -35.1f, -24.85f, -57.22f, -24.85f);
        contentStream.lineTo(-208.23f, -26.45f);
        contentStream.curveTo(-240.45f, -26.45f, -271.23f, -14.75f, -293.35f, 8.66f);
        contentStream.curveTo(-316.76f, 30.78f, -328.46f, 61.56f, -328.46f, 93.78f);
        contentStream.lineTo(-327.02f, 244.95f);
        contentStream.curveTo(-325.57f, 265.47f, -318.2f, 285.98f, -302.17f, 302.18f);
        contentStream.lineTo(58.68f, 663.02f);
        contentStream.lineTo(360.85f, 360.69f);
        contentStream.lineTo(0f, 0f);
        contentStream.lineTo(0f, 0f);
        contentStream.closePath();
        contentStream.moveTo(57.23f, 621.82f);
        contentStream.lineTo(-283.09f, 281.5f);
        contentStream.curveTo(-293.35f, 271.24f, -299.12f, 258.09f, -299.12f, 243.34f);
        contentStream.lineTo(-300.57f, 93.78f);
        contentStream.curveTo(-300.57f, 70.38f, -290.31f, 46.81f, -274.12f, 29.34f);
        contentStream.curveTo(-256.64f, 11.7f, -233.08f, 1.44f, -208.23f, 1.44f);
        contentStream.lineTo(-58.67f, 2.89f);
        contentStream.curveTo(-44.08f, 2.89f, -30.77f, 8.66f, -20.51f, 19.08f);
        contentStream.lineTo(319.81f, 359.4f);
        contentStream.lineTo(57.23f, 621.82f);
        contentStream.closePath();
        contentStream.fill();
        contentStream.restoreGraphicsState();
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
