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

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.multipdf.Splitter;

/**
 * This is the main program that will take a pdf document and split it into
 * a number of other documents.
 *
 * @author Ben Litchfield
 */
public final class PDFSplit
{
    @SuppressWarnings({"squid:S2068"})
    private static final String PASSWORD = "-password";
    private static final String SPLIT = "-split";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String OUTPUT_PREFIX = "-outputPrefix";

    private PDFSplit()
    {
    }
    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        PDFSplit split = new PDFSplit();
        split.split( args );
    }

    private void split( String[] args ) throws IOException
    {
        @SuppressWarnings({"squid:S2068"})
        String password = "";
        String split = null;
        String startPage = null;
        String endPage = null;
        Splitter splitter = new Splitter();
        String pdfFile = null;
        String outputPrefix = null;
        for( int i=0; i<args.length; i++ )
        {
            switch (args[i])
            {
                case PASSWORD:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    password = args[i];
                    break;
                case SPLIT:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    split = args[i];
                    break;
                case START_PAGE:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    startPage = args[i];
                    break;
                case END_PAGE:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    endPage = args[i];
                    break;
                case OUTPUT_PREFIX:
                    i++;
                    outputPrefix = args[i];
                    break;
                default:
                    if (pdfFile == null)
                    {
                        pdfFile = args[i];
                    }
                    break;
            }
        }

        if( pdfFile == null )
        {
            usage();
        }
        else
        {          
            if (outputPrefix == null)
            {
                outputPrefix = pdfFile.substring(0, pdfFile.lastIndexOf('.'));
            }
            PDDocument document = null;
            List<PDDocument> documents = null;
            try
            {
                document = PDDocument.load(new File(pdfFile), password);

                int numberOfPages = document.getNumberOfPages();
                boolean startEndPageSet = false;
                if (startPage != null)
                {
                    splitter.setStartPage(Integer.parseInt( startPage ));
                    startEndPageSet = true;
                    if (split == null)
                    {
                        splitter.setSplitAtPage(numberOfPages);
                    }
                }
                if (endPage != null)
                {
                    splitter.setEndPage(Integer.parseInt( endPage ));
                    startEndPageSet = true;
                    if (split == null)
                    {
                        splitter.setSplitAtPage(Integer.parseInt( endPage ));
                    }
                }
                if (split != null)
                {
                    splitter.setSplitAtPage( Integer.parseInt( split ) );
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
            finally
            {
                if( document != null )
                {
                    document.close();
                }
                for( int i=0; documents != null && i<documents.size(); i++ )
                {
                    PDDocument doc = documents.get(i);
                    doc.close();
                }
            }
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar PDFSplit [options] <inputfile>\n"
                + "\nOptions:\n"
                + "  -password  <password>  : Password to decrypt document\n"
                + "  -split     <integer>   : split after this many pages (default 1, if startPage and endPage are unset)\n"
                + "  -startPage <integer>   : start page\n"
                + "  -endPage   <integer>   : end page\n"
                + "  -outputPrefix <prefix> : Filename prefix for splitted files\n"
                + "  <inputfile>            : The PDF document to use\n";
        
        System.err.println(message);
        System.exit( 1 );
    }
}