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

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.util.PDFTextStripperByArea;


/**
 * This is an example of how to access a URL in a PDF document.
 *
 * @author Ben Litchfield
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
                doc = PDDocument.load( new File(args[0]) );
                int pageNum = 0;
                for( PDPage page : doc.getPages() )
                {
                    pageNum++;
                    PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                    List<PDAnnotation> annotations = page.getAnnotations();
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
                            int rotation = page.getRotation();
                            if( rotation == 0 )
                            {
                                PDRectangle pageSize = page.getMediaBox();
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
                                System.out.println( "Page " + pageNum +":'" + urlText + "'=" + uri.getURI() );
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
