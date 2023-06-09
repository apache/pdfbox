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
package org.apache.pdfbox.tools;

import java.awt.RenderingHints;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.print.DocFlavor;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.Sides;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This is a command line program that will print a PDF document.
 * 
 * @author Ben Litchfield
 */
@Command(name = "printpdf", header = "Prints a PDF document", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class PrintPDF implements Callable<Integer>
{
    // We need this helper class because the Sides class isn't a real enum class.
    enum Duplex
    {
        SIMPLEX(0), DUPLEX(1), TUMBLE(2), DOCUMENT(3);

        int num;

        Duplex(int p)
        {
            num = p;
        }
        
        Sides toSides()
        {
            switch (num)
            {
                case 0:
                    return Sides.ONE_SIDED;
                case 1:
                    return Sides.DUPLEX;
                case 2:
                    return Sides.TUMBLE;
                default:
                    return null;
            }
        }
    }

    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Option(names = "-password", description = "the password to decrypt the document.", arity = "0..1", interactive = true)    
    private String password;

    @Option(names = "-silentPrint", description = "print without printer dialog box.")    
    private boolean silentPrint;

    @Option(names = "-printerName", description = "print to specific printer.")    
    private String printerName;

    @Option(names = "-orientation", description = "print using orientation [${COMPLETION-CANDIDATES}] (default: ${DEFAULT-VALUE}).")    
    private Orientation orientation = Orientation.AUTO;

    @Option(names = "-duplex", description = "print using duplex [${COMPLETION-CANDIDATES}] (default: ${DEFAULT-VALUE}).")    
    private Duplex duplex = Duplex.DOCUMENT;

    @Option(names = "-tray", description = "print using tray.")    
    private String tray;

    @Option(names = "-mediaSize", description = "print using media size name.")    
    private String mediaSize;

    @Option(names = "-border", description = "print with border.")    
    private boolean border;

    @Option(names = "-dpi", description = "render into intermediate image with specific dpi and then print")
    private int dpi;

    @Option(names = "-noColorOpt", description = "disable color optimizations (useful when printing barcodes)")
    private boolean noColorOpt;

    @Option(names = {"-i", "--input"}, description = "the PDF files to print.", required = true)
    private File infile;

    /**
     * Infamous main method.
     * 
     * @param args Command line arguments, should be one and a reference to a file.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");
        int exitCode = new CommandLine(new PrintPDF()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call()
    {
        RenderingHints renderingHints = null;

        if (noColorOpt)
        {
            renderingHints = new RenderingHints(null);
            renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        try (PDDocument document = Loader.loadPDF(infile, password))
        {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canPrint())
            {
                throw new IOException("You do not have permission to print");
            }

            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setJobName(infile.getName());

            if (printerName != null)
            {
                PrintService[] printServices = PrinterJob.lookupPrintServices();
                boolean printerFound = false;
                for (PrintService printService : printServices)
                {
                    if (printService.getName().equals(printerName))
                    {
                        printJob.setPrintService(printService);
                        printerFound = true;
                        break;
                    }
                }
                if (!printerFound)
                {
                    SYSERR.println("printer '" + printerName + "' not found, using default '" +
                            printJob.getPrintService().getName() + "'");
                    showAvailablePrinters();
                }
            }

            PrintService printService = printJob.getPrintService();
            PrintRequestAttributeSet pras = createPrintRequestAttributeSet(document);

            if (tray != null)
            {
                // find the object with the same name
                boolean found = false;
                for (Media media : getTraysFromPrintService(printService))
                {
                    if (tray.equals(media.toString()))
                    {
                        pras.add(media);
                        found = true;
                        break;
                    }                            
                }
                if (!found)
                {
                    SYSERR.println("Tray '" + tray + "' not supported, ignored. Valid values: " +
                            getTraysFromPrintService(printService));
                }
            }

            if (mediaSize != null)
            {
                // find the object with the same name
                boolean found = false;
                for (Media media : getMediaSizesFromPrintService(printService))
                {
                    if (mediaSize.equals(media.toString()))
                    {
                        pras.add(media);
                        found = true;
                        break;
                    }                            
                }
                if (!found)
                {
                    SYSERR.println("media size '" + mediaSize + "' not supported, ignored. Valid values: " +
                            getMediaSizesFromPrintService(printService));
                }
            }

            PDFPageable pageable = new PDFPageable(document, orientation, border, dpi);
            pageable.setRenderingHints(renderingHints);
            printJob.setPageable(pageable);

            // We're not using PDFPrintable, because then
            // "the PageFormat for each page is the default page format"
            // which results in the image appearing in the middle of the page, and padded
            // when printing on XPS. Also PDFPageable.getPageFormat() won't be called.

            if (silentPrint || printJob.printDialog(pras))
            {
                printJob.print(pras);
            }
        }
        catch (IOException | PrinterException ex)
        {
            SYSERR.println("Error printing document [" + ex.getClass().getSimpleName() + "]: " + ex.getMessage());
            return 4;
        }
        return 0;
    }

    private static List<Media> getTraysFromPrintService(PrintService printService)
    {
        Media[] medias = (Media[]) printService.getSupportedAttributeValues(
                Media.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
        if (medias == null)
        {
            return Collections.emptyList();
        }
        List<Media> trayList = new ArrayList<>();
        for (Media media : medias)
        {
            if (media instanceof MediaTray)
            {
                trayList.add(media);
            }
        }
        return trayList;
    }

    private static List<Media> getMediaSizesFromPrintService(PrintService printService)
    {
        Media[] medias = (Media[]) printService.getSupportedAttributeValues(
                Media.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
        if (medias == null)
        {
            return Collections.emptyList();
        }
        List<Media> sizeList = new ArrayList<>();
        for (Media media : medias)
        {
            if (media instanceof MediaSizeName)
            {
                sizeList.add(media);
            }
        }
        return sizeList;
    }

    private PrintRequestAttributeSet createPrintRequestAttributeSet(final PDDocument document)
    {
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        if (duplex.toSides() == null)
        {
            PDViewerPreferences vp = document.getDocumentCatalog().getViewerPreferences();
            if (vp != null && vp.getDuplex() != null)
            {
                String dp = vp.getDuplex();
                if (PDViewerPreferences.DUPLEX.DuplexFlipLongEdge.toString().equals(dp))
                {
                    pras.add(Sides.TWO_SIDED_LONG_EDGE);
                }
                else if (PDViewerPreferences.DUPLEX.DuplexFlipShortEdge.toString().equals(dp))
                {
                    pras.add(Sides.TWO_SIDED_SHORT_EDGE);
                }
                else if (PDViewerPreferences.DUPLEX.Simplex.toString().equals(dp))
                {
                    pras.add(Sides.ONE_SIDED);
                }
            }
        }
        else
        {
            pras.add(duplex.toSides());
        }
        return pras;
    }

    @Command(name = "listPrinters", description = "list available printers", helpCommand = true)
    private static void showAvailablePrinters()
    {
        SYSERR.println("Available printer names:");
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices)
        {
            SYSERR.println("    " + printService.getName());
            SYSERR.println("        Sizes: " + getMediaSizesFromPrintService(printService));
            SYSERR.println("        Trays: " + getTraysFromPrintService(printService));
        }
    }
}
