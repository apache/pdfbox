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

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This will take a PDF document and export the AcroForm form data to FDF.
 *
 * @author Ben Litchfield
 */
@Command(name = "exportfdf", header = "Exports AcroForm form data to FDF", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class ExportFDF implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = {"-i", "--input"}, description = "the PDF file to export", required = true)
    private File infile;

    @Option(names = {"-o", "--output"}, description = "the FDF data file", required = true)
    private File outfile;
   
    /**
     * Constructor.
     */
    public ExportFDF()
    {
        SYSERR = System.err;
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new ExportFDF()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        try (PDDocument pdf = Loader.loadPDF(infile))
        {
            PDAcroForm form = pdf.getDocumentCatalog().getAcroForm();
            if( form == null )
            {
                SYSERR.println( "Error: This PDF does not contain a form." );
                return 1;
            }
            else
            {
                if (outfile == null)
                {
                    String outPath = FilenameUtils.removeExtension(infile.getAbsolutePath()) + ".fdf";
                    outfile = new File(outPath);
                }
                try (FDFDocument fdf = form.exportFDF())
                {
                    fdf.save( outfile );
                }
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error exporting FDF data [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
