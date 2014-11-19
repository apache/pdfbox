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
package org.apache.pdfbox.examples.pdmodel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;


/**
 * This is an example on how to add a rubber stamp with an image to pages of a PDF document.
 * @author Andreas Lehmkühler
 */
public class RubberStampWithImage
{
    private static final String SAVE_GRAPHICS_STATE = "q\n";
    private static final String RESTORE_GRAPHICS_STATE = "Q\n";
    private static final String CONCATENATE_MATRIX = "cm\n";
    private static final String XOBJECT_DO = "Do\n";
    private static final String SPACE = " ";

    private static final NumberFormat formatDecimal = NumberFormat.getNumberInstance( Locale.US );

    /**
     * Add a rubber stamp with an jpg image to every page of the given document.
     * @param args the command line arguments
     * @throws IOException an exception is thrown if something went wrong
     */
    public void doIt( String[] args ) throws IOException
    {
        if( args.length != 3 )
        {
            usage();
        }
        else 
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load( new File(args[0]) );
                if( document.isEncrypted() )
                {
                    throw new IOException( "Encrypted documents are not supported for this example" );
                }
    
                for (int i = 0; i < document.getNumberOfPages(); i++)
                {
                    PDPage page = document.getPage(i);
                    List<PDAnnotation> annotations = page.getAnnotations();
                    PDAnnotationRubberStamp rubberStamp = new PDAnnotationRubberStamp();
                    rubberStamp.setName(PDAnnotationRubberStamp.NAME_TOP_SECRET);
                    rubberStamp.setRectangle(new PDRectangle(200,100));
                    rubberStamp.setContents("A top secret note");

                    // create a PDXObjectImage with the given image file
                    String imageFilename = args[2];
                    PDImageXObject ximage;
                    if( imageFilename.toLowerCase().endsWith( ".jpg" ) )
                    {
                        ximage = JPEGFactory.createFromStream(document, new FileInputStream(imageFilename));
                    }
                    else if (imageFilename.toLowerCase().endsWith(".tif") || imageFilename.toLowerCase().endsWith(".tiff"))
                    {
                        ximage = CCITTFactory.createFromRandomAccess(document, new RandomAccessFile(new File(imageFilename),"r"));
                    }
                    else if (imageFilename.toLowerCase().endsWith(".gif") || 
                            imageFilename.toLowerCase().endsWith(".bmp") || 
                            imageFilename.toLowerCase().endsWith(".png"))
                    {
                        BufferedImage bim = ImageIO.read(new File(imageFilename));
                        ximage = LosslessFactory.createFromImage(document, bim);
                    }
                    else
                    {
                        throw new IOException( "Image type not supported: " + imageFilename );
                    }                    

                    // define and set the target rectangle
                    int lowerLeftX = 250;
                    int lowerLeftY = 550;
                    int formWidth = 150;
                    int formHeight = 25;
                    int imgWidth = 50;
                    int imgHeight = 25;
                    
                    PDRectangle rect = new PDRectangle();
                    rect.setLowerLeftX(lowerLeftX);
                    rect.setLowerLeftY(lowerLeftY);
                    rect.setUpperRightX(lowerLeftX + formWidth);
                    rect.setUpperRightY(lowerLeftY + formHeight);

                    // Create a PDFormXObject
                    PDStream stream = new PDStream(document);
                    OutputStream os = stream.createOutputStream();
                    PDFormXObject form = new PDFormXObject(stream);
                    form.setResources(new PDResources());
                    form.setBBox(rect);
                    form.setFormType(1);

                    // adjust the image to the target rectangle and add it to the stream
                    drawXObject(ximage, form.getResources(), os, lowerLeftX, lowerLeftY, imgWidth, imgHeight);
                    os.close();

                    PDAppearanceStream myDic = new PDAppearanceStream(form.getCOSStream());
                    PDAppearanceDictionary appearance = new PDAppearanceDictionary(new COSDictionary());
                    appearance.setNormalAppearance(myDic);
                    rubberStamp.setAppearance(appearance);
                    rubberStamp.setRectangle(rect);

                    // add the new RubberStamp to the document
                    annotations.add(rubberStamp);
                
                }
                document.save( args[1] );
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
    
    private void drawXObject( PDImageXObject xobject, PDResources resources, OutputStream os,
            float x, float y, float width, float height ) throws IOException
    {
        // This is similar to PDPageContentStream.drawXObject()
        COSName xObjectId = resources.add(xobject);

        appendRawCommands( os, SAVE_GRAPHICS_STATE );
        appendRawCommands( os, formatDecimal.format( width ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, formatDecimal.format( 0 ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, formatDecimal.format( 0 ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, formatDecimal.format( height ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, formatDecimal.format( x ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, formatDecimal.format( y ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, CONCATENATE_MATRIX );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, "/" );
        appendRawCommands( os, xObjectId.getName() );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, XOBJECT_DO );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, RESTORE_GRAPHICS_STATE );
    }

    private void appendRawCommands(OutputStream os, String commands) throws IOException
    {
        os.write( commands.getBytes("ISO-8859-1"));
    }

    /**
     * This creates an instance of RubberStampWithImage.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        RubberStampWithImage rubberStamp = new RubberStampWithImage();
        rubberStamp.doIt(args);
    }

    /**
     * This will print the usage for this example.
     */
    private void usage()
    {
        System.err.println( "Usage: java "+getClass().getName()+" <input-pdf> <output-pdf> <image-filename>" );
    }
}
