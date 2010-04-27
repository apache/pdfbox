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
 * This is an example of how to create a page with a landscape orientation.
 * @version $Revision: 1.0 $
 */
public class CreateLandscapePDF
{
    /**
     * Constructor.
     */
    public CreateLandscapePDF()
    {
        super();
    }

    /**
     * creates a sample document with a landscape orientation and some text surrounded by a box.
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

            PDFont font = PDType1Font.HELVETICA;
            PDPage page = new PDPage();
            page.setMediaBox(PDPage.PAGE_SIZE_A4);
            page.setRotation(90);
            doc.addPage(page);
            PDRectangle pageSize = page.findMediaBox();
            float pageWidth = pageSize.getWidth();
            float fontSize = 12;
            float stringWidth = font.getStringWidth( message )*fontSize/1000f;
            float startX = 100;
            float startY = 100;
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, false, false);
            // add the rotation using the current transformation matrix
            // including a translation of pageWidth to use the lower left corner as 0,0 reference
            contentStream.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();
            contentStream.moveTextPositionByAmount(startX, startY);
            contentStream.drawString( message);
            contentStream.moveTextPositionByAmount(0, 100);
            contentStream.drawString( message);
            contentStream.moveTextPositionByAmount(100, 100);
            contentStream.drawString( message);
            contentStream.endText();
            
            contentStream.drawLine(startX-2, startY-2, startX-2, startY+200+fontSize);
            contentStream.drawLine(startX-2, startY+200+fontSize, startX+100+stringWidth+2, startY+200+fontSize);
            contentStream.drawLine(startX+100+stringWidth+2, startY+200+fontSize, startX+100+stringWidth+2, startY-2);
            contentStream.drawLine(startX+100+stringWidth+2, startY-2, startX-2, startY-2);
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
     * This will create a PDF document with a landscape orientation and some text surrounded by a box.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        CreateLandscapePDF app = new CreateLandscapePDF();
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
