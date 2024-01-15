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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.Overlay.Position;
import org.apache.pdfbox.pdmodel.PDDocument;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * Adds an overlay to an existing PDF document.
 *  
 * Based on code contributed by Balazs Jerk. 
 * 
 */
@Command(name = "overlaypdf", header = "Adds an overlay to a PDF document", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class OverlayPDF implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = "-odd", description = "overlay file used for odd pages")
    private File oddPageOverlay;

    @Option(names = "-even", description = "overlay file used for even pages")
    private File evenPageOverlay;

    @Option(names = "-first", description = "overlay file used for the first page")
    private File firstPageOverlay;

    @Option(names = "-last", description = "overlay file used for the last page")
    private File lastPageOverlay;

    @Option(names = "-useAllPages", description = "overlay file used for overlay, all pages are used by simply repeating them")
    private File useAllPages;

    @Option(names = "-page", description = "overlay file used for the given page number, may occur more than once")    
    Map<Integer, String> specificPageOverlayFile = new HashMap<>();

    @Option(names = {"-default"}, description = "the default overlay file")
    private File defaultOverlay;

    @Option(names = "-position", description = "where to put the overlay file: foreground or background (default: ${DEFAULT-VALUE})")    
    private Position position = Position.BACKGROUND;

    @Option(names = {"-i", "--input"}, description = "the PDF input file", required = true)
    private File infile;

    @Option(names = {"-o", "--output"}, description = "the PDF output file", required = true)
    private File outfile;

    /**
     * Constructor.
     */
    public OverlayPDF()
    {
        SYSERR = System.err;
    }

    /**
     * This will overlay a document and write out the results.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new OverlayPDF()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call()
    {
        int retcode = 0;

        Overlay overlayer = new Overlay();
        overlayer.setOverlayPosition(position);

        if (firstPageOverlay != null)
        {
            overlayer.setFirstPageOverlayFile(firstPageOverlay.getAbsolutePath());
        }

        if (lastPageOverlay != null)
        {
            overlayer.setLastPageOverlayFile(lastPageOverlay.getAbsolutePath());
        }

        if (oddPageOverlay != null)
        {
            overlayer.setOddPageOverlayFile(oddPageOverlay.getAbsolutePath());
        }

        if (evenPageOverlay != null)
        {
            overlayer.setEvenPageOverlayFile(evenPageOverlay.getAbsolutePath());
        }

        if (useAllPages != null)
        {
            overlayer.setAllPagesOverlayFile(useAllPages.getAbsolutePath());
        }

        if (defaultOverlay != null)
        {
            overlayer.setDefaultOverlayFile(defaultOverlay.getAbsolutePath());
        }

        if (infile != null)
        {
            overlayer.setInputFile(infile.getAbsolutePath());
        }


        try (PDDocument result = overlayer.overlay(specificPageOverlayFile))
        {
            result.save(outfile);
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error adding overlay(s) to PDF [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        finally
        {
            // close the input files AFTER saving the resulting file as some 
            // streams are shared among the input and the output files
            try
            {
                overlayer.close();
            }
            catch (IOException ioe)
            {
                SYSERR.println( "Error adding overlay(s) to PDF [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
                retcode = 4;
            }
        }

        return retcode;
    }
}
