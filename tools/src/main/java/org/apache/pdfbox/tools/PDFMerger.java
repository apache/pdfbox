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

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * This is the main program that will take a list of pdf documents and merge them,
 * saving the result in a new document.
 *
 * @author Ben Litchfield
 */
@Command(name = "PDFMerger", description = "Merge multiple PDFs into one.")
public final class PDFMerger implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Parameters(paramLabel = "inputfile", arity = "2..*", description = "the PDF files to merge.")
    private File[] infiles;

    @Parameters(paramLabel = "outputfile", index="1", description = "the merged PDF file.")
    private File outfile;

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be at least 3.
     */
    public static void main(final String[] args )
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        final int exitCode = new CommandLine(new PDFMerger()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        final PDFMergerUtility merger = new PDFMergerUtility();

        try
        {
            for (final File infile : infiles)
            {
                merger.addSource(infile);
            }

            merger.setDestinationFileName(outfile.getAbsolutePath());
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        }
        catch (final IOException ioe)
        {
            SYSERR.println( "Error merging documents: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
