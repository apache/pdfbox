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

package org.apache.pdfbox.printing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Prints pages from a PDF document using any page size or scaling mode.
 *
 * @author John Hewson
 */
public final class PDFPrintable implements Printable
{
    private final PDDocument document;
    private final PDFRenderer renderer;
    
    private final boolean showPageBorder;
    private final Scaling scaling;
    private final float dpi;
    private final boolean center;

    /**
     * Creates a new PDFPrintable.
     *
     * @param document the document to print
     */
    public PDFPrintable(PDDocument document)
    {
        this(document, Scaling.SHRINK_TO_FIT);
    }

    /**
     * Creates a new PDFPrintable with the given page scaling.
     *
     * @param document the document to print
     * @param scaling page scaling policy
     */
    public PDFPrintable(PDDocument document, Scaling scaling)
    {
       this(document, scaling, false, 0);
    }

    /**
     * Creates a new PDFPrintable with the given page scaling and with optional page borders shown.
     *
     * @param document the document to print
     * @param scaling page scaling policy
     * @param showPageBorder true if page borders are to be printed
     */
    public PDFPrintable(PDDocument document, Scaling scaling, boolean showPageBorder)
    {
        this(document, scaling, showPageBorder, 0);
    }

    /**
     * Creates a new PDFPrintable with the given page scaling and with optional page borders shown.
     * The image will be rasterized at the given DPI before being sent to the printer.
     *
     * @param document the document to print
     * @param scaling page scaling policy
     * @param showPageBorder true if page borders are to be printed
     * @param dpi if non-zero then the image will be rasterized at the given DPI
     */
    public PDFPrintable(PDDocument document, Scaling scaling, boolean showPageBorder, float dpi)
    {
        this(document, scaling, showPageBorder, dpi, true);
    }
    
    /**
     * Creates a new PDFPrintable with the given page scaling and with optional page borders shown.
     * The image will be rasterized at the given DPI before being sent to the printer.
     *
     * @param document the document to print
     * @param scaling page scaling policy
     * @param showPageBorder true if page borders are to be printed
     * @param dpi if non-zero then the image will be rasterized at the given DPI
     * @param center true if the content is to be centered on the page (otherwise top-left).
     */
    public PDFPrintable(PDDocument document, Scaling scaling, boolean showPageBorder, float dpi,
                        boolean center)
    {
        this.document = document;
        this.renderer = new PDFRenderer(document);
        this.scaling = scaling;
        this.showPageBorder = showPageBorder;
        this.dpi = dpi;
        this.center = center;
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException
    {
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages())
        {
            return NO_SUCH_PAGE;
        }
        try
        {
            Graphics2D graphics2D = (Graphics2D)graphics;

            PDPage page = document.getPage(pageIndex);
            PDRectangle cropBox = getRotatedCropBox(page);

            // the imageable area is the area within the page margins
            final double imageableWidth = pageFormat.getImageableWidth();
            final double imageableHeight = pageFormat.getImageableHeight();

            double scale = 1;
            if (scaling != Scaling.ACTUAL_SIZE)
            {
                // scale to fit
                double scaleX = imageableWidth / cropBox.getWidth();
                double scaleY = imageableHeight / cropBox.getHeight();
                scale = Math.min(scaleX, scaleY);

                // only shrink to fit when enabled
                if (scale > 1 && scaling == Scaling.SHRINK_TO_FIT)
                {
                    scale = 1;
                }
                
                // only stretch to fit when enabled
                if (scale < 1 && scaling == Scaling.STRETCH_TO_FIT)
                {
                    scale = 1;
                }
            }

            // set the graphics origin to the origin of the imageable area (i.e the margins)
            graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // center on page
            if (center)
            {
                graphics2D.translate((imageableWidth - cropBox.getWidth() * scale) / 2,
                                     (imageableHeight - cropBox.getHeight() * scale) / 2);
            }

            // rasterize to bitmap (optional)
            Graphics2D printerGraphics = null;
            BufferedImage image = null;
            if (dpi > 0)
            {
                float dpiScale = dpi / 72;
                image = new BufferedImage((int)(imageableWidth * dpiScale / scale),
                                          (int)(imageableHeight * dpiScale / scale),
                                          BufferedImage.TYPE_INT_ARGB);

                printerGraphics = graphics2D;
                graphics2D = image.createGraphics();

                // rescale
                printerGraphics.scale(scale / dpiScale, scale / dpiScale);
                scale = dpiScale;
            }

            // draw to graphics using PDFRender
            AffineTransform transform = (AffineTransform)graphics2D.getTransform().clone();
            graphics2D.setBackground(Color.WHITE);
            renderer.renderPageToGraphics(pageIndex, graphics2D, (float)scale);

            // draw crop box
            if (showPageBorder)
            {
                graphics2D.setTransform(transform);
                graphics2D.setClip(0, 0, (int)imageableWidth, (int)imageableHeight);
                graphics2D.scale(scale, scale);
                graphics2D.setColor(Color.GRAY);
                graphics2D.setStroke(new BasicStroke(0.5f));
                graphics.drawRect(0, 0, (int)cropBox.getWidth(), (int)cropBox.getHeight());
            }

            // draw rasterized bitmap (optional)
            if (printerGraphics != null)
            {
                printerGraphics.setBackground(Color.WHITE);
                printerGraphics.clearRect(0, 0, image.getWidth(), image.getHeight());
                printerGraphics.drawImage(image, 0, 0, null);
                graphics2D.dispose();
            }

            return PAGE_EXISTS;
        }
        catch (IOException e)
        {
            throw new PrinterIOException(e);
        }
    }

    /**
     * This will find the CropBox with rotation applied, for this page by looking up the hierarchy
     * until it finds them.
     *
     * @return The CropBox at this level in the hierarchy.
     */
    static PDRectangle getRotatedCropBox(PDPage page)
    {
        PDRectangle cropBox = page.getCropBox();
        int rotationAngle = page.getRotation();
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            return new PDRectangle(cropBox.getLowerLeftY(), cropBox.getLowerLeftX(),
                                   cropBox.getHeight(), cropBox.getWidth());
        }
        else
        {
            return cropBox;
        }
    }

    /**
     * This will find the MediaBox with rotation applied, for this page by looking up the hierarchy
     * until it finds them.
     *
     * @return The MediaBox at this level in the hierarchy.
     */
    static PDRectangle getRotatedMediaBox(PDPage page)
    {
        PDRectangle mediaBox = page.getMediaBox();
        int rotationAngle = page.getRotation();
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            return new PDRectangle(mediaBox.getLowerLeftY(), mediaBox.getLowerLeftX(),
                                   mediaBox.getHeight(), mediaBox.getWidth());
        }
        else
        {
            return mediaBox;
        }
    }
}
