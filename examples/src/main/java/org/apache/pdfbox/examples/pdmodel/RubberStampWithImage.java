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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;


/**
 * This is an example on how to add a rubber stamp annotation with an image to pages of a PDF
 * document. To add a normal image, use the AddImageToPDF.java example.
 */
public class RubberStampWithImage
{
    private static final String SAVE_GRAPHICS_STATE = "q\n";
    private static final String RESTORE_GRAPHICS_STATE = "Q\n";
    private static final String CONCATENATE_MATRIX = "cm\n";
    private static final String XOBJECT_DO = "Do\n";
    private static final String SPACE = " ";

    private static final NumberFormat FORMATDECIMAL = NumberFormat.getNumberInstance( Locale.US );

    /**
     * Add a rubber stamp with an jpg image to every page of the given document.
     * @param args the command line arguments
     * @throws IOException an exception is thrown if something went wrong
     */
    public void doIt(final String[] args ) throws IOException
    {
        if( args.length != 3 )
        {
            usage();
        }
        else 
        {
            try (PDDocument document = Loader.loadPDF(new File(args[0])))
            {
                if( document.isEncrypted() )
                {
                    throw new IOException( "Encrypted documents are not supported for this example" );
                }
    
                for (int i = 0; i < document.getNumberOfPages(); i++)
                {
                    final PDPage page = document.getPage(i);
                    final List<PDAnnotation> annotations = page.getAnnotations();
                    final PDAnnotationRubberStamp rubberStamp = new PDAnnotationRubberStamp();
                    rubberStamp.setName(PDAnnotationRubberStamp.NAME_TOP_SECRET);
                    rubberStamp.setRectangle(new PDRectangle(200,100));
                    rubberStamp.setContents("A top secret note");

                    // create a PDXObjectImage with the given image file
                    // if you already have the image in a BufferedImage, 
                    // call LosslessFactory.createFromImage() instead
                    final PDImageXObject ximage = PDImageXObject.createFromFile(args[2], document);

                    // define and set the target rectangle
                    final float lowerLeftX = 250;
                    final float lowerLeftY = 550;
                    final float formWidth = 150;
                    final float formHeight = 25;
                    final float imgWidth = 50;
                    final float imgHeight = 25;
                    
                    final PDRectangle rect = new PDRectangle();
                    rect.setLowerLeftX(lowerLeftX);
                    rect.setLowerLeftY(lowerLeftY);
                    rect.setUpperRightX(lowerLeftX + formWidth);
                    rect.setUpperRightY(lowerLeftY + formHeight);

                    // Create a PDFormXObject
                    final PDFormXObject form = new PDFormXObject(document);
                    form.setResources(new PDResources());
                    form.setBBox(rect);
                    form.setFormType(1);

                    // adjust the image to the target rectangle and add it to the stream
                    try (OutputStream os = form.getStream().createOutputStream())
                    {
                        drawXObject(ximage, form.getResources(), os, lowerLeftX, lowerLeftY, imgWidth, imgHeight);
                    }

                    final PDAppearanceStream myDic = new PDAppearanceStream(form.getCOSObject());
                    final PDAppearanceDictionary appearance = new PDAppearanceDictionary(new COSDictionary());
                    appearance.setNormalAppearance(myDic);
                    rubberStamp.setAppearance(appearance);
                    rubberStamp.setRectangle(rect);

                    // add the new RubberStamp to the document
                    annotations.add(rubberStamp);
                
                }
                document.save( args[1] );
            }
        }
    }
    
    private void drawXObject(final PDImageXObject xobject, final PDResources resources, final OutputStream os,
                             final float x, final float y, final float width, final float height ) throws IOException
    {
        // This is similar to PDPageContentStream.drawXObject()
        final COSName xObjectId = resources.add(xobject);

        appendRawCommands( os, SAVE_GRAPHICS_STATE );
        appendRawCommands( os, FORMATDECIMAL.format( width ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, FORMATDECIMAL.format( 0 ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, FORMATDECIMAL.format( 0 ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, FORMATDECIMAL.format( height ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, FORMATDECIMAL.format( x ) );
        appendRawCommands( os, SPACE );
        appendRawCommands( os, FORMATDECIMAL.format( y ) );
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

    private void appendRawCommands(final OutputStream os, final String commands) throws IOException
    {
        os.write( commands.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * This creates an instance of RubberStampWithImage.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main(final String[] args ) throws IOException
    {
        final RubberStampWithImage rubberStamp = new RubberStampWithImage();
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
