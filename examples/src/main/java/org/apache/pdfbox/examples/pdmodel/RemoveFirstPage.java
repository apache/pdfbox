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

import java.io.File;
import java.io.IOException;

/**
 * This is an example on how to remove pages from a PDF document.
 *
 * @author Ben Litchfield
 */
public class RemoveFirstPage
{
    private RemoveFirstPage()
    {
        //utility class, should not be instantiated.
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
        if( args.length != 2 )
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
                    throw new IOException( "Encrypted documents are not supported for this example" );
                }
                if( document.getNumberOfPages() <= 1 )
                {
                    throw new IOException( "Error: A PDF document must have at least one page, " +
                                           "cannot remove the last page!");
                }
                document.removePage( 0 );
                document.save( args[1] );
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
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.RemoveFirstPage <input-pdf> <output-pdf>" );
    }
}
