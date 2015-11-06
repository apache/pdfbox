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

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Prints a PDF document using its original paper size.
 *
 * @author John Hewson
 */
public final class PDFPageable extends Book
{
    private final PDDocument document;
    private final boolean showPageBorder;
    private final float dpi;
    private final Orientation orientation;

    /**
     * Creates a new PDFPageable.
     *
     * @param document the document to print
     */
    public PDFPageable(PDDocument document)
    {
        this(document, Orientation.AUTO, false, 0);
    }
    
    /**
     * Creates a new PDFPageable with the given page orientation.
     *
     * @param document the document to print
     * @param orientation page orientation policy
     */
    public PDFPageable(PDDocument document, Orientation orientation)
    {
        this(document, orientation, false, 0);
    }
    
    /**
     * Creates a new PDFPageable with the given page orientation and with optional page borders
     * shown. The image will be rasterized at the given DPI before being sent to the printer.
     *
     * @param document the document to print
     * @param orientation page orientation policy
     * @param showPageBorder true if page borders are to be printed
     */
    public PDFPageable(PDDocument document, Orientation orientation, boolean showPageBorder)
    {
        this(document, orientation, showPageBorder, 0);
    }

    /**
     * Creates a new PDFPageable with the given page orientation and with optional page borders
     * shown. The image will be rasterized at the given DPI before being sent to the printer.
     *
     * @param document the document to print
     * @param orientation page orientation policy
     * @param showPageBorder true if page borders are to be printed
     * @param dpi if non-zero then the image will be rasterized at the given DPI
     */
    public PDFPageable(PDDocument document, Orientation orientation, boolean showPageBorder,
                       float dpi)
    {
        this.document = document;
        this.orientation = orientation;
        this.showPageBorder = showPageBorder;
        this.dpi = dpi;
    }

    @Override
    public int getNumberOfPages()
    {
        return document.getNumberOfPages();
    }

    /**
     * {@inheritDoc}
     * 
     * Returns the actual physical size of the pages in the PDF file. May not fit the local printer.
     */
    @Override
    public PageFormat getPageFormat(int pageIndex)
    {
        PDPage page = document.getPage(pageIndex);
        PDRectangle mediaBox = PDFPrintable.getRotatedMediaBox(page);
        PDRectangle cropBox = PDFPrintable.getRotatedCropBox(page);
        
        // Java does not seem to understand landscape paper sizes, i.e. where width > height, it
        // always crops the imageable area as if the page were in portrait. I suspect that this is
        // a JDK bug but it might be by design, see PDFBOX-2922.
        //
        // As a workaround, we normalise all Page(s) to be portrait, then flag them as landscape in
        // the PageFormat.
        Paper paper;
        boolean isLandscape;
        if (mediaBox.getWidth() > mediaBox.getHeight())
        {
            // rotate
            paper = new Paper();
            paper.setSize(mediaBox.getHeight(), mediaBox.getWidth());
            paper.setImageableArea(cropBox.getLowerLeftY(), cropBox.getLowerLeftX(),
                    cropBox.getHeight(), cropBox.getWidth());
            isLandscape = true;
        }
        else
        {
            paper = new Paper();
            paper.setSize(mediaBox.getWidth(), mediaBox.getHeight());
            paper.setImageableArea(cropBox.getLowerLeftX(), cropBox.getLowerLeftY(),
                    cropBox.getWidth(), cropBox.getHeight());
            isLandscape = false;
        }

        PageFormat format = new PageFormat();
        format.setPaper(paper);
        
        // auto portrait/landscape
        if (orientation == Orientation.AUTO)
        {
            if (isLandscape)
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
        
        return format;
    }
    
    @Override
    public Printable getPrintable(int i)
    {
        if (i >= getNumberOfPages())
        {
            throw new IndexOutOfBoundsException(i + " >= " + getNumberOfPages());
        }
        return new PDFPrintable(document, Scaling.ACTUAL_SIZE, showPageBorder, dpi);
    }
}
