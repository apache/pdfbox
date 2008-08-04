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
package org.pdfbox.examples.pdmodel;

import java.io.IOException;
import java.util.List;

import org.pdfbox.exceptions.COSVisitorException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.edit.PDPageContentStream;

import org.pdfbox.pdmodel.font.PDFont;
import org.pdfbox.pdmodel.font.PDType1Font;


/**
 * This is an example of how to add a message to every page
 * in a pdf document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
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
     * @throws COSVisitorException If there is an error writing the PDF.
     */
    public void doIt( String file, String message, String  outfile ) throws IOException, COSVisitorException
    {
        // the document
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( file );
            
            List allPages = doc.getDocumentCatalog().getAllPages();
            PDFont font = PDType1Font.HELVETICA_BOLD;
            float fontSize = 12.0f;
            
            for( int i=0; i<allPages.size(); i++ )
            {
                PDPage page = (PDPage)allPages.get( i );
                PDRectangle pageSize = page.findMediaBox();
                float stringWidth = font.getStringWidth( message );
                float centeredPosition = (pageSize.getWidth() - (stringWidth*fontSize)/1000f)/2f;
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                contentStream.beginText();
                contentStream.setFont( font, fontSize );
                contentStream.moveTextPositionByAmount( centeredPosition, 30 );
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
    public static void main(String[] args)
    {
        AddMessageToEachPage app = new AddMessageToEachPage();
        try
        {
            if( args.length != 3 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1], args[2] );
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
        System.err.println( "usage: " + this.getClass().getName() + " <input-file> <Message> <output-file>" );
    }
}