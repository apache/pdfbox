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
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import picocli.CommandLine;

/**
 * Extract the XMP metadata from the document or from a page.
 *
 * @author Tilman Hausherr
 */
@CommandLine.Command(name = "extractxmp", header = "Extracts the xmp stream from a PDF document", versionProvider = Version.class, mixinStandardHelpOptions = true)
public class ExtractXMP implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSOUT;
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @CommandLine.Option(names = "-page", description = "extract the XMP information from a specific page (1 based)")
    private int page = 0;

    @CommandLine.Option(names = "-password", description = "the password for the PDF or certificate in keystore.", arity = "0..1", interactive = true)    
    private String password = "";

    @CommandLine.Option(names = "-console", description = "Send text to console instead of file")
    private boolean toConsole = false;

    @CommandLine.Option(names = {"-i", "--input"}, description = "the PDF file", required = true)
    private File infile;

    @CommandLine.Option(names = {"-o", "--output"}, description = "the exported text file")
    private File outfile;

    /**
     * Constructor.
     */
    public ExtractXMP()
    {
        SYSOUT = System.out;
        SYSERR = System.err;
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new ExtractXMP()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Starts the xmp extraction.
     */
    @Override
    public Integer call()
    {
        if (outfile == null)
        {
            String outPath = FilenameUtils.removeExtension(infile.getAbsolutePath()) + ".xml";
            outfile = new File(outPath);
        }
        
        try (PDDocument document = Loader.loadPDF(infile, password))
        {
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata meta;
            if (page == 0)
            {
                meta = catalog.getMetadata();
            }
            else
            {
                if (page > document.getNumberOfPages())
                {
                    SYSERR.println("Page " + page + " doesn't exist");
                    return 1;
                }
                meta = document.getPage(page - 1).getMetadata();
            }
            if (meta == null)
            {
                SYSERR.println("No XMP metadata available");
                return 1;
            }
            try (PrintStream ps = toConsole ? SYSOUT : new PrintStream(outfile))
            {
                ps.write(meta.toByteArray());
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error extracting text for document [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }

        return 0;
    }    
}
