/**
 * Copyright (c) 2003-2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.font;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;
import org.pdfbox.pdmodel.common.PDMatrix;

import java.awt.Graphics;
import java.awt.Image;

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
        font.setItem( COSName.SUBTYPE, COSName.getPDFName( "Type3" ) );
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
            COSDictionary charProcs = (COSDictionary)font.getDictionaryObject( COSName.getPDFName( "CharProcs" ) );
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
     * This will draw a string on a canvas using the font.
     *
     * @param string The string to draw.
     * @param g The graphics to draw onto.
     * @param fontSize The size of the font to draw.
     * @param x The x coordinate to draw at.
     * @param y The y coordinate to draw at.
     *
     * @throws IOException If there is an error drawing the image on the screen.
     */
    public void drawString( String string, Graphics g, float fontSize, float x, float y ) throws IOException
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