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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
    protected final PDDocument document;
    protected final PDFRenderer renderer;
    protected final PrinterJob printerJob;

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
    }

    /**
     * Prints the given document using the default printer without prompting the user.
     * @throws java.awt.print.PrinterException if the document cannot be printed
     */
    public void silentPrint() throws PrinterException
    {
        silentPrint(PrinterJob.getPrinterJob());
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
        print(PrinterJob.getPrinterJob());
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
            Dimension media = page.findMediaBox().createDimension();
            Dimension crop = page.findCropBox().createDimension();

            // Center the ImageableArea if the crop is smaller than the media
            double diffWidth = 0.0;
            double diffHeight = 0.0;
            if (!media.equals(crop))
            {
                diffWidth = (media.getWidth() - crop.getWidth()) / 2.0;
                diffHeight = (media.getHeight() - crop.getHeight()) / 2.0;
            }

            Paper paper = format.getPaper();
            if (media.getWidth() < media.getHeight())
            {
                format.setOrientation(PageFormat.PORTRAIT);
                paper.setImageableArea(diffWidth, diffHeight, crop.getWidth(), crop.getHeight());
            }
            else
            {
                format.setOrientation(PageFormat.LANDSCAPE);
                paper.setImageableArea(diffHeight, diffWidth, crop.getHeight(), crop.getWidth());
            }
            format.setPaper(paper);

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

            // draws to graphics using PDFRender
            try
            {
                // TODO need to specify print DPI
                renderer.renderPageToGraphics(pageIndex, (Graphics2D) graphics);
                return PAGE_EXISTS;
            }
            catch (IOException io)
            {
                throw new PrinterIOException(io);
            }
        }
    }
}
