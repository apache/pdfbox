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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This program will just save the loaded pdf without any changes. As PDFBox doesn't support writing compressed object
 * streams those streams are stripped and will be gone in the resulting file. This is very helpful when trying to debug
 * problems as it'll make it possible to easily look through a PDF using a text editor. It also exposes problems which
 * stem from objects inside object streams overwriting other objects.
 * 
 * @author Adam Nichols
 */
@Command(name = "DecompressObjectstreams", header = "Decompresses object streams in a PDF file.")
public final class DecompressObjectstreams implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    
    @Option(names = {"-i", "--input"}, description = "the PDF file to decompress", required = true)
    private File infile;

    @Option(names = {"-o", "--output"}, description = "the decompressed PDF file. If omitted the original file is overwritten.")
    private File outfile;
    
    /**
     * Constructor.
     */
    public DecompressObjectstreams()
    {
        SYSERR = System.err;
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new DecompressObjectstreams()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        try (PDDocument doc = Loader.loadPDF(infile))
        {
            // overwrite inputfile if no outputfile was specified
            if (outfile == null) {
                outfile = infile;
            }

            doc.save(outfile, CompressParameters.NO_COMPRESSION);
        }
        catch (IOException ioe)
        {
            SYSERR.println("Error processing file [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }
}
