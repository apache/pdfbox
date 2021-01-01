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
import java.io.PrintStream;
import java.io.Reader;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This will take a text file and output a pdf with that text.
 *
 * @author Ben Litchfield
 */
@Command(name = "texttopdf", header = "Creates a PDF document from text", versionProvider = Version.class, mixinStandardHelpOptions = true)
public class TextToPDF implements Callable<Integer>
{
    /**
     * The scaling factor for font units to PDF units
     */
    private static final int FONTSCALE = 1000;

    /**
     * The default font size
     */
    private static final int DEFAULT_FONT_SIZE = 10;
    
    /**
     * The line height as a factor of the font size
     */
    private static final float LINE_HEIGHT_FACTOR = 1.05f;

    private PDRectangle mediaBox = PDRectangle.LETTER;
    private PDFont font = Standard14Fonts.HELVETICA.getFont();

    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Option(names = "-fontSize", description = "the size of the font to use (default: ${DEFAULT-VALUE}")
    private int fontSize = DEFAULT_FONT_SIZE;
    
    @Option(names = "-landscape", description = "set orientation to landscape")
    private boolean landscape = false;

    @Option(names = "-pageSize", description = "the page size to use. \nCandidates: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    private PageSizes pageSize = PageSizes.LETTER;

    @Option(names = "-standardFont", 
        description = "the font to use for the text. Either this or -ttf should be specified but not both.\nCandidates: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    private Standard14Fonts standardFont = Standard14Fonts.HELVETICA;

    @Option(names = "-ttf", paramLabel="<ttf file>", description = "the TTF font to use for the text. Either this or -standardFont should be specified but not both.")
    private File ttf;

    @Option(names = {"-i", "--input"}, description = "the text file to convert", required = true)
    private File infile;

    @Option(names = {"-o", "--output"}, description = "the generated PDF file", required = true)
    private File outfile;


    private enum PageSizes
    {
        LETTER(PDRectangle.LETTER),
        LEGAL(PDRectangle.LEGAL),
        A0(PDRectangle.A0),
        A1(PDRectangle.A1),
        A2(PDRectangle.A2),
        A3(PDRectangle.A3),
        A4(PDRectangle.A4),
        A5(PDRectangle.A5),
        A6(PDRectangle.A6);

        final PDRectangle pageSize;

        private PageSizes(PDRectangle pageSize)
        {
            this.pageSize = pageSize;
        }

        public PDRectangle getPageSize()
        {
            return this.pageSize;
        }
    }

    private enum Standard14Fonts
    {
        TIMES_ROMAN(PDType1Font.TIMES_ROMAN.getBaseFont(), PDType1Font.TIMES_ROMAN),
        TIMES_BOLD(PDType1Font.TIMES_BOLD.getBaseFont(), PDType1Font.TIMES_BOLD),
        TIMES_ITALIC(PDType1Font.TIMES_ITALIC.getBaseFont(), PDType1Font.TIMES_ITALIC),
        TIMES_BOLD_ITALIC(PDType1Font.TIMES_BOLD_ITALIC.getBaseFont(), PDType1Font.TIMES_BOLD_ITALIC),
        HELVETICA(PDType1Font.HELVETICA.getBaseFont(), PDType1Font.HELVETICA),
        HELVETICA_BOLD(PDType1Font.HELVETICA_BOLD.getBaseFont(), PDType1Font.HELVETICA_BOLD),
        HELVETICA_OBLIQUE(PDType1Font.HELVETICA_OBLIQUE.getBaseFont(), PDType1Font.HELVETICA_OBLIQUE),
        HELVETICA_BOLD_OBLIQUE(PDType1Font.HELVETICA_BOLD_OBLIQUE.getBaseFont(), PDType1Font.HELVETICA_BOLD_OBLIQUE),
        COURIER(PDType1Font.COURIER.getBaseFont(), PDType1Font.COURIER),
        COURIER_BOLD(PDType1Font.COURIER_BOLD.getBaseFont(), PDType1Font.COURIER_BOLD),
        COURIER_OBLIQUE(PDType1Font.COURIER_OBLIQUE.getBaseFont(), PDType1Font.COURIER_OBLIQUE),
        COURIER_BOLD_OBLIQUE(PDType1Font.COURIER_BOLD_OBLIQUE.getBaseFont(), PDType1Font.COURIER_BOLD_OBLIQUE),
        SYMBOL(PDType1Font.SYMBOL.getBaseFont(), PDType1Font.SYMBOL),
        ZAPF_DINGBATS(PDType1Font.ZAPF_DINGBATS.getBaseFont(), PDType1Font.ZAPF_DINGBATS);

        final String displayName;
        final PDFont font;

        private Standard14Fonts(String displayName, PDFont font)
        {
            this.displayName = displayName;
            this.font = font;
        }

        public PDFont getFont()
        {
            return font;
        }
        

        @Override 
        public String toString() { 
            return this.displayName; 
        }
    }

    /**
     * This will create a PDF document with some text in it.
     * <br>
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new TextToPDF()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        try (PDDocument doc = new PDDocument())
        {
            if (ttf != null)
            {
                font = PDType0Font.load(doc, ttf);
            }
            else
            {
                font = standardFont.getFont();
            }

            setFont(font);
            setFontSize(fontSize);
            setMediaBox(pageSize.getPageSize());
            setLandscape(landscape);

            try (FileReader fileReader = new FileReader(infile))
            {
                createPDFFromText(doc, fileReader);
            }
            doc.save(outfile);
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error converting text to PDF [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
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
