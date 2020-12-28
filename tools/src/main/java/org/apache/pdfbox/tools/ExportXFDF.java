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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import org.apache.pdfbox.pdmodel.fdf.FDFDocument;

/**
 * This will take a PDF document and export the AcroForm form data to FDF.
 *
 * @author Ben Litchfield
 */
@Command(name = "exportxfdf", description = "Exports AcroForm form data to XFDF", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class ExportXFDF implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Parameters(paramLabel = "inputfile", index = "0", arity = "1", description = "the PDF file to export.")
    private File infile;

    @Parameters(paramLabel = "outputfile", index = "1", arity = "0..1", description = "the XFDF data file.")
    private File outfile;
    
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

        int exitCode = new CommandLine(new ExportXFDF()).execute(args);
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
            }
            else
            {
                if (outfile == null)
                {
                    String outPath = FilenameUtils.removeExtension(infile.getAbsolutePath()) + ".xfdf";
                    outfile = new File(outPath);
                }
                
                try (FDFDocument fdf = form.exportFDF())
                {
                    fdf.saveXFDF(outfile);
                }
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error exporting XFDF data: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
