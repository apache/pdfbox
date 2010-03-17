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
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.util.List;

/**
 * This is an example on how to add bookmarks to a PDF document.  It simply
 * adds 1 bookmark for every page.
 *
 * Usage: java org.apache.pdfbox.examples.pdmodel.CreateBookmarks &lt;input-pdf&gt; &lt;output-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class CreateBookmarks
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
                document = PDDocument.load( args[0] );
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Cannot add bookmarks to encrypted document." );
                    System.exit( 1 );
                }
                PDDocumentOutline outline =  new PDDocumentOutline();
                document.getDocumentCatalog().setDocumentOutline( outline );
                PDOutlineItem pagesOutline = new PDOutlineItem();
                pagesOutline.setTitle( "All Pages" );
                outline.appendChild( pagesOutline );
                List pages = document.getDocumentCatalog().getAllPages();
                for( int i=0; i<pages.size(); i++ )
                {
                    PDPage page = (PDPage)pages.get( i );
                    PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
                    dest.setPage( page );
                    PDOutlineItem bookmark = new PDOutlineItem();
                    bookmark.setDestination( dest );
                    bookmark.setTitle( "Page " + (i+1) );
                    pagesOutline.appendChild( bookmark );
                }
                pagesOutline.openNode();
                outline.openNode();

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
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.CreateBookmarks <input-pdf> <output-pdf>" );
    }
}
