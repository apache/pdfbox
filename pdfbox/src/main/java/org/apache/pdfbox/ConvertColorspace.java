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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

/**
 * This is the main program that simply parses the pdf document and replace
 * change a PDF to use a specific colorspace.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @author Pierre-Yves Landuré (pierre-yves@landure.org)
 * @version $Revision: 1.5 $
 */
public class ConvertColorspace
{

    private static final String PASSWORD = "-password";
    private static final String CONVERSION = "-equiv";
    private static final String DEST_COLORSPACE = "-toColorspace";

    /**
     * private constructor.
    */
    private ConvertColorspace()
    {
        //static class
    }

    /**
     * The method that replace RGB colors by CMYK ones.
     *
     * @param inputFile input file name.
     * @param colorEquivalents a dictionnary for the color equivalents.
     * @param destColorspace The destination colorspace, currently CMYK is supported.
     *
     * @throws IOException If there is an error parsing the document.
     */
    private void replaceColors( PDDocument inputFile,
                               Hashtable colorEquivalents,
                               String destColorspace ) throws IOException
    {
        if( !destColorspace.equals( "CMYK" ) )
        {
            throw new IOException( "Error: Unknown colorspace " + destColorspace );
        }
        List pagesList =  inputFile.getDocumentCatalog().getAllPages();

        PDPage currentPage = null;
        PDFStreamParser parser = null;
        List pageTokens = null;
        List editedPageTokens = null;

        for(int pageCounter = 0; pageCounter < pagesList.size(); pageCounter++) // For each document page
        {
            currentPage = (PDPage)pagesList.get( pageCounter );

            parser = new PDFStreamParser(currentPage.getContents().getStream());
            parser.parse();
            pageTokens = parser.getTokens();
            editedPageTokens = new ArrayList();

            for( int counter = 0; counter < pageTokens.size(); counter++) // For each page token
            {
                Object token = pageTokens.get( counter );
                if( token instanceof PDFOperator ) // Test if PDFOperator
                {
                    PDFOperator tokenOperator = (PDFOperator)token;

                    if(tokenOperator.getOperation().equals("rg")) // Test if "rg" Operator.
                    {
                        if( destColorspace.equals( "CMYK" ) )
                        {
                            replaceRGBTokensWithCMYKTokens( editedPageTokens, pageTokens, counter, colorEquivalents );
                            editedPageTokens.add( PDFOperator.getOperator( "k" ));
                        }
                    }
                    else if(tokenOperator.getOperation().equals("RG")) // Test if "rg" Operator.
                    {
                        if( destColorspace.equals( "CMYK" ) )
                        {
                            replaceRGBTokensWithCMYKTokens( editedPageTokens, pageTokens, counter, colorEquivalents );
                            editedPageTokens.add( PDFOperator.getOperator( "K" ));
                        }
                    }
                    else if(tokenOperator.getOperation().equals("g")) // Test if "rg" Operator.
                    {
                        if( destColorspace.equals( "CMYK" ) )
                        {
                            replaceGrayTokensWithCMYKTokens( editedPageTokens, pageTokens, counter, colorEquivalents );
                            editedPageTokens.add( PDFOperator.getOperator( "k" ));
                        }
                    }
                    else if(tokenOperator.getOperation().equals("G")) // Test if "rg" Operator.
                    {
                        if( destColorspace.equals( "CMYK" ) )
                        {
                            replaceGrayTokensWithCMYKTokens( editedPageTokens, pageTokens, counter, colorEquivalents );
                            editedPageTokens.add( PDFOperator.getOperator( "K" ));
                        }
                    }
                    else
                    {
                        editedPageTokens.add( token );
                    }
                }
                else // Test if PDFOperator
                {
                    editedPageTokens.add( token );
                }
            } // For each page token

            // We replace original page content by the edited one.
            PDStream updatedPageContents = new PDStream(inputFile);
            ContentStreamWriter contentWriter = new ContentStreamWriter( updatedPageContents.createOutputStream() );
            contentWriter.writeTokens( editedPageTokens );
            currentPage.setContents( updatedPageContents );

        } // For each document page
    }

    private void replaceRGBTokensWithCMYKTokens( List editedPageTokens,
                                                 List pageTokens,
                                                 int counter,
                                                 Hashtable colorEquivalents )
    {
//      Get current RGB color.
        float red = ((COSNumber)pageTokens.get( counter - 3 )).floatValue();
        float green = ((COSNumber)pageTokens.get( counter - 2 )).floatValue();
        float blue = ((COSNumber)pageTokens.get( counter - 1 )).floatValue();

        int intRed = Math.round(red * 255.0f);
        int intGreen = Math.round(green * 255.0f);
        int intBlue = Math.round(blue * 255.0f);

        ColorSpaceInstance rgbColor = new ColorSpaceInstance();
        rgbColor.colorspace = "RGB";
        rgbColor.colorspaceValues = new int[] { intRed, intGreen, intBlue };
        ColorSpaceInstance cmykColor = (ColorSpaceInstance)colorEquivalents.get(rgbColor);
        float[] cmyk = null;

        if( cmykColor != null )
        {
            cmyk = new float[] {
                cmykColor.colorspaceValues[0] / 100.0f,
                cmykColor.colorspaceValues[1] / 100.0f,
                cmykColor.colorspaceValues[2] / 100.0f,
                cmykColor.colorspaceValues[3] / 100.0f
            };
        }
        else
        {
            cmyk = convertRGBToCMYK( red, green, blue );
        }

        //remove the RGB components that are already part of the editedPageTokens list
        editedPageTokens.remove( editedPageTokens.size() -1 );
        editedPageTokens.remove( editedPageTokens.size() -1 );
        editedPageTokens.remove( editedPageTokens.size() -1 );

        // Add the new CMYK color
        editedPageTokens.add( new COSFloat( cmyk[0] ) );
        editedPageTokens.add( new COSFloat( cmyk[1] ) );
        editedPageTokens.add( new COSFloat( cmyk[2] ) );
        editedPageTokens.add( new COSFloat( cmyk[3] ) );
    }

    private void replaceGrayTokensWithCMYKTokens( List editedPageTokens,
                                                  List pageTokens,
                                                  int counter,
                                                  Hashtable colorEquivalents )
    {
//      Get current RGB color.
        float gray = ((COSNumber)pageTokens.get( counter - 1 )).floatValue();

        ColorSpaceInstance grayColor = new ColorSpaceInstance();
        grayColor.colorspace = "Grayscale";
        grayColor.colorspaceValues = new int[] { Math.round( gray * 100 ) };
        ColorSpaceInstance cmykColor = (ColorSpaceInstance)colorEquivalents.get(grayColor);
        float[] cmyk = null;

        if( cmykColor != null )
        {
            cmyk = new float[] {
                cmykColor.colorspaceValues[0] / 100.0f,
                cmykColor.colorspaceValues[1] / 100.0f,
                cmykColor.colorspaceValues[2] / 100.0f,
                cmykColor.colorspaceValues[3] / 100.0f
            };
        }
        else
        {
            cmyk = new float[] {0,0,0,gray};
        }

        //remove the Gray components that are already part of the editedPageTokens list
        editedPageTokens.remove( editedPageTokens.size() -1 );

        // Add the new CMYK color
        editedPageTokens.add( new COSFloat( cmyk[0] ) );
        editedPageTokens.add( new COSFloat( cmyk[1] ) );
        editedPageTokens.add( new COSFloat( cmyk[2] ) );
        editedPageTokens.add( new COSFloat( cmyk[3] ) );
    }

    private static float[] convertRGBToCMYK( float red, float green, float blue )
    {
        //
        // RGB->CMYK from From
        // http://en.wikipedia.org/wiki/Talk:CMYK_color_model
        //
        float c = 1.0f - red;
        float m = 1.0f - green;
        float y = 1.0f - blue;
        float k = 1.0f;

        k = Math.min( Math.min( Math.min( c,k ), m), y );

        c = ( c - k ) / ( 1 - k );
        m = ( m - k ) / ( 1 - k );
        y = ( y - k ) / ( 1 - k );
        return new float[] { c,m,y,k};
    }

    private static int[] stringToIntArray( String string )
    {
        String[] ints = string.split( "," );
        int[] retval = new int[ints.length];
        for( int i=0; i<ints.length; i++ )
        {
            retval[i] = Integer.parseInt( ints[i] );
        }
        return retval;
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        String password = "";
        String inputFile = null;
        String outputFile = null;
        String destColorspace = "CMYK";

        Pattern colorEquivalentPattern = Pattern.compile(
            "^(.*):\\((.*)\\)" +
            "=(.*):\\((.*)\\)$");
        Matcher colorEquivalentMatcher = null;

        //key= value=java.awt.Color
        Hashtable colorEquivalents = new Hashtable();

        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            if( args[i].equals( DEST_COLORSPACE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                destColorspace = args[i];
            }
            if(args[i].equals( CONVERSION ) )
            {
              i++;
              if( i >= args.length )
              {
                  usage();
              }

              colorEquivalentMatcher = colorEquivalentPattern.matcher(args[i]);
              if(!colorEquivalentMatcher.matches())
              {
                  usage();
              }
              String srcColorSpace = colorEquivalentMatcher.group(1);
              String srcColorvalues = colorEquivalentMatcher.group(2);
              String destColorSpace = colorEquivalentMatcher.group(3);
              String destColorvalues = colorEquivalentMatcher.group(4);

              ConvertColorspace.ColorSpaceInstance source = new ColorSpaceInstance();
              source.colorspace = srcColorSpace;
              source.colorspaceValues = stringToIntArray( srcColorvalues );

              ColorSpaceInstance dest = new ColorSpaceInstance();
              dest.colorspace = destColorSpace;
              dest.colorspaceValues = stringToIntArray( destColorvalues );

              colorEquivalents.put(source, dest);

            }
            else
            {
                if( inputFile == null )
                {
                    inputFile = args[i];
                }
                else
                {
                    outputFile = args[i];
                }
            }
        }

        if( inputFile == null )
        {
            usage();
        }

        if( outputFile == null || outputFile.equals(inputFile))
        {
            usage();
        }

        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( inputFile );
            if( doc.isEncrypted() )
            {
                try
                {
                    doc.decrypt( password );
                }
                catch( InvalidPasswordException e )
                {
                    if( !password.equals( "" ) )//they supplied the wrong password
                    {
                        System.err.println( "Error: The supplied password is incorrect." );
                        System.exit( 2 );
                    }
                    else
                    {
                        //they didn't suppply a password and the default of "" was wrong.
                        System.err.println( "Error: The document is encrypted." );
                        usage();
                    }
                }
            }
            ConvertColorspace converter = new ConvertColorspace();
            converter.replaceColors(doc, colorEquivalents, destColorspace );
            doc.save( outputFile );
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
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.ConvertColorspace [OPTIONS] <PDF Input file> "
            +"<PDF Output File>\n" +
            "  -password  <password>                Password to decrypt document\n" +
            "  -equiv <color equivalent>            Color equivalent to use for conversion.\n" +
            "  -destColorspace <color equivalent>   The destination colorspace, CMYK is the only '" +
            "supported colorspace." +
            "  \n" +
            " The equiv format is : <source colorspace>:(colorspace value)=<dest colorspace>:(colorspace value)" +
            " This option can be used as many times as necessary\n" +
            " The supported equiv colorspaces are RGB and CMYK.\n" +
            " RGB color values are integers between 0 and 255" +
            " CMYK color values are integer between 0 and 100.\n" +
            " Example: java org.apache.pdfbox.ConvertColorspace -equiv RGB:(255,0,0)=CMYK(0,99,100,0)" +
            " input.pdf output.pdf\n" +
            "  <PDF Input file>             The PDF document to use\n" +
            "  <PDF Output file>            The PDF file to write the result to. Must be different of input file\n"
            );
        System.exit( 1 );
    }

    /**
     *
     *
     */
    private static class ColorSpaceInstance
    {
        private String colorspace = null;
        private int[] colorspaceValues = null;

        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {
            int code = colorspace.hashCode();
            for( int i=0; i<colorspaceValues.length; i++ )
            {
                code += colorspaceValues[i];
            }
            return code;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals( Object o )
        {
            boolean retval = false;
            if( o instanceof ColorSpaceInstance )
            {
                ColorSpaceInstance other = (ColorSpaceInstance)o;
                if( this.colorspace.equals( other.colorspace ) &&
                         colorspaceValues.length == other.colorspaceValues.length )
                {
                    retval = true;
                    for( int i=0; i<colorspaceValues.length && retval; i++ )
                    {
                        retval = retval && colorspaceValues[i] == other.colorspaceValues[i];
                    }
                }
            }
            return retval;
        }
    }
}

