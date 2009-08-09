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
package org.apache.pdfbox.pdmodel.font;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDMatrix;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/**
 * This is implementation of the Type3 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class PDType3Font extends PDSimpleFont
{
    //A map of character code to java.awt.Image for the glyph
    private Map images = new HashMap();

    /**
     * Constructor.
     */
    public PDType3Font()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.TYPE3 );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType3Font( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
     * Type3 fonts have their glyphs defined as a content stream.  This
     * will create the image that represents that character
     *
     * @throws IOException If there is an error creating the image.
     */
    private Image createImageIfNecessary( char character ) throws IOException
    {
        Character c = new Character( character );
        Image retval = (Image)images.get( c );
        if( retval == null )
        {
            COSDictionary charProcs = (COSDictionary)font.getDictionaryObject( COSName.CHAR_PROCS );
            COSStream stream = (COSStream)charProcs.getDictionaryObject( COSName.getPDFName( "" + character ) );
            if( stream != null )
            {
                Type3StreamParser parser = new Type3StreamParser();
                retval = parser.createImage( stream );
                images.put( c, retval );
            }
            else
            {
                //stream should not be null!!
            }
        }
        return retval;

    }

    /**
     * {@inheritDoc}
     */
    public void drawString( String string, Graphics g, float fontSize, AffineTransform at, float x, float y ) 
        throws IOException
    {
        //if( string.equals( "V" )|| string.equals( "o" ) )
        {
            for(int i=0; i<string.length(); i++)
            {
                //todo need to use image observers and such
                char c = string.charAt( i );
                Image image = createImageIfNecessary( c );
                if( image != null )
                {
                    int newWidth = (int)(.12*image.getWidth(null));
                    int newHeight = (int)(.12*image.getHeight(null));
                    if( newWidth > 0 && newHeight > 0 )
                    {
                        image = image.getScaledInstance( newWidth, newHeight, Image.SCALE_SMOOTH );
                        g.drawImage( image, (int)x, (int)y, null );
                        x+=newWidth;
                    }
                }
            }
        }
    }

    /**
     * Set the font matrix for this type3 font.
     *
     * @param matrix The font matrix for this type3 font.
     */
    public void setFontMatrix( PDMatrix matrix )
    {
        font.setItem( "FontMatrix", matrix );
    }
}
