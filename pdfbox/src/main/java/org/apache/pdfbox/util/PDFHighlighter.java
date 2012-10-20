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
package org.apache.pdfbox.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;


/**
 * Highlighting of words in a PDF document with an XML file.
 *
 * @author slagraulet (slagraulet@cardiweb.com)
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 *
 * @see <a href="http://partners.adobe.com/public/developer/en/pdf/HighlightFileFormat.pdf">
 *      Adobe Highlight File Format</a>
 */
public class PDFHighlighter extends PDFTextStripper
{
    private Writer highlighterOutput = null;
    //private Color highlightColor = Color.YELLOW;

    private String[] searchedWords;
    private ByteArrayOutputStream textOS = null;
    private Writer textWriter = null;
    private static final String ENCODING = "UTF-16";

    /**
     * Default constructor.
     *
     * @throws IOException If there is an error constructing this class.
     */
    public PDFHighlighter() throws IOException
    {
        super(ENCODING);
        super.setLineSeparator( "" );
        super.setPageSeparator( "" );
        super.setWordSeparator( "" );
        super.setShouldSeparateByBeads( false );
        super.setSuppressDuplicateOverlappingText( false );
    }

    /**
     * Generate an XML highlight string based on the PDF.
     *
     * @param pdDocument The PDF to find words in.
     * @param highlightWord The word to search for.
     * @param xmlOutput The resulting output xml file.
     *
     * @throws IOException If there is an error reading from the PDF, or writing to the XML.
     */
    public void generateXMLHighlight(PDDocument pdDocument, String highlightWord, Writer xmlOutput ) throws IOException
    {
        generateXMLHighlight( pdDocument, new String[] { highlightWord }, xmlOutput );
    }

    /**
     * Generate an XML highlight string based on the PDF.
     *
     * @param pdDocument The PDF to find words in.
     * @param sWords The words to search for.
     * @param xmlOutput The resulting output xml file.
     *
     * @throws IOException If there is an error reading from the PDF, or writing to the XML.
     */
    public void generateXMLHighlight(PDDocument pdDocument, String[] sWords, Writer xmlOutput ) throws IOException
    {
        highlighterOutput = xmlOutput;
        searchedWords = sWords;
        highlighterOutput.write("<XML>\n<Body units=characters " +
                                //color and mode are not implemented by the highlight spec
                                //so don't include them for now
                                //" color=#" + getHighlightColorAsString() +
                                //" mode=active " + */
                                " version=2>\n<Highlight>\n");
        textOS = new ByteArrayOutputStream();
        textWriter = new OutputStreamWriter( textOS, ENCODING);
        writeText(pdDocument, textWriter);
        highlighterOutput.write("</Highlight>\n</Body>\n</XML>");
        highlighterOutput.flush();
    }

    /**
     * {@inheritDoc}
     */
    protected void endPage( PDPage pdPage ) throws IOException
    {
        textWriter.flush();

        String page = new String( textOS.toByteArray(), ENCODING );
        textOS.reset();
        //page = page.replaceAll( "\n", "" );
        //page = page.replaceAll( "\r", "" );
        //page = CCRStringUtil.stripChar(page, '\n');
        //page = CCRStringUtil.stripChar(page, '\r');

        // Traitement des listes � puces (caract�res sp�ciaux)
        if (page.indexOf("a") != -1)
        {
            page = page.replaceAll("a[0-9]{1,3}", ".");
        }

        for (int i = 0; i < searchedWords.length; i++)
        {
            Pattern pattern = Pattern.compile(searchedWords[i], Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(page);
            while( matcher.find() )
            {
                int begin = matcher.start();
                int end = matcher.end();
                highlighterOutput.write("    <loc " +
                        "pg=" + (getCurrentPageNo()-1)
                        + " pos=" + begin
                        + " len="+ (end - begin)
                        + ">\n");
            }
        }
    }

    /**
     * Command line application.
     *
     * @param args The command line arguments to the application.
     *
     * @throws IOException If there is an error generating the highlight file.
     */
    public static void main(String[] args) throws IOException
    {
        PDFHighlighter xmlExtractor = new PDFHighlighter();
        PDDocument doc = null;
        try
        {
            if( args.length < 2 )
            {
                usage();
            }
            String[] highlightStrings = new String[ args.length - 1];
            System.arraycopy( args, 1, highlightStrings, 0, highlightStrings.length );
            doc = PDDocument.load( args[0] );

            xmlExtractor.generateXMLHighlight(
                doc,
                highlightStrings,
                new OutputStreamWriter( System.out ) );
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    private static void usage()
    {
        System.err.println( "usage: java " + PDFHighlighter.class.getName() + " <pdf file> word1 word2 word3 ..." );
        System.exit( 1 );
    }


    /**
     * Get the color to highlight the strings with.  Default is Color.YELLOW.
     *
     * @return The color to highlight strings with.
     */
    /*public Color getHighlightColor()
    {
        return highlightColor;
    }**/

    /**
     * Get the color to highlight the strings with.  Default is Color.YELLOW.
     *
     * @param color The color to highlight strings with.
     */
    /*public void setHighlightColor(Color color)
    {
        this.highlightColor = color;
    }**/

    /**
     * Set the highlight color using HTML like rgb string.  The string must be 6 characters long.
     *
     * @param color The color to use for highlighting.  Should be in the format of "FF0000".
     */
    /*public void setHighlightColor( String color )
    {
        highlightColor = Color.decode( color );
    }**/

    /**
     * Get the highlight color as an HTML like string.  This will return a string of six characters.
     *
     * @return The current highlight color.  For example FF0000
     */
    /*public String getHighlightColorAsString()
    {
        //BJL: kudos to anyone that has a cleaner way of doing this!
        String red = Integer.toHexString( highlightColor.getRed() );
        String green = Integer.toHexString( highlightColor.getGreen() );
        String blue = Integer.toHexString( highlightColor.getBlue() );

        return (red.length() < 2 ? "0" + red : red) +
               (green.length() < 2 ? "0" + green : green) +
               (blue.length() < 2 ? "0" + blue : blue);
    }**/
}
