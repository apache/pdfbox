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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

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
    private PDFont font = null;

    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = "-fontSize", description = "the size of the font to use (default: ${DEFAULT-VALUE}")
    private int fontSize = DEFAULT_FONT_SIZE;
    
    @Option(names = "-landscape", description = "set orientation to landscape")
    private boolean landscape = false;

    @Option(names = "-pageSize", description = "the page size to use. \nCandidates: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    private PageSizes pageSize = PageSizes.LETTER;

    @Option(names = "-charset", description = "the charset to use. \n(default: ${DEFAULT-VALUE})")
    private Charset charset = Charset.defaultCharset();

    @Option(names = "-standardFont", 
        description = "the font to use for the text. Either this or -ttf should be specified but not both.\nCandidates: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
    private FontName standardFont = FontName.HELVETICA;

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

    /**
     * Constructor.
     */
    public TextToPDF()
    {
        SYSERR = System.err;
    }

    /**
     * This will create a PDF document with some text in it. <br>
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

    @Override
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
                font = new PDType1Font(standardFont);
            }

            setFont(font);
            setFontSize(fontSize);
            setMediaBox(pageSize.getPageSize());
            setLandscape(landscape);
            
            boolean hasUtf8BOM = false;
            if (charset.equals(StandardCharsets.UTF_8))
            {
                // check for utf8 BOM
                // FileInputStream doesn't support mark/reset
                try (InputStream is = new FileInputStream(infile))
                {
                    if (is.read() == 0xEF && is.read() == 0xBB && is.read() == 0xBF)
                    {
                        hasUtf8BOM = true;
                    }
                }
            }
            try (InputStream is = new FileInputStream(infile))
            {
                if (hasUtf8BOM)
                {
                    long skipped = is.skip(3);
                    if (skipped != 3)
                    {
                        throw new IOException("Could not skip 3 bytes, size changed?!");
                    }
                }
                try (Reader reader = new InputStreamReader(is, charset))
                {
                    createPDFFromText(doc, reader);
                }
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
        // for some reason the font isn't initialized -> use default font
        if (font == null)
        {
            font = new PDType1Font(standardFont);
        }
        final int margin = 40;
        float height = font.getBoundingBox().getHeight() / FONTSCALE;
        PDRectangle actualMediaBox =
                landscape ? new PDRectangle(mediaBox.getHeight(), mediaBox.getWidth()) : mediaBox;

        //calculate font height and increase by a factor.
        height = height * fontSize * LINE_HEIGHT_FACTOR;
        BufferedReader data = new BufferedReader(text);
        String nextLine;
        PDPage page = new PDPage(actualMediaBox);
        PDPageContentStream contentStream = null;
        float y = -1;
        float maxStringLength = page.getMediaBox().getWidth() - 2 * margin;

        // There is a special case of creating a PDF document from an empty string.
        boolean textIsEmpty = true;

        StringBuilder nextLineToDraw = new StringBuilder();

        while ((nextLine = data.readLine()) != null)
        {
            // The input text is nonEmpty. New pages will be created and added
            // to the PDF document as they are needed, depending on the length of
            // the text.
            textIsEmpty = false;

            String[] lineWords = nextLine.replaceAll("[\\n\\r]+$", "").split(" ", -1);
            int lineIndex = 0;
            while (lineIndex < lineWords.length)
            {
                nextLineToDraw.setLength(0);
                boolean addSpace = false;
                float lengthIfUsingNextWord = 0;
                boolean ff = false;
                do
                {
                    String word1, word2 = "";
                    String word = lineWords[lineIndex];
                    int indexFF = word.indexOf('\f');
                    if (indexFF == -1)
                    {
                        word1 = word;
                    }
                    else
                    {
                        ff = true;
                        word1 = word.substring(0, indexFF);
                        if (indexFF < word.length())
                        {
                            word2 = word.substring(indexFF + 1);
                        }
                    }
                    // word1 is the part before ff, word2 after
                    // both can be empty
                    // word1 can also be empty without ff, if a line has many spaces
                    if (!word1.isEmpty() || !ff)
                    {
                        if (addSpace)
                        {
                            nextLineToDraw.append(' ');
                        }
                        else
                        {
                            addSpace = true;
                        }
                        nextLineToDraw.append(word1);
                    }
                    if (!ff || word2.isEmpty())
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
                    if (lineIndex < lineWords.length)
                    {
                        // need cut off at \f in next word to avoid IllegalArgumentException
                        String nextWord = lineWords[lineIndex];
                        indexFF = nextWord.indexOf('\f');
                        if (indexFF != -1)
                        {
                            nextWord = nextWord.substring(0, indexFF);
                        }

                        String lineWithNextWord = nextLineToDraw + " " + nextWord;
                        lengthIfUsingNextWord
                                = (font.getStringWidth(lineWithNextWord) / FONTSCALE) * fontSize;
                    }
                }
                while (lineIndex < lineWords.length && lengthIfUsingNextWord < maxStringLength);

                if (y < margin)
                {
                    // We have crossed the end-of-page boundary and need to extend the
                    // document by another page.
                    page = new PDPage(actualMediaBox);
                    doc.addPage(page);
                    if (contentStream != null)
                    {
                        contentStream.endText();
                        contentStream.close();
                    }
                    contentStream = new PDPageContentStream(doc, page);
                    contentStream.setFont(font, fontSize);
                    contentStream.beginText();
                    y = page.getMediaBox().getHeight() - margin + height;
                    contentStream.newLineAtOffset(margin, y);
                }

                if (contentStream == null)
                {
                    throw new IOException("Error:Expected non-null content stream.");
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

        if (contentStream != null)
        {
            contentStream.endText();
            contentStream.close();
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
     * @param landscape true for landscape orientation
     */
    public void setLandscape(boolean landscape)
    {
        this.landscape = landscape;
    }
}
