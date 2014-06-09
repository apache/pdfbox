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
package org.apache.pdfbox.rendering;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.printing.Scaling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.IOException;

/**
 * Prints a PDF document using AWT.
 * This class may be overridden in order to perform custom printing.
 *
 * @author Andreas Lehmkühler
 * @author John Hewson
 */
public class PDFPrinter
{
    private final PDDocument document;
    private final PDFRenderer renderer;
    private final PrinterJob printerJob;
    private final Scaling scaling;
    private final boolean showPageBorder;
    private final float dpi;

    /**
     * Creates a new PDFPrinter.
     * @param document the document to print
     * @throws PrinterException if something went wrong
     */
    public PDFPrinter(PDDocument document) throws PrinterException
    {
        this(document, PrinterJob.getPrinterJob());
    }

    /**
     * Creates a new PDFPrinter for a given printer job.
     * @param document the document to print
     * @param printerJob the printer job to use
     * @throws PrinterException if something went wrong
     */
    public PDFPrinter(PDDocument document, PrinterJob printerJob) throws PrinterException
    {
        this(document, printerJob, Scaling.SHRINK_TO_FIT, false, 0);
    }

    /**
     * Creates a new PDFPrinter with the given page scaling.
     * @param document the document to print
     * @param scaling page scaling policy
     * @throws PrinterException if something went wrong
     */
    public PDFPrinter(PDDocument document, Scaling scaling)
            throws PrinterException
    {
        this(document, PrinterJob.getPrinterJob(), scaling, false, 0);
    }

    /**
     * Creates a new PDFPrinter with the given page scaling.
     * @param document the document to print
     * @param scaling page scaling policy
     * @param dpi if non-zero then the image will be rasterized at the given DPI
     * @throws PrinterException if something went wrong
     */
    public PDFPrinter(PDDocument document, Scaling scaling, float dpi) throws PrinterException
    {
        this(document, PrinterJob.getPrinterJob(), scaling, false, dpi);
    }

    /**
     * Creates a new PDFPrinter for a given printer job, the given page scaling,
     * and with optional page borders shown.
     * @param document the document to print
     * @param printerJob the printer job to use
     * @param scaling page scaling policy
     * @param showPageBorder true if page borders are to be printed
     * @param dpi if non-zero then the image will be rasterized at the given DPI
     * @throws PrinterException if something went wrong
     */
    public PDFPrinter(PDDocument document, PrinterJob printerJob, Scaling scaling,
                      boolean showPageBorder, float dpi)
            throws PrinterException
    {
        if (document == null)
        {
            throw new IllegalArgumentException("document");
        }
        else if (printerJob == null)
        {
            throw new IllegalArgumentException("printerJob");
        }
        else if (!document.getCurrentAccessPermission().canPrint())
        {
            throw new PrinterException("You do not have permission to print this document");
        }
        this.document = document;
        this.renderer = new PDFRenderer(document);
        this.printerJob = printerJob;
        this.scaling = scaling;
        this.showPageBorder = showPageBorder;
        this.dpi = dpi;
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @throws java.awt.print.PrinterException if the document cannot be printed
     */
    public void silentPrint() throws PrinterException
    {
        silentPrint(printerJob);
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @param printerJob a printer job definition
     * @throws PrinterException if the document cannot be printed
     */
    public void silentPrint(PrinterJob printerJob) throws PrinterException
    {
        print(printerJob, true);
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * The image is generated using {@link PageDrawer}.
     * This is a convenience method to create the java.awt.print.PrinterJob.
     * Advanced printing tasks can be performed using {@link #getPageable()} instead.
     * @throws PrinterException if the document cannot be printed
     */
    public void print() throws PrinterException
    {
        print(printerJob);
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @param printerJob the printer job.
     * @throws PrinterException if the document cannot be printed
     */
    public void print(PrinterJob printerJob) throws PrinterException
    {
        print(printerJob, false);
    }

    // todo: new
    /**
     * Returns a newly create PDFPageable.
     * @return an newly created instance of PDFPageable
     */
    public PDFPageable getPageable()
    {
        return new PDFPageable();
    }

    // prints a document
    private void print(PrinterJob job, boolean isSilent) throws PrinterException
    {
        if (job == null)
        {
            throw new IllegalArgumentException("job cannot be null");
        }
        else
        {
            job.setPageable(getPageable());
            if (isSilent || job.printDialog())
            {
                job.print();
            }
        }
    }

    /**
     * PDFPageable implements the interface java.awt.print.Pageable.
     */
    protected class PDFPageable implements Pageable
    {
        @Override
        public int getNumberOfPages()
        {
            return document.getNumberOfPages();
        }

        @Override
        public PageFormat getPageFormat(int pageIndex)
        {
            PDPage page = document.getPage(pageIndex);
            Dimension media = page.findMediaBox().createDimension();
            Dimension crop = page.findCropBox().createDimension();
            // Center the ImageableArea if the crop is smaller than the media
            double diffWidth = 0.0;
            double diffHeight = 0.0;
            if( !media.equals( crop ) )
            {
                   diffWidth = (media.getWidth() - crop.getWidth()) / 2.0;
                   diffHeight = (media.getHeight() - crop.getHeight()) / 2.0;
            }
            PageFormat wantedFormat = new PageFormat();
            Paper wantedPaper = new Paper();
            boolean hasRotation = page.findRotation() != 0;
            Dimension rotatedCrop = null;
            if (hasRotation)
            {
                rotatedCrop = page.findRotatedCropBox().createDimension();
            }
            else
            {
                rotatedCrop = crop;
            }

            if( rotatedCrop.getWidth() <= rotatedCrop.getHeight() )
            {
                   wantedFormat.setOrientation( PageFormat.PORTRAIT );
                   if (hasRotation)
                   {
                       wantedPaper.setSize( media.getHeight(), media.getWidth() );
                       wantedPaper.setImageableArea( diffHeight, diffWidth, crop.getHeight(), crop.getWidth() );
                   }
                   else
                   {
                       wantedPaper.setSize( media.getWidth(), media.getHeight() );
                       wantedPaper.setImageableArea( diffWidth, diffHeight, crop.getWidth(), crop.getHeight() );
                   }
            }
            else
            {
                   wantedFormat.setOrientation( PageFormat.LANDSCAPE );
                   if (hasRotation)
                   {
                       wantedPaper.setSize( media.getWidth(), media.getHeight() );
                       wantedPaper.setImageableArea( diffWidth, diffHeight, crop.getWidth(), crop.getHeight() );
                   }
                   else
                   {
                       wantedPaper.setSize( media.getHeight(), media.getWidth() );
                       wantedPaper.setImageableArea( diffHeight, diffWidth, crop.getHeight(), crop.getWidth() );
                   }
            }
            wantedFormat.setPaper( wantedPaper );
            return printerJob.validatePage( wantedFormat );
        }

        @Override
        public Printable getPrintable(int i)
        {
            if (i >= getNumberOfPages())
            {
                throw new IndexOutOfBoundsException(i + " >= " +  getNumberOfPages());
            }
            return new PDFPrintable();
        }
    }

    /**
     * PDFPageable implements the interface java.awt.print.Printable.
     */
    protected class PDFPrintable implements Printable
    {
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
                PDRectangle cropBox = page.findRotatedCropBox();

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
                }

                // set the graphics origin to the origin of the imageable area (i.e the margins)
                graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // center on page
                graphics2D.translate((imageableWidth - cropBox.getWidth() * scale) / 2,
                        (imageableHeight - cropBox.getHeight() * scale) / 2);

                // rasterize to bitmap (optional)
                Graphics2D printerGraphics = null;
                BufferedImage image = null;
                if (dpi > 0)
                {
                    float dpiScale = dpi / 72;
                    image = new BufferedImage((int)(imageableWidth * dpiScale), (int)(imageableHeight * dpiScale),
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
    }
}
