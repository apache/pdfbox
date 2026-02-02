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
import java.io.PrintStream;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This example will take a PDF document and fill the fields with data from the
 * FDF fields.
 *
 * @author Ben Litchfield
 */
@Command(name = "importfdf", header = "Imports AcroForm form data from FDF", versionProvider = Version.class, mixinStandardHelpOptions = true)
public class ImportFDF implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;
    
    @Option(names = {"-i", "--input"}, description = "the PDF file to import to", required = true)
    private File infile;

    @Option(names = {"-o", "--output"}, description = "the PDF file to save to. If omitted the original file will be used")
    private File outfile;

    @Option(names = {"--data"}, description = "the FDF data file to import from", required = true)
    private File fdffile;
    /**
     * This will takes the values from the fdf document and import them into the
     * PDF document.
     *
     * @param pdfDocument The document to put the fdf data into.
     * @param fdfDocument The FDF document to get the data from.
     *
     * @throws IOException If there is an error setting the data in the field.
     */
    public void importFDF( PDDocument pdfDocument, FDFDocument fdfDocument ) throws IOException
    {
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        if (acroForm == null)
        {
            return;
        }
        acroForm.setCacheFields( true );
        acroForm.importFDF( fdfDocument );
        
        //TODO this can be removed when we create appearance streams
        acroForm.setNeedAppearances(true);
    }

    /**
     * Constructor.
     */
    public ImportFDF()
    {
        SYSERR = System.err;
    }

    /**
     * This will import an fdf document and write out another pdf. <br>
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new ImportFDF()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        ImportFDF importer = new ImportFDF();

        try (PDDocument pdf = Loader.loadPDF(infile);
                FDFDocument fdf = Loader.loadFDF(fdffile))
        {
            importer.importFDF( pdf, fdf );

            if (outfile == null)
            {
                outfile = infile;
            }

            pdf.save(outfile);
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error importing FDF data [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
