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

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

/**
 * This is an example on how to add bookmarks to a PDF document.  It simply
 * adds 1 bookmark for every page.
 *
 * @author Ben Litchfield
 */
public final class CreateBookmarks
{
    private CreateBookmarks()
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
            PDDocument document = null;
            try
            {
                document = PDDocument.load( new File(args[0]) );
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Cannot add bookmarks to encrypted document." );
                    System.exit( 1 );
                }
                PDDocumentOutline outline =  new PDDocumentOutline();
                document.getDocumentCatalog().setDocumentOutline( outline );
                PDOutlineItem pagesOutline = new PDOutlineItem();
                pagesOutline.setTitle( "All Pages" );
                outline.addLast( pagesOutline );
                int pageNum = 0;
                for( PDPage page : document.getPages() )
                {
                    pageNum++;
                    PDPageDestination dest = new PDPageFitWidthDestination();
                    // If you want to have several bookmarks pointing to different areas 
                    // on the same page, have a look at the other classes derived from PDPageDestination.

                    dest.setPage( page );
                    PDOutlineItem bookmark = new PDOutlineItem();
                    bookmark.setDestination( dest );
                    bookmark.setTitle( "Page " + pageNum );
                    pagesOutline.addLast( bookmark );
                }
                pagesOutline.openNode();
                outline.openNode();
                
                // optional: show the outlines when opening the file
                document.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);

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
        System.err.println( "Usage: java " + CreateBookmarks.class.getName() + " <input-pdf> <output-pdf>" );
    }
}
