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

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

/**
 * Convert a PDF document to an image.
 *
 * @author Ben Litchfield
 */
public final class PDFToImage
{

    private static final Log LOG = LogFactory.getLog(PDFToImage.class);

    private static final String PASSWORD = "-password";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String PAGE = "-page";
    private static final String IMAGE_TYPE = "-imageType";
    private static final String FORMAT = "-format";
    private static final String OUTPUT_PREFIX = "-outputPrefix";
    private static final String PREFIX = "-prefix";
    private static final String COLOR = "-color";
    private static final String RESOLUTION = "-resolution";
    private static final String DPI = "-dpi";
    private static final String QUALITY = "-quality";
    private static final String CROPBOX = "-cropbox";
    private static final String TIME = "-time";

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
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        try
        {
            // force KCMS (faster than LCMS) if available
            Class.forName("sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        }
        catch (ClassNotFoundException e)
        {
            LOG.debug("KCMS service not found - using LCMS", e);
        }

        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        String password = "";
        String pdfFile = null;
        String outputPrefix = null;
        String imageFormat = "jpg";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        String color = "rgb";
        int dpi;
        float quality = 1.0f;
        float cropBoxLowerLeftX = 0;
        float cropBoxLowerLeftY = 0;
        float cropBoxUpperRightX = 0;
        float cropBoxUpperRightY = 0;
        boolean showTime = false;
        try
        {
            dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        catch( HeadlessException e )
        {
            dpi = 96;
        }
        for( int i = 0; i < args.length; i++ )
        {
            switch (args[i])
            {
                case PASSWORD:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    password = args[i];
                    break;
                case START_PAGE:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    startPage = Integer.parseInt(args[i]);
                    break;
                case END_PAGE:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    endPage = Integer.parseInt(args[i]);
                    break;
                case PAGE:
                    i++;
                    if (i >= args.length)
                    {
                        usage();
                    }
                    startPage = Integer.parseInt(args[i]);
                    endPage = Integer.parseInt(args[i]);
                    break;
                case IMAGE_TYPE:
                case FORMAT:
                    i++;
                    imageFormat = args[i];
                    break;
                case OUTPUT_PREFIX:
                case PREFIX:
                    i++;
                    outputPrefix = args[i];
                    break;
                case COLOR:
                    i++;
                    color = args[i];
                    break;
                case RESOLUTION:
                case DPI:
                    i++;
                    dpi = Integer.parseInt(args[i]);
                    break;
                case QUALITY:
                    i++;
                    quality = Float.parseFloat(args[i]);
                    break;
                case CROPBOX:
                    i++;
                    cropBoxLowerLeftX = Float.valueOf(args[i]);
                    i++;
                    cropBoxLowerLeftY = Float.valueOf(args[i]);
                    i++;
                    cropBoxUpperRightX = Float.valueOf(args[i]);
                    i++;
                    cropBoxUpperRightY = Float.valueOf(args[i]);
                    break;
                case TIME:
                    showTime = true;
                    break;
                default:
                    if (pdfFile == null)
                    {
                        pdfFile = args[i];
                    }
                    break;
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

            try (PDDocument document = PDDocument.load(new File(pdfFile), password))
            {
                ImageType imageType = null;
                if ("bilevel".equalsIgnoreCase(color))
                {
                    imageType = ImageType.BINARY;
                }
                else if ("gray".equalsIgnoreCase(color))
                {
                    imageType = ImageType.GRAY;
                }
                else if ("rgb".equalsIgnoreCase(color))
                {
                    imageType = ImageType.RGB;
                }
                else if ("rgba".equalsIgnoreCase(color))
                {
                    imageType = ImageType.ARGB;
                }
                
                if (imageType == null)
                {
                    System.err.println( "Error: Invalid color." );
                    System.exit( 2 );
                }

                //if a CropBox has been specified, update the CropBox:
                //changeCropBoxes(PDDocument document,float a, float b, float c,float d)
                if (Float.compare(cropBoxLowerLeftX, 0) !=0 ||
                    Float.compare(cropBoxLowerLeftY, 0) !=0 ||
                    Float.compare(cropBoxUpperRightX, 0) !=0 ||
                    Float.compare(cropBoxUpperRightY, 0) !=0 )
                {
                    changeCropBox(document,
                            cropBoxLowerLeftX, cropBoxLowerLeftY,
                            cropBoxUpperRightX, cropBoxUpperRightY);
                }

                long startTime = System.nanoTime();

                // render the pages
                boolean success = true;
                endPage = Math.min(endPage, document.getNumberOfPages());
                PDFRenderer renderer = new PDFRenderer(document);
                for (int i = startPage - 1; i < endPage; i++)
                {
                    BufferedImage image = renderer.renderImageWithDPI(i, dpi, imageType);
                    String fileName = outputPrefix + (i + 1) + "." + imageFormat;
                    success &= ImageIOUtil.writeImage(image, fileName, dpi, quality);
                }

                // performance stats
                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                int count = 1 + endPage - startPage;
                if (showTime)
                {
                    System.err.printf("Rendered %d page%s in %dms\n", count, count == 1 ? "" : "s",
                                      duration / 1000000);
                }

                if (!success)
                {
                    System.err.println( "Error: no writer found for image format '"
                            + imageFormat + "'" );
                    System.exit(1);
                }
            }
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar PDFToImage [options] <inputfile>\n"
            + "\nOptions:\n"
            + "  -password  <password>            : Password to decrypt document\n"
            + "  -format <string>                 : Image format: " + getImageFormats() + "\n"
            + "  -prefix <string>                 : Filename prefix for image files\n"
            + "  -page <number>                   : The only page to extract (1-based)\n"
            + "  -startPage <int>                 : The first page to start extraction (1-based)\n"
            + "  -endPage <int>                   : The last page to extract(inclusive)\n"
            + "  -color <int>                     : The color depth (valid: bilevel, gray, rgb (default), rgba)\n"
            + "  -dpi <int>                       : The DPI of the output image, default: screen resolution or 96 if unknown\n"
            + "  -quality <float>                 : The quality to be used when compressing the image (0 < quality <= 1 (default))\n"
            + "  -cropbox <int> <int> <int> <int> : The page area to export\n"
            + "  -time                            : Prints timing information to stdout\n"
            + "  <inputfile>                      : The PDF document to use\n";
        
        System.err.println(message);
        System.exit( 1 );
    }

    private static String getImageFormats()
    {
        StringBuilder retval = new StringBuilder();
        String[] formats = ImageIO.getReaderFormatNames();
        for( int i = 0; i < formats.length; i++ )
        {
           if (formats[i].equalsIgnoreCase(formats[i]))
           {
               retval.append( formats[i] );
               if( i + 1 < formats.length )
               {
                   retval.append( ", " );
               }
           }
        }
        return retval.toString();
    }

    private static void changeCropBox(PDDocument document, float a, float b, float c, float d)
    {
        for (PDPage page : document.getPages())
        {
            System.out.println("resizing page");
            PDRectangle rectangle = new PDRectangle();
            rectangle.setLowerLeftX(a);
            rectangle.setLowerLeftY(b);
            rectangle.setUpperRightX(c);
            rectangle.setUpperRightY(d);
            page.setCropBox(rectangle);
        }
    }
}
