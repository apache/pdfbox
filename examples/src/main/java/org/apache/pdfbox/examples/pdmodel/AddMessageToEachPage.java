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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * This is an example of how to add a message to every page
 * in a pdf document.
 *
 * @author Ben Litchfield
 */
public class AddMessageToEachPage
{
    /**
     * Constructor.
     */
    public AddMessageToEachPage()
    {
        super();
    }

    /**
     * create the second sample document from the PDF file format specification.
     *
     * @param file The file to write the PDF to.
     * @param message The message to write in the file.
     * @param outfile The resulting PDF.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void doIt( String file, String message, String  outfile ) throws IOException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( new File(file) );

            PDFont font = PDType1Font.HELVETICA_BOLD;
            float fontSize = 36.0f;

            for( PDPage page : doc.getPages() )
            {
                PDRectangle pageSize = page.getMediaBox();
                float stringWidth = font.getStringWidth( message )*fontSize/1000f;
                // calculate to center of the page
                int rotation = page.getRotation();
                boolean rotate = rotation == 90 || rotation == 270;
                float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
                float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
                double centeredXPosition = rotate ? pageHeight/2f : (pageWidth - stringWidth)/2f;
                double centeredYPosition = rotate ? (pageWidth - stringWidth)/2f : pageHeight/2f;
                // append the content to the existing stream
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true,true);
                contentStream.beginText();
                // set font and font size
                contentStream.setFont( font, fontSize );
                // set text color to red
                contentStream.setNonStrokingColor(255, 0, 0);
                if (rotate)
                {
                    // rotate the text according to the page rotation
                    contentStream.setTextRotation(Math.PI/2, centeredXPosition, centeredYPosition);
                }
                else
                {
                    contentStream.setTextTranslation(centeredXPosition, centeredYPosition);
                }
                contentStream.drawString( message );
                contentStream.endText();
                contentStream.close();
            }

            doc.save( outfile );
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
     * This will create a hello world PDF document.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws IOException
    {
        AddMessageToEachPage app = new AddMessageToEachPage();
        if( args.length != 3 )
        {
            app.usage();
        }
        else
        {
            app.doIt( args[0], args[1], args[2] );
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + " <input-file> <Message> <output-file>" );
    }
}
