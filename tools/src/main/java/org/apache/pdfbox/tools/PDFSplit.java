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
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * This is the main program that will take a pdf document and split it into
 * a number of other documents.
 *
 * @author Ben Litchfield
 */
@Command(name = "PDFSplit", description = "Split a PDF into number of new documents.")
public final class PDFSplit implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = "-password", description = "the password to decrypt the document.")    
    private String password;

    @Option(names = "-split", description = "split after this many pages (default 1, if startPage and endPage are unset).")    
    private final int split = -1;

    @Option(names = "-startPage", description = "start page.")    
    private final int startPage = -1;

    @Option(names = "-endPage", description = "end page.")    
    private final int endPage = -1;

    @Option(names = "-outputPrefix", description = "the filename prefix for split files.")    
    private String outputPrefix;

    @Parameters(paramLabel = "inputfile", description = "the PDF file to split.")
    private File infile;

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     */
    public static void main( String[] args )
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new PDFSplit()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        Splitter splitter = new Splitter();

        if (outputPrefix == null)
        {
            outputPrefix = FilenameUtils.removeExtension(infile.getAbsolutePath());
        }

        List<PDDocument> documents = null;

        try (PDDocument document = Loader.loadPDF(infile, password))
        {
            int numberOfPages = document.getNumberOfPages();
            boolean startEndPageSet = false;
            if (startPage != -1)
            {
                splitter.setStartPage(startPage);
                startEndPageSet = true;
                if (split == -1)
                {
                    splitter.setSplitAtPage(numberOfPages);
                }
            }
            if (endPage != -1)
            {
                splitter.setEndPage(endPage);
                startEndPageSet = true;
                if (split == -1)
                {
                    splitter.setSplitAtPage(endPage);
                }
            }
            if (split != -1)
            {
                splitter.setSplitAtPage(split);
            }
            else 
            {
                if (!startEndPageSet)
                {
                    splitter.setSplitAtPage(1);
                }
            }
                
            documents = splitter.split( document );
            for( int i=0; i<documents.size(); i++ )
            {
                try (PDDocument doc = documents.get(i))
                {
                    doc.save(outputPrefix + "-" + (i + 1) + ".pdf");
                }
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error splitting document: " + ioe.getMessage());
            return 4;
        }
        finally
        {
            for( int i=0; documents != null && i<documents.size(); i++ )
            {
                PDDocument doc = documents.get(i);
                IOUtils.closeQuietly(doc);
            }
        }
        return 0;
    }
}
