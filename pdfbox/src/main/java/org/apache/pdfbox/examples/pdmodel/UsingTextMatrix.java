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

import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


/**
 * This is an example of how to use a text matrix.
 * @version $Revision: 1.0 $
 */
public class UsingTextMatrix
{
    /**
     * Constructor.
     */
    public UsingTextMatrix()
    {
        super();
    }

    /**
     * creates a sample document with some text using a text matrix.
     *
     * @param message The message to write in the file.
     * @param outfile The resulting PDF.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public void doIt( String message, String  outfile ) throws IOException, COSVisitorException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();

            // Page 1
            PDFont font = PDType1Font.HELVETICA;
            PDPage page = new PDPage();
            page.setMediaBox(PDPage.PAGE_SIZE_A4);
            doc.addPage(page);
            float fontSize = 12.0f;

            PDRectangle pageSize = page.findMediaBox();
            float centeredXPosition = (pageSize.getWidth() - fontSize/1000f)/2f;
            float stringWidth = font.getStringWidth( message );
            float centeredYPosition = (pageSize.getHeight() - (stringWidth*fontSize)/1000f)/3f;

            PDPageContentStream contentStream = new PDPageContentStream(doc, page, false, false);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();
            // counterclockwise rotation
            for (int i=0;i<8;i++) 
            {
                contentStream.setTextRotation(i*Math.PI*0.25, centeredXPosition, 
                        pageSize.getHeight()-centeredYPosition);
                contentStream.drawString( message + " " + i);
            }
            // clockwise rotation
            for (int i=0;i<8;i++) 
            {
                contentStream.setTextRotation(-i*Math.PI*0.25, centeredXPosition, centeredYPosition);
                contentStream.drawString( message + " " + i);
            }

            contentStream.endText();
            contentStream.close();

            // Page 2
            page = new PDPage();
            page.setMediaBox(PDPage.PAGE_SIZE_A4);
            doc.addPage(page);
            fontSize = 1.0f;

            contentStream = new PDPageContentStream(doc, page, false, false);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();

            // text scaling
            for (int i=0;i<10;i++)
            {
                contentStream.setTextScaling(12+(i*6), 12+(i*6), 100, 100+i*50);
                contentStream.drawString( message + " " +i);
            }
            contentStream.endText();
            contentStream.close();

            // Page 3
            page = new PDPage();
            page.setMediaBox(PDPage.PAGE_SIZE_A4);
            doc.addPage(page);
            fontSize = 1.0f;

            contentStream = new PDPageContentStream(doc, page, false, false);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();

            int i = 0;
            // text scaling combined with rotation 
            contentStream.setTextMatrix(12, 0, 0, 12, centeredXPosition, centeredYPosition*1.5);
            contentStream.drawString( message + " " +i++);

            contentStream.setTextMatrix(0, 18, -18, 0, centeredXPosition, centeredYPosition*1.5);
            contentStream.drawString( message + " " +i++);

            contentStream.setTextMatrix(-24, 0, 0, -24, centeredXPosition, centeredYPosition*1.5);
            contentStream.drawString( message + " " +i++);

            contentStream.setTextMatrix(0, -30, 30, 0, centeredXPosition, centeredYPosition*1.5);
            contentStream.drawString( message + " " +i++);

            contentStream.endText();
            contentStream.close();

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
     * This will create a PDF document with some examples how to use a text matrix.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        UsingTextMatrix app = new UsingTextMatrix();
        try
        {
            if( args.length != 2 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1] );
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
        System.err.println( "usage: " + this.getClass().getName() + " <Message> <output-file>" );
    }
}
