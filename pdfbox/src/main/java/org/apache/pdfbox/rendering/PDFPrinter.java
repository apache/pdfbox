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
import org.apache.pdfbox.rendering.printing.Orientation;
import org.apache.pdfbox.rendering.printing.Scaling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
 * @author Andreas Lehmk�hler
 * @author John Hewson
 */
public class PDFPrinter
{
    protected final PDDocument document;
    protected final PDFRenderer renderer;
    protected final PrinterJob printerJob;
    protected final Scaling scaling;
    protected final Orientation orientation;
    protected final boolean showPageBorder;
    protected final Paper paper; // may be null

    /**
     * Creates a new PDFPrinter.
     * @param document the document to print
     */
    public PDFPrinter(PDDocument document) throws PrinterException
    {
        this(document, PrinterJob.getPrinterJob());
    }

    /**
     * Creates a new PDFPrinter for a given printer job.
     * @param document the document to print
     * @param printerJob the printer job to use
     */
    public PDFPrinter(PDDocument document, PrinterJob printerJob) throws PrinterException
    {
        this(document, printerJob, Scaling.SHRINK_TO_FIT, Orientation.AUTO, false, null);
    }

    /**
     * Creates a new PDFPrinter with the given page scaling and orientation.
     * @param document the document to print
     * @param scaling page scaling policy
     * @param orientation page orientation policy
     */
    public PDFPrinter(PDDocument document, Scaling scaling, Orientation orientation)
            throws PrinterException
    {
        this(document, PrinterJob.getPrinterJob(), scaling, orientation, false, null);
    }

    /**
     * Creates a new PDFPrinter with the given page scaling and orientation.
     * @param document the document to print
     * @param scaling page scaling policy
     * @param orientation page orientation policy
     */
    public PDFPrinter(PDDocument document, Scaling scaling, Orientation orientation, Paper paper)
            throws PrinterException
    {
        this(document, PrinterJob.getPrinterJob(), scaling, orientation, false, paper);
    }

    /**
     * Creates a new PDFPrinter for a given printer job, the given page scaling and orientation,
     * and with optional page borders shown.
     * @param document the document to print
     * @param printerJob the printer job to use
     * @param scaling page scaling policy
     * @param orientation page orientation policy
     * @param showPageBorder true if page borders are to be printed
     * @throws PrinterException
     */
    public PDFPrinter(PDDocument document, PrinterJob printerJob, Scaling scaling,
                      Orientation orientation, boolean showPageBorder, Paper paper)
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
        this.orientation = orientation;
        this.showPageBorder = showPageBorder;
        this.paper = paper;
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
            job.setPageable(new PDFPageable());
            if (isSilent || job.printDialog())
            {
                job.print();
            }
        }
    }

    protected class PDFPageable implements Pageable
    {
        @Override
        public int getNumberOfPages()
        {
            return document.getNumberOfPages();
        }

        @Override
        public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException
        {
            PageFormat format = printerJob.defaultPage();
            PDPage page = document.getPage(pageIndex);

            // auto portrait/landscape
            if (orientation == Orientation.AUTO)
            {
                Dimension cropBox = page.findRotatedCropBox().createDimension();
                if (cropBox.getWidth() > cropBox.getHeight())
                {
                    format.setOrientation(PageFormat.LANDSCAPE);
                }
                else
                {
                    format.setOrientation(PageFormat.PORTRAIT);
                }
            }
            else if (orientation == Orientation.LANDSCAPE)
            {
                format.setOrientation(PageFormat.LANDSCAPE);
            }
            else if (orientation == Orientation.PORTRAIT)
            {
                format.setOrientation(PageFormat.PORTRAIT);
            }

            // custom paper
            if (paper != null)
            {
                format.setPaper(paper);
            }

            return format;
        }

        @Override
        public Printable getPrintable(int i) throws IndexOutOfBoundsException
        {
            if (i >= getNumberOfPages())
            {
                throw new IndexOutOfBoundsException(i + " >= " +  getNumberOfPages());
            }
            return new PDFPrintable();
        }
    }

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

                return PAGE_EXISTS;
            }
            catch (IOException e)
            {
                throw new PrinterIOException(e);
            }
        }
    }
}
