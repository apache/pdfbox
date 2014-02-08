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
package org.apache.pdfbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * This is the main program that simply parses the pdf document and transforms it
 * into text.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.14 $
 */
public class ExtractText
{
    private static final String PASSWORD = "-password";
    private static final String ENCODING = "-encoding";
    private static final String CONSOLE = "-console";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String SORT = "-sort";
    private static final String IGNORE_BEADS = "-ignoreBeads";
    private static final String DEBUG = "-debug";
    // jjb - added simple HTML output
    private static final String HTML = "-html";  
    // enables pdfbox to skip corrupt objects
    private static final String FORCE = "-force"; 
    private static final String NONSEQ = "-nonSeq";

    /*
     * debug flag
     */
    private boolean debug = false;

    /**
     * private constructor.
    */
    private ExtractText()
    {
        //static class
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        ExtractText extractor = new ExtractText();
        extractor.startExtraction(args);
    }
    /**
     * Starts the text extraction.
     *  
     * @param args the commandline arguments.
     * 
     * @throws Exception if something went wrong.
     */
    public void startExtraction( String[] args ) throws Exception
    {
        boolean toConsole = false;
        boolean toHTML = false;
        boolean force = false;
        boolean sort = false;
        boolean separateBeads = true;
        boolean useNonSeqParser = false; 
        String password = "";
        String encoding = null;
        String pdfFile = null;
        String outputFile = null;
        // Defaults to text files
        String ext = ".txt";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( ENCODING ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                encoding = args[i];
            }
            else if( args[i].equals( START_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                startPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( HTML ) )
            {
                toHTML = true;
                ext = ".html";
            }
            else if( args[i].equals( SORT ) )
            {
                sort = true;
            }
            else if( args[i].equals( IGNORE_BEADS ) )
            {
                separateBeads = false;
            }
            else if( args[i].equals( DEBUG ) )
            {
                debug = true;
            }
            else if( args[i].equals( END_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                endPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( CONSOLE ) )
            {
                toConsole = true;
            }
            else if( args[i].equals( FORCE ) )
            {
                force = true;
            }
            else if( args[i].equals( NONSEQ ) )
            {
                useNonSeqParser = true;
            }
            else
            {
                if( pdfFile == null )
                {
                    pdfFile = args[i];
                }
                else
                {
                    outputFile = args[i];
                }
            }
        }

        if( pdfFile == null )
        {
            usage();
        }
        else
        {

            Writer output = null;
            PDDocument document = null;
            try
            {
                long startTime = startProcessing("Loading PDF "+pdfFile);
                if( outputFile == null && pdfFile.length() >4 )
                {
                    outputFile = new File( pdfFile.substring( 0, pdfFile.length() -4 ) + ext ).getAbsolutePath();
                }
                if (useNonSeqParser) 
                {
                    document = PDDocument.loadNonSeq(new File( pdfFile ), null, password);
                }
                else
                {
                    document = PDDocument.load(pdfFile, force);
                    if( document.isEncrypted() )
                    {
                        StandardDecryptionMaterial sdm = new StandardDecryptionMaterial( password );
                        document.openProtection( sdm );
                    }
                }
                
                AccessPermission ap = document.getCurrentAccessPermission();
                if( ! ap.canExtractContent() )
                {
                    throw new IOException( "You do not have permission to extract text" );
                }
                
                stopProcessing("Time for loading: ", startTime);


                if ((encoding == null) && (toHTML))
                {
                    encoding = "UTF-8";
                }

                if( toConsole )
                {
                    output = new OutputStreamWriter( System.out );
                }
                else
                {
                    if( encoding != null )
                    {
                        output = new OutputStreamWriter(
                                new FileOutputStream( outputFile ), encoding );
                    }
                    else
                    {
                        //use default encoding
                        output = new OutputStreamWriter(
                                new FileOutputStream( outputFile ) );
                    }
                }

                PDFTextStripper stripper = null;
                if(toHTML)
                {
                    stripper = new PDFText2HTML(encoding);
                }
                else
                {
                    stripper = new PDFTextStripper(encoding);
                }
                stripper.setForceParsing( force );
                stripper.setSortByPosition( sort );
                stripper.setShouldSeparateByBeads( separateBeads );
                stripper.setStartPage( startPage );
                stripper.setEndPage( endPage );

                startTime = startProcessing("Starting text extraction");
                if (debug) 
                {
                    System.err.println("Writing to "+outputFile);
                }
                
                // Extract text for main document:
                stripper.writeText( document, output );
                
                // ... also for any embedded PDFs:
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDDocumentNameDictionary names = catalog.getNames();    
                if (names != null)
                {
                    PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
                    if (embeddedFiles != null)
                    {
                        Map<String,COSObjectable> embeddedFileNames = embeddedFiles.getNames();
                        if (embeddedFileNames != null) {
                            for (Map.Entry<String,COSObjectable> ent : embeddedFileNames.entrySet()) 
                            {
                                if (debug)
                                {
                                    System.err.println("Processing embedded file " + ent.getKey() + ":");
                                }
                                PDComplexFileSpecification spec = (PDComplexFileSpecification) ent.getValue();
                                PDEmbeddedFile file = spec.getEmbeddedFile();
                                if (file.getSubtype().equals("application/pdf")) 
                                {
                                    if (debug)
                                    {
                                        System.err.println("  is PDF (size=" + file.getSize() + ")");
                                    }
                                    InputStream fis = file.createInputStream();
                                    PDDocument subDoc = null;
                                    try 
                                    {
                                        subDoc = PDDocument.load(fis);
                                    } 
                                    finally 
                                    {
                                        fis.close();
                                    }
                                    try 
                                    {
                                        stripper.writeText( subDoc, output );
                                    } 
                                    finally 
                                    {
                                        subDoc.close();
                                    }
                                }
                            } 
                        }
                    }
                }
                stopProcessing("Time for extraction: ", startTime);
            }
            finally
            {
                if( output != null )
                {
                    output.close();
                }
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    private long startProcessing(String message) 
    {
        if (debug) 
        {
            System.err.println(message);
        }
        return System.currentTimeMillis();
    }
    
    private void stopProcessing(String message, long startTime) 
    {
        if (debug)
        {
            long stopTime = System.currentTimeMillis();
            float elapsedTime = ((float)(stopTime - startTime))/1000;
            System.err.println(message + elapsedTime + " seconds");
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java -jar pdfbox-app-x.y.z.jar ExtractText [OPTIONS] <PDF file> [Text File]\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n" +
            "  -console                     Send text to console instead of file\n" +
            "  -html                        Output in HTML format instead of raw text\n" +
            "  -sort                        Sort the text before writing\n" +
            "  -ignoreBeads                 Disables the separation by beads\n" +
            "  -force                       Enables pdfbox to ignore corrupt objects\n" +
            "  -debug                       Enables debug output about the time consumption of every stage\n" +
            "  -startPage <number>          The first page to start extraction(1 based)\n" +
            "  -endPage <number>            The last page to extract(inclusive)\n" +
            "  -nonSeq                      Enables the new non-sequential parser\n" +
            "  <PDF file>                   The PDF document to use\n" +
            "  [Text File]                  The file to write the text to\n"
            );
        System.exit( 1 );
    }
}
