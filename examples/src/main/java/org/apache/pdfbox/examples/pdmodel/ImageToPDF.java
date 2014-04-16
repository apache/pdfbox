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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.apache.pdfbox.io.RandomAccessFile;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

/**
 * This is an example that creates a simple document.
 *
 * The example is taken from the pdf file format specification.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class ImageToPDF
{
    /**
     * create the second sample document from the PDF file format specification.
     *
     * @param file The file to write the PDF to.
     * @param image The filename of the image to put in the PDF.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void createPDFFromImage( String file, String image) throws IOException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();

            PDPage page = new PDPage();
            doc.addPage( page );

            PDImageXObject pdImage;
            if( image.toLowerCase().endsWith( ".jpg" ) )
            {
                pdImage = JPEGFactory.createFromStream(doc, new FileInputStream(image));
            }
            else if (image.toLowerCase().endsWith(".tif") || 
                    image.toLowerCase().endsWith(".tiff"))
            {
                pdImage = CCITTFactory.createFromRandomAccess(doc, new RandomAccessFile(new File(image),"r"));
            }
            else if (image.toLowerCase().endsWith(".gif") || 
                    image.toLowerCase().endsWith(".bmp") || 
                    image.toLowerCase().endsWith(".png"))
            {
                BufferedImage bim = ImageIO.read(new File(image));
                pdImage = LosslessFactory.createFromImage(doc, bim);
            }
            else
            {
                throw new IOException( "Image type not supported: " + image );
            }
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            //contentStream.drawImage(pdImage, 20, 20 );
            // better method inspired by http://stackoverflow.com/a/22318681/535646
            float scale = 1f; // reduce this value if the image is too large
            contentStream.drawXObject(pdImage, 20, 20, pdImage.getWidth()*scale, pdImage.getHeight()*scale);

            contentStream.close();
            doc.save( file );
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
     * This will create a PDF document with a single image on it.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        ImageToPDF app = new ImageToPDF();
        try
        {
            if( args.length != 2 )
            {
                app.usage();
            }
            else
            {
                app.createPDFFromImage( args[0], args[1] );
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
        System.err.println( "usage: " + this.getClass().getName() + " <output-file> <image>" );
    }
}
