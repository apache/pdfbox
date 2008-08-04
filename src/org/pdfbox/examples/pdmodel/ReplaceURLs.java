/**
 * Copyright (c) 2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.examples.pdmodel;

import java.util.List;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

import org.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;


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