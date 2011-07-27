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

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFImageWriter;

/**
 * Convert a PDF document to an image.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDFToImage
{

    private static final String PASSWORD = "-password";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String IMAGE_FORMAT = "-imageType";
    private static final String OUTPUT_PREFIX = "-outputPrefix";
    private static final String COLOR = "-color";
    private static final String RESOLUTION = "-resolution";
    private static final String CROPBOX = "-cropbox";

    /**
     * private constructor.
    */
    private PDFToImage()
    {
        //static class
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
        String pdfFile = null;
        String outputPrefix = null;
        String imageFormat = "jpg";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        String color = "rgb";
        int resolution;
        float cropBoxLowerLeftX = 0;
        float cropBoxLowerLeftY = 0;
        float cropBoxUpperRightX = 0;
        float cropBoxUpperRightY = 0;
        try
        {
            resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        catch( HeadlessException e )
        {
            resolution = 96;
        }
        for( int i = 0; i < args.length; i++ )
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
            else if( args[i].equals( START_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                startPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( END_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                endPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( IMAGE_FORMAT ) )
            {
                i++;
                imageFormat = args[i];
            }
            else if( args[i].equals( OUTPUT_PREFIX ) )
            {
                i++;
                outputPrefix = args[i];
            }
            else if( args[i].equals( COLOR ) )
            {
                i++;
                color = args[i];
            }
            else if( args[i].equals( RESOLUTION ) )
            {
                i++;
                resolution = Integer.parseInt(args[i]);
            }
            else if( args[i].equals( CROPBOX ) )
            {
                i++;
                cropBoxLowerLeftX = Float.valueOf(args[i]).floatValue();
                i++;
                cropBoxLowerLeftY = Float.valueOf(args[i]).floatValue();
                i++;
                cropBoxUpperRightX = Float.valueOf(args[i]).floatValue();
                i++;
                cropBoxUpperRightY = Float.valueOf(args[i]).floatValue();
            }
            else
            {
                if( pdfFile == null )
                {
                    pdfFile = args[i];
                }
            }
        }
        if( pdfFile == null )
        {
            usage();
        }
        else
        {
            if(outputPrefix == null)
            {
                outputPrefix = pdfFile.substring( 0, pdfFile.lastIndexOf( '.' ));
            }

            PDDocument document = null;
            try
            {
                document = PDDocument.load( pdfFile );


                //document.print();
                if( document.isEncrypted() )
                {
                    try
                    {
                        document.decrypt( password );
                    }
                    catch( InvalidPasswordException e )
                    {
                        if( args.length == 4 )//they supplied the wrong password
                        {
                            System.err.println( "Error: The supplied password is incorrect." );
                            System.exit( 2 );
                        }
                        else
                        {
                            //they didn't supply a password and the default of "" was wrong.
                            System.err.println( "Error: The document is encrypted." );
                            usage();
                        }
                    }
                }
                int imageType = 24;
                if ("bilevel".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_BYTE_BINARY;
                }
                else if ("indexed".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_BYTE_INDEXED;
                }
                else if ("gray".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_BYTE_GRAY;
                }
                else if ("rgb".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_INT_RGB;
                }
                else if ("rgba".equalsIgnoreCase(color))
                {
                    imageType = BufferedImage.TYPE_INT_ARGB;
                }
                else
                {
                    System.err.println( "Error: the number of bits per pixel must be 1, 8 or 24." );
                    System.exit( 2 );
                }

                //if a CropBox has been specified, update the CropBox:
                //changeCropBoxes(PDDocument document,float a, float b, float c,float d)
                if ( cropBoxLowerLeftX!=0 || cropBoxLowerLeftY!=0
                        || cropBoxUpperRightX!=0 || cropBoxUpperRightY!=0 )
                {
                    changeCropBoxes(document,
                            cropBoxLowerLeftX, cropBoxLowerLeftY,
                            cropBoxUpperRightX, cropBoxUpperRightY);
                }

                //Make the call
                PDFImageWriter imageWriter = new PDFImageWriter();
                boolean success = imageWriter.writeImage(document, imageFormat, password,
                        startPage, endPage, outputPrefix, imageType, resolution);
                if (!success)
                {
                    System.err.println( "Error: no writer found for image format '"
                            + imageFormat + "'" );
                    System.exit(1);
                }
            }
            catch (Exception e)
            {
                System.err.println(e);
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java -jar pdfbox-app-x.y.z.jar PDFToImage [OPTIONS] <PDF file>\n" +
            "  -password  <password>          Password to decrypt document\n" +
            "  -imageType <image type>        (" + getImageFormats() + ")\n" +
            "  -outputPrefix <output prefix>  Filename prefix for image files\n" +
            "  -startPage <number>            The first page to start extraction(1 based)\n" +
            "  -endPage <number>              The last page to extract(inclusive)\n" +
            "  -color <string>                The color depth (valid: bilevel, indexed, gray, rgb, rgba)\n" +
            "  -resolution <number>           The bitmap resolution in dpi\n" +
            "  -cropbox <number> <number> <number> <number> The page area to export\n" +
            "  <PDF file>                     The PDF document to use\n"
            );
        System.exit( 1 );
    }

    private static String getImageFormats()
    {
        StringBuffer retval = new StringBuffer();
        String[] formats = ImageIO.getReaderFormatNames();
        for( int i = 0; i < formats.length; i++ )
        {
            retval.append( formats[i] );
            if( i + 1 < formats.length )
            {
                retval.append( "," );
            }
        }
        return retval.toString();
    }

    private static void changeCropBoxes(PDDocument document,float a, float b, float c,float d)
    {
        List pages = document.getDocumentCatalog().getAllPages();
        for( int i = 0; i < pages.size(); i++ )
        {
              System.out.println("resizing page");
              PDPage page = (PDPage)pages.get( i );
              PDRectangle rectangle = new PDRectangle();
              rectangle.setLowerLeftX(a);
              rectangle.setLowerLeftY(b);
              rectangle.setUpperRightX(c);
              rectangle.setUpperRightY(d);
              page.setMediaBox(rectangle);
              page.setCropBox(rectangle);

        }
    }

}
