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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.util.Matrix;

/**
 * This is an example of how to use a text matrix.
 */
public class UsingTextMatrix
{
    /**
     * Constructor.
     */
    public UsingTextMatrix()
    {
    }

    /**
     * creates a sample document with some text using a text matrix.
     *
     * @param message The message to write in the file.
     * @param outfile The resulting PDF.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void doIt( String message, String  outfile ) throws IOException
    {
        // the document
        try (PDDocument doc = new PDDocument())
        {
            // Page 1
            PDFont font = new PDType1Font(FontName.HELVETICA);
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            float fontSize = 12.0f;

            PDRectangle pageSize = page.getMediaBox();
            float centeredXPosition = (pageSize.getWidth() - fontSize/1000f)/2f;
            float stringWidth = font.getStringWidth( message );
            float centeredYPosition = (pageSize.getHeight() - (stringWidth*fontSize)/1000f)/3f;

            PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();
            // counterclockwise rotation
            for (int i=0;i<8;i++) 
            {
                contentStream.setTextMatrix(Matrix.getRotateInstance(i * Math.PI * 0.25,
                        centeredXPosition, pageSize.getHeight() - centeredYPosition));
                contentStream.showText(message + " " + i);
            }
            // clockwise rotation
            for (int i=0;i<8;i++) 
            {
                contentStream.setTextMatrix(Matrix.getRotateInstance(-i*Math.PI*0.25,
                        centeredXPosition, centeredYPosition));
                contentStream.showText(message + " " + i);
            }

            contentStream.endText();
            contentStream.close();

            // Page 2
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            fontSize = 1.0f;

            contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();

            // text scaling and translation
            for (int i=0;i<10;i++)
            {
                contentStream.setTextMatrix(new Matrix(12f + (i * 6), 0, 0, 12f + (i * 6), 
                                                       100, 100f + i * 50));
                contentStream.showText(message + " " + i);
            }
            contentStream.endText();
            contentStream.close();

            // Page 3
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            fontSize = 1.0f;

            contentStream = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, false);
            contentStream.setFont( font, fontSize );
            contentStream.beginText();

            int i = 0;
            // text scaling combined with rotation 
            contentStream.setTextMatrix(new Matrix(12, 0, 0, 12, centeredXPosition, centeredYPosition*1.5f));
            contentStream.showText(message + " " + i++);

            contentStream.setTextMatrix(new Matrix(0, 18, -18, 0, centeredXPosition, centeredYPosition*1.5f));
            contentStream.showText(message + " " + i++);

            contentStream.setTextMatrix(new Matrix(-24, 0, 0, -24, centeredXPosition, centeredYPosition*1.5f));
            contentStream.showText(message + " " + i++);

            contentStream.setTextMatrix(new Matrix(0, -30, 30, 0, centeredXPosition, centeredYPosition*1.5f));
            contentStream.showText(message + " " + i++);

            contentStream.endText();
            contentStream.close();

            doc.save( outfile );
        }
    }

    /**
     * This will create a PDF document with some examples how to use a text matrix.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws IOException
    {
        UsingTextMatrix app = new UsingTextMatrix();
        if( args.length != 2 )
        {
            app.usage();
        }
        else
        {
            app.doIt( args[0], args[1] );
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
