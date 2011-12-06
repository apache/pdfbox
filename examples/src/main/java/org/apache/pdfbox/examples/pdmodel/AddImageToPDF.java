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
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

import org.apache.pdfbox.pdmodel.graphics.xobject.PDCcitt;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;


/**
 * This is an example that creates a reads a document and adds an image to it..
 *
 * The example is taken from the pdf file format specification.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.1 $
 */
public class AddImageToPDF
{

    /**
     * Add an image to an existing PDF document.
     *
     * @param inputFile The input PDF to add the image to.
     * @param image The filename of the image to put in the PDF.
     * @param outputFile The file to write to the pdf to.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public void createPDFFromImage( String inputFile, String image, String outputFile ) 
        throws IOException, COSVisitorException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( inputFile );

            //we will add the image to the first page.
            PDPage page = (PDPage)doc.getDocumentCatalog().getAllPages().get( 0 );

            PDXObjectImage ximage = null;
            if( image.toLowerCase().endsWith( ".jpg" ) )
            {
                ximage = new PDJpeg(doc, new FileInputStream( image ) );
            }
            else if (image.toLowerCase().endsWith(".tif") || image.toLowerCase().endsWith(".tiff"))
            {
                ximage = new PDCcitt(doc, new RandomAccessFile(new File(image),"r"));
            }
            else
            {
                //BufferedImage awtImage = ImageIO.read( new File( image ) );
                //ximage = new PDPixelMap(doc, awtImage);
                throw new IOException( "Image type not supported:" + image );
            }
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);

            contentStream.drawImage( ximage, 20, 20 );

            contentStream.close();
            doc.save( outputFile );
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
     * This will load a PDF document and add a single image on it.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        AddImageToPDF app = new AddImageToPDF();
        try
        {
            if( args.length != 3 )
            {
                app.usage();
            }
            else
            {
                app.createPDFFromImage( args[0], args[1], args[2] );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + " <input-pdf> <image> <output-pdf>" );
    }
}
