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

import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

/**
 * This is implementation of the Type0 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class PDType0Font extends /*PDFont following is a hack ...*/ PDType1Font
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDType0Font.class);

    private PDFont descendentFont;
    /**
     * Constructor.
     */
    public PDType0Font()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.TYPE0 );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType0Font( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
     * {@inheritDoc}
     */
    public void drawString( String string, Graphics g, float fontSize, AffineTransform at, float x, float y ) 
        throws IOException
    {
        //throw new RuntimeException( "Not yet implemented" );
        super.drawString(string, g, fontSize, at, x, y);
        log.info("Called Type1Font.drawString since Type0 is not yet implemented");
    }

    /**
     * This will get the fonts bouding box.
     *
     * @return The fonts bouding box.
     *
     * @throws IOException If there is an error getting the bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontWidth( byte[] c, int offset, int length ) throws IOException
    {
        if (descendentFont == null) 
        {
            COSArray descendantFontArray =
                (COSArray)font.getDictionaryObject( COSName.DESCENDANT_FONTS );
            
            COSDictionary descendantFontDictionary = (COSDictionary)descendantFontArray.getObject( 0 );
            descendentFont = PDFontFactory.createFont( descendantFontDictionary );
        }
        return descendentFont.getFontWidth( c, offset, length );
    }

    /**
     * This will get the font height for a character.
     *
     * @param c The character code to get the height for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontHeight( byte[] c, int offset, int length ) throws IOException
    {
        if (descendentFont == null) 
        {
            COSArray descendantFontArray =
                (COSArray)font.getDictionaryObject( COSName.DESCENDANT_FONTS );
            
            COSDictionary descendantFontDictionary = (COSDictionary)descendantFontArray.getObject( 0 );
            descendentFont = PDFontFactory.createFont( descendantFontDictionary );
        }
        return descendentFont.getFontHeight( c, offset, length );
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getAverageFontWidth() throws IOException
    {
        if (descendentFont == null) 
        {
            COSArray descendantFontArray =
                (COSArray)font.getDictionaryObject( COSName.DESCENDANT_FONTS );
            
            COSDictionary descendantFontDictionary = (COSDictionary)descendantFontArray.getObject( 0 );
            descendentFont = PDFontFactory.createFont( descendantFontDictionary );
        }
        return descendentFont.getAverageFontWidth();
    }
}
