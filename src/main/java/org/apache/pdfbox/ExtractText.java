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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
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
    private static final String HTML = "-html";  // jjb - added simple HTML output

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
        boolean toConsole = false;
        boolean toHTML = false;
        boolean sort = false;
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
                try
                {
                    //basically try to load it from a url first and if the URL
                    //is not recognized then try to load it from the file system.
                    URL url = new URL( pdfFile );
                    document = PDDocument.load( url );
                    String fileName = url.getFile();
                    if( outputFile == null && fileName.length() >4 )
                    {
                        outputFile = new File( fileName.substring( 0, fileName.length() -4 ) + ext ).getName();
                    }
                }
                catch( MalformedURLException e )
                {
                    document = PDDocument.load( pdfFile );
                    if( outputFile == null && pdfFile.length() >4 )
                    {
                        outputFile = pdfFile.substring( 0, pdfFile.length() -4 ) + ext;
                    }
                }

                //document.print();
                if( document.isEncrypted() )
                {
                    StandardDecryptionMaterial sdm = new StandardDecryptionMaterial( password );
                    document.openProtection( sdm );
                    AccessPermission ap = document.getCurrentAccessPermission();

                    if( ! ap.canExtractContent() )
                    {
                        throw new IOException( "You do not have permission to extract text" );
                    }
                }

                if ((encoding == null) && (toHTML))
                    encoding = "UTF-8";

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
                    stripper = new PDFTextStripper();
                }
                stripper.setSortByPosition( sort );
                stripper.setStartPage( startPage );
                stripper.setEndPage( endPage );
                stripper.writeText( document, output );
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

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.ExtractText [OPTIONS] <PDF file> [Text File]\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n" +
            "  -console                     Send text to console instead of file\n" +
            "  -html                        Output in HTML format instead of raw text\n" +
            "  -sort                        Sort the text before writing\n" +
            "  -startPage <number>          The first page to start extraction(1 based)\n" +
            "  -endPage <number>            The last page to extract(inclusive)\n" +
            "  <PDF file>                   The PDF document to use\n" +
            "  [Text File]                  The file to write the text to\n"
            );
        System.exit( 1 );
    }
}
