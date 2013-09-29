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
 * 
 */
public class RenderUtil
{
    /**
     * Fully transparent that can fall back to white when image type has no alpha.
     */
    private static final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 0);

    private RenderUtil()
    {
        // Utility class. Don't instantiate.
    }

    private static void print(PDDocument document, PrinterJob job, boolean silent) throws PrinterException
    {
        if (job == null)
        {
            throw new PrinterException("The given printer job is null.");
        }
        else
        {
            job.setPageable(new PDPageable(document, job));
            if (silent || job.printDialog())
            {
                job.print();
            }
        }
    }

    /**
     * This will send the PDF to the default printer without prompting the user for any printer settings.
     * 
     * @param document the document to be printed
     * @param printJob A printer job definition.
     * @see RenderUtil#print()
     * 
     * @throws PrinterException If there is an error while printing.
     */
    public static void silentPrint(PDDocument document, PrinterJob printJob) throws PrinterException
    {
        print(document, printJob, true);
    }

    /**
     * @see RenderUtil#print()
     * 
     * @param document the document to be printed
     * @param printJob The printer job.
     * 
     * @throws PrinterException If there is an error while sending the PDF to the printer, or you do not have
     *             permissions to print this document.
     */
    public static void print(PDDocument document, PrinterJob printJob) throws PrinterException
    {
        print(document, printJob, false);
    }

    /**
     * This will send the PDF document to a printer. The printing functionality depends on the
     * org.apache.pdfbox.pdfviewer.PageDrawer functionality. The PageDrawer is a work in progress and some PDFs will
     * print correctly and some will not. This is a convenience method to create the java.awt.print.PrinterJob. The
     * PDPageable implements the java.awt.print.Pageable interface and the java.awt.print.Printable interface, so
     * advanced printing capabilities can be done by using those interfaces instead of this method.
     * 
     * @param document the document to be printed
     * @throws PrinterException If there is an error while sending the PDF to the printer, or you do not have
     *             permissions to print the document.
     */
    public static void print(PDDocument document) throws PrinterException
    {
        print(document, PrinterJob.getPrinterJob());
    }

    /**
     * This will send the given PDF to the default printer without prompting the user for any printer settings.
     * 
     * @param document the document to be printed
     * @see RenderUtil#print()
     * 
     * @throws PrinterException If there is an error while printing.
     */
    public static void silentPrint(PDDocument document) throws PrinterException
    {
        silentPrint(document, PrinterJob.getPrinterJob());
    }

    /**
     * Convert the given page to an output image with 8 bits per pixel and the double default screen resolution.
     * 
     * @param page the page to be converted.
     * @return A graphical representation of this page.
     * 
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
     * 
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
        BufferedImage retval = null;
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
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            retval = new BufferedImage(heightPx, widthPx, imageType);
        }
        else
        {
            retval = new BufferedImage(widthPx, heightPx, imageType);
        }
        Graphics2D graphics2D = (Graphics2D) retval.getGraphics();
        renderPage(page, graphics2D, retval.getWidth(), retval.getHeight(), scale, scale);
        graphics2D.dispose();
        return retval;
    }

    private static void renderPage(PDPage page, Graphics2D graphics, int width, int height, float scaleX, float scaleY)
            throws IOException
    {
        graphics.setBackground(TRANSPARENT_WHITE);
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
        // TODO The following reduces accuracy. It should really be a Dimension2D.Float.
        drawer.drawPage(graphics, page, page.findCropBox().createDimension());
        drawer.dispose();
    }
}
