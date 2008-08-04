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
package org.pdfbox.pdmodel.font;

import java.awt.Graphics;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.pdmodel.common.PDRectangle;

/**
 * This is implementation for the CIDFontType0/CIDFontType2 Fonts.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.11 $
 */
public abstract class PDCIDFont extends PDFont
{
    private Map widthCache = new HashMap();

    /**
     * Constructor.
     */
    public PDCIDFont()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFont( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }
    
    /**
     * Get the font descriptor associated with this CID font.
     * 
     * @return The font descriptor.
     */
    public PDFontDescriptor getFontDescriptor()
    {
        PDFontDescriptor desc = null;
        COSDictionary dict = (COSDictionary)font.getDictionaryObject( "FontDescriptor" );
        if( dict != null )
        {
            desc = new PDFontDescriptorDictionary( dict );
        }
        return desc;
    }
    
    /**
     * Set the font descriptor associated with this CID Font.
     * 
     * @param desc The font descriptor.
     */
    public void setFontDescriptor( PDFontDescriptorDictionary desc )
    {
        font.setItem( "FontDescriptor", desc );
    }

    /**
     * {@inheritDoc}
     */
    public void drawString( String string, Graphics g, float fontSize, 
        float xScale, float yScale, float x, float y )
    {
        throw new RuntimeException( "Not yet implemented" );
    }
    
    /**
     * This will get the fonts bouding box.
     *
     * @return The fonts bouding box.
     * 
     * @throws IOException If there is an error getting the font bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * This will get the default width.  The default value for the default width is 1000.
     *
     * @return The default width for the glyphs in this font.
     */
    public long getDefaultWidth()
    {
        long dw = 1000;
        COSNumber number = (COSNumber)font.getDictionaryObject( COSName.getPDFName( "DW" ) );
        if( number != null )
        {
            dw = number.intValue();
        }
        return dw;
    }

    /**
     * This will set the default width for the glyphs of this font.
     *
     * @param dw The default width.
     */
    public void setDefaultWidth( long dw )
    {
        font.setItem( COSName.getPDFName( "DW" ), new COSInteger( dw ) );
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

        float retval = 0.0f;
        int code = getCodeFromArray( c, offset, length );

        Float widthFloat = (Float)widthCache.get( new Integer( code ) );
        if( widthFloat == null )
        {
            COSArray widths = (COSArray)font.getDictionaryObject( COSName.getPDFName( "W" ) );

            if( widths != null )
            {
                boolean foundWidth = false;
                for( int i=0; !foundWidth && i<widths.size(); i++ )
                {
                    COSNumber firstCode = (COSNumber)widths.getObject( i++ );
                    COSBase next = widths.getObject( i );
                    if( next instanceof COSArray )
                    {
                        COSArray array = (COSArray)next;
                        if( code >= firstCode.intValue() &&
                            code < firstCode.intValue() + array.size() )
                        {
                            COSNumber rangeWidth =
                                (COSNumber)array.get( code - firstCode.intValue() );
                            retval = rangeWidth.floatValue();
                            foundWidth = true;
                        }
                    }
                    else
                    {
                        COSNumber secondCode = (COSNumber)next;
                        i++;
                        COSNumber rangeWidth = (COSNumber)widths.getObject( i );
                        if( code >= firstCode.intValue() &&
                            code <= secondCode.intValue() )
                        {
                            retval = rangeWidth.floatValue();
                            foundWidth = true;
                        }
                    }
                }
                widthCache.put( new Integer( code ), new Float( retval ) );
            }
        }
        else
        {
            retval = widthFloat.floatValue();
        }
        return retval;
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
        float retval = 0;
        PDFontDescriptor desc = getFontDescriptor();
        float xHeight = desc.getXHeight();
        float capHeight = desc.getCapHeight();
        if( xHeight != 0f && capHeight != 0 )
        {
            //do an average of these two.  Can we do better???
            retval = (xHeight + capHeight)/2f;
        }
        else if( xHeight != 0 )
        {
            retval = xHeight;
        }
        else if( capHeight != 0 )
        {
            retval = capHeight;
        }
        else
        {
            retval = 0;
        }
        if( retval == 0 )
        {
            retval = desc.getAscent();
        }
        return retval;
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
        float totalWidths = 0.0f;
        float characterCount = 0.0f;
        float defaultWidth = getDefaultWidth();
        COSArray widths = (COSArray)font.getDictionaryObject( COSName.getPDFName( "W" ) );

        if( widths != null )
        {
            for( int i=0; i<widths.size(); i++ )
            {
                COSNumber firstCode = (COSNumber)widths.getObject( i++ );
                COSBase next = widths.getObject( i );
                if( next instanceof COSArray )
                {
                    COSArray array = (COSArray)next;
                    for( int j=0; j<array.size(); j++ )
                    {
                        COSNumber width = (COSNumber)array.get( j );
                        totalWidths+=width.floatValue();
                        characterCount += 1;
                    }
                }
                else
                {
                    i++;
                    COSNumber rangeWidth = (COSNumber)widths.getObject( i );
                    if( rangeWidth.floatValue() > 0 )
                    {
                        totalWidths += rangeWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }
        }
        float average = totalWidths / characterCount;
        if( average <= 0 )
        {
            average = defaultWidth;
        }
        return average;
    }
}