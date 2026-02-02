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

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This is the main program that will take a list of pdf documents and merge them,
 * saving the result in a new document.
 *
 * @author Ben Litchfield
 */
@Command(name = "merge", header = "Merges multiple PDF documents into one", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class PDFMerger implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = {"-i", "--input"}, description = "the PDF files to merge.", paramLabel = "<infile>", required = true)
    private File[] infiles;

    @Option(names = {"-o", "--output"}, description = "the merged PDF file.", required = true)
    private File outfile;

    /**
     * Constructor.
     */
    public PDFMerger()
    {
        SYSERR = System.err;
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be at least 3.
     */
    public static void main( String[] args )
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new PDFMerger()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        PDFMergerUtility merger = new PDFMergerUtility();

        try
        {
            for (File infile : infiles)
            {
                merger.addSource(infile);
            }

            merger.setDestinationFileName(outfile.getAbsolutePath());
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error merging documents [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
