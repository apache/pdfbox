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
package org.apache.pdfbox.examples.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

/**
 * This is an example on how to get some x/y coordinates of text.
 *
 * Usage: java org.apache.pdfbox.examples.util.PrintTextLocations &lt;input-pdf&gt;
 *
 * @author Ben Litchfield
 */
public class PrintTextLocations extends PDFTextStripper
{
    /**
     * Default constructor.
     *
     * @throws IOException If there is an error loading text stripper properties.
     */
    public PrintTextLocations() throws IOException
    {
        super.setSortByPosition( true );
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            usage();
        }
        else
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load( new File(args[0]) );
                if( document.isEncrypted() )
                {
                    try
                    {
                        StandardDecryptionMaterial sdm = new StandardDecryptionMaterial("");
                        document.openProtection(sdm);
                    }
                    catch( InvalidPasswordException e )
                    {
                        System.err.println( "Error: Document is encrypted with a password." );
                        System.exit( 1 );
                    }
                }
                PrintTextLocations printer = new PrintTextLocations();
                int pageNum = 0;
                for( PDPage page : document.getPages() )
                {
                    pageNum++;
                    System.out.println( "Processing page: " + pageNum );
                    PDStream contents = page.getStream();
                    if( contents != null )
                    {
                        printer.processPage(page);
                    }
                }
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * A method provided as an event interface to allow a subclass to perform
     * some specific functionality when text needs to be processed.
     *
     * @param text The text to be processed
     */
    protected void processTextPosition( TextPosition text )
    {
        System.out.println( "String[" + text.getXDirAdj() + "," +
                text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale=" +
                text.getXScale() + " height=" + text.getHeightDir() + " space=" +
                text.getWidthOfSpace() + " width=" +
                text.getWidthDirAdj() + "]" + text.getUnicode() );
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.PrintTextLocations <input-pdf>" );
    }

}
