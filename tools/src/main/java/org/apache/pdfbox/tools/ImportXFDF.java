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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


/**
 * This example will take a PDF document and fill the fields with data from the
 * XFDF fields.
 *
 * @author Ben Litchfield
 */
@Command(name = "ImportFDF", description = "Import AcroForm form data from XFDF.")
public class ImportXFDF
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Parameters(paramLabel = "pdffile", index = "0", arity = "1", description = "the PDF file to import to.")
    private File infile;

    @Parameters(paramLabel = "xfdffile", index = "1", arity = "1", description = "the XFDF data file to import from.")
    private File xfdffile;

    @Parameters(paramLabel = "outputfile", index = "2", arity = "0..1", description = "the PDF file to save to. If omitted the orginal PDF will be used.")
    private File outfile;

    /**
     * This will takes the values from the fdf document and import them into the
     * PDF document.
     *
     * @param pdfDocument The document to put the fdf data into.
     * @param fdfDocument The FDF document to get the data from.
     *
     * @throws IOException If there is an error setting the data in the field.
     */
    public void importFDF(final PDDocument pdfDocument, final FDFDocument fdfDocument ) throws IOException
    {
        final PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        final PDAcroForm acroForm = docCatalog.getAcroForm();
        acroForm.setCacheFields( true );
        acroForm.importFDF( fdfDocument );
    }

    /**
     * This will import an fdf document and write out another pdf.
     * <br>
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        final int exitCode = new CommandLine(new ImportXFDF()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        final ImportFDF importer = new ImportFDF();
        try (PDDocument pdf = Loader.loadPDF(infile);
             final FDFDocument fdf = Loader.loadXFDF(xfdffile))
        {
            importer.importFDF( pdf, fdf );

            if (outfile == null)
            {
                outfile = infile;
            }

            pdf.save(outfile);
        }
        catch (final IOException ioe)
        {
            SYSERR.println( "Error importing XFDF data: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
