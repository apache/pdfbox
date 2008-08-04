/**
 * Copyright (c) 2007, www.pdfbox.org
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.io.RandomAccessFile;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

import org.pdfbox.pdmodel.edit.PDPageContentStream;

import org.pdfbox.pdmodel.graphics.xobject.PDCcitt;
import org.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;


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
    public void createPDFFromImage( String inputFile, String image, String outputFile ) throws IOException, COSVisitorException
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