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
package org.apache.pdfbox.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * RenderUtil provides some convenience methods to print or draw a single page of a document.
 * @author Andreas Lehmkühler
 */
public final class RenderUtil
{
    private RenderUtil()
    {
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @param document the document to be printed
     * @throws PrinterException if the document cannot be printed
     */
    public static void silentPrint(PDDocument document) throws PrinterException
    {
        silentPrint(document, PrinterJob.getPrinterJob());
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @param document the document to be printed
     * @param printerJob a printer job definition
     * @throws PrinterException if the document cannot be printed
     */
    public static void silentPrint(PDDocument document, PrinterJob printerJob) throws PrinterException
    {
        print(document, printerJob, true);
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * The image is generated using {@link org.apache.pdfbox.pdfviewer.PageDrawer}.
     * This is a convenience method to create the java.awt.print.PrinterJob.
     * Advanced printing tasks can be performed using {@link PDPageable} instead.
     * @param document the document to be printed
     * @throws PrinterException if the document cannot be printed
     */
    public static void print(PDDocument document) throws PrinterException
    {
        print(document, PrinterJob.getPrinterJob());
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @param document the document to be printed
     * @param printerJob The printer job.
     * @throws PrinterException if the document cannot be printed
     */
    public static void print(PDDocument document, PrinterJob printerJob) throws PrinterException
    {
        print(document, printerJob, false);
    }

    // prints a document
    private static void print(PDDocument document, PrinterJob job, boolean isSilent)
            throws PrinterException
    {
        if (job == null)
        {
            throw new IllegalArgumentException("job cannot be null");
        }
        else
        {
            job.setPageable(new PDPageable(document, job));
            if (isSilent || job.printDialog())
            {
                job.print();
            }
        }
    }

    // =============================================================================================

    /**
     * Convert the given page to an output image with 8 bits per pixel and the double default screen resolution.
     *
     * @param page the page to be converted.
     * @return A graphical representation of this page.
     * @throws IOException If there is an error drawing to the image.
     */
    public static BufferedImage convertToImage(PDPage page) throws IOException
    {
        // note we are doing twice as many pixels because
        // the default size is not really good resolution,
        // so create an image that is twice the size
        // and let the client scale it down.
        return convertToImage(page, BufferedImage.TYPE_INT_RGB, 2 * PDPage.DEFAULT_USER_SPACE_UNIT_DPI);
    }

    /**
     * Convert the given page to an output image.
     *
     * @param page the page to be converted.
     * @param imageType the image type (see {@link BufferedImage}.TYPE_*)
     * @param resolution the resolution in dpi (dots per inch)
     * @return A graphical representation of this page.
     * @throws IOException If there is an error drawing to the image.
     */
    public static BufferedImage convertToImage(PDPage page, int imageType, int resolution) throws IOException
    {
        PDRectangle cropBox = page.findCropBox();
        float widthPt = cropBox.getWidth();
        float heightPt = cropBox.getHeight();
        float scale = resolution / (float) PDPage.DEFAULT_USER_SPACE_UNIT_DPI;
        int widthPx = Math.round(widthPt * scale);
        int heightPx = Math.round(heightPt * scale);
        int rotationAngle = page.findRotation();

        // normalize the rotation angle
        if (rotationAngle < 0)
        {
            rotationAngle += 360;
        }
        else if (rotationAngle >= 360)
        {
            rotationAngle -= 360;
        }

        // swap width and height
        BufferedImage image;
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            image = new BufferedImage(heightPx, widthPx, imageType);
        }
        else
        {
            image = new BufferedImage(widthPx, heightPx, imageType);
        }

        // use a transparent background if the imageType supports alpha
        Graphics2D g = image.createGraphics();
        if (!image.getColorModel().hasAlpha())
        {
            g.setBackground(Color.WHITE);
        }

        renderPage(page, g, image.getWidth(), image.getHeight(), scale, scale);
        g.dispose();

        return image;
    }

    // renders a page to the given graphics
    private static void renderPage(PDPage page, Graphics2D graphics, int width, int height, float scaleX, float scaleY)
            throws IOException
    {
        graphics.clearRect(0, 0, width, height);
        int rotationAngle = page.findRotation();
        if (rotationAngle != 0)
        {
            int translateX = 0;
            int translateY = 0;
            switch (rotationAngle)
            {
            case 90:
                translateX = width;
                break;
            case 270:
                translateY = height;
                break;
            case 180:
                translateX = width;
                translateY = height;
                break;
            default:
                break;
            }
            graphics.translate(translateX, translateY);
            graphics.rotate((float) Math.toRadians(rotationAngle));
        }
        graphics.scale(scaleX, scaleY);
        PageDrawer drawer = new PageDrawer();
        drawer.drawPage(graphics, page, page.findCropBox());
        drawer.dispose();
    }
}
