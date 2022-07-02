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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.IOException;

/**
 * This is an example on how to an action to go to the second page when the PDF is opened.
 *
 * @author Ben Litchfield
 */
public final class GoToSecondBookmarkOnOpen
{
    private GoToSecondBookmarkOnOpen()
    {
        //utility class
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            try (PDDocument document = Loader.loadPDF(new File(args[0])))
            {
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Cannot add bookmark destination to encrypted documents." );
                    System.exit( 1 );
                }

                if( document.getNumberOfPages() < 2 )
                {
                    throw new IOException( "Error: The PDF must have at least 2 pages.");
                }
                PDDocumentOutline bookmarks = document.getDocumentCatalog().getDocumentOutline();
                if( bookmarks == null )
                {
                    throw new IOException( "Error: The PDF does not contain any bookmarks" );
                }
                PDOutlineItem item = bookmarks.getFirstChild().getNextSibling();
                PDDestination dest = item.getDestination();
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest, action.SUB_TYPE);
                document.getDocumentCatalog().setOpenAction(action);

                document.save( args[1] );
            }
        }
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + GoToSecondBookmarkOnOpen.class.getName() +
            "<input-pdf> <output-pdf>" );
    }
}
