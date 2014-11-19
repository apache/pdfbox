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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * Usage: java org.apache.pdfbox.examples.util.PrintImageLocations &lt;input-pdf&gt;
 *
 * @author Ben Litchfield
 */
public class PrintImageLocations extends PDFStreamEngine
{
    /**
     * Default constructor.
     *
     * @throws IOException If there is an error loading text stripper properties.
     */
    public PrintImageLocations() throws IOException
    {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
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
                document = PDDocument.load( new File(args[0]) );
                PrintImageLocations printer = new PrintImageLocations();
                int pageNum = 0;
                for( PDPage page : document.getPages() )
                {
                    pageNum++;
                    System.out.println( "Processing page: " + pageNum );
                    printer.processPage(page);
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
    protected void processOperator( Operator operator, List<COSBase> arguments ) throws IOException
    {
        String operation = operator.getName();
        if( "Do".equals(operation) )
        {
            COSName objectName = (COSName)arguments.get( 0 );
            PDXObject xobject = getResources().getXObject( objectName );
            if( xobject instanceof PDImageXObject)
            {
                PDImageXObject image = (PDImageXObject)xobject;
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                System.out.println("*******************************************************************");
                System.out.println("Found image [" + objectName.getName() + "]");
        
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                AffineTransform imageTransform = ctmNew.createAffineTransform();
                imageTransform.scale(1.0 / imageWidth, -1.0 / imageHeight);
                imageTransform.translate(0, -imageHeight);

                
                double imageXScale = imageTransform.getScaleX();
                double imageYScale = imageTransform.getScaleY();
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
            else if(xobject instanceof PDFormXObject)
            {
                // save the graphics state
                saveGraphicsState();
                
                PDFormXObject form = (PDFormXObject)xobject;
                // if there is an optional form matrix, we have to map the form space to the user space
                Matrix matrix = form.getMatrix();
                if (matrix != null) 
                {
                    Matrix xobjectCTM = matrix.multiply( getGraphicsState().getCurrentTransformationMatrix());
                    getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }
                processChildStream(form);
                
                // restore the graphics state
                restoreGraphicsState();
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
