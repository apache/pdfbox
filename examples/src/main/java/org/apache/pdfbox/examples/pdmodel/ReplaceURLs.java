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
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;


/**
 * This is an example of how to replace a URL in a PDF document.  This
 * will only replace the URL that the text refers to and not the text
 * itself.
 *
 * @author Ben Litchfield
 */
public final class ReplaceURLs
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
     * http://pdfbox.apache.org.
     * <br>
     * see usage() for commandline
     *
     * @param args Command line arguments.
     *
     * @throws IOException If there is an error during the process.
     */
    public static void main(final String[] args) throws IOException
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
                doc = Loader.loadPDF(new File(args[0]));
                int pageNum = 0;
                for( final PDPage page : doc.getPages() )
                {
                    pageNum++;
                    final List<PDAnnotation> annotations = page.getAnnotations();

                    for (final PDAnnotation annotation : annotations)
                    {
                        final PDAnnotation annot = annotation;
                        if( annot instanceof PDAnnotationLink )
                        {
                            final PDAnnotationLink link = (PDAnnotationLink)annot;
                            final PDAction action = link.getAction();
                            if( action instanceof PDActionURI )
                            {
                                final PDActionURI uri = (PDActionURI)action;
                                final String oldURI = uri.getURI();
                                final String newURI = "http://pdfbox.apache.org";
                                System.out.println( "Page " + pageNum +": Replacing " + oldURI + " with " + newURI );
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
