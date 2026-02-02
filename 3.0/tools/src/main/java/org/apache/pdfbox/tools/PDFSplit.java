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

/**
 * This is the main program that will take a pdf document and split it into
 * a number of other documents.
 *
 * @author Ben Litchfield
 */
@Command(name = "pdfsplit", header = "Splits a PDF document into number of new documents", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class PDFSplit implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = "-password", description = "the password to decrypt the document.", arity = "0..1", interactive = true)    
    private String password;

    @Option(names = "-split", description = "split after this many pages (default 1, if startPage and endPage are unset).")    
    private int split = -1;

    @Option(names = "-startPage", description = "start page.")    
    private int startPage = -1;

    @Option(names = "-endPage", description = "end page.")    
    private int endPage = -1;

    @Option(names = "-outputPrefix", description = "the filename prefix for split files.")    
    private String outputPrefix;

    @Option(names = {"-i", "--input"}, description = "the PDF file to split", required = true)
    private File infile;

    /**
     * Constructor.
     */
    public PDFSplit()
    {
        SYSERR = System.err;
    }

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
            boolean startEndPageSet = false;
            if (startPage != -1)
            {
                splitter.setStartPage(startPage);
                startEndPageSet = true;
                if (split == -1)
                {
                    int numberOfPages = document.getNumberOfPages();
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
            SYSERR.println( "Error splitting document [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        finally
        {
            if (documents != null)
            {
                documents.forEach(IOUtils::closeQuietly);
            }
        }
        return 0;
    }
}
