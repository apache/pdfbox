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

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;

/**
 * This example will take a PDF document and fill the fields with data from the
 * FDF fields.
 *
 * @author Ben Litchfield
 */
public class ExportFDF
{
    /**
     * Creates a new instance of ImportFDF.
     */
    public ExportFDF()
    {
    }

    /**
     * This will import an fdf document and write out another pdf.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     *
     * @throws Exception If there is an error importing the FDF document.
     */
    public static void main(String[] args) throws Exception
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        ExportFDF exporter = new ExportFDF();
        exporter.exportFDF( args );
    }

    private void exportFDF( String[] args ) throws Exception
    {
        PDDocument pdf = null;
        FDFDocument fdf = null;

        try
        {
            if( args.length != 1 && args.length != 2 )
            {
                usage();
            }
            else
            {
                pdf = PDDocument.load( new File(args[0]) );
                PDAcroForm form = pdf.getDocumentCatalog().getAcroForm();
                if( form == null )
                {
                    System.err.println( "Error: This PDF does not contain a form." );
                }
                else
                {
                    String fdfName = null;
                    if( args.length == 2 )
                    {
                        fdfName = args[1];
                    }
                    else
                    {
                        if( args[0].length() > 4 )
                        {
                            fdfName = args[0].substring( 0, args[0].length() -4 ) + ".fdf";
                        }
                    }
                    fdf = form.exportFDF();
                    fdf.save( fdfName );
                }
            }
        }
        finally
        {
            close( fdf );
            close( pdf );
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: org.apache.pdfbox.ExortFDF <pdf-file> [output-fdf-file]" );
        System.err.println( "    [output-fdf-file] - Default is pdf name, test.pdf->test.fdf" );
    }

    /**
     * Close the document.
     *
     * @param doc The doc to close.
     *
     * @throws IOException If there is an error closing the document.
     */
    public void close( FDFDocument doc ) throws IOException
    {
        if( doc != null )
        {
            doc.close();
        }
    }

    /**
     * Close the document.
     *
     * @param doc The doc to close.
     *
     * @throws IOException If there is an error closing the document.
     */
    public void close( PDDocument doc ) throws IOException
    {
        if( doc != null )
        {
            doc.close();
        }
    }
}
