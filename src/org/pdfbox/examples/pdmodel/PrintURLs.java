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

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.pdfbox.util.PDFTextStripperByArea;


/**
 * This is an example of how to access a URL in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PrintURLs
{
    /**
     * Constructor.
     */
    private PrintURLs()
    {
        //utility class
    }

    /**
     * This will create a hello world PDF document.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     * 
     * @throws Exception If there is an error extracting the URLs.
     */
    public static void main(String[] args) throws Exception
    {
        PDDocument doc = null;
        try
        {
            if( args.length != 1 )
            {
                usage();
            }
            else
            {
                doc = PDDocument.load( args[0] );
                List allPages = doc.getDocumentCatalog().getAllPages();
                for( int i=0; i<allPages.size(); i++ )
                {
                    PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                    PDPage page = (PDPage)allPages.get( i );
                    List annotations = page.getAnnotations();
                    //first setup text extraction regions
                    for( int j=0; j<annotations.size(); j++ )
                    {
                        PDAnnotation annot = (PDAnnotation)annotations.get( j );
                        if( annot instanceof PDAnnotationLink )
                        {
                            PDAnnotationLink link = (PDAnnotationLink)annot;
                            PDRectangle rect = link.getRectangle();
                            //need to reposition link rectangle to match text space
                            float x = rect.getLowerLeftX();
                            float y = rect.getUpperRightY();
                            float width = rect.getWidth();
                            float height = rect.getHeight();
                            int rotation = page.findRotation();
                            if( rotation == 0 )
                            {
                                PDRectangle pageSize = page.findMediaBox();
                                y = pageSize.getHeight() - y;
                            }
                            else if( rotation == 90 )
                            {
                                //do nothing
                            }
                            
                            Rectangle2D.Float awtRect = new Rectangle2D.Float( x,y,width,height );
                            stripper.addRegion( "" + j, awtRect );
                        }
                    }
                    
                    stripper.extractRegions( page );
                    
                    for( int j=0; j<annotations.size(); j++ )
                    {
                        PDAnnotation annot = (PDAnnotation)annotations.get( j );
                        if( annot instanceof PDAnnotationLink )
                        {
                            PDAnnotationLink link = (PDAnnotationLink)annot;
                            PDAction action = link.getAction();
                            String urlText = stripper.getTextForRegion( "" + j );
                            if( action instanceof PDActionURI )
                            {
                                PDActionURI uri = (PDActionURI)action;
                                System.out.println( "Page " + (i+1) +":'" + urlText + "'=" + uri.getURI() );
                            }
                        }
                    }
                }
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
        System.err.println( "usage: " + PrintURLs.class.getName() + " <input-file>" );
    }
}