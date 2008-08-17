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
package org.pdfbox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.pdfbox.exceptions.InvalidPasswordException;

import org.pdfbox.pdmodel.PDDocument;
//import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.util.PDFImageWriter;

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
    private static final String IMAGE_TYPE = "-imageType";
    private static final String OUTPUT_PREFIX = "-outputPrefix";

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
        String imageType = "jpg";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
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
            else if( args[i].equals( IMAGE_TYPE ) )
            {
                i++;
                imageType = args[i];
            }
            else if( args[i].equals( OUTPUT_PREFIX ) )
            {
                i++;
                outputPrefix = args[i];
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
                            //they didn't suppply a password and the default of "" was wrong.
                            System.err.println( "Error: The document is encrypted." );
                            usage();
                        }
                    }
                }

		//Make the call
		PDFImageWriter W = new PDFImageWriter();
		W.WriteImage(document, imageType, password, startPage, endPage, outputPrefix);
	    } catch(Exception e){
		    System.err.println( e);
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
        System.err.println( "Usage: java org.pdfbox.PDFToImage [OPTIONS] <PDF file>\n" +
            "  -password  <password>          Password to decrypt document\n" +
            "  -imageType <image type>        (" + getImageFormats() + ")\n" +
            "  -outputPrefix <output prefix>  Filename prefix for image files\n" +
            "  -startPage <number>          The first page to start extraction(1 based)\n" +
            "  -endPage <number>            The last page to extract(inclusive)\n" +
            "  <PDF file>                   The PDF document to use\n"
            );
        System.exit( 1 );
    }

    private static String getImageFormats()
    {
        StringBuffer retval = new StringBuffer();
        String[] formats = ImageIO.getReaderFormatNames();
        for( int i=0; i<formats.length; i++ )
        {
            retval.append( formats[i] );
            if( i+1<formats.length )
            {
                retval.append( "," );
            }
        }
        return retval.toString();
    }
}
