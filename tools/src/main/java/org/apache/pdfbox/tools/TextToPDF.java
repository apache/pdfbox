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
package org.apache.pdfbox.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * This will take a text file and ouput a pdf with that text.
 *
 * @author Ben Litchfield
 */
public class TextToPDF
{
    /**
     * The scaling factor for font units to PDF units
     */
    private static final int FONTSCALE = 1000;
    
    /**
     * The default font
     */
    private static final PDType1Font DEFAULT_FONT = PDType1Font.HELVETICA;

    /**
     * The default font size
     */
    private static final int DEFAULT_FONT_SIZE = 10;
    
    /**
     * The line height as a factor of the font size
     */
    private static final float LINE_HEIGHT_FACTOR = 1.05f;

    private int fontSize = DEFAULT_FONT_SIZE;
    private PDRectangle mediaBox = PDRectangle.LETTER;
    private boolean landscape = false;
    private PDFont font = DEFAULT_FONT;

    private static final Map<String, PDType1Font> STANDARD_14 = new HashMap<>();
    static
    {
        STANDARD_14.put(PDType1Font.TIMES_ROMAN.getBaseFont(), PDType1Font.TIMES_ROMAN);
        STANDARD_14.put(PDType1Font.TIMES_BOLD.getBaseFont(), PDType1Font.TIMES_BOLD);
        STANDARD_14.put(PDType1Font.TIMES_ITALIC.getBaseFont(), PDType1Font.TIMES_ITALIC);
        STANDARD_14.put(PDType1Font.TIMES_BOLD_ITALIC.getBaseFont(), PDType1Font.TIMES_BOLD_ITALIC);
        STANDARD_14.put(PDType1Font.HELVETICA.getBaseFont(), PDType1Font.HELVETICA);
        STANDARD_14.put(PDType1Font.HELVETICA_BOLD.getBaseFont(), PDType1Font.HELVETICA_BOLD);
        STANDARD_14.put(PDType1Font.HELVETICA_OBLIQUE.getBaseFont(), PDType1Font.HELVETICA_OBLIQUE);
        STANDARD_14.put(PDType1Font.HELVETICA_BOLD_OBLIQUE.getBaseFont(), PDType1Font.HELVETICA_BOLD_OBLIQUE);
        STANDARD_14.put(PDType1Font.COURIER.getBaseFont(), PDType1Font.COURIER);
        STANDARD_14.put(PDType1Font.COURIER_BOLD.getBaseFont(), PDType1Font.COURIER_BOLD);
        STANDARD_14.put(PDType1Font.COURIER_OBLIQUE.getBaseFont(), PDType1Font.COURIER_OBLIQUE);
        STANDARD_14.put(PDType1Font.COURIER_BOLD_OBLIQUE.getBaseFont(), PDType1Font.COURIER_BOLD_OBLIQUE);
        STANDARD_14.put(PDType1Font.SYMBOL.getBaseFont(), PDType1Font.SYMBOL);
        STANDARD_14.put(PDType1Font.ZAPF_DINGBATS.getBaseFont(), PDType1Font.ZAPF_DINGBATS);
    }

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
        PDDocument doc = new PDDocument();
        createPDFFromText(doc, text);
        return doc;
    }

    /**
     * Create a PDF document with some text.
     *
     * @param doc The document.
     * @param text The stream of text data.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void createPDFFromText( PDDocument doc, Reader text ) throws IOException
    {
        try
        {

            final int margin = 40;
            float height = font.getBoundingBox().getHeight() / FONTSCALE;
            PDRectangle actualMediaBox = mediaBox;
            if (landscape)
            {
                actualMediaBox = new PDRectangle(mediaBox.getHeight(), mediaBox.getWidth());
            }

            //calculate font height and increase by a factor.
            height = height*fontSize*LINE_HEIGHT_FACTOR;
            BufferedReader data = new BufferedReader( text );
            String nextLine;
            PDPage page = new PDPage(actualMediaBox);
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

                String[] lineWords = nextLine.replaceAll("[\\n\\r]+$", "").split(" ");
                int lineIndex = 0;
                while( lineIndex < lineWords.length )
                {
                    StringBuilder nextLineToDraw = new StringBuilder();
                    float lengthIfUsingNextWord = 0;
                    boolean ff = false;
                    do
                    {
                        String word1, word2 = "";
                        int indexFF = lineWords[lineIndex].indexOf('\f');
                        if (indexFF == -1)
                        {
                            word1 = lineWords[lineIndex];
                        }
                        else
                        {
                            ff = true;
                            word1 = lineWords[lineIndex].substring(0, indexFF);
                            if (indexFF < lineWords[lineIndex].length())
                            {
                                word2 = lineWords[lineIndex].substring(indexFF + 1);
                            }
                        }
                        // word1 is the part before ff, word2 after
                        // both can be empty
                        // word1 can also be empty without ff, if a line has many spaces
                        if (word1.length() > 0 || !ff)
                        {
                            nextLineToDraw.append(word1);
                            nextLineToDraw.append(" ");
                        }
                        if (!ff || word2.length() == 0)
                        {
                            lineIndex++;
                        }
                        else
                        {
                            lineWords[lineIndex] = word2;
                        }
                        if (ff)
                        {
                            break;
                        }
                        if( lineIndex < lineWords.length )
                        {
                            // need cut off at \f in next word to avoid IllegalArgumentException
                            String nextWord = lineWords[lineIndex];
                            indexFF = nextWord.indexOf('\f');
                            if (indexFF != -1)
                            {
                                nextWord = nextWord.substring(0, indexFF);
                            }
                            
                            String lineWithNextWord = nextLineToDraw.toString() + " " + nextWord;
                            lengthIfUsingNextWord =
                                (font.getStringWidth( lineWithNextWord )/FONTSCALE) * fontSize;
                        }
                    }
                    while (lineIndex < lineWords.length && lengthIfUsingNextWord < maxStringLength);

                    if( y < margin )
                    {
                        // We have crossed the end-of-page boundary and need to extend the
                        // document by another page.
                        page = new PDPage(actualMediaBox);
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
                        contentStream.newLineAtOffset(margin, y);
                    }

                    if( contentStream == null )
                    {
                        throw new IOException( "Error:Expected non-null content stream." );
                    }
                    contentStream.newLineAtOffset(0, -height);
                    y -= height;
                    contentStream.showText(nextLineToDraw.toString());
                    if (ff)
                    {
                        page = new PDPage(actualMediaBox);
                        doc.addPage(page);
                        contentStream.endText();
                        contentStream.close();
                        contentStream = new PDPageContentStream(doc, page);
                        contentStream.setFont(font, fontSize);
                        contentStream.beginText();
                        y = page.getMediaBox().getHeight() - margin + height;
                        contentStream.newLineAtOffset(margin, y);
                    }
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
    }

    /**
     * This will create a PDF document with some text in it.
     * <br>
     * see usage() for commandline
     *
     * @param args Command line arguments.
     *
     * @throws IOException If there is an error with the PDF.
     */
    public static void main(String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        TextToPDF app = new TextToPDF();
                
        try (PDDocument doc = new PDDocument())
        {
            if (args.length < 2)
            {
                app.usage();
            }
            else
            {
                for( int i=0; i<args.length-2; i++ )
                {
                    switch (args[i])
                    {
                        case "-standardFont":
                            i++;
                            app.setFont(getStandardFont(args[i]));
                            break;
                        case "-ttf":
                            i++;
                            PDFont font = PDType0Font.load(doc, new File(args[i]));
                            app.setFont(font);
                            break;
                        case "-fontSize":
                            i++;
                            app.setFontSize(Integer.parseInt(args[i]));
                            break;
                        case "-pageSize":
                            i++;
                            PDRectangle rectangle = createRectangle(args[i]);
                            if (rectangle == null)
                            {
                                throw new IOException("Unknown argument: " + args[i]);
                            }
                            app.setMediaBox(rectangle);
                            break;
                        case "-landscape":
                            app.setLandscape(true);
                            break;
                        default:
                            throw new IOException("Unknown argument: " + args[i]);
                    }
                }

                try (FileReader fileReader = new FileReader(args[args.length - 1]))
                {
                    app.createPDFFromText(doc, fileReader);
                }
                doc.save(args[args.length - 2]);
            }
        }
    }

    private static PDRectangle createRectangle( String paperSize )
    {
        if ("letter".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.LETTER;
        }
        else if ("legal".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.LEGAL;
        }
        else if ("A0".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A0;
        }
        else if ("A1".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A1;
        }
        else if ("A2".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A2;
        }
        else if ("A3".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A3;
        }
    	else if ("A4".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A4;
        }
        else if ("A5".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A5;
        }
        else if ("A6".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A6;
        }
        else
        {
            return null;
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        String[] std14 = getStandard14Names();
        
        StringBuilder message = new StringBuilder();       
        message.append("Usage: jar -jar pdfbox-app-x.y.z.jar TextToPDF [options] <outputfile> <textfile>\n");
        message.append("\nOptions:\n");
        message.append("  -standardFont <name> : ").append(DEFAULT_FONT.getBaseFont()).append(" (default)\n");

        for (String std14String : std14)
        {
            message.append("                         ").append(std14String).append("\n");
        }
        message.append("  -ttf <ttf file>      : The TTF font to use.\n");
        message.append("  -fontSize <fontSize> : default: ").append(DEFAULT_FONT_SIZE).append("\n");
        message.append("  -pageSize <pageSize> : Letter (default)\n");
        message.append("                         Legal\n");
        message.append("                         A0\n");
        message.append("                         A1\n");
        message.append("                         A2\n");
        message.append("                         A3\n");
        message.append("                         A4\n");
        message.append("                         A5\n");
        message.append("                         A6\n");
        message.append("  -landscape           : sets orientation to landscape" );

        System.err.println(message.toString());
        System.exit(1);
    }


    /**
     * A convenience method to get one of the standard 14 font from name.
     *
     * @param name The name of the font to get.
     *
     * @return The font that matches the name or null if it does not exist.
     */
    private static PDType1Font getStandardFont(String name)
    {
        return STANDARD_14.get(name);
    }

    /**
     * This will get the names of the standard 14 fonts.
     *
     * @return An array of the names of the standard 14 fonts.
     */
    private static String[] getStandard14Names()
    {
        return STANDARD_14.keySet().toArray(new String[14]);
    }


    /**
     * @return Returns the font.
     */
    public PDFont getFont()
    {
        return font;
    }
    /**
     * @param aFont The font to set.
     */
    public void setFont(PDFont aFont)
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

    /**
     * Sets page size of produced PDF.
     *
     * @return returns the page size (media box)
     */
    public PDRectangle getMediaBox()
    {
        return mediaBox;
    }

    /**
     * Sets page size of produced PDF.
     *
     * @param mediaBox
     */
    public void setMediaBox(PDRectangle mediaBox)
    {
        this.mediaBox = mediaBox;
    }

    /**
     * Tells the paper orientation.
     *
     * @return true for landscape orientation
     */
    public boolean isLandscape()
    {
        return landscape;
    }

    /**
     * Sets paper orientation.
     *
     * @param landscape
     */
    public void setLandscape(boolean landscape)
    {
        this.landscape = landscape;
    }
}
