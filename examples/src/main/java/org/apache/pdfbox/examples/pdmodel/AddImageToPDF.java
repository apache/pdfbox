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
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * This is an example that creates a reads a document and adds an image to it..
 *
 * The example is taken from the pdf file format specification.
 *
 * @author Ben Litchfield
 */
public class AddImageToPDF
{
    /**
     * Add an image to an existing PDF document.
     *
     * @param inputFile The input PDF to add the image to.
     * @param imagePath The filename of the image to put in the PDF.
     * @param outputFile The file to write to the pdf to.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void createPDFFromImage( String inputFile, String imagePath, String outputFile )
            throws IOException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( new File(inputFile) );

            //we will add the image to the first page.
            PDPage page = doc.getPage(0);

            // createFromFile is the easiest way with an image file
            // if you already have the image in a BufferedImage, 
            // call LosslessFactory.createFromImage() instead
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true);

            // contentStream.drawImage(ximage, 20, 20 );
            // better method inspired by http://stackoverflow.com/a/22318681/535646
            // reduce this value if the image is too large
            float scale = 1f;
            contentStream.drawImage(pdImage, 20, 20, pdImage.getWidth()*scale, pdImage.getHeight()*scale);

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
     * <br>
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws IOException
    {
        AddImageToPDF app = new AddImageToPDF();
        if( args.length != 3 )
        {
            app.usage();
        }
        else
        {
            app.createPDFFromImage( args[0], args[1], args[2] );
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
