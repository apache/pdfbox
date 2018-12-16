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
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

/**
 * This is an example on how to access the bookmarks that are part of a pdf document.
 *
 * @author Ben Litchfield
 * 
 */
public class PrintBookmarks
{
    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
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
                PrintBookmarks meta = new PrintBookmarks();
                PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
                if( outline != null )
                {
                    meta.printBookmark(document, outline, "");
                }
                else
                {
                    System.out.println( "This document does not contain any bookmarks" );
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
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + PrintBookmarks.class.getName() + " <input-pdf>" );
    }

    /**
     * This will print the documents bookmarks to System.out.
     *
     * @param document The document.
     * @param bookmark The bookmark to print out.
     * @param indentation A pretty printing parameter
     *
     * @throws IOException If there is an error getting the page count.
     */
    public void printBookmark(PDDocument document, PDOutlineNode bookmark, String indentation) throws IOException
    {
        PDOutlineItem current = bookmark.getFirstChild();
        while( current != null )
        {
            // one could also use current.findDestinationPage(document) to get the page number,
            // but this example does it the hard way to explain the different types
            // Note that bookmarks can also do completely different things, e.g. link to a website,
            // or to an external file. This example focuses on internal pages.

            if (current.getDestination() instanceof PDPageDestination)
            {
                PDPageDestination pd = (PDPageDestination) current.getDestination();
                System.out.println(indentation + "Destination page: " + (pd.retrievePageNumber() + 1));
            }
            else if (current.getDestination() instanceof PDNamedDestination)
            {
                PDPageDestination pd = document.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) current.getDestination());
                if (pd != null)
                {
                    System.out.println(indentation + "Destination page: " + (pd.retrievePageNumber() + 1));
                }
            }
            else if (current.getDestination() != null)
            {
                System.out.println(indentation + "Destination class: " + current.getDestination().getClass().getSimpleName());
            }

            if (current.getAction() instanceof PDActionGoTo)
            {
                PDActionGoTo gta = (PDActionGoTo) current.getAction();
                if (gta.getDestination() instanceof PDPageDestination)
                {
                    PDPageDestination pd = (PDPageDestination) gta.getDestination();
                    System.out.println(indentation + "Destination page: " + (pd.retrievePageNumber() + 1));
                }
                else if (gta.getDestination() instanceof PDNamedDestination)
                {
                    PDPageDestination pd = document.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) gta.getDestination());
                    if (pd != null)
                    {
                        System.out.println(indentation + "Destination page: " + (pd.retrievePageNumber() + 1));
                    }
                }
                else
                {
                    System.out.println(indentation + "Destination class: " + gta.getDestination().getClass().getSimpleName());
                }
            }
            else if (current.getAction() != null)
            {
                System.out.println(indentation + "Action class: " + current.getAction().getClass().getSimpleName());
            }
            System.out.println( indentation + current.getTitle() );
            printBookmark( document, current, indentation + "    " );
            current = current.getNextSibling();
        }
    }
}
