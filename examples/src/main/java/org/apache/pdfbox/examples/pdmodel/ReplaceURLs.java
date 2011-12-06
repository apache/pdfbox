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

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;


/**
 * This is an example of how to replace a URL in a PDF document.  This
 * will only replace the URL that the text refers to and not the text
 * itself.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class ReplaceURLs
{
    /**
     * Constructor.
     */
    private ReplaceURLs()
    {
        //utility class
    }

    /**
     * This will read in a document and replace all of the urls with
     * http://www.pdfbox.org.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     *
     * @throws Exception If there is an error during the process.
     */
    public static void main(String[] args) throws Exception
    {
        PDDocument doc = null;
        try
        {
            if( args.length != 2 )
            {
                usage();
            }
            else
            {
                doc = PDDocument.load( args[0] );
                List allPages = doc.getDocumentCatalog().getAllPages();
                for( int i=0; i<allPages.size(); i++ )
                {
                    PDPage page = (PDPage)allPages.get( i );
                    List annotations = page.getAnnotations();

                    for( int j=0; j<annotations.size(); j++ )
                    {
                        PDAnnotation annot = (PDAnnotation)annotations.get( j );
                        if( annot instanceof PDAnnotationLink )
                        {
                            PDAnnotationLink link = (PDAnnotationLink)annot;
                            PDAction action = link.getAction();
                            if( action instanceof PDActionURI )
                            {
                                PDActionURI uri = (PDActionURI)action;
                                String oldURI = uri.getURI();
                                String newURI = "http://www.pdfbox.org";
                                System.out.println( "Page " + (i+1) +": Replacing " + oldURI + " with " + newURI );
                                uri.setURI( newURI );
                            }
                        }
                    }
                }
                doc.save( args[1] );
            }
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: " + ReplaceURLs.class.getName() + " <input-file> <output-file>" );
    }
}
