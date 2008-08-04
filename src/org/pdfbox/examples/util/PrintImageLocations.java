/**
 * Copyright (c) 2006, www.pdfbox.org
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
package org.pdfbox.examples.util;

import org.pdfbox.cos.COSName;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.exceptions.WrappedIOException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.pdfbox.util.Matrix;
import org.pdfbox.util.PDFOperator;
import org.pdfbox.util.PDFStreamEngine;
import org.pdfbox.util.ResourceLoader;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;

import java.util.List;
import java.util.Map;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * Usage: java org.pdfbox.examples.util.PrintImageLocations &lt;input-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
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
        super( ResourceLoader.loadProperties( "Resources/PDFTextStripper.properties", true ) );
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
        if( operation.equals( "Do" ) )
        {
            COSName objectName = (COSName)arguments.get( 0 );
            Map xobjects = getResources().getXObjects();
            PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );
            if( xobject instanceof PDXObjectImage )
            {
                try
                {
                    PDXObjectImage image = (PDXObjectImage)xobject;
                    PDPage page = getCurrentPage();
                    Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
                    double rotationInRadians =(page.findRotation() * Math.PI)/180;
                     
                    
                    AffineTransform rotation = new AffineTransform();
                    rotation.setToRotation( rotationInRadians );
                    AffineTransform rotationInverse = rotation.createInverse();
                    Matrix rotationInverseMatrix = new Matrix();
                    rotationInverseMatrix.setFromAffineTransform( rotationInverse );
                    Matrix rotationMatrix = new Matrix();
                    rotationMatrix.setFromAffineTransform( rotation );
                    
                    Matrix unrotatedCTM = ctm.multiply( rotationInverseMatrix );
                    float xScale = unrotatedCTM.getXScale();
                    float yScale = unrotatedCTM.getYScale();
                    
                    System.out.println( "Found image[" + objectName.getName() + "] " + 
                            "at " + unrotatedCTM.getXPosition() + "," + unrotatedCTM.getYPosition() +
                            " size=" + (xScale/100f*image.getWidth()) + "," + (yScale/100f*image.getHeight() ));
                }
                catch( NoninvertibleTransformException e )
                {
                    throw new WrappedIOException( e );
                }
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
        System.err.println( "Usage: java org.pdfbox.examples.pdmodel.PrintImageLocations <input-pdf>" );
    }

}