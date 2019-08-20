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
import java.util.HashMap;
import java.util.Map;
import javax.print.PrintService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;

/**
 * This is a command line program that will print a PDF document.
 * 
 * @author Ben Litchfield
 */
public final class PrintPDF
{
    @SuppressWarnings({"squid:S2068"})
    private static final String PASSWORD = "-password";
    private static final String SILENT = "-silentPrint";
    private static final String PRINTER_NAME = "-printerName";
    private static final String ORIENTATION = "-orientation";
    private static final String BORDER = "-border";
    private static final String DPI = "-dpi";
    private static final String NOCOLOROPT = "-noColorOpt";

    /**
     * private constructor.
     */
    private PrintPDF()
    {
        // static class
    }

    /**
     * Infamous main method.
     * 
     * @param args Command line arguments, should be one and a reference to a file.
     * @throws PrinterException if the specified service cannot support the Pageable and Printable interfaces.
     * @throws IOException if there is an error parsing the file.
     */
    public static void main(String[] args) throws PrinterException, IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        @SuppressWarnings({"squid:S2068"})
        String password = "";
        String pdfFile = null;
        boolean silentPrint = false;
        String printerName = null;
        Orientation orientation = Orientation.AUTO;
        boolean showPageBorder = false;
        int dpi = 0;
        Map <String,Orientation> orientationMap = new HashMap<String,Orientation>();
        orientationMap.put("auto", Orientation.AUTO);
        orientationMap.put("landscape", Orientation.LANDSCAPE);
        orientationMap.put("portrait", Orientation.PORTRAIT);
        RenderingHints renderingHints = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(PASSWORD))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                password = args[i];
            }
            else if (args[i].equals(PRINTER_NAME))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                printerName = args[i];
            }
            else if (args[i].equals(SILENT))
            {
                silentPrint = true;
            }
            else if (args[i].equals(ORIENTATION))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                orientation = orientationMap.get(args[i]);
                if (orientation == null)
                {
                    usage();
                }
            }
            else if (args[i].equals(BORDER))
            {
                showPageBorder = true;
            }
            else if (args[i].equals(NOCOLOROPT))
            {
                renderingHints = new RenderingHints(null);
                renderingHints.put(RenderingHints.KEY_INTERPOLATION,
                                   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                renderingHints.put(RenderingHints.KEY_RENDERING,
                                   RenderingHints.VALUE_RENDER_QUALITY);
                renderingHints.put(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            else if (args[i].equals(DPI))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                dpi = Integer.parseInt(args[i]);
            }
            else
            {
                pdfFile = args[i];
            }
        }

        if (pdfFile == null)
        {
            usage();
        }

        PDDocument document = null;
        try
        {
            document = PDDocument.load(new File(pdfFile), password);

            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canPrint())
            {
                throw new IOException("You do not have permission to print");
            }

            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setJobName(new File(pdfFile).getName());

            if (printerName != null)
            {
                PrintService[] printServices = PrinterJob.lookupPrintServices();
                boolean printerFound = false;
                for (int i = 0; !printerFound && i < printServices.length; i++)
                {
                    if (printServices[i].getName().equals(printerName))
                    {
                        printJob.setPrintService(printServices[i]);
                        printerFound = true;
                    }
                }
                if (!printerFound)
                {
                    System.err.println("printer '" + printerName + "' not found, using default");
                    showAvailablePrinters();
                }
            }
            PDFPageable pageable = new PDFPageable(document, orientation, showPageBorder, dpi);
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
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar PrintPDF [options] <inputfile>\n"
                + "\nOptions:\n"
                + "  -password  <password>                : Password to decrypt document\n"
                + "  -printerName <name>                  : Print to specific printer\n"
                + "  -orientation auto|portrait|landscape : Print using orientation\n"
                + "                                           (default: auto)\n"
                + "  -border                              : Print with border\n"
                + "  -dpi                                 : Render into intermediate image with\n"
                + "                                           specific dpi and then print\n"
                + "  -noColorOpt                          : Disable color optimizations\n"
                + "                                           (useful when printing barcodes)\n"
                + "  -silentPrint                         : Print without printer dialog box\n";
        System.err.println(message);
        showAvailablePrinters();
        System.exit(1);
    }

    private static void showAvailablePrinters()
    {
        System.err.println("Available printer names:");
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices)
        {
            System.err.println("    " + printService.getName());
        }
    }
}
