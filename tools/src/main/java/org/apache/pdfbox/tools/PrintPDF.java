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
import java.util.concurrent.Callable;

import javax.print.PrintService;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
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
                for (int i = 0; i < printServices.length; i++)
                {
                    if (printServices[i].getName().equals(printerName))
                    {
                        printJob.setPrintService(printServices[i]);
                        printerFound = true;
                        break;
                    }
                }
                if (!printerFound)
                {
                    SYSERR.println("printer '" + printerName + "' not found, using default");
                    showAvailablePrinters();
                }
            }
            PDFPageable pageable = new PDFPageable(document, orientation, border, dpi);
            pageable.setRenderingHints(renderingHints);
            printJob.setPageable(pageable);

            // We're not using PDFPrintable, because then
            // "the PageFormat for each page is the default page format"
            // which results in the image appearing in the middle of the page, and padded
            // when printing on XPS. Also PDFPageable.getPageFormat() won't be called.

            if (silentPrint || printJob.printDialog())
            {
                printJob.print();
            }
        }
        catch (IOException | PrinterException ex)
        {
            SYSERR.println("Error printing document [" + ex.getClass().getSimpleName() + "]: " + ex.getMessage());
            return 4;
        }
        return 0;
    }

    @Command(name = "listPrinters", description = "list available printers", helpCommand = true)
    private static void showAvailablePrinters()
    {
        SYSERR.println("Available printer names:");
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices)
        {
            SYSERR.println("    " + printService.getName());
        }
    }
}
