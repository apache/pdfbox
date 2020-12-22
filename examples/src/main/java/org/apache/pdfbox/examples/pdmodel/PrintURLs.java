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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.text.PDFTextStripperByArea;


/**
 * This is an example of how to access a URL in a PDF document.
 *
 * @author Ben Litchfield
 */
public final class PrintURLs
{
    /**
     * Constructor.
     */
    private PrintURLs()
    {
        //utility class
    }

    /**
     * This will output all URLs and the texts in the annotation rectangle of a document.
     * <br>
     * see usage() for commandline
     *
     * @param args Command line arguments.
     *
     * @throws IOException If there is an error extracting the URLs.
     */
    public static void main(final String[] args) throws IOException
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
                doc = Loader.loadPDF(new File(args[0]));
                int pageNum = 0;
                for( final PDPage page : doc.getPages() )
                {
                    pageNum++;
                    final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                    final List<PDAnnotation> annotations = page.getAnnotations();
                    //first setup text extraction regions
                    for( int j=0; j<annotations.size(); j++ )
                    {
                        final PDAnnotation annot = annotations.get(j);

                        if (getActionURI(annot) != null)
                        {
                            final PDRectangle rect = annot.getRectangle();
                            //need to reposition link rectangle to match text space
                            final float x = rect.getLowerLeftX();
                            float y = rect.getUpperRightY();
                            final float width = rect.getWidth();
                            final float height = rect.getHeight();
                            final int rotation = page.getRotation();
                            if( rotation == 0 )
                            {
                                final PDRectangle pageSize = page.getMediaBox();
                                // area stripper uses java coordinates, not PDF coordinates
                                y = pageSize.getHeight() - y;
                            }
                            else
                            {
                                // do nothing
                                // please send us a sample file
                            }

                            final Rectangle2D.Float awtRect = new Rectangle2D.Float( x,y,width,height );
                            stripper.addRegion( "" + j, awtRect );
                        }
                    }

                    stripper.extractRegions( page );

                    for( int j=0; j<annotations.size(); j++ )
                    {
                        final PDAnnotation annot = annotations.get(j);
                        final PDActionURI uri = getActionURI(annot);
                        if (uri != null)
                        {
                            final String urlText = stripper.getTextForRegion("" + j);
                            System.out.println("Page " + pageNum + ":'" + urlText.trim() + "'=" + uri.getURI());
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

    private static PDActionURI getActionURI(final PDAnnotation annot)
    {
        // use reflection to catch all annotation types that have getAction()
        // If you can't use reflection, then check for classes
        // PDAnnotationLink and PDAnnotationWidget, and call getAction() and check for a 
        // PDActionURI result type
        try
        {
            final Method actionMethod = annot.getClass().getDeclaredMethod("getAction");
            if (actionMethod.getReturnType().equals(PDAction.class))
            {
                final PDAction action = (PDAction) actionMethod.invoke(annot);
                if (action instanceof PDActionURI)
                {
                    return (PDActionURI) action;
                }
            }
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
        }
        return null;
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: " + PrintURLs.class.getName() + " <input-file>" );
    }
}
