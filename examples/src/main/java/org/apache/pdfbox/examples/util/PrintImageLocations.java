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
package org.apache.pdfbox.examples.util;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.exceptions.WrappedIOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;

import java.util.List;
import java.util.Map;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * Usage: java org.apache.pdfbox.examples.util.PrintImageLocations &lt;input-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PrintImageLocations extends PDFStreamEngine
{
    
    private static final String INVOKE_OPERATOR = "Do";
    /**
     * Default constructor.
     *
     * @throws IOException If there is an error loading text stripper properties.
     */
    public PrintImageLocations() throws IOException
    {
        super( ResourceLoader.loadProperties(
                "org/apache/pdfbox/resources/PDFTextStripper.properties", true ) );
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
        if( args.length != 1 )
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
                    try
                    {
                        document.decrypt( "" );
                    }
                    catch( InvalidPasswordException e )
                    {
                        System.err.println( "Error: Document is encrypted with a password." );
                        System.exit( 1 );
                    }
                }
                PrintImageLocations printer = new PrintImageLocations();
                List allPages = document.getDocumentCatalog().getAllPages();
                for( int i=0; i<allPages.size(); i++ )
                {
                    PDPage page = (PDPage)allPages.get( i );
                    System.out.println( "Processing page: " + i );
                    printer.processStream( page, page.findResources(), page.getContents().getStream() );
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
     * This is used to handle an operation.
     *
     * @param operator The operation to perform.
     * @param arguments The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    protected void processOperator( PDFOperator operator, List arguments ) throws IOException
    {
        String operation = operator.getOperation();
        if( INVOKE_OPERATOR.equals(operation) )
        {
            COSName objectName = (COSName)arguments.get( 0 );
            Map<String, PDXObject> xobjects = getResources().getXObjects();
            PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );
            if( xobject instanceof PDXObjectImage )
            {
                PDXObjectImage image = (PDXObjectImage)xobject;
                PDPage page = getCurrentPage();
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                double pageHeight = page.getMediaBox().getHeight();
                System.out.println("*******************************************************************");
                System.out.println("Found image [" + objectName.getName() + "]");
        
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float yScaling = ctmNew.getYScale();
                float angle = (float)Math.acos(ctmNew.getValue(0, 0)/ctmNew.getXScale());
                if (ctmNew.getValue(0, 1) < 0 && ctmNew.getValue(1, 0) > 0)
                {
                    angle = (-1)*angle;
                }
                ctmNew.setValue(2, 1, (float)(pageHeight - ctmNew.getYPosition() - Math.cos(angle)*yScaling));
                ctmNew.setValue(2, 0, (float)(ctmNew.getXPosition() - Math.sin(angle)*yScaling));
                // because of the moved 0,0-reference, we have to shear in the opposite direction
                ctmNew.setValue(0, 1, (-1)*ctmNew.getValue(0, 1));
                ctmNew.setValue(1, 0, (-1)*ctmNew.getValue(1, 0));
                AffineTransform ctmAT = ctmNew.createAffineTransform();
                ctmAT.scale(1f/imageWidth, 1f/imageHeight);

                float imageXScale = ctmNew.getXScale();
                float imageYScale = ctmNew.getYScale();
                System.out.println("position = " + ctmNew.getXPosition() + ", " + ctmNew.getYPosition());
                // size in pixel
                System.out.println("size = " + imageWidth + "px, " + imageHeight + "px");
                // size in page units
                System.out.println("size = " + imageXScale + ", " + imageYScale);
                // size in inches 
                imageXScale /= 72;
                imageYScale /= 72;
                System.out.println("size = " + imageXScale + "in, " + imageYScale + "in");
                // size in millimeter
                imageXScale *= 25.4;
                imageYScale *= 25.4;
                System.out.println("size = " + imageXScale + "mm, " + imageYScale + "mm");
                System.out.println();
            }
            else if(xobject instanceof PDXObjectForm)
            {
                // save the graphics state
                getGraphicsStack().push( (PDGraphicsState)getGraphicsState().clone() );
                PDPage page = getCurrentPage();
                
                PDXObjectForm form = (PDXObjectForm)xobject;
                COSStream invoke = (COSStream)form.getCOSObject();
                PDResources pdResources = form.getResources();
                if(pdResources == null)
                {
                    pdResources = page.findResources();
                }
                // if there is an optional form matrix, we have to
                // map the form space to the user space
                Matrix matrix = form.getMatrix();
                if (matrix != null) 
                {
                    Matrix xobjectCTM = matrix.multiply( getGraphicsState().getCurrentTransformationMatrix());
                    getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }
                processSubStream( page, pdResources, invoke );
                
                // restore the graphics state
                setGraphicsState( (PDGraphicsState)getGraphicsStack().pop() );
            }
            
        }
        else
        {
            super.processOperator( operator, arguments );
        }
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.PrintImageLocations <input-pdf>" );
    }

}
