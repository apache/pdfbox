/**
 * Copyright (c) 2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox;

import java.awt.print.PrinterJob;

import javax.print.PrintService;

import org.pdfbox.pdmodel.PDDocument;

/**
 * This is a command line program that will print a PDF document.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PrintPDF
{

    private static final String PASSWORD     = "-password";
    private static final String SILENT       = "-silentPrint";
    private static final String PRINTER_NAME = "-printerName";

    /**
     * private constructor.
    */
    private PrintPDF()
    {
        //static class
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        String password = "";
        String pdfFile = null;
        boolean silentPrint = false;
        String printerName = null;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( PRINTER_NAME ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                printerName = args[i];
            }
            else if( args[i].equals( SILENT ) )
            {
                silentPrint = true;
            }
            else
            {
                pdfFile = args[i];
            }
        }

        if( pdfFile == null )
        {
            usage();
        }

        PDDocument document = null;
        try
        {
            document = PDDocument.load( pdfFile );

            if( document.isEncrypted() )
            {
                document.decrypt( password );
            }
            PrinterJob printJob = PrinterJob.getPrinterJob();
            
            if(printerName != null )
            {
                PrintService[] printService = PrinterJob.lookupPrintServices();
                boolean printerFound = false;
                for(int i = 0; !printerFound && i < printService.length; i++)
                {
                    if(printService[i].getName().indexOf(printerName) != -1)
                    {
                        printJob.setPrintService(printService[i]);
                        printerFound = true;
                    }
                }
            }
            
            if( silentPrint )
            {
                document.silentPrint( printJob );
            }
            else
            {
                document.print( printJob );
            }
        }
        finally
        {
            if( document != null )
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
        System.err.println( "Usage: java org.pdfbox.PrintPDF [OPTIONS] <PDF file>\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -silentPrint                 Print without prompting for printer info\n"
            );
        System.exit( 1 );
    }
}