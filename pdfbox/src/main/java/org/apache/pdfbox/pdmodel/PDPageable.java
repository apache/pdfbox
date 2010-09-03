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
package org.apache.pdfbox.pdmodel;

import static javax.print.attribute.standard.OrientationRequested.LANDSCAPE;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;
import javax.print.attribute.standard.OrientationRequested;

/**
 * Adapter class that implements the {@link Pageable} and {@link Printable}
 * interfaces for printing a given PDF document. Note that the given PDF
 * document should not be modified (pages added, removed, etc.) while an
 * instance of this class is being used.
 *
 * @since Apache PDFBox 1.3.0
 * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-788">PDFBOX-788</a>
 */
public class PDPageable implements Pageable, Printable {

    /**
     * List of all pages in the given PDF document.
     */
    private final List<PDPage> pages = new ArrayList<PDPage>();

    /**
     * The printer job for printing the given PDF document.
     */
    private final PrinterJob job;

    /**
     * Creates a {@link Pageable} adapter for the given PDF document and
     * printer job.
     *
     * @param document PDF document
     * @param job printer job
     * @throws IllegalArgumentException if an argument is <code>null</code>
     * @throws PrinterException if the document permissions prevent printing
     */
    public PDPageable(PDDocument document, PrinterJob job)
            throws IllegalArgumentException, PrinterException {
        if (document == null || job == null) {
            throw new IllegalArgumentException(
                    "PDPageable(" + document + ", " + job + ")");
        } else if (!document.getCurrentAccessPermission().canPrint()) {
            throw new PrinterException(
                "You do not have permission to print this document");
        } else {
            document.getDocumentCatalog().getPages().getAllKids(pages);
            this.job = job;
        }
    }

    /**
     * Creates a {@link Pageable} adapter for the given PDF document using
     * a default printer job returned by {@link PrinterJob#getPrinterJob()}.
     *
     * @param document PDF document
     * @throws IllegalArgumentException if the argument is <code>null</code>
     * @throws PrinterException if the document permissions prevent printing
     */
    public PDPageable(PDDocument document)
            throws IllegalArgumentException, PrinterException {
        this(document, PrinterJob.getPrinterJob());
    }

    /**
     * Returns the printer job for printing the given PDF document.
     *
     * @return printer job
     */
    public PrinterJob getPrinterJob() {
        return job;
    }

    //------------------------------------------------------------< Pageable >

    /**
     * Returns the number of pages in the given PDF document.
     *
     * @return number of pages
     */
    public int getNumberOfPages() {
        return pages.size();
    }

    /**
     * Returns the format of the page at the given index.
     *
     * @param i page index, zero-based
     * @return page format
     * @throws IndexOutOfBoundsException if the page index is invalid
     */
    public PageFormat getPageFormat(int i) throws IndexOutOfBoundsException {
        PageFormat format = job.defaultPage();

        PDPage page = pages.get(i); // can throw IOOBE
        Dimension media = page.findMediaBox().createDimension();
        Dimension crop = page.findCropBox().createDimension();

        // Center the ImageableArea if the crop is smaller than the media
        double diffWidth = 0.0;
        double diffHeight = 0.0;
        if (!media.equals(crop)) {
            diffWidth = (media.getWidth() - crop.getWidth()) / 2.0;
            diffHeight = (media.getHeight() - crop.getHeight()) / 2.0;
        }

        Paper paper = format.getPaper();
        PrintService service = job.getPrintService(); // can be null
        Class<OrientationRequested> orientation = OrientationRequested.class;
        if (service != null
                && service.getDefaultAttributeValue(orientation) == LANDSCAPE) {
           format.setOrientation(PageFormat.LANDSCAPE);
           paper.setImageableArea(
                   diffHeight, diffWidth, crop.getHeight(), crop.getWidth());
           paper.setSize(media.getHeight(), media.getWidth());
        } else {
           format.setOrientation(PageFormat.PORTRAIT);
           paper.setImageableArea(
                   diffWidth, diffHeight, crop.getWidth(), crop.getHeight());
           paper.setSize(media.getWidth(), media.getHeight());
        }
        format.setPaper(paper);

        return format;
    }

    /**
     * Returns a {@link Printable} for the page at the given index.
     * Currently this method simply returns the underlying {@link PDPage}
     * object that directly implements the {@link Printable} interface, but
     * future versions may choose to return a different adapter instance.
     *
     * @param i page index, zero-based
     * @return printable
     * @throws IndexOutOfBoundsException if the page index is invalid
     */
    public Printable getPrintable(int i) throws IndexOutOfBoundsException {
        return pages.get(i);
    }

    //-----------------------------------------------------------< Printable >

    /**
     * Prints the page at the given index.
     *
     * @param graphics printing target
     * @param format page format
     * @param i page index, zero-based
     * @return {@link Printable#PAGE_EXISTS} if the page was printed,
     *         or {@link Printable#NO_SUCH_PAGE} if page index was invalid
     * @throws PrinterException if printing failed
     */
    @SuppressWarnings("deprecation")
    public int print(Graphics graphics, PageFormat format, int i)
            throws PrinterException {
        if (0 <= i && i < pages.size()) {
            return pages.get(i).print(graphics, format, i);
        } else {
            return NO_SUCH_PAGE;
        }
    }

}
