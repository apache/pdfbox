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
package org.pdfbox.pdmodel.graphics.xobject;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.PDStream;
import org.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * An image class for JPegs. 
 * 
 * @author mathiak
 * @version $Revision: 1.5 $
 */
public class PDJpeg extends PDXObjectImage 
{
    
    private static final List DCT_FILTERS = new ArrayList();
    
    static
    {
        DCT_FILTERS.add( COSName.DCT_DECODE.getName() );
        DCT_FILTERS.add( COSName.DCT_DECODE_ABBREVIATION.getName() );
    }
    
    /**
     * Standard constructor.
     * 
     * @param jpeg The COSStream from which to extract the JPeg
     */
    public PDJpeg(PDStream jpeg) 
    {
        super(jpeg, "jpg");
    }
    
    /**
     * Construct from a stream.
     * 
     * @param doc The document to create the image as part of.
     * @param is The stream that contains the jpeg data.
     * @throws IOException If there is an error reading the jpeg data.
     */
    public PDJpeg( PDDocument doc, InputStream is ) throws IOException
    {
        super( new PDStream( doc, is, true ), "jpg" );
        COSDictionary dic = getCOSStream();
        dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
        dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
        dic.setItem( COSName.TYPE, COSName.getPDFName( "XObject" ) );
        
        BufferedImage image = getRGBImage();
        setBitsPerComponent( 8 );
        setColorSpace( PDDeviceRGB.INSTANCE );
        setHeight( image.getHeight() );
        setWidth( image.getWidth() );
        
    }
    
    /**
     * Construct from a buffered image.
     * 
     * @param doc The document to create the image as part of.
     * @param bi The image to convert to a jpeg
     * @throws IOException If there is an error processing the jpeg data.
     */
    public PDJpeg( PDDocument doc, BufferedImage bi ) throws IOException
    {
        super( new PDStream( doc ), "jpg" );
        
        java.io.OutputStream os = getCOSStream().createFilteredStream();
        try 
        {
            
            ImageIO.write(bi,"jpeg",os);
            
            COSDictionary dic = getCOSStream();
            dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
            dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
            dic.setItem( COSName.TYPE, COSName.getPDFName( "XObject" ) );
        
            setBitsPerComponent( 8 );
            setColorSpace( PDDeviceRGB.INSTANCE );
            setHeight( bi.getHeight() );
            setWidth( bi.getWidth() );
        }
        finally 
        {
            os.close();
        }
    }
    
    /**
     * Returns an image of the JPeg, or null if JPegs are not supported. (They should be. )
     * {@inheritDoc}
     */
    public BufferedImage getRGBImage() throws IOException
    {
        return ImageIO.read(getPDStream().getPartiallyFilteredStream( DCT_FILTERS ));
    }
    
    /**
     * This writes the JPeg to out. 
     * {@inheritDoc}
     */
    public void write2OutputStream(OutputStream out) throws IOException
    {
        InputStream data = getPDStream().getPartiallyFilteredStream( DCT_FILTERS );
        byte[] buf = new byte[1024];
        int amountRead = -1;
        while( (amountRead = data.read( buf )) != -1 )
        {
            out.write( buf, 0, amountRead );
        }
    }
}
