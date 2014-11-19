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
package org.apache.pdfbox.examples.pdmodel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This is an example on how to get a documents metadata information.
 *
 * Usage: java org.apache.pdfbox.examples.pdmodel.PrintDocumentMetaData &lt;input-pdf&gt;
 *
 * @author Ben Litchfield
 * 
 */
public class PrintDocumentMetaData
{
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
                document = PDDocument.load( new File(args[0]));
                PrintDocumentMetaData meta = new PrintDocumentMetaData();
                meta.printMetadata( document );
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
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.PrintDocumentMetaData <input-pdf>" );
    }

    /**
     * This will print the documents data to System.out.
     *
     * @param document The document to get the metadata from.
     *
     * @throws IOException If there is an error getting the page count.
     */
    public void printMetadata( PDDocument document ) throws IOException
    {
        PDDocumentInformation info = document.getDocumentInformation();
        PDDocumentCatalog cat = document.getDocumentCatalog();
        PDMetadata metadata = cat.getMetadata();
        System.out.println( "Page Count=" + document.getNumberOfPages() );
        System.out.println( "Title=" + info.getTitle() );
        System.out.println( "Author=" + info.getAuthor() );
        System.out.println( "Subject=" + info.getSubject() );
        System.out.println( "Keywords=" + info.getKeywords() );
        System.out.println( "Creator=" + info.getCreator() );
        System.out.println( "Producer=" + info.getProducer() );
        System.out.println( "Creation Date=" + formatDate( info.getCreationDate() ) );
        System.out.println( "Modification Date=" + formatDate( info.getModificationDate() ) );
        System.out.println( "Trapped=" + info.getTrapped() );
        if( metadata != null )
        {
            System.out.println( "Metadata=" + metadata.getInputStreamAsString() );
        }
    }

    /**
     * This will format a date object.
     *
     * @param date The date to format.
     *
     * @return A string representation of the date.
     */
    private String formatDate( Calendar date )
    {
        String retval = null;
        if( date != null )
        {
            SimpleDateFormat formatter = new SimpleDateFormat();
            retval = formatter.format( date.getTime() );
        }

        return retval;
    }
}
