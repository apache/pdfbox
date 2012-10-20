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
package org.apache.pdfbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


/**
 * This will take a text file and ouput a pdf with that text.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class TextToPDF
{
    private int fontSize = 10;
    private PDSimpleFont font = PDType1Font.HELVETICA;

    /**
     * Create a PDF document with some text.
     *
     * @param text The stream of text data.
     *
     * @return The document with the text in it.
     *
     * @throws IOException If there is an error writing the data.
     */
    public PDDocument createPDFFromText( Reader text ) throws IOException
    {
        PDDocument doc = null;
        try
        {

            final int margin = 40;
            float height = font.getFontDescriptor().getFontBoundingBox().getHeight()/1000;

            //calculate font height and increase by 5 percent.
            height = height*fontSize*1.05f;
            doc = new PDDocument();
            BufferedReader data = new BufferedReader( text );
            String nextLine = null;
            PDPage page = new PDPage();
            PDPageContentStream contentStream = null;
            float y = -1;
            float maxStringLength = page.getMediaBox().getWidth() - 2*margin;

            // There is a special case of creating a PDF document from an empty string.
            boolean textIsEmpty = true;

            while( (nextLine = data.readLine()) != null )
            {

                // The input text is nonEmpty. New pages will be created and added
                // to the PDF document as they are needed, depending on the length of
                // the text.
                textIsEmpty = false;

                String[] lineWords = nextLine.trim().split( " " );
                int lineIndex = 0;
                while( lineIndex < lineWords.length )
                {
                    StringBuffer nextLineToDraw = new StringBuffer();
                    float lengthIfUsingNextWord = 0;
                    do
                    {
                        nextLineToDraw.append( lineWords[lineIndex] );
                        nextLineToDraw.append( " " );
                        lineIndex++;
                        if( lineIndex < lineWords.length )
                        {
                            String lineWithNextWord = nextLineToDraw.toString() + lineWords[lineIndex];
                            lengthIfUsingNextWord =
                                (font.getStringWidth( lineWithNextWord )/1000) * fontSize;
                        }
                    }
                    while( lineIndex < lineWords.length &&
                           lengthIfUsingNextWord < maxStringLength );
                    if( y < margin )
                    {
                        // We have crossed the end-of-page boundary and need to extend the
                        // document by another page.
                        page = new PDPage();
                        doc.addPage( page );
                        if( contentStream != null )
                        {
                            contentStream.endText();
                            contentStream.close();
                        }
                        contentStream = new PDPageContentStream(doc, page);
                        contentStream.setFont( font, fontSize );
                        contentStream.beginText();
                        y = page.getMediaBox().getHeight() - margin + height;
                        contentStream.moveTextPositionByAmount(
                            margin, y );

                    }
                    //System.out.println( "Drawing string at " + x + "," + y );

                    if( contentStream == null )
                    {
                        throw new IOException( "Error:Expected non-null content stream." );
                    }
                    contentStream.moveTextPositionByAmount( 0, -height);
                    y -= height;
                    contentStream.drawString( nextLineToDraw.toString() );
                }


            }

            // If the input text was the empty string, then the above while loop will have short-circuited
            // and we will not have added any PDPages to the document.
            // So in order to make the resultant PDF document readable by Adobe Reader etc, we'll add an empty page.
            if (textIsEmpty)
            {
                doc.addPage(page);
            }

            if( contentStream != null )
            {
                contentStream.endText();
                contentStream.close();
            }
        }
        catch( IOException io )
        {
            if( doc != null )
            {
                doc.close();
            }
            throw io;
        }
        return doc;
    }

    /**
     * This will create a PDF document with some text in it.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     *
     * @throws IOException If there is an error with the PDF.
     */
    public static void main(String[] args) throws IOException
    {
        TextToPDF app = new TextToPDF();
        PDDocument doc = null;
        try
        {
            if( args.length < 2 )
            {
                app.usage();
            }
            else
            {
                for( int i=0; i<args.length-2; i++ )
                {
                    if( args[i].equals( "-standardFont" ))
                    {
                        i++;
                        app.setFont( PDType1Font.getStandardFont( args[i] ));
                    }
                    else if( args[i].equals( "-ttf" ))
                    {
                        i++;
                        PDTrueTypeFont font = PDTrueTypeFont.loadTTF( doc, new File( args[i]));
                        app.setFont( font );
                    }
                    else if( args[i].equals( "-fontSize" ))
                    {
                        i++;
                        app.setFontSize( Integer.parseInt( args[i] ) );
                    }
                    else
                    {
                        throw new IOException( "Unknown argument:" + args[i] );
                    }
                }
                doc = app.createPDFFromText( new FileReader( args[args.length-1] ) );
                doc.save( args[args.length-2] );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        String[] std14 = PDType1Font.getStandard14Names();
        System.err.println( "usage: jar -jar pdfbox-app-x.y.z.jar TextToPDF [options] <output-file> <text-file>" );
        System.err.println( "    -standardFont <name>    default:" + PDType1Font.HELVETICA.getBaseFont() );
        for( int i=0; i<std14.length; i++ )
        {
            System.err.println( "                                    " + std14[i] );
        }
        System.err.println( "    -ttf <ttf file>         The TTF font to use.");
        System.err.println( "    -fontSize <fontSize>    default:10" );


    }
    /**
     * @return Returns the font.
     */
    public PDSimpleFont getFont()
    {
        return font;
    }
    /**
     * @param aFont The font to set.
     */
    public void setFont(PDSimpleFont aFont)
    {
        this.font = aFont;
    }
    /**
     * @return Returns the fontSize.
     */
    public int getFontSize()
    {
        return fontSize;
    }
    /**
     * @param aFontSize The fontSize to set.
     */
    public void setFontSize(int aFontSize)
    {
        this.fontSize = aFontSize;
    }
}
